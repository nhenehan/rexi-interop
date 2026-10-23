// nagasena-cs-driver: Nagasena for .NET (OpenEXI's C# port) behind the same
// command line as rexi bench and the exip driver, so the harness can drive it.
//
//   nagasena-cs-driver encode --xsc schema.xsc [stream options] [bench] --out out.exi in.xml
//   nagasena-cs-driver decode --xsc schema.xsc [stream options] [bench] --events-out out.events in.exi
//
// stream options: --strict, --alignment bit-packed|byte-aligned|compression
// bench: --warmup N (default 20), --warmup-ms MS (default 0), --iterations N (default 200)
//
// Warm-up runs at least N times and for at least MS milliseconds. .NET's tiered
// JIT only optimizes a method after about 100 ms in which nothing new was
// compiled, so a warm-up counted in runs can end before the optimized code is in.
//
// The C# port has no schema compiler, only a reader for compiled schemas. The
// harness compiles each case's XSD with Java Nagasena (EXISchema.writeOut) and
// passes the result here as --xsc.
//
// One JSON object goes to stdout: {"input": ..., "setup_ns": ..., "ns": [...]}.
// The timing loop runs in this process, so .NET start-up is never measured.

using System;
using System.Collections.Generic;
using System.Diagnostics;
using System.Globalization;
using System.IO;
using System.Text;
using Nagasena.Proc;
using Nagasena.Proc.Common;
using Nagasena.Proc.Events;
using Nagasena.Proc.Grammars;
using Nagasena.Proc.IO;
using Nagasena.Sax;
using Nagasena.Schema;
using Org.System.Xml.Sax;

internal static class Driver {
    private const string Xsi = "http://www.w3.org/2001/XMLSchema-instance";

    private static int Main(string[] argv) {
        try {
            return Run(argv);
        } catch (Exception e) {
            // the harness shows the first lines of stderr in the report
            Console.Error.WriteLine($"nagasena-cs-driver: {e.GetType().Name}: {e.Message}");
            return 1;
        }
    }

    private static int Run(string[] argv) {
        if (argv.Length < 1 || (argv[0] != "encode" && argv[0] != "decode")) {
            Console.Error.WriteLine("usage: nagasena-cs-driver encode|decode --xsc F [--strict] "
                + "[--alignment A] [--warmup N] [--warmup-ms MS] [--iterations N] (--out F | --events-out F) INPUT");
            return 2;
        }
        var opts = new Dictionary<string, string>();
        string? input = null;
        bool strict = false;
        for (int i = 1; i < argv.Length; i++) {
            if (argv[i] == "--strict") {
                strict = true;
            } else if (argv[i].StartsWith("--", StringComparison.Ordinal) && i + 1 < argv.Length) {
                opts[argv[i]] = argv[++i];
            } else if (input == null) {
                input = argv[i];
            } else {
                throw new ArgumentException($"unexpected argument {argv[i]}");
            }
        }
        if (input == null || !opts.TryGetValue("--xsc", out string? xsc)) {
            throw new ArgumentException("--xsc and an input file are required");
        }
        int warmup = int.Parse(opts.GetValueOrDefault("--warmup", "20"), CultureInfo.InvariantCulture);
        long warmupMs = long.Parse(opts.GetValueOrDefault("--warmup-ms", "0"), CultureInfo.InvariantCulture);
        int iterations = int.Parse(opts.GetValueOrDefault("--iterations", "200"), CultureInfo.InvariantCulture);
        AlignmentType alignment = opts.GetValueOrDefault("--alignment", "bit-packed") switch {
            "bit-packed" => AlignmentType.bitPacked,
            "byte-aligned" => AlignmentType.byteAligned,
            "compression" => AlignmentType.compress,
            var a => throw new ArgumentException($"unknown alignment {a}"),
        };

        long t0 = Stopwatch.GetTimestamp();
        EXISchema schema;
        using (var s = File.OpenRead(xsc)) {
            schema = EXISchema.readIn(s);
        }
        var grammars = new GrammarCache(schema, strict ? GrammarOptions.STRICT_OPTIONS : GrammarOptions.DEFAULT_OPTIONS);
        byte[] data = File.ReadAllBytes(input);

        if (argv[0] == "encode") {
            string output = opts.GetValueOrDefault("--out") ?? throw new ArgumentException("--out is required");
            var t = new Transmogrifier {
                GrammarCache = grammars,
                AlignmentType = alignment,
                OutputOptions = HeaderOptionsOutputType.none,
                OutputCookie = false,
            };
            long setup = Elapsed(t0);
            var buffer = new MemoryStream(1024);
            void Encode() {
                buffer.SetLength(0);
                t.OutputStream = buffer;
                t.encode(new InputSource<Stream>(new MemoryStream(data, false)));
            }
            Encode();
            File.WriteAllBytes(output, buffer.ToArray());
            Report("xml", setup, Time(warmup, warmupMs, iterations, Encode));
        } else {
            string output = opts.GetValueOrDefault("--events-out") ?? throw new ArgumentException("--events-out is required");
            var d = new EXIDecoder { GrammarCache = grammars, AlignmentType = alignment };
            long setup = Elapsed(t0);
            File.WriteAllText(output, Events(d, data), new UTF8Encoding(false));
            Report("exi", setup, Time(warmup, warmupMs, iterations, () => {
                d.InputStream = new MemoryStream(data, false);
                Scanner scanner = d.processHeader();
                while (scanner.nextEvent() != null) {
                    // only count: the loop measures decoding, not event handling
                }
            }));
        }
        return 0;
    }

