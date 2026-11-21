# EXI interop and benchmark report

Generated 2026-11-21T19:32:38Z on AMD Ryzen 7 7840U w/ Radeon 780M Graphics, Linux 7.2.6-76070206-generic, Java 25.0.4.1.

- **rexi-interop**: 7acf2fcb9b43 (with local changes)
- **EXIficient**: 1.0.7 (Maven Central), in-process
- **Nagasena**: W3C exi-testsuite 23d5f587ba3e candidates/openexi/lib, in-process
- **exip**: nhenehan/exip b2870c106f0c, Release build, drivers/exip (cc (Ubuntu 13.3.0-6ubuntu2~24.04.1) 13.3.0)
- **rexi**: v0.3.1 (50063e9b9a97), release build, rexi bench (rustc 1.99.0 (b940084d7 2026-09-28))
- **Nagasena.NET**: OpenEXI svn r1188 trunk/nagasenacs, drivers/nagasena-cs (.NET 10.0.402), schemas compiled by Java Nagasena
- **EXIFormat**: v0.1.0 (44bfbfeabd3c), bin/exiformat (julia version 1.12.7)

Cases (fixtures/cases.tsv):

- S1: `order-complex.xml` against `order-complex.xsd` — exip interop-tests
- S2: `telemetry-complex.xml` against `telemetry-complex.xsd` — exip interop-tests
- S3: `inheritance-extension.xml` against `inheritance-extension.xsd` — exip interop-tests
- S4: `cross-schema-main.xml` against `cross-schema-main.xsd` + imports — exip interop-tests
- S5: `polymorphic-choice.xml` against `polymorphic-choice.xsd` — exip interop-tests
- R1: `order-complex.xml` against `order-complex.xsd` — rexi fixtures
- R2: `telemetry-complex.xml` against `telemetry-complex.xsd` — rexi fixtures
- R3: `inheritance-extension.xml` against `inheritance-extension.xsd` — rexi fixtures
- R4: `cross-schema-main.xml` against `cross-schema-main.xsd` + imports — rexi fixtures
- R5: `polymorphic-choice.xml` against `polymorphic-choice.xsd` — rexi fixtures
- X1: `shapes.xml` against `shapes.xsd` — xsi:type casts (rexi-interop)
- X2: `readings.xml` against `readings.xsd` — xsi:nil (rexi-interop)
- U1: `multilingual.xml` against `multilingual.xsd` — multilingual UTF-8 (rexi-interop)
- B1: `binary-types.xml` against `binary-types.xsd` — hexBinary / base64Binary (rexi-interop)
- F1: `home-water.xml` against `home-water.xsd`, as a fragment (fragment option set) — home water system log as an EXI fragment (rexi-interop)

All profiles carry the options out of band: no options document, no cookie.
Decoded events are compared with the XML's own events (attributes in EXI order, whitespace-only text dropped); values compare as written, then as numbers, then as booleans. ✓ correct, ✗ failed (see notes), – not supported.

## P0: strict, bit-packed

### Encode by X, decode by Y

| encoder → decoder | S1 | S2 | S3 | S4 | S5 | R1 | R2 | R3 | R4 | R5 | X1 | X2 | U1 | B1 | F1 |
|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|
| EXIficient → EXIficient | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✗[1] | ✓ | ✓ | ✓ |
| EXIficient → Nagasena | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✗[2] | ✓ | ✓ | ✓ |
| EXIficient → Nagasena.NET | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✗[3] | ✓ | ✓ | ✓ |
| EXIficient → exip | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✗[4] | ✓ | ✓ | – |
| EXIficient → rexi | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✗[5] | ✓ | ✓ | ✓ |
| EXIficient → EXIFormat | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✗[6] | ✓ | ✓ | – |
| Nagasena → EXIficient | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ |
| Nagasena → Nagasena | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ |
| Nagasena → Nagasena.NET | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ |
| Nagasena → exip | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | – |
| Nagasena → rexi | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ |
| Nagasena → EXIFormat | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | – |
| Nagasena.NET → EXIficient | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ |
| Nagasena.NET → Nagasena | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ |
| Nagasena.NET → Nagasena.NET | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ |
| Nagasena.NET → exip | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | – |
| Nagasena.NET → rexi | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ |
| Nagasena.NET → EXIFormat | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | – |
| exip → EXIficient | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | – |
| exip → Nagasena | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | – |
| exip → Nagasena.NET | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | – |
| exip → exip | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | – |
| exip → rexi | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | – |
| exip → EXIFormat | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | – |
| rexi → EXIficient | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ |
| rexi → Nagasena | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ |
| rexi → Nagasena.NET | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ |
| rexi → exip | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | – |
| rexi → rexi | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ |
| rexi → EXIFormat | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | – |
| EXIFormat → EXIficient | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | – |
| EXIFormat → Nagasena | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | – |
| EXIFormat → Nagasena.NET | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | – |
| EXIFormat → exip | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | – |
| EXIFormat → rexi | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | – |
| EXIFormat → EXIFormat | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | – |

### Bytes compared with EXIficient's

| encoder | S1 | S2 | S3 | S4 | S5 | R1 | R2 | R3 | R4 | R5 | X1 | X2 | U1 | B1 | F1 |
|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|
| EXIficient | = | = | = | = | = | = | = | = | = | = | = | = | = | = | = |
| Nagasena | = | = | = | = | = | = | = | = | = | = | = | ≠ 51 B vs 50 B | = | = | = |
| Nagasena.NET | = | = | = | = | = | = | = | = | = | = | = | ≠ 51 B vs 50 B | = | = | = |
| exip | = | = | = | = | = | = | = | = | = | = | = | ≠ 51 B vs 50 B | = | = | – |
| rexi | = | = | = | = | = | = | = | = | = | = | = | ≠ 51 B vs 50 B | = | = | = |
| EXIFormat | = | = | = | = | = | = | = | = | = | = | = | ≠ 51 B vs 50 B | = | = | – |

