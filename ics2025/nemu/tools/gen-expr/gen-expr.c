/***************************************************************************************
* Copyright (c) 2014-2024 Zihao Yu, Nanjing University
*
* NEMU is licensed under Mulan PSL v2.
* You can use this software according to the terms and conditions of the Mulan PSL v2.
* You may obtain a copy of Mulan PSL v2 at:
*          http://license.coscl.org.cn/MulanPSL2
*
* THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
* EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
* MERCHANTABILITY OR FIT FOR A PARTICULAR PURPOSE.
*
* See the Mulan PSL v2 for more details.
***************************************************************************************/

#include <stdint.h>
#include <stdio.h>
#include <stdlib.h>
#include <time.h>
#include <assert.h>
#include <string.h>

// this should be enough
static char buf[65536] = {};
static char *buf_ptr = buf;
static char code_buf[65536 + 128] = {}; // a little larger than `buf`
static char *code_format =
"#include <stdio.h>\n"
"int main() { "
"  unsigned result = %s; "
"  printf(\"%%u\", result); "
"  return 0; "
"}";

const int inf32 = 2147483637;
const int inf16 = 65536;

int choose(int x) {
  return rand() % x;
}

void gen_space() {
  for (int i = 0; i < choose(5); i++) {
    snprintf(buf_ptr, 10, " ");
    buf_ptr += 1;
  }
}

void gen_rand_op() {
  gen_space();
  char *op;
  switch (choose(20)) {
    case 0: case 1: case 2: case 3: case 5:
      op = " + "; break;
    case 6: case 7: case 8: case 9: case 10:
      op = " - "; break;
    case 11: case 12: case 13: case 14: case 15:
     op = " * "; break;
    case 16:
     op = " / "; break;
    case 17: op = " == "; break;
    case 18: op = " != "; break;
    default: op = " && "; break;
  }
  snprintf(buf_ptr, 10, "%s", op);
  buf_ptr += strlen(op);
}

void gen(char ch) {
  gen_space();
  snprintf(buf_ptr, 10, "%c", ch);
  buf_ptr += 1;
}

void gen_num() {
  gen_space();
  int x = rand() % inf16;

  snprintf(buf_ptr, 10, "0x");
  buf_ptr += 2;

  int res[10] = {0};
  int cnt = 0;
  while (x) {
    res[cnt] = x % 16;
    cnt += 1;
    x >>= 4;
  }

  for (int i = cnt - 1; i >= 0; i--) {
    if (res[i] <= 9) {
      snprintf(buf_ptr, 10, "%c", (char)res[i] + 48);
      buf_ptr += 1;
    } else {
      snprintf(buf_ptr, 10, "%c", (res[i] - 10 + 'a'));
      buf_ptr += 1;
    }
  }
  snprintf(buf_ptr, 10, "u");
  buf_ptr += 1;
}

static void gen_rand_expr(int depth) {
  // buf[0] = '\0';
  if (depth > 16) {
    gen_num();
  } else {
    switch (choose(3)) {
      case 0: gen_num(); break;
      case 1: gen('('); gen_rand_expr(depth + 1); gen(')'); break;
      default: gen_rand_expr(depth + 1); gen_rand_op(); gen_rand_expr(depth + 1); break;
    }
  }
}

int main(int argc, char *argv[]) {
  int seed = time(0);
  srand(seed);
  int loop = 1;
  if (argc > 1) {
    sscanf(argv[1], "%d", &loop);
  }
  int i;
  for (i = 0; i < loop;) {
    buf[0] = '\0';
    buf_ptr = buf;
    gen_rand_expr(1);

    snprintf(code_buf, sizeof(code_buf), code_format, buf);

    FILE *fp = fopen("/tmp/.code.c", "w");
    assert(fp != NULL);
    fputs(code_buf, fp);
    fclose(fp);

    int ret = system("gcc /tmp/.code.c -o /tmp/.expr");
    if (ret != 0) continue;

    fp = popen("/tmp/.expr", "r");
    assert(fp != NULL);

    int result;
    ret = fscanf(fp, "%d", &result);
    int status = pclose(fp);

    if (ret != 1 || status) {
      continue;
    }
    // printf("%d-%d: " , i, loop);

    printf("%u %s\n", result, buf);
    ++i;
  }
  return 0;
}
