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
                    RegisterContainer.RegLabel.PC, "PC", arrayOf(RegisterContainer.Register(ByteValue.Type.Dec("-1", REG_ADDRESS_SIZE), listOf("pc"), ByteValue(REG_INIT, REG_SIZE), "program counter"))
                ),

                RegisterContainer.RegisterFile(
                    RegisterContainer.RegLabel.MAIN, "MAIN", arrayOf(
                        RegisterContainer.Register(ByteValue.Type.Dec("0", REG_ADDRESS_SIZE), listOf("zero"), ByteValue(REG_INIT, REG_SIZE), "hardwired zero"),
                        RegisterContainer.Register(ByteValue.Type.Dec("1", REG_ADDRESS_SIZE), listOf("ra"), ByteValue(REG_INIT, REG_SIZE), "return address"),
                        RegisterContainer.Register(ByteValue.Type.Dec("2", REG_ADDRESS_SIZE), listOf("sp"), ByteValue(REG_INIT, REG_SIZE), "stack pointer"),
                        RegisterContainer.Register(ByteValue.Type.Dec("3", REG_ADDRESS_SIZE), listOf("gp"), ByteValue(REG_INIT, REG_SIZE), "global pointer"),
                        RegisterContainer.Register(ByteValue.Type.Dec("4", REG_ADDRESS_SIZE), listOf("tp"), ByteValue(REG_INIT, REG_SIZE), "thread pointer"),
                        RegisterContainer.Register(ByteValue.Type.Dec("5", REG_ADDRESS_SIZE), listOf("t0"), ByteValue(REG_INIT, REG_SIZE), "temporary register 0"),
                        RegisterContainer.Register(ByteValue.Type.Dec("6", REG_ADDRESS_SIZE), listOf("t1"), ByteValue(REG_INIT, REG_SIZE), "temporary register 1"),
                        RegisterContainer.Register(ByteValue.Type.Dec("7", REG_ADDRESS_SIZE), listOf("t2"), ByteValue(REG_INIT, REG_SIZE), "temporary register 2"),
                        RegisterContainer.Register(ByteValue.Type.Dec("8", REG_ADDRESS_SIZE), listOf("s0","fp"), ByteValue(REG_INIT, REG_SIZE), "saved register 0 / frame pointer"),
                        RegisterContainer.Register(ByteValue.Type.Dec("9", REG_ADDRESS_SIZE), listOf("s1"), ByteValue(REG_INIT, REG_SIZE), "saved register 1"),
                        RegisterContainer.Register(ByteValue.Type.Dec("10", REG_ADDRESS_SIZE), listOf("a0"), ByteValue(REG_INIT, REG_SIZE), "function argument 0 / return value 0"),
                        RegisterContainer.Register(ByteValue.Type.Dec("11", REG_ADDRESS_SIZE), listOf("a1"), ByteValue(REG_INIT, REG_SIZE), "function argument 1 / return value 1"),
                        RegisterContainer.Register(ByteValue.Type.Dec("12", REG_ADDRESS_SIZE), listOf("a2"), ByteValue(REG_INIT, REG_SIZE), "function argument 2"),
                        RegisterContainer.Register(ByteValue.Type.Dec("13", REG_ADDRESS_SIZE), listOf("a3"), ByteValue(REG_INIT, REG_SIZE), "function argument 3"),
                        RegisterContainer.Register(ByteValue.Type.Dec("14", REG_ADDRESS_SIZE), listOf("a4"), ByteValue(REG_INIT, REG_SIZE), "function argument 4"),
                        RegisterContainer.Register(ByteValue.Type.Dec("15", REG_ADDRESS_SIZE), listOf("a5"), ByteValue(REG_INIT, REG_SIZE), "function argument 5"),
                        RegisterContainer.Register(ByteValue.Type.Dec("16", REG_ADDRESS_SIZE), listOf("a6"), ByteValue(REG_INIT, REG_SIZE), "function argument 6"),
                        RegisterContainer.Register(ByteValue.Type.Dec("17", REG_ADDRESS_SIZE), listOf("a7"), ByteValue(REG_INIT, REG_SIZE), "function argument 7"),
                        RegisterContainer.Register(ByteValue.Type.Dec("18", REG_ADDRESS_SIZE), listOf("s2"), ByteValue(REG_INIT, REG_SIZE), "saved register 2"),
                        RegisterContainer.Register(ByteValue.Type.Dec("19", REG_ADDRESS_SIZE), listOf("s3"), ByteValue(REG_INIT, REG_SIZE), "saved register 3"),
                        RegisterContainer.Register(ByteValue.Type.Dec("20", REG_ADDRESS_SIZE), listOf("s4"), ByteValue(REG_INIT, REG_SIZE), "saved register 4"),
                        RegisterContainer.Register(ByteValue.Type.Dec("21", REG_ADDRESS_SIZE), listOf("s5"), ByteValue(REG_INIT, REG_SIZE), "saved register 5"),
                        RegisterContainer.Register(ByteValue.Type.Dec("22", REG_ADDRESS_SIZE), listOf("s6"), ByteValue(REG_INIT, REG_SIZE), "saved register 6"),
                        RegisterContainer.Register(ByteValue.Type.Dec("23", REG_ADDRESS_SIZE), listOf("s7"), ByteValue(REG_INIT, REG_SIZE), "saved register 7"),
                        RegisterContainer.Register(ByteValue.Type.Dec("24", REG_ADDRESS_SIZE), listOf("s8"), ByteValue(REG_INIT, REG_SIZE), "saved register 8"),
                        RegisterContainer.Register(ByteValue.Type.Dec("25", REG_ADDRESS_SIZE), listOf("s9"), ByteValue(REG_INIT, REG_SIZE), "saved register 9"),
                        RegisterContainer.Register(ByteValue.Type.Dec("26", REG_ADDRESS_SIZE), listOf("s10"), ByteValue(REG_INIT, REG_SIZE), "saved register 10"),
                        RegisterContainer.Register(ByteValue.Type.Dec("27", REG_ADDRESS_SIZE), listOf("s11"), ByteValue(REG_INIT, REG_SIZE), "saved register 11"),
                        RegisterContainer.Register(ByteValue.Type.Dec("28", REG_ADDRESS_SIZE), listOf("t3"), ByteValue(REG_INIT, REG_SIZE), "temporary register 3"),
                        RegisterContainer.Register(ByteValue.Type.Dec("29", REG_ADDRESS_SIZE), listOf("t4"), ByteValue(REG_INIT, REG_SIZE), "temporary register 4"),
                        RegisterContainer.Register(ByteValue.Type.Dec("30", REG_ADDRESS_SIZE), listOf("t5"), ByteValue(REG_INIT, REG_SIZE), "temporary register 5"),
                        RegisterContainer.Register(ByteValue.Type.Dec("31", REG_ADDRESS_SIZE), listOf("t6"), ByteValue(REG_INIT, REG_SIZE), "temporary register 6")
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