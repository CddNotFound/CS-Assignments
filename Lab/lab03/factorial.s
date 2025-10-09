.globl factorial

.data
n: .word 8

.text
main:
    la t0, n
    lw a0, 0(t0)
    jal ra, factorial

    addi a1, a0, 0
    addi a0, x0, 1
    ecall # Print Result

    addi a1, x0, '\n'
    addi a0, x0, 11
    ecall # Print newline

    addi a0, x0, 10
    ecall # Exit

factorial:
    # YOUR CODE HERE

    beq a0, zero, return1

    addi sp, sp, -8
    sw s0, 0(sp)   # s0: n
    sw ra, 4(sp)
    mv s0, a0

    li t0, 1       # t0: i
    li a0, 1
factorial_loop_begin:
    blt s0, t0, factorial_loop_end
    mul a0, a0, t0
    addi t0, t0, 1
    j factorial_loop_begin

factorial_loop_end:

    lw s0, 0(sp)
    lw ra, 4(sp)
    addi sp, sp, 8
    ret

return1:
    li a0, 1
    ret