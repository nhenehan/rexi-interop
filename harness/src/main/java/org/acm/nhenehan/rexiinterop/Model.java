package org.acm.nhenehan.rexiinterop;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/** Cases, profiles, events, timings and outcomes shared by every adapter. */
final class Model {
    private Model() {}

    /** One row of fixtures/cases.tsv. */
    record Case(String id, Path xml, Path mainXsd, List<Path> importXsds, String origin) {
        List<Path> xsds() {
            List<Path> all = new ArrayList<>();
            all.add(mainXsd);
            all.addAll(importXsds);
            return all;
        }

        String label() {
            return id + " " + mainXsd.getFileName().toString().replace(".xsd", "");
        }

        static List<Case> load(Path fixtures) throws IOException {
            List<Case> cases = new ArrayList<>();
            List<String> lines = Files.readAllLines(fixtures.resolve("cases.tsv"));
            for (String line : lines.subList(1, lines.size())) {
                if (line.isBlank()) {
                    continue;
                }
                String[] c = line.split("\t", -1);
                Path dir = fixtures.resolve(c[1]);
                List<Path> imports = new ArrayList<>();
                for (String imp : c[4].split(",")) {
                    if (!imp.isBlank()) {
                        imports.add(dir.resolve(imp.trim()));
                    }
                }
                cases.add(new Case(c[0], dir.resolve(c[2]), dir.resolve(c[3]), imports,
                        c.length > 5 ? c[5] : ""));
            }
            return cases;
        }
    }

    enum Alignment { BIT_PACKED, BYTE_ALIGNED, COMPRESSION }

    /**
     * Stream options; always out of band (no options document, no cookie), as
     * the seed project's P0..P3. Strict lanes are the byte-equality gate.
     */
    enum Profile {
        P0("strict, bit-packed", true, Alignment.BIT_PACKED),
        P1("non-strict, bit-packed", false, Alignment.BIT_PACKED),
        P2("strict, byte-aligned", true, Alignment.BYTE_ALIGNED),
        P3("strict, compression", true, Alignment.COMPRESSION);

        final String title;
        final boolean strict;
        final Alignment alignment;

        Profile(String title, boolean strict, Alignment alignment) {
            this.title = title;
            this.strict = strict;
            this.alignment = alignment;
        }

        boolean byteGate() {
            return strict && alignment != Alignment.COMPRESSION;
        }
    }

    /** One event in the .events line format shared with rexi and the exip driver. */
    record Ev(String kind, String name, String value) {
        static Ev of(String kind) {
            return new Ev(kind, null, null);
        }

        String line() {
            StringBuilder b = new StringBuilder(kind);
            if (name != null) {
                b.append(' ').append(name);
            }
            if (value != null) {
                b.append(' ').append(escape(value));
            }
            return b.toString();
        }

        static String escape(String s) {
            StringBuilder b = new StringBuilder(s.length());
            for (char c : s.toCharArray()) {
                switch (c) {
                    case '\\' -> b.append("\\\\");
                    case '\n' -> b.append("\\n");
                    case '\r' -> b.append("\\r");
                    case '\t' -> b.append("\\t");
                    default -> b.append(c);
                }
            }
            return b.toString();
        }

        static String unescape(String s) {
            StringBuilder b = new StringBuilder(s.length());
            for (int i = 0; i < s.length(); i++) {
                char c = s.charAt(i);
                if (c == '\\' && i + 1 < s.length()) {
                    char n = s.charAt(++i);
                    b.append(switch (n) {
                        case 'n' -> '\n';
                        case 'r' -> '\r';
                        case 't' -> '\t';
                        default -> n;
                    });
                } else {
                    b.append(c);
                }
            }
            return b.toString();
        }

        /** Parses one .events line. */
        static Ev parse(String line) {
            int sp = line.indexOf(' ');
            String kind = sp < 0 ? line : line.substring(0, sp);
            String rest = sp < 0 ? null : line.substring(sp + 1);
            return switch (kind) {
                case "SD", "ED", "EE" -> of(kind);
                case "SE", "XSI_TYPE" -> new Ev(kind, rest, null);
                case "XSI_NIL" -> new Ev(kind, null, rest);
                case "CH" -> new Ev(kind, null, unescape(rest == null ? "" : rest));
                case "AT" -> {
                    int sp2 = rest.indexOf(' ', rest.indexOf('}'));
                    yield sp2 < 0 ? new Ev(kind, rest, "")
                            : new Ev(kind, rest.substring(0, sp2), unescape(rest.substring(sp2 + 1)));
                }
                default -> throw new IllegalArgumentException("unknown event line: " + line);
            };
        }

