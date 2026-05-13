#include <am.h>
#include <nemu.h>

#define AUDIO_FREQ_ADDR      (AUDIO_ADDR + 0x00)
#define AUDIO_CHANNELS_ADDR  (AUDIO_ADDR + 0x04)
#define AUDIO_SAMPLES_ADDR   (AUDIO_ADDR + 0x08)
#define AUDIO_SBUF_SIZE_ADDR (AUDIO_ADDR + 0x0c)
#define AUDIO_INIT_ADDR      (AUDIO_ADDR + 0x10)
#define AUDIO_COUNT_ADDR     (AUDIO_ADDR + 0x14)

static uint32_t idx = 0;

void __am_audio_init() {
}

void __am_audio_config(AM_AUDIO_CONFIG_T *cfg) {
  cfg->present = true;
  cfg->bufsize = inl(AUDIO_SBUF_SIZE_ADDR);
}

void __am_audio_ctrl(AM_AUDIO_CTRL_T *ctrl) {
  outl(AUDIO_FREQ_ADDR, ctrl->freq);
  outl(AUDIO_CHANNELS_ADDR, ctrl->channels);
  outl(AUDIO_SAMPLES_ADDR, ctrl->samples);
  outl(AUDIO_INIT_ADDR, 1);
}

void __am_audio_status(AM_AUDIO_STATUS_T *stat) {
  stat->count = inl(AUDIO_COUNT_ADDR);
}

void __am_audio_play(AM_AUDIO_PLAY_T *ctl) {
  char *bufStart = ctl->buf.start;
  char *bufEnd = ctl->buf.end;
  int len = bufEnd - bufStart;
  uint32_t bufSize = inl(AUDIO_SBUF_SIZE_ADDR);

  while (bufStart < bufEnd) {
    int count = inl(AUDIO_COUNT_ADDR);
    int restSize = bufSize - count;

    if (restSize <= 0) { continue; }

    int n = len > restSize ? restSize : len;

    uint8_t *sbuf = (uint8_t*)(uintptr_t*)AUDIO_SBUF_ADDR;
    for (int i = 0; i < n; i++) {
      char data = *bufStart;
      sbuf[idx] = data;
      // outb(AUDIO_SBUF_ADDR + idx, data);
      
      idx = (idx + 1) % bufSize;
      bufStart += 1;
    }
    
    // outl(AUDIO_WRITE_LCOK, 1);
    // count = inl(AUDIO_COUNT_ADDR);
    // count += n;
    outl(AUDIO_COUNT_ADDR, inl(AUDIO_COUNT_ADDR) + n);
    len -= n;

    // outl(AUDIO_WRITE_LCOK, 0);
  }
}
