.globl dot

.text
# =======================================================
# FUNCTION: Dot product of 2 int vectors
# Arguments:
#   a0 (int*) is the pointer to the start of v0
#   a1 (int*) is the pointer to the start of v1
#   a2 (int)  is the length of the vectors
#   a3 (int)  is the stride of v0
#   a4 (int)  is the stride of v1
# Returns:
#   a0 (int)  is the dot product of v0 and v1
# Exceptions:
# - If the length of the vector is less than 1,
#   this function terminates the program with error code 75.
# - If the stride of either vector is less than 1,
#   this function terminates the program with error code 76.
# =======================================================
dot:

    # Prologue

    li t0, 1
    blt a2, t0, empty
    blt a3, t0, TooShortStride
    blt a4, t0, TooShortStride

    li t0, 0   # i
    add t1, zero, a0  # a[i]
    add t2, zero, a1  # b[i]
    li a0, 0   # result
    li t3, 4

loop_start:
    bge t0, a2, loop_end
    lw t4, 0(t1)
    lw t5, 0(t2)
    mul t6, t4, t5
    add a0, a0, t6

    mul t6, a3, t3
    add t1, t1, t6
    mul t6, a4, t3
    add t2, t2, t6
    addi t0, t0, 1 
    j loop_start

loop_end:


    # Epilogue

    
    ret

empty:
    li a0 75
    ret

TooShortStride:
    li a0 76
    ret

