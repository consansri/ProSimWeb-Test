package extendable.archs.riscv

import extendable.components.*
import extendable.components.connected.*
import extendable.components.types.*

object RISCV {

    // PROCESSOR
    val MEM_INIT: String = "0"

    val REG_INIT: String = "0"
    val REG_SIZE = ByteValue.Size.Bit32()
    val REG_ADDRESS_SIZE = ByteValue.Size.Bit8()


    // Assembler CONFIG
    val asmConfig = AsmConfig(
        RISCVGrammar(),
        RISCVAssembly(RISCVBinMapper())
    )


    // PROCESSOR CONFIG
    val config = Config(
        """RISC-V""",
        RegisterContainer(
            listOf(
                RegisterContainer.RegisterFile(
                    RegisterContainer.RegLabel.PC, "PC", arrayOf(RegisterContainer.Register(ByteValue.Type.Dec("-1", REG_ADDRESS_SIZE), "pc", ByteValue(REG_INIT, REG_SIZE), "program counter"))
                ),

                RegisterContainer.RegisterFile(
                    RegisterContainer.RegLabel.MAIN, "MAIN", arrayOf(
                        RegisterContainer.Register(ByteValue.Type.Dec("0", REG_ADDRESS_SIZE), "zero", ByteValue(REG_INIT, REG_SIZE), "hardwired zero"),
                        RegisterContainer.Register(ByteValue.Type.Dec("1", REG_ADDRESS_SIZE), "ra", ByteValue(REG_INIT, REG_SIZE), "return address"),
                        RegisterContainer.Register(ByteValue.Type.Dec("2", REG_ADDRESS_SIZE), "sp", ByteValue(REG_INIT, REG_SIZE), "stack pointer"),
                        RegisterContainer.Register(ByteValue.Type.Dec("3", REG_ADDRESS_SIZE), "gp", ByteValue(REG_INIT, REG_SIZE), "global pointer"),
                        RegisterContainer.Register(ByteValue.Type.Dec("4", REG_ADDRESS_SIZE), "tp", ByteValue(REG_INIT, REG_SIZE), "thread pointer"),
                        RegisterContainer.Register(ByteValue.Type.Dec("5", REG_ADDRESS_SIZE), "t0", ByteValue(REG_INIT, REG_SIZE), "temporary register 0"),
                        RegisterContainer.Register(ByteValue.Type.Dec("6", REG_ADDRESS_SIZE), "t1", ByteValue(REG_INIT, REG_SIZE), "temporary register 1"),
                        RegisterContainer.Register(ByteValue.Type.Dec("7", REG_ADDRESS_SIZE), "t2", ByteValue(REG_INIT, REG_SIZE), "temporary register 2"),
                        RegisterContainer.Register(ByteValue.Type.Dec("8", REG_ADDRESS_SIZE), "s0 / fp", ByteValue(REG_INIT, REG_SIZE), "saved register 0 / frame pointer"),
                        RegisterContainer.Register(ByteValue.Type.Dec("9", REG_ADDRESS_SIZE), "s1", ByteValue(REG_INIT, REG_SIZE), "saved register 1"),
                        RegisterContainer.Register(ByteValue.Type.Dec("10", REG_ADDRESS_SIZE), "a0", ByteValue(REG_INIT, REG_SIZE), "function argument 0 / return value 0"),
                        RegisterContainer.Register(ByteValue.Type.Dec("11", REG_ADDRESS_SIZE), "a1", ByteValue(REG_INIT, REG_SIZE), "function argument 1 / return value 1"),
                        RegisterContainer.Register(ByteValue.Type.Dec("12", REG_ADDRESS_SIZE), "a2", ByteValue(REG_INIT, REG_SIZE), "function argument 2"),
                        RegisterContainer.Register(ByteValue.Type.Dec("13", REG_ADDRESS_SIZE), "a3", ByteValue(REG_INIT, REG_SIZE), "function argument 3"),
                        RegisterContainer.Register(ByteValue.Type.Dec("14", REG_ADDRESS_SIZE), "a4", ByteValue(REG_INIT, REG_SIZE), "function argument 4"),
                        RegisterContainer.Register(ByteValue.Type.Dec("15", REG_ADDRESS_SIZE), "a5", ByteValue(REG_INIT, REG_SIZE), "function argument 5"),
                        RegisterContainer.Register(ByteValue.Type.Dec("16", REG_ADDRESS_SIZE), "a6", ByteValue(REG_INIT, REG_SIZE), "function argument 6"),
                        RegisterContainer.Register(ByteValue.Type.Dec("17", REG_ADDRESS_SIZE), "a7", ByteValue(REG_INIT, REG_SIZE), "function argument 7"),
                        RegisterContainer.Register(ByteValue.Type.Dec("18", REG_ADDRESS_SIZE), "s2", ByteValue(REG_INIT, REG_SIZE), "saved register 2"),
                        RegisterContainer.Register(ByteValue.Type.Dec("19", REG_ADDRESS_SIZE), "s3", ByteValue(REG_INIT, REG_SIZE), "saved register 3"),
                        RegisterContainer.Register(ByteValue.Type.Dec("20", REG_ADDRESS_SIZE), "s4", ByteValue(REG_INIT, REG_SIZE), "saved register 4"),
                        RegisterContainer.Register(ByteValue.Type.Dec("21", REG_ADDRESS_SIZE), "s5", ByteValue(REG_INIT, REG_SIZE), "saved register 5"),
                        RegisterContainer.Register(ByteValue.Type.Dec("22", REG_ADDRESS_SIZE), "s6", ByteValue(REG_INIT, REG_SIZE), "saved register 6"),
                        RegisterContainer.Register(ByteValue.Type.Dec("23", REG_ADDRESS_SIZE), "s7", ByteValue(REG_INIT, REG_SIZE), "saved register 7"),
                        RegisterContainer.Register(ByteValue.Type.Dec("24", REG_ADDRESS_SIZE), "s8", ByteValue(REG_INIT, REG_SIZE), "saved register 8"),
                        RegisterContainer.Register(ByteValue.Type.Dec("25", REG_ADDRESS_SIZE), "s9", ByteValue(REG_INIT, REG_SIZE), "saved register 9"),
                        RegisterContainer.Register(ByteValue.Type.Dec("26", REG_ADDRESS_SIZE), "s10", ByteValue(REG_INIT, REG_SIZE), "saved register 10"),
                        RegisterContainer.Register(ByteValue.Type.Dec("27", REG_ADDRESS_SIZE), "s11", ByteValue(REG_INIT, REG_SIZE), "saved register 11"),
                        RegisterContainer.Register(ByteValue.Type.Dec("28", REG_ADDRESS_SIZE), "t3", ByteValue(REG_INIT, REG_SIZE), "temporary register 3"),
                        RegisterContainer.Register(ByteValue.Type.Dec("29", REG_ADDRESS_SIZE), "t4", ByteValue(REG_INIT, REG_SIZE), "temporary register 4"),
                        RegisterContainer.Register(ByteValue.Type.Dec("30", REG_ADDRESS_SIZE), "t5", ByteValue(REG_INIT, REG_SIZE), "temporary register 5"),
                        RegisterContainer.Register(ByteValue.Type.Dec("31", REG_ADDRESS_SIZE), "t6", ByteValue(REG_INIT, REG_SIZE), "temporary register 6")
                    )
                )
            )
        ),
        Memory(ByteValue.Size.Bit32(), MEM_INIT, ByteValue.Size.Bit8(), Memory.Endianess.LittleEndian),
        Transcript()
    )

    // EDITOR

    val FileExtension = "rvasm"
    val FileTypeDescription = "RISC-V Assembler"

}