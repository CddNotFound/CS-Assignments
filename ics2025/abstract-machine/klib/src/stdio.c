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
static char * putInt(char *out, int d) {
  if (!d) {
    *out++ = '0';
    return out;
  }

  if (d < 0) {
    *out++ = '-';
  }
  char buf[16];
  int cnt = 0;
  while (d) {
    buf[++cnt] = (char)(d % 10 + 48);
    d /= 10;
  }

  while (cnt) {
    *out++ = buf[cnt];
    cnt -= 1;
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

    switch (*fmt++) {
      case 's': out = putStr(out, va_arg(ap, char *)); break;
      case 'd': out = putInt(out, va_arg(ap, int)); break;
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
