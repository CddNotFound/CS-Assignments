#include <klib.h>
#include <klib-macros.h>
#include <stdint.h>

#if !defined(__ISA_NATIVE__) || defined(__NATIVE_USE_KLIB__)

size_t strlen(const char *s) {
  size_t len = 0;
  while (*s) {
    len += 1;
    s++;
  }

  return len;
}

char *strcpy(char *dst, const char *src) {
  char *ret = dst;
  while (*src) {
    *dst++ = *src++;
  }
  *dst = '\0';

  return ret;
}

char *strncpy(char *dst, const char *src, size_t n) {
  char *ret = dst;
  while (*src) {
    if (n <= 0) { break; }
    n -= 1;
    *dst++ = *src++;
  }
  while (n > 0) {
    *dst++ = '\0';
    n -= 1;
  }

  return ret;
}

char *strcat(char *dst, const char *src) {
  char *ret = dst;
  while (*dst) dst++;
  while (*src) {
    *dst++ = *src++;
  }
  *dst = '\0';
  
  return ret;
}

int strcmp(const char *s1, const char *s2) {
  while (*s1 && *s1 == *s2) {
    s1 += 1, s2 += 1;
  }
  
  if (*s1 && *s2) {
    return *s1 - *s2;
  } else if (*s1) {
    return *s1;
  } else if (*s2) {
    return -*s2;
  } else {
    return 0;
  }
}

int strncmp(const char *s1, const char *s2, size_t n) {
  while (*s1 && *s1 == *s2) {
    if (n <= 0) { break; }
    s1 += 1, s2 += 1;
    n -= 1;
  }

  if (!n) {
    return 0;
  }
  
  if (*s1 && *s2) {
    return *s1 - *s2;
  } else if (*s1) {
    return *s1;
  } else if (*s2) {
    return -*s2;
  } else {
    return 0;
  }
}

void *memset(void *s, int c, size_t n) {
  unsigned char* p = s;
  for (int i = 0; i < n; i++) {
    p[i] = (unsigned char)c;
  }
  return s;
}

void *memmove(void *dst, const void *src, size_t n) {
  unsigned char *d = dst;
  const unsigned char *s = src;
  if (d < s) {
    for (int i = 0; i < n; i++) {
      d[i] = s[i];
    }
  } else {
    for (int i = n - 1; i >= 0; i--) {
      d[i] = s[i];
    }
  }
  return dst;
}

void *memcpy(void *out, const void *in, size_t n) {
  unsigned char *d = out;
  const unsigned char *s = in;
  for (int i = 0; i < n; i++) {
    d[i] = s[i];
  }

  return out;
}

int memcmp(const void *s1, const void *s2, size_t n) {
  const unsigned char *p1 = s1;
  const unsigned char *p2 = s2;
  for (int i = 0; i < n; i++) {
    if (p1[i] != p2[i]) {
      return (int)p1[i] - (int)p2[i];
    }
  }

  return 0;
}

#endif
