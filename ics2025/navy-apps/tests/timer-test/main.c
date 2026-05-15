#include <stdio.h>
#include <assert.h>
#include <NDL.h>

int main() {
  NDL_Init(0);
  
  int lstTime = NDL_GetTicks();

  while (1) {
    int curTime = NDL_GetTicks();

    if (curTime > lstTime + 500000) {
      printf("Times passed 0.5 second!\n");

      lstTime += 500000;
    }
  }

  NDL_Quit();

  return 0;
}
