#include <am.h>
#include <nemu.h>

#define SYNC_ADDR (VGACTL_ADDR + 4)

#include<stdio.h>
void __am_gpu_init() {
  int i = 0;
  int w = io_read(AM_GPU_CONFIG).width;
  int h = io_read(AM_GPU_CONFIG).height;
  uint32_t *fb = (uint32_t*)(uintptr_t)FB_ADDR;
  for (i = 0; i < w * h; i++) fb[i] = i;
  outl(SYNC_ADDR, 1);
}

void __am_gpu_config(AM_GPU_CONFIG_T *cfg) {
  uint32_t wh = inl(VGACTL_ADDR);
  uint32_t whMask = 0xffff;
  *cfg = (AM_GPU_CONFIG_T) {
    .present = true, .has_accel = false,
    .width = wh >> 16, .height = wh & whMask,
    .vmemsz = 0
  };
}

void __am_gpu_fbdraw(AM_GPU_FBDRAW_T *ctl) {
  int maxW = io_read(AM_GPU_CONFIG).width;
  int maxH = io_read(AM_GPU_CONFIG).height;

  uint32_t *pixels = ctl->pixels;
  uint32_t *fb = (uint32_t*)(uintptr_t)FB_ADDR;
  for (int i = 0; i < ctl->w; i++) {
    for (int j = 0; j < ctl->h; j++) {
      int posX = ctl->x + i;
      int posY = ctl->y + j;
      if (posX < 0 || posX >= maxW || posY < 0 || posY >= maxH) { continue; }
      uint32_t addr = posY * maxW + posX;
      fb[addr] = pixels[j * ctl->w + i];
    }
  }

  if (ctl->sync) {
    outl(SYNC_ADDR, 1);
  }
}

void __am_gpu_status(AM_GPU_STATUS_T *status) {
  status->ready = true;
}
