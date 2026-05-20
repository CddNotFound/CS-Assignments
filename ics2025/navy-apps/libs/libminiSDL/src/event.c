#include <NDL.h>
#include <SDL.h>
#include <stdio.h>
#include <string.h>

#define keyname(k) #k,

static const char *keyname[] = {
  "NONE",
  _KEYS(keyname)
};

int SDL_PushEvent(SDL_Event *ev) {
  return 0;
}

int pollSingleEvent(SDL_Event *event) {
  char buf[100];
  int ret = NDL_PollEvent(buf, 100);
  if (ret == 0) { return 0; }

  char keyEvent[5], keyName[100];
  sscanf(buf, "%s %s", keyEvent, keyName);

  int keyNum = sizeof(keyname) / sizeof(keyname[0]);
  int keyIdx = -1;
  for (int i = 1; i < keyNum; i++) {
    if (strcmp(keyname[i], keyName) == 0) {
      keyIdx = i;
      break;
    }
  }
  if (keyIdx == -1) { return 0; }

  event->key.keysym.sym = keyIdx;
  if (strcmp(keyEvent, "ku") == 0) {
    event->key.type = event->type = SDL_KEYUP;
  } else if (strcmp(keyEvent, "kd") == 0) {
    event->key.type = event->type = SDL_KEYDOWN;
  }  else {
    return 0;
  }

  return 1;
}

int SDL_PollEvent(SDL_Event *ev) {
  return pollSingleEvent(ev);
}

int SDL_WaitEvent(SDL_Event *event) {
  while (1) {
    if (pollSingleEvent(event)) { break; }
  }
  return 1;
}

int SDL_PeepEvents(SDL_Event *ev, int numevents, int action, uint32_t mask) {
  return 0;
}

uint8_t* SDL_GetKeyState(int *numkeys) {
  return NULL;
}
