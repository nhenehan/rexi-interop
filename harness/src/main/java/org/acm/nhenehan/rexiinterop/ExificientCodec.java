package org.acm.nhenehan.rexiinterop;

import com.siemens.ct.exi.core.CodingMode;
import com.siemens.ct.exi.core.EXIBodyDecoder;
import com.siemens.ct.exi.core.EXIFactory;
import com.siemens.ct.exi.core.EncodingOptions;
import com.siemens.ct.exi.core.FidelityOptions;
import com.siemens.ct.exi.core.grammars.Grammars;
import com.siemens.ct.exi.core.helpers.DefaultEXIFactory;
import com.siemens.ct.exi.core.values.QNameValue;
import com.siemens.ct.exi.core.values.Value;
import com.siemens.ct.exi.grammars.GrammarFactory;
import com.siemens.ct.exi.main.api.sax.EXIResult;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import javax.xml.parsers.SAXParserFactory;
import org.acm.nhenehan.rexiinterop.Model.Bench;
import org.acm.nhenehan.rexiinterop.Model.Case;
import org.acm.nhenehan.rexiinterop.Model.Ev;
import org.acm.nhenehan.rexiinterop.Model.Outcome;
import org.acm.nhenehan.rexiinterop.Model.Profile;
import org.acm.nhenehan.rexiinterop.Model.Timing;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

/** EXIficient 1.0.7, in-process: SAX encoding, low-level body decoding. */
final class ExificientCodec implements Codec {
    @Override
    public String name() {
        return "EXIficient";
    }

    @Override
    public String unsupported(Profile p) {
        return null;
    }

    private static EXIFactory factory(Case c, Profile p) throws Exception {
        EXIFactory f = DefaultEXIFactory.newInstance();
        Grammars g = GrammarFactory.newInstance().createGrammars(c.mainXsd().toString());
        f.setGrammars(g);
        f.setCodingMode(switch (p.alignment) {
            case BIT_PACKED -> CodingMode.BIT_PACKED;
            case BYTE_ALIGNED -> CodingMode.BYTE_PACKED;
            case COMPRESSION -> CodingMode.COMPRESSION;
        });
        f.setFidelityOptions(p.strict ? FidelityOptions.createStrict() : FidelityOptions.createDefault());
        f.setEncodingOptions(EncodingOptions.createDefault());
        return f;
    }

    @Override
    public Outcome encode(Case c, Profile p, Bench bench) throws Exception {
        long t0 = System.nanoTime();
        EXIFactory f = factory(c, p);
        long setup = System.nanoTime() - t0;
        byte[] xml = Files.readAllBytes(c.xml());
        SAXParserFactory spf = SAXParserFactory.newDefaultInstance();
        spf.setNamespaceAware(true);
        byte[][] last = new byte[1][];
        Codec.Op once = () -> {
            ByteArrayOutputStream out = new ByteArrayOutputStream(1024);
            EXIResult result = new EXIResult(f);
            result.setOutputStream(out);
            XMLReader r = spf.newSAXParser().getXMLReader();
            r.setContentHandler(result.getHandler());
            r.parse(new InputSource(new ByteArrayInputStream(xml)));
            last[0] = out.toByteArray();
        };
        once.run();
        byte[] bytes = last[0];
        Timing t = Codec.time("xml", setup, bench, once);
        return Outcome.encoded(bytes, t);
    }

    @Override
    public Outcome decode(Case c, Profile p, byte[] exi, Bench bench) throws Exception {
        long t0 = System.nanoTime();
        EXIFactory f = factory(c, p);
        long setup = System.nanoTime() - t0;
        List<Ev> events = decodeEvents(f, exi, true);
        Timing t = Codec.time("exi", setup, bench, () -> decodeEvents(f, exi, false));
        return Outcome.decoded(events, t);
    }

