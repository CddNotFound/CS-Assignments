#include <common.h>
#include "syscall.h"
#include <proc.h>

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

typedef struct {
  uint32_t tv_sec;
  uint32_t tv_usec;
}TimeVal;

extern void switch_boot_pcb() ;

static char oriEnvp[128][128];
static char *envpAddr[128];
static int envc = 0;
static bool envInit = 0;

static Context *SYS_Execve(Context *c) {
#ifdef CONFIG_STRACE
  Log("System Call: Execve.\n");
#endif
  // printf("Next file: %s\n", (char *)c->GPR2);
  
  char *filename = (char *)c->GPR2;
  char *const *argv = (char *const *)c->GPR3;
  char *const *envp = (char *const *)c->GPR4;
  
  if (!envInit) {
    while (envp && envp[envc]) { ++envc; }
    for (int i = 0; i < envc; i++) { strcpy(oriEnvp[i], envp[i]); envpAddr[i] = oriEnvp[i]; }
    envInit = true;
  }

  context_uload(current, filename, argv, envp);

  c -> GPRx = 0;
  return current->cp;
}

static Context *SYS_Exit(Context *c) {
#ifdef CONFIG_STRACE
  Log("System Call: exit.\n");
#endif

  // halt(c->GPR2);
  char *filename = "/bin/nterm";
  char *argv[] = {"/bin/nterm", NULL};
  Context Menu = *c;
  Menu.GPR2 = (uintptr_t)filename;
  Menu.GPR3 = (uintptr_t)argv;
  Menu.GPR4 = (uintptr_t)envpAddr;
  Context *ret = SYS_Execve(&Menu);
  c->GPRx = 0;

  return ret;
}

static void SYS_Yield(Context *c) {
#ifdef CONFIG_STRACE
  Log("System Call: yield.\n");
#endif

  yield();

  c->GPRx = 0;
}

static void SYS_Write(Context *c) {
#ifdef CONFIG_STRACE
  Log("System Call: write.\n");
#endif

  int fd = c->GPR2;
  char *buf = (char *)c->GPR3;
  size_t count = c->GPR4;

  int ret = fs_write(fd, buf, count);

  c->GPRx = ret;
}

static void SYS_Brk(Context *c) {
#ifdef CONFIG_STRACE
  Log("System Call: sbrk.\n");
#endif

  c->GPRx = 0;
}

static void SYS_Open(Context *c) {
#ifdef CONFIG_STRACE
  Log("System Call: open.\n");
#endif  

  char *path = (char *)c->GPR2;
  int flags = c->GPR3;
  int modes = c->GPR4;

  int ret = fs_open(path, flags, modes);

  c->GPRx = ret;
}

static void SYS_Read(Context *c) {
#ifdef CONFIG_STRACE
  Log("System Call: read.\n");
#endif  

  int fd = c->GPR2;
  char *buf = (char *)c->GPR3;
  size_t count = c->GPR4;

  int ret = fs_read(fd, buf, count);

  c->GPRx = ret;
}

static void SYS_Close(Context *c) {
#ifdef CONFIG_STRACE
  Log("System Call: close.\n");
#endif  

  int fd = c->GPR2;

  int ret = fs_close(fd);

  c->GPRx = ret;
}

static void SYS_Lseek(Context *c) {
#ifdef CONFIG_STRACE
  Log("System Call: lseek.\n");
#endif  

  int fd = c->GPR2;
  uint32_t offset = c->GPR3;
  int whence = c->GPR4;

  int ret = fs_lseek(fd, offset, whence);

  c->GPRx = ret;
}

static void SYS_Gettimeofday(Context *c) {
#ifdef CONFIG_STRACE
  Log("System Call: get time of day.\n");
#endif  
  TimeVal *tv = (TimeVal *)c->GPR2;
  
  uint32_t us = io_read(AM_TIMER_UPTIME).us;
  
  tv->tv_sec = us / 1000000;
  tv->tv_usec = us % 1000000;

  c -> GPRx = 0;
}

Context *do_syscall(Context *c) {
  uintptr_t a[4];
  a[0] = c->GPR1;
  // a[1] = c->GPR2;
  // a[2] = c->GPR3;
  // a[3] = c->GPR4;

  switch (a[0]) {
    case EXIT        : c = SYS_Exit(c);         break;
    case YIELD       :     SYS_Yield(c);        break;
    case WRITE       :     SYS_Write(c);        break;
    case BRK         :     SYS_Brk(c);          break;
    case READ        :     SYS_Read(c);         break;
    case CLOSE       :     SYS_Close(c);        break;
    case OPEN        :     SYS_Open(c);         break;
    case LSEEK       :     SYS_Lseek(c);        break;
    case GETTIMEOFDAY:     SYS_Gettimeofday(c); break;
    case EXECVE      : c = SYS_Execve(c);       break;
    default: panic("Unhandled syscall ID = %d", a[0]);
  }

  return c;
}
