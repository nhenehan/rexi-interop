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

    /** Why this implementation cannot run the case under the profile, or null if it can. */
    default String unsupported(Case c, Profile p) {
        return unsupported(p);
    }

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
        SaxEvents h = new SaxEvents();
        parseXml(false, xml, h);
        return h.events;
    }

    /** The reference events of a case's XML, which may be a fragment. */
    static List<Ev> referenceEvents(Case c) throws Exception {
        SaxEvents h = new SaxEvents();
        parseXml(c, java.nio.file.Files.readAllBytes(c.xml()), h);
        return h.events;
    }

    /**
     * Parses a case's XML into {@code handler}. A fragment has several root
     * elements, which a SAX parser refuses, so it is parsed inside a wrapper
     * element that {@link FragmentFilter} then hides.
     */
    static void parseXml(Case c, byte[] xml, org.xml.sax.ContentHandler handler) throws Exception {
        parseXml(c.fragment(), xml, handler);
    }

    static void parseXml(boolean fragment, byte[] xml, org.xml.sax.ContentHandler handler) throws Exception {
        SAXParserFactory f = SAXParserFactory.newDefaultInstance();
        f.setNamespaceAware(true);
        XMLReader r = f.newSAXParser().getXMLReader();
        if (fragment) {
            r.setContentHandler(new FragmentFilter(handler));
            r.parse(new InputSource(new ByteArrayInputStream(wrapFragment(xml))));
        } else {
            r.setContentHandler(handler);
            r.parse(new InputSource(new ByteArrayInputStream(xml)));
        }
    }

    /** The fragment's text inside one wrapper element (the fragment has no XML declaration). */
    static byte[] wrapFragment(byte[] xml) {
        byte[] open = "<rexi-interop-fragment>".getBytes(java.nio.charset.StandardCharsets.UTF_8);
        byte[] close = "</rexi-interop-fragment>".getBytes(java.nio.charset.StandardCharsets.UTF_8);
        byte[] out = new byte[open.length + xml.length + close.length];
        System.arraycopy(open, 0, out, 0, open.length);
        System.arraycopy(xml, 0, out, open.length, xml.length);
        System.arraycopy(close, 0, out, open.length + xml.length, close.length);
        return out;
    }

    /**
     * Passes SAX events on, without the wrapper element of {@link #wrapFragment}
     * and without the whitespace between the fragment's root elements.
     */
    final class FragmentFilter extends org.xml.sax.helpers.XMLFilterImpl {
        private int depth;

        FragmentFilter(org.xml.sax.ContentHandler next) {
            setContentHandler(next);
        }

        @Override
        public void startElement(String uri, String local, String qName, Attributes atts)
                throws org.xml.sax.SAXException {
            if (depth++ > 0) {
                super.startElement(uri, local, qName, atts);
            }
        }

        @Override
        public void endElement(String uri, String local, String qName) throws org.xml.sax.SAXException {
            if (--depth > 0) {
                super.endElement(uri, local, qName);
            }
        }

        @Override
        public void characters(char[] ch, int start, int length) throws org.xml.sax.SAXException {
            if (depth > 1) {
                super.characters(ch, start, length);
            }
        }

        @Override
        public void ignorableWhitespace(char[] ch, int start, int length) throws org.xml.sax.SAXException {
            if (depth > 1) {
                super.ignorableWhitespace(ch, start, length);
            }
        }
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
