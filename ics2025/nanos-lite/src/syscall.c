#include <common.h>
#include "syscall.h"

enum {
  EXIT, 
  YIELD, 
  OPEN, 
  READ, 
  WRITE, 
  KILL, 
  GETPID, 
  CLOSE, 
  LSEEK, 
  BRK, 
  FSTAT, 
  TIME, 
  SIGNAL, 
  EXECVE, 
  FORK, 
  LINK, 
  UNLINK, 
  WAIT, 
  TIMES, 
  GETTIMEOFDAY,
};

static void SYS_exit(Context *c) {
#ifdef CONFIG_STRACE
  Log("System Call: exit.\n");
#endif

  halt(c->GPR2);

  c->GPRx = 0;
}

static void SYS_yield(Context *c) {
#ifdef CONFIG_STRACE
  Log("System Call: yield.\n");
#endif

  yield();

  c->GPRx = 0;
}

static void SYS_write(Context *c) {
#ifdef CONFIG_STRACE
  Log("System Call: write.\n");
#endif

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

static void SYS_brk(Context *c) {
#ifdef CONFIG_STRACE
  Log("System Call: sbrk.\n");
#endif

  c->GPRx = 0;
}

void do_syscall(Context *c) {
  uintptr_t a[4];
  a[0] = c->GPR1;
  // a[1] = c->GPR2;
  // a[2] = c->GPR3;
  // a[3] = c->GPR4;

  switch (a[0]) {
    case EXIT : SYS_exit(c);  break;
    case YIELD: SYS_yield(c); break;
    case WRITE: SYS_write(c); break;
    case BRK  : SYS_brk(c);   break;
    default: panic("Unhandled syscall ID = %d", a[0]);
  }
}
