#include <am.h>
#include <klib.h>
#include <klib-macros.h>
#include <stdarg.h>

#if !defined(__ISA_NATIVE__) || defined(__NATIVE_USE_KLIB__)

static char * putStr(char *out, const char* s) {
  while (*s) {
    *out++ = *s++;
  }
  return out;
}
static char * putChar(char *out, const char s) {
  *out++ = s;
  return out;
}
static char * putInt(char *out, int len, int d) {
  if (d < 0) {
    *out++ = '-';
  }

  char buf[128];
  int cnt = 0;
  if (!d) {
    cnt = 1;
    buf[1] = '0';
  }
  while (d) {
    buf[++cnt] = (char)(d % 10 + 48);
    d /= 10;
  }

  if (!len) { len = cnt; }

  if (cnt <= len) {
    for (int i = 0; i < len - cnt; i++) {
      *out++ = '0';
    }
  }

  while (cnt) {
    *out++ = buf[cnt];
    cnt -= 1;

    len -= 1;
    if (len <= 0) { break; }
  }

  return out;
}

int vsprintf(char *out, const char *fmt, va_list ap) {
  char *start = out;
  while (*fmt) {
    if (*fmt != '%') {
      *out++ = *fmt++;
      continue;
    }

    fmt++;

    int setLen = 0;
    while (*fmt >= '0' && *fmt <= '9') {
      setLen = setLen * 10 + (*fmt - '0');
      fmt++;
    }

    switch (*fmt++) {
      case 's': out = putStr(out, va_arg(ap, char *)); break;
      case 'd': out = putInt(out, setLen, va_arg(ap, int)); break;
      case 'c': out = putChar(out, (char)va_arg(ap, int)); break;
      case '%': *out++ = '%'; break;
      default: break;
    }
  }

  *out = '\0';

  return out - start;
}

int printf(const char *fmt, ...) {
  char buf[4096];

  va_list ap;

  va_start(ap, fmt);
  int ret = vsprintf(buf, fmt, ap);
  va_end(ap);

  for (char *ch = buf; *ch; ch++) {
    putch(*ch);
  }

  return ret;
}

int sprintf(char *out, const char *fmt, ...) {
  va_list ap;

  va_start(ap, fmt);
  int ret = vsprintf(out, fmt, ap);
  va_end(ap);

  return ret;
}

int snprintf(char *out, size_t n, const char *fmt, ...) {
  panic("Not implemented");
}

int vsnprintf(char *out, size_t n, const char *fmt, va_list ap) {
  panic("Not implemented");
}

#endif
