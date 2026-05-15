#include <stdio.h>
#include <assert.h>
#include <sys/_timeval.h>
#include <sys/time.h>

// typedef struct {
//   uint32_t tv_sec;
//   uint32_t tv_usec;
// }TimeVal;

int main() {
  struct timeval tv;
  gettimeofday(&tv, NULL);

  int lstTime = tv.tv_sec * 1000000 + tv.tv_usec;

  while (1) {
    gettimeofday(&tv, NULL);
    int curTime = tv.tv_sec * 1000000 + tv.tv_usec;

    if (curTime > lstTime + 500000) {
      printf("Times passed 0.5 second!\n");

      lstTime += 500000;
    }
  }

  return 0;
}
