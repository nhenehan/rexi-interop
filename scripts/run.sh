#!/usr/bin/env bash
# Build the drivers and the harness, then run the interop matrix and timing loops.
#
#   scripts/run.sh                         full run (writes reports/latest.{md,json})
#   scripts/run.sh --no-timing             correctness matrix only
#   scripts/run.sh --warmup 200 --warmup-ms 500 --iterations 500 --cases S1,R1 --profiles P0,P2
#
# Needs: scripts/fetch-third-party.sh --build (exip, rexi, Nagasena), cmake, a C
# compiler, Maven and Java 25. Extra arguments go to the harness.
# jexi joins the matrix when julia is found and the pinned jexi checkout exists.
# Nagasena for .NET joins the matrix when the .NET SDK (10 or later) is found:
# `dotnet` on PATH, or $DOTNET_ROOT/dotnet, or ~/.dotnet/dotnet. Without it the
# column is left out, with a warning.
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
TP="$("$ROOT/scripts/fetch-third-party.sh" --print-dir)"
"$ROOT/scripts/fetch-third-party.sh" --check >/dev/null || {
    echo "third parties missing or off their pins: run scripts/fetch-third-party.sh --build" >&2
    exit 1
}

pin() { awk -v n="$1" '!/^[[:space:]]*(#|$)/ && $1 == n { print $3 }' "$ROOT/third-party.lock"; }

echo "building drivers/exip" >&2
cmake -S "$ROOT/drivers/exip" -B "$ROOT/build/exip" -DEXIP_DIR="$TP/exip" \
    -DCMAKE_BUILD_TYPE=Release >/dev/null
cmake --build "$ROOT/build/exip" >/dev/null

DOTNET="$(command -v dotnet || true)"
for d in "${DOTNET_ROOT:-}" "$HOME/.dotnet"; do
    [[ -z "$DOTNET" && -n "$d" && -x "$d/dotnet" ]] && DOTNET="$d/dotnet"
done
CS_ARGS=()
CS_META=()
if [[ -n "$DOTNET" ]]; then
    # the driver's launcher finds the runtime through DOTNET_ROOT
    DOTNET_ROOT="$(dirname "$(readlink -f "$DOTNET")")"
    export DOTNET_ROOT
    echo "building drivers/nagasena-cs" >&2
    DOTNET_CLI_TELEMETRY_OPTOUT=1 DOTNET_NOLOGO=1 "$DOTNET" build -c Release --nologo -v q \
        "$ROOT/drivers/nagasena-cs/nagasena-cs-driver.csproj" \
        -p:NagasenaCsDir="$TP/nagasenacs" -o "$ROOT/build/nagasena-cs" >/dev/null
    CS_ARGS=(--nagasena-cs-driver build/nagasena-cs/nagasena-cs-driver)
    CS_META=(--meta "Nagasena.NET=OpenEXI svn r$(pin nagasenacs) trunk/nagasenacs, drivers/nagasena-cs (.NET $("$DOTNET" --version)), schemas compiled by Java Nagasena")
else
    echo "warning: no .NET SDK found; the Nagasena.NET column is left out" >&2
fi

JEXI_ARGS=()
JEXI_META=()
if command -v julia >/dev/null && [[ -x "$TP/jexi/bin/jexi" ]]; then
    JEXI_ARGS=(--jexi "$TP/jexi/bin/jexi")
    JEXI_META=(--meta "jexi=$(pin jexi | cut -c1-12), bin/jexi ($(julia --version))")
else
    echo "warning: julia or the jexi checkout not found; the jexi column is left out" >&2
fi

echo "building the harness" >&2
(cd "$ROOT/harness" && mvn -q -Dnagasena.version="testsuite-$(pin exi-testsuite | cut -c1-12)" clean package)

REXI="$TP/rexi-target/release/rexi"
[[ -x "$REXI" ]] || { echo "rexi not built: run scripts/fetch-third-party.sh --build" >&2; exit 1; }

self="$(git -C "$ROOT" rev-parse --short=12 HEAD 2>/dev/null || echo uncommitted)"
[[ -n "$(git -C "$ROOT" status --porcelain 2>/dev/null)" ]] && self="$self (with local changes)"

cd "$ROOT"
java -jar harness/target/rexi-interop-harness.jar \
    --fixtures fixtures --work reports/work --out reports \
    --rexi "$REXI" --exip-driver build/exip/exip-driver "${CS_ARGS[@]}" "${JEXI_ARGS[@]}" \
    --meta "rexi-interop=$self" \
    --meta "EXIficient=1.0.7 (Maven Central), in-process" \
    --meta "Nagasena=W3C exi-testsuite $(pin exi-testsuite | cut -c1-12) candidates/openexi/lib, in-process" \
    --meta "exip=nhenehan/exip $(pin exip | cut -c1-12), Release build, drivers/exip ($(cc --version | head -1))" \
    --meta "rexi=$(pin rexi | cut -c1-12), release build, rexi bench ($(rustc --version))" \
    "${CS_META[@]}" "${JEXI_META[@]}" \
    "$@" 2> >(grep -v '^SLF4J' >&2)
