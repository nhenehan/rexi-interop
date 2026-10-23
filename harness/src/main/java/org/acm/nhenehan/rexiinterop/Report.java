package org.acm.nhenehan.rexiinterop;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.acm.nhenehan.rexiinterop.Main.CaseRun;
import org.acm.nhenehan.rexiinterop.Main.TimingRow;
import org.acm.nhenehan.rexiinterop.Model.Bench;
import org.acm.nhenehan.rexiinterop.Model.Case;
import org.acm.nhenehan.rexiinterop.Model.Outcome;
import org.acm.nhenehan.rexiinterop.Model.Profile;
import org.acm.nhenehan.rexiinterop.Model.Status;

/** Writes reports/latest.md (for people) and reports/latest.json (for tools). */
final class Report {
    private Report() {}

    static void write(Path out, List<CaseRun> runs, List<Codec> codecs, List<Profile> profiles, List<Case> cases,
            Bench bench, boolean timing, List<String> meta) throws IOException {
        Files.createDirectories(out);
        // getBytes replaces malformed UTF-16 (a decoder that splits a surrogate pair) with '?';
        // Files.writeString would throw and lose the whole report
        Files.write(out.resolve("latest.md"),
                markdown(runs, codecs, profiles, cases, bench, timing, meta).getBytes(StandardCharsets.UTF_8));
        ObjectMapper m = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
        Files.write(out.resolve("latest.json"),
                m.writeValueAsString(json(m, runs, meta, bench, timing)).getBytes(StandardCharsets.UTF_8));
    }

    private static String cpu() {
        try {
            for (String line : Files.readAllLines(Path.of("/proc/cpuinfo"))) {
                if (line.startsWith("model name")) {
                    return line.substring(line.indexOf(':') + 1).strip();
                }
            }
        } catch (IOException ignored) {
            // not Linux
        }
        return System.getProperty("os.arch");
    }

    private static String markdown(List<CaseRun> runs, List<Codec> codecs, List<Profile> profiles,
            List<Case> cases, Bench bench, boolean timing, List<String> meta) {
        StringBuilder b = new StringBuilder();
        List<String> notes = new ArrayList<>();
        Map<String, Integer> noteIds = new LinkedHashMap<>();
        java.util.function.Function<String, String> note = msg -> {
            Integer id = noteIds.get(msg);
            if (id == null) {
                notes.add(msg);
                id = notes.size();
                noteIds.put(msg, id);
            }
            return "[" + id + "]";
        };

        b.append("# EXI interop and benchmark report\n\n");
        b.append("Generated ").append(Instant.now().truncatedTo(ChronoUnit.SECONDS)).append(" on ")
                .append(cpu()).append(", ").append(System.getProperty("os.name")).append(' ')
                .append(System.getProperty("os.version")).append(", Java ")
                .append(System.getProperty("java.version")).append(".\n\n");
        for (String m : meta) {
            int eq = m.indexOf('=');
            b.append("- **").append(eq < 0 ? m : m.substring(0, eq)).append("**: ")
                    .append(eq < 0 ? "" : m.substring(eq + 1)).append('\n');
        }
        b.append("\nCases (fixtures/cases.tsv):\n\n");
        for (Case c : cases) {
            b.append("- ").append(c.id()).append(": `").append(c.xml().getFileName()).append("` against `")
                    .append(c.mainXsd().getFileName()).append('`')
                    .append(c.importXsds().isEmpty() ? "" : " + imports").append(" — ").append(c.origin())
                    .append('\n');
        }
        b.append("\nAll profiles carry the options out of band: no options document, no cookie.\n");
        b.append("Decoded events are compared with the XML's own events (attributes in EXI order, ")
                .append("whitespace-only text dropped); values compare as written, then as numbers, then as ")
                .append("booleans. ✓ correct, ✗ failed (see notes), – not supported.\n");

        for (Profile p : profiles) {
            List<CaseRun> pr = runs.stream().filter(r -> r.p() == p).toList();
            b.append("\n## ").append(p).append(": ").append(p.title).append("\n\n");
            b.append("### Encode by X, decode by Y\n\n| encoder → decoder |");
            for (CaseRun r : pr) {
                b.append(' ').append(r.c().id()).append(" |");
            }
            b.append("\n|---|").append("---|".repeat(pr.size())).append('\n');
            for (Codec enc : codecs) {
                for (Codec dec : codecs) {
                    b.append("| ").append(enc.name()).append(" → ").append(dec.name()).append(" |");
                    for (CaseRun r : pr) {
                        Outcome e = r.encodes().get(enc.name());
                        Outcome d = r.decodes().get(enc.name()).get(dec.name());
                        String cell;
                        if (e.status() == Status.UNSUPPORTED || d.status() == Status.UNSUPPORTED) {
                            cell = "–";
                        } else if (e.status() == Status.FAIL) {
                            cell = "enc ✗" + note.apply(p + " " + r.c().id() + ": " + enc.name() + " encode: " + e.message());
                        } else if (d.status() == Status.FAIL) {
                            cell = "✗" + note.apply(p + " " + r.c().id() + ": " + dec.name() + " decoding "
                                    + enc.name() + "'s stream: " + d.message());
                        } else {
                            cell = "✓";
                        }
                        b.append(' ').append(cell).append(" |");
                    }
                    b.append('\n');
                }
            }
            if (p.byteGate()) {
                b.append("\n### Bytes compared with EXIficient's\n\n| encoder |");
                for (CaseRun r : pr) {
                    b.append(' ').append(r.c().id()).append(" |");
                }
                b.append("\n|---|").append("---|".repeat(pr.size())).append('\n');
                for (Codec enc : codecs) {
                    b.append("| ").append(enc.name()).append(" |");
                    for (CaseRun r : pr) {
                        b.append(' ').append(r.bytes().getOrDefault(enc.name(), "–")).append(" |");
                    }
                    b.append('\n');
                }
            }
            if (timing) {
                b.append("\n### Timing: median µs (p10–p90)\n\n");
                b.append("Encoders time their natural input: XML (parsing included) for EXIficient, ")
                        .append("Nagasena (Java and .NET), rexi and jexi; a pre-parsed event stream for exip and for the ")
                        .append("`events` rows of rexi and jexi. Decoders all decode EXIficient's stream. Schema compilation ")
                        .append("is excluded.\n\n| implementation | op |");
                for (CaseRun r : pr) {
                    b.append(' ').append(r.c().id()).append(" |");
                }
                b.append("\n|---|---|").append("---|".repeat(pr.size())).append('\n');
                List<String> keys = new ArrayList<>();
                for (CaseRun r : pr) {
                    for (TimingRow t : r.timings()) {
                        String key = t.impl() + "\t" + t.op() + (t.timing() == null ? "" : " (" + t.timing().input() + ")");
                        if (!keys.contains(key)) {
                            keys.add(key);
                        }
                    }
                }
                for (String key : keys) {
                    String[] k = key.split("\t");
                    b.append("| ").append(k[0]).append(" | ").append(k[1]).append(" |");
                    for (CaseRun r : pr) {
                        String cell = "–";
                        for (TimingRow t : r.timings()) {
                            String tk = t.impl() + "\t" + t.op()
                                    + (t.timing() == null ? "" : " (" + t.timing().input() + ")");
                            if (tk.equals(key)) {
                                cell = t.timing() == null ? "✗" + note.apply(p + " " + r.c().id() + ": " + t.impl() + " "
                                        + t.op() + " timing: " + t.error())
                                        : String.format(Locale.ROOT, "%.1f (%.1f–%.1f)", t.timing().medianUs(),
                                                t.timing().percentileUs(10), t.timing().percentileUs(90));
                            }
                        }
                        b.append(' ').append(cell).append(" |");
                    }
                    b.append('\n');
                }
            }
        }
        if (timing) {
            b.append("\nTiming: warm-up of at least ").append(bench.warmup()).append(" runs and ")
                    .append(bench.warmupMs()).append(" ms, then ").append(bench.iterations())
                    .append(" timed iterations per cell, each implementation timing itself in-process.\n");
        }
        if (!notes.isEmpty()) {
            b.append("\n## Notes\n\n");
            for (int i = 0; i < notes.size(); i++) {
                b.append(i + 1).append(". ").append(notes.get(i).replace("\n", " ").replace("|", "\\|")).append('\n');
            }
        }
        return b.toString();
    }

