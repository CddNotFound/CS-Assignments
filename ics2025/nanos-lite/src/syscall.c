#include <common.h>
#include "syscall.h"

enum {
  EXIT, 
  YIELD,
};

static void SYS_exit(Context *c) {
  Log("System Call: exit.\n");
  halt(c->GPR2);
  c->GPRx = 0;
}

static void SYS_yield(Context *c) {
  Log("System Call: yield.\n");
  yield();
  c->GPRx = 0;
}

void do_syscall(Context *c) {
  uintptr_t a[4];
  a[0] = c->GPR1;
  a[1] = c->GPR2;
  a[2] = c->GPR3;
  a[3] = c->GPR4;

  switch (a[0]) {
    case EXIT: SYS_exit(c); break;
    case YIELD: SYS_yield(c); break;
    default: panic("Unhandled syscall ID = %d", a[0]);
  }
}
