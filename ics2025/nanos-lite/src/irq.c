#include <common.h>
#include <proc.h>

void context_kload(PCB *pcb, void (entry)(void *), void *arg) {
  pcb->cp = kcontext((Area){pcb->stack, pcb->stack + 1}, entry, arg);
}

extern void do_syscall(Context* c) ;

static Context* do_event(Event e, Context* c) {
  switch (e.event) {
    case EVENT_YIELD : return schedule(c); break;
    case EVENT_SYSCALL : do_syscall(c); break; 
    default: panic("Unhandled event ID = %d", e.event);
  }

  return c;
}

void init_irq(void) {
  Log("Initializing interrupt/exception handler...");
  cte_init(do_event);
}
