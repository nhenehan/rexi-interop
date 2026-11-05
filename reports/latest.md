# EXI interop and benchmark report

Generated 2026-11-05T04:11:13Z on AMD Ryzen 7 7840U w/ Radeon 780M Graphics, Linux 7.2.6-76070206-generic, Java 25.0.4.1.

- **rexi-interop**: 15e1c1551263 (with local changes)
- **EXIficient**: 1.0.7 (Maven Central), in-process
- **Nagasena**: W3C exi-testsuite 23d5f587ba3e candidates/openexi/lib, in-process
- **exip**: nhenehan/exip b2870c106f0c, Release build, drivers/exip (cc (Ubuntu 13.3.0-6ubuntu2~24.04.1) 13.3.0)
- **rexi**: b2ab2a8eb4bc, release build, rexi bench (rustc 1.99.0 (b940084d7 2026-09-28))
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
| EXIficient | encode (xml) | 108.6 (13.0–121.2) | 11.6 (11.4–12.0) | 10.5 (10.3–11.0) | 11.3 (11.0–11.7) | 11.9 (11.5–12.3) | 14.4 (14.1–14.9) | 16.8 (16.5–17.4) | 12.1 (11.8–12.6) | 12.7 (12.4–13.2) | 14.6 (14.3–15.1) | 14.9 (14.6–15.4) | 15.2 (14.9–15.7) | 27.4 (27.0–28.3) | 10.4 (10.2–10.8) |
| EXIficient | decode (exi) | 1.1 (1.0–1.1) | 1.0 (1.0–1.0) | 0.7 (0.7–0.7) | 0.9 (0.8–0.9) | 1.1 (1.0–1.1) | 1.6 (1.6–1.7) | 2.1 (2.0–2.2) | 1.2 (1.2–1.3) | 1.0 (1.0–1.1) | 1.6 (1.6–1.6) | 1.4 (1.4–1.4) | 1.4 (1.4–1.5) | 5.7 (5.7–5.8) | 0.6 (0.6–0.6) |
| Nagasena | encode (xml) | 5.2 (5.0–5.4) | 5.2 (5.1–5.4) | 4.3 (4.2–4.6) | 4.9 (4.8–5.0) | 5.3 (5.2–5.6) | 7.1 (6.9–7.4) | 8.6 (8.4–8.8) | 5.4 (5.3–5.6) | 5.6 (5.5–5.9) | 7.0 (6.9–7.2) | 7.3 (7.2–7.4) | 7.4 (7.3–7.6) | 18.1 (17.9–18.8) | 4.0 (3.9–4.3) |
| Nagasena | decode (exi) | 0.8 (0.7–1.3) | 0.8 (0.7–0.8) | 0.5 (0.5–0.5) | 0.6 (0.6–0.6) | 0.8 (0.8–0.8) | 1.4 (1.3–1.4) | 1.9 (1.8–1.9) | 9.1 (1.0–16.2) | 0.8 (0.8–0.8) | 1.3 (1.3–1.4) | 1.3 (1.2–1.3) | 1.3 (1.2–1.3) | 4.6 (4.5–4.7) | 0.5 (0.5–0.5) |
| Nagasena.NET | encode (xml) | 5.1 (4.9–5.4) | 5.2 (4.9–5.5) | 3.5 (3.3–3.9) | 4.4 (4.1–4.7) | 4.9 (4.7–5.2) | 7.0 (6.7–7.4) | 9.3 (9.0–9.9) | 4.9 (4.7–5.2) | 5.4 (5.1–5.6) | 7.2 (6.9–9.0) | 7.5 (7.2–7.8) | 7.5 (7.2–7.9) | 17.1 (16.8–17.8) | 3.2 (3.1–3.5) |
| Nagasena.NET | decode (exi) | 0.8 (0.7–1.2) | 0.8 (0.7–1.2) | 0.7 (0.5–0.8) | 0.8 (0.6–0.9) | 0.9 (0.7–1.2) | 1.4 (1.3–2.0) | 1.5 (1.5–2.3) | 1.0 (0.9–1.3) | 1.0 (0.7–1.2) | 1.3 (1.1–1.7) | 1.0 (0.9–1.6) | 1.4 (1.0–1.6) | 4.7 (4.5–4.8) | 0.5 (0.5–0.6) |
| exip | encode (events) | 4.5 (4.5–4.6) | 4.6 (4.6–4.7) | 4.0 (3.9–4.0) | 4.2 (4.2–4.2) | 4.6 (4.6–4.7) | 5.7 (5.6–5.8) | 6.5 (6.4–6.6) | 4.9 (4.8–5.0) | 4.5 (4.4–4.5) | 5.5 (5.5–5.6) | 4.9 (4.9–5.0) | 5.1 (5.0–5.1) | 13.2 (13.0–13.6) | 3.7 (3.7–3.8) |
| exip | decode (exi) | 1.4 (1.4–1.4) | 1.4 (1.4–1.4) | 0.9 (0.9–1.0) | 1.0 (1.0–1.1) | 1.3 (1.3–1.3) | 2.2 (2.2–2.3) | 2.7 (2.7–2.7) | 1.7 (1.6–1.7) | 1.2 (1.1–1.2) | 2.0 (1.9–2.0) | 1.7 (1.7–1.8) | 1.9 (1.9–1.9) | 6.7 (6.6–6.7) | 0.7 (0.7–0.8) |
| rexi | encode (xml) | 3.2 (3.1–3.3) | 3.1 (3.1–3.2) | 1.9 (1.8–1.9) | 2.4 (2.4–2.5) | 2.7 (2.7–2.8) | 4.7 (4.6–4.8) | 6.6 (6.4–6.7) | 3.4 (3.2–4.5) | 3.4 (3.3–3.5) | 4.5 (4.4–4.6) | 4.7 (4.6–4.8) | 4.6 (4.5–4.7) | 14.9 (14.6–15.2) | 1.7 (1.6–1.7) |
| rexi | encode (events) | 1.8 (1.7–1.8) | 1.7 (1.7–1.8) | 1.0 (1.0–1.0) | 1.3 (1.3–1.4) | 1.6 (1.5–1.6) | 2.8 (2.8–2.9) | 3.9 (3.9–4.0) | 1.7 (1.7–1.8) | 1.6 (1.6–1.7) | 2.8 (2.7–2.9) | 2.7 (2.6–2.8) | 2.5 (2.5–2.6) | 11.5 (11.3–11.7) | 1.0 (1.0–1.0) |
| rexi | decode (exi) | 0.8 (0.8–0.9) | 0.8 (0.8–0.8) | 0.5 (0.5–0.5) | 0.6 (0.6–0.6) | 0.7 (0.7–0.7) | 1.2 (1.2–1.2) | 1.6 (1.5–1.6) | 0.9 (0.8–0.9) | 0.7 (0.7–0.7) | 1.1 (1.1–1.1) | 1.2 (1.2–1.3) | 1.1 (1.1–1.2) | 4.4 (4.3–4.5) | 0.4 (0.4–0.4) |
| jexi | encode (xml) | 4.8 (4.7–5.1) | 5.2 (5.1–5.5) | 2.9 (2.7–3.1) | 3.3 (3.1–3.5) | 4.2 (4.1–4.5) | 6.5 (6.3–6.8) | 9.1 (8.9–9.5) | 4.0 (3.9–4.3) | 4.4 (4.3–4.8) | 6.5 (6.2–6.8) | 7.2 (6.9–7.6) | 7.7 (7.5–8.2) | 21.1 (20.5–22.0) | 3.2 (3.0–3.4) |
| jexi | encode (events) | 2.9 (2.8–3.1) | 3.1 (3.0–3.3) | 1.6 (1.6–1.8) | 1.8 (1.7–2.0) | 2.3 (2.2–2.5) | 4.3 (4.2–4.5) | 5.9 (5.7–6.2) | 2.7 (2.6–2.9) | 2.7 (2.6–2.9) | 4.0 (3.8–4.3) | 5.0 (4.8–5.3) | 4.8 (4.6–5.0) | 14.3 (13.8–15.0) | 2.1 (1.9–2.6) |
| jexi | decode (exi) | 0.9 (0.9–1.1) | 0.8 (0.8–1.0) | 0.6 (0.6–0.8) | 0.6 (0.6–0.8) | 0.8 (0.8–1.0) | 1.3 (1.2–1.4) | 1.8 (1.7–2.0) | 1.0 (0.9–1.1) | 0.8 (0.7–0.9) | 1.4 (1.2–1.5) | 1.2 (1.2–1.4) | 1.1 (1.1–1.2) | 5.9 (5.5–6.3) | 0.5 (0.4–0.6) |

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
| EXIficient | encode (xml) | 12.2 (11.9–12.7) | 12.4 (12.1–12.8) | 10.8 (10.5–11.2) | 11.7 (11.4–12.1) | 12.2 (12.0–12.6) | 14.9 (14.5–15.4) | 17.2 (16.9–17.8) | 12.4 (12.1–12.8) | 12.9 (12.6–13.4) | 14.9 (14.6–15.3) | 15.0 (14.7–15.5) | 15.1 (14.9–15.6) | 27.5 (27.1–28.5) | 10.4 (10.1–10.8) |
| EXIficient | decode (exi) | 1.2 (1.1–1.2) | 1.1 (1.1–1.1) | 0.8 (0.7–0.8) | 0.9 (0.9–1.0) | 1.2 (1.2–1.2) | 1.7 (1.7–1.8) | 2.2 (2.1–2.3) | 1.2 (1.2–1.3) | 1.1 (1.1–1.1) | 1.6 (1.6–1.7) | 1.4 (1.4–1.5) | 1.5 (1.5–1.6) | 5.9 (5.8–6.0) | 0.6 (0.6–0.6) |
| Nagasena | encode (xml) | 5.4 (5.3–5.6) | 5.4 (5.3–5.6) | 4.4 (4.3–4.5) | 5.0 (4.9–5.2) | 5.4 (5.3–5.7) | 7.2 (7.0–7.4) | 9.0 (8.9–9.3) | 5.6 (5.5–5.8) | 5.9 (5.7–6.1) | 7.2 (7.1–7.5) | 7.7 (7.5–7.9) | 7.6 (7.4–7.8) | 18.6 (18.4–19.4) | 3.9 (3.8–4.1) |
| Nagasena | decode (exi) | 0.9 (0.8–0.9) | 0.8 (0.8–0.8) | 0.6 (0.6–0.6) | 0.7 (0.7–0.7) | 0.9 (0.9–0.9) | 1.4 (1.4–1.4) | 1.9 (1.8–1.9) | 1.0 (1.0–1.1) | 0.8 (0.8–0.8) | 1.4 (1.3–1.4) | 1.4 (1.4–1.5) | 1.3 (1.3–1.3) | 4.8 (4.7–5.0) | 0.6 (0.5–0.6) |
| Nagasena.NET | encode (xml) | 5.1 (4.9–5.4) | 5.0 (4.8–5.3) | 3.6 (3.4–3.9) | 4.3 (4.1–4.6) | 5.0 (4.8–5.3) | 7.2 (6.9–7.6) | 9.9 (9.5–10.4) | 4.9 (4.7–5.2) | 5.3 (5.1–5.6) | 7.1 (6.8–7.5) | 7.6 (7.3–8.0) | 7.7 (7.3–8.1) | 17.5 (17.0–18.3) | 3.3 (3.1–3.6) |
| Nagasena.NET | decode (exi) | 0.9 (0.8–1.3) | 1.0 (0.7–1.2) | 0.8 (0.5–0.8) | 0.8 (0.6–0.9) | 1.0 (0.8–1.2) | 1.4 (1.3–1.8) | 1.7 (1.6–2.4) | 1.1 (0.9–1.4) | 1.0 (0.7–1.1) | 1.3 (1.2–1.9) | 1.0 (1.0–1.6) | 1.0 (1.0–1.6) | 4.9 (4.7–5.0) | 0.6 (0.5–0.6) |
| exip | encode (events) | 4.6 (4.5–4.6) | 4.7 (4.6–4.7) | 4.0 (4.0–4.0) | 4.2 (4.2–4.3) | 4.7 (4.7–4.8) | 5.6 (5.6–5.7) | 6.8 (6.7–7.0) | 4.9 (4.9–5.0) | 4.5 (4.5–4.5) | 5.6 (5.6–5.7) | 46.9 (5.1–76.2) | 5.2 (5.2–5.3) | 13.5 (13.2–14.0) | 3.7 (3.7–3.8) |
| exip | decode (exi) | 1.5 (1.5–1.5) | 1.5 (1.5–1.5) | 1.0 (1.0–1.0) | 1.1 (1.0–1.1) | 1.4 (1.4–1.4) | 2.3 (2.2–2.3) | 2.9 (2.8–2.9) | 1.7 (1.7–1.8) | 1.2 (1.2–1.3) | 2.1 (2.1–2.1) | 1.8 (1.8–1.8) | 1.8 (1.7–1.8) | 7.0 (6.8–7.2) | 0.7 (0.7–0.7) |

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
| EXIficient | encode (xml) | 12.5 (12.1–12.9) | 12.8 (12.5–13.2) | 11.1 (10.9–11.4) | 11.9 (11.6–12.3) | 12.3 (12.0–12.7) | 14.7 (14.4–15.1) | 17.0 (16.7–17.6) | 12.5 (12.3–12.9) | 13.0 (12.7–13.5) | 14.7 (14.4–15.2) | 15.3 (15.0–15.7) | 15.6 (15.3–16.2) | 26.9 (26.6–27.8) | 10.6 (10.3–10.9) |
| EXIficient | decode (exi) | 1.2 (1.2–1.3) | 1.2 (1.2–1.3) | 0.8 (0.8–0.9) | 1.1 (1.0–1.1) | 1.4 (1.3–1.4) | 1.9 (1.9–1.9) | 2.4 (2.3–2.4) | 1.4 (1.4–1.4) | 1.1 (1.1–1.2) | 1.8 (1.7–1.8) | 1.6 (1.5–1.6) | 1.6 (1.6–1.7) | 6.2 (6.1–6.4) | 0.6 (0.6–0.6) |
| Nagasena | encode (xml) | 5.5 (5.4–5.7) | 5.3 (5.2–5.5) | 4.3 (4.1–4.4) | 4.8 (4.7–5.0) | 5.2 (5.1–5.4) | 6.9 (6.8–7.2) | 8.6 (8.5–8.8) | 5.3 (5.2–5.5) | 5.7 (5.6–5.9) | 7.0 (6.8–7.2) | 7.6 (7.4–7.8) | 7.6 (7.5–7.8) | 17.6 (17.4–18.4) | 3.9 (3.8–4.0) |
| Nagasena | decode (exi) | 1.1 (1.1–1.1) | 1.0 (1.0–1.1) | 0.7 (0.7–0.8) | 0.9 (0.8–0.9) | 1.1 (1.1–1.1) | 1.8 (1.7–1.8) | 2.2 (2.1–2.3) | 1.3 (1.3–1.3) | 1.0 (1.0–1.0) | 1.7 (1.7–1.7) | 1.5 (1.5–1.6) | 1.5 (1.5–1.6) | 6.4 (6.3–6.5) | 0.6 (0.6–0.6) |
| Nagasena.NET | encode (xml) | 5.0 (4.8–5.3) | 5.0 (4.8–5.3) | 3.5 (3.3–3.8) | 4.2 (4.0–4.6) | 4.7 (4.5–5.0) | 6.8 (6.5–7.3) | 9.3 (9.0–9.8) | 4.8 (4.6–5.1) | 5.2 (4.9–5.5) | 7.0 (6.7–7.4) | 7.2 (7.0–7.6) | 7.5 (7.2–7.8) | 15.7 (15.4–16.2) | 3.3 (3.1–3.5) |
| Nagasena.NET | decode (exi) | 1.1 (1.1–1.6) | 0.9 (0.6–1.0) | 0.6 (0.4–0.7) | 0.7 (0.5–0.8) | 0.9 (0.6–1.0) | 1.7 (1.0–2.6) | 1.3 (1.2–2.0) | 1.0 (0.7–1.2) | 0.9 (0.6–1.0) | 1.2 (0.9–1.5) | 1.2 (0.8–1.4) | 0.9 (0.8–1.4) | 3.5 (3.3–3.6) | 0.5 (0.3–0.6) |
| exip | encode (events) | 4.4 (4.3–4.4) | 4.5 (4.5–4.6) | 3.9 (3.8–3.9) | 4.1 (4.1–4.1) | 4.5 (4.5–4.6) | 5.4 (5.3–5.5) | 6.0 (5.9–6.1) | 4.7 (4.6–4.7) | 4.3 (4.3–4.4) | 5.5 (5.4–5.5) | 4.8 (4.7–4.9) | 5.0 (4.9–5.0) | 12.2 (11.9–12.4) | 3.6 (3.6–3.6) |
| exip | decode (exi) | 1.3 (1.3–1.3) | 1.4 (1.3–1.4) | 0.9 (0.9–1.0) | 1.0 (1.0–1.0) | 1.3 (1.3–1.3) | 2.2 (2.1–2.2) | 2.6 (2.6–2.6) | 1.6 (1.6–1.7) | 1.2 (1.1–1.2) | 1.9 (1.9–2.0) | 1.7 (1.7–1.7) | 1.6 (1.6–1.6) | 6.4 (6.3–9.9) | 0.7 (0.7–0.7) |
| rexi | encode (xml) | 3.1 (3.0–3.1) | 3.0 (2.9–3.1) | 1.7 (1.6–1.7) | 2.4 (2.4–2.5) | 2.8 (2.7–2.8) | 4.2 (4.1–4.3) | 6.1 (6.0–6.3) | 2.7 (2.7–2.8) | 2.7 (2.7–2.8) | 4.2 (4.1–4.3) | 4.6 (4.5–4.7) | 4.7 (4.6–4.9) | 14.5 (14.2–14.8) | 1.7 (1.6–1.7) |
| rexi | encode (events) | 1.6 (1.6–1.7) | 1.6 (1.6–1.7) | 0.9 (0.9–0.9) | 1.2 (1.2–1.3) | 1.5 (1.4–1.5) | 2.5 (2.4–2.6) | 3.7 (3.6–3.7) | 1.5 (1.4–1.5) | 1.5 (1.5–1.5) | 2.6 (2.5–2.6) | 2.6 (2.5–2.7) | 2.4 (2.4–2.5) | 9.6 (9.5–9.8) | 1.0 (1.0–1.0) |
| rexi | decode (exi) | 0.8 (0.8–0.8) | 0.8 (0.8–0.8) | 0.5 (0.5–0.5) | 0.6 (0.6–0.6) | 0.7 (0.7–0.7) | 1.2 (1.1–1.2) | 1.5 (1.5–1.6) | 0.8 (0.8–0.8) | 0.7 (0.6–0.7) | 1.0 (1.0–1.1) | 1.1 (1.0–1.1) | 1.1 (1.1–1.2) | 4.0 (3.9–4.2) | 0.4 (0.4–0.4) |
| jexi | encode (xml) | 5.1 (5.0–5.4) | 5.2 (5.0–5.4) | 3.2 (3.0–3.4) | 3.4 (3.2–3.6) | 4.2 (4.1–4.5) | 7.2 (7.0–7.5) | 9.4 (9.1–9.8) | 3.9 (3.8–4.3) | 4.2 (4.0–4.4) | 6.4 (6.2–6.7) | 7.6 (7.3–8.0) | 7.8 (7.6–8.0) | 19.7 (19.2–20.4) | 3.1 (3.0–3.4) |
| jexi | encode (events) | 3.1 (3.0–3.3) | 3.1 (3.0–3.3) | 1.7 (1.6–1.8) | 1.8 (1.7–2.0) | 2.3 (2.2–2.5) | 4.3 (4.2–4.6) | 5.9 (5.8–6.2) | 2.6 (2.6–2.8) | 2.7 (2.6–2.9) | 4.1 (3.9–4.3) | 5.1 (5.0–5.4) | 4.9 (4.7–5.2) | 14.5 (14.0–15.1) | 2.0 (2.0–2.2) |
| jexi | decode (exi) | 0.9 (0.8–1.0) | 0.8 (0.8–1.0) | 0.5 (0.5–0.7) | 0.5 (0.5–0.7) | 0.7 (0.7–0.9) | 1.2 (1.1–1.3) | 1.7 (1.6–1.9) | 0.9 (0.8–1.0) | 0.7 (0.7–0.8) | 1.3 (1.2–1.5) | 1.1 (1.1–1.3) | 1.1 (1.1–1.3) | 5.4 (5.1–5.8) | 0.5 (0.4–0.5) |

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
| EXIficient | encode (xml) | 20.5 (20.1–21.1) | 20.9 (20.5–21.5) | 17.5 (17.1–18.1) | 19.8 (19.4–20.6) | 22.9 (22.2–23.7) | 28.9 (28.6–29.7) | 31.8 (31.4–32.9) | 23.3 (22.8–24.3) | 21.3 (21.0–22.1) | 27.5 (27.0–28.4) | 25.3 (24.8–26.1) | 25.2 (24.8–26.0) | 87.9 (86.6–92.3) | 14.4 (14.2–14.8) |
| EXIficient | decode (exi) | 10.4 (10.2–11.7) | 5.7 (5.5–7.7) | 6.8 (5.1–10.0) | 5.8 (5.5–7.8) | 6.9 (6.7–8.1) | 10.0 (9.8–10.7) | 10.9 (10.4–22.0) | 7.7 (7.5–8.0) | 5.9 (5.7–6.1) | 9.1 (8.8–18.2) | 7.9 (7.8–8.2) | 7.1 (6.8–14.5) | 42.3 (41.6–44.1) | 7.7 (7.4–9.1) |
| Nagasena | encode (xml) | 13.7 (13.1–17.6) | 13.2 (12.8–17.4) | 12.0 (11.5–16.1) | 12.9 (12.5–17.1) | 14.0 (13.5–18.1) | 16.7 (16.1–20.9) | 18.4 (17.9–22.7) | 14.0 (13.5–18.2) | 14.1 (13.6–18.1) | 16.7 (15.8–20.7) | 16.4 (15.6–20.6) | 15.7 (15.3–19.8) | 33.7 (32.8–37.9) | 11.1 (10.6–15.2) |
| Nagasena | decode (exi) | 3.0 (2.6–3.9) | 2.9 (2.5–3.8) | 2.4 (2.0–3.4) | 2.6 (2.2–3.6) | 3.0 (2.6–4.0) | 3.8 (3.4–4.8) | 4.4 (4.1–5.5) | 3.2 (2.9–4.3) | 2.9 (2.5–3.8) | 3.7 (3.4–4.8) | 4.3 (3.9–5.3) | 3.5 (3.1–4.6) | 9.8 (9.5–10.9) | 2.5 (2.0–3.4) |
| Nagasena.NET | encode (xml) | 55.4 (55.0–56.7) | 56.0 (55.1–58.4) | 53.8 (53.4–55.1) | 54.5 (54.0–55.9) | 55.5 (54.9–58.5) | 58.4 (57.4–61.9) | 60.0 (59.4–63.0) | 56.2 (55.2–59.1) | 55.7 (55.3–57.1) | 57.4 (57.0–59.2) | 58.9 (57.8–62.0) | 59.4 (57.9–63.9) | 69.9 (67.4–72.9) | 53.6 (53.3–55.0) |
| Nagasena.NET | decode (exi) | 2.1 (1.8–2.5) | 1.9 (1.7–2.3) | 1.6 (1.4–1.9) | 1.7 (1.6–2.1) | 2.1 (1.9–2.5) | 2.9 (2.7–3.5) | 3.5 (3.2–3.9) | 2.4 (2.1–2.7) | 1.9 (1.7–2.4) | 2.9 (2.6–3.3) | 5.9 (5.7–6.3) | 2.3 (2.1–3.0) | 12.1 (11.9–12.5) | 1.4 (1.2–1.7) |
| exip | encode (events) | 7.7 (7.7–7.8) | 8.1 (8.1–8.3) | 6.9 (6.8–6.9) | 7.2 (7.2–7.2) | 8.2 (8.2–8.3) | 10.1 (9.9–10.3) | 11.1 (10.9–11.3) | 8.7 (8.5–8.8) | 7.7 (7.7–7.8) | 9.8 (9.7–10.0) | 8.6 (8.5–8.8) | 8.5 (8.4–8.6) | 25.7 (25.3–26.6) | 6.0 (5.9–6.0) |
| exip | decode (exi) | 2.3 (2.3–2.3) | 2.2 (2.2–2.2) | 1.6 (1.5–1.6) | 14.7 (1.6–27.0) | 2.2 (2.1–2.3) | 3.4 (3.3–3.5) | 4.1 (4.0–4.2) | 2.4 (2.4–2.9) | 1.9 (1.8–2.0) | 3.2 (3.1–3.4) | 3.6 (3.6–3.7) | 2.7 (2.7–2.8) | 11.6 (11.5–11.9) | 1.2 (1.2–1.3) |

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
