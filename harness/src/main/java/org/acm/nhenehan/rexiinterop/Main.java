package org.acm.nhenehan.rexiinterop;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.acm.nhenehan.rexiinterop.Model.Bench;
import org.acm.nhenehan.rexiinterop.Model.Case;
import org.acm.nhenehan.rexiinterop.Model.Ev;
import org.acm.nhenehan.rexiinterop.Model.Outcome;
import org.acm.nhenehan.rexiinterop.Model.Profile;
import org.acm.nhenehan.rexiinterop.Model.Status;

/**
 * Runs the interop matrix and the timing loops, then writes reports/.
 *
 * <pre>
 *   java -jar rexi-interop-harness.jar --fixtures fixtures --rexi BIN --exip-driver BIN
 *        [--nagasena-cs-driver BIN] [--jexi BIN] --work DIR --out reports [--warmup N] [--warmup-ms MS] [--iterations N] [--cases S1,R2]
 *        [--profiles P0,P2] [--no-timing] [--meta key=value ...]
 * </pre>
 */
public final class Main {
    private Main() {}

    /** Matrix results for one case under one profile. */
    record CaseRun(Case c, Profile p, Map<String, Outcome> encodes, Map<String, Map<String, Outcome>> decodes,
            Map<String, String> bytes, List<TimingRow> timings) {}

    record TimingRow(String impl, String op, Model.Timing timing, String error) {}

    public static void main(String[] argv) throws Exception {
        Map<String, String> a = new LinkedHashMap<>();
        List<String> meta = new ArrayList<>();
        for (int i = 0; i < argv.length; i++) {
            String k = argv[i];
            if (k.equals("--no-timing")) {
                a.put(k, "true");
            } else if (k.equals("--meta")) {
                meta.add(argv[++i]);
            } else if (k.startsWith("--") && i + 1 < argv.length) {
                a.put(k, argv[++i]);
            } else {
                throw new IllegalArgumentException("unknown argument " + k);
            }
        }
        Path fixtures = Path.of(a.getOrDefault("--fixtures", "fixtures"));
        Path work = Path.of(a.getOrDefault("--work", "reports/work"));
        Path out = Path.of(a.getOrDefault("--out", "reports"));
        Bench bench = new Bench(Integer.parseInt(a.getOrDefault("--warmup", "2000")),
                Integer.parseInt(a.getOrDefault("--warmup-ms", "3000")),
                Integer.parseInt(a.getOrDefault("--iterations", "2000")));
        boolean timing = !a.containsKey("--no-timing");
        Files.createDirectories(work);

        List<Case> cases = Case.load(fixtures);
        if (a.containsKey("--cases")) {
            List<String> keep = Arrays.asList(a.get("--cases").split(","));
            cases.removeIf(c -> !keep.contains(c.id()));
        }
        List<Profile> profiles = new ArrayList<>(List.of(Profile.values()));
        if (a.containsKey("--profiles")) {
            List<String> keep = Arrays.asList(a.get("--profiles").split(","));
            profiles.removeIf(p -> !keep.contains(p.name()));
        }

        List<Codec> codecs = new ArrayList<>(List.of(new ExificientCodec(), new NagasenaCodec()));
        if (a.containsKey("--nagasena-cs-driver")) {
            codecs.add(new DriverCodec.NagasenaCs(Path.of(a.get("--nagasena-cs-driver")), work));
        }
        codecs.add(new DriverCodec.Exip(Path.of(require(a, "--exip-driver")), work));
        codecs.add(new DriverCodec.Rexi(Path.of(require(a, "--rexi")), work));
        if (a.containsKey("--jexi")) {
            codecs.add(new DriverCodec.Jexi(Path.of(a.get("--jexi")), work));
        }

        List<CaseRun> runs = new ArrayList<>();
        for (Profile p : profiles) {
            for (Case c : cases) {
                System.err.printf("%s %s ...%n", p, c.label());
                runs.add(runCase(c, p, codecs, timing ? bench : null));
            }
        }
        Report.write(out, runs, codecs, profiles, cases, bench, timing, meta);
        System.err.println("wrote " + out.resolve("latest.md"));
    }

