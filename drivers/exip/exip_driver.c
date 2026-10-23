/*
 * exip-driver: the exip adapter of the rexi-interop benchmark.
 *
 *   exip-driver encode --schema a.xsd.exi[,b.xsd.exi] [stream options] [bench
 * options]
 *                      --events in.events --out out.exi
 *   exip-driver decode --schema a.xsd.exi[,...]       [stream options] [bench
 * options]
 *                      --exi in.exi --events-out out.events
 *
 * stream options (always out of band: no options document, no cookie):
 *   --strict                strict schema-informed grammars
 *   --alignment bit-packed|byte-aligned|pre-compression|compression
 *   --block-size N          (pre-compression, compression; default 1000000)
 * bench options:
 *   --warmup N (default 20)  --warmup-ms MS (default 0)  --iterations N (default 200)
 *   (warm-up runs at least N times and for at least MS milliseconds)
 *
 * exip has no XML front end, so the encoder reads the document as an event
 * stream in the harness's .events format (one event per line) and turns each
 * lexical value into the value type exip reports for that position. The event
 * file is parsed before timing; the timed region is exip's serializer only.
 * The decoder times parsing with handlers that only count events, then makes
 * one more pass that writes the .events dump.
 *
 * Prints one JSON object on stdout:
 *   {"impl":"exip","op":...,"input":...,"input_bytes":...,"output":...,
 *    "setup_ns":...,"warmup":...,"iterations":...,"ns":[...]}
 * Exits 1 with a message on stderr on any failure.
 */
#define _POSIX_C_SOURCE 200809L

#include <errno.h>
#include <limits.h>
#include <stdint.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <time.h>

#include "EXIParser.h"
#include "EXISerializer.h"
#include "grammarGenerator.h"
#include "stringManipulate.h"

#define MAX_SCHEMAS 10
#define CHUNK 4096

static void die(const char *fmt, const char *arg) {
    fprintf(stderr, "exip-driver: ");
    fprintf(stderr, fmt, arg);
    fprintf(stderr, "\n");
    exit(1);
}

static void die_code(const char *what, errorCode code) {
    fprintf(stderr, "exip-driver: %s failed with exip error %d\n", what, (int)code);
    exit(1);
}

static uint64_t now_ns(void) {
    struct timespec ts;
    clock_gettime(CLOCK_MONOTONIC, &ts);
    return (uint64_t)ts.tv_sec * 1000000000ull + (uint64_t)ts.tv_nsec;
}

/* ---- files and growable byte buffers ---------------------------------- */

typedef struct {
    char *data;
    size_t len, cap;
} Bytes;

static void bytes_append(Bytes *b, const void *src, size_t n) {
    if (b->len + n + 1 > b->cap) {
        size_t cap = b->cap ? b->cap : CHUNK;
        while (cap < b->len + n + 1)
            cap *= 2;
        b->data = realloc(b->data, cap);
        if (!b->data)
            die("%s", "out of memory");
        b->cap = cap;
    }
    memcpy(b->data + b->len, src, n);
    b->len += n;
    b->data[b->len] = '\0';
}

static void bytes_puts(Bytes *b, const char *s) { bytes_append(b, s, strlen(s)); }

static Bytes read_file(const char *path) {
    Bytes b = {0};
    FILE *f = fopen(path, "rb");
    if (!f)
        die("cannot open %s", path);
    char tmp[CHUNK];
    size_t n;
    while ((n = fread(tmp, 1, sizeof tmp, f)) > 0)
        bytes_append(&b, tmp, n);
    fclose(f);
    if (!b.data)
        bytes_append(&b, "", 0);
    return b;
}

static void write_file(const char *path, const char *data, size_t len) {
    FILE *f = fopen(path, "wb");
    if (!f || fwrite(data, 1, len, f) != len)
        die("cannot write %s", path);
    fclose(f);
}

/* ---- options and schema ------------------------------------------------ */

typedef struct {
    const char *mode;
    char *schemas;
    const char *events, *out, *exi, *events_out;
    int strict;
    const char *alignment;
    long block_size;
    long warmup, warmup_ms, iterations;
} Args;

/* Warm-up goes on until it has run at least --warmup times and for at least
 * --warmup-ms milliseconds since `start`. */
static int warming_up(const Args *a, uint64_t done, uint64_t start) {
    return done < (uint64_t)a->warmup || now_ns() - start < (uint64_t)a->warmup_ms * 1000000ull;
}

static void set_options(EXIOptions *opts, const Args *a) {
    makeDefaultOpts(opts);
    if (a->strict)
        SET_STRICT(opts->enumOpt);
    if (strcmp(a->alignment, "byte-aligned") == 0) {
        SET_ALIGNMENT(opts->enumOpt, BYTE_ALIGNMENT);
    } else if (strcmp(a->alignment, "pre-compression") == 0) {
        SET_ALIGNMENT(opts->enumOpt, PRE_COMPRESSION);
    } else if (strcmp(a->alignment, "compression") == 0) {
        SET_COMPRESSION(opts->enumOpt);
    } else if (strcmp(a->alignment, "bit-packed") != 0) {
        die("unknown alignment %s", a->alignment);
    }
    if (a->block_size > 0)
        opts->blockSize = (uint32_t)a->block_size;
}

