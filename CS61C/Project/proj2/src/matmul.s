.globl matmul

.text
# =======================================================
# FUNCTION: Matrix Multiplication of 2 integer matrices
# 	d = matmul(m0, m1)
# Arguments:
# 	a0 (int*)  is the pointer to the start of m0 
#	a1 (int)   is the # of rows (height) of m0
#	a2 (int)   is the # of columns (width) of m0
#	a3 (int*)  is the pointer to the start of m1
# 	a4 (int)   is the # of rows (height) of m1
#	a5 (int)   is the # of columns (width) of m1
#	a6 (int*)  is the pointer to the the start of d
# Returns:
#	None (void), sets d = matmul(m0, m1)
# Exceptions:
#   Make sure to check in top to bottom order!
#   - If the dimensions of m0 do not make sense,
#     this function terminates the program with exit code 72.
#   - If the dimensions of m1 do not make sense,
#     this function terminates the program with exit code 73.
#   - If the dimensions of m0 and m1 don't match,
#     this function terminates the program with exit code 74.
# =======================================================
matmul:
    # Error checks 
    li t0, 1
    blt a1, t0, m0NoSense
    blt a2, t0, m0NoSense
    blt a4, t0, m1NoSense
    blt a5, t0, m1NoSense
    bne a2, a4, NotMatch

    # Prologue

    addi sp, sp, -32
    sw s0, 0(sp)
    sw s1, 4(sp)
    sw s2, 8(sp)
    sw s3, 12(sp)
    sw s4, 16(sp)
    sw s5, 20(sp)
    sw s6, 24(sp)
    sw ra, 28(sp)
    mv s0, a0
    mv s1, a1
    mv s2, a2
    mv s3, a3
    mv s4, a4
    mv s5, a5
    mv s6, a6

    li t0, 0           # i    
                       # s2: length of vector
    li t2, 1           # t2: stride of m0
                       # s5: stride of m1
outer_loop_start:
    bge t0, s1, outer_loop_end

    li t1, 0           # j
inner_loop_start:
    bge t1, s5, inner_loop_end

    # save temporaries
    addi sp, sp, -12
    sw t0, 0(sp)
    sw t1, 4(sp)
    sw t2, 8(sp)

    li t6, 4
    mul t3, t0, s2
    mul t3, t3, t6
    add a0, s0, t3  # vector of m0
    mul t3, t2, t1
    mul t3, t3, t6
    add a1, s3, t3  # vector of m1
    mv a2, s2
    mv a3, t2
    mv a4, s5

    jal dot
    
    # load temporaries
    lw t0, 0(sp)
    lw t1, 4(sp)
    lw t2, 8(sp)
    addi sp, sp, 12

    mul t3, t0, s5  # i * col
    mul t4, t1, t2  # j * 1
    li t6, 4
    add t3, t3, t4
    mul t3, t3, t6
    mv t5, s6
    add t5, t5, t3  # t5: pointer to d[i][j]
    #sw a0, 0(t5)
    sw a0, 0(t5)

    addi t1, t1, 1  # ++j
    j inner_loop_start

inner_loop_end:

    addi t0, t0, 1  # ++i
    j outer_loop_start
outer_loop_end:

    # Epilogue
    lw s0, 0(sp)
    lw s1, 4(sp)
    lw s2, 8(sp)
    lw s3, 12(sp)
    lw s4, 16(sp)
    lw s5, 20(sp)
    lw s6, 24(sp)
    lw ra, 28(sp)
    addi sp, sp, 32
    
    ret

m0NoSense:
    li a0, 72
    ret

m1NoSense:
    li a0, 73
    ret

NotMatch:
    li a0, 74
    ret

