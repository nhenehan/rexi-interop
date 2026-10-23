#!/usr/bin/env python3
"""Run exip against the W3C EXI test suite's schema-informed declaredProductions tests.

    python3 scripts/w3c-declared.py --exip-driver build/exip/exip-driver --testsuite DIR
                                    [--oracle JAR] [--work reports/w3c-work] [--out reports/w3c-declared.md]

DIR is a full checkout of https://github.com/w3c/exi-testsuite (the cached
copy made by fetch-third-party.sh holds only the Nagasena jars, so it will not do).

Each test case takes its schema and options from the suite's config
(config/testCases-interop/schemaInformedGrammar/declaredProductions/*.xml: group
params, overridden per test case). Only strict cases run: useSchemas, no
schemaDeviations, no fragments, no Preserve options; the others are listed as
skipped with the reason. All streams are bit-packed, options out of band.

Per case:
  reference  EXIficient 1.0.7 (exip's utils/xsd2exip) encodes the XML;
  encode     exip encodes EXIficient's events for the reference (its dump-events:
             whitespace it dropped is gone, attributes in EXI order) and the bytes
             are compared with the reference;
  decode     exip decodes the reference and its events are compared with
             EXIficient's dump-events of the same stream.
exip's stderr is kept in <work>/<case>.{enc,dec}.err.
The schemas (main + transitive imports/includes) are encoded for exip with the
oracle's encode-xsd.
"""

import argparse
import os
import re
import subprocess
import sys
import xml.etree.ElementTree as ET

HERE = os.path.dirname(os.path.abspath(__file__))
ROOT = os.path.dirname(HERE)
CFG_REL = "config/testCases-interop/schemaInformedGrammar/declaredProductions"
XS = "{http://www.w3.org/2001/XMLSchema}"
P = "org.w3c.exi.ttf."


def params(elem, inherited):
    out = dict(inherited)
    for p in elem.findall("{*}param"):
        out[p.get("name")] = p.get("value")
    return out


def subst(value, env):
    return re.sub(r"\$\{([^}]+)\}", lambda m: env.get(m.group(1), m.group(0)), value)


def cases(testsuite):
    env = {"japex.exi.ttfms.testCasesDir": os.path.join(testsuite, "data")}
    cfg = os.path.join(testsuite, CFG_REL)
    for name in sorted(os.listdir(cfg)):
        if not name.endswith(".xml"):
            continue
        group = ET.parse(os.path.join(cfg, name)).getroot()
        gp = params(group, {})
        for tc in group.findall("{*}testCase"):
            tp = params(tc, gp)
            for _ in range(5):  # params refer to each other
                tp = {k: subst(v, {**env, **tp}) for k, v in tp.items()}
            yield tc.get("name").split(".")[-1], tp


def skip_reason(tp):
    if tp.get(P + "useSchemas") != "true":
        return "schema-less"
    if tp.get(P + "schemaDeviations") == "true":
        return "non-strict (schemaDeviations)"
    if tp.get(P + "fragments") == "true":
        return "fragment"
    if tp.get(P + "preserve"):
        return "Preserve." + tp[P + "preserve"].replace(" ", ",")
    return None


def schema_closure(path, seen=None):
    seen = [] if seen is None else seen
    path = os.path.normpath(path)
    if path in seen:
        return seen
    seen.append(path)
    for el in ET.parse(path).getroot():
        if el.tag in (XS + "import", XS + "include", XS + "redefine") and el.get(
            "schemaLocation"
        ):
            schema_closure(
                os.path.join(os.path.dirname(path), el.get("schemaLocation")), seen
            )
    return seen


def run(cmd):
    r = subprocess.run(cmd, capture_output=True, text=True)
    return r.returncode, r.stdout, r.stderr


def last_line(text):
    lines = [
        line
        for line in text.strip().splitlines()
        if line.strip() and not line.startswith("SLF4J")
    ]
    return lines[-1][:200] if lines else ""


def driver(a, op, schemas, *args, err_file):
    """Run the exip driver once; None on success, else "fail: <last stderr line>"."""
    cmd = [a.exip_driver, op, "--schema", schemas, "--strict", "--warmup", "0", "--iterations", "1", *args]
    code, _, err = run(cmd)
    if code == 0:
        return None
    with open(err_file, "w") as f:
        f.write(err)
    return "fail: " + last_line(err)


