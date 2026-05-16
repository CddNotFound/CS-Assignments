#include <fs.h>
#include <proc.h>

typedef size_t (*ReadFn) (void *buf, size_t offset, size_t len);
typedef size_t (*WriteFn) (const void *buf, size_t offset, size_t len);

typedef struct {
  char *name;
  size_t size;
  size_t disk_offset;
  uint32_t openOffset;
  ReadFn read;
  WriteFn write;
} Finfo;

enum {FD_STDIN, FD_STDOUT, FD_STDERR, FD_EVENT, FD_FB, FD_DISINFO};

size_t invalid_read(void *buf, size_t offset, size_t len) {
  panic("should not reach here");
  return 0;
}

size_t invalid_write(const void *buf, size_t offset, size_t len) {
  panic("should not reach here");
  return 0;
}

/* This is the information about all files in disk. */
static Finfo file_table[] __attribute__((used)) = {
  [FD_STDIN]  = {"stdin", 0, 0, 0, invalid_read, invalid_write},
  [FD_STDOUT] = {"stdout", 0, 0, 0, invalid_read, serial_write},
  [FD_STDERR] = {"stderr", 0, 0, 0, invalid_read, serial_write},
  [FD_EVENT]  = {"/dev/events", 0, 0, 0, events_read, invalid_write},
  [FD_FB]     = {"/dev/fb", 0, 0, 0, invalid_read, fb_write},
  [FD_DISINFO]= {"/proc/dispinfo", 0, 0, 0, dispinfo_read, invalid_write},
#include "files.h"
};

const size_t fileNum = LENGTH(file_table);

void init_fs() {
  // TODO: initialize the size of /dev/fb

  AM_GPU_CONFIG_T cfg = io_read(AM_GPU_CONFIG);
  file_table[FD_FB].size = cfg.width * cfg.height * 4;

  for (int i = 0; i < fileNum; i++) {
    file_table[i].openOffset = 0;
  }
}


size_t fs_lseek(int fd, size_t offset, int whence) {
  switch (whence) {
    case SEEK_SET: file_table[fd].openOffset = offset; break;
    case SEEK_CUR: file_table[fd].openOffset += offset; break;
    case SEEK_END: file_table[fd].openOffset = file_table[fd].size + offset; break;
    default : panic("Unsupported lseek.");
  }

  return file_table[fd].openOffset;
}

int fs_open(const char *pathname, int flags, int mode) {
  for (int i = 0; i < fileNum; i++) {
    if (strcmp(file_table[i].name, pathname)) { continue; }

    return i;
  }

  panic("File not found.");

  return -1;
}

size_t fs_read(int fd, void *buf, size_t len) {
  if (file_table[fd].read != NULL) {
    size_t ret = file_table[fd].read(buf, file_table[fd].openOffset, len);
    fs_lseek(fd, ret, SEEK_CUR);

    return ret;
  }

  int openOffset = file_table[fd].openOffset;
  int fileOffset = file_table[fd].disk_offset;

  char *data = buf;
  int ret = ramdisk_read(data, fileOffset + openOffset, len);
  fs_lseek(fd, len, 1);

  return ret;
}

size_t fs_write(int fd, const void *buf, size_t len) {
  if (file_table[fd].write != NULL) {
    size_t ret = file_table[fd].write(buf, file_table[fd].openOffset, len);
    fs_lseek(fd, ret, SEEK_CUR);

    return ret;
  }

  int openOffset = file_table[fd].openOffset;
  int fileOffset = file_table[fd].disk_offset;

  int ret = ramdisk_write(buf, fileOffset + openOffset, len);
  fs_lseek(fd, len, 1);

  return ret;
}

int fs_close(int fd) {
  file_table[fd].openOffset = 0;

  return 0;
}