### Timing: median µs (p10–p90)

Encoders time their natural input: XML (parsing included) for EXIficient, Nagasena (Java and .NET), rexi and EXIFormat; a pre-parsed event stream for exip and for the `events` rows of rexi and EXIFormat. Decoders all decode EXIficient's stream. Schema compilation is excluded.

| implementation | op | S1 | S2 | S3 | S4 | S5 | R1 | R2 | R3 | R4 | R5 | X1 | X2 | U1 | B1 | F1 |
|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|
| EXIficient | encode (xml) | 13.6 (13.3–17.8) | 13.7 (13.3–107.0) | 93.7 (10.7–115.9) | 13.0 (12.7–16.2) | 12.0 (11.7–12.6) | 16.2 (15.9–19.6) | 19.6 (17.0–30.1) | 12.2 (11.8–13.8) | 14.9 (14.4–16.7) | 15.7 (15.0–19.4) | 15.5 (15.2–16.1) | 17.4 (15.7–28.2) | 27.6 (26.9–31.3) | 11.8 (11.6–15.5) | 37.4 (37.0–41.6) |
| EXIficient | decode (exi) | 1.3 (1.1–2.1) | 1.1 (1.1–1.1) | 0.7 (0.7–0.8) | 1.0 (0.9–1.0) | 1.1 (1.0–1.1) | 1.9 (1.9–1.9) | 2.4 (2.1–3.2) | 1.4 (1.4–1.4) | 1.2 (1.2–1.2) | 1.6 (1.6–1.7) | 1.4 (1.4–1.4) | 1.5 (1.5–2.4) | 6.0 (5.9–6.1) | 0.6 (0.6–0.7) | 5.3 (5.2–5.5) |
| Nagasena | encode (xml) | 6.1 (6.0–6.3) | 6.0 (5.4–10.5) | 4.8 (4.7–5.0) | 5.0 (4.8–5.2) | 5.3 (5.2–5.4) | 7.0 (6.9–7.2) | 10.0 (9.9–10.2) | 6.2 (6.1–6.4) | 5.9 (5.7–6.1) | 7.2 (7.1–7.5) | 67.9 (7.9–90.6) | 67.8 (7.9–91.1) | 20.3 (18.2–21.3) | 4.0 (3.9–4.3) | 33.1 (28.7–37.9) |
| Nagasena | decode (exi) | 0.8 (0.8–0.8) | 0.9 (0.8–0.9) | 0.6 (0.5–0.6) | 0.7 (0.7–0.7) | 0.9 (0.9–0.9) | 1.6 (1.5–2.6) | 1.8 (1.8–1.8) | 1.1 (1.1–1.2) | 0.8 (0.8–0.8) | 1.4 (1.3–1.4) | 1.3 (1.3–1.4) | 1.4 (1.4–1.5) | 4.9 (4.8–4.9) | 0.6 (0.6–0.6) | 5.3 (5.3–5.4) |
| Nagasena.NET | encode (xml) | 6.4 (6.1–6.8) | 6.3 (6.0–6.6) | 6.4 (4.6–8.2) | 5.8 (5.1–6.5) | 4.8 (4.6–5.0) | 8.2 (7.8–10.2) | 11.4 (10.9–14.8) | 6.0 (5.5–6.7) | 5.4 (5.1–5.6) | 9.5 (8.8–14.9) | 7.5 (7.3–8.0) | 8.5 (7.9–9.5) | 25.4 (23.2–38.3) | 3.4 (3.0–3.7) | 19.0 (18.3–22.1) |
| Nagasena.NET | decode (exi) | 0.9 (0.8–1.4) | 1.1 (0.7–1.3) | 0.9 (0.6–1.1) | 1.0 (0.7–1.2) | 0.8 (0.7–1.7) | 1.6 (1.5–2.1) | 1.7 (1.7–3.1) | 1.1 (1.0–1.6) | 0.7 (0.7–1.2) | 1.7 (1.3–2.3) | 1.1 (1.0–1.7) | 1.5 (1.0–2.0) | 5.1 (4.6–5.4) | 0.5 (0.3–0.6) | 4.4 (4.2–4.6) |
| exip | encode (events) | 5.3 (5.2–5.3) | 5.4 (5.4–5.4) | 4.0 (4.0–4.1) | 4.9 (4.8–4.9) | 4.7 (4.7–6.4) | 6.5 (6.5–6.7) | 7.3 (7.3–7.4) | 5.7 (5.6–5.8) | 4.6 (4.5–4.7) | 6.5 (6.5–6.6) | 5.0 (4.9–5.0) | 6.1 (6.0–6.1) | 124.5 (14.0–220.1) | 4.3 (4.3–4.4) | – |
| exip | decode (exi) | 1.6 (1.6–1.6) | 1.5 (1.5–1.5) | 0.9 (0.9–0.9) | 1.1 (1.1–1.1) | 1.3 (1.2–1.3) | 2.5 (2.5–2.6) | 3.1 (3.1–3.2) | 1.8 (1.7–1.8) | 1.1 (1.1–1.2) | 2.2 (2.2–2.3) | 1.7 (1.7–1.8) | 2.0 (2.0–2.0) | 6.8 (6.7–6.9) | 0.8 (0.8–0.8) | – |
| rexi | encode (xml) | 3.5 (3.4–3.6) | 3.5 (3.4–4.7) | 16.2 (1.8–16.8) | 2.7 (2.6–2.7) | 2.8 (2.8–2.9) | 4.6 (4.5–4.7) | 7.1 (7.0–7.3) | 6.2 (5.7–6.9) | 2.9 (2.9–3.0) | 4.5 (4.4–4.7) | 4.7 (4.6–4.9) | 5.4 (5.3–5.8) | 15.4 (15.0–15.8) | 2.1 (2.0–2.1) | 18.8 (16.2–19.1) |
| rexi | encode (events) | 2.1 (2.0–2.1) | 15.9 (1.7–16.9) | 1.0 (1.0–1.1) | 1.7 (1.6–1.7) | 1.6 (1.6–1.7) | 3.2 (3.1–3.2) | 4.9 (4.8–5.0) | 3.2 (3.0–3.5) | 1.7 (1.7–1.8) | 2.8 (2.7–2.9) | 3.1 (3.0–3.1) | 2.9 (2.8–23.1) | 11.6 (11.5–12.0) | 1.1 (1.1–1.2) | 12.8 (11.1–13.0) |
| rexi | decode (exi) | 0.9 (0.9–0.9) | 11.5 (1.5–12.3) | 0.6 (0.6–0.6) | 0.7 (0.7–0.7) | 0.7 (0.7–0.7) | 1.2 (1.2–1.2) | 1.7 (1.6–1.7) | 2.0 (1.8–2.1) | 0.7 (0.7–0.7) | 1.2 (1.0–1.6) | 1.2 (1.2–1.3) | 10.1 (1.1–10.6) | 4.8 (4.8–4.9) | 0.4 (0.4–0.5) | 4.9 (4.8–5.0) |
| EXIFormat | encode (xml) | 5.6 (5.4–7.0) | 5.5 (5.3–5.8) | 3.4 (3.2–5.3) | 4.1 (4.0–4.4) | 5.1 (4.9–5.4) | 8.1 (7.8–10.6) | 10.5 (10.2–11.7) | 5.0 (4.8–5.5) | 4.3 (4.1–4.6) | 7.0 (6.8–7.6) | 7.5 (7.2–7.9) | 9.3 (8.8–79.9) | 23.9 (22.9–35.5) | 3.6 (3.4–3.9) | – |
| EXIFormat | encode (events) | 3.0 (2.8–3.2) | 3.2 (3.1–3.4) | 1.8 (1.7–2.0) | 1.9 (1.8–2.1) | 2.8 (2.7–3.1) | 4.3 (4.1–6.1) | 6.6 (6.5–7.0) | 6.5 (5.9–7.3) | 3.2 (3.0–3.4) | 4.2 (3.9–38.1) | 5.4 (5.3–7.9) | 4.5 (4.3–5.2) | 17.3 (16.4–25.8) | 2.3 (2.2–2.6) | – |
| EXIFormat | decode (exi) | 1.3 (1.1–2.6) | 1.0 (0.9–1.2) | 0.6 (0.6–0.8) | 0.6 (0.6–0.8) | 0.8 (0.8–1.0) | 1.5 (1.4–1.7) | 2.2 (2.0–3.8) | 1.1 (1.0–1.4) | 0.8 (0.7–0.9) | 1.6 (1.4–2.3) | 1.9 (1.3–3.7) | 1.3 (1.2–1.5) | 7.4 (6.7–12.6) | 0.5 (0.5–0.6) | – |