static void load_schema(char *list, EXIPSchema *schema) {
    BinaryBuffer buffers[MAX_SCHEMAS];
    Bytes files[MAX_SCHEMAS];
    unsigned int count = 0;
    for (char *tok = strtok(list, ","); tok; tok = strtok(NULL, ",")) {
        if (count == MAX_SCHEMAS)
            die("%s", "too many schema files");
        files[count] = read_file(tok);
        buffers[count].buf = files[count].data;
        buffers[count].bufLen = files[count].len;
        buffers[count].bufContent = files[count].len;
        buffers[count].ioStrm.readWriteToStream = NULL;
        buffers[count].ioStrm.stream = NULL;
        count++;
    }
    if (count == 0)
        die("%s", "--schema is required");
    errorCode err =
        generateSchemaInformedGrammars(buffers, count, SCHEMA_FORMAT_XSD_EXI, NULL, schema, NULL);
    for (unsigned int i = 0; i < count; i++)
        free(files[i].data);
    if (err != EXIP_OK)
        die_code("schema loading", err);
}

/* ---- the .events format ------------------------------------------------ */

typedef enum { EV_SD, EV_ED, EV_SE, EV_EE, EV_AT, EV_CH, EV_XSI_TYPE, EV_XSI_NIL } EvKind;

typedef struct {
    EvKind kind;
    String uri, local; /* SE, AT, XSI_TYPE */
    String value;      /* AT, CH (unescaped); XSI_NIL "true"/"false" */
    char *cvalue;      /* NUL-terminated copy of value, for lexical parsing */
} Ev;

typedef struct {
    Ev *items;
    size_t len, cap;
} Events;

static String mkstr(char *s, size_t n) {
    String out;
    out.str = s;
    out.length = n;
    return out;
}

/* "{uri}local" at p; returns the rest of the line after the name. */
static char *parse_qname(char *p, Ev *ev) {
    if (*p != '{')
        die("expected {uri}local at: %s", p);
    char *close = strchr(p, '}');
    if (!close)
        die("unterminated {uri} at: %s", p);
    ev->uri = mkstr(p + 1, (size_t)(close - p - 1));
    char *local = close + 1;
    char *end = local + strcspn(local, " ");
    ev->local = mkstr(local, (size_t)(end - local));
    return *end ? end + 1 : end;
}

/* Undo the dump escaping (\\ \n \r \t) in place. */
static size_t unescape(char *s) {
    char *w = s;
    for (char *r = s; *r; r++) {
        if (*r == '\\' && r[1]) {
            r++;
            *w++ = *r == 'n' ? '\n' : *r == 'r' ? '\r' : *r == 't' ? '\t' : *r;
        } else {
            *w++ = *r;
        }
    }
    *w = '\0';
    return (size_t)(w - s);
}

static void set_value(Ev *ev, char *text) {
    size_t n = unescape(text);
    ev->value = mkstr(text, n);
    ev->cvalue = text;
}

static Events parse_events(char *text) {
    Events evs = {0};
    char *save = NULL;
    for (char *line = strtok_r(text, "\n", &save); line; line = strtok_r(NULL, "\n", &save)) {
        Ev ev;
        memset(&ev, 0, sizeof ev);
        if (strcmp(line, "SD") == 0) {
            ev.kind = EV_SD;
        } else if (strcmp(line, "ED") == 0) {
            ev.kind = EV_ED;
        } else if (strcmp(line, "EE") == 0) {
            ev.kind = EV_EE;
        } else if (strncmp(line, "SE ", 3) == 0) {
            ev.kind = EV_SE;
            parse_qname(line + 3, &ev);
            ev.local.str[ev.local.length] = '\0';
        } else if (strncmp(line, "AT ", 3) == 0) {
            ev.kind = EV_AT;
            char *rest = parse_qname(line + 3, &ev);
            ev.local.str[ev.local.length] = '\0';
            set_value(&ev, rest);
        } else if (strncmp(line, "CH ", 3) == 0 || strcmp(line, "CH") == 0) {
            ev.kind = EV_CH;
            set_value(&ev, line[2] ? line + 3 : line + 2);
        } else if (strncmp(line, "XSI_TYPE ", 9) == 0) {
            ev.kind = EV_XSI_TYPE;
            parse_qname(line + 9, &ev);
        } else if (strncmp(line, "XSI_NIL ", 8) == 0) {
            ev.kind = EV_XSI_NIL;
            set_value(&ev, line + 8);
        } else {
            die("unknown event line: %s", line);
        }
        if (evs.len == evs.cap) {
            evs.cap = evs.cap ? evs.cap * 2 : 64;
            evs.items = realloc(evs.items, evs.cap * sizeof(Ev));
            if (!evs.items)
                die("%s", "out of memory");
        }
        evs.items[evs.len++] = ev;
    }
    return evs;
}

/* ---- lexical value -> exip value --------------------------------------- */

/* Decimal or scientific lexical form -> mantissa/exponent, trailing zeros of
 * the mantissa folded into the exponent (EXIficient's canonical form). */
