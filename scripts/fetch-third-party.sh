#!/usr/bin/env bash
# Fetch the implementations pinned in third-party.lock into a cache outside the
# working tree, and optionally build them.
#
#   scripts/fetch-third-party.sh                 check out every pinned commit
#   scripts/fetch-third-party.sh --build         ... and build: exip (Release),
#                                                rexi (release), jexi (Julia
#                                                packages and precompile, when
#                                                julia is found), Nagasena jars
#                                                installed into ~/.m2 (the C#
#                                                port is built by scripts/run.sh)
#   scripts/fetch-third-party.sh --check         exit 1 unless every checkout is
#                                                present, clean and at its pin
#   scripts/fetch-third-party.sh --update NAME   re-pin NAME to its remote HEAD
#   scripts/fetch-third-party.sh --print-dir     print the cache directory
#   ... --sources                                also check out (or --check) the
#                                                upstream sources in sources.lock
#                                                (EXIficient, Nagasena) into
#                                                <cache>/sources; reading only
#
# Env: REXI_INTEROP_THIRD_PARTY  cache directory
#      (default ${XDG_CACHE_HOME:-$HOME/.cache}/rexi-interop/third_party)
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
LOCK="$ROOT/third-party.lock"
SOURCES_LOCK="$ROOT/sources.lock"
DIR="${REXI_INTEROP_THIRD_PARTY:-${XDG_CACHE_HOME:-$HOME/.cache}/rexi-interop/third_party}"

MODE=fetch
BUILD=0
SOURCES=0
UPDATE=""
while [[ $# -gt 0 ]]; do
    case "$1" in
        --build) BUILD=1 ;;
        --sources) SOURCES=1 ;;
        --check) MODE=check ;;
        --update) MODE=update; UPDATE="${2:?--update needs a name}"; shift ;;
        --print-dir) echo "$DIR"; exit 0 ;;
        -h|--help) sed -n '2,20p' "$0" | sed 's/^# \{0,1\}//'; exit 0 ;;
        *) echo "unknown argument: $1" >&2; exit 2 ;;
    esac
    shift
done

command -v git >/dev/null || { echo "missing required command: git" >&2; exit 1; }
[[ -f "$LOCK" ]] || { echo "missing $LOCK" >&2; exit 1; }

# Lock entries as "name url sha [sparse]" lines. A url starting with svn: is a
# Subversion repository: then sha is a revision and sparse the path inside it.
entries() { awk '!/^[[:space:]]*(#|$)/ { print $1, $2, $3, $4 }' "$LOCK"; }

