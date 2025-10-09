.globl read_matrix

.text
# ==============================================================================
# FUNCTION: Allocates memory and reads in a binary file as a matrix of integers
#
# FILE FORMAT:
#   The first 8 bytes are two 4 byte ints representing the # of rows and columns
#   in the matrix. Every 4 bytes afterwards is an element of the matrix in
#   row-major order.
# Arguments:
#   a0 (char*) is the pointer to string representing the filename
#   a1 (int*)  is a pointer to an integer, we will set it to the number of rows
#   a2 (int*)  is a pointer to an integer, we will set it to the number of columns
# Returns:
#   a0 (int*)  is the pointer to the matrix in memory
# Exceptions:
# - If malloc returns an error,
#   this function terminates the program with error code 88.
# - If you receive an fopen error or eof, 
#   this function terminates the program with error code 90.
# - If you receive an fread error or eof,
#   this function terminates the program with error code 91.
# - If you receive an fclose error or eof,
#   this function terminates the program with error code 92.
# ==============================================================================
read_matrix:

    # Prologue

    addi sp, sp, -24
    sw s0, 0(sp)
    sw s1, 4(sp)
    sw s2, 8(sp)
    sw s3, 12(sp)
    sw s4, 16(sp)
    sw ra, 20(sp)
    mv s0, a0
    mv s1, a1  # s1: pointer to n
    mv s2, a2  # s2: pointer to m
               # s3: n
               # s4: m

    # fopen
    mv a1, s0
    li a2, 0   # read
    jal fopen
    mv t0, a0  # t0: file descriptor
    li t1, -1  # t1: -1
    beq t0, t1, fopen_err

    # read n, m
    addi sp, sp, -4
    sw t0, 0(sp)

    mv a1, t0
    mv a2, s1
    li a3, 4
    jal fread
    li t1, 4

    lw t0, 0(sp)
    addi sp, sp, 4
    bne a0, t1, fread_err

    addi sp, sp, -4
    sw t0, 0(sp)

    mv a1, t0
    mv a2, s2
    li a3, 4
    jal fread
    li t1, 4

    lw t0, 0(sp)
    addi sp, sp, 4
    bne a0, t1, fread_err

    lw s3, 0(s1)
    lw s4, 0(s2)

    # malloc
    addi sp, sp, -4
    sw t0, 0(sp)

    mul a0, s3, s4
    li t6, 4   # t6: 4
    mul a0, a0, t6
    jal malloc
    mv t4, a0  # t4: pointer to a[]

    lw t0, 0(sp)
    addi sp, sp, 4
    beq t4, zero, malloc_fail

    # fread
    addi sp, sp, -8
    sw t0, 0(sp)
    sw t4, 4(sp)

    mv a1, t0
    mv a2, t4
    li a3, 4
    mul a3, a3, s3
    mul a3, a3, s4  # n * m * 4 bytes

    addi sp, sp, -4
    sw a3, 0(sp)
    jal fread
    lw a3, 0(sp)
    addi sp, sp, 4

    lw t0, 0(sp)
    lw t4, 4(sp)
    addi sp, sp, 8
    bne a0, a3, fread_err

    # fclose
    addi sp, sp, -8
    sw t0, 0(sp)
    sw t4, 4(sp)

    mv a1, t0
    jal fclose

    lw t0, 0(sp)
    lw t4, 4(sp)
    addi sp, sp, 8
    li t1, -1
    beq a0, t1, fclose_err

    mv a0, t4
    # Epilogue

    lw s0, 0(sp)
    lw s1, 4(sp)
    lw s2, 8(sp)
    lw s3, 12(sp)
    lw s4, 16(sp)
    lw ra, 20(sp)
    addi sp, sp, 24

    ret

malloc_fail:
    li a1, 88
    jal exit2

fopen_err:
    li a1, 90
    jal exit2

fread_err:
    li a1, 91
    jal exit2

fclose_err:
    li a1, 92
    jal exit2
