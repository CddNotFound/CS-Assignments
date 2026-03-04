    j main

mul:
    mul a0, a0, a1
    addi t0, x0, -10
    addi t1, x0, -10
    addi t2, x0, -10
    ret

main:
    addi sp, sp, -8
    sw s0, 0(sp)
    sw ra, 4(sp)

    addi t2, x0, 10
    
    li t0, 0
outer_loop_begin:
    bge t0, t2, outer_loop_end

    li t1, 0
inner_loop_begin:
    bge t1, t2, inner_loop_end

    addi sp, sp, -12
    sw t0, 0(sp) 
    sw t1, 4(sp)
    sw t2, 8(sp)
    mv a0 t0, 
    mv a1 t1
    jal mul

    lw t0, 0(sp)
    lw t1, 4(sp)
    lw t2, 8(sp)
    addi sp, sp, 12
    add s0, s0, a0

    addi t1, t1, 1
    j inner_loop_begin
inner_loop_end:

    addi t0, t0, 1
    j outer_loop_begin
outer_loop_end:


    mv a0, s0

    lw s0, 0(sp)
    lw ra, 4(sp)
    addi sp, sp, 4