        static List<Ev> parseAll(String text) {
            List<Ev> out = new ArrayList<>();
            for (String line : text.split("\n")) {
                if (!line.isEmpty()) {
                    out.add(parse(line));
                }
            }
            return out;
        }

        static String dump(List<Ev> events) {
            StringBuilder b = new StringBuilder();
            for (Ev e : events) {
                b.append(e.line()).append('\n');
            }
            return b.toString();
        }
    }

    /**
     * Compares decoded events with the reference. Values are compared as
     * written, then as numbers (canonical EXI float/decimal forms such as
     * 2495E-2 equal 24.95), then as booleans. Returns null when equal.
     */
    static String diff(List<Ev> expected, List<Ev> actual) {
        int n = Math.min(expected.size(), actual.size());
        for (int i = 0; i < n; i++) {
            Ev e = expected.get(i);
            Ev a = actual.get(i);
            if (!e.kind().equals(a.kind()) || !java.util.Objects.equals(e.name(), a.name())
                    || !valueEquals(e.value(), a.value())) {
                return "event " + (i + 1) + ": expected `" + e.line() + "`, got `" + a.line() + "`";
            }
        }
        if (expected.size() != actual.size()) {
            return "expected " + expected.size() + " events, got " + actual.size();
        }
        return null;
    }

    static boolean valueEquals(String e, String a) {
        if (java.util.Objects.equals(e, a)) {
            return true;
        }
        if (e == null || a == null) {
            return false;
        }
        String x = e.strip();
        String y = a.strip();
        if (x.equals(y)) {
            return true;
        }
        BigDecimal bx = number(x);
        BigDecimal by = number(y);
        if (bx != null && by != null) {
            return bx.compareTo(by) == 0;
        }
        Boolean px = bool(x);
        Boolean py = bool(y);
        return px != null && px.equals(py);
    }

    private static BigDecimal number(String s) {
        if (!s.matches("[+-]?(\\d+\\.?\\d*|\\.\\d+)([eE][+-]?\\d+)?")) {
            return null;
        }
        try {
            return new BigDecimal(s);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private static Boolean bool(String s) {
        return switch (s) {
            case "true", "1" -> Boolean.TRUE;
            case "false", "0" -> Boolean.FALSE;
            default -> null;
        };
    }

    /** Timed iterations of one operation, in nanoseconds. */
    record Timing(String input, long setupNs, long[] ns) {
        double medianUs() {
            return percentileUs(50);
        }

        double percentileUs(int p) {
            if (ns.length == 0) {
                return Double.NaN;
            }
            long[] s = ns.clone();
            Arrays.sort(s);
            int idx = (int) Math.min(s.length - 1, Math.round(p / 100.0 * (s.length - 1)));
            return s[idx] / 1000.0;
        }
    }

    enum Status { OK, UNSUPPORTED, FAIL }

    /** Result of one encode or decode. */
    record Outcome(Status status, String message, byte[] bytes, List<Ev> events, Timing timing) {
        static Outcome unsupported(String why) {
            return new Outcome(Status.UNSUPPORTED, why, null, null, null);
        }

        static Outcome fail(String why) {
            return new Outcome(Status.FAIL, why, null, null, null);
        }

        static Outcome encoded(byte[] bytes, Timing timing) {
            return new Outcome(Status.OK, null, bytes, null, timing);
        }

        static Outcome decoded(List<Ev> events, Timing timing) {
            return new Outcome(Status.OK, null, null, events, timing);
        }
    }

    /**
     * Warm-up and timed iterations; 0 iterations means correctness only. Warm-up
     * runs at least {@code warmup} times and for at least {@code warmupMs}
     * milliseconds: JIT runtimes reach their optimized code after some time, not
     * after a fixed count (.NET's tiered JIT waits for about 100 ms without new
     * compiles, then optimizes in stages).
     */
    record Bench(int warmup, int warmupMs, int iterations) {
        static final Bench ONCE = new Bench(0, 0, 0);
    }

    static String read(Path p) throws IOException {
        return Files.readString(p, StandardCharsets.UTF_8);
    }
}
