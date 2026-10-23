package org.acm.nhenehan.rexiinterop;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.util.List;
import org.acm.nhenehan.rexiinterop.Model.Ev;
import org.junit.jupiter.api.Test;

/** The comparison logic the whole matrix depends on. */
class ModelTest {
    @Test
    void eventLinesRoundTripWithEscapes() {
        List<Ev> events = List.of(Ev.of("SD"), new Ev("SE", "{urn:x}a", null),
                new Ev("AT", "{}b", "x y\\z\n\t"), new Ev("CH", null, "line1\r\nline2"),
                new Ev("XSI_TYPE", "{urn:x}T", null), new Ev("XSI_NIL", null, "true"), Ev.of("EE"),
                Ev.of("ED"));
        String dump = Ev.dump(events);
        assertEquals(events, Ev.parseAll(dump));
        assertTrue(dump.contains("AT {}b x y\\\\z\\n\\t\n"), dump);
    }

    @Test
    void valuesCompareAsWrittenNumbersOrBooleans() {
        assertTrue(Model.valueEquals("24.95", "2495E-2"));
        assertTrue(Model.valueEquals("85.00", "85E0"));
        assertTrue(Model.valueEquals("3.10", "3.1"));
        assertTrue(Model.valueEquals("1", "true"));
        assertTrue(Model.valueEquals(" gold ", "gold"));
        assertFalse(Model.valueEquals("24.95", "24.96"));
        assertFalse(Model.valueEquals("1", "2"));
        assertFalse(Model.valueEquals("CUST-42", "CUST-43"));
        assertFalse(Model.valueEquals("2026-05-01T10:15:00Z", "2026--2--31T-1:63:00+14:59"));
    }

    @Test
    void diffReportsTheFirstDifference() {
        List<Ev> a = List.of(Ev.of("SD"), new Ev("CH", null, "1.50"), Ev.of("ED"));
        assertNull(Model.diff(a, List.of(Ev.of("SD"), new Ev("CH", null, "15E-1"), Ev.of("ED"))));
        assertEquals("event 2: expected `CH 1.50`, got `CH 2`",
                Model.diff(a, List.of(Ev.of("SD"), new Ev("CH", null, "2"), Ev.of("ED"))));
        assertEquals("expected 3 events, got 2", Model.diff(a, a.subList(0, 2)));
    }

    @Test
    void referenceEventsFollowStrictEventOrder() throws Exception {
        String xml = "<r xmlns='urn:x' xmlns:t='urn:t' xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance'"
                + " z='1' a='2' xsi:nil='false' xsi:type='t:T'>\n  <c>v</c>\n</r>";
        List<Ev> ev = Codec.referenceEvents(xml.getBytes(StandardCharsets.UTF_8));
        assertEquals(List.of(Ev.of("SD"), new Ev("SE", "{urn:x}r", null), new Ev("XSI_TYPE", "{urn:t}T", null),
                new Ev("XSI_NIL", null, "false"), new Ev("AT", "{}a", "2"), new Ev("AT", "{}z", "1"),
                new Ev("SE", "{urn:x}c", null), new Ev("CH", null, "v"), Ev.of("EE"), Ev.of("EE"),
                Ev.of("ED")), ev);
    }
}