## P1: non-strict, bit-packed

### Encode by X, decode by Y

| encoder → decoder | S1 | S2 | S3 | S4 | S5 | R1 | R2 | R3 | R4 | R5 | X1 | X2 | U1 | B1 | F1 |
|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|
| EXIficient → EXIficient | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✗[7] | ✓ | ✓ | ✓ |
| EXIficient → Nagasena | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✗[8] | ✓ | ✓ | ✓ |
| EXIficient → Nagasena.NET | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✗[9] | ✓ | ✓ | ✓ |
| EXIficient → exip | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✗[10] | ✓ | ✓ | – |
| EXIficient → rexi | – | – | – | – | – | – | – | – | – | – | – | – | – | – | – |
| EXIficient → EXIFormat | – | – | – | – | – | – | – | – | – | – | – | – | – | – | – |
| Nagasena → EXIficient | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ |
| Nagasena → Nagasena | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ |
| Nagasena → Nagasena.NET | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ |
| Nagasena → exip | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | – |
| Nagasena → rexi | – | – | – | – | – | – | – | – | – | – | – | – | – | – | – |
| Nagasena → EXIFormat | – | – | – | – | – | – | – | – | – | – | – | – | – | – | – |
| Nagasena.NET → EXIficient | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ |
| Nagasena.NET → Nagasena | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ |
| Nagasena.NET → Nagasena.NET | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ |
| Nagasena.NET → exip | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | – |
| Nagasena.NET → rexi | – | – | – | – | – | – | – | – | – | – | – | – | – | – | – |
| Nagasena.NET → EXIFormat | – | – | – | – | – | – | – | – | – | – | – | – | – | – | – |
| exip → EXIficient | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | – |
| exip → Nagasena | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | – |
| exip → Nagasena.NET | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | – |
| exip → exip | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | – |
| exip → rexi | – | – | – | – | – | – | – | – | – | – | – | – | – | – | – |
| exip → EXIFormat | – | – | – | – | – | – | – | – | – | – | – | – | – | – | – |
| rexi → EXIficient | – | – | – | – | – | – | – | – | – | – | – | – | – | – | – |
| rexi → Nagasena | – | – | – | – | – | – | – | – | – | – | – | – | – | – | – |
| rexi → Nagasena.NET | – | – | – | – | – | – | – | – | – | – | – | – | – | – | – |
| rexi → exip | – | – | – | – | – | – | – | – | – | – | – | – | – | – | – |
| rexi → rexi | – | – | – | – | – | – | – | – | – | – | – | – | – | – | – |
| rexi → EXIFormat | – | – | – | – | – | – | – | – | – | – | – | – | – | – | – |
| EXIFormat → EXIficient | – | – | – | – | – | – | – | – | – | – | – | – | – | – | – |
| EXIFormat → Nagasena | – | – | – | – | – | – | – | – | – | – | – | – | – | – | – |
| EXIFormat → Nagasena.NET | – | – | – | – | – | – | – | – | – | – | – | – | – | – | – |
| EXIFormat → exip | – | – | – | – | – | – | – | – | – | – | – | – | – | – | – |
| EXIFormat → rexi | – | – | – | – | – | – | – | – | – | – | – | – | – | – | – |
| EXIFormat → EXIFormat | – | – | – | – | – | – | – | – | – | – | – | – | – | – | – |

