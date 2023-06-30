package extendable.archs.riscv

import extendable.ArchConst
import extendable.Architecture
import extendable.components.*
import extendable.components.assembly.Assembly
import extendable.components.assembly.Grammar
import extendable.components.connected.*
import extendable.components.types.*

object RISCV {

    // PROCESSOR
    val REGISTER_PC_WIDTH = 32

    val INSTRUCTION_WIDTH = 32

    val MEMORY_WORD_BYTES = 1
    val MEMORY_ADDRESS_WIDTH = 32
    val MEM_INIT: String = "0"

    val REG_INIT: String = "0"
    val REG_BYTES = 4


    // Assembler CONFIG
    val asmConfig = AsmConfig(
        RISCVGrammar()
    )


    // PROCESSOR CONFIG
    val config = Config(
        """RISC-V""",
        RegisterContainer(
            listOf(
                RegisterContainer.RegisterFile(
                    RegisterContainer.RegLabel.PC, "PC", arrayOf(RegisterContainer.Register(Address(ArchConst.ADDRESS_NOVALUE, 32), "pc", ByteValue(REG_INIT, REG_BYTES), "program counter"))
                ),

                RegisterContainer.RegisterFile(
                    RegisterContainer.RegLabel.MAIN, "MAIN", arrayOf(
                        RegisterContainer.Register(Address(0, 32), "zero", ByteValue(REG_INIT, REG_BYTES), "hardwired zero"),
                        RegisterContainer.Register(Address(1, 32), "ra", ByteValue(REG_INIT, REG_BYTES), "return address"),
                        RegisterContainer.Register(Address(2, 32), "sp", ByteValue(REG_INIT, REG_BYTES), "stack pointer"),
                        RegisterContainer.Register(Address(3, 32), "gp", ByteValue(REG_INIT, REG_BYTES), "global pointer"),
                        RegisterContainer.Register(Address(4, 32), "tp", ByteValue(REG_INIT, REG_BYTES), "thread pointer"),
                        RegisterContainer.Register(Address(5, 32), "t0", ByteValue(REG_INIT, REG_BYTES), "temporary register 0"),
                        RegisterContainer.Register(Address(6, 32), "t1", ByteValue(REG_INIT, REG_BYTES), "temporary register 1"),
                        RegisterContainer.Register(Address(7, 32), "t2", ByteValue(REG_INIT, REG_BYTES), "temporary register 2"),
                        RegisterContainer.Register(Address(8, 32), "s0 / fp", ByteValue(REG_INIT, REG_BYTES), "saved register 0 / frame pointer"),
                        RegisterContainer.Register(Address(9, 32), "s1", ByteValue(REG_INIT, REG_BYTES), "saved register 1"),
                        RegisterContainer.Register(Address(10, 32), "a0", ByteValue(REG_INIT, REG_BYTES), "function argument 0 / return value 0"),
                        RegisterContainer.Register(Address(11, 32), "a1", ByteValue(REG_INIT, REG_BYTES), "function argument 1 / return value 1"),
                        RegisterContainer.Register(Address(12, 32), "a2", ByteValue(REG_INIT, REG_BYTES), "function argument 2"),
                        RegisterContainer.Register(Address(13, 32), "a3", ByteValue(REG_INIT, REG_BYTES), "function argument 3"),
                        RegisterContainer.Register(Address(14, 32), "a4", ByteValue(REG_INIT, REG_BYTES), "function argument 4"),
                        RegisterContainer.Register(Address(15, 32), "a5", ByteValue(REG_INIT, REG_BYTES), "function argument 5"),
                        RegisterContainer.Register(Address(16, 32), "a6", ByteValue(REG_INIT, REG_BYTES), "function argument 6"),
                        RegisterContainer.Register(Address(17, 32), "a7", ByteValue(REG_INIT, REG_BYTES), "function argument 7"),
                        RegisterContainer.Register(Address(18, 32), "s2", ByteValue(REG_INIT, REG_BYTES), "saved register 2"),
                        RegisterContainer.Register(Address(19, 32), "s3", ByteValue(REG_INIT, REG_BYTES), "saved register 3"),
                        RegisterContainer.Register(Address(20, 32), "s4", ByteValue(REG_INIT, REG_BYTES), "saved register 4"),
                        RegisterContainer.Register(Address(21, 32), "s5", ByteValue(REG_INIT, REG_BYTES), "saved register 5"),
                        RegisterContainer.Register(Address(22, 32), "s6", ByteValue(REG_INIT, REG_BYTES), "saved register 6"),
                        RegisterContainer.Register(Address(23, 32), "s7", ByteValue(REG_INIT, REG_BYTES), "saved register 7"),
                        RegisterContainer.Register(Address(24, 32), "s8", ByteValue(REG_INIT, REG_BYTES), "saved register 8"),
                        RegisterContainer.Register(Address(25, 32), "s9", ByteValue(REG_INIT, REG_BYTES), "saved register 9"),
                        RegisterContainer.Register(Address(26, 32), "s10", ByteValue(REG_INIT, REG_BYTES), "saved register 10"),
                        RegisterContainer.Register(Address(27, 32), "s11", ByteValue(REG_INIT, REG_BYTES), "saved register 11"),
                        RegisterContainer.Register(Address(28, 32), "t3", ByteValue(REG_INIT, REG_BYTES), "temporary register 3"),
                        RegisterContainer.Register(Address(29, 32), "t4", ByteValue(REG_INIT, REG_BYTES), "temporary register 4"),
                        RegisterContainer.Register(Address(30, 32), "t5", ByteValue(REG_INIT, REG_BYTES), "temporary register 5"),
                        RegisterContainer.Register(Address(31, 32), "t6", ByteValue(REG_INIT, REG_BYTES), "temporary register 6")
                    )
                )
            )
        ),
        Memory(MEMORY_ADDRESS_WIDTH, MEM_INIT, MEMORY_WORD_BYTES, Memory.Endianess.LittleEndian),
        Transcript(arrayOf("Address", "Line", "Code", "Labels", "Instruction"))
    )

    // EDITOR

    val FileExtension = "rvasm"
    val FileTypeDescription = "RISC-V Assembler"

}