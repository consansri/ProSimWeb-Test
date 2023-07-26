package extendable.archs.riscv

import extendable.components.*
import extendable.components.connected.*
import extendable.components.types.*

object RISCV {

    // PROCESSOR
    val MEM_INIT: String = "0"

    val REG_INIT: String = "0"
    val REG_SIZE = MutVal.Size.Bit32()
    val REG_ADDRESS_SIZE = MutVal.Size.Bit8()


    // Assembler CONFIG
    val asmConfig = AsmConfig(
        RISCVGrammarV1(),
        RISCVAssembly(RISCVBinMapper(), MutVal.Value.Hex("10000000", MutVal.Size.Bit32()))
    )

    // PROCESSOR CONFIG
    val config = Config(
        """RISC-V""",
        FileHandler("rvasm"),
        RegisterContainer(
            listOf(
                RegisterContainer.RegisterFile(
                    RegisterContainer.RegLabel.MAIN, "MAIN", arrayOf(
                        RegisterContainer.Register(MutVal.Value.Dec("0", REG_ADDRESS_SIZE), listOf("zero", "x0"), MutVal(REG_INIT, REG_SIZE), "hardwired zero", hardwire = true),
                        RegisterContainer.Register(MutVal.Value.Dec("1", REG_ADDRESS_SIZE), listOf("ra", "x1"), MutVal(REG_INIT, REG_SIZE), "return address"),
                        RegisterContainer.Register(MutVal.Value.Dec("2", REG_ADDRESS_SIZE), listOf("sp", "x2"), MutVal(REG_INIT, REG_SIZE), "stack pointer"),
                        RegisterContainer.Register(MutVal.Value.Dec("3", REG_ADDRESS_SIZE), listOf("gp", "x3"), MutVal(REG_INIT, REG_SIZE), "global pointer"),
                        RegisterContainer.Register(MutVal.Value.Dec("4", REG_ADDRESS_SIZE), listOf("tp", "x4"), MutVal(REG_INIT, REG_SIZE), "thread pointer"),
                        RegisterContainer.Register(MutVal.Value.Dec("5", REG_ADDRESS_SIZE), listOf("t0", "x5"), MutVal(REG_INIT, REG_SIZE), "temporary register 0"),
                        RegisterContainer.Register(MutVal.Value.Dec("6", REG_ADDRESS_SIZE), listOf("t1", "x6"), MutVal(REG_INIT, REG_SIZE), "temporary register 1"),
                        RegisterContainer.Register(MutVal.Value.Dec("7", REG_ADDRESS_SIZE), listOf("t2", "x7"), MutVal(REG_INIT, REG_SIZE), "temporary register 2"),
                        RegisterContainer.Register(MutVal.Value.Dec("8", REG_ADDRESS_SIZE), listOf("s0", "fp", "x8"), MutVal(REG_INIT, REG_SIZE), "saved register 0 / frame pointer"),
                        RegisterContainer.Register(MutVal.Value.Dec("9", REG_ADDRESS_SIZE), listOf("s1", "x9"), MutVal(REG_INIT, REG_SIZE), "saved register 1"),
                        RegisterContainer.Register(MutVal.Value.Dec("10", REG_ADDRESS_SIZE), listOf("a0", "x10"), MutVal(REG_INIT, REG_SIZE), "function argument 0 / return value 0"),
                        RegisterContainer.Register(MutVal.Value.Dec("11", REG_ADDRESS_SIZE), listOf("a1", "x11"), MutVal(REG_INIT, REG_SIZE), "function argument 1 / return value 1"),
                        RegisterContainer.Register(MutVal.Value.Dec("12", REG_ADDRESS_SIZE), listOf("a2", "x12"), MutVal(REG_INIT, REG_SIZE), "function argument 2"),
                        RegisterContainer.Register(MutVal.Value.Dec("13", REG_ADDRESS_SIZE), listOf("a3", "x13"), MutVal(REG_INIT, REG_SIZE), "function argument 3"),
                        RegisterContainer.Register(MutVal.Value.Dec("14", REG_ADDRESS_SIZE), listOf("a4", "x14"), MutVal(REG_INIT, REG_SIZE), "function argument 4"),
                        RegisterContainer.Register(MutVal.Value.Dec("15", REG_ADDRESS_SIZE), listOf("a5", "x15"), MutVal(REG_INIT, REG_SIZE), "function argument 5"),
                        RegisterContainer.Register(MutVal.Value.Dec("16", REG_ADDRESS_SIZE), listOf("a6", "x16"), MutVal(REG_INIT, REG_SIZE), "function argument 6"),
                        RegisterContainer.Register(MutVal.Value.Dec("17", REG_ADDRESS_SIZE), listOf("a7", "x17"), MutVal(REG_INIT, REG_SIZE), "function argument 7"),
                        RegisterContainer.Register(MutVal.Value.Dec("18", REG_ADDRESS_SIZE), listOf("s2", "x18"), MutVal(REG_INIT, REG_SIZE), "saved register 2"),
                        RegisterContainer.Register(MutVal.Value.Dec("19", REG_ADDRESS_SIZE), listOf("s3", "x19"), MutVal(REG_INIT, REG_SIZE), "saved register 3"),
                        RegisterContainer.Register(MutVal.Value.Dec("20", REG_ADDRESS_SIZE), listOf("s4", "x20"), MutVal(REG_INIT, REG_SIZE), "saved register 4"),
                        RegisterContainer.Register(MutVal.Value.Dec("21", REG_ADDRESS_SIZE), listOf("s5", "x21"), MutVal(REG_INIT, REG_SIZE), "saved register 5"),
                        RegisterContainer.Register(MutVal.Value.Dec("22", REG_ADDRESS_SIZE), listOf("s6", "x22"), MutVal(REG_INIT, REG_SIZE), "saved register 6"),
                        RegisterContainer.Register(MutVal.Value.Dec("23", REG_ADDRESS_SIZE), listOf("s7", "x23"), MutVal(REG_INIT, REG_SIZE), "saved register 7"),
                        RegisterContainer.Register(MutVal.Value.Dec("24", REG_ADDRESS_SIZE), listOf("s8", "x24"), MutVal(REG_INIT, REG_SIZE), "saved register 8"),
                        RegisterContainer.Register(MutVal.Value.Dec("25", REG_ADDRESS_SIZE), listOf("s9", "x25"), MutVal(REG_INIT, REG_SIZE), "saved register 9"),
                        RegisterContainer.Register(MutVal.Value.Dec("26", REG_ADDRESS_SIZE), listOf("s10", "x26"), MutVal(REG_INIT, REG_SIZE), "saved register 10"),
                        RegisterContainer.Register(MutVal.Value.Dec("27", REG_ADDRESS_SIZE), listOf("s11", "x27"), MutVal(REG_INIT, REG_SIZE), "saved register 11"),
                        RegisterContainer.Register(MutVal.Value.Dec("28", REG_ADDRESS_SIZE), listOf("t3", "x28"), MutVal(REG_INIT, REG_SIZE), "temporary register 3"),
                        RegisterContainer.Register(MutVal.Value.Dec("29", REG_ADDRESS_SIZE), listOf("t4", "x29"), MutVal(REG_INIT, REG_SIZE), "temporary register 4"),
                        RegisterContainer.Register(MutVal.Value.Dec("30", REG_ADDRESS_SIZE), listOf("t5", "x30"), MutVal(REG_INIT, REG_SIZE), "temporary register 5"),
                        RegisterContainer.Register(MutVal.Value.Dec("31", REG_ADDRESS_SIZE), listOf("t6", "x31"), MutVal(REG_INIT, REG_SIZE), "temporary register 6")
                    )
                )
            ),
            pcSize = MutVal.Size.Bit32()
        ),
        Memory(MutVal.Size.Bit32(), MEM_INIT, MutVal.Size.Bit8(), Memory.Endianess.LittleEndian),
        Transcript()
    )

    // EDITOR

    val FileExtension = "rvasm"
    val FileTypeDescription = "RISC-V Assembler"

}