### Timing: median µs (p10–p90)

Encoders time their natural input: XML (parsing included) for EXIficient, Nagasena (Java and .NET), rexi and EXIFormat; a pre-parsed event stream for exip and for the `events` rows of rexi and EXIFormat. Decoders all decode EXIficient's stream. Schema compilation is excluded.

| implementation | op | S1 | S2 | S3 | S4 | S5 | R1 | R2 | R3 | R4 | R5 | X1 | X2 | U1 | B1 | F1 |
|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|
| EXIficient | encode (xml) | 14.2 (13.9–18.9) | 14.1 (13.7–22.1) | 12.6 (12.2–19.6) | 11.7 (11.5–12.2) | 13.9 (12.3–16.7) | 16.7 (14.6–20.4) | 20.0 (19.7–20.6) | 14.2 (13.8–19.6) | 14.0 (13.2–17.8) | 16.9 (16.7–17.7) | 17.6 (17.4–18.0) | 17.8 (16.0–22.7) | 31.1 (30.7–35.4) | 12.2 (11.9–12.5) | 38.3 (33.2–47.7) |
| EXIficient | decode (exi) | 1.3 (1.3–2.0) | 1.4 (1.2–2.2) | 0.9 (0.9–0.9) | 1.1 (1.0–1.1) | 1.4 (1.3–1.4) | 2.0 (2.0–2.0) | 2.9 (2.7–3.7) | 1.4 (1.4–1.5) | 1.1 (1.1–1.1) | 1.7 (1.6–1.7) | 1.7 (1.7–1.7) | 1.8 (1.7–1.8) | 6.7 (6.7–6.9) | 0.7 (0.6–0.7) | 4.9 (4.9–5.0) |
| Nagasena | encode (xml) | 6.3 (6.1–9.5) | 6.3 (5.9–9.7) | 4.6 (4.4–4.8) | 4.9 (4.8–5.1) | 6.2 (6.0–6.4) | 8.1 (8.0–8.8) | 10.1 (10.0–10.3) | 6.3 (6.2–6.5) | 6.7 (6.5–10.4) | 8.1 (8.0–8.3) | 7.8 (7.5–9.6) | 7.8 (7.5–12.0) | 17.9 (17.5–18.6) | 4.5 (4.4–4.6) | 34.3 (33.8–41.7) |
| Nagasena | decode (exi) | 1.0 (1.0–1.0) | 0.9 (0.9–0.9) | 0.6 (0.6–0.6) | 0.8 (0.8–0.8) | 1.0 (1.0–1.0) | 1.6 (1.6–1.7) | 2.2 (2.2–2.2) | 1.2 (1.2–1.2) | 1.0 (1.0–1.0) | 1.6 (1.6–1.6) | 1.4 (1.4–1.4) | 1.5 (1.3–2.1) | 4.9 (4.8–5.0) | 0.5 (0.5–0.6) | 5.0 (4.8–5.0) |
| Nagasena.NET | encode (xml) | 6.3 (6.0–9.2) | 6.1 (5.8–7.9) | 4.4 (4.1–5.0) | 5.1 (4.8–5.5) | 5.7 (5.4–6.8) | 9.4 (8.7–12.0) | 12.3 (11.4–18.9) | 6.2 (5.6–6.7) | 5.3 (5.1–5.5) | 9.4 (8.4–11.8) | 9.6 (9.1–10.3) | 9.0 (8.6–10.2) | 20.2 (19.7–21.7) | 3.2 (3.1–3.4) | 22.5 (21.9–25.0) |
| Nagasena.NET | decode (exi) | 1.0 (0.8–1.4) | 1.0 (0.7–1.2) | 0.8 (0.6–0.9) | 0.9 (0.7–1.1) | 1.4 (1.0–3.1) | 1.5 (1.4–2.2) | 1.9 (1.8–2.8) | 1.2 (1.2–12.9) | 1.1 (0.8–1.8) | 1.4 (1.4–3.9) | 1.1 (0.9–1.6) | 1.1 (1.1–1.7) | 4.6 (4.5–4.8) | 0.6 (0.4–0.7) | 4.2 (3.9–4.6) |
| exip | encode (events) | 4.6 (4.5–5.4) | 5.5 (5.4–5.5) | 4.6 (4.6–4.6) | 4.9 (4.9–4.9) | 5.5 (5.5–5.5) | 6.6 (6.5–6.7) | 7.4 (7.4–7.5) | 5.7 (5.6–5.8) | 5.3 (5.2–5.4) | 6.6 (6.5–6.6) | 5.1 (5.0–5.2) | 5.2 (5.2–5.3) | 15.6 (15.5–15.8) | 3.7 (3.7–3.8) | – |
| exip | decode (exi) | 1.7 (1.7–1.8) | 1.8 (1.8–1.8) | 1.1 (1.1–1.1) | 1.1 (1.1–1.1) | 1.5 (1.5–1.5) | 3.0 (2.9–3.1) | 3.3 (3.3–3.4) | 1.8 (1.8–1.9) | 1.3 (1.3–1.4) | 2.3 (2.3–2.3) | 2.0 (1.9–2.0) | 1.8 (1.8–1.8) | 7.0 (6.9–7.3) | 0.8 (0.8–0.9) | – |

