#include <stdint.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include <sys/time.h>
#include <assert.h>
#include <fcntl.h>

static int evtdev = -1;
static int fbdev = -1;
static int screen_w = 0, screen_h = 0;
static int canvasOffset = 0;

uint32_t NDL_GetTicks() {
  struct timeval tv;
  gettimeofday(&tv, NULL);

  uint32_t ret = tv.tv_sec * 1000000 + tv.tv_usec;

  return ret;
}

int NDL_PollEvent(char *buf, int len) {
  if (evtdev < 0) {
    evtdev = open("/dev/events", 0, 0);
  }
  char event[100];

  int readSiz = read(evtdev, buf, len);

  close(evtdev);

  return readSiz >= 1 ? 1 : 0;
}

void NDL_OpenCanvas(int *w, int *h) {
  int fd = open("/proc/dispinfo", 0, 0);
  assert(fd != -1);
  char data[100];
  read(fd, data, sizeof(data) - 1);
  int width = 0, height = 0;
  sscanf(data, "WIDTH : %d\nHEIGHT : %d\n", &width, &height);

  if (*w == 0) { *w = width; }
  if (*h == 0) { *h = height; }

  if (getenv("NWM_APP")) {
    int fbctl = 4;
    fbdev = 5;
    screen_w = *w; screen_h = *h;
    char buf[64];
    int len = sprintf(buf, "%d %d", screen_w, screen_h);
    // let NWM resize the window and create the frame buffer
    write(fbctl, buf, len);
    while (1) {
      // 3 = evtdev
      int nread = read(3, buf, sizeof(buf) - 1);
      if (nread <= 0) continue;
      buf[nread] = '\0';
      if (strcmp(buf, "mmap ok") == 0) break;
    }
    close(fbctl);
  } else {
    screen_w = width;
    screen_h = height;
  }

  // printf("Canvans info : h = %d, w = %d\n", *h, *w);
  // printf("Display info : h = %d, w = %d\n", height, width);

  close(fd);
}

void NDL_DrawRect(uint32_t *pixels, int x, int y, int w, int h) {
  // int len = w * h * sizeof(uint32_t);
  if (fbdev < 0) {
    fbdev = open("/dev/fb", 0, 0);
  }
  assert(fbdev != -1);

  // printf("[DNL_DrawRect]:\nposition: %d, %d\n, RectSize: %d, %d\n", x, y, w, h);

  for (int i = 0; i < h; i++) {
    int offset = sizeof(uint32_t) * ((y + i) * screen_w + x);
    int length = sizeof(uint32_t) * w;
    lseek(fbdev, offset, SEEK_SET);
    write(fbdev, pixels + (w * i), length);
  }

  // close(fbdev);
}

void NDL_OpenAudio(int freq, int channels, int samples) {
}

void NDL_CloseAudio() {
}

int NDL_PlayAudio(void *buf, int len) {
  return 0;
}

int NDL_QueryAudio() {
  return 0;
}

int NDL_Init(uint32_t flags) {
  if (getenv("NWM_APP")) {
    evtdev = 3;
  }
  return 0;
}

void NDL_Quit() {
}
