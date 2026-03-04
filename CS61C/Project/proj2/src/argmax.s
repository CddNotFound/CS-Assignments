.globl argmax

.text
# =================================================================
# FUNCTION: Given a int vector, return the index of the largest
#	element. If there are multiple, return the one
#	with the smallest index.
# Arguments:
# 	a0 (int*) is the pointer to the start of the vector
#	a1 (int)  is the # of elements in the vector
# Returns:
#	a0 (int)  is the first index of the largest element
# Exceptions:
# - If the length of the vector is less than 1,
#   this function terminates the program with error code 77.
# =================================================================
argmax:

    # Prologue

    addi t0, zero, 1
    blt a1, t0, empty

    li t0, 0           # i
    mv t1, a0  # a[i]
    li a0, 0           # maxPos
    li t2, -10000      # maxValue

loop_start:
    bge t0, a1, loop_end
    lw t3, 0(t1)       # a[i]
    bge t2, t3, loop_continue
    mv a0, t0
    mv t2, t3

loop_continue:
    addi t0, t0, 1
    addi t1, t1, 4
    j loop_start

loop_end:

    # Epilogue

    ret

empty:
    addi a0, zero, 77
    ret 