## P2: strict, byte-aligned

### Encode by X, decode by Y

| encoder → decoder | S1 | S2 | S3 | S4 | S5 | R1 | R2 | R3 | R4 | R5 | X1 | X2 | U1 | B1 | F1 |
|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|
| EXIficient → EXIficient | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✗[11] | ✓ | ✓ | ✓ |
| EXIficient → Nagasena | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✗[12] | ✓ | ✓ | ✓ |
| EXIficient → Nagasena.NET | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✗[13] | ✓ | ✓ | ✓ |
| EXIficient → exip | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✗[14] | ✓ | ✓ | – |
| EXIficient → rexi | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✗[15] | ✓ | ✓ | ✓ |
| EXIficient → EXIFormat | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✗[16] | ✓ | ✓ | – |
| Nagasena → EXIficient | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ |
| Nagasena → Nagasena | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ |
| Nagasena → Nagasena.NET | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ |
| Nagasena → exip | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | – |
| Nagasena → rexi | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ |
| Nagasena → EXIFormat | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | – |
| Nagasena.NET → EXIficient | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ |
| Nagasena.NET → Nagasena | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ |
| Nagasena.NET → Nagasena.NET | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ |
| Nagasena.NET → exip | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | – |
| Nagasena.NET → rexi | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ |
| Nagasena.NET → EXIFormat | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | – |
| exip → EXIficient | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | – |
| exip → Nagasena | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | – |
| exip → Nagasena.NET | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | – |
| exip → exip | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | – |
| exip → rexi | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | – |
| exip → EXIFormat | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | – |
| rexi → EXIficient | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ |
| rexi → Nagasena | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ |
| rexi → Nagasena.NET | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ |
| rexi → exip | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | – |
| rexi → rexi | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ |
| rexi → EXIFormat | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | – |
| EXIFormat → EXIficient | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | – |
| EXIFormat → Nagasena | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | – |
| EXIFormat → Nagasena.NET | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | – |
| EXIFormat → exip | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | – |
| EXIFormat → rexi | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | – |
| EXIFormat → EXIFormat | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | – |

### Bytes compared with EXIficient's

| encoder | S1 | S2 | S3 | S4 | S5 | R1 | R2 | R3 | R4 | R5 | X1 | X2 | U1 | B1 | F1 |
|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|
| EXIficient | = | = | = | = | = | = | = | = | = | = | = | = | = | = | = |
| Nagasena | = | = | = | = | = | = | = | = | = | = | = | ≠ 87 B vs 84 B | = | = | = |
| Nagasena.NET | = | = | = | = | = | = | = | = | = | = | = | ≠ 87 B vs 84 B | = | = | = |
| exip | = | = | = | = | = | = | = | = | = | = | = | ≠ 87 B vs 84 B | = | = | – |
| rexi | = | = | = | = | = | = | = | = | = | = | = | ≠ 87 B vs 84 B | = | = | = |
| EXIFormat | = | = | = | = | = | = | = | = | = | = | = | ≠ 87 B vs 84 B | = | = | – |

### Timing: median µs (p10–p90)

Encoders time their natural input: XML (parsing included) for EXIficient, Nagasena (Java and .NET), rexi and EXIFormat; a pre-parsed event stream for exip and for the `events` rows of rexi and EXIFormat. Decoders all decode EXIficient's stream. Schema compilation is excluded.