static int parse_float(const char *s, Float *out) {
    while (*s == ' ' || *s == '\t' || *s == '\n' || *s == '\r')
        s++;
    if (strncmp(s, "INF", 3) == 0) {
        out->mantissa = 1;
        out->exponent = -16384;
        return 1;
    }
    if (strncmp(s, "-INF", 4) == 0) {
        out->mantissa = -1;
        out->exponent = -16384;
        return 1;
    }
    if (strncmp(s, "NaN", 3) == 0) {
        out->mantissa = 0;
        out->exponent = -16384;
        return 1;
    }
    int neg = 0;
    if (*s == '-' || *s == '+')
        neg = *s++ == '-';
    int64_t mant = 0;
    long exp = 0;
    int digits = 0;
    for (; *s >= '0' && *s <= '9'; s++, digits++)
        mant = mant * 10 + (*s - '0');
    if (*s == '.') {
        for (s++; *s >= '0' && *s <= '9'; s++, digits++) {
            mant = mant * 10 + (*s - '0');
            exp--;
        }
    }
    if (*s == 'e' || *s == 'E') {
        char *end;
        exp += strtol(s + 1, &end, 10);
        s = end;
    }
    while (*s == ' ' || *s == '\t' || *s == '\n' || *s == '\r')
        s++;
    if (digits == 0 || *s)
        return 0;
    while (mant != 0 && mant % 10 == 0) {
        mant /= 10;
        exp++;
    }
    if (mant == 0)
        exp = 0;
    out->mantissa = neg ? -mant : mant;
    out->exponent = (int16_t)exp;
    return 1;
}

/* [-]YYYY-MM-DD, hh:mm:ss[.fff], or both with 'T'; optional Z / +hh:mm. */
static int parse_datetime(const char *s, EXIPDateTime *dt) {
    memset(dt, 0, sizeof *dt);
    dt->dateTime.tm_year = dt->dateTime.tm_mon = dt->dateTime.tm_mday = INT_MIN;
    dt->dateTime.tm_hour = dt->dateTime.tm_min = dt->dateTime.tm_sec = INT_MIN;
    const char *p = s;
    int y, mo, d, h, mi, sec, n = 0;
    if (sscanf(p, "%d-%d-%d%n", &y, &mo, &d, &n) == 3 && n >= 10) {
        dt->dateTime.tm_year = y - 1900;
        dt->dateTime.tm_mon = mo - 1;
        dt->dateTime.tm_mday = d;
        p += n;
        if (*p == 'T')
            p++;
        else
            goto zone;
    }
    if (sscanf(p, "%d:%d:%d%n", &h, &mi, &sec, &n) != 3)
        return 0;
    dt->dateTime.tm_hour = h;
    dt->dateTime.tm_min = mi;
    dt->dateTime.tm_sec = sec;
    p += n;
    if (*p == '.') {
        unsigned int value = 0;
        int count = 0;
        for (p++; *p >= '0' && *p <= '9'; p++, count++)
            value = value * 10 + (unsigned)(*p - '0');
        while (count > 0 && value % 10 == 0) {
            value /= 10;
            count--;
        }
        if (count > 0) {
            dt->fSecs.value = value;
            dt->fSecs.offset = (unsigned char)(count - 1);
            dt->presenceMask |= FRACT_PRESENCE;
        }
    }
zone:
    if (*p == 'Z') {
        dt->TimeZone = 0;
        dt->presenceMask |= TZONE_PRESENCE;
        p++;
    } else if (*p == '+' || *p == '-') {
        int th, tm;
        if (sscanf(p + 1, "%d:%d%n", &th, &tm, &n) != 2)
            return 0;
        int sign = *p == '-' ? -1 : 1;
        dt->TimeZone = (int16_t)(sign * (th * 64 + tm));
        dt->presenceMask |= TZONE_PRESENCE;
        p += 1 + n;
    }
    return *p == '\0';
}

static int is_true(const char *s) { return strcmp(s, "true") == 0 || strcmp(s, "1") == 0; }

static int is_blank(const String *v) {
    for (Index i = 0; i < v->length; i++) {
        char c = v->str[i];
        if (c != ' ' && c != '\t' && c != '\n' && c != '\r')
            return 0;
    }
    return 1;
}

/* Binary values: hexBinary where exip reports VALUE_TYPE_HEX_BINARY (when
 * the tree has EXIP_SERIALIZER_EXI_TYPE / EXIP_PARSER_VALUE_EXI_TYPE), base64Binary
 * otherwise. Older exip trees cannot tell them apart and get base64 throughout. */
static const char B64[] = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/";

static int b64_digit(char c) {
    const char *p = c ? strchr(B64, c) : NULL;
    return p ? (int)(p - B64) : -1;
}

static int b64_decode(const char *s, Bytes *out) {
    unsigned int acc = 0;
    int bits = 0;
    for (; *s; s++) {
        int d;
        if (*s == '=' || *s == ' ' || *s == '\n' || *s == '\r' || *s == '\t')
            continue;
        if ((d = b64_digit(*s)) < 0)
            return 0;
        acc = (acc << 6) | (unsigned)d;
        bits += 6;
        if (bits >= 8) {
            char byte = (char)((acc >> (bits - 8)) & 0xff);
            bytes_append(out, &byte, 1);
            bits -= 8;
        }
    }
    return 1;
}

static void b64_encode(const char *v, size_t n, Bytes *out) {
    for (size_t i = 0; i < n; i += 3) {
        unsigned int w = (unsigned char)v[i] << 16;
        if (i + 1 < n)
            w |= (unsigned char)v[i + 1] << 8;
        if (i + 2 < n)
            w |= (unsigned char)v[i + 2];
        char q[4] = {B64[(w >> 18) & 63], B64[(w >> 12) & 63], i + 1 < n ? B64[(w >> 6) & 63] : '=',
                     i + 2 < n ? B64[w & 63] : '='};
        bytes_append(out, q, 4);
    }
}

