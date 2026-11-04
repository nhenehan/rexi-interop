# EXI interop and benchmark report

Generated 2026-11-04T05:02:43Z on AMD Ryzen 7 7840U w/ Radeon 780M Graphics, Linux 7.2.6-76070206-generic, Java 25.0.4.1.

- **rexi-interop**: 78672553f301 (with local changes)
- **EXIficient**: 1.0.7 (Maven Central), in-process
- **Nagasena**: W3C exi-testsuite 23d5f587ba3e candidates/openexi/lib, in-process
- **exip**: nhenehan/exip b2870c106f0c, Release build, drivers/exip (cc (Ubuntu 13.3.0-6ubuntu2~24.04.1) 13.3.0)
- **rexi**: 91e5d3ccb233, release build, rexi bench (rustc 1.99.0 (b940084d7 2026-09-28))
- **Nagasena.NET**: OpenEXI svn r1188 trunk/nagasenacs, drivers/nagasena-cs (.NET 10.0.402), schemas compiled by Java Nagasena
- **jexi**: e336de8b408e, bin/jexi (julia version 1.12.7)

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

All profiles carry the options out of band: no options document, no cookie.
Decoded events are compared with the XML's own events (attributes in EXI order, whitespace-only text dropped); values compare as written, then as numbers, then as booleans. ✓ correct, ✗ failed (see notes), – not supported.

## P0: strict, bit-packed

### Encode by X, decode by Y

| encoder → decoder | S1 | S2 | S3 | S4 | S5 | R1 | R2 | R3 | R4 | R5 | X1 | X2 | U1 | B1 |
|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|
| EXIficient → EXIficient | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✗[1] | ✓ | ✓ |
| EXIficient → Nagasena | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✗[2] | ✓ | ✓ |
| EXIficient → Nagasena.NET | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✗[3] | ✓ | ✓ |
| EXIficient → exip | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✗[4] | ✓ | ✓ |
| EXIficient → rexi | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✗[5] | ✓ | ✓ |
| EXIficient → jexi | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✗[6] | ✓ | ✓ |
| Nagasena → EXIficient | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ |
| Nagasena → Nagasena | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ |
| Nagasena → Nagasena.NET | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ |
| Nagasena → exip | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ |
| Nagasena → rexi | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ |
| Nagasena → jexi | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ |
| Nagasena.NET → EXIficient | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ |
| Nagasena.NET → Nagasena | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ |
| Nagasena.NET → Nagasena.NET | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ |
| Nagasena.NET → exip | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ |
| Nagasena.NET → rexi | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ |
| Nagasena.NET → jexi | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ |
| exip → EXIficient | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ |
| exip → Nagasena | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ |
| exip → Nagasena.NET | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ |
| exip → exip | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ |
| exip → rexi | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ |
| exip → jexi | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ |
| rexi → EXIficient | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ |
| rexi → Nagasena | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ |
| rexi → Nagasena.NET | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ |
| rexi → exip | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ |
| rexi → rexi | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ |
| rexi → jexi | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ |
| jexi → EXIficient | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ |
| jexi → Nagasena | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ |
| jexi → Nagasena.NET | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ |
| jexi → exip | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ |
| jexi → rexi | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ |
| jexi → jexi | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ |

### Bytes compared with EXIficient's

| encoder | S1 | S2 | S3 | S4 | S5 | R1 | R2 | R3 | R4 | R5 | X1 | X2 | U1 | B1 |
|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|
| EXIficient | = | = | = | = | = | = | = | = | = | = | = | = | = | = |
| Nagasena | = | = | = | = | = | = | = | = | = | = | = | ≠ 51 B vs 50 B | = | = |
| Nagasena.NET | = | = | = | = | = | = | = | = | = | = | = | ≠ 51 B vs 50 B | = | = |
| exip | = | = | = | = | = | = | = | = | = | = | = | ≠ 51 B vs 50 B | = | = |
| rexi | = | = | = | = | = | = | = | = | = | = | = | ≠ 51 B vs 50 B | = | = |
| jexi | = | = | = | = | = | = | = | = | = | = | = | ≠ 51 B vs 50 B | = | = |

### Timing: median µs (p10–p90)

Encoders time their natural input: XML (parsing included) for EXIficient, Nagasena (Java and .NET), rexi and jexi; a pre-parsed event stream for exip and for the `events` rows of rexi and jexi. Decoders all decode EXIficient's stream. Schema compilation is excluded.

