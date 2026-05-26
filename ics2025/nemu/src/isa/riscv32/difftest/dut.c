/***************************************************************************************
* Copyright (c) 2014-2024 Zihao Yu, Nanjing University
*
* NEMU is licensed under Mulan PSL v2.
* You can use this software according to the terms and conditions of the Mulan PSL v2.
* You may obtain a copy of Mulan PSL v2 at:
*          http://license.coscl.org.cn/MulanPSL2
*
* THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
* EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
* MERCHANTABILITY OR FIT FOR A PARTICULAR PURPOSE.
*
* See the Mulan PSL v2 for more details.
***************************************************************************************/

#include <isa.h>
#include <cpu/difftest.h>
#include <memory/paddr.h>
#include "../local-include/reg.h"

bool isa_difftest_checkregs(CPU_state *ref_r, vaddr_t pc) {
  for (int i = 0; i < 32; i++) {
    word_t nemuValue = gpr(i);
    word_t REFValue = ref_r -> gpr[i];
    if (nemuValue != REFValue) {
      printf("RegDiff at idx %d\n.", i);
      return false;
    }
  }

  if (cpu.pc != ref_r -> pc) {
    printf("PC diff at 0x%8x.\n", pc);
    return false;
  }

  return true;
}

#define CSRW(csr, rs1) (((csr) << 20) | ((rs1) << 15) | (1 << 12) | 0x73)
#define PMEM_LEFT  ((paddr_t)CONFIG_MBASE)
#define PMEM_RIGHT ((paddr_t)CONFIG_MBASE + CONFIG_MSIZE - 1)
#define RESET_VECTOR (PMEM_LEFT + CONFIG_PC_RESET_OFFSET)
paddr_t scratch = RESET_VECTOR;

void isa_difftest_attach() {
  uint32_t code[] = {
    CSRW(0x300, 5), // mstatus <- x5
    CSRW(0x305, 6), // mtvec   <- x6
    CSRW(0x341, 7), // mepc    <- x7
    CSRW(0x342, 8), // mcause  <- x8
  };

  ref_difftest_memcpy(scratch, code, sizeof(code), DIFFTEST_TO_REF);

  CPU_state tmp = cpu;
  tmp.gpr[5] = cpu.csr[MSTATUS];
  tmp.gpr[6] = cpu.csr[MTVEC];
  tmp.gpr[7] = cpu.csr[MEPC];
  tmp.gpr[8] = cpu.csr[MCAUSE];
  tmp.pc = scratch;

  ref_difftest_regcpy(&tmp, DIFFTEST_TO_REF);
  ref_difftest_exec(4);
  ref_difftest_memcpy(scratch, guest_to_host(scratch), sizeof(code), DIFFTEST_TO_REF);
  ref_difftest_regcpy(&cpu, DIFFTEST_TO_REF);
}
