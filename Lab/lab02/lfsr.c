#include <stdio.h>
#include <stdint.h>
#include <stdlib.h>
#include <string.h>
#include "lfsr.h"

void lfsr_calculate(uint16_t *reg) {
    /* YOUR CODE HERE */

    uint16_t num0 = *reg & 1u, num2 = *reg >> 2 & 1u, num3 = *reg >> 3 & 1u, num5 = *reg >> 5 & 1u;
    *reg >>= 1;
    *reg |= ((num0 ^ num2 ^ num3 ^ num5) << 15);
}