| implementation | op | S1 | S2 | S3 | S4 | S5 | R1 | R2 | R3 | R4 | R5 | X1 | X2 | U1 | B1 |
|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|
| EXIficient | encode (xml) | 12.6 (12.0–13.8) | 13.4 (13.1–13.9) | 12.0 (11.7–12.3) | 11.1 (10.9–11.5) | 11.9 (11.6–12.4) | 14.4 (14.1–14.8) | 155.3 (150.4–194.6) | 12.3 (12.0–12.8) | 13.0 (12.7–13.5) | 14.5 (14.3–15.1) | 136.6 (18.3–193.0) | 15.2 (14.9–15.8) | 27.4 (26.8–28.6) | 12.3 (11.7–21.7) |
| EXIficient | decode (exi) | 1.2 (1.1–1.2) | 1.1 (1.1–1.1) | 0.8 (0.8–0.8) | 0.8 (0.8–0.9) | 1.1 (1.1–1.1) | 1.6 (1.6–1.7) | 2.1 (2.1–2.1) | 1.2 (1.2–1.3) | 1.0 (1.0–1.1) | 1.6 (1.5–1.6) | 1.4 (1.4–1.4) | 1.4 (1.4–1.5) | 5.8 (5.7–5.9) | 0.6 (0.6–0.7) |
| Nagasena | encode (xml) | 5.9 (5.2–6.1) | 5.3 (5.1–5.5) | 4.9 (4.8–5.6) | 5.0 (4.9–5.2) | 6.2 (6.0–6.9) | 7.0 (6.9–7.2) | 8.8 (8.6–9.0) | 5.5 (5.4–5.7) | 5.8 (5.6–6.0) | 8.2 (8.1–8.4) | 7.4 (7.3–7.6) | 7.5 (7.3–7.6) | 18.1 (17.9–18.8) | 4.1 (4.0–4.3) |
| Nagasena | decode (exi) | 0.8 (0.8–0.9) | 0.8 (0.7–0.8) | 0.5 (0.5–0.6) | 0.7 (0.7–0.7) | 0.8 (0.8–0.8) | 1.4 (1.3–1.4) | 2.0 (2.0–2.1) | 1.0 (1.0–1.0) | 0.8 (0.8–0.8) | 1.3 (1.3–1.4) | 1.3 (1.3–1.3) | 1.3 (1.2–1.3) | 4.7 (4.5–4.8) | 0.6 (0.6–0.7) |
| Nagasena.NET | encode (xml) | 6.3 (6.0–6.6) | 6.0 (5.7–6.6) | 4.2 (3.9–6.3) | 4.9 (4.6–5.6) | 5.5 (5.2–5.8) | 7.1 (6.8–7.4) | 9.2 (8.8–12.9) | 5.0 (4.8–5.4) | 5.5 (5.2–5.7) | 7.1 (6.8–7.5) | 7.4 (7.1–7.7) | 7.6 (7.3–7.9) | 21.8 (20.6–34.7) | 4.3 (3.8–5.9) |
| Nagasena.NET | decode (exi) | 0.8 (0.8–1.3) | 0.7 (0.7–1.8) | 0.8 (0.5–1.1) | 0.8 (0.6–0.9) | 0.8 (0.8–1.4) | 1.4 (1.4–2.0) | 1.7 (1.7–2.5) | 1.1 (0.9–1.3) | 0.9 (0.7–1.0) | 1.2 (1.1–1.8) | 0.9 (0.9–1.8) | 1.0 (0.9–1.5) | 4.7 (4.5–4.9) | 0.6 (0.4–0.7) |
| exip | encode (events) | 5.3 (5.2–5.3) | 5.4 (5.4–5.5) | 4.6 (4.6–6.1) | 4.9 (4.2–4.9) | 5.4 (5.4–5.5) | 5.7 (5.6–5.7) | 6.4 (6.3–6.5) | 4.9 (4.8–4.9) | 4.4 (4.4–4.5) | 5.6 (5.5–5.6) | 5.0 (4.9–5.0) | 5.2 (5.1–5.3) | 13.1 (13.0–13.4) | 3.7 (3.7–3.8) |
| exip | decode (exi) | 1.6 (1.6–1.6) | 1.6 (1.6–1.7) | 1.1 (1.1–1.1) | 1.1 (1.1–1.2) | 1.5 (1.5–1.5) | 2.2 (2.1–2.2) | 2.8 (2.8–2.8) | 2.0 (1.9–2.0) | 1.2 (1.1–1.2) | 2.0 (2.0–2.1) | 1.7 (1.7–1.8) | 1.7 (1.7–1.8) | 6.7 (6.6–6.8) | 0.7 (0.7–0.7) |
| rexi | encode (xml) | 3.2 (3.1–3.3) | 3.0 (2.9–3.1) | 2.1 (2.0–2.1) | 2.8 (2.7–3.0) | 3.2 (3.1–3.3) | 4.6 (4.5–4.8) | 6.7 (6.6–6.8) | 2.9 (2.9–3.0) | 3.0 (2.9–3.1) | 4.6 (4.5–4.7) | 4.8 (4.7–4.9) | 4.7 (4.6–4.8) | 17.6 (17.4–17.8) | 1.7 (1.7–1.8) |
| rexi | encode (events) | 1.8 (1.8–1.9) | 2.0 (1.9–2.0) | 1.1 (1.1–1.2) | 1.4 (1.3–1.4) | 1.6 (1.6–1.7) | 2.9 (2.8–2.9) | 4.2 (4.2–4.3) | 1.8 (1.7–1.8) | 1.7 (1.6–1.7) | 2.9 (2.8–3.0) | 3.2 (3.2–3.3) | 2.6 (2.5–2.7) | 10.8 (10.6–11.0) | 1.0 (1.0–1.1) |
| rexi | decode (exi) | 7.7 (1.0–8.1) | 0.9 (0.9–0.9) | 0.6 (0.6–0.6) | 0.6 (0.6–0.7) | 0.8 (0.8–0.8) | 1.4 (1.4–1.5) | 1.7 (1.7–1.8) | 0.9 (0.9–0.9) | 0.8 (0.7–0.8) | 1.3 (1.3–1.4) | 1.2 (1.2–1.3) | 1.3 (1.3–1.4) | 5.0 (4.9–5.0) | 0.4 (0.4–0.5) |
| jexi | encode (xml) | 4.8 (4.6–5.1) | 5.9 (5.1–6.3) | 2.9 (2.8–3.2) | 3.6 (3.4–3.8) | 4.8 (4.7–5.2) | 6.5 (6.3–6.8) | 9.2 (8.9–9.6) | 4.0 (3.9–4.3) | 4.4 (4.3–4.7) | 6.4 (6.2–6.8) | 7.5 (7.3–7.8) | 7.4 (7.2–7.8) | 20.5 (19.8–23.9) | 3.2 (3.0–3.4) |
| jexi | encode (events) | 3.3 (3.2–3.5) | 3.2 (3.0–3.4) | 16.8 (1.9–27.6) | 2.2 (2.0–2.9) | 2.4 (2.3–2.6) | 4.9 (4.2–5.1) | 6.1 (6.0–6.4) | 4.0 (2.6–5.8) | 2.7 (2.6–2.9) | 4.2 (4.1–4.5) | 5.1 (4.9–5.3) | 4.9 (4.7–5.2) | 16.5 (15.9–20.8) | 2.1 (1.9–2.2) |
| jexi | decode (exi) | 1.0 (1.0–1.1) | 0.9 (0.8–1.1) | 0.6 (0.6–0.7) | 0.6 (0.6–0.8) | 0.8 (0.8–1.0) | 1.3 (1.2–1.5) | 1.9 (1.7–2.1) | 0.9 (0.9–1.1) | 0.7 (0.7–0.9) | 1.4 (1.3–1.6) | 1.2 (1.2–1.4) | 1.1 (1.1–1.3) | 6.6 (5.9–7.2) | 0.5 (0.5–0.6) |