    private static ObjectNode json(ObjectMapper m, List<CaseRun> runs, List<String> meta, Bench bench,
            boolean timing) {
        ObjectNode root = m.createObjectNode();
        root.put("generated", Instant.now().truncatedTo(ChronoUnit.SECONDS).toString());
        root.put("cpu", cpu());
        root.put("java", System.getProperty("java.version"));
        ObjectNode metaNode = root.putObject("meta");
        for (String s : meta) {
            int eq = s.indexOf('=');
            metaNode.put(eq < 0 ? s : s.substring(0, eq), eq < 0 ? "" : s.substring(eq + 1));
        }
        root.put("warmup", bench.warmup());
        root.put("warmup_ms", bench.warmupMs());
        root.put("iterations", timing ? bench.iterations() : 0);
        ArrayNode results = root.putArray("results");
        for (CaseRun r : runs) {
            ObjectNode n = results.addObject();
            n.put("case", r.c().id());
            n.put("profile", r.p().name());
            ObjectNode enc = n.putObject("encode");
            r.encodes().forEach((k, v) -> {
                ObjectNode o = enc.putObject(k);
                o.put("status", v.status().name());
                if (v.message() != null) o.put("message", v.message());
                if (v.bytes() != null) o.put("bytes", v.bytes().length);
            });
            ObjectNode dec = n.putObject("decode");
            r.decodes().forEach((e, row) -> {
                ObjectNode o = dec.putObject(e);
                row.forEach((d, v) -> {
                    ObjectNode c = o.putObject(d);
                    c.put("status", v.status().name());
                    if (v.message() != null) c.put("message", v.message());
                });
            });
            ObjectNode bytes = n.putObject("bytes_vs_exificient");
            r.bytes().forEach(bytes::put);
            ArrayNode ts = n.putArray("timing");
            for (TimingRow t : r.timings()) {
                ObjectNode o = ts.addObject();
                o.put("impl", t.impl());
                o.put("op", t.op());
                if (t.timing() != null) {
                    o.put("input", t.timing().input());
                    o.put("setup_ns", t.timing().setupNs());
                    o.put("median_us", t.timing().medianUs());
                    o.put("p10_us", t.timing().percentileUs(10));
                    o.put("p90_us", t.timing().percentileUs(90));
                } else {
                    o.put("error", t.error());
                }
            }
        }
        return root;
    }
}