static errorCode put_value(EXIStream *strm, EXITypeClass type, Ev *ev);

#ifdef EXIP_SERIALIZER_EXI_TYPE
static int hex_digit(char c) {
    if (c >= '0' && c <= '9')
        return c - '0';
    if (c >= 'a' && c <= 'f')
        return c - 'a' + 10;
    if (c >= 'A' && c <= 'F')
        return c - 'A' + 10;
    return -1;
}

static int hex_decode(const char *s, Bytes *out) {
    while (*s == ' ' || *s == '\t' || *s == '\n' || *s == '\r')
        s++;
    size_t n = strlen(s);
    while (n > 0 && (s[n - 1] == ' ' || s[n - 1] == '\t' || s[n - 1] == '\n' || s[n - 1] == '\r'))
        n--;
    if (n % 2)
        return 0;
    for (size_t i = 0; i < n; i += 2) {
        int hi = hex_digit(s[i]), lo = hex_digit(s[i + 1]);
        if (hi < 0 || lo < 0)
            return 0;
        char byte = (char)(hi * 16 + lo);
        bytes_append(out, &byte, 1);
    }
    return 1;
}
#endif

#ifdef EXIP_PARSER_VALUE_EXI_TYPE
static void hex_encode(const char *v, size_t n, Bytes *out) {
    static const char hex[] = "0123456789ABCDEF";
    for (size_t i = 0; i < n; i++) {
        char pair[2] = {hex[(unsigned char)v[i] >> 4], hex[(unsigned char)v[i] & 15]};
        bytes_append(out, pair, 2);
    }
}
#endif

static int is_xml_space(char c) { return c == ' ' || c == '\t' || c == '\n' || c == '\r'; }

/* An xs:list value: its whitespace-separated items, each with the item type exip
 * reports after listData (string when the exip tree cannot report it). */
static errorCode put_list(EXIStream *strm, Ev *ev) {
    size_t n = strlen(ev->cvalue), i = 0, count = 0;
    errorCode err;

    for (i = 0; i < n;) {
        while (i < n && is_xml_space(ev->cvalue[i]))
            i++;
        if (i < n)
            count++;
        while (i < n && !is_xml_space(ev->cvalue[i]))
            i++;
    }
    if ((err = serialize.listData(strm, (unsigned int)count)) != EXIP_OK)
        return err;
    for (i = 0; i < n;) {
        size_t start;
        while (i < n && is_xml_space(ev->cvalue[i]))
            i++;
        if (i == n)
            break;
        start = i;
        while (i < n && !is_xml_space(ev->cvalue[i]))
            i++;
        {
            Ev item = *ev;
            EXITypeClass type = VALUE_TYPE_STRING_CLASS;
            char *text = malloc(i - start + 1);
            if (!text)
                die("%s", "out of memory");
            memcpy(text, ev->cvalue + start, i - start);
            text[i - start] = '\0';
            item.cvalue = text;
            item.value = mkstr(text, i - start);
#ifdef EXIP_SERIALIZER_VALUE_TYPE
            if ((err = serialize.valueType(strm, &type)) != EXIP_OK) {
                free(text);
                return err;
            }
#endif
            err = put_value(strm, type, &item);
            free(text);
            if (err != EXIP_OK)
                return err;
        }
    }
    return EXIP_OK;
}

static errorCode put_value(EXIStream *strm, EXITypeClass type, Ev *ev) {
    switch (type) {
    case VALUE_TYPE_INTEGER_CLASS: {
        char *end;
        errno = 0;
        long long v = strtoll(ev->cvalue, &end, 10);
        if (errno || *end)
            die("not an integer: %s", ev->cvalue);
        return serialize.intData(strm, (Integer)v);
    }
    case VALUE_TYPE_FLOAT_CLASS: {
        Float f;
        if (!parse_float(ev->cvalue, &f))
            die("not a float: %s", ev->cvalue);
        return serialize.floatData(strm, f);
    }
    case VALUE_TYPE_DECIMAL_CLASS: {
        Decimal d;
        if (!parse_float(ev->cvalue, &d))
            die("not a decimal: %s", ev->cvalue);
        return serialize.decimalData(strm, d);
    }
    case VALUE_TYPE_BOOLEAN_CLASS:
        return serialize.booleanData(strm, is_true(ev->cvalue) ? TRUE : FALSE);
    case VALUE_TYPE_DATE_TIME_CLASS: {
        EXIPDateTime dt;
        if (!parse_datetime(ev->cvalue, &dt))
            die("unsupported date/time: %s", ev->cvalue);
        return serialize.dateTimeData(strm, dt);
    }
    case VALUE_TYPE_BINARY_CLASS: {
        Bytes b = {0};
#ifdef EXIP_SERIALIZER_EXI_TYPE
        int hex = 0;
        EXIType exact;
        if (serialize.exiType(strm, &exact) == EXIP_OK && exact == VALUE_TYPE_HEX_BINARY)
            hex = 1;
        if (hex) {
            if (!hex_decode(ev->cvalue, &b))
                die("not hexBinary: %s", ev->cvalue);
        } else
#endif
        if (!b64_decode(ev->cvalue, &b))
            die("not base64Binary: %s", ev->cvalue);
        errorCode e = serialize.binaryData(strm, b.data ? b.data : "", (Index)b.len);
        free(b.data);
        return e;
    }
    case VALUE_TYPE_LIST_CLASS:
        return put_list(strm, ev);
    default: /* string, untyped, enumerations (exip maps them via stringData) */
        return serialize.stringData(strm, ev->value);
    }
}