    /** Decodes with EXIficient's body decoder; collect=false only counts. */
    static List<Ev> decodeEvents(EXIFactory f, byte[] exi, boolean collect) throws Exception {
        List<Ev> out = new ArrayList<>();
        EXIBodyDecoder d = f.createEXIStreamDecoder().decodeHeader(new ByteArrayInputStream(exi));
        com.siemens.ct.exi.core.grammars.event.EventType et;
        int count = 0;
        while ((et = d.next()) != null) {
            count++;
            switch (et) {
                case START_DOCUMENT -> {
                    d.decodeStartDocument();
                    if (collect) out.add(Ev.of("SD"));
                }
                case END_DOCUMENT -> {
                    d.decodeEndDocument();
                    if (collect) out.add(Ev.of("ED"));
                }
                case START_ELEMENT, START_ELEMENT_NS, START_ELEMENT_GENERIC,
                        START_ELEMENT_GENERIC_UNDECLARED -> {
                    var q = d.decodeStartElement();
                    if (collect) out.add(new Ev("SE", "{" + q.getNamespaceUri() + "}" + q.getLocalName(), null));
                }
                case END_ELEMENT, END_ELEMENT_UNDECLARED -> {
                    d.decodeEndElement();
                    if (collect) out.add(Ev.of("EE"));
                }
                case ATTRIBUTE, ATTRIBUTE_NS, ATTRIBUTE_GENERIC, ATTRIBUTE_INVALID_VALUE,
                        ATTRIBUTE_ANY_INVALID_VALUE, ATTRIBUTE_GENERIC_UNDECLARED -> {
                    var q = d.decodeAttribute();
                    Value v = d.getAttributeValue();
                    if (collect) {
                        out.add(new Ev("AT", "{" + q.getNamespaceUri() + "}" + q.getLocalName(), text(v)));
                    }
                }
                case ATTRIBUTE_XSI_TYPE -> {
                    d.decodeAttributeXsiType();
                    Value v = d.getAttributeValue();
                    if (collect) {
                        String name = v instanceof QNameValue qv
                                ? "{" + qv.getNamespaceUri() + "}" + qv.getLocalName()
                                : text(v);
                        out.add(new Ev("XSI_TYPE", name, null));
                    }
                }
                case ATTRIBUTE_XSI_NIL -> {
                    d.decodeAttributeXsiNil();
                    Value v = d.getAttributeValue();
                    if (collect) out.add(new Ev("XSI_NIL", null, text(v)));
                }
                case CHARACTERS, CHARACTERS_GENERIC, CHARACTERS_GENERIC_UNDECLARED -> {
                    Value v = d.decodeCharacters();
                    if (collect) out.add(new Ev("CH", null, text(v)));
                }
                default -> throw new IllegalStateException("unexpected event " + et);
            }
        }
        if (!collect && count == 0) {
            throw new IllegalStateException("no events");
        }
        return out;
    }

    private static String text(Value v) {
        return v == null ? "" : v.toString();
    }

    /**
     * Encodes an XSD the way exip loads schemas: schema-less EXI with
     * Preserve.prefixes and the options in the header (xsd2exip's Main.generate).
     */
    static void schemaForExip(Path xsd, Path out) throws Exception {
        EXIFactory f = DefaultEXIFactory.newInstance();
        FidelityOptions fo = FidelityOptions.createDefault();
        fo.setFidelity(FidelityOptions.FEATURE_PREFIX, true);
        f.setFidelityOptions(fo);
        EncodingOptions eo = EncodingOptions.createDefault();
        eo.setOption(EncodingOptions.INCLUDE_OPTIONS);
        f.setEncodingOptions(eo);
        try (var os = Files.newOutputStream(out)) {
            EXIResult result = new EXIResult(f);
            result.setOutputStream(os);
            SAXParserFactory spf = SAXParserFactory.newDefaultInstance();
            spf.setNamespaceAware(true);
            XMLReader r = spf.newSAXParser().getXMLReader();
            r.setContentHandler(result.getHandler());
            r.parse(xsd.toUri().toString());
        }
    }
}