# A path relative to this repository becomes an absolute local path.
resolve_url() {
    case "$1" in
        *://*|git@*) echo "$1" ;;
        /*) echo "$1" ;;
        *) (cd "$ROOT" && cd "$1" && pwd) ;;
    esac
}

sha_of() { awk -v n="$1" '!/^[[:space:]]*(#|$)/ && $1 == n { print $3 }' "$LOCK"; }

dirty() { [[ -n "$(git -C "$1" status --porcelain --untracked-files=no)" ]]; }

checkout() { # name url sha [sparse]
    local name="$1" url sha="$3" sparse="${4:-}" path="$DIR/$1"
    if [[ "$2" == svn:* ]]; then
        svn_checkout "$name" "${2#svn:}" "$sha" "$sparse" "$path"
        return
    fi
    url="$(resolve_url "$2")"
    if [[ ! -d "$path/.git" ]]; then
        [[ -e "$path" ]] && { echo "$path exists but is not a git checkout" >&2; return 1; }
        mkdir -p "$path"
        git -C "$path" init -q
        git -C "$path" remote add origin "$url"
        if [[ -n "$sparse" ]]; then
            git -C "$path" sparse-checkout set "$sparse"
        fi
    fi
    # The lock may have moved this entry to another repository.
    if [[ "$(git -C "$path" remote get-url origin)" != "$url" ]]; then
        git -C "$path" remote set-url origin "$url"
    fi
    if dirty "$path"; then
        echo "$name: $path has local changes; refusing to touch it" >&2
        return 1
    fi
    if [[ "$(git -C "$path" rev-parse -q --verify HEAD || true)" == "$sha" ]]; then
        echo "$name: at $sha"
        return 0
    fi
    echo "$name: fetching $sha"
    local filter=()
    [[ -n "$sparse" ]] && filter=(--filter=blob:none)
    # Fetching a bare commit needs uploadpack.allowReachableSHA1InWant (GitHub
    # allows it); a local repository may not, so fall back to fetching its refs.
    if ! git -C "$path" fetch -q --depth 1 "${filter[@]}" origin "$sha" 2>/dev/null; then
        git -C "$path" fetch -q "${filter[@]}" origin '+refs/heads/*:refs/remotes/origin/*'
    fi
    git -C "$path" checkout -q --detach "$sha"
}

check() { # name url sha
    local name="$1" sha="$3" path="$DIR/$1"
    if [[ "$2" == svn:* ]]; then
        svn_check "$name" "$sha" "$path"
        return
    fi
    if [[ ! -d "$path/.git" ]]; then
        echo "MISSING  $name ($path)" >&2
        return 1
    fi
    local head
    head="$(git -C "$path" rev-parse -q --verify HEAD || true)"
    if [[ "$head" != "$sha" ]]; then
        echo "MOVED    $name: at ${head:-nothing}, pinned $sha" >&2
        return 1
    fi
    if dirty "$path"; then
        echo "DIRTY    $name" >&2
        return 1
    fi
    echo "ok       $name $sha"
}

# sources.lock entries as "name vcs url rev [path]" lines.
source_entries() { awk '!/^[[:space:]]*(#|$)/ { print $1, $2, $3, $4, $5 }' "$SOURCES_LOCK"; }

svn_checkout() { # name url rev path [dest]
    local name="$1" url="$2" rev="$3" path="${5:-$DIR/sources/$1}" label="${5:+$1}"
    label="${label:-sources/$1}"
    require svn || return 1
    if [[ -e "$path" && ! -d "$path/.svn" ]]; then
        echo "$path exists but is not a Subversion checkout" >&2
        return 1
    fi
    if [[ -d "$path/.svn" ]]; then
        if [[ -n "$(svn status -q "$path")" ]]; then
            echo "$label: $path has local changes; refusing to touch it" >&2
            return 1
        fi
        if [[ "$(svn info --show-item revision "$path")" == "$rev" ]]; then
            echo "$label: at r$rev"
            return 0
        fi
        echo "$label: updating to r$rev"
        svn update -q -r "$rev" "$path"
    else
        echo "$label: checking out r$rev"
        mkdir -p "$(dirname "$path")"
        svn checkout -q -r "$rev" "$url/$4" "$path"
    fi
}

svn_check() { # name rev [dest]
    local name="$1" rev="$2" path="${3:-$DIR/sources/$1}" label="${3:+$1}"
    label="${label:-sources/$1}"
    if [[ ! -d "$path/.svn" ]]; then
        echo "MISSING  $label ($path)" >&2
        return 1
    fi
    local at
    at="$(svn info --show-item revision "$path")"
    if [[ "$at" != "$rev" ]]; then
        echo "MOVED    $label: at r$at, pinned r$rev" >&2
        return 1
    fi
    if [[ -n "$(svn status -q "$path")" ]]; then
        echo "DIRTY    $label" >&2
        return 1
    fi
    echo "ok       $label r$rev"
}

sources() { # fetch | check
    [[ -f "$SOURCES_LOCK" ]] || { echo "missing $SOURCES_LOCK" >&2; return 1; }
    local st=0 name vcs url rev path
    while read -r name vcs url rev path; do
        case "$vcs:$1" in
            git:fetch) checkout "sources/$name" "$url" "$rev" "$path" || st=1 ;;
            git:check) check "sources/$name" "$url" "$rev" || st=1 ;;
            svn:fetch) svn_checkout "$name" "$url" "$rev" "$path" || st=1 ;;
            svn:check) svn_check "$name" "$rev" || st=1 ;;
            *) echo "sources.lock: unknown kind $vcs for $name" >&2; st=1 ;;
        esac
    done < <(source_entries)
    return $st
}

require() { command -v "$1" >/dev/null || { echo "missing required command: $1" >&2; return 1; }; }

build() {
    require cmake && require cargo && require mvn || return 1

    local exip="$DIR/exip"
    echo "building exip (Release) in $exip/build"
    # Older exip code treats warnings as errors; newer compilers surface more.
    cmake -S "$exip" -B "$exip/build" -DCMAKE_BUILD_TYPE=Release -DSKIP_TESTS=ON \
        -DSKIP_VALGRIND=ON -DCMAKE_C_FLAGS="-Wno-error=type-limits" >/dev/null
    cmake --build "$exip/build" --target exipd -j

    echo "building rexi (release) into $DIR/rexi-target"
    cargo build -q --release --manifest-path "$DIR/rexi/Cargo.toml" \
        --target-dir "$DIR/rexi-target" -p rexi-xml --bin rexi

    if command -v julia >/dev/null; then
        echo "instantiating and precompiling jexi in $DIR/jexi"
        julia --startup-file=no --project="$DIR/jexi" -e 'using Pkg; Pkg.instantiate(); using JExi' >/dev/null
    else
        echo "warning: julia not found; jexi is not built (its column is left out)" >&2
    fi

    local lib="$DIR/exi-testsuite/candidates/openexi/lib"
    local version
    version="$(sha_of exi-testsuite | cut -c1-12)"
    # nagasena.jar: the codec; nagasena-rta.jar: its schema compiler (scomp).
    for artifact in nagasena nagasena-rta; do
        echo "installing org.openexi:$artifact:testsuite-$version into ~/.m2"
        mvn -q install:install-file -Dfile="$lib/$artifact.jar" -DgroupId=org.openexi \
            -DartifactId="$artifact" -Dversion="testsuite-$version" -Dpackaging=jar
    done
}

status=0
case "$MODE" in
    check)
        while read -r name url sha _sparse; do
            check "$name" "$url" "$sha" || status=1
        done < <(entries)
        if [[ $SOURCES -eq 1 ]]; then
            sources check || status=1
        fi
        exit $status
        ;;
    update)
        line="$(entries | awk -v n="$UPDATE" '$1 == n')"
        [[ -n "$line" ]] || { echo "no lock entry named $UPDATE" >&2; exit 2; }
        read -r name url old sparse <<<"$line"
        if [[ "$url" == svn:* ]]; then
            new="$(svn info --show-item last-changed-revision "${url#svn:}/$sparse")"
            [[ "$new" =~ ^[0-9]+$ ]] || { echo "could not resolve HEAD of $url" >&2; exit 1; }
        else
            new="$(git ls-remote "$(resolve_url "$url")" HEAD | awk '$2 == "HEAD" { print $1 }')"
            [[ ${#new} -eq 40 ]] || { echo "could not resolve HEAD of $url" >&2; exit 1; }
        fi
        awk -v n="$name" -v s="$new" \
            '!/^[[:space:]]*#/ && $1 == n { $3 = s; print; next } { print }' \
            "$LOCK" >"$LOCK.tmp" && mv "$LOCK.tmp" "$LOCK"
        echo "$name: $old -> $new"
        checkout "$name" "$url" "$new" "$sparse"
        ;;
    fetch)
        mkdir -p "$DIR"
        while read -r name url sha sparse; do
            checkout "$name" "$url" "$sha" "$sparse" || status=1
        done < <(entries)
        if [[ $SOURCES -eq 1 ]]; then
            sources fetch || status=1
        fi
        ;;
esac

if [[ $BUILD -eq 1 && $status -eq 0 ]]; then
    build || status=1
fi
echo "third-party cache: $DIR"
exit $status