/* ---- encode ------------------------------------------------------------ */

static size_t sink_write(void *buf, size_t size, void *stream) {
    bytes_append((Bytes *)stream, buf, size);
    return size;
}

static String XSI_NS = {"http://www.w3.org/2001/XMLSchema-instance", 41};
static String XSI_TYPE = {"type", 4};
static String XSI_NIL = {"nil", 3};

static void encode_once(EXIPSchema *schema, const Args *a, Events *evs, Bytes *out) {
    EXIStream strm;
    char chunk[CHUNK];
    BinaryBuffer buffer;
    /* Value type of each open element: exip reports it at startElement. */
    EXITypeClass types[256];
    int depth = 0;
    errorCode err;

    out->len = 0;
    /* exip's closeEXIStream flushes one byte past the content when the stream
     * ends on a byte boundary; zero the buffer so that byte is deterministic. */
    memset(chunk, 0, sizeof chunk);
    buffer.buf = chunk;
    buffer.bufLen = sizeof chunk;
    buffer.bufContent = 0;
    buffer.ioStrm.readWriteToStream = sink_write;
    buffer.ioStrm.stream = out;

    serialize.initHeader(&strm);
    strm.header.has_cookie = FALSE;
    strm.header.has_options = FALSE;
    set_options(&strm.header.opts, a);
    if ((err = serialize.initStream(&strm, buffer, schema)) != EXIP_OK)
        die_code("initStream", err);
    if ((err = serialize.exiHeader(&strm)) != EXIP_OK)
        die_code("exiHeader", err);

    for (size_t i = 0; i < evs->len; i++) {
        Ev *ev = &evs->items[i];
        QName q = {&ev->uri, &ev->local, NULL};
        EXITypeClass at_type;
        switch (ev->kind) {
        case EV_SD:
            err = serialize.startDocument(&strm);
            break;
        case EV_ED:
            err = serialize.endDocument(&strm);
            break;
        case EV_SE:
            if (depth == (int)(sizeof types / sizeof types[0]))
                die("%s", "elements nested too deeply");
            err = serialize.startElement(&strm, q, &types[depth++]);
            break;
        case EV_EE:
            if (depth > 0)
                depth--;
            err = serialize.endElement(&strm);
            break;
        case EV_AT:
            err = serialize.attribute(&strm, q, TRUE, &at_type);
            if (err == EXIP_OK)
                err = put_value(&strm, at_type, ev);
            break;
        case EV_CH: {
            EXITypeClass ch_type = depth > 0 ? types[depth - 1] : VALUE_TYPE_NONE_CLASS;
#ifdef EXIP_SERIALIZER_VALUE_TYPE
            /* The type in the current state: simple content after attributes, or
             * the type an xsi:type cast selected */
            if ((err = serialize.valueType(&strm, &ch_type)) != EXIP_OK)
                break;
#endif
            /* Whitespace between child elements is not content (an XML front end
             * drops it); exip rejects CH where the element has no simple content. */
            if (ch_type == VALUE_TYPE_NONE_CLASS && is_blank(&ev->value)) {
                err = EXIP_OK;
                break;
            }
            err = put_value(&strm, ch_type, ev);
            break;
        }
        case EV_XSI_TYPE: {
            QName xsi = {&XSI_NS, &XSI_TYPE, NULL};
            err = serialize.attribute(&strm, xsi, TRUE, &at_type);
            if (err == EXIP_OK)
                err = serialize.qnameData(&strm, q);
            break;
        }
        case EV_XSI_NIL: {
            QName xsi = {&XSI_NS, &XSI_NIL, NULL};
            err = serialize.attribute(&strm, xsi, TRUE, &at_type);
            if (err == EXIP_OK)
                err = serialize.booleanData(&strm, is_true(ev->cvalue) ? TRUE : FALSE);
            break;
        }
        default:
            err = EXIP_UNEXPECTED_ERROR;
        }
        if (err != EXIP_OK) {
            fprintf(stderr, "exip-driver: event %zu ", i + 1);
            die_code("encoding", err);
        }
    }
    if ((err = serialize.closeEXIStream(&strm)) != EXIP_OK)
        die_code("closeEXIStream", err);
}

/* ---- decode ------------------------------------------------------------ */

typedef struct {
    size_t count;
    Bytes *dump;   /* NULL: count only */
    int pending;   /* an AT whose value has not arrived yet */
    Bytes at_name; /* "{uri}local" of the pending attribute */
    int at_is_type, at_is_nil;
    Parser *parser;         /* for parse.valueExiType inside handlers */
    unsigned int list_left; /* items of an xs:list value still to come */
    int list_items;         /* items of it collected so far */
    Bytes list_text;        /* the items, separated by single spaces */
} Sink;