## P1: non-strict, bit-packed

### Encode by X, decode by Y

| encoder → decoder | S1 | S2 | S3 | S4 | S5 | R1 | R2 | R3 | R4 | R5 | X1 | X2 | U1 | B1 |
|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|
| EXIficient → EXIficient | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✗[7] | ✓ | ✓ |
| EXIficient → Nagasena | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✗[8] | ✓ | ✓ |
| EXIficient → Nagasena.NET | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✗[9] | ✓ | ✓ |
| EXIficient → exip | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✗[10] | ✓ | ✓ |
| EXIficient → rexi | – | – | – | – | – | – | – | – | – | – | – | – | – | – |
| EXIficient → jexi | – | – | – | – | – | – | – | – | – | – | – | – | – | – |
| Nagasena → EXIficient | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ |
| Nagasena → Nagasena | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ |
| Nagasena → Nagasena.NET | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ |
| Nagasena → exip | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ |
| Nagasena → rexi | – | – | – | – | – | – | – | – | – | – | – | – | – | – |
| Nagasena → jexi | – | – | – | – | – | – | – | – | – | – | – | – | – | – |
| Nagasena.NET → EXIficient | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ |
| Nagasena.NET → Nagasena | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ |
| Nagasena.NET → Nagasena.NET | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ |
| Nagasena.NET → exip | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ |
| Nagasena.NET → rexi | – | – | – | – | – | – | – | – | – | – | – | – | – | – |
| Nagasena.NET → jexi | – | – | – | – | – | – | – | – | – | – | – | – | – | – |
| exip → EXIficient | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ |
| exip → Nagasena | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ |
| exip → Nagasena.NET | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ |
| exip → exip | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ |
| exip → rexi | – | – | – | – | – | – | – | – | – | – | – | – | – | – |
| exip → jexi | – | – | – | – | – | – | – | – | – | – | – | – | – | – |
| rexi → EXIficient | – | – | – | – | – | – | – | – | – | – | – | – | – | – |
| rexi → Nagasena | – | – | – | – | – | – | – | – | – | – | – | – | – | – |
| rexi → Nagasena.NET | – | – | – | – | – | – | – | – | – | – | – | – | – | – |
| rexi → exip | – | – | – | – | – | – | – | – | – | – | – | – | – | – |
| rexi → rexi | – | – | – | – | – | – | – | – | – | – | – | – | – | – |
| rexi → jexi | – | – | – | – | – | – | – | – | – | – | – | – | – | – |
| jexi → EXIficient | – | – | – | – | – | – | – | – | – | – | – | – | – | – |
| jexi → Nagasena | – | – | – | – | – | – | – | – | – | – | – | – | – | – |
| jexi → Nagasena.NET | – | – | – | – | – | – | – | – | – | – | – | – | – | – |
| jexi → exip | – | – | – | – | – | – | – | – | – | – | – | – | – | – |
| jexi → rexi | – | – | – | – | – | – | – | – | – | – | – | – | – | – |
| jexi → jexi | – | – | – | – | – | – | – | – | – | – | – | – | – | – |

### Timing: median µs (p10–p90)

Encoders time their natural input: XML (parsing included) for EXIficient, Nagasena (Java and .NET), rexi and jexi; a pre-parsed event stream for exip and for the `events` rows of rexi and jexi. Decoders all decode EXIficient's stream. Schema compilation is excluded.

