#include <NDL.h>
#include <SDL.h>
#include <stdlib.h>
#include <unistd.h>
#include <fcntl.h>
#include <string.h>
#include <assert.h>
#include <stdbool.h>

static int freq = 0;
static int channels = 0;
static int samples = 0;

static int bytesPerSample = 2;
static int frameSize = 0;
static int callbackLen = 0;
static uint32_t callbackInterval = 1000;
static uint16_t format = 0;
static int lastCallback = 0;
static int audioPaused = 1;

static void (*callback)(void *userdata, uint8_t *stream, int len);
static void *userdata;

static int calledHelper = 0;

#define min(x, y) ((x) < (y) ? (x) : (y))
#define max(x, y) ((x) > (y) ? (x) : (y))

uint8_t *stream = NULL;

int SDL_OpenAudio(SDL_AudioSpec *desired, SDL_AudioSpec *obtained) {
  if (obtained) { *obtained = *desired; }

  freq = desired->freq;
  channels = desired->channels;
  samples = desired->samples;
  format = desired->format;
  userdata = desired->userdata;
  frameSize = channels * bytesPerSample;

  callbackLen = samples * channels * bytesPerSample;
  callbackInterval = samples * 1000 / freq;
  // printf("Interval == %d\n", callbackInterval);
  callback = desired->callback;

  stream = malloc(sizeof(uint8_t) * callbackLen);

  lastCallback = NDL_GetTicks();
  calledHelper = 0;

  NDL_OpenAudio(freq, channels, samples);

  return 0;
}


void CallbackHelper() {
  if (calledHelper) { return ; }
  
  calledHelper += 1;

  // printf("Try to call!\n");
  if (!freq) {
    return ;
  }

  // if (NDL_GetTicks() <= lastCallback + callbackInterval) { return ;}
  if (audioPaused) { return ;}

  // lastCallback = NDL_GetTicks();
  lastCallback += callbackInterval;

  if (callback == NULL) {return ;}
  
  while (1) {
    int restSize = NDL_QueryAudio();
    int use = callbackLen < restSize ? callbackLen : restSize;
    use = use / frameSize * frameSize;

    if (use == 0) {return ;}

    // printf("Call!\n");
    // printf("Get data size = %d\n", use);
    callback(userdata, stream, use);
    NDL_PlayAudio(stream, use);
  }
  // printf("Leave Call!\n");

  calledHelper -= 1;
}

void SDL_CloseAudio() {
  freq = channels = samples = format = frameSize = 0;
  callbackInterval = 1000, callbackLen = 0;
  userdata = NULL;
  audioPaused = 1;

  free(stream);
  stream = NULL;
  callback = NULL;
  calledHelper = 0;

  NDL_CloseAudio();

  return ;
}

void SDL_PauseAudio(int pause_on) {
  audioPaused = pause_on;
}

void SDL_MixAudio(uint8_t *dst, uint8_t *src, uint32_t len, int volume) {
  int16_t *d = (int16_t *)dst;
  int16_t *s = (int16_t *)src;
  int length = len / sizeof(int16_t);
  for (int i = 0; i < length; i++) {
    int32_t tmp = *(d + i);
    tmp += (int32_t)*(s + i) * volume / SDL_MIX_MAXVOLUME;
    tmp = max(tmp, -32768);
    tmp = min(tmp, 32767);
    *(d + i) = tmp;
  }
}

typedef struct {
  uint16_t audioFormat;
  uint16_t numChannels;
  uint32_t sampleRate;
  uint32_t byteRate;
  uint16_t blockAlign;
  uint16_t bitsPerSample;
} WavFmt;

SDL_AudioSpec *SDL_LoadWAV(const char *file, SDL_AudioSpec *spec, uint8_t **audio_buf, uint32_t *audio_len) {
  int fd = open(file, 0, 0);
  assert(fd != -1);

  char riff[4], wave[4];
  uint32_t headSize;
  read(fd, riff, 4);
  read(fd, &headSize, 4);
  read(fd, wave, 4);

  if (memcmp(riff, "RIFF", 4) != 0 || memcmp(wave, "WAVE", 4) != 0) {
    close(fd);
    return NULL;
  }

  WavFmt fmt;
  bool getFmt = false, getData = false;

  while (1) {
    char id[4];
    uint32_t size;
    int rd;

    rd = read(fd, id, 4);
    if (rd != 4) { break; }
    rd = read(fd, &size, 4);
    if (rd != 4) { break; }

    if (memcmp(id, "fmt ", 4) == 0) {
      read(fd, &fmt, sizeof(fmt));
      if (size > sizeof(fmt)) {
        lseek(fd, size - sizeof(fmt), SEEK_CUR);
      }

      getFmt = true;
    } else if (memcmp(id, "data", 4) == 0) {
      *audio_buf = malloc(size);
      assert(*audio_buf);

      read(fd, *audio_buf, size);
      *audio_len = size;

      getData = true;
    } else {
      lseek(fd, size, SEEK_CUR);
    }

    if (size & 1) {
      lseek(fd, 1, SEEK_CUR);
    }

    if (getData && getFmt) { break; }
  }

  close(fd);

  if (!getData || !getFmt) return NULL;
  assert(fmt.audioFormat == 1);
  assert(fmt.bitsPerSample == 16);

  spec->freq = fmt.sampleRate;
  spec->channels = fmt.numChannels;
  spec->format = AUDIO_S16SYS;
  spec->samples = 1024;
  spec->callback = NULL;
  spec->userdata = NULL;

  return spec;
}

void SDL_FreeWAV(uint8_t *audio_buf) {
  free(audio_buf);
}

void SDL_LockAudio() {
}

void SDL_UnlockAudio() {
}