static void dump_qname(Bytes *b, const char *tag, QName q) {
    bytes_puts(b, tag);
    bytes_puts(b, " {");
    bytes_append(b, q.uri->str, q.uri->length);
    bytes_puts(b, "}");
    bytes_append(b, q.localName->str, q.localName->length);
}

static void dump_escaped(Bytes *b, const char *s, size_t n) {
    for (size_t i = 0; i < n; i++) {
        switch (s[i]) {
        case '\\':
            bytes_puts(b, "\\\\");
            break;
        case '\n':
            bytes_puts(b, "\\n");
            break;
        case '\r':
            bytes_puts(b, "\\r");
            break;
        case '\t':
            bytes_puts(b, "\\t");
            break;
        default:
            bytes_append(b, &s[i], 1);
        }
    }
}

/* A value event: AT or CH, depending on whether an attribute is pending. */
static errorCode value_text(Sink *s, const char *text, size_t n) {
    if (s->list_left > 0) {
        /* an xs:list item: collect until the last one, then report the whole value */
        if (s->list_items++ > 0)
            bytes_puts(&s->list_text, " ");
        bytes_append(&s->list_text, text, n);
        if (--s->list_left > 0)
            return EXIP_OK;
        text = s->list_text.data ? s->list_text.data : "";
        n = s->list_text.len;
    }
    s->count++;
    if (!s->dump) {
        s->pending = 0;
        return EXIP_OK;
    }
    if (s->pending) {
        if (s->at_is_nil) {
            bytes_puts(s->dump, "XSI_NIL ");
            bytes_append(s->dump, text, n);
        } else {
            bytes_puts(s->dump, "AT ");
            bytes_append(s->dump, s->at_name.data, s->at_name.len);
            bytes_puts(s->dump, " ");
            dump_escaped(s->dump, text, n);
        }
        s->pending = 0;
    } else {
        bytes_puts(s->dump, "CH ");
        dump_escaped(s->dump, text, n);
    }
    bytes_puts(s->dump, "\n");
    return EXIP_OK;
}

static errorCode h_list(EXITypeClass exiType, unsigned int itemCount, void *app) {
    Sink *s = app;
    (void)exiType;
    s->list_text.len = 0;
    s->list_items = 0;
    s->list_left = itemCount;
    if (itemCount == 0)
        return value_text(s, "", 0); /* an empty list is an empty value */
    return EXIP_OK;
}

static errorCode h_fatal(const errorCode code, const char *msg, void *app) {
    (void)app;
    fprintf(stderr, "exip-driver: parser error %d: %s\n", (int)code, msg);
    return EXIP_HANDLER_STOP;
}

static errorCode h_sd(void *app) {
    Sink *s = app;
    s->count++;
    if (s->dump)
        bytes_puts(s->dump, "SD\n");
    return EXIP_OK;
}

static errorCode h_ed(void *app) {
    Sink *s = app;
    s->count++;
    if (s->dump)
        bytes_puts(s->dump, "ED\n");
    return EXIP_OK;
}

static errorCode h_se(QName q, void *app) {
    Sink *s = app;
    s->count++;
    if (s->dump) {
        dump_qname(s->dump, "SE", q);
        bytes_puts(s->dump, "\n");
    }
    return EXIP_OK;
}

static errorCode h_ee(void *app) {
    Sink *s = app;
    s->count++;
    if (s->dump)
        bytes_puts(s->dump, "EE\n");
    return EXIP_OK;
}

static errorCode h_at(QName q, void *app) {
    Sink *s = app;
    s->pending = 1;
    if (!s->dump)
        return EXIP_OK;
    int xsi = q.uri->length == XSI_NS.length && memcmp(q.uri->str, XSI_NS.str, XSI_NS.length) == 0;
    s->at_is_type = xsi && q.localName->length == 4 && memcmp(q.localName->str, "type", 4) == 0;
    s->at_is_nil = xsi && q.localName->length == 3 && memcmp(q.localName->str, "nil", 3) == 0;
    s->at_name.len = 0;
    bytes_puts(&s->at_name, "{");
    bytes_append(&s->at_name, q.uri->str, q.uri->length);
    bytes_puts(&s->at_name, "}");
    bytes_append(&s->at_name, q.localName->str, q.localName->length);
    return EXIP_OK;
}

static errorCode h_string(const String v, void *app) { return value_text(app, v.str, v.length); }

static errorCode h_int(Integer v, void *app) {
    char buf[32];
    int n = snprintf(buf, sizeof buf, "%lld", (long long)v);
    return value_text(app, buf, (size_t)n);
}

static errorCode h_bool(boolean v, void *app) {
    return value_text(app, v ? "true" : "false", v ? 4 : 5);
}

static errorCode h_float(Float v, void *app) {
    char buf[48];
    int n;
    if (v.exponent == -16384)
        n = snprintf(buf, sizeof buf, "%s",
                     v.mantissa == 1    ? "INF"
                     : v.mantissa == -1 ? "-INF"
                                        : "NaN");
    else
        n = snprintf(buf, sizeof buf, "%lldE%d", (long long)v.mantissa, (int)v.exponent);
    return value_text(app, buf, (size_t)n);
}

