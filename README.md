# rexi-interop

rexi-interop checks that six EXI libraries understand each other. Every library
encodes the same XML documents, and every library decodes every other library's
output. It also times each library. The results are in
[reports/latest.md](reports/latest.md).

The libraries:

| implementation | language | how it is driven |
|---|---|---|
| [EXIficient](https://github.com/EXIficient/exificient) 1.0.7 | Java | in-process |
| [Nagasena](https://sourceforge.net/projects/openexi/) (OpenEXI) 0000.0002.0073.0 | Java | in-process |
| [exip](https://github.com/nhenehan/exip) | C | `drivers/exip` (this repo), linked against libexip |
| [Nagasena for .NET](https://sourceforge.net/projects/openexi/) (OpenEXI C# port) 0000.0002.0056.1 | C# | `drivers/nagasena-cs` (this repo), .NET 10 |
| [rexi](https://github.com/nhenehan/rexi) | Rust | `rexi bench` (the rexi CLI) |
| [jexi](https://github.com/nhenehan/jexi) | Julia | `bin/jexi bench` (the jexi CLI, rexi's command line) |

For every case and profile, every implementation encodes the document and every
implementation decodes every encoding (encode-by-X / decode-by-Y). Decoded events
are checked against the XML; on strict lanes the bytes are also compared with
EXIficient's. Then each implementation times itself in-process.

## How to run it

You need:

- Java 25 and Maven
- CMake and a C compiler
- Rust (cargo)
- Julia, for the jexi column (without it, `run.sh` leaves the column out and says so)
- Subversion, for the Nagasena for .NET checkout
- optional: the .NET SDK 10, for the Nagasena for .NET column (`dotnet` on `PATH`,
  `$DOTNET_ROOT` or `~/.dotnet`; without it, `run.sh` leaves the column out and says so)

Then:

```bash
scripts/fetch-third-party.sh --build   # once: download the pinned libraries and build them
scripts/run.sh                         # the full run: writes reports/latest.md and reports/latest.json
```

Other ways to run it:

```bash
scripts/run.sh --no-timing             # correctness only (a few seconds)
scripts/run.sh --cases S1,R1 --profiles P0 --warmup 500 --iterations 1000
scripts/fetch-third-party.sh --sources # optional: EXIficient and Nagasena sources, for reading
```

The full run takes about 36 minutes, most of it timing. The downloaded libraries go
to a cache outside this folder (`~/.cache/rexi-interop/third_party` by default, see
[Third parties and pins](#third-parties-and-pins)); in this folder, only `build/`,
`harness/target/` and `reports/` are written.

## How to read the report

[reports/latest.md](reports/latest.md) starts with the machine and the exact version
of every library, then the list of test cases. Then there is one section per
profile (P0 to P3, see [Method](#method)), each with up to three tables:

- **Encode by X, decode by Y.** One row per encoder → decoder pair, one column per
  test case.
  - ✓ means the decoder got back exactly the document's events.
  - ✗ means it did not. The number next to it points to a note at the end of the
    report that says what went wrong. "enc ✗" means the encoder itself failed.
  - – means the library does not support that profile (for example, rexi and jexi
    have no compression).
- **Bytes compared with EXIficient's** (strict profiles P0 and P2 only). `=` means
  the encoder wrote the same bytes as EXIficient; `≠ 51 B vs 50 B` means it did
  not, and gives both sizes.
- **Timing.** Each cell is the median time for one run, in microseconds (µs), with
  the 10th and 90th percentiles in parentheses: `5.3 (5.1–5.6)` means half of the runs
  took 5.3 µs or less, and 8 runs in 10 took between 5.1 and 5.6 µs. Lower is faster.
  "encode (xml)" starts from XML text, so XML parsing is included; "encode
  (events)" starts from events that were parsed beforehand; "decode (exi)" decodes
  EXIficient's stream. A ✗ here means the timing run failed (see its note); – means
  there is no timing for that cell. Each library times itself in its own
  process, after a warm-up, so start-up and schema loading are not counted.

## Results

From the committed report (`reports/latest.md`, every cell and message).

- **Interoperability.** On every profile each implementation takes part in, every
  encoder→decoder pair decodes to the document's events, on all 14 cases, with
  one exception: EXIficient drops `xsi:nil="false"` when encoding (X2), so its
  stream decodes without that attribute ("Note: in some cases we can simply skip
  the xsi:nil event",
  [`AbstractEXIBodyEncoder.java#L886-L894`](https://github.com/EXIficient/exificient-core/blob/9d853cb456a9d5557fa9d4544917a894c576ab96/src/main/java/com/siemens/ct/exi/core/coder/AbstractEXIBodyEncoder.java#L886-L894),
  v1.0.7). The other encoders keep the attribute and are byte-identical to each
  other there.
- **Who takes part where.** EXIficient, Nagasena, Nagasena for .NET and exip run
  all four profiles; rexi and jexi run P0 and P2 (they implement neither
  non-strict nor compression).
- **Byte equality.** On P0 and P2, every encoder's bytes equal EXIficient's on
  every case but X2.
- **Speed.** See `reports/latest.md` for every timing; the sections below give
  the headlines.

### rexi

The Rust implementation. Its decoder is below EXIficient's median on all 14
cases in P0 and P2, by 1.25× or more (U1 4.4 against 5.8 µs in P0, 4.1 against
6.1 in P2); its encoder, from XML with parsing included, is below EXIficient's
too.

### jexi

The Julia port of rexi, driven through `bin/jexi`, which takes rexi's command
line and prints the same bench JSON (Julia 1.12.7, with a PrecompileTools
workload). Same results as rexi in every cell it takes part in.

- **Decoding**: 0.4–6.0 µs median, below EXIficient's on every case but P0 U1
  (6.0 against 5.8), 0.92–1.36× rexi. Its p90 is higher than EXIficient's
  (collector pauses).
- **Encoding from XML** (parsing included): 2.9–21.6 µs, below EXIficient's on
  all 14 cases in P0 and P2 (U1 20.3 against 27.3 in P0), 0.76–0.97× rexi. jexi
  streams XML.jl's tokens straight into the encoder.
- **Encoding from events**: 1.7–6 µs (U1 14.4–14.7), 1.2–1.8× rexi.

### Nagasena for .NET

svn r1188 (2016-08-22, version 0000.0002.0056.1, the port's last change), built
for .NET 10.

- **Correct and interoperable on every profile.** It reads and is read by all four
  other implementations in P0–P3 (rexi only where rexi takes part: P0, P2), on all
  14 cases. On P0 and P2 its bytes equal EXIficient's on every case, and so also
  Java Nagasena's and rexi's.
- **X2** fails exactly as Java Nagasena does: EXIficient drops `xsi:nil="false"`, so
  its stream decodes without the attribute (see above). Its own X2 stream is the
  same as Java Nagasena's, one byte longer than EXIficient's.
- **Speed.** On P0–P2 it is about as fast as Java Nagasena. Compression encode
  (P3) is 2–6× slower than Java Nagasena whatever the document size: most of it
  is SharpZipLib's `Deflater.Reset()`, which runs three times per stream.

## License

rexi-interop is under the BSD 3-Clause License; see [LICENSE](LICENSE).

Some files keep the license of the project they come from:

- `fixtures/seed/` (cases S1–S5) are copied from exip's `interop-tests`
  (`interop-tests/src/test/resources/fixtures/standards/cases/` in
  [nhenehan/exip](https://github.com/nhenehan/exip)), under exip's BSD 3-Clause license.
- `fixtures/rexi/` (cases R1–R5) are copied from rexi's
  `crates/rexi-tests/fixtures/interop/` ([nhenehan/rexi](https://github.com/nhenehan/rexi)),
  under rexi's BSD 3-Clause license.
- The other fixtures (X1, X2, U1, B1) were written for this repository.

The third parties that the scripts and builds download (EXIficient from Maven
Central, Nagasena and Nagasena for .NET, exip, rexi, jexi, the W3C exi-testsuite,
and the packages their builds pull in, such as SharpZipLib from NuGet) are not part
of this repository and keep their own licenses.

## Thanks

EXI is a W3C Recommendation, and it is a really good one: a careful design, written up clearly,
with solid open libraries behind it. That work is what got this project started. rexi and JExi are
my effort to take EXI further, to more languages and more places, and this comparison exists to
show how well all of these libraries work together.

- **Rumen Kyusakov** wrote [exip](https://exip.sourceforge.net/), a compact C implementation of EXI,
  and co-edited the Second Edition of the [EXI specification](https://www.w3.org/TR/exi/).
- **Daniel Peintner** wrote [EXIficient](https://exificient.github.io/) with Richard Kuntschke at
  Siemens, co-edited the Second Edition, and edited the [EXI Primer](https://www.w3.org/TR/exi-primer/).
  EXIficient's bytes are the reference this matrix compares against.
- **Takuki Kamiya** edited the First Edition, chaired the EXI Working Group, and wrote Nagasena
  ([OpenEXI](https://sourceforge.net/projects/openexi/)), including its .NET port. **John Schneider**
  also edited the First Edition, and **Santiago Pericas-Geertsen** co-edited the Primer and wrote the
  W3C test framework this repository runs.
- The **W3C EXI Working Group** as a whole made the specification all of these libraries follow.

Thank you.

## For maintainers / details

### Where each library comes from

| implementation | built from | upstream source (`--sources`) |
|---|---|---|
| EXIficient 1.0.7 | Maven Central `com.siemens.ct.exi:*:1.0.7` | git: [exificient](https://github.com/EXIficient/exificient.git), [-core](https://github.com/EXIficient/exificient-core.git), [-grammars](https://github.com/EXIficient/exificient-grammars.git) at `v1.0.7` |
| Nagasena 0000.0002.0073.0 | `nagasena.jar` / `nagasena-rta.jar` from the pinned [W3C exi-testsuite](https://github.com/w3c/exi-testsuite.git), installed into `~/.m2` (not on Maven Central) | svn: `https://svn.code.sf.net/p/openexi/code/trunk/nagasena` r1206 |
| exip | git clone of `https://github.com/nhenehan/exip.git`, built with CMake | the same checkout |
| Nagasena for .NET 0000.0002.0056.1 | svn: `https://svn.code.sf.net/p/openexi/code/trunk/nagasenacs` r1188, built with the .NET SDK by `scripts/run.sh` | the same checkout |
| rexi | git clone of `https://github.com/nhenehan/rexi.git`, built with cargo | the same checkout |
| jexi | git clone of `https://github.com/nhenehan/jexi.git`, packages instantiated and precompiled with julia | the same checkout |

### Third parties and pins

Third parties live in
`${REXI_INTEROP_THIRD_PARTY:-~/.cache/rexi-interop/third_party}`, never in this
tree; `third-party.lock` pins them (`scripts/fetch-third-party.sh --check`,
`--update NAME`).
`sources.lock` pins the upstream sources of the packaged Java builds; they are
checked out only with `--sources` (Subversion is needed for Nagasena), into
`<cache>/sources`, and are never built or run by the harness.

### Layout

| path | contents |
|---|---|
| `fixtures/cases.tsv` | the cases: S1–S5 from exip's `interop-tests`, R1–R5 rexi's byte-exact fixtures, X1 `xsi:type` casts, X2 `xsi:nil`, U1 multilingual UTF-8 and B1 hexBinary / base64Binary (this repo) |
| `drivers/exip/` | `exip-driver`: encode from an event stream / decode to events, with timing loops (C, CMake) |
| `drivers/nagasena-cs/` | `nagasena-cs-driver`: encode XML / decode to events with Nagasena for .NET, with timing loops (C#); `OpenEXI.Nagasena.csproj` builds the svn checkout with today's SDK (upstream used NAnt and .NET Framework 4.0) |
| `harness/` | the matrix runner and report writer (Java 25, Maven; `mvn test` checks the comparison logic) |
| `scripts/` | `fetch-third-party.sh`, `run.sh`, `w3c-declared.py` |
| `reports/` | `latest.md` / `latest.json` from the last full run (committed as a snapshot) |

### Method

- **Profiles.** P0 strict bit-packed, P1 non-strict bit-packed, P2 strict
  byte-aligned, P3 strict compression (the seed project's set). Options always
  travel out of band: no options document, no cookie.
- **Correctness.** The reference is the XML's own event stream: `xsi:type` and
  `xsi:nil` first, other attributes sorted by local name then namespace, adjacent
  text merged, whitespace-only text dropped. Values compare as written, then as
  numbers (`2495E-2` = `24.95`), then as booleans.
- **Byte equality.** On P0 and P2, every encoder's stream is compared with
  EXIficient's (the reference rexi was built against).
- **Timing.** Every implementation times itself inside its own process; process
  start-up is never measured. Schema loading is done once and excluded.
  - Encoders time their natural input: XML bytes (parsing included) for
    EXIficient, both Nagasenas, rexi and jexi; an event stream parsed beforehand for exip
    (it has no XML front end). rexi and jexi are also timed from pre-parsed events,
    the comparable rows for exip.
  - Decoders all decode EXIficient's stream for the profile, with handlers that
    only count events.
  - Defaults: a warm-up of at least 2000 runs and 3 s (`--warmup`,
    `--warmup-ms`), then 2000 timed iterations per cell; the report gives the
    median and p10–p90 in microseconds. The warm-up is timed because a JIT
    reaches its optimized code after some time, not after a count: .NET's tiered
    JIT waits for about 100 ms without new compiles and then optimizes in stages.
    With 2000 runs only (about 10 ms on a small case), Nagasena.NET was timed on
    unoptimized code: S3 P0 14–22 µs instead of 4, U1 123–143 instead of 19. Its
    medians level off after 2–3 s of warm-up (measured 2026-10-18). A full run
    takes about 36 minutes; `--warmup-ms 0` gives the old count-only warm-up.
- **Nagasena for .NET.** The C# port reads compiled schemas but has no schema
  compiler, so the harness compiles each case's XSD with Java Nagasena
  (`EXISchemaFactory`) and hands the driver the binary form
  (`EXISchema.writeOut`, read back by the C# `EXISchema.readIn`). The driver encodes
  XML through `Transmogrifier` (.NET's `XmlReader` underneath) and decodes with
  `EXIDecoder`'s event scanner. SharpZipLib 1.4.2 from NuGet stands in for the
  0.86 DLL in upstream's `lib/`. Timing is in-process, after JIT warm-up.
- **The exip driver.** exip reports the expected value type when the encoder
  calls `startElement` / `attribute`; the driver converts each lexical value
  accordingly (integers, floats and decimals to mantissa/exponent with trailing
  zeros folded into the exponent, booleans, date-times; strings and enumerations
  through `stringData`, base64Binary through `binaryData`, `xs:list` values item by item
  through `listData`, each item in the type exip reports for it). For character data
  it asks exip for the type in the current state (`serialize.valueType`,
  when the exip tree has `EXIP_SERIALIZER_VALUE_TYPE`), so simple
  content after attributes and `xsi:type` casts get their type; older exip
  trees fall back to `startElement`'s report. It drops whitespace-only text where
  exip expects no character data, as an XML front end would.
  EXIficient's encoder also drops it between tags in mixed content, citing an EXI WG
  mail ([`AbstractEXIBodyEncoder.java#L1490-L1566`](https://github.com/EXIficient/exificient-core/blob/9d853cb456a9d5557fa9d4544917a894c576ab96/src/main/java/com/siemens/ct/exi/core/coder/AbstractEXIBodyEncoder.java#L1490-L1566),
  v1.0.7); rexi follows it too. Binary values are hex for
  `xs:hexBinary` (upper case, as EXIficient prints it) and base64 for
  `xs:base64Binary`, from `serialize.exiType` / `parse.valueExiType` (with
  `EXIP_SERIALIZER_EXI_TYPE` / `EXIP_PARSER_VALUE_EXI_TYPE`); older exip trees get
  base64 throughout.

### W3C EXI test suite

The broad correctness matrix is the [W3C exi-testsuite](https://github.com/w3c/exi-testsuite).
It needs a full checkout of that repository (the cached copy that
`scripts/fetch-third-party.sh` makes holds only the Nagasena jars).

Optional, not yet published: the rexi results below come from a local branch of the
suite that adds a rexi candidate (it wraps the `rexi` binary from a rexi release
build) and changes the framework so that it runs on JDK 25 (the upstream framework
and candidates were written for Java 5–8). The branch is called `java25`, and its
`JAVA25.md` explains what had to change for JDK 25. With that branch checked out
next to this repository, the run was:

```bash
cd ../exi-testsuite
(cd candidates/rexi && ant)    # the rexi candidate: wraps ../rexi/target/release/rexi
ant -f build-interop.xml run-iot-encoding-classes-java -DtestCases=config/testCases-interop/all.xml
```

Result on 2026-10-10 (bit-packed encoding, all 3172 interop cases): EXIficient
3172, OpenEXI (Nagasena) 3172, rexi 342 (296 before a schema-compiler fix of the same day).
- rexi declines 2773 cases as unsupported features: non-strict 1066,
  compression 771, pre-compression 761, fragments 60, `Preserve.lexicalValues`
  54, schema-less 46, datatype representation maps 14, plus 1 other.
- The rest need features rexi does not have yet: values for declared attribute
  wildcards (`AT(uri:*)`, reported as "non-strict", 31), wildcard element content
  (17), and a few edge cases.
- The 112 cases that failed on one schema-compiler bug (a namespace named only in
  an attribute wildcard's list was not added to the uri partition) pass since
  that fix.
- On the restricted v1 set, rexi passes the 5 strict schema-informed FixML 4.4
  cases and declines the other 109.

#### exip on the declaredProductions group

`scripts/w3c-declared.py` runs exip on the suite's schema-informed
declaredProductions cases. Each case gets the schema and options from its config,
and only strict-eligible cases run. EXIficient 1.0.7 (exip's `utils/xsd2exip`)
encodes the reference bytes. exip encodes EXIficient's decoded events for them,
compares the bytes, and decodes the reference. It needs a full checkout of
https://github.com/w3c/exi-testsuite (`--testsuite`, required) and exip's
`xsd2exip-cli.jar` (`--oracle`; by default the one in the cached exip checkout,
built with `mvn -f <cache>/exip/utils/xsd2exip/pom.xml package`).

```bash
git clone https://github.com/w3c/exi-testsuite.git /path/to/exi-testsuite
scripts/run.sh --no-timing     # builds build/exip/exip-driver
python3 scripts/w3c-declared.py --exip-driver build/exip/exip-driver \
    --testsuite /path/to/exi-testsuite   # writes reports/w3c-declared.md
```

On 2026-10-11, before exip's later schema-informed conformance fixes, 59 of 80
cases were strict-eligible; 1 passed and 57 failed at schema loading.