def main():
    ap = argparse.ArgumentParser(
        description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter
    )
    ap.add_argument(
        "--exip-driver",
        required=True,
        metavar="PATH",
        help="the exip driver that scripts/run.sh builds (build/exip/exip-driver)",
    )
    ap.add_argument(
        "--testsuite",
        required=True,
        metavar="DIR",
        help="a full checkout of https://github.com/w3c/exi-testsuite "
        "(needs its config/ and data/ folders)",
    )
    cache = os.environ.get("REXI_INTEROP_THIRD_PARTY") or os.path.join(
        os.environ.get("XDG_CACHE_HOME") or os.path.expanduser("~/.cache"),
        "rexi-interop",
        "third_party",
    )
    ap.add_argument(
        "--oracle",
        metavar="JAR",
        default=os.path.join(
            cache, "exip", "utils", "xsd2exip", "target", "xsd2exip-cli.jar"
        ),
        help="exip's xsd2exip-cli.jar (EXIficient 1.0.7); build it with "
        "'mvn -f <cache>/exip/utils/xsd2exip/pom.xml package' "
        "(default: %(default)s)",
    )
    ap.add_argument("--work", default=os.path.join(ROOT, "reports", "w3c-work"))
    ap.add_argument("--out", default=os.path.join(ROOT, "reports", "w3c-declared.md"))
    a = ap.parse_args()
    for f in (a.exip_driver, a.oracle):
        if not os.path.exists(f):
            sys.exit("missing " + f)
    os.makedirs(os.path.join(a.work, "schemas"), exist_ok=True)

    schema_exi = {}

    def encode_schema(xsd):
        if xsd not in schema_exi:
            out = os.path.join(a.work, "schemas", os.path.basename(xsd) + ".exi")
            code, _, err = run(
                [
                    "java",
                    "-jar",
                    a.oracle,
                    "encode-xsd",
                    "--xsd",
                    xsd,
                    "--xsd-exi-out",
                    out,
                ]
            )
            if code != 0:
                raise RuntimeError("encode-xsd " + xsd + ": " + last_line(err))
            schema_exi[xsd] = out
        return schema_exi[xsd]

    rows = []
    for name, tp in cases(a.testsuite):
        xml = tp["japex.inputFile"]
        why = skip_reason(tp)
        if why:
            rows.append((name, "skip", why, "", ""))
            continue
        xsd = tp[P + "schemaLocation"]
        w = os.path.join(a.work, name)
        try:
            schemas = ",".join(encode_schema(s) for s in schema_closure(xsd))
        except (RuntimeError, OSError, ET.ParseError) as ex:
            rows.append((name, os.path.basename(xsd), "schema: " + str(ex), "", ""))
            continue
        code, _, err = run(
            [
                "java",
                "-jar",
                a.oracle,
                "encode-xml",
                "--xsd",
                xsd,
                "--xml",
                xml,
                "--out",
                w + ".ref.exi",
                "--strict",
            ]
        )
        if code != 0:
            rows.append(
                (
                    name,
                    os.path.basename(xsd),
                    "reference: EXIficient rejects (" + last_line(err) + ")",
                    "",
                    "",
                )
            )
            continue
        # EXIficient's own events for its reference: whitespace it dropped is gone,
        # attributes are in EXI order, values are as it typed them.
        code, want, err = run(
            ["java", "-jar", a.oracle, "dump-events", "--xsd", xsd, "--exi", w + ".ref.exi", "--strict"]
        )
        if code != 0:
            rows.append(
                (name, os.path.basename(xsd), "EXIficient cannot decode its reference: " + last_line(err), "", "")
            )
            continue
        with open(w + ".ref.events", "w") as f:
            f.write(want)
        ref = open(w + ".ref.exi", "rb").read()

        enc = driver(a, "encode", schemas, "--events", w + ".ref.events", "--out", w + ".exip.exi", err_file=w + ".enc.err")
        if enc is None:
            got = open(w + ".exip.exi", "rb").read()
            enc = "=" if got == ref else "≠ %d B vs %d B (%s vs %s)" % (len(got), len(ref), got.hex(), ref.hex())

        dec = driver(a, "decode", schemas, "--exi", w + ".ref.exi", "--events-out", w + ".exip.events", err_file=w + ".dec.err")
        if dec is None:
            got = open(w + ".exip.events").read().splitlines()
            want = want.splitlines()
            if got == want:
                dec = "="
            else:
                i = next((i for i, (x, y) in enumerate(zip(got, want)) if x != y), min(len(got), len(want)))
                dec = "≠ event %d: `%s` vs `%s`" % (
                    i,
                    got[i] if i < len(got) else "<end>",
                    want[i] if i < len(want) else "<end>",
                )
        rows.append((name, os.path.basename(xsd), "ran", enc, dec))

    ran = [r for r in rows if r[2] == "ran"]
    with open(a.out, "w") as f:
        f.write(
            "# W3C declaredProductions: exip vs EXIficient 1.0.7 (strict, bit-packed)\n\n"
        )
        f.write("exip driver: `%s`\n\n" % a.exip_driver)
        f.write("| | cases |\n|---|---|\n")
        f.write("| in the suite | %d |\n" % len(rows))
        f.write(
            "| skipped (not strict-eligible) | %d |\n"
            % sum(r[1] == "skip" for r in rows)
        )
        f.write(
            "| no reference (schema or EXIficient) | %d |\n"
            % sum(r[1] != "skip" and r[2] != "ran" for r in rows)
        )
        f.write("| run | %d |\n" % len(ran))
        f.write("| encode = | %d |\n" % sum(r[3] == "=" for r in ran))
        f.write("| encode ≠ | %d |\n" % sum(r[3].startswith("≠") for r in ran))
        f.write("| encode fail | %d |\n" % sum(r[3].startswith("fail") for r in ran))
        f.write("| decode = | %d |\n" % sum(r[4] == "=" for r in ran))
        f.write("| decode ≠ / fail | %d |\n\n" % sum(r[4] != "=" for r in ran))
        f.write("| case | schema | status | encode | decode |\n|---|---|---|---|---|\n")
        for r in rows:
            f.write("| %s |\n" % " | ".join(c.replace("|", "\\|") for c in r))
    print(open(a.out).read().split("| case |")[0])


if __name__ == "__main__":
    main()
