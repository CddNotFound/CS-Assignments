#include <common.h>
#include <proc.h>

#if defined(MULTIPROGRAM) && !defined(TIME_SHARING)
# define MULTIPROGRAM_YIELD() yield()
#else
# define MULTIPROGRAM_YIELD()
#endif

#define NAME(key) \
  [AM_KEY_##key] = #key,

static const char *keyname[256] __attribute__((used)) = {
  [AM_KEY_NONE] = "NONE",
  AM_KEYS(NAME)
};

size_t serial_write(const void *buf, size_t offset, size_t len) {
  char *data = (char *)buf;
  for (int i = 0; i < len; i++) {
    putch(*(data + i));
  }

  return len;
}

size_t events_read(void *buf, size_t offset, size_t len) {
  // int fd = fs_open("dev/event");
  // assert(fd >= 0);

  // Get event;
  AM_INPUT_KEYBRD_T ev = io_read(AM_INPUT_KEYBRD);
  if (ev.keycode == AM_KEY_NONE) {
    return 0;
  }

  char event[100];
  sprintf(event, "%s %s\n", ev.keydown ? "kd" : "ku", keyname[ev.keycode]);

  int ret = 0;
  char *data = (char *)buf;
  for (int i = 0; i < len; i++) {
    *(data + i) = *(event + i);
    ret += 1;
    if (*(event + i) == '\n') {break; }
  }

  return ret;
}

size_t dispinfo_read(void *buf, size_t offset, size_t len) {
  return 0;
}

size_t fb_write(const void *buf, size_t offset, size_t len) {
  return 0;
}

void init_device() {
  Log("Initializing devices...");
  ioe_init();
}