| implementation | op | S1 | S2 | S3 | S4 | S5 | R1 | R2 | R3 | R4 | R5 | X1 | X2 | U1 | B1 |
|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|
| EXIficient | encode (xml) | 12.5 (12.2–12.9) | 12.5 (12.3–12.9) | 10.8 (10.5–11.1) | 11.8 (11.4–12.2) | 12.2 (11.9–12.7) | 14.7 (14.4–15.3) | 17.0 (16.7–17.6) | 12.0 (11.7–12.6) | 13.2 (12.9–13.7) | 14.6 (14.3–15.3) | 15.1 (14.8–15.5) | 15.4 (15.1–15.9) | 27.7 (27.2–28.7) | 10.3 (9.9–10.7) |
| EXIficient | decode (exi) | 1.2 (1.2–1.2) | 1.1 (1.1–1.1) | 0.8 (0.8–0.8) | 1.0 (0.9–1.0) | 1.2 (1.2–1.2) | 1.8 (1.7–1.8) | 2.3 (2.2–2.3) | 1.3 (1.2–1.3) | 1.1 (1.1–1.1) | 1.7 (1.6–1.7) | 1.5 (1.4–1.5) | 1.5 (1.5–1.6) | 6.0 (5.8–6.1) | 0.6 (0.6–0.6) |
| Nagasena | encode (xml) | 5.6 (5.5–5.8) | 5.5 (5.4–5.7) | 5.0 (4.4–6.6) | 5.2 (5.0–5.3) | 5.5 (5.4–5.7) | 7.4 (7.3–7.7) | 9.2 (9.0–9.6) | 5.6 (5.5–5.8) | 5.9 (5.7–6.1) | 7.3 (7.1–7.6) | 7.7 (7.5–8.0) | 7.7 (7.6–8.0) | 18.5 (18.2–19.2) | 4.0 (3.9–4.1) |
| Nagasena | decode (exi) | 0.9 (0.9–0.9) | 0.8 (0.8–0.8) | 0.6 (0.6–0.6) | 0.7 (0.7–0.7) | 0.9 (0.9–0.9) | 1.4 (1.4–1.4) | 1.8 (1.8–1.9) | 1.1 (1.0–1.1) | 0.8 (0.8–0.9) | 1.4 (1.3–1.4) | 1.3 (1.3–1.4) | 1.3 (1.3–1.4) | 4.9 (4.7–4.9) | 0.6 (0.6–0.6) |
| Nagasena.NET | encode (xml) | 5.2 (5.0–5.5) | 5.2 (4.9–5.4) | 3.6 (3.4–3.9) | 4.3 (4.0–4.6) | 4.9 (4.7–5.2) | 7.2 (6.9–7.6) | 9.6 (9.2–10.0) | 5.1 (4.8–5.5) | 5.4 (5.1–5.7) | 7.1 (6.9–7.5) | 7.4 (7.2–7.8) | 7.7 (7.4–8.1) | 17.5 (17.1–18.2) | 3.3 (3.2–3.6) |
| Nagasena.NET | decode (exi) | 0.9 (0.8–1.3) | 1.1 (0.8–1.2) | 0.8 (0.5–0.8) | 0.8 (0.6–0.9) | 0.9 (0.8–1.2) | 1.3 (1.3–1.8) | 1.7 (1.6–2.5) | 1.1 (1.0–1.5) | 1.0 (0.7–1.1) | 1.2 (1.1–1.8) | 1.0 (1.0–1.5) | 1.0 (1.0–1.7) | 4.9 (4.7–5.1) | 0.6 (0.4–0.6) |
| exip | encode (events) | 4.5 (4.5–4.6) | 4.7 (4.6–4.8) | 4.0 (4.0–4.0) | 4.2 (4.2–4.3) | 4.9 (4.9–5.0) | 5.7 (5.6–5.9) | 6.4 (6.3–6.5) | 4.9 (4.9–4.9) | 4.6 (4.5–4.7) | 5.7 (5.6–5.9) | 5.1 (5.0–5.2) | 5.2 (5.2–5.3) | 13.4 (13.3–13.6) | 3.8 (3.8–3.8) |
| exip | decode (exi) | 1.5 (1.5–1.5) | 1.5 (1.5–1.5) | 1.0 (1.0–1.0) | 1.1 (1.1–1.1) | 1.4 (1.4–1.4) | 2.3 (2.3–2.4) | 2.9 (2.8–3.0) | 1.7 (1.7–1.7) | 1.2 (1.2–1.3) | 2.1 (2.1–2.2) | 1.8 (1.8–1.9) | 1.8 (1.8–1.8) | 6.9 (6.8–7.0) | 0.7 (0.7–0.8) |

## P2: strict, byte-aligned

### Encode by X, decode by Y