    private static long Elapsed(long start) =>
        (long)Stopwatch.GetElapsedTime(start).TotalNanoseconds;

    private static long[] Time(int warmup, long warmupMs, int iterations, Action op) {
        long start = Stopwatch.GetTimestamp();
        for (long i = 0; i < warmup || Stopwatch.GetElapsedTime(start).TotalMilliseconds < warmupMs; i++) {
            op();
        }
        var ns = new long[iterations];
        for (int i = 0; i < iterations; i++) {
            long t = Stopwatch.GetTimestamp();
            op();
            ns[i] = Elapsed(t);
        }
        return ns;
    }

    private static void Report(string input, long setupNs, long[] ns) {
        var b = new StringBuilder();
        b.Append("{\"input\":\"").Append(input).Append("\",\"setup_ns\":").Append(setupNs).Append(",\"ns\":[");
        for (int i = 0; i < ns.Length; i++) {
            b.Append(i == 0 ? "" : ",").Append(ns[i]);
        }
        Console.Out.Write(b.Append("]}\n").ToString());
    }

    // The .events format of the harness (Model.Ev): one event per line, xsi:type and
    // xsi:nil first, other attributes sorted by local name then namespace, adjacent
    // text merged, whitespace-only text dropped, namespace declarations left out.
    private static string Events(EXIDecoder d, byte[] exi) {
        d.InputStream = new MemoryStream(exi, false);
        Scanner scanner = d.processHeader();
        var lines = new StringBuilder();
        var text = new StringBuilder();
        string? xsiType = null;
        string? xsiNil = null;
        var attrs = new List<(string Local, string Uri, string Value)>();
        bool inStartTag = false;

        void FlushStartTag() {
            if (!inStartTag) {
                return;
            }
            if (xsiType != null) {
                lines.Append("XSI_TYPE ").Append(xsiType).Append('\n');
            }
            if (xsiNil != null) {
                lines.Append("XSI_NIL ").Append(xsiNil).Append('\n');
            }
            attrs.Sort((a, b) => {
                int c = string.CompareOrdinal(a.Local, b.Local);
                return c != 0 ? c : string.CompareOrdinal(a.Uri, b.Uri);
            });
            foreach (var a in attrs) {
                lines.Append("AT {").Append(a.Uri).Append('}').Append(a.Local).Append(' ')
                    .Append(Escape(a.Value)).Append('\n');
            }
            xsiType = null;
            xsiNil = null;
            attrs.Clear();
            inStartTag = false;
        }

        void FlushText() {
            if (text.Length > 0) {
                string s = text.ToString();
                if (!string.IsNullOrWhiteSpace(s)) {
                    lines.Append("CH ").Append(Escape(s)).Append('\n');
                }
                text.Clear();
            }
        }

        EventDescription ev;
        while ((ev = scanner.nextEvent()) != null) {
            switch (ev.EventKind) {
                case EventDescription_Fields.EVENT_SD:
                    lines.Append("SD\n");
                    break;
                case EventDescription_Fields.EVENT_ED:
                    lines.Append("ED\n");
                    break;
                case EventDescription_Fields.EVENT_SE:
                    FlushStartTag();
                    FlushText();
                    lines.Append("SE {").Append(ev.URI).Append('}').Append(ev.Name).Append('\n');
                    inStartTag = true;
                    break;
                case EventDescription_Fields.EVENT_TP:
                    var tp = (EXIEventSchemaType)ev;
                    xsiType = "{" + tp.TypeURI + "}" + tp.TypeName;
                    break;
                case EventDescription_Fields.EVENT_NL:
                    xsiNil = ((EXIEventSchemaNil)ev).Nilled ? "true" : "false";
                    break;
                case EventDescription_Fields.EVENT_AT:
                    if (ev.URI == Xsi && ev.Name == "type") {
                        // an untyped xsi:type in non-strict streams
                        xsiType = ev.Characters.makeString();
                    } else {
                        attrs.Add((ev.Name, ev.URI, ev.Characters.makeString()));
                    }
                    break;
                case EventDescription_Fields.EVENT_CH:
                    FlushStartTag();
                    Characters ch = ev.Characters;
                    text.Append(ch.characters, ch.startIndex, ch.length);
                    break;
                case EventDescription_Fields.EVENT_EE:
                    FlushStartTag();
                    FlushText();
                    lines.Append("EE\n");
                    break;
                default:
                    // NS, CM, PI, DTD, ER: not part of the comparison
                    break;
            }
        }
        return lines.ToString();
    }

    private static string Escape(string s) =>
        s.Replace("\\", "\\\\").Replace("\n", "\\n").Replace("\r", "\\r").Replace("\t", "\\t");
}
