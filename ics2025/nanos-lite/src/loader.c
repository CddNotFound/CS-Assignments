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

  Elf_Ehdr elf;
  ramdisk_read(&elf, 0, sizeof(elf));
  assert(*(uint32_t *)elf.e_ident == 0x464c457f);

  assert(elf.e_machine == EM_RISCV); 
   
  // size_t ramSize = get_ramdisk_size();

  for (int i = 0; i < elf.e_phnum; i++) {
    Elf_Phdr seg;
    ramdisk_read(&seg, elf.e_phoff + i * sizeof(Elf_Phdr), sizeof(seg));

    if (seg.p_type != PT_LOAD) { continue; }

    uint32_t virtAddr = seg.p_vaddr, memSize = seg.p_memsz, fileSize = seg.p_filesz, offset = seg.p_offset;
    char *data = (char *)virtAddr;
    memset(data, 0, memSize);
    ramdisk_read(data, offset, fileSize);
  }

  return 0;
}

void naive_uload(PCB *pcb, const char *filename) {
  uintptr_t entry = loader(pcb, filename);
  Log("Jump to entry = %p", entry);
  ((void(*)())entry) ();
}