/* Decimal (mantissa * 10^exponent) as a plain decimal string. */
static errorCode h_decimal(Decimal v, void *app) {
    char digits[32], buf[64];
    long long m = (long long)v.mantissa;
    int neg = m < 0;
    snprintf(digits, sizeof digits, "%lld", neg ? -m : m);
    int len = (int)strlen(digits), exp = v.exponent, n;
    if (exp >= 0) {
        n = snprintf(buf, sizeof buf, "%s%s%0*d", neg ? "-" : "", digits, exp, 0);
        if (exp == 0)
            n = snprintf(buf, sizeof buf, "%s%s", neg ? "-" : "", digits);
    } else if (-exp < len) {
        n = snprintf(buf, sizeof buf, "%s%.*s.%s", neg ? "-" : "", len + exp, digits,
                     digits + len + exp);
    } else {
        n = snprintf(buf, sizeof buf, "%s0.%0*d%s", neg ? "-" : "", -exp - len, 0, digits);
        if (-exp == len)
            n = snprintf(buf, sizeof buf, "%s0.%s", neg ? "-" : "", digits);
    }
    return value_text(app, buf, (size_t)n);
}

static errorCode h_datetime(EXIPDateTime v, void *app) {
    char buf[64];
    int n = 0;
    struct tm *t = &v.dateTime;
    if (t->tm_year != INT_MIN)
        n += snprintf(buf + n, sizeof buf - n, "%04d-%02d-%02d", t->tm_year + 1900, t->tm_mon + 1,
                      t->tm_mday);
    if (t->tm_hour != INT_MIN) {
        if (n)
            buf[n++] = 'T';
        n += snprintf(buf + n, sizeof buf - n, "%02d:%02d:%02d", t->tm_hour, t->tm_min, t->tm_sec);
        if (IS_PRESENT(v.presenceMask, FRACT_PRESENCE))
            n += snprintf(buf + n, sizeof buf - n, ".%0*u", v.fSecs.offset + 1, v.fSecs.value);
    }
    if (IS_PRESENT(v.presenceMask, TZONE_PRESENCE)) {
        if (v.TimeZone == 0) {
            n += snprintf(buf + n, sizeof buf - n, "Z");
        } else {
            int tz = v.TimeZone < 0 ? -v.TimeZone : v.TimeZone;
            n += snprintf(buf + n, sizeof buf - n, "%c%02d:%02d", v.TimeZone < 0 ? '-' : '+',
                          tz / 64, tz % 64);
        }
    }
    return value_text(app, buf, (size_t)n);
}

static errorCode h_binary(const char *v, Index nbytes, void *app) {
    Bytes b = {0};
#ifdef EXIP_PARSER_VALUE_EXI_TYPE
    Sink *s = app;
    EXIType exact;
    if (s->parser && parse.valueExiType(s->parser, &exact) == EXIP_OK && exact == VALUE_TYPE_HEX_BINARY)
        hex_encode(v, nbytes, &b);
    else
#endif
        b64_encode(v, nbytes, &b);
    errorCode e = value_text(app, b.data ? b.data : "", b.len);
    free(b.data);
    return e;
}

static errorCode h_qname(const QName q, void *app) {
    Sink *s = app;
    s->count++;
    if (s->dump) {
        if (s->pending && s->at_is_type) {
            dump_qname(s->dump, "XSI_TYPE", q);
        } else {
            bytes_puts(s->dump, s->pending ? "AT " : "CH ");
            if (s->pending) {
                bytes_append(s->dump, s->at_name.data, s->at_name.len);
                bytes_puts(s->dump, " ");
            }
            bytes_puts(s->dump, "{");
            bytes_append(s->dump, q.uri->str, q.uri->length);
            bytes_puts(s->dump, "}");
            bytes_append(s->dump, q.localName->str, q.localName->length);
        }
        bytes_puts(s->dump, "\n");
    }
    s->pending = 0;
    return EXIP_OK;
}

static void decode_once(EXIPSchema *schema, const Args *a, Bytes *exi, Sink *sink) {
    Parser parser;
    BinaryBuffer buffer;
    EXIOptions opts;
    errorCode err;

    buffer.buf = exi->data;
    buffer.bufLen = exi->len;
    buffer.bufContent = exi->len;
    buffer.ioStrm.readWriteToStream = NULL;
    buffer.ioStrm.stream = NULL;

    if ((err = parse.initParser(&parser, buffer, sink)) != EXIP_OK)
        die_code("initParser", err);
    sink->parser = &parser;
    set_options(&opts, a);
    parser.strm.header.opts = opts;
    parser.handler.fatalError = h_fatal;
    parser.handler.error = h_fatal;
    parser.handler.startDocument = h_sd;
    parser.handler.endDocument = h_ed;
    parser.handler.startElement = h_se;
    parser.handler.endElement = h_ee;
    parser.handler.attribute = h_at;
    parser.handler.stringData = h_string;
    parser.handler.intData = h_int;
    parser.handler.booleanData = h_bool;
    parser.handler.floatData = h_float;
    parser.handler.decimalData = h_decimal;
    parser.handler.dateTimeData = h_datetime;
    parser.handler.binaryData = h_binary;
    parser.handler.qnameData = h_qname;
    parser.handler.listData = h_list;

    if ((err = parse.parseHeader(&parser, TRUE)) != EXIP_OK)
        die_code("parseHeader", err);
    if ((err = parse.setSchema(&parser, schema)) != EXIP_OK)
        die_code("setSchema", err);
    while ((err = parse.parseNext(&parser)) == EXIP_OK) {
    }
    parse.destroyParser(&parser);
    if (err != EXIP_PARSING_COMPLETE)
        die_code("parsing", err);
}

