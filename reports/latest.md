# EXI interop and benchmark report

Generated 2026-10-23T01:19:43Z on AMD Ryzen 7 7840U w/ Radeon 780M Graphics, Linux 7.2.6-76070206-generic, Java 25.0.4.1.

- **rexi-interop**: 766c0536231a
- **EXIficient**: 1.0.7 (Maven Central), in-process
- **Nagasena**: W3C exi-testsuite 23d5f587ba3e candidates/openexi/lib, in-process
- **exip**: nhenehan/exip b2870c106f0c, Release build, drivers/exip (cc (Ubuntu 13.3.0-6ubuntu2~24.04.1) 13.3.0)
- **rexi**: ba2c7313e03e, release build, rexi bench (rustc 1.99.0 (b940084d7 2026-09-28))
- **Nagasena.NET**: OpenEXI svn r1188 trunk/nagasenacs, drivers/nagasena-cs (.NET 10.0.402), schemas compiled by Java Nagasena
- **jexi**: a2473c18ff34, bin/jexi (julia version 1.12.7)

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
| EXIficient | encode (xml) | 111.0 (14.5–131.2) | 11.9 (11.6–12.3) | 10.9 (10.6–11.3) | 11.9 (11.6–12.3) | 11.8 (11.6–12.2) | 14.9 (14.6–15.3) | 17.6 (17.2–18.2) | 12.5 (12.2–12.9) | 13.4 (13.1–13.8) | 15.5 (15.1–16.0) | 15.6 (15.3–17.7) | 15.8 (15.4–16.3) | 28.8 (28.1–30.0) | 12.5 (12.1–13.2) |
| EXIficient | decode (exi) | 1.1 (1.0–1.1) | 1.0 (0.9–1.0) | 0.7 (0.7–0.7) | 0.9 (0.8–0.9) | 1.1 (1.1–1.1) | 1.7 (1.7–1.8) | 2.2 (2.1–2.2) | 1.3 (1.2–1.3) | 1.0 (1.0–1.1) | 1.7 (1.6–1.7) | 1.4 (1.3–1.4) | 1.4 (1.4–1.5) | 5.8 (5.7–6.0) | 0.5 (0.5–0.6) |
| Nagasena | encode (xml) | 5.4 (5.3–5.6) | 5.4 (5.3–5.6) | 4.4 (4.2–4.5) | 5.1 (4.9–5.2) | 5.3 (5.1–5.6) | 7.2 (7.0–7.4) | 8.9 (8.7–9.2) | 5.6 (5.5–6.1) | 5.9 (5.7–6.1) | 7.3 (7.1–7.6) | 7.5 (7.4–7.8) | 7.6 (7.4–7.9) | 18.5 (18.0–19.3) | 3.9 (3.8–4.1) |
| Nagasena | decode (exi) | 0.8 (0.7–0.8) | 0.8 (0.7–0.8) | 0.5 (0.5–0.6) | 0.6 (0.6–0.6) | 0.8 (0.8–0.8) | 1.4 (1.4–1.4) | 1.8 (1.7–4.4) | 1.0 (1.0–1.0) | 0.8 (0.8–0.8) | 1.3 (1.3–1.4) | 1.3 (1.3–1.4) | 1.3 (1.3–1.3) | 4.7 (4.6–4.8) | 0.6 (0.5–0.6) |
| Nagasena.NET | encode (xml) | 5.7 (5.3–7.5) | 5.0 (4.8–5.3) | 3.6 (3.4–3.9) | 4.4 (4.1–4.7) | 4.9 (4.7–5.2) | 7.2 (6.8–7.6) | 9.6 (9.3–10.1) | 5.1 (4.8–5.5) | 5.8 (5.6–6.2) | 7.3 (6.9–7.7) | 7.7 (7.4–8.1) | 9.1 (8.7–10.2) | 17.5 (16.9–20.6) | 3.3 (3.2–3.6) |
| Nagasena.NET | decode (exi) | 1.0 (0.7–1.2) | 1.1 (0.7–1.2) | 0.8 (0.7–0.9) | 0.8 (0.6–0.9) | 1.1 (0.7–1.2) | 1.5 (1.2–1.9) | 1.6 (1.5–3.1) | 1.2 (0.9–1.4) | 1.0 (0.7–1.2) | 1.5 (1.2–1.9) | 1.0 (0.9–2.1) | 1.3 (1.0–1.6) | 5.6 (5.1–5.7) | 0.8 (0.4–1.0) |
| exip | encode (events) | 4.6 (4.5–4.6) | 4.7 (4.7–4.8) | 4.0 (4.0–4.0) | 4.2 (4.2–4.3) | 4.7 (4.7–4.8) | 5.7 (5.6–5.8) | 6.4 (6.3–6.5) | 4.9 (4.8–5.0) | 4.5 (4.5–4.6) | 5.9 (5.8–6.0) | 5.1 (5.0–5.2) | 5.2 (5.2–5.3) | 15.1 (15.0–15.3) | 4.3 (4.2–4.3) |
| exip | decode (exi) | 1.5 (1.5–1.6) | 1.4 (1.4–1.4) | 0.9 (0.9–0.9) | 1.0 (1.0–1.0) | 1.2 (1.2–1.3) | 2.3 (2.3–2.4) | 2.8 (2.8–2.9) | 1.6 (1.6–1.6) | 1.2 (1.2–1.2) | 2.1 (2.0–2.1) | 1.9 (1.8–1.9) | 1.7 (1.7–1.8) | 8.2 (8.2–8.3) | 0.8 (0.8–0.8) |
| rexi | encode (xml) | 5.7 (5.6–5.9) | 5.5 (5.4–5.7) | 3.2 (3.1–3.2) | 3.8 (3.7–3.9) | 4.9 (4.8–5.1) | 8.5 (8.3–8.7) | 11.8 (11.4–12.3) | 4.9 (4.8–5.1) | 5.0 (4.9–5.1) | 8.2 (8.1–8.4) | 8.2 (8.0–8.4) | 8.6 (8.4–8.8) | 26.4 (26.0–26.7) | 3.0 (2.9–3.1) |
| rexi | encode (events) | 2.0 (2.0–2.1) | 2.0 (1.9–2.1) | 1.1 (1.1–1.2) | 1.5 (1.5–1.5) | 1.8 (1.8–1.9) | 3.2 (3.1–3.3) | 4.4 (4.4–4.5) | 2.0 (1.9–2.0) | 1.8 (1.7–1.8) | 3.2 (3.1–3.2) | 3.2 (3.1–3.2) | 3.2 (3.1–3.2) | 11.2 (11.0–11.4) | 1.1 (1.1–1.2) |
| rexi | decode (exi) | 0.8 (0.8–0.9) | 0.8 (0.8–0.8) | 0.5 (0.5–0.6) | 0.6 (0.6–0.6) | 0.7 (0.7–0.8) | 1.2 (1.2–1.3) | 1.6 (1.5–1.6) | 0.9 (0.8–0.9) | 0.7 (0.7–0.7) | 1.1 (1.1–1.2) | 1.1 (1.1–1.2) | 1.2 (1.2–1.3) | 4.4 (4.3–4.5) | 0.4 (0.4–0.5) |
| jexi | encode (xml) | 4.9 (4.7–5.2) | 5.4 (5.2–5.7) | 3.2 (3.0–3.4) | 3.3 (3.2–3.6) | 4.0 (3.9–4.3) | 7.1 (6.9–7.3) | 9.2 (8.9–9.6) | 4.4 (4.3–4.7) | 4.5 (4.4–4.8) | 6.9 (6.7–7.2) | 7.2 (6.9–7.6) | 7.3 (7.1–7.6) | 21.1 (20.6–22.0) | 3.1 (2.9–3.3) |
| jexi | encode (events) | 3.0 (2.9–3.2) | 3.2 (3.1–3.4) | 1.7 (1.6–1.9) | 1.8 (1.7–2.0) | 2.3 (2.2–2.5) | 4.1 (4.0–4.4) | 5.9 (5.7–6.3) | 2.6 (2.5–2.8) | 2.8 (2.6–3.0) | 4.0 (3.8–4.3) | 4.9 (4.8–5.2) | 4.5 (4.3–4.7) | 14.2 (13.8–14.9) | 2.2 (2.1–2.5) |
| jexi | decode (exi) | 0.9 (0.9–1.1) | 0.9 (0.8–1.0) | 0.6 (0.6–0.7) | 0.6 (0.6–0.8) | 0.8 (0.8–0.9) | 1.3 (1.3–1.5) | 1.9 (1.8–2.1) | 1.0 (0.9–1.1) | 0.8 (0.7–0.9) | 1.5 (1.3–1.7) | 1.2 (1.2–1.4) | 1.2 (1.1–1.3) | 6.8 (6.5–7.3) | 0.6 (0.5–1.0) |

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
| EXIficient | encode (xml) | 14.7 (14.3–15.2) | 13.7 (13.3–14.4) | 10.9 (10.7–11.4) | 14.0 (13.6–14.6) | 12.7 (12.4–13.1) | 15.6 (15.2–16.1) | 18.5 (17.9–19.4) | 12.9 (12.6–13.4) | 13.7 (13.4–14.0) | 15.5 (15.1–16.0) | 17.7 (17.5–25.8) | 16.3 (15.9–16.8) | 28.7 (28.0–29.9) | 11.1 (10.9–11.5) |
| EXIficient | decode (exi) | 1.3 (1.3–1.3) | 1.1 (1.1–1.1) | 0.8 (0.8–0.8) | 0.9 (0.9–1.0) | 1.2 (1.1–10.7) | 1.7 (1.7–1.8) | 2.3 (2.2–2.3) | 1.3 (1.3–1.3) | 1.1 (1.1–1.1) | 1.7 (1.6–1.8) | 1.5 (1.4–1.5) | 1.6 (1.5–1.6) | 6.0 (5.9–6.1) | 0.6 (0.6–0.6) |
| Nagasena | encode (xml) | 6.2 (6.1–6.4) | 5.5 (5.3–5.7) | 5.0 (4.9–5.6) | 5.2 (5.0–5.4) | 5.6 (5.4–5.8) | 7.3 (7.1–7.6) | 9.1 (8.8–9.4) | 5.6 (5.5–5.8) | 5.9 (5.7–6.2) | 7.5 (7.3–7.7) | 8.8 (8.7–9.2) | 8.0 (7.8–8.2) | 18.5 (18.1–19.3) | 4.0 (3.9–4.1) |
| Nagasena | decode (exi) | 1.0 (1.0–1.0) | 0.8 (0.8–0.8) | 0.7 (0.7–0.7) | 0.7 (0.7–0.7) | 0.9 (0.9–0.9) | 1.5 (1.4–1.5) | 2.1 (2.1–16.9) | 1.0 (1.0–1.1) | 0.9 (0.8–0.9) | 1.4 (1.4–1.4) | 1.4 (1.4–1.5) | 1.3 (1.3–1.4) | 4.9 (4.8–4.9) | 0.6 (0.6–0.6) |
| Nagasena.NET | encode (xml) | 5.4 (5.2–5.8) | 6.0 (5.8–7.1) | 3.7 (3.6–4.1) | 4.3 (4.1–4.6) | 5.1 (4.8–5.3) | 7.1 (6.8–7.4) | 11.3 (10.8–12.6) | 5.1 (4.8–5.4) | 5.2 (5.0–5.5) | 7.5 (7.2–7.8) | 7.6 (7.3–7.9) | 7.8 (7.5–8.2) | 17.8 (17.3–18.5) | 3.3 (3.2–3.6) |
| Nagasena.NET | decode (exi) | 1.2 (0.8–1.4) | 1.2 (0.8–1.4) | 0.8 (0.7–1.0) | 0.8 (0.6–0.9) | 1.1 (0.8–1.3) | 1.5 (1.3–1.9) | 1.9 (1.8–2.8) | 1.0 (0.9–1.9) | 0.8 (0.7–1.6) | 1.6 (1.2–2.0) | 1.0 (1.0–1.6) | 1.0 (1.0–1.6) | 5.0 (4.7–5.2) | 0.6 (0.5–0.6) |
| exip | encode (events) | 4.6 (4.6–4.7) | 5.4 (5.4–5.5) | 4.0 (4.0–4.1) | 4.3 (4.2–4.9) | 4.7 (4.7–4.8) | 5.7 (5.7–5.9) | 6.5 (6.4–6.6) | 4.9 (4.9–5.0) | 4.6 (4.5–10.4) | 5.7 (5.6–5.8) | 5.1 (5.0–5.2) | 5.3 (5.2–5.4) | 13.4 (13.2–13.7) | 3.7 (3.7–3.8) |
| exip | decode (exi) | 1.6 (1.6–1.6) | 1.7 (1.7–1.7) | 1.0 (0.9–1.4) | 1.0 (1.0–1.0) | 1.3 (1.3–1.4) | 2.4 (2.3–2.4) | 3.0 (2.9–3.1) | 1.7 (1.7–1.8) | 1.2 (1.2–1.2) | 2.2 (2.2–2.2) | 2.3 (2.3–2.3) | 2.0 (1.9–2.0) | 7.1 (7.0–7.3) | 0.7 (0.7–0.8) |

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
| EXIficient | encode (xml) | 12.9 (12.5–13.3) | 13.0 (12.7–13.4) | 11.5 (11.2–11.8) | 12.2 (12.0–12.7) | 12.5 (12.2–13.0) | 15.1 (14.8–15.6) | 17.6 (17.2–18.1) | 12.6 (12.2–15.4) | 17.3 (14.0–152.3) | 15.5 (15.0–16.1) | 15.7 (15.4–16.1) | 16.2 (15.8–16.7) | 27.6 (27.0–28.6) | 10.8 (10.6–11.2) |
| EXIficient | decode (exi) | 1.2 (1.2–1.3) | 1.2 (1.2–1.3) | 0.9 (0.8–0.9) | 1.0 (1.0–1.1) | 1.3 (1.3–1.3) | 1.9 (1.8–2.0) | 2.4 (2.3–2.4) | 1.4 (1.3–1.4) | 1.2 (1.2–1.2) | 1.8 (1.8–1.9) | 1.6 (1.5–1.6) | 1.6 (1.6–1.7) | 6.2 (6.1–6.3) | 0.6 (0.6–0.6) |
| Nagasena | encode (xml) | 5.7 (5.5–5.9) | 5.7 (5.5–5.9) | 5.1 (5.0–5.3) | 5.2 (5.0–5.4) | 5.6 (5.5–5.9) | 7.7 (7.5–7.9) | 9.2 (8.9–9.5) | 5.7 (5.6–8.3) | 5.9 (5.8–6.1) | 7.6 (7.4–7.8) | 7.8 (7.6–8.1) | 7.8 (7.7–8.1) | 17.8 (17.4–18.5) | 4.0 (3.9–4.1) |
| Nagasena | decode (exi) | 1.1 (1.0–1.1) | 1.1 (1.1–1.1) | 0.7 (0.7–0.7) | 0.9 (0.9–0.9) | 1.2 (1.1–1.2) | 1.8 (1.7–1.8) | 2.2 (2.2–2.3) | 1.3 (1.2–1.3) | 1.0 (1.0–1.0) | 1.7 (1.6–1.7) | 1.6 (1.5–1.6) | 1.5 (1.5–1.6) | 7.4 (7.2–7.6) | 0.6 (0.6–0.6) |
| Nagasena.NET | encode (xml) | 5.2 (5.0–5.4) | 5.1 (4.8–5.4) | 3.5 (3.3–3.8) | 4.3 (4.0–4.6) | 4.8 (4.6–5.1) | 7.1 (6.8–7.4) | 9.3 (9.0–9.8) | 4.8 (4.5–5.1) | 5.1 (4.9–5.5) | 7.9 (7.6–8.2) | 7.6 (7.3–8.0) | 7.6 (7.3–8.0) | 16.1 (15.6–16.6) | 3.3 (3.1–3.6) |
| Nagasena.NET | decode (exi) | 1.0 (0.6–1.1) | 0.8 (0.7–1.8) | 0.7 (0.5–0.8) | 0.7 (0.6–0.8) | 1.0 (0.7–1.1) | 1.0 (1.0–2.3) | 1.5 (1.3–2.2) | 1.0 (0.7–1.2) | 0.9 (0.6–1.0) | 1.4 (1.0–1.6) | 1.3 (0.8–1.5) | 0.9 (0.9–1.5) | 3.6 (3.4–3.7) | 0.5 (0.5–0.6) |
| exip | encode (events) | 4.4 (4.3–4.5) | 4.6 (4.5–4.6) | 3.9 (3.9–4.0) | 4.1 (4.1–4.2) | 4.5 (4.5–4.6) | 5.4 (5.3–5.5) | 7.0 (6.9–7.1) | 5.5 (5.5–5.6) | 4.4 (4.3–4.5) | 6.2 (5.3–6.3) | 4.9 (4.8–4.9) | 5.0 (4.9–5.1) | 12.3 (12.0–12.5) | 3.7 (3.6–3.7) |
| exip | decode (exi) | 1.3 (1.3–1.4) | 1.3 (1.3–1.3) | 0.8 (0.8–0.9) | 0.9 (0.9–0.9) | 1.2 (1.2–1.2) | 2.2 (2.1–2.2) | 3.0 (2.7–4.1) | 1.9 (1.8–1.9) | 1.1 (1.1–1.2) | 2.0 (2.0–2.1) | 1.8 (1.7–1.8) | 1.6 (1.6–1.7) | 6.8 (6.6–6.9) | 0.7 (0.7–0.7) |
| rexi | encode (xml) | 6.4 (6.3–6.5) | 5.3 (5.2–5.5) | 3.5 (3.3–3.7) | 3.7 (3.6–3.8) | 4.9 (4.8–5.1) | 7.8 (7.6–8.0) | 11.6 (11.3–11.9) | 4.7 (4.6–4.7) | 4.9 (4.8–5.0) | 8.0 (7.8–8.2) | 8.1 (8.0–8.3) | 8.6 (8.4–8.8) | 23.2 (22.7–23.6) | 3.0 (2.9–3.1) |
| rexi | encode (events) | 2.6 (2.6–2.6) | 2.1 (2.0–2.1) | 1.0 (1.0–1.1) | 1.3 (1.3–1.3) | 1.7 (1.7–1.7) | 2.9 (2.8–3.0) | 4.2 (4.1–4.3) | 1.7 (1.6–1.8) | 1.7 (1.6–1.7) | 2.9 (2.8–3.0) | 2.8 (2.7–2.8) | 2.7 (2.6–2.8) | 10.6 (10.3–10.8) | 1.4 (1.3–1.4) |
| rexi | decode (exi) | 0.9 (0.8–0.9) | 0.8 (0.8–0.8) | 0.6 (0.6–0.6) | 0.6 (0.6–0.6) | 0.7 (0.7–0.8) | 1.2 (1.2–1.2) | 1.6 (1.5–1.6) | 0.8 (0.8–0.8) | 0.7 (0.6–0.7) | 1.1 (1.0–1.1) | 1.1 (1.1–1.2) | 1.2 (1.2–1.3) | 4.1 (4.0–4.2) | 0.4 (0.4–0.4) |
| jexi | encode (xml) | 4.9 (4.8–5.2) | 5.1 (4.9–5.3) | 3.0 (2.8–3.2) | 3.4 (3.3–3.7) | 4.2 (4.1–4.5) | 7.8 (7.5–8.1) | 9.9 (9.6–10.3) | 4.2 (4.0–4.5) | 4.5 (4.4–4.8) | 8.0 (7.7–8.4) | 7.2 (7.0–7.6) | 7.4 (7.2–7.7) | 20.3 (19.7–21.1) | 3.2 (3.1–3.5) |
| jexi | encode (events) | 3.8 (3.0–30.7) | 3.2 (3.1–3.4) | 1.7 (1.6–1.9) | 2.0 (1.9–2.2) | 2.2 (2.1–2.4) | 4.4 (4.2–4.6) | 6.8 (6.5–7.8) | 4.6 (3.1–5.7) | 27.0 (4.2–45.4) | 4.5 (4.3–5.6) | 4.9 (4.8–5.2) | 4.6 (4.5–5.0) | 14.4 (13.9–15.2) | 2.0 (1.9–2.2) |
| jexi | decode (exi) | 0.9 (0.8–1.0) | 0.9 (0.8–1.2) | 0.6 (0.5–0.7) | 0.6 (0.6–0.7) | 0.8 (0.8–0.9) | 1.2 (1.2–1.4) | 1.8 (1.6–2.1) | 0.9 (0.9–1.1) | 0.7 (0.7–0.9) | 1.5 (1.4–1.7) | 1.2 (1.1–1.3) | 1.2 (1.1–1.3) | 5.7 (5.4–6.2) | 0.5 (0.4–0.6) |

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
| EXIficient | encode (xml) | 21.7 (21.2–22.8) | 21.5 (20.9–22.7) | 18.0 (17.6–18.7) | 21.0 (20.5–21.9) | 23.4 (22.9–24.2) | 30.8 (30.0–32.2) | 33.4 (32.5–35.1) | 24.0 (23.5–25.0) | 22.4 (22.0–23.1) | 28.8 (28.1–30.3) | 26.6 (26.1–27.6) | 25.8 (25.3–26.7) | 90.2 (87.8–94.5) | 14.7 (14.4–15.3) |
| EXIficient | decode (exi) | 5.7 (5.4–8.0) | 6.0 (5.5–9.0) | 4.4 (4.1–5.9) | 5.3 (5.2–6.7) | 6.8 (6.6–7.3) | 9.8 (9.6–10.2) | 10.7 (10.5–11.1) | 7.5 (7.3–7.8) | 5.7 (5.5–6.0) | 8.9 (8.7–10.2) | 8.0 (7.8–8.4) | 6.9 (6.7–7.2) | 41.1 (39.7–42.7) | 5.1 (4.0–6.0) |
| Nagasena | encode (xml) | 13.7 (13.2–17.8) | 13.6 (13.2–16.9) | 12.3 (11.7–16.3) | 13.5 (12.7–17.4) | 14.3 (13.6–18.3) | 17.3 (16.5–21.4) | 18.7 (18.2–22.5) | 14.5 (13.8–18.5) | 14.1 (13.5–18.2) | 16.5 (15.9–20.7) | 16.3 (15.5–20.5) | 16.0 (15.5–20.2) | 34.5 (33.7–38.8) | 11.2 (10.9–15.0) |
| Nagasena | decode (exi) | 3.0 (2.6–4.0) | 2.9 (2.5–3.8) | 2.5 (2.1–3.5) | 2.6 (2.2–3.5) | 3.0 (2.6–3.9) | 3.8 (3.4–4.8) | 4.5 (4.1–5.4) | 3.3 (2.9–4.2) | 2.9 (2.5–3.8) | 4.0 (3.6–5.0) | 4.6 (4.2–5.5) | 4.0 (3.6–4.9) | 9.9 (9.6–11.0) | 2.5 (2.1–3.4) |
| Nagasena.NET | encode (xml) | 57.5 (56.6–60.6) | 57.1 (56.2–60.4) | 55.5 (54.8–58.4) | 55.6 (54.8–58.4) | 56.3 (55.5–63.2) | 59.1 (58.3–62.6) | 61.6 (60.5–64.8) | 57.1 (56.4–60.4) | 57.4 (56.5–60.6) | 59.1 (58.2–62.3) | 59.3 (58.4–62.5) | 60.8 (59.8–64.3) | 70.0 (68.6–73.4) | 54.6 (53.7–57.6) |
| Nagasena.NET | decode (exi) | 2.1 (1.8–2.6) | 2.0 (1.7–2.5) | 1.6 (1.4–1.9) | 1.9 (1.6–2.2) | 2.2 (2.0–2.7) | 3.0 (2.7–3.5) | 3.5 (3.3–4.1) | 2.5 (2.2–2.9) | 2.1 (1.9–2.5) | 2.8 (2.6–3.4) | 6.5 (6.3–6.8) | 2.5 (2.2–3.2) | 12.3 (12.0–12.6) | 1.5 (1.3–1.8) |
| exip | encode (events) | 7.9 (7.8–8.0) | 8.0 (7.9–8.2) | 7.0 (6.9–7.0) | 7.4 (7.3–7.5) | 8.5 (8.4–8.6) | 10.3 (10.2–10.4) | 11.2 (11.1–11.3) | 8.7 (8.6–8.8) | 7.9 (7.8–8.0) | 10.1 (9.9–10.2) | 8.6 (8.5–8.8) | 8.6 (8.5–8.8) | 26.6 (25.9–27.2) | 6.0 (6.0–6.2) |
| exip | decode (exi) | 2.2 (2.2–2.2) | 2.1 (2.1–2.2) | 1.5 (1.5–1.6) | 1.6 (1.5–1.6) | 2.1 (2.1–2.2) | 3.4 (3.3–3.5) | 4.3 (4.2–4.5) | 2.5 (2.5–2.6) | 2.0 (1.9–2.0) | 3.3 (3.2–3.4) | 3.7 (3.6–3.8) | 2.8 (2.7–2.8) | 11.9 (11.6–12.1) | 1.3 (1.2–1.3) |

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
