#include <common.h>
#include "syscall.h"

enum {
  EXIT, 
  YIELD,
  A,
  B,
  WRITE, 
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

static void SYS_write(Context *c) {
  int fd = c->GPR2;
  char *buf = (char *)c->GPR3;
  size_t count = c->GPR4;

  if (fd != 1 && fd != 2) {
    c -> GPRx = -1;
    return ;
  }
  
  for (int i = 0; i < count; i++) {
    putch(*(buf + i));
  }

  c->GPRx = count;
}

void do_syscall(Context *c) {
  uintptr_t a[4];
  a[0] = c->GPR1;
  // a[1] = c->GPR2;
  // a[2] = c->GPR3;
  // a[3] = c->GPR4;

  switch (a[0]) {
    case EXIT: SYS_exit(c); break;
    case YIELD: SYS_yield(c); break;
    case WRITE: SYS_write(c); break;
    default: panic("Unhandled syscall ID = %d", a[0]);
  }
}