| encoder → decoder | S1 | S2 | S3 | S4 | S5 | R1 | R2 | R3 | R4 | R5 | X1 | X2 | U1 | B1 |
|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|
| EXIficient → EXIficient | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✗[11] | ✓ | ✓ |
| EXIficient → Nagasena | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✗[12] | ✓ | ✓ |
| EXIficient → Nagasena.NET | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✗[13] | ✓ | ✓ |
| EXIficient → exip | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✗[14] | ✓ | ✓ |
| EXIficient → rexi | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✗[15] | ✓ | ✓ |
| EXIficient → jexi | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✗[16] | ✓ | ✓ |
| Nagasena → EXIficient | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ |
| Nagasena → Nagasena | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ |
| Nagasena → Nagasena.NET | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ |
| Nagasena → exip | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ |
| Nagasena → rexi | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ |
| Nagasena → jexi | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ |
| Nagasena.NET → EXIficient | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ |
| Nagasena.NET → Nagasena | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ |
| Nagasena.NET → Nagasena.NET | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ |
| Nagasena.NET → exip | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ |
| Nagasena.NET → rexi | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ |
| Nagasena.NET → jexi | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ |
| exip → EXIficient | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ |
| exip → Nagasena | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ |
| exip → Nagasena.NET | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ |
| exip → exip | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ |
| exip → rexi | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ |
| exip → jexi | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ |
| rexi → EXIficient | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ |
| rexi → Nagasena | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ |
| rexi → Nagasena.NET | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ |
| rexi → exip | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ |
| rexi → rexi | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ |
| rexi → jexi | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ |
| jexi → EXIficient | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ |
| jexi → Nagasena | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ |
| jexi → Nagasena.NET | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ |
| jexi → exip | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ |
| jexi → rexi | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ |
| jexi → jexi | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ |

### Bytes compared with EXIficient's

| encoder | S1 | S2 | S3 | S4 | S5 | R1 | R2 | R3 | R4 | R5 | X1 | X2 | U1 | B1 |
|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|
| EXIficient | = | = | = | = | = | = | = | = | = | = | = | = | = | = |
| Nagasena | = | = | = | = | = | = | = | = | = | = | = | ≠ 87 B vs 84 B | = | = |
| Nagasena.NET | = | = | = | = | = | = | = | = | = | = | = | ≠ 87 B vs 84 B | = | = |
| exip | = | = | = | = | = | = | = | = | = | = | = | ≠ 87 B vs 84 B | = | = |
| rexi | = | = | = | = | = | = | = | = | = | = | = | ≠ 87 B vs 84 B | = | = |
| jexi | = | = | = | = | = | = | = | = | = | = | = | ≠ 87 B vs 84 B | = | = |

### Timing: median µs (p10–p90)

Encoders time their natural input: XML (parsing included) for EXIficient, Nagasena (Java and .NET), rexi and jexi; a pre-parsed event stream for exip and for the `events` rows of rexi and jexi. Decoders all decode EXIficient's stream. Schema compilation is excluded.

