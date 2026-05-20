#include <stdio.h>
#include <stdlib.h>
#include "fixedptc.h"

#define I(x) fixedpt_fromint(x)

static void check(const char *name, fixedpt got, fixedpt expect) {
  if (got != expect) {
    printf("FAIL %s: got raw=%lld, expect raw=%lld\n",name, (long long)got, (long long)expect);
    exit(1);    
  }
}

int main() {
  fixedpt q = FIXEDPT_ONE / 4;
  fixedpt h = FIXEDPT_ONE / 2;

  check("floor  1.25", fixedpt_floor(I(1) + q), I(1));
  check("ceil   1.25", fixedpt_ceil (I(1) + q), I(2));

  check("floor  1.00", fixedpt_floor(I(1)), I(1));
  check("ceil   1.00", fixedpt_ceil (I(1)), I(1));

  check("floor -1.25", fixedpt_floor(I(-1) - q), I(-2));
  check("ceil  -1.25", fixedpt_ceil (I(-1) - q), I(-1));
 
  check("floor -1.00", fixedpt_floor(I(-1)), I(-1));
  check("ceil  -1.00", fixedpt_ceil (I(-1)), I(-1));

  check("floor -0.25", fixedpt_floor(-q), I(-1));
  check("ceil  -0.25", fixedpt_ceil (-q), I(0));

  check("floor tiny neg", fixedpt_floor((fixedpt)-1), I(-1));
  check("ceil  tiny neg", fixedpt_ceil ((fixedpt)-1), I(0));

  check("mul 1.5 * 2", fixedpt_mul(I(1) + h, I(2)), I(3));
  check("mul -1.5 * 2", fixedpt_mul(I(-1) - h, I(2)), I(-3));

  check("div 3 / 2", fixedpt_div(I(3), I(2)), I(1) + h);
  check("div -3 / 2", fixedpt_div(I(-3), I(2)), I(-1) - h);

  printf("all fixedpt tests passed\n");
  return 0;
}
