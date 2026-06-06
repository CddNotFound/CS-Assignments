#include <proc.h>

#define MAX_NR_PROC 4

static PCB pcb[MAX_NR_PROC] __attribute__((used)) = {};
static PCB pcb_boot = {};
PCB *current = NULL;

void switch_boot_pcb() {
  current = &pcb_boot;
}

void hello_fun(void *arg) {
  int j = 1;
  while (1) {

    if (j % 200 == 0) {
      Log("Hello World from Nanos-lite with arg '%p' for the %dth time!", (uintptr_t)arg, j);
    }
    j ++;
    yield();
  }
}

void init_proc() {
  switch_boot_pcb();

  Log("Initializing processes...");

  // load program here

  context_kload(&pcb[0], (void *)hello_fun, (void *)114514);
  context_uload(&pcb[1], "/bin/nterm", (char *[]){"nterm", NULL}, (char *[]){"PATH=/bin:/usr/bin:/QAQ", "SecondPath=QAQ", NULL});
  // context_uload(&pcb[1], "/bin/exec-test", (char *[]){"/bin/exec-test", "ls", "message", NULL}, (char *[]){"PATH=*", "INST=r|m -rf ~/PATH", NULL});
  switch_boot_pcb();
  yield();

  panic("Shouldn't reach here.");
}

Context* schedule(Context *prev) {
  current->cp = prev;
  current = (current == &pcb[0] ? &pcb[1] : &pcb[0]);
  return current->cp;
}