| implementation | op | S1 | S2 | S3 | S4 | S5 | R1 | R2 | R3 | R4 | R5 | X1 | X2 | U1 | B1 | F1 |
|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|
| EXIficient | encode (xml) | 14.1 (13.8–14.6) | 12.5 (12.3–13.0) | 12.2 (11.9–22.1) | 15.1 (14.4–23.3) | 13.9 (13.5–21.6) | 15.4 (14.9–17.3) | 17.1 (16.7–17.7) | 12.5 (12.2–13.0) | 14.8 (14.5–19.6) | 15.7 (14.8–25.2) | 17.8 (17.2–30.7) | 16.4 (16.0–29.1) | 29.9 (29.5–37.3) | 12.4 (11.8–29.2) | 39.0 (38.1–49.9) |
| EXIficient | decode (exi) | 1.4 (1.4–1.7) | 1.3 (1.3–1.4) | 0.9 (0.9–1.0) | 1.5 (1.1–2.3) | 1.4 (1.4–1.5) | 1.9 (1.9–2.0) | 2.7 (2.7–3.0) | 1.4 (1.4–1.4) | 1.3 (1.3–1.3) | 2.0 (2.0–3.1) | 1.8 (1.7–1.8) | 2.0 (1.9–3.4) | 6.9 (6.8–8.0) | 0.6 (0.6–0.6) | 5.7 (5.6–7.1) |
| Nagasena | encode (xml) | 6.4 (5.4–10.6) | 6.0 (5.9–6.2) | 5.3 (5.1–6.0) | 5.5 (5.4–5.7) | 10.5 (9.7–11.4) | 6.9 (6.7–8.0) | 8.7 (8.6–9.0) | 5.4 (5.3–5.5) | 6.5 (6.4–6.7) | 8.0 (7.8–8.2) | 8.5 (8.4–8.8) | 9.7 (8.8–18.3) | 19.5 (17.6–28.0) | 4.5 (4.4–4.6) | 34.4 (33.6–45.2) |
| Nagasena | decode (exi) | 1.0 (1.0–1.1) | 1.1 (1.1–1.2) | 0.8 (0.8–0.8) | 0.9 (0.9–0.9) | 1.1 (1.1–1.1) | 1.9 (1.8–2.5) | 2.1 (2.1–2.1) | 1.1 (1.1–1.2) | 1.0 (1.0–1.1) | 1.7 (1.7–1.8) | 1.9 (1.6–3.4) | 1.6 (1.6–1.7) | 6.7 (6.6–8.8) | 0.7 (0.7–0.7) | 6.0 (6.0–7.1) |
| Nagasena.NET | encode (xml) | 5.8 (5.6–6.0) | 6.2 (5.8–6.6) | 3.8 (3.7–4.1) | 5.3 (4.8–5.9) | 6.6 (5.8–9.7) | 8.1 (7.6–11.0) | 9.0 (8.7–9.3) | 6.2 (5.5–6.6) | 5.2 (5.0–5.4) | 8.2 (7.8–8.7) | 9.1 (8.4–14.8) | 9.0 (8.0–9.6) | 20.0 (19.5–21.1) | 3.9 (3.7–4.4) | 21.3 (20.8–22.6) |
| Nagasena.NET | decode (exi) | 1.0 (0.6–1.1) | 0.9 (0.8–2.0) | 0.6 (0.4–0.7) | 0.8 (0.5–1.1) | 1.0 (0.7–1.3) | 1.2 (1.1–2.6) | 1.3 (1.3–2.1) | 1.2 (0.8–1.8) | 1.2 (0.7–1.8) | 1.2 (1.1–3.1) | 1.0 (0.9–1.6) | 1.0 (1.0–1.7) | 4.3 (3.9–4.5) | 0.6 (0.4–0.6) | 6.5 (5.2–45.9) |
| exip | encode (events) | 4.4 (4.3–5.1) | 5.2 (5.1–5.3) | 3.9 (3.8–3.9) | 9.4 (8.2–10.0) | 5.2 (5.2–5.3) | 6.2 (6.2–6.3) | 6.1 (6.0–6.2) | 5.4 (5.4–5.5) | 4.4 (4.3–4.5) | 6.2 (6.2–6.4) | 5.6 (5.6–5.7) | 5.7 (5.7–5.8) | 14.1 (13.8–115.3) | 4.2 (4.1–4.2) | – |
| exip | decode (exi) | 1.6 (1.6–1.6) | 1.6 (1.5–1.6) | 0.9 (0.8–1.0) | 8.4 (1.1–9.3) | 1.3 (1.3–1.3) | 2.5 (2.5–2.5) | 2.6 (2.6–2.7) | 1.7 (1.7–1.8) | 1.2 (1.2–1.2) | 2.2 (2.1–2.2) | 1.9 (1.9–2.0) | 1.6 (1.6–1.7) | 6.6 (6.6–6.7) | 0.8 (0.8–0.8) | – |
| rexi | encode (xml) | 3.0 (2.8–3.1) | 3.6 (3.5–3.7) | 1.9 (1.9–2.0) | 2.3 (2.3–2.4) | 2.7 (2.6–2.8) | 4.9 (4.8–5.0) | 6.9 (6.8–7.0) | 3.1 (3.0–3.1) | 2.8 (2.7–2.9) | 4.8 (4.7–4.9) | 4.6 (4.5–6.5) | 4.5 (4.4–4.7) | 15.4 (14.4–17.1) | 1.9 (1.8–1.9) | 18.6 (16.3–19.0) |
| rexi | encode (events) | 1.9 (1.8–1.9) | 1.7 (1.6–1.7) | 1.0 (1.0–1.0) | 1.3 (1.3–1.4) | 1.5 (1.4–1.5) | 2.9 (2.8–2.9) | 3.8 (3.7–3.9) | 1.5 (1.4–1.5) | 1.5 (1.5–1.6) | 3.0 (2.9–3.0) | 2.6 (2.5–2.7) | 2.5 (2.4–2.6) | 11.1 (10.9–12.1) | 1.1 (1.1–1.1) | 12.5 (10.8–12.7) |
| rexi | decode (exi) | 0.9 (0.9–0.9) | 0.9 (0.8–0.9) | 0.5 (0.5–0.5) | 0.6 (0.6–0.7) | 0.8 (0.8–0.8) | 1.3 (1.2–1.3) | 1.5 (1.4–1.5) | 0.8 (0.8–0.8) | 0.7 (0.7–0.7) | 1.0 (1.0–1.0) | 1.2 (1.2–1.3) | 1.3 (1.2–1.3) | 4.5 (4.5–4.6) | 0.4 (0.4–0.6) | 4.3 (4.2–39.6) |
| EXIFormat | encode (xml) | 5.2 (4.8–5.8) | 5.7 (5.6–6.1) | 3.4 (3.2–3.7) | 4.1 (3.6–34.4) | 5.1 (4.8–8.7) | 8.4 (8.2–8.8) | 10.4 (10.1–10.8) | 4.7 (4.5–5.0) | 5.4 (5.2–5.7) | 7.2 (6.9–7.5) | 8.2 (8.0–8.6) | 9.2 (8.9–13.0) | 22.9 (21.2–25.1) | 3.2 (3.0–3.5) | – |
| EXIFormat | encode (events) | 3.0 (2.8–3.2) | 3.8 (3.5–5.5) | 2.2 (1.8–5.9) | 2.2 (2.0–4.0) | 2.3 (2.2–2.5) | 4.5 (4.3–5.0) | 6.0 (5.8–6.3) | 2.6 (2.5–2.8) | 2.8 (2.7–3.0) | 4.0 (3.8–4.4) | 4.8 (4.7–5.0) | 4.6 (4.4–5.0) | 16.9 (16.1–25.4) | 2.2 (2.1–2.6) | – |
| EXIFormat | decode (exi) | 1.0 (0.9–1.1) | 1.0 (0.9–1.2) | 0.6 (0.6–0.8) | 0.6 (0.6–0.9) | 0.9 (0.8–1.1) | 1.2 (1.1–1.3) | 1.7 (1.6–1.9) | 1.0 (0.9–1.1) | 0.8 (0.8–1.0) | 1.7 (1.4–3.4) | 1.2 (1.1–1.9) | 1.2 (1.1–1.3) | 6.6 (6.0–12.8) | 0.5 (0.5–0.7) | – |

## P3: strict, compression

### Encode by X, decode by Y

| encoder → decoder | S1 | S2 | S3 | S4 | S5 | R1 | R2 | R3 | R4 | R5 | X1 | X2 | U1 | B1 | F1 |
|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|
| EXIficient → EXIficient | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✗[17] | ✓ | ✓ | ✓ |
| EXIficient → Nagasena | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✗[18] | ✓ | ✓ | ✓ |
| EXIficient → Nagasena.NET | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✗[19] | ✓ | ✓ | ✓ |
| EXIficient → exip | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✗[20] | ✓ | ✓ | – |
| EXIficient → rexi | – | – | – | – | – | – | – | – | – | – | – | – | – | – | – |
| EXIficient → EXIFormat | – | – | – | – | – | – | – | – | – | – | – | – | – | – | – |
| Nagasena → EXIficient | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ |
| Nagasena → Nagasena | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ |
| Nagasena → Nagasena.NET | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ |
| Nagasena → exip | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | – |
| Nagasena → rexi | – | – | – | – | – | – | – | – | – | – | – | – | – | – | – |
| Nagasena → EXIFormat | – | – | – | – | – | – | – | – | – | – | – | – | – | – | – |
| Nagasena.NET → EXIficient | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ |
| Nagasena.NET → Nagasena | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ |
| Nagasena.NET → Nagasena.NET | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ |
| Nagasena.NET → exip | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | – |
| Nagasena.NET → rexi | – | – | – | – | – | – | – | – | – | – | – | – | – | – | – |
| Nagasena.NET → EXIFormat | – | – | – | – | – | – | – | – | – | – | – | – | – | – | – |
| exip → EXIficient | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | – |
| exip → Nagasena | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | – |
| exip → Nagasena.NET | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | – |
| exip → exip | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | – |
| exip → rexi | – | – | – | – | – | – | – | – | – | – | – | – | – | – | – |
| exip → EXIFormat | – | – | – | – | – | – | – | – | – | – | – | – | – | – | – |
| rexi → EXIficient | – | – | – | – | – | – | – | – | – | – | – | – | – | – | – |
| rexi → Nagasena | – | – | – | – | – | – | – | – | – | – | – | – | – | – | – |
| rexi → Nagasena.NET | – | – | – | – | – | – | – | – | – | – | – | – | – | – | – |
| rexi → exip | – | – | – | – | – | – | – | – | – | – | – | – | – | – | – |
| rexi → rexi | – | – | – | – | – | – | – | – | – | – | – | – | – | – | – |
| rexi → EXIFormat | – | – | – | – | – | – | – | – | – | – | – | – | – | – | – |
| EXIFormat → EXIficient | – | – | – | – | – | – | – | – | – | – | – | – | – | – | – |
| EXIFormat → Nagasena | – | – | – | – | – | – | – | – | – | – | – | – | – | – | – |
| EXIFormat → Nagasena.NET | – | – | – | – | – | – | – | – | – | – | – | – | – | – | – |
| EXIFormat → exip | – | – | – | – | – | – | – | – | – | – | – | – | – | – | – |
| EXIFormat → rexi | – | – | – | – | – | – | – | – | – | – | – | – | – | – | – |
| EXIFormat → EXIFormat | – | – | – | – | – | – | – | – | – | – | – | – | – | – | – |

### Timing: median µs (p10–p90)

Encoders time their natural input: XML (parsing included) for EXIficient, Nagasena (Java and .NET), rexi and EXIFormat; a pre-parsed event stream for exip and for the `events` rows of rexi and EXIFormat. Decoders all decode EXIficient's stream. Schema compilation is excluded.

| implementation | op | S1 | S2 | S3 | S4 | S5 | R1 | R2 | R3 | R4 | R5 | X1 | X2 | U1 | B1 | F1 |
|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|
| EXIficient | encode (xml) | 23.7 (21.7–28.2) | 23.9 (23.5–25.8) | 18.5 (17.6–20.3) | 23.0 (22.6–30.1) | 25.5 (23.8–29.8) | 30.2 (29.4–32.0) | 33.4 (32.9–34.6) | 24.3 (23.8–25.5) | 22.1 (21.5–23.3) | 31.9 (28.1–34.6) | 28.6 (25.1–29.8) | 26.4 (25.6–29.5) | 90.9 (87.3–100.4) | 17.9 (16.5–33.1) | 81.0 (72.6–97.3) |
| EXIficient | decode (exi) | 7.9 (6.2–13.3) | 12.6 (11.0–14.1) | 4.9 (4.3–7.0) | 6.1 (5.8–10.3) | 7.6 (7.4–9.0) | 14.3 (11.0–20.8) | 10.9 (10.7–11.4) | 8.1 (7.5–11.4) | 5.8 (5.6–6.1) | 9.9 (9.7–13.0) | 8.9 (8.6–9.7) | 7.9 (7.6–12.8) | 42.4 (41.4–43.8) | 5.7 (3.5–10.3) | 28.6 (28.3–32.7) |
| Nagasena | encode (xml) | 15.2 (13.9–23.9) | 14.7 (13.1–19.5) | 12.3 (11.8–16.4) | 14.7 (13.5–21.6) | 15.4 (14.6–19.7) | 17.7 (16.8–22.1) | 18.4 (17.8–22.6) | 15.8 (15.1–20.3) | 14.1 (13.5–18.3) | 17.1 (15.6–22.2) | 18.4 (17.3–23.0) | 16.1 (15.4–20.4) | 34.0 (32.9–38.3) | 12.5 (11.5–17.0) | 50.6 (48.1–363.5) |
| Nagasena | decode (exi) | 3.3 (2.9–4.3) | 3.0 (2.5–4.0) | 2.7 (2.2–3.7) | 3.3 (2.6–4.7) | 3.1 (2.7–4.1) | 3.9 (3.5–4.9) | 4.6 (4.2–5.5) | 3.3 (2.8–4.2) | 3.0 (2.5–4.2) | 3.8 (3.4–4.9) | 4.2 (3.9–5.2) | 3.5 (3.1–4.5) | 10.0 (9.7–11.1) | 2.7 (2.2–3.6) | 15.5 (11.3–19.6) |
| Nagasena.NET | encode (xml) | 65.9 (64.8–74.2) | 67.5 (60.2–83.7) | 63.2 (54.9–68.7) | 64.5 (63.5–70.2) | 67.5 (61.1–72.7) | 60.2 (58.0–63.8) | 72.6 (62.7–78.3) | 68.2 (60.4–80.9) | 62.1 (60.4–66.8) | 66.7 (60.5–75.0) | 65.1 (61.6–97.2) | 61.0 (59.3–64.2) | 69.9 (68.1–73.6) | 63.1 (53.5–68.7) | 78.8 (76.9–88.6) |
| Nagasena.NET | decode (exi) | 2.5 (2.2–3.1) | 2.0 (1.8–2.6) | 1.7 (1.4–2.0) | 2.1 (1.9–3.7) | 2.7 (2.4–5.1) | 2.9 (2.7–3.3) | 4.4 (3.6–34.2) | 2.7 (2.5–3.2) | 2.7 (2.1–4.5) | 3.5 (3.0–5.3) | 6.0 (5.8–6.4) | 2.2 (2.0–2.9) | 14.1 (13.7–14.7) | 1.7 (1.4–3.9) | 13.1 (12.8–14.6) |
| exip | encode (events) | 9.1 (9.0–9.3) | 8.2 (8.2–8.3) | 7.1 (7.0–7.1) | 8.5 (8.4–8.5) | 9.8 (9.8–9.9) | 10.4 (10.3–10.6) | 12.9 (12.7–19.9) | 10.0 (9.9–10.3) | 7.9 (7.9–8.1) | 12.9 (12.2–21.4) | 8.6 (8.5–8.9) | 8.8 (8.7–9.0) | 26.0 (25.6–26.9) | 6.9 (6.9–7.0) | – |
| exip | decode (exi) | 2.5 (2.5–2.6) | 19.8 (2.2–20.2) | 1.8 (1.8–1.8) | 1.8 (1.7–1.8) | 2.3 (2.3–2.3) | 3.9 (3.8–3.9) | 37.0 (4.0–37.7) | 2.7 (2.4–2.8) | 2.2 (2.1–2.2) | 3.8 (3.6–6.6) | 3.7 (3.6–3.8) | 2.7 (2.6–2.8) | 12.0 (11.9–12.2) | 1.4 (1.4–1.5) | – |

Timing: warm-up of at least 2000 runs and 3000 ms, then 2000 timed iterations per cell, each implementation timing itself in-process.

## Notes

1. P0 X2: EXIficient decoding EXIficient's stream: event 43: expected `XSI_NIL false`, got `CH 0`
2. P0 X2: Nagasena decoding EXIficient's stream: event 43: expected `XSI_NIL false`, got `CH 0`
3. P0 X2: Nagasena.NET decoding EXIficient's stream: event 43: expected `XSI_NIL false`, got `CH 0`
4. P0 X2: exip decoding EXIficient's stream: event 43: expected `XSI_NIL false`, got `CH 0`
5. P0 X2: rexi decoding EXIficient's stream: event 43: expected `XSI_NIL false`, got `CH 0`
6. P0 X2: EXIFormat decoding EXIficient's stream: event 43: expected `XSI_NIL false`, got `CH 0`
7. P1 X2: EXIficient decoding EXIficient's stream: event 43: expected `XSI_NIL false`, got `CH 0`
8. P1 X2: Nagasena decoding EXIficient's stream: event 43: expected `XSI_NIL false`, got `CH 0`
9. P1 X2: Nagasena.NET decoding EXIficient's stream: event 43: expected `XSI_NIL false`, got `CH 0`
10. P1 X2: exip decoding EXIficient's stream: event 43: expected `XSI_NIL false`, got `CH 0`
11. P2 X2: EXIficient decoding EXIficient's stream: event 43: expected `XSI_NIL false`, got `CH 0`
12. P2 X2: Nagasena decoding EXIficient's stream: event 43: expected `XSI_NIL false`, got `CH 0`
13. P2 X2: Nagasena.NET decoding EXIficient's stream: event 43: expected `XSI_NIL false`, got `CH 0`
14. P2 X2: exip decoding EXIficient's stream: event 43: expected `XSI_NIL false`, got `CH 0`
15. P2 X2: rexi decoding EXIficient's stream: event 43: expected `XSI_NIL false`, got `CH 0`
16. P2 X2: EXIFormat decoding EXIficient's stream: event 43: expected `XSI_NIL false`, got `CH 0`
17. P3 X2: EXIficient decoding EXIficient's stream: event 43: expected `XSI_NIL false`, got `CH 0`
18. P3 X2: Nagasena decoding EXIficient's stream: event 43: expected `XSI_NIL false`, got `CH 0`
19. P3 X2: Nagasena.NET decoding EXIficient's stream: event 43: expected `XSI_NIL false`, got `CH 0`
20. P3 X2: exip decoding EXIficient's stream: event 43: expected `XSI_NIL false`, got `CH 0`
