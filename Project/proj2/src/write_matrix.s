.globl write_matrix

.text
# ==============================================================================
# FUNCTION: Writes a matrix of integers into a binary file
# FILE FORMAT:
#   The first 8 bytes of the file will be two 4 byte ints representing the
#   numbers of rows and columns respectively. Every 4 bytes thereafter is an
#   element of the matrix in row-major order.
# Arguments:
#   a0 (char*) is the pointer to string representing the filename
#   a1 (int*)  is the pointer to the start of the matrix in memory
#   a2 (int)   is the number of rows in the matrix
#   a3 (int)   is the number of columns in the matrix
# Returns:
#   None
# Exceptions:
# - If you receive an fopen error or eof,
#   this function terminates the program with error code 93.
# - If you receive an fwrite error or eof,
#   this function terminates the program with error code 94.
# - If you receive an fclose error or eof,
#   this function terminates the program with error code 95.
# ==============================================================================
write_matrix:

    # Prologue

    addi sp, sp, -24
    sw s0, 0(sp)
    sw s1, 4(sp)
    sw s2, 8(sp)
    sw s3, 12(sp)
    sw s4, 16(sp)
    sw ra, 20(sp)
    mv s0, a0  # s0: filename
    mv s1, a1  # s1: *a
    mv s2, a2  # s2: n
    mv s3, a3  # s3: m

    # fopen
    mv a1, s0
    li a2, 1   # write
    jal fopen
    mv s4, a0  # s4: file descriptor
    li t1, -1  # t1: -1
    beq s4, t1, fopen_err

    # write n, m
    addi sp, sp, -4
    sw s2, 0(sp)
    mv a1, s4
    mv a2, sp
    li a3, 1
    li a4, 4
    jal fwrite
    addi sp, sp, 4
    li t1, 1
    blt a0, t1, fwrite_err
    
    addi sp, sp, -4
    sw s3, 0(sp)
    mv a1, s4
    mv a2, sp
    li a3, 1
    li a4, 4
    jal fwrite
    addi sp, sp, 4
    li t1, 1
    blt a0, t1, fwrite_err

    # write a[][]
    mv a1, s4
    mv a2, s1
    mul a3, s2, s3
    li a4, 4
    jal fwrite
    mul t1, s2, s3
    blt a0, t1, fwrite_err

    # fclose
    mv a1, s4
    jal fclose
    li t1, -1
    beq a0, t1, fclose_err

    # Epilogue

    mv a0, zero
    lw s0, 0(sp)
    lw s1, 4(sp)
    lw s2, 8(sp)
    lw s3, 12(sp)
    lw s4, 16(sp)
    lw ra, 20(sp)
    addi sp, sp, 24

    ret

fopen_err:
    li a1, 93
    jal exit2

fwrite_err:
    li a1, 94
    jal exit2

fclose_err:
    li a1, 95
    jal exit2