package org.acm.nhenehan.rexiinterop;

import java.io.ByteArrayInputStream;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.parsers.SAXParserFactory;
import org.acm.nhenehan.rexiinterop.Model.Bench;
import org.acm.nhenehan.rexiinterop.Model.Case;
import org.acm.nhenehan.rexiinterop.Model.Ev;
import org.acm.nhenehan.rexiinterop.Model.Outcome;
import org.acm.nhenehan.rexiinterop.Model.Profile;
import org.acm.nhenehan.rexiinterop.Model.Timing;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

/** One EXI implementation under test. */
interface Codec {
    String name();

    /** Why this implementation cannot run the profile, or null if it can. */
    String unsupported(Profile p);

    /** Encodes the case's document (bench: timing loop after one untimed run). */
    Outcome encode(Case c, Profile p, Bench bench) throws Exception;

    /** Decodes `exi` to events (bench: timing loop after one untimed run). */
    Outcome decode(Case c, Profile p, byte[] exi, Bench bench) throws Exception;

    @FunctionalInterface
    interface Op {
        void run() throws Exception;
    }

    /** In-process timing loop shared by the JVM adapters. */
    static Timing time(String input, long setupNs, Bench bench, Op op) throws Exception {
        long start = System.nanoTime();
        long minNs = bench.warmupMs() * 1_000_000L;
        for (int i = 0; i < bench.warmup() || System.nanoTime() - start < minNs; i++) {
            op.run();
        }
        long[] ns = new long[bench.iterations()];
        for (int i = 0; i < ns.length; i++) {
            long t = System.nanoTime();
            op.run();
            ns[i] = System.nanoTime() - t;
        }
        return new Timing(input, setupNs, ns);
    }

    /**
     * The reference event stream of an XML document, in the order a strict EXI
     * stream carries it: xsi:type and xsi:nil first, other attributes sorted
     * by local name then namespace, adjacent text merged, whitespace-only text
     * dropped (none of the fixtures has significant whitespace-only content),
     * namespace declarations omitted.
     */
    static List<Ev> referenceEvents(byte[] xml) throws Exception {
        SAXParserFactory f = SAXParserFactory.newDefaultInstance();
        f.setNamespaceAware(true);
        XMLReader r = f.newSAXParser().getXMLReader();
        SaxEvents h = new SaxEvents();
        r.setContentHandler(h);
        r.parse(new InputSource(new ByteArrayInputStream(xml)));
        return h.events;
    }

    /** SAX → events, also used for decoders that report through SAX (Nagasena). */
    final class SaxEvents extends DefaultHandler {
        static final String XSI = "http://www.w3.org/2001/XMLSchema-instance";
        final List<Ev> events = new ArrayList<>();
        private final StringBuilder text = new StringBuilder();
        private final Deque<Map<String, String>> scopes = new ArrayDeque<>();
        private Map<String, String> pending = new HashMap<>();

        @Override
        public void startDocument() {
            events.add(Ev.of("SD"));
        }

        @Override
        public void endDocument() {
            events.add(Ev.of("ED"));
        }

        @Override
        public void startPrefixMapping(String prefix, String uri) {
            pending.put(prefix, uri);
        }

        private String resolvePrefix(String prefix) {
            if (pending.containsKey(prefix)) {
                return pending.get(prefix);
            }
            for (Map<String, String> m : scopes) {
                if (m.containsKey(prefix)) {
                    return m.get(prefix);
                }
            }
            return "";
        }

        private void flush() {
            if (text.length() > 0) {
                String s = text.toString();
                if (!s.isBlank()) {
                    events.add(new Ev("CH", null, s));
                }
                text.setLength(0);
            }
        }

        @Override
        public void startElement(String uri, String local, String qName, Attributes atts) {
            flush();
            events.add(new Ev("SE", "{" + uri + "}" + local, null));
            Ev xsiType = null;
            Ev xsiNil = null;
            List<String[]> plain = new ArrayList<>();
            for (int i = 0; i < atts.getLength(); i++) {
                String auri = atts.getURI(i);
                String alocal = atts.getLocalName(i);
                String q = atts.getQName(i);
                if (q.equals("xmlns") || q.startsWith("xmlns:")) {
                    continue;
                }
                String value = atts.getValue(i);
                if (XSI.equals(auri) && alocal.equals("type")) {
                    String v = value.strip();
                    int colon = v.indexOf(':');
                    String prefix = colon < 0 ? "" : v.substring(0, colon);
                    xsiType = new Ev("XSI_TYPE", "{" + resolvePrefix(prefix) + "}" + v.substring(colon + 1), null);
                } else if (XSI.equals(auri) && alocal.equals("nil")) {
                    String v = value.strip();
                    xsiNil = new Ev("XSI_NIL", null, v.equals("1") ? "true" : v.equals("0") ? "false" : v);
                } else {
                    plain.add(new String[] {alocal, auri, value});
                }
            }
            scopes.push(pending);
            pending = new HashMap<>();
            if (xsiType != null) {
                events.add(xsiType);
            }
            if (xsiNil != null) {
                events.add(xsiNil);
            }
            plain.sort(Comparator.<String[], String>comparing(a -> a[0]).thenComparing(a -> a[1]));
            for (String[] a : plain) {
                events.add(new Ev("AT", "{" + a[1] + "}" + a[0], a[2]));
            }
        }

        @Override
        public void endElement(String uri, String local, String qName) {
            flush();
            scopes.pop();
            events.add(Ev.of("EE"));
        }

        @Override
        public void characters(char[] ch, int start, int length) {
            text.append(ch, start, length);
        }
    }
}
