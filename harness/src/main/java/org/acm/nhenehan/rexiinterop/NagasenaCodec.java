package org.acm.nhenehan.rexiinterop;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.file.Files;
import javax.xml.parsers.SAXParserFactory;
import org.acm.nhenehan.rexiinterop.Model.Bench;
import org.acm.nhenehan.rexiinterop.Model.Case;
import org.acm.nhenehan.rexiinterop.Model.Outcome;
import org.acm.nhenehan.rexiinterop.Model.Profile;
import org.acm.nhenehan.rexiinterop.Model.Timing;
import org.openexi.proc.HeaderOptionsOutputType;
import org.openexi.proc.common.AlignmentType;
import org.openexi.proc.common.GrammarOptions;
import org.openexi.proc.grammars.GrammarCache;
import org.openexi.sax.EXIReader;
import org.openexi.sax.Transmogrifier;
import org.openexi.schema.EXISchema;
import org.openexi.scomp.EXISchemaFactory;
import org.xml.sax.InputSource;
import org.xml.sax.helpers.DefaultHandler;

/** OpenEXI Nagasena (the W3C test suite's jar), in-process through its SAX API. */
final class NagasenaCodec implements Codec {
    @Override
    public String name() {
        return "Nagasena";
    }

    @Override
    public String unsupported(Profile p) {
        return null;
    }

    private static GrammarCache grammars(Case c, Profile p) throws Exception {
        EXISchema schema = new EXISchemaFactory().compile(new InputSource(c.mainXsd().toUri().toString()));
        return new GrammarCache(schema, p.strict ? GrammarOptions.STRICT_OPTIONS : GrammarOptions.DEFAULT_OPTIONS);
    }

    private static AlignmentType alignment(Profile p) {
        return switch (p.alignment) {
            case BIT_PACKED -> AlignmentType.bitPacked;
            case BYTE_ALIGNED -> AlignmentType.byteAligned;
            case COMPRESSION -> AlignmentType.compress;
        };
    }

    @Override
    public Outcome encode(Case c, Profile p, Bench bench) throws Exception {
        long t0 = System.nanoTime();
        GrammarCache gc = grammars(c, p);
        SAXParserFactory spf = SAXParserFactory.newDefaultInstance();
        spf.setNamespaceAware(true);
        Transmogrifier t = new Transmogrifier(spf);
        t.setGrammarCache(gc);
        t.setAlignmentType(alignment(p));
        t.setOutputOptions(HeaderOptionsOutputType.none);
        t.setOutputCookie(false);
        long setup = System.nanoTime() - t0;
        byte[] xml = Files.readAllBytes(c.xml());
        byte[][] last = new byte[1][];
        Codec.Op once = () -> {
            ByteArrayOutputStream out = new ByteArrayOutputStream(1024);
            t.setOutputStream(out);
            t.encode(new InputSource(new ByteArrayInputStream(xml)));
            last[0] = out.toByteArray();
        };
        once.run();
        byte[] bytes = last[0];
        Timing timing = Codec.time("xml", setup, bench, once);
        return Outcome.encoded(bytes, timing);
    }

    @Override
    public Outcome decode(Case c, Profile p, byte[] exi, Bench bench) throws Exception {
        long t0 = System.nanoTime();
        EXIReader r = new EXIReader();
        r.setGrammarCache(grammars(c, p));
        r.setAlignmentType(alignment(p));
        long setup = System.nanoTime() - t0;
        Codec.SaxEvents events = new Codec.SaxEvents();
        r.setContentHandler(events);
        r.parse(new InputSource(new ByteArrayInputStream(exi)));
        DefaultHandler sink = new DefaultHandler();
        Timing timing = Codec.time("exi", setup, bench, () -> {
            r.setContentHandler(sink);
            r.parse(new InputSource(new ByteArrayInputStream(exi)));
        });
        return Outcome.decoded(events.events, timing);
    }
}
