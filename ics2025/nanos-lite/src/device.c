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
  AM_GPU_CONFIG_T dispInfo = io_read(AM_GPU_CONFIG);
  uint16_t width = dispInfo.width;
  uint16_t height = dispInfo.height;

  char *data = (char *)buf;
  sprintf(data, "WIDTH : %d\n", width);
  sprintf(data + strlen(data), "HEIGHT : %d\n", height);

  return strlen(data);
}

size_t fb_write(const void *buf, size_t offset, size_t len) {
  AM_GPU_CONFIG_T dispInfo = io_read(AM_GPU_CONFIG);
  uint16_t width = dispInfo.width;
  // uint16_t height = dispInfo.height;
  uint32_t pixelOffset = offset / sizeof(uint32_t);

  int x = pixelOffset % width;
  int y = pixelOffset / width;
  int cnt = len / sizeof(uint32_t);
  
  io_write(AM_GPU_FBDRAW, x, y, (void *)buf, cnt, 1, true);

  return len;
}

void init_device() {
  Log("Initializing devices...");
  ioe_init();
}
