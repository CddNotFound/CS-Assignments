
#include <common.h>
#include <elf.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <assert.h>

#ifdef CONFIG_FTRACE

typedef struct {
  char funcName[64];
  unsigned int funcAddr;
}FtraceStack;

typedef struct {
  char name[64];
  unsigned int addr;
  unsigned int size;
//   unsigned char *type;
}SymbolTable;

SymbolTable symbolTable[512];
int symbolNum = 0;

// FtraceStack functionStack[65536];
int currentStage = 0;

char ftraceLog[65536][128];
int logCnt = 0;

void initFtrace(char *fileName) {
  FILE *fp = fopen(fileName, "rb");
  Assert(fp, "Can not open '%s'", fileName); 

  Elf32_Ehdr header;
  int ret = fread(&header, sizeof(header), 1, fp);
  Assert(ret == 1, "Fail to read elf header.");

  Elf32_Shdr *shdrs = malloc(header.e_shentsize * header.e_shnum);

  ret = fseek(fp, header.e_shoff, SEEK_SET);
  Assert(ret == 0, "Fail to locate section header table.");
  ret = fread(shdrs, header.e_shentsize, header.e_shnum, fp);
  Assert(ret == header.e_shnum, "Fail to read section header table.");

  for (int i = 0; i < header.e_shnum; i++) {
    if (shdrs[i].sh_type != SHT_SYMTAB) {
      continue;
    }

    Elf32_Shdr symtab_sh = shdrs[i];
    Elf32_Shdr strtab_sh = shdrs[symtab_sh.sh_link];

    Elf32_Sym *syms = malloc(symtab_sh.sh_size);
    char *strtab = malloc(strtab_sh.sh_size);
    assert(syms != NULL && strtab != NULL);

    ret = fseek(fp, symtab_sh.sh_offset, SEEK_SET);
    Assert(ret == 0, "Fail to locate symtab.");
    ret = fread(syms, symtab_sh.sh_size, 1, fp);
    Assert(ret, "Fail to read symtab.");

    ret = fseek(fp, strtab_sh.sh_offset, SEEK_SET);
    Assert(ret == 0, "Fail to locate strtab.");
    ret = fread(strtab, strtab_sh.sh_size, 1, fp);
    Assert(ret, "Fail to read strtab.");

    symbolNum = symtab_sh.sh_size / sizeof(Elf32_Sym);

    for (int j = 0; j < symbolNum; j++) {
      Elf32_Sym *sym = &syms[j];
      const char *name = strtab + sym->st_name;

      int type = ELF32_ST_TYPE(sym->st_info);
      if (type != STT_FUNC) { continue; }

      strcpy(symbolTable[j].name, name);
      symbolTable[j].addr = sym->st_value;
      symbolTable[j].size = sym->st_size;
      printf("%s: %u %u\n", name, sym->st_value, sym->st_size);
    //   strcpy(symbolTable[j].type, sym->st_info);

    }

    free(syms);
    free(strtab);
    free(shdrs);
    fclose(fp);
  }
}

void callFunction(const int addr, const int instAddr) {
  for (int i = 0; i < symbolNum; i++) {
    // if (strcmp(symbolTable[i].type, "FUNC") != 0) {
    //   continue;
    // }

    int startAddr = symbolTable[i].addr;
    int symbolSize =symbolTable[i].size;
    if (startAddr <= addr && addr < startAddr + symbolSize) {
    //   int cur = currentStage;
    //   strcpy(functionStack[cur].funcName, symbolTable[i].name);
    //   functionStack[cur].funcAddr = startAddr;

      char *p = ftraceLog[logCnt];
      sprintf(p, "0x%8x: ", instAddr);
      p += 12;
      for (int j = 0; j < currentStage; j++) {
          sprintf(p, "  ");
          p += 2;
      }
      sprintf(p, "call [%s@0x%8x]", symbolTable[i].name, startAddr);

      log_write("%s\n", ftraceLog[logCnt]);

      logCnt += 1;
      currentStage += 1;

      return ;
    }
  }

  printf("???: addr is not in a function.");
}

void retFunction(const int addr) {
  --currentStage;
  for (int i = 0; i < symbolNum; i++) {
    // if (strcmp(symbolTable[i].type, "FUNC") != 0) {
    //     continue;
    // }

    int startAddr = symbolTable[i].addr;
    int symbolSize =symbolTable[i].size;
    if (startAddr <= addr && addr < startAddr + symbolSize) {
    //   int cur = currentStage;

      char *p = ftraceLog[logCnt];
      sprintf(p, "0x%8x: ", addr);
      p += 12;
      for (int j = 0; j < currentStage; j++) {
          sprintf(p, "  ");
          p += 2;
      }
      sprintf(p, "ret [%s]", symbolTable[i].name);

      log_write("%s\n", ftraceLog[logCnt]);

      logCnt += 1;
      return ;
    }
  }
}
#endif