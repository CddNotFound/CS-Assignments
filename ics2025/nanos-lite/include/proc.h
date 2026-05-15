#ifndef __PROC_H__
#define __PROC_H__

#include <common.h>
#include <memory.h>

#define STACK_SIZE (8 * PGSIZE)

typedef union {
  uint8_t stack[STACK_SIZE] PG_ALIGN;
  struct {
    Context *cp;
    AddrSpace as;
    // we do not free memory, so use `max_brk' to determine when to call _map()
    uintptr_t max_brk;
  };
} PCB;

extern PCB *current;

void naive_uload(PCB *pcb, const char *filename) ;

// disk

size_t ramdisk_read(void *buf, size_t offset, size_t len) ;
size_t ramdisk_write(const void *buf, size_t offset, size_t len) ;
size_t get_ramdisk_size() ;

// file

size_t fs_lseek(int fd, size_t offset, int whence) ;
int fs_open(const char *pathname, int flags, int mode) ;
size_t fs_read(int fd, void *buf, size_t len) ;
size_t fs_write(int fd, const void *buf, size_t len) ;
int fs_close(int fd) ;

size_t serial_write(const void *buf, size_t offset, size_t len) ;

// keyboard

size_t events_read(void *buf, size_t offset, size_t len) ;


#endif