/* ---- main -------------------------------------------------------------- */

static void print_json(const char *op, const char *input, size_t in_bytes, size_t out,
                       uint64_t setup_ns, const Args *a, const uint64_t *ns) {
    printf("{\"impl\":\"exip\",\"op\":\"%s\",\"input\":\"%s\",\"input_bytes\":%zu,"
           "\"output\":%zu,\"setup_ns\":%llu,\"warmup\":%ld,\"iterations\":%ld,"
           "\"ns\":[",
           op, input, in_bytes, out, (unsigned long long)setup_ns, a->warmup, a->iterations);
    for (long i = 0; i < a->iterations; i++)
        printf("%s%llu", i ? "," : "", (unsigned long long)ns[i]);
    printf("]}\n");
}

static long parse_count(const char *flag, const char *v) {
    char *end;
    long n = strtol(v, &end, 10);
    if (*end || n < 0)
        die("bad value for %s", flag);
    return n;
}

int main(int argc, char **argv) {
    Args a = {0};
    a.alignment = "bit-packed";
    a.warmup = 20;
    a.iterations = 200;
    if (argc < 2)
        die("%s", "usage: exip-driver encode|decode --schema ... (see source header)");
    a.mode = argv[1];
    for (int i = 2; i < argc; i++) {
        const char *f = argv[i];
#define VALUE() (i + 1 < argc ? argv[++i] : (die("%s needs a value", f), ""))
        if (strcmp(f, "--schema") == 0)
            a.schemas = strdup(VALUE());
        else if (strcmp(f, "--events") == 0)
            a.events = VALUE();
        else if (strcmp(f, "--out") == 0)
            a.out = VALUE();
        else if (strcmp(f, "--exi") == 0)
            a.exi = VALUE();
        else if (strcmp(f, "--events-out") == 0)
            a.events_out = VALUE();
        else if (strcmp(f, "--strict") == 0)
            a.strict = 1;
        else if (strcmp(f, "--alignment") == 0)
            a.alignment = VALUE();
        else if (strcmp(f, "--block-size") == 0)
            a.block_size = parse_count(f, VALUE());
        else if (strcmp(f, "--warmup") == 0)
            a.warmup = parse_count(f, VALUE());
        else if (strcmp(f, "--warmup-ms") == 0)
            a.warmup_ms = parse_count(f, VALUE());
        else if (strcmp(f, "--iterations") == 0)
            a.iterations = parse_count(f, VALUE());
        else
            die("unknown option %s", f);
#undef VALUE
    }
    if (!a.schemas)
        die("%s", "--schema is required");

    EXIPSchema schema;
    uint64_t t0 = now_ns();
    load_schema(a.schemas, &schema);
    uint64_t setup_ns = now_ns() - t0;
    uint64_t *ns = calloc((size_t)a.iterations + 1, sizeof *ns);

    if (strcmp(a.mode, "encode") == 0) {
        if (!a.events)
            die("%s", "encode needs --events");
        Bytes text = read_file(a.events);
        size_t text_len = text.len;
        Events evs = parse_events(text.data);
        Bytes out = {0};
        encode_once(&schema, &a, &evs, &out);
        if (a.out)
            write_file(a.out, out.data, out.len);
        size_t out_len = out.len;
        for (uint64_t start = now_ns(), i = 0; warming_up(&a, i, start); i++)
            encode_once(&schema, &a, &evs, &out);
        for (long i = 0; i < a.iterations; i++) {
            uint64_t s = now_ns();
            encode_once(&schema, &a, &evs, &out);
            ns[i] = now_ns() - s;
        }
        print_json("encode", "events", text_len, out_len, setup_ns, &a, ns);
        free(out.data);
        free(evs.items);
        free(text.data);
    } else if (strcmp(a.mode, "decode") == 0) {
        if (!a.exi)
            die("%s", "decode needs --exi");
        Bytes exi = read_file(a.exi);
        Bytes dump = {0};
        Sink sink;
        memset(&sink, 0, sizeof sink);
        sink.dump = &dump;
        decode_once(&schema, &a, &exi, &sink);
        if (a.events_out)
            write_file(a.events_out, dump.data ? dump.data : "", dump.len);
        size_t events = sink.count;
        free(sink.at_name.data);
        free(sink.list_text.data);
        for (uint64_t start = now_ns(), i = 0; warming_up(&a, i, start); i++) {
            Sink counter;
            memset(&counter, 0, sizeof counter);
            decode_once(&schema, &a, &exi, &counter);
            free(counter.at_name.data);
            free(counter.list_text.data);
        }
        for (long i = 0; i < a.iterations; i++) {
            Sink counter;
            memset(&counter, 0, sizeof counter);
            uint64_t s = now_ns();
            decode_once(&schema, &a, &exi, &counter);
            ns[i] = now_ns() - s;
            free(counter.at_name.data);
            free(counter.list_text.data);
        }
        print_json("decode", "exi", exi.len, events, setup_ns, &a, ns);
        free(dump.data);
        free(exi.data);
    } else {
        die("unknown mode %s (encode|decode)", a.mode);
    }
    free(ns);
    free(a.schemas);
    destroySchema(&schema);
    return 0;
}