    private static String require(Map<String, String> a, String k) {
        String v = a.get(k);
        if (v == null) {
            throw new IllegalArgumentException(k + " is required");
        }
        return v;
    }

    private static Outcome attempt(Codec.Op op, Outcome[] slot) {
        try {
            op.run();
            return slot[0];
        } catch (Exception | AssertionError e) {
            String msg = e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage();
            return Outcome.fail(msg.strip().lines().limit(3).reduce((x, y) -> x + " / " + y).orElse(msg));
        }
    }

    static CaseRun runCase(Case c, Profile p, List<Codec> codecs, Bench bench) throws Exception {
        List<Ev> reference = Codec.referenceEvents(Files.readAllBytes(c.xml()));
        Map<String, Outcome> encodes = new LinkedHashMap<>();
        Map<String, Map<String, Outcome>> decodes = new LinkedHashMap<>();
        Map<String, String> bytes = new LinkedHashMap<>();
        List<TimingRow> timings = new ArrayList<>();

        for (Codec enc : codecs) {
            String why = enc.unsupported(p);
            Outcome[] slot = new Outcome[1];
            encodes.put(enc.name(), why != null ? Outcome.unsupported(why)
                    : attempt(() -> slot[0] = enc.encode(c, p, Bench.ONCE), slot));
        }
        for (Codec enc : codecs) {
            Outcome e = encodes.get(enc.name());
            Map<String, Outcome> row = new LinkedHashMap<>();
            for (Codec dec : codecs) {
                String why = dec.unsupported(p);
                if (e.status() != Status.OK) {
                    row.put(dec.name(), e.status() == Status.UNSUPPORTED ? e : Outcome.fail("encoder failed"));
                } else if (why != null) {
                    row.put(dec.name(), Outcome.unsupported(why));
                } else {
                    Outcome[] slot = new Outcome[1];
                    Outcome d = attempt(() -> slot[0] = dec.decode(c, p, e.bytes(), Bench.ONCE), slot);
                    if (d.status() == Status.OK) {
                        String diff = Model.diff(reference, d.events());
                        if (diff != null) {
                            d = Outcome.fail(diff);
                        }
                    }
                    row.put(dec.name(), d);
                }
            }
            decodes.put(enc.name(), row);
        }

        Outcome ref = encodes.get("EXIficient");
        for (Codec enc : codecs) {
            Outcome e = encodes.get(enc.name());
            if (!p.byteGate() || e.status() != Status.OK || ref.status() != Status.OK) {
                continue;
            }
            bytes.put(enc.name(), Arrays.equals(e.bytes(), ref.bytes()) ? "="
                    : "≠ " + e.bytes().length + " B vs " + ref.bytes().length + " B");
        }

        if (bench != null && ref.status() == Status.OK) {
            for (Codec codec : codecs) {
                if (codec.unsupported(p) != null) {
                    continue;
                }
                timings.add(timed(codec.name(), "encode", () -> codec.encode(c, p, bench)));
                if (codec instanceof DriverCodec.Rexi rexi) {
                    timings.add(timed(codec.name(), "encode", () -> rexi.encodeFromEvents(c, p, bench)));
                }
                timings.add(timed(codec.name(), "decode", () -> codec.decode(c, p, ref.bytes(), bench)));
            }
        }
        return new CaseRun(c, p, encodes, decodes, bytes, timings);
    }

    @FunctionalInterface
    interface Timed {
        Outcome run() throws Exception;
    }

    private static TimingRow timed(String impl, String op, Timed t) {
        try {
            Outcome o = t.run();
            return new TimingRow(impl, op, o.timing(), null);
        } catch (Exception e) {
            return new TimingRow(impl, op, null, String.valueOf(e.getMessage()));
        }
    }
}
