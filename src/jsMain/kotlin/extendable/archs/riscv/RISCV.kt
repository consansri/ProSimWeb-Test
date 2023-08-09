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

    enum class TS_COMPILED_HEADERS {
        addr,
        label,
        instr,
        params
    }

    enum class TS_DISASSEMBLED_HEADERS {
        addr,
        label,
        instr,
        params
    }

    // Assembler CONFIG
    val asmConfig = AsmConfig(
        RISCVGrammar(),
        RISCVAssembly(RISCVBinMapper(), MutVal.Value.Hex("00001000", MutVal.Size.Bit32()))
    )

    // PROCESSOR CONFIG
    val config = Config(
        """RISC-V""",
        FileHandler("rvasm"),
        RegisterContainer(
            listOf(
                RegisterContainer.RegisterFile(
                    "main", arrayOf(
                        RegisterContainer.Register(MutVal.Value.Dec("0", REG_ADDRESS_SIZE), listOf("x0"), listOf("zero"), MutVal(REG_INIT, REG_SIZE), "hardwired zero", hardwire = true),
                        RegisterContainer.Register(MutVal.Value.Dec("1", REG_ADDRESS_SIZE), listOf("x1"), listOf("ra"), MutVal(REG_INIT, REG_SIZE), "return address"),
                        RegisterContainer.Register(MutVal.Value.Dec("2", REG_ADDRESS_SIZE), listOf("x2"), listOf("sp"), MutVal(REG_INIT, REG_SIZE), "stack pointer"),
                        RegisterContainer.Register(MutVal.Value.Dec("3", REG_ADDRESS_SIZE), listOf("x3"), listOf("gp"), MutVal(REG_INIT, REG_SIZE), "global pointer"),
                        RegisterContainer.Register(MutVal.Value.Dec("4", REG_ADDRESS_SIZE), listOf("x4"), listOf("tp"), MutVal(REG_INIT, REG_SIZE), "thread pointer"),
                        RegisterContainer.Register(MutVal.Value.Dec("5", REG_ADDRESS_SIZE), listOf("x5"), listOf("t0"), MutVal(REG_INIT, REG_SIZE), "temporary register 0"),
                        RegisterContainer.Register(MutVal.Value.Dec("6", REG_ADDRESS_SIZE), listOf("x6"), listOf("t1"), MutVal(REG_INIT, REG_SIZE), "temporary register 1"),
                        RegisterContainer.Register(MutVal.Value.Dec("7", REG_ADDRESS_SIZE), listOf("x7"), listOf("t2"), MutVal(REG_INIT, REG_SIZE), "temporary register 2"),
                        RegisterContainer.Register(MutVal.Value.Dec("8", REG_ADDRESS_SIZE), listOf("x8"), listOf("s0", "fp"), MutVal(REG_INIT, REG_SIZE), "saved register 0 / frame pointer"),
                        RegisterContainer.Register(MutVal.Value.Dec("9", REG_ADDRESS_SIZE), listOf("x9"), listOf("s1"), MutVal(REG_INIT, REG_SIZE), "saved register 1"),
                        RegisterContainer.Register(MutVal.Value.Dec("10", REG_ADDRESS_SIZE), listOf("x10"), listOf("a0"), MutVal(REG_INIT, REG_SIZE), "function argument 0 / return value 0"),
                        RegisterContainer.Register(MutVal.Value.Dec("11", REG_ADDRESS_SIZE), listOf("x11"), listOf("a1"), MutVal(REG_INIT, REG_SIZE), "function argument 1 / return value 1"),
                        RegisterContainer.Register(MutVal.Value.Dec("12", REG_ADDRESS_SIZE), listOf("x12"), listOf("a2"), MutVal(REG_INIT, REG_SIZE), "function argument 2"),
                        RegisterContainer.Register(MutVal.Value.Dec("13", REG_ADDRESS_SIZE), listOf("x13"), listOf("a3"), MutVal(REG_INIT, REG_SIZE), "function argument 3"),
                        RegisterContainer.Register(MutVal.Value.Dec("14", REG_ADDRESS_SIZE), listOf("x14"), listOf("a4"), MutVal(REG_INIT, REG_SIZE), "function argument 4"),
                        RegisterContainer.Register(MutVal.Value.Dec("15", REG_ADDRESS_SIZE), listOf("x15"), listOf("a5"), MutVal(REG_INIT, REG_SIZE), "function argument 5"),
                        RegisterContainer.Register(MutVal.Value.Dec("16", REG_ADDRESS_SIZE), listOf("x16"), listOf("a6"), MutVal(REG_INIT, REG_SIZE), "function argument 6"),
                        RegisterContainer.Register(MutVal.Value.Dec("17", REG_ADDRESS_SIZE), listOf("x17"), listOf("a7"), MutVal(REG_INIT, REG_SIZE), "function argument 7"),
                        RegisterContainer.Register(MutVal.Value.Dec("18", REG_ADDRESS_SIZE), listOf("x18"), listOf("s2"), MutVal(REG_INIT, REG_SIZE), "saved register 2"),
                        RegisterContainer.Register(MutVal.Value.Dec("19", REG_ADDRESS_SIZE), listOf("x19"), listOf("s3"), MutVal(REG_INIT, REG_SIZE), "saved register 3"),
                        RegisterContainer.Register(MutVal.Value.Dec("20", REG_ADDRESS_SIZE), listOf("x20"), listOf("s4"), MutVal(REG_INIT, REG_SIZE), "saved register 4"),
                        RegisterContainer.Register(MutVal.Value.Dec("21", REG_ADDRESS_SIZE), listOf("x21"), listOf("s5"), MutVal(REG_INIT, REG_SIZE), "saved register 5"),
                        RegisterContainer.Register(MutVal.Value.Dec("22", REG_ADDRESS_SIZE), listOf("x22"), listOf("s6"), MutVal(REG_INIT, REG_SIZE), "saved register 6"),
                        RegisterContainer.Register(MutVal.Value.Dec("23", REG_ADDRESS_SIZE), listOf("x23"), listOf("s7"), MutVal(REG_INIT, REG_SIZE), "saved register 7"),
                        RegisterContainer.Register(MutVal.Value.Dec("24", REG_ADDRESS_SIZE), listOf("x24"), listOf("s8"), MutVal(REG_INIT, REG_SIZE), "saved register 8"),
                        RegisterContainer.Register(MutVal.Value.Dec("25", REG_ADDRESS_SIZE), listOf("x25"), listOf("s9"), MutVal(REG_INIT, REG_SIZE), "saved register 9"),
                        RegisterContainer.Register(MutVal.Value.Dec("26", REG_ADDRESS_SIZE), listOf("x26"), listOf("s10"), MutVal(REG_INIT, REG_SIZE), "saved register 10"),
                        RegisterContainer.Register(MutVal.Value.Dec("27", REG_ADDRESS_SIZE), listOf("x27"), listOf("s11"), MutVal(REG_INIT, REG_SIZE), "saved register 11"),
                        RegisterContainer.Register(MutVal.Value.Dec("28", REG_ADDRESS_SIZE), listOf("x28"), listOf("t3"), MutVal(REG_INIT, REG_SIZE), "temporary register 3"),
                        RegisterContainer.Register(MutVal.Value.Dec("29", REG_ADDRESS_SIZE), listOf("x29"), listOf("t4"), MutVal(REG_INIT, REG_SIZE), "temporary register 4"),
                        RegisterContainer.Register(MutVal.Value.Dec("30", REG_ADDRESS_SIZE), listOf("x30"), listOf("t5"), MutVal(REG_INIT, REG_SIZE), "temporary register 5"),
                        RegisterContainer.Register(MutVal.Value.Dec("31", REG_ADDRESS_SIZE), listOf("x31"), listOf("t6"), MutVal(REG_INIT, REG_SIZE), "temporary register 6")
                    )
                )
            ),
            pcSize = MutVal.Size.Bit32()
        ),
        Memory(MutVal.Size.Bit32(), MEM_INIT, MutVal.Size.Bit8(), Memory.Endianess.LittleEndian),
        Transcript(TS_COMPILED_HEADERS.entries.map { it.name }, TS_DISASSEMBLED_HEADERS.entries.map { it.name })
    )

    // EDITOR

    val FileExtension = "rvasm"
    val FileTypeDescription = "RISC-V Assembler"

}