| implementation | op | S1 | S2 | S3 | S4 | S5 | R1 | R2 | R3 | R4 | R5 | X1 | X2 | U1 | B1 |
|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|
| EXIficient | encode (xml) | 12.4 (12.2–12.8) | 12.6 (12.3–12.9) | 11.0 (10.7–11.4) | 13.4 (13.1–13.7) | 12.3 (12.1–12.7) | 14.8 (14.4–15.3) | 17.0 (16.7–17.5) | 12.3 (12.0–12.7) | 12.8 (12.6–13.2) | 15.0 (14.6–15.6) | 15.2 (14.9–15.7) | 15.9 (15.6–16.6) | 27.0 (26.5–28.2) | 10.4 (10.1–10.8) |
| EXIficient | decode (exi) | 1.2 (1.2–1.3) | 1.2 (1.2–1.2) | 0.8 (0.8–0.9) | 1.0 (1.0–1.0) | 1.3 (1.3–1.3) | 1.9 (1.9–2.0) | 2.4 (2.3–2.4) | 1.4 (1.3–1.4) | 1.2 (1.1–1.2) | 1.8 (1.8–1.9) | 1.5 (1.5–1.6) | 1.7 (1.7–1.8) | 6.1 (6.0–6.2) | 0.6 (0.5–0.6) |
| Nagasena | encode (xml) | 5.4 (5.3–5.6) | 5.4 (5.3–5.6) | 4.4 (4.3–4.5) | 5.0 (4.9–5.2) | 5.3 (5.2–5.5) | 7.1 (6.9–7.3) | 8.8 (8.7–9.1) | 5.5 (5.3–5.8) | 5.7 (5.5–5.9) | 7.0 (6.9–7.2) | 7.5 (7.4–7.7) | 7.7 (7.6–7.9) | 17.7 (17.4–18.4) | 4.0 (3.9–4.1) |
| Nagasena | decode (exi) | 1.0 (1.0–1.0) | 1.1 (1.1–1.1) | 0.7 (0.7–0.7) | 0.8 (0.7–0.8) | 1.0 (1.0–1.0) | 1.6 (1.5–1.6) | 2.0 (2.0–2.1) | 1.2 (1.1–1.2) | 0.9 (0.9–0.9) | 1.5 (1.5–1.6) | 1.4 (1.4–1.5) | 1.5 (1.4–1.5) | 5.8 (5.7–6.0) | 0.6 (0.6–0.6) |
| Nagasena.NET | encode (xml) | 5.1 (4.8–5.3) | 5.2 (5.0–7.1) | 3.6 (3.4–3.9) | 4.2 (3.9–4.5) | 4.7 (4.4–5.0) | 6.6 (6.4–7.4) | 9.3 (9.0–9.8) | 4.9 (4.6–5.2) | 5.2 (4.9–5.5) | 6.8 (6.6–10.2) | 7.3 (7.1–11.5) | 7.4 (7.2–7.9) | 15.7 (15.4–16.2) | 3.2 (3.1–3.5) |
| Nagasena.NET | decode (exi) | 0.9 (0.6–1.1) | 0.7 (0.6–1.6) | 0.6 (0.4–0.7) | 0.7 (0.5–0.8) | 0.9 (0.6–1.0) | 1.0 (1.0–1.7) | 1.4 (1.3–2.0) | 0.8 (0.7–1.1) | 0.9 (0.6–1.0) | 1.2 (1.0–1.6) | 1.0 (0.8–1.4) | 0.9 (0.8–1.6) | 3.5 (3.4–3.7) | 0.5 (0.3–0.6) |
| exip | encode (events) | 4.4 (4.3–4.4) | 4.6 (4.5–4.6) | 3.9 (3.9–4.0) | 4.8 (4.7–39.0) | 4.6 (4.5–4.7) | 5.4 (5.3–5.6) | 6.0 (6.0–6.2) | 4.7 (4.6–4.7) | 4.3 (4.3–4.4) | 5.4 (5.3–5.5) | 4.7 (4.7–4.8) | 5.0 (4.9–5.1) | 12.1 (11.9–15.4) | 3.6 (3.6–3.6) |
| exip | decode (exi) | 1.3 (1.3–1.4) | 1.4 (1.4–1.4) | 0.9 (0.9–0.9) | 1.0 (1.0–1.0) | 1.3 (1.3–1.3) | 2.1 (2.1–2.2) | 2.6 (2.6–2.6) | 1.6 (1.6–1.6) | 1.1 (1.1–1.2) | 2.0 (1.9–2.0) | 1.7 (1.7–1.7) | 1.6 (1.6–1.6) | 6.5 (6.4–6.6) | 0.7 (0.7–0.7) |
| rexi | encode (xml) | 3.1 (3.0–3.2) | 3.0 (2.9–3.0) | 1.7 (1.6–1.8) | 2.2 (2.2–2.3) | 2.7 (2.7–2.8) | 4.4 (4.2–4.5) | 6.2 (6.1–6.5) | 2.7 (2.7–2.8) | 2.8 (2.8–2.9) | 4.4 (4.3–4.5) | 4.7 (4.6–4.8) | 4.7 (4.5–4.8) | 17.1 (16.9–17.3) | 1.7 (1.6–1.7) |
| rexi | encode (events) | 1.7 (1.6–1.7) | 1.7 (1.6–1.7) | 0.9 (0.9–0.9) | 1.2 (1.2–1.2) | 1.5 (1.5–1.6) | 2.6 (2.5–2.6) | 3.8 (3.7–3.9) | 1.5 (1.5–1.6) | 1.6 (1.6–1.7) | 2.7 (2.6–2.8) | 2.7 (2.6–2.8) | 2.5 (2.5–2.6) | 10.0 (9.9–10.3) | 1.0 (0.9–1.0) |
| rexi | decode (exi) | 0.9 (0.8–0.9) | 0.8 (0.8–0.8) | 0.5 (0.5–0.6) | 0.6 (0.6–0.6) | 0.8 (0.7–0.8) | 1.2 (1.2–1.3) | 1.7 (1.6–1.7) | 0.9 (0.8–0.9) | 0.8 (0.8–0.9) | 1.2 (1.1–1.2) | 1.2 (1.2–1.3) | 1.3 (1.3–1.3) | 4.1 (4.0–4.2) | 0.4 (0.4–0.4) |
| jexi | encode (xml) | 5.2 (5.1–5.5) | 5.3 (5.1–5.6) | 2.8 (2.7–3.1) | 3.6 (3.4–3.8) | 4.2 (4.1–4.4) | 6.6 (6.4–6.9) | 9.3 (9.0–9.6) | 4.3 (4.1–4.5) | 4.4 (4.3–4.6) | 6.7 (6.5–7.0) | 7.5 (7.3–7.8) | 8.2 (8.0–8.6) | 20.0 (19.5–20.9) | 3.0 (2.8–3.2) |
| jexi | encode (events) | 2.9 (2.8–3.1) | 3.2 (3.0–3.4) | 1.7 (1.6–1.8) | 1.8 (1.7–2.0) | 2.7 (2.6–2.9) | 4.4 (4.2–4.6) | 6.0 (5.8–6.3) | 2.7 (2.6–3.0) | 2.7 (2.5–2.9) | 4.1 (3.9–4.3) | 5.1 (4.8–5.3) | 4.9 (4.7–5.3) | 14.6 (14.1–15.4) | 2.0 (1.9–2.2) |
| jexi | decode (exi) | 0.8 (0.8–1.0) | 0.8 (0.8–1.0) | 0.6 (0.5–0.7) | 0.6 (0.5–0.7) | 0.7 (0.7–0.9) | 1.2 (1.1–1.3) | 1.8 (1.6–2.0) | 0.8 (0.8–1.0) | 0.7 (0.7–0.8) | 1.3 (1.2–1.5) | 1.1 (1.1–1.3) | 1.2 (1.1–1.3) | 5.3 (5.0–5.8) | 0.5 (0.4–0.6) |

## P3: strict, compression

### Encode by X, decode by Y

| encoder → decoder | S1 | S2 | S3 | S4 | S5 | R1 | R2 | R3 | R4 | R5 | X1 | X2 | U1 | B1 |
|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|
| EXIficient → EXIficient | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✗[17] | ✓ | ✓ |
| EXIficient → Nagasena | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✗[18] | ✓ | ✓ |
| EXIficient → Nagasena.NET | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✗[19] | ✓ | ✓ |
| EXIficient → exip | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✗[20] | ✓ | ✓ |
| EXIficient → rexi | – | – | – | – | – | – | – | – | – | – | – | – | – | – |
| EXIficient → jexi | – | – | – | – | – | – | – | – | – | – | – | – | – | – |
| Nagasena → EXIficient | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ |
| Nagasena → Nagasena | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ |
| Nagasena → Nagasena.NET | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ |
| Nagasena → exip | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ |
| Nagasena → rexi | – | – | – | – | – | – | – | – | – | – | – | – | – | – |
| Nagasena → jexi | – | – | – | – | – | – | – | – | – | – | – | – | – | – |
| Nagasena.NET → EXIficient | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ |
| Nagasena.NET → Nagasena | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ |
| Nagasena.NET → Nagasena.NET | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ |
| Nagasena.NET → exip | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ |
| Nagasena.NET → rexi | – | – | – | – | – | – | – | – | – | – | – | – | – | – |
| Nagasena.NET → jexi | – | – | – | – | – | – | – | – | – | – | – | – | – | – |
| exip → EXIficient | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ |
| exip → Nagasena | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ |
| exip → Nagasena.NET | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ |
| exip → exip | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ |
| exip → rexi | – | – | – | – | – | – | – | – | – | – | – | – | – | – |
| exip → jexi | – | – | – | – | – | – | – | – | – | – | – | – | – | – |
| rexi → EXIficient | – | – | – | – | – | – | – | – | – | – | – | – | – | – |
| rexi → Nagasena | – | – | – | – | – | – | – | – | – | – | – | – | – | – |
| rexi → Nagasena.NET | – | – | – | – | – | – | – | – | – | – | – | – | – | – |
| rexi → exip | – | – | – | – | – | – | – | – | – | – | – | – | – | – |
| rexi → rexi | – | – | – | – | – | – | – | – | – | – | – | – | – | – |
| rexi → jexi | – | – | – | – | – | – | – | – | – | – | – | – | – | – |
| jexi → EXIficient | – | – | – | – | – | – | – | – | – | – | – | – | – | – |
| jexi → Nagasena | – | – | – | – | – | – | – | – | – | – | – | – | – | – |
| jexi → Nagasena.NET | – | – | – | – | – | – | – | – | – | – | – | – | – | – |
| jexi → exip | – | – | – | – | – | – | – | – | – | – | – | – | – | – |
| jexi → rexi | – | – | – | – | – | – | – | – | – | – | – | – | – | – |
| jexi → jexi | – | – | – | – | – | – | – | – | – | – | – | – | – | – |

### Timing: median µs (p10–p90)

Encoders time their natural input: XML (parsing included) for EXIficient, Nagasena (Java and .NET), rexi and jexi; a pre-parsed event stream for exip and for the `events` rows of rexi and jexi. Decoders all decode EXIficient's stream. Schema compilation is excluded.

