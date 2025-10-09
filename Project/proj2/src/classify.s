.globl classify

.text
classify:
    # =====================================
    # COMMAND LINE ARGUMENTS
    # =====================================
    # Args:
    #   a0 (int)    argc
    #   a1 (char**) argv
    #   a2 (int)    print_classification, if this is zero, 
    #               you should print the classification. Otherwise,
    #               this function should not print ANYTHING.
    # Returns:
    #   a0 (int)    Classification
    # Exceptions:
    # - If there are an incorrect number of command line args,
    #   this function terminates the program with exit code 89.
    # - If malloc fails, this function terminats the program with exit code 88.
    #
    # Usage:
    #   main.s <M0_PATH> <M1_PATH> <INPUT_PATH> <OUTPUT_PATH>

begin:
    addi sp, sp, -36
    sw s0, 0(sp)
    sw s1, 4(sp)
    sw s2, 8(sp)
    sw s3, 12(sp)
    sw s4, 16(sp)
    sw s5, 20(sp)
    sw s6, 24(sp)
    sw s7, 28(sp)
    sw ra, 32(sp)
    mv s0, a0   # s0: argc
    mv s1, a1   # s1: argv
    mv s2, a2   #
                # s3: pointer to m0
                # s4: pointer to m1
                # s5: pointer to input
                # s6: temporary result1
                # s7: temporary result2

	# =====================================
    # LOAD MATRICES
    # =====================================

    addi sp, sp, -24   # space to 3 pairs of (row, col)

    # Load pretrained m0
    lw a0, 4(s1)
    addi a1, sp, 0
    addi a2, sp, 4
    jal read_matrix
    mv s3, a0


    # Load pretrained m1
    lw a0, 8(s1)
    addi a1, sp, 8
    addi a2, sp, 12
    jal read_matrix
    mv s4, a0


    # Load input matrix

    lw a0, 12(s1)
    addi a1, sp, 16
    addi a2, sp, 20
    jal read_matrix
    mv s5, a0

    # =====================================
    # RUN LAYERS
    # =====================================
    # 1. LINEAR LAYER:    m0 * input
    # 2. NONLINEAR LAYER: ReLU(m0 * input)
    # 3. LINEAR LAYER:    m1 * ReLU(m0 * input)

    # malloc result matrix
    lw t0, 0(sp)
    lw t1, 20(sp)
    mul a0, t0, t1
    slli a0, a0, 2
    jal malloc
    mv s6, a0
    beq s6, zero, malloc_fault
    lw t0, 8(sp)
    lw t1, 20(sp)
    mul a0, t0, t1
    slli a0, a0, 2
    jal malloc
    mv s7, a0
    beq s7, zero, malloc_fault


    # 1. m0 * input
    mv a0, s3
    lw a1, 0(sp)
    lw a2, 4(sp)
    mv a3, s5
    lw a4, 16(sp)
    lw a5, 20(sp)
    mv a6, s6
    jal matmul  


    # 2. call relu
    lw t0, 0(sp)    # row of (m0 * input)
    lw t1, 20(sp)   # col of (m0 * input) 
    mv a0, s6
    mul a1, t0, t1
    jal relu


    # 3. m1 * ReLU
    mv a0, s4
    lw a1, 8(sp)
    lw a2, 12(sp)
    mv a3, s6
    lw a4, 0(sp)
    lw a5, 20(sp)
    mv a6, s7
    jal matmul 


    # =====================================
    # WRITE OUTPUT
    # =====================================
    # Write output matrix

    lw a0, 16(s1)
    mv a1, s7
    lw a2, 8(sp)
    lw a3, 20(sp)
    jal write_matrix


    # =====================================
    # CALCULATE CLASSIFICATION/LABEL
    # =====================================
    # Call argmax

    lw t0, 8(sp)
    lw t1, 20(sp)
    mv a0, s7
    mul a1, t0, t1
    jal argmax
    mv t2, a0   # t2: position
    

    # Print classification

    bne s2, zero, end
    mv a1, t2
    jal print_int

    # Print newline afterwards for clarity

end:

    # free s6 & s7
    mv a0, s6
    jal free
    mv a0, s7
    jal free

    addi sp, sp, 24
    lw s0, 0(sp)
    lw s1, 4(sp)
    lw s2, 8(sp)
    lw s3, 12(sp)
    lw s4, 16(sp)
    lw s5, 20(sp)
    lw s6, 24(sp)
    lw s7, 28(sp)
    lw ra, 32(sp)
    addi sp, sp, 36


    ret

malloc_fault:
    li a1, 88
    call exit2

arg_not_match:
    li a1, 89
    call exit2
