#include <stdint.h>
#include <stdlib.h>
#include <assert.h>
// #include <../../libc/src/misc/init.c>

int main(int argc, char *argv[], char *envp[]);
extern char **environ;
void __libc_init_array() ;

void call_main(uintptr_t *args) {
  char *empty[] =  {NULL };
  environ = empty;

  __libc_init_array();

  exit(main(0, empty, empty));
  assert(0);
}
