#include <proc.h>
#include <elf.h>

#ifdef __LP64__
# define Elf_Ehdr Elf64_Ehdr
# define Elf_Phdr Elf64_Phdr
#else
# define Elf_Ehdr Elf32_Ehdr
# define Elf_Phdr Elf32_Phdr
#endif

static uintptr_t loader(PCB *pcb, const char *filename) {
  uint32_t fd = fs_open(filename, 0, 0);

  Elf_Ehdr elf;
  fs_read(fd, &elf, sizeof(elf));
  assert(*(uint32_t *)elf.e_ident == 0x464c457f);

  assert(elf.e_machine == EM_RISCV); 
   
  // size_t ramSize = get_ramdisk_size();

  fs_lseek(fd, elf.e_phoff, 0);
  for (int i = 0; i < elf.e_phnum; i++) {
    Elf_Phdr seg;
    fs_lseek(fd, elf.e_phoff + i * sizeof(Elf_Phdr), 0);
    fs_read(fd, &seg, sizeof(seg));

    if (seg.p_type != PT_LOAD) { continue; }

    uint32_t virtAddr = seg.p_vaddr, memSize = seg.p_memsz, fileSize = seg.p_filesz, offset = seg.p_offset;
    char *data = (char *)virtAddr;
    memset(data, 0, memSize);
    fs_lseek(fd, offset, 0);
    fs_read(fd, data, fileSize);
  }

  fs_close(fd);

  return elf.e_entry;
}

void naive_uload(PCB *pcb, const char *filename) {
  uintptr_t entry = loader(pcb, filename);
  Log("Jump to entry = %p", entry);
  ((void(*)())entry) ();
}

const int pageSize = 4096;

void context_uload(PCB *pcb, const char *filename, char *const argv[], char *const envp[]) {
  uintptr_t entry = loader(pcb, filename);

  pcb->cp = ucontext(NULL, (Area){pcb->stack, pcb->stack + STACK_SIZE}, (void *)entry);

  char *ustack = new_page(8);
  char *pointer = ustack + pageSize * 8;
  
  int argc = 0, envc = 0;
  while (argv && argv[argc]) ++argc;
  while (envp && envp[envc]) ++envc;

  char *argvAddr[argc];
  char *envpAddr[envc];

  for (int i = 0; i < argc; i++) {
    int len = strlen(argv[i]) + 1;
    pointer -= len;
    strcpy(pointer, argv[i]);
    argvAddr[i] = pointer;
  }
  for (int i = 0; i < envc; i++) {
    int len = strlen(envp[i]) + 1;
    pointer -= len;
    strcpy(pointer, envp[i]);
    envpAddr[i] = pointer;
  }

  pointer = (char *)((uintptr_t)pointer & ~0x3);

  pointer -= sizeof(char *);
  *(char **)pointer = NULL;
  for (int i = envc - 1; i >= 0; i--) {
    pointer -= sizeof(char *);
    *(char **)pointer = envpAddr[i];
  }
  pointer -= sizeof(char *);
  *(char **)pointer = NULL;
  for (int i = argc - 1; i >= 0; i--) {
    pointer -= sizeof(char *);
    *(char **)pointer = argvAddr[i];
  }
  
  pointer -= sizeof(int);
  *(int *)pointer = argc;

  pcb->cp->GPRx = (uintptr_t)pointer;
}