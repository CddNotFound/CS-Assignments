#define SDL_malloc  malloc
#define SDL_free    free
#define SDL_realloc realloc

#define SDL_STBIMAGE_IMPLEMENTATION
#include "SDL_stbimage.h"

#include <fcntl.h>
#include <unistd.h>

SDL_Surface* IMG_Load_RW(SDL_RWops *src, int freesrc) {
  assert(src->type == RW_TYPE_MEM);
  assert(freesrc == 0);
  return NULL;
}

SDL_Surface* IMG_Load(const char *filename) {
  int fd = open(filename, 1);
  assert(fd != -1);
  int fileStart = lseek(fd, 0, SEEK_SET);
  int fileEnd = lseek(fd, 0, SEEK_END);
  int fileSize = fileEnd - fileStart;

  char *image = malloc(sizeof(char) * fileSize);
  assert(image);

  read(fd, image, fileSize);
  SDL_Surface *s = STBIMG_LoadFromMemory(image, fileSize);

  close(fd);
  return s;
}

int IMG_isPNG(SDL_RWops *src) {
  return 0;
}

SDL_Surface* IMG_LoadJPG_RW(SDL_RWops *src) {
  return IMG_Load_RW(src, 0);
}

char *IMG_GetError() {
  return "Navy does not support IMG_GetError()";
}