| implementation | op | S1 | S2 | S3 | S4 | S5 | R1 | R2 | R3 | R4 | R5 | X1 | X2 | U1 | B1 |
|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|
| EXIficient | encode (xml) | 20.9 (20.4–24.0) | 21.2 (20.8–22.1) | 18.1 (17.5–18.9) | 20.1 (19.7–20.7) | 22.4 (22.0–23.3) | 29.6 (29.0–30.8) | 32.2 (31.6–33.4) | 22.9 (22.6–23.7) | 21.1 (20.8–21.8) | 28.0 (27.6–29.0) | 26.0 (25.5–27.0) | 25.1 (24.6–26.1) | 88.8 (87.4–93.1) | 14.3 (14.1–14.8) |
| EXIficient | decode (exi) | 6.8 (5.4–8.4) | 5.5 (5.3–7.4) | 4.5 (4.1–7.2) | 12.0 (7.3–27.3) | 9.1 (7.3–12.5) | 9.9 (9.6–11.2) | 10.6 (10.4–11.2) | 7.6 (7.4–7.9) | 12.8 (5.6–39.5) | 8.8 (8.5–9.1) | 7.9 (7.7–8.3) | 6.9 (6.8–7.2) | 41.0 (40.1–45.6) | 2.6 (2.4–2.9) |
| Nagasena | encode (xml) | 13.6 (13.0–17.8) | 13.5 (13.0–17.7) | 12.0 (11.6–16.3) | 13.1 (12.4–17.3) | 14.0 (13.4–18.6) | 16.8 (16.0–21.0) | 18.4 (17.8–22.8) | 14.4 (13.7–18.6) | 15.5 (14.8–19.8) | 16.8 (15.8–21.0) | 20.4 (17.8–32.6) | 16.0 (15.4–20.3) | 33.7 (32.7–38.2) | 11.4 (10.8–15.5) |
| Nagasena | decode (exi) | 2.8 (2.5–3.9) | 2.9 (2.4–3.9) | 2.4 (2.0–3.4) | 2.6 (2.1–3.5) | 3.1 (2.6–4.1) | 3.8 (3.4–4.8) | 4.5 (4.1–5.5) | 3.2 (2.8–4.2) | 2.8 (2.4–3.8) | 3.8 (3.4–4.9) | 5.3 (4.5–7.1) | 3.5 (3.1–4.5) | 9.5 (9.1–10.5) | 2.4 (2.0–3.3) |
| Nagasena.NET | encode (xml) | 56.7 (55.5–59.6) | 55.9 (54.8–59.0) | 63.6 (53.4–526.0) | 56.2 (54.8–58.9) | 55.8 (54.7–58.9) | 57.6 (57.1–61.4) | 60.8 (59.6–63.9) | 56.8 (55.4–59.4) | 55.6 (55.3–59.2) | 58.3 (57.4–61.6) | 58.9 (57.9–62.1) | 59.0 (58.0–62.0) | 68.6 (67.1–72.1) | 54.7 (53.7–56.9) |
| Nagasena.NET | decode (exi) | 2.0 (1.8–2.5) | 2.1 (1.8–2.6) | 1.7 (1.5–2.1) | 1.8 (1.6–2.1) | 2.2 (2.0–2.6) | 3.1 (2.8–3.7) | 3.4 (3.2–3.9) | 2.4 (2.2–2.8) | 1.9 (1.7–2.3) | 2.9 (2.7–3.5) | 6.0 (5.8–6.3) | 2.3 (2.1–2.9) | 12.5 (12.3–12.8) | 1.5 (1.3–1.8) |
| exip | encode (events) | 8.0 (7.9–8.1) | 7.8 (7.7–7.9) | 7.0 (6.9–7.1) | 7.3 (7.2–7.5) | 8.4 (8.3–8.5) | 10.1 (10.0–10.3) | 11.3 (11.1–11.5) | 8.7 (8.6–8.8) | 7.7 (7.7–7.8) | 9.9 (9.8–10.2) | 8.6 (8.5–8.7) | 8.4 (8.4–8.5) | 25.7 (25.5–26.1) | 6.0 (5.9–6.0) |
| exip | decode (exi) | 2.3 (2.2–2.3) | 2.2 (2.1–2.2) | 1.6 (1.6–1.7) | 1.6 (1.6–1.6) | 2.2 (2.1–2.2) | 3.5 (3.4–3.6) | 4.1 (4.0–4.2) | 2.5 (2.5–2.6) | 2.1 (2.0–2.1) | 3.8 (3.8–3.9) | 3.6 (3.5–3.7) | 2.7 (2.6–2.8) | 107.4 (11.6–155.4) | 1.3 (1.2–1.3) |

Timing: warm-up of at least 2000 runs and 3000 ms, then 2000 timed iterations per cell, each implementation timing itself in-process.

## Notes

1. P0 X2: EXIficient decoding EXIficient's stream: event 43: expected `XSI_NIL false`, got `CH 0`
2. P0 X2: Nagasena decoding EXIficient's stream: event 43: expected `XSI_NIL false`, got `CH 0`
3. P0 X2: Nagasena.NET decoding EXIficient's stream: event 43: expected `XSI_NIL false`, got `CH 0`
4. P0 X2: exip decoding EXIficient's stream: event 43: expected `XSI_NIL false`, got `CH 0`
5. P0 X2: rexi decoding EXIficient's stream: event 43: expected `XSI_NIL false`, got `CH 0`
6. P0 X2: jexi decoding EXIficient's stream: event 43: expected `XSI_NIL false`, got `CH 0`
7. P1 X2: EXIficient decoding EXIficient's stream: event 43: expected `XSI_NIL false`, got `CH 0`
8. P1 X2: Nagasena decoding EXIficient's stream: event 43: expected `XSI_NIL false`, got `CH 0`
9. P1 X2: Nagasena.NET decoding EXIficient's stream: event 43: expected `XSI_NIL false`, got `CH 0`
10. P1 X2: exip decoding EXIficient's stream: event 43: expected `XSI_NIL false`, got `CH 0`
11. P2 X2: EXIficient decoding EXIficient's stream: event 43: expected `XSI_NIL false`, got `CH 0`
12. P2 X2: Nagasena decoding EXIficient's stream: event 43: expected `XSI_NIL false`, got `CH 0`
13. P2 X2: Nagasena.NET decoding EXIficient's stream: event 43: expected `XSI_NIL false`, got `CH 0`
14. P2 X2: exip decoding EXIficient's stream: event 43: expected `XSI_NIL false`, got `CH 0`
15. P2 X2: rexi decoding EXIficient's stream: event 43: expected `XSI_NIL false`, got `CH 0`
16. P2 X2: jexi decoding EXIficient's stream: event 43: expected `XSI_NIL false`, got `CH 0`
17. P3 X2: EXIficient decoding EXIficient's stream: event 43: expected `XSI_NIL false`, got `CH 0`
18. P3 X2: Nagasena decoding EXIficient's stream: event 43: expected `XSI_NIL false`, got `CH 0`
19. P3 X2: Nagasena.NET decoding EXIficient's stream: event 43: expected `XSI_NIL false`, got `CH 0`
20. P3 X2: exip decoding EXIficient's stream: event 43: expected `XSI_NIL false`, got `CH 0`
