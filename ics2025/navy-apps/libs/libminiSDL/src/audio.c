#include <NDL.h>
#include <SDL.h>
#include <stdlib.h>

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
  printf("Interval == %d\n", callbackInterval);
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
}

SDL_AudioSpec *SDL_LoadWAV(const char *file, SDL_AudioSpec *spec, uint8_t **audio_buf, uint32_t *audio_len) {
  return NULL;
}

void SDL_FreeWAV(uint8_t *audio_buf) {
}

void SDL_LockAudio() {
}

void SDL_UnlockAudio() {
}
