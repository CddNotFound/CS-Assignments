.globl relu

.text
# ==============================================================================
# FUNCTION: Performs an inplace element-wise ReLU on an array of ints
# Arguments:
# 	a0 (int*) is the pointer to the array
#	a1 (int)  is the # of elements in the array
# Returns:
#	None
# Exceptions:
# - If the length of the vector is less than 1,
#   this function terminates the program with error code 78.
# ==============================================================================
relu:
    # Prologue
    # addi, sp, sp, -8
    # sw s0, 0(sp)
    # sw s1, 4(sp)


    addi t0, zero, 1
    blt a1, t0, empty

    addi t1, zero 0  # i
    mv t2, a0        # pointer to a[i]

loop_start:
    bge t1, a1, loop_end
    lw t3, 0(t2)
    bge t3, zero, loop_continue
    sw zero, 0(t2)

loop_continue:
    addi t1, t1, 1  # ++i
    addi t2, t2, 4
    j loop_start

loop_end:
    # Epilogue
    # lw s0, 0(sp)
    # lw s1, 4(sp)
    # addi sp, sp, 8

    ret
    
empty:
    addi a0, zero, 78
    # lw s0, 0(sp)
    # lw s1, 4(sp)
    # addi sp, sp, 8
	ret
