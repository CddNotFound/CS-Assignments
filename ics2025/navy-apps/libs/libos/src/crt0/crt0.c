#include <stdint.h>
#include <stdlib.h>
#include <assert.h>
// #include <../../libc/src/misc/init.c>

int main(int argc, char *argv[], char *envp[]);
extern char **environ;
void __libc_init_array() ;

#include<stdio.h>
void call_main(uintptr_t *args) {
  char *data = (char *)args;
  int argc = *(int *)data;
  char **argv = (char **)(data + sizeof(int));
  char **envp = (char **)(data + sizeof(int) + sizeof(char *) * (argc + 1));

  __libc_init_array();
  environ = envp;
  exit(main(argc, argv, envp));
  assert(0);
}
