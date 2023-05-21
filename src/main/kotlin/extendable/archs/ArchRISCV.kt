package extendable.archs

import extendable.Architecture
import extendable.components.DataMemory
import extendable.components.ProgramMemory
import extendable.components.Register

class ArchRISCV : Architecture {

    constructor() : super("IKR RISC-V",
        ProgramMemory(4, 32, 32),
        DataMemory(32, 4),
        arrayOf(
            Register(0,"zero",0, "hardwired zero"),
            Register(1,"ra",0, "return address"),
            Register(2,"sp",0, "stack pointer"),
            Register(3, "gp",0,"global pointer"),
            Register(4, "tp",0,"thread pointer"),
            Register(5, "t0",0,"temporary register 0"),
            Register(6, "t1",0,"temporary register 1"),
            Register(7, "t2",0,"temporary register 2"),
            Register(8, "s0 / fp",0,"saved register 0 / frame pointer"),
            Register(9, "s1",0,"saved register 1"),
            Register(10, "a0",0,"function argument 0 / return value 0"),
            Register(11, "a1",0,"function argument 1 / return value 1"),
            Register(12, "a2",0,"function argument 2"),
            Register(13, "a3",0,"function argument 3"),
            Register(14, "a4",0,"function argument 4"),
            Register(15, "a5",0,"function argument 5"),
            Register(16, "a6",0,"function argument 6"),
            Register(17, "a7",0,"function argument 7"),
            Register(18, "s2",0,"saved register 2"),
            Register(19, "s3",0,"saved register 3"),
            Register(20, "s4",0,"saved register 4"),
            Register(21, "s5",0,"saved register 5"),
            Register(22, "s6",0,"saved register 6"),
            Register(23, "s7",0,"saved register 7"),
            Register(24, "s8",0,"saved register 8"),
            Register(25, "s9",0,"saved register 9"),
            Register(26, "s10",0,"saved register 10"),
            Register(27, "s11",0,"saved register 11"),
            Register(28, "t3",0,"temporary register 3"),
            Register(29, "t4",0,"temporary register 4"),
            Register(30, "t5",0,"temporary register 5"),
            Register(31, "t6",0,"temporary register 6")

        )) {

    }


}