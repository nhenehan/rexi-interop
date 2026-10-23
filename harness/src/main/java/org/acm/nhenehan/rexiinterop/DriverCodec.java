package org.acm.nhenehan.rexiinterop;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.acm.nhenehan.rexiinterop.Model.Bench;
import org.acm.nhenehan.rexiinterop.Model.Case;
import org.acm.nhenehan.rexiinterop.Model.Ev;
import org.acm.nhenehan.rexiinterop.Model.Outcome;
import org.acm.nhenehan.rexiinterop.Model.Profile;
import org.acm.nhenehan.rexiinterop.Model.Timing;
import org.openexi.scomp.EXISchemaFactory;
import org.xml.sax.InputSource;

/**
 * An implementation driven through its own benchmark program: rexi's
 * `rexi bench`, jexi's `jexi bench` (the same command line), the exip driver or
 * the Nagasena for .NET driver. The program times itself in-process and
 * prints one JSON object; process start-up is never in the numbers.
 */
abstract class DriverCodec implements Codec {
    private static final ObjectMapper JSON = new ObjectMapper();
    protected final Path work;

    DriverCodec(Path work) {
        this.work = work;
    }

    protected Path file(Case c, Profile p, String suffix) throws IOException {
        Path dir = work.resolve(name().toLowerCase().replace(' ', '-'));
        Files.createDirectories(dir);
        return dir.resolve(c.id() + "." + p + "." + suffix);
    }

    protected static List<String> benchArgs(Bench b) {
        return List.of("--warmup", Integer.toString(b.warmup()), "--warmup-ms", Integer.toString(b.warmupMs()),
                "--iterations", Integer.toString(b.iterations()));
    }

    /** Runs the driver; returns its JSON, or throws with its stderr. */
    protected static JsonNode run(List<String> cmd) throws IOException, InterruptedException {
        Process proc = new ProcessBuilder(cmd).redirectErrorStream(false).start();
        byte[] out = proc.getInputStream().readAllBytes();
        byte[] err = proc.getErrorStream().readAllBytes();
        int code = proc.waitFor();
        if (code != 0) {
            String msg = new String(err, StandardCharsets.UTF_8).strip();
            throw new DriverFailure(msg.isEmpty() ? "exit " + code : msg);
        }
        return JSON.readTree(out);
    }

    protected static Timing timing(JsonNode j) {
        JsonNode ns = j.get("ns");
        long[] values = new long[ns.size()];
        for (int i = 0; i < values.length; i++) {
            values[i] = ns.get(i).asLong();
        }
        return new Timing(j.get("input").asText(), j.get("setup_ns").asLong(), values);
    }

    static final class DriverFailure extends IOException {
        DriverFailure(String msg) {
            super(msg);
        }
    }

    /** Encode-from-events variant of an encoder, for the timing table. */
    Outcome encodeFromEvents(Case c, Profile p, Bench bench) throws Exception {
        return encode(c, p, bench);
    }

    // --------------------------------------------------------------- rexi

    static class Rexi extends DriverCodec {
        private final Path binary;
        private final String name;

        Rexi(Path binary, Path work) {
            this(binary, work, "rexi");
        }

        protected Rexi(Path binary, Path work, String name) {
            super(work);
            this.binary = binary;
            this.name = name;
        }

        @Override
        public String name() {
            return name;
        }

        @Override
        public String unsupported(Profile p) {
            if (!p.strict) {
                return "non-strict not implemented";
            }
            if (p.alignment == Model.Alignment.COMPRESSION) {
                return "compression not implemented";
            }
            return null;
        }

        private List<String> base(String op, Case c, Profile p) {
            List<String> cmd = new ArrayList<>(List.of(binary.toString(), "bench", op, "--xsd",
                    c.xsds().stream().map(Path::toString).collect(Collectors.joining(",")), "--strict",
                    "--alignment", p.alignment == Model.Alignment.BYTE_ALIGNED ? "byte-aligned" : "bit-packed"));
            return cmd;
        }

        private Outcome encode(Case c, Profile p, Bench bench, String from) throws Exception {
            Path out = file(c, p, from + ".exi");
            List<String> cmd = base("encode", c, p);
            cmd.addAll(benchArgs(bench));
            cmd.addAll(List.of("--from", from, "--out", out.toString(), c.xml().toString()));
            JsonNode j = run(cmd);
            return Outcome.encoded(Files.readAllBytes(out), timing(j));
        }

        @Override
        public Outcome encode(Case c, Profile p, Bench bench) throws Exception {
            return encode(c, p, bench, "xml");
        }

        @Override
        Outcome encodeFromEvents(Case c, Profile p, Bench bench) throws Exception {
            return encode(c, p, bench, "events");
        }

        @Override
        public Outcome decode(Case c, Profile p, byte[] exi, Bench bench) throws Exception {
            Path in = file(c, p, "in.exi");
            Path events = file(c, p, "out.events");
            Files.write(in, exi);
            List<String> cmd = base("decode", c, p);
            cmd.addAll(benchArgs(bench));
            cmd.addAll(List.of("--events-out", events.toString(), in.toString()));
            JsonNode j = run(cmd);
            return Outcome.decoded(Ev.parseAll(Model.read(events)), timing(j));
        }
    }

    // --------------------------------------------------------------- jexi

    /**
     * jexi, the Julia implementation: `bin/jexi` takes rexi's command line and
     * prints the same JSON, and it supports the same profiles.
     */
    static final class Jexi extends Rexi {
        Jexi(Path binary, Path work) {
            super(binary, work, "jexi");
        }
    }

    // --------------------------------------------------------------- exip

    /**
     * exip through drivers/exip: it has no XML front end, so it always encodes
     * from the reference event stream (written once per case).
     */
    static final class Exip extends DriverCodec {
        private final Path binary;

        Exip(Path binary, Path work) {
            super(work);
            this.binary = binary;
        }

        @Override
        public String name() {
            return "exip";
        }

        @Override
        public String unsupported(Profile p) {
            return null;
        }

        private List<String> base(String op, Case c, Profile p) throws Exception {
            List<String> schemas = new ArrayList<>();
            for (Path xsd : c.xsds()) {
                Path exi = work.resolve("schemas").resolve(c.id() + "." + xsd.getFileName() + ".exi");
                if (!Files.exists(exi)) {
                    Files.createDirectories(exi.getParent());
                    ExificientCodec.schemaForExip(xsd, exi);
                }
                schemas.add(exi.toString());
            }
            List<String> cmd = new ArrayList<>(List.of(binary.toString(), op, "--schema", String.join(",", schemas),
                    "--alignment", switch (p.alignment) {
                        case BIT_PACKED -> "bit-packed";
                        case BYTE_ALIGNED -> "byte-aligned";
                        case COMPRESSION -> "compression";
                    }));
            if (p.strict) {
                cmd.add("--strict");
            }
            return cmd;
        }

        @Override
        public Outcome encode(Case c, Profile p, Bench bench) throws Exception {
            Path events = file(c, p, "in.events");
            Files.writeString(events, Ev.dump(Codec.referenceEvents(Files.readAllBytes(c.xml()))));
            Path out = file(c, p, "out.exi");
            List<String> cmd = base("encode", c, p);
            cmd.addAll(benchArgs(bench));
            cmd.addAll(List.of("--events", events.toString(), "--out", out.toString()));
            JsonNode j = run(cmd);
            return Outcome.encoded(Files.readAllBytes(out), timing(j));
        }

        @Override
        public Outcome decode(Case c, Profile p, byte[] exi, Bench bench) throws Exception {
            Path in = file(c, p, "in.exi");
            Path events = file(c, p, "out.events");
            Files.write(in, exi);
            List<String> cmd = base("decode", c, p);
            cmd.addAll(benchArgs(bench));
            cmd.addAll(List.of("--exi", in.toString(), "--events-out", events.toString()));
            JsonNode j = run(cmd);
            return Outcome.decoded(Ev.parseAll(Model.read(events)), timing(j));
        }
    }

    // ------------------------------------------------------- Nagasena .NET

    /**
     * Nagasena for .NET (OpenEXI's C# port) through drivers/nagasena-cs. The port
     * can read compiled schemas but has no schema compiler, so the schema is
     * compiled here by Java Nagasena and handed over in its binary form
     * (EXISchema.writeOut, read back by the C# EXISchema.readIn).
     */
    static final class NagasenaCs extends DriverCodec {
        private final Path binary;

        NagasenaCs(Path binary, Path work) {
            super(work);
            this.binary = binary;
        }

        @Override
        public String name() {
            return "Nagasena.NET";
        }

        @Override
        public String unsupported(Profile p) {
            return null;
        }

        private Path xsc(Case c) throws Exception {
            Path xsc = work.resolve("schemas").resolve(c.id() + ".nagasena.xsc");
            if (!Files.exists(xsc)) {
                Files.createDirectories(xsc.getParent());
                var schema = new EXISchemaFactory().compile(new InputSource(c.mainXsd().toUri().toString()));
                try (OutputStream o = Files.newOutputStream(xsc); DataOutputStream d = new DataOutputStream(o)) {
                    schema.writeOut(d);
                }
            }
            return xsc;
        }

        private List<String> base(String op, Case c, Profile p) throws Exception {
            List<String> cmd = new ArrayList<>(List.of(binary.toString(), op, "--xsc", xsc(c).toString(),
                    "--alignment", switch (p.alignment) {
                        case BIT_PACKED -> "bit-packed";
                        case BYTE_ALIGNED -> "byte-aligned";
                        case COMPRESSION -> "compression";
                    }));
            if (p.strict) {
                cmd.add("--strict");
            }
            return cmd;
        }

        @Override
        public Outcome encode(Case c, Profile p, Bench bench) throws Exception {
            Path out = file(c, p, "out.exi");
            List<String> cmd = base("encode", c, p);
            cmd.addAll(benchArgs(bench));
            cmd.addAll(List.of("--out", out.toString(), c.xml().toString()));
            JsonNode j = run(cmd);
            return Outcome.encoded(Files.readAllBytes(out), timing(j));
        }

        @Override
        public Outcome decode(Case c, Profile p, byte[] exi, Bench bench) throws Exception {
            Path in = file(c, p, "in.exi");
            Path events = file(c, p, "out.events");
            Files.write(in, exi);
            List<String> cmd = base("decode", c, p);
            cmd.addAll(benchArgs(bench));
            cmd.addAll(List.of("--events-out", events.toString(), in.toString()));
            JsonNode j = run(cmd);
            return Outcome.decoded(Ev.parseAll(Model.read(events)), timing(j));
        }
    }
}
