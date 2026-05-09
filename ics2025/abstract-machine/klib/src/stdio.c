#include <am.h>
#include <klib.h>
#include <klib-macros.h>
#include <stdarg.h>

#if !defined(__ISA_NATIVE__) || defined(__NATIVE_USE_KLIB__)

int printf(const char *fmt, ...) {
  panic("Not implemented");
}

int vsprintf(char *out, const char *fmt, va_list ap) {
  panic("Not implemented");
}

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

int sprintf(char *out, const char *fmt, ...) {
  va_list ap;

  va_start(ap, fmt);
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
  va_end(ap);

  *out = '\0';

  return 0;
}

int snprintf(char *out, size_t n, const char *fmt, ...) {
  panic("Not implemented");
}

int vsnprintf(char *out, size_t n, const char *fmt, va_list ap) {
  panic("Not implemented");
}

#endif
