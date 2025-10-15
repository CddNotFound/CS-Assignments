    addi s0, x0, 12
    addi s1, x0, 13

    
    j back1
br1:
    j back2
br2:
    j back3
br3:
    j back4
br4:
    j back5
br5:
    j back6
br6:
    j back7
br7:
    j back8
br8:
    j back9
br9:
    j back10
br10:
    j back11
br11:
    j back12
br12:
    j back13
br13:
    j back14
br14:
    j back15
br15:
    j back16
br16:
    j back17
br17:
    j back18
br18:
    add s0, s0, s1
    j end
    
back1:
    blt s0, s1, br1
back2:
    blt s1, s0, br2
back3:
    blt s0, s0, br3
back4:
    bge s0, s1, br4
back5:
    bge s1, s0, br5
back6:
    bge s0, s0, br6
back7:
    beq s0, s1, br7
back8:
    beq s1, s0, br8
back9:
    beq s0, s0, br9
back10:
    bne s0, s1, br10
back11:
    bne s1, s0, br11
back12:
    bne s0, s0, br12
back13:
    bltu s0, s1, br13
back14:
    bltu s1, s0, br14
back15:
    bltu s0, s0, br15
back16:
    bgeu s0, s1, br16
back17:
    bgeu s1, s0, br17
back18:
    bgeu s0, s0, br18

end: