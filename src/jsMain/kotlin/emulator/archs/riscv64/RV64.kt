package emulator.archs.riscv64

import emulator.archs.riscv32.RV32
import emulator.kit.common.*
import emulator.kit.configs.AsmConfig
import emulator.kit.configs.Config
import emulator.kit.types.Variable

object RV64 {

    val asmConfig = AsmConfig(RV64Syntax(), RV64Assembly())

    val config = Config(
        Config.Description("RV64I", "RISC-V 64Bit", Docs()),
        FileHandler("s"),
        RegisterContainer(
            listOf(
                RegisterContainer.RegisterFile(
                    "main", arrayOf(
                        RegisterContainer.Register(Variable.Value.UDec("0", RV32.REG_ADDRESS_SIZE), listOf("x0"), listOf("zero"), Variable(RV32.REG_INIT, RV32.REG_VALUE_SIZE), "hardwired zero", hardwire = true),
                        RegisterContainer.Register(Variable.Value.UDec("1", RV32.REG_ADDRESS_SIZE), listOf("x1"), listOf("ra"), Variable(RV32.REG_INIT, RV32.REG_VALUE_SIZE), "return address"),
                        RegisterContainer.Register(Variable.Value.UDec("2", RV32.REG_ADDRESS_SIZE), listOf("x2"), listOf("sp"), Variable(RV32.REG_INIT, RV32.REG_VALUE_SIZE), "stack pointer"),
                        RegisterContainer.Register(Variable.Value.UDec("3", RV32.REG_ADDRESS_SIZE), listOf("x3"), listOf("gp"), Variable(RV32.REG_INIT, RV32.REG_VALUE_SIZE), "global pointer"),
                        RegisterContainer.Register(Variable.Value.UDec("4", RV32.REG_ADDRESS_SIZE), listOf("x4"), listOf("tp"), Variable(RV32.REG_INIT, RV32.REG_VALUE_SIZE), "thread pointer"),
                        RegisterContainer.Register(Variable.Value.UDec("5", RV32.REG_ADDRESS_SIZE), listOf("x5"), listOf("t0"), Variable(RV32.REG_INIT, RV32.REG_VALUE_SIZE), "temporary register 0"),
                        RegisterContainer.Register(Variable.Value.UDec("6", RV32.REG_ADDRESS_SIZE), listOf("x6"), listOf("t1"), Variable(RV32.REG_INIT, RV32.REG_VALUE_SIZE), "temporary register 1"),
                        RegisterContainer.Register(Variable.Value.UDec("7", RV32.REG_ADDRESS_SIZE), listOf("x7"), listOf("t2"), Variable(RV32.REG_INIT, RV32.REG_VALUE_SIZE), "temporary register 2"),
                        RegisterContainer.Register(Variable.Value.UDec("8", RV32.REG_ADDRESS_SIZE), listOf("x8"), listOf("s0", "fp"), Variable(RV32.REG_INIT, RV32.REG_VALUE_SIZE), "saved register 0 / frame pointer"),
                        RegisterContainer.Register(Variable.Value.UDec("9", RV32.REG_ADDRESS_SIZE), listOf("x9"), listOf("s1"), Variable(RV32.REG_INIT, RV32.REG_VALUE_SIZE), "saved register 1"),
                        RegisterContainer.Register(Variable.Value.UDec("10", RV32.REG_ADDRESS_SIZE), listOf("x10"), listOf("a0"), Variable(RV32.REG_INIT, RV32.REG_VALUE_SIZE), "function argument 0 / return value 0"),
                        RegisterContainer.Register(Variable.Value.UDec("11", RV32.REG_ADDRESS_SIZE), listOf("x11"), listOf("a1"), Variable(RV32.REG_INIT, RV32.REG_VALUE_SIZE), "function argument 1 / return value 1"),
                        RegisterContainer.Register(Variable.Value.UDec("12", RV32.REG_ADDRESS_SIZE), listOf("x12"), listOf("a2"), Variable(RV32.REG_INIT, RV32.REG_VALUE_SIZE), "function argument 2"),
                        RegisterContainer.Register(Variable.Value.UDec("13", RV32.REG_ADDRESS_SIZE), listOf("x13"), listOf("a3"), Variable(RV32.REG_INIT, RV32.REG_VALUE_SIZE), "function argument 3"),
                        RegisterContainer.Register(Variable.Value.UDec("14", RV32.REG_ADDRESS_SIZE), listOf("x14"), listOf("a4"), Variable(RV32.REG_INIT, RV32.REG_VALUE_SIZE), "function argument 4"),
                        RegisterContainer.Register(Variable.Value.UDec("15", RV32.REG_ADDRESS_SIZE), listOf("x15"), listOf("a5"), Variable(RV32.REG_INIT, RV32.REG_VALUE_SIZE), "function argument 5"),
                        RegisterContainer.Register(Variable.Value.UDec("16", RV32.REG_ADDRESS_SIZE), listOf("x16"), listOf("a6"), Variable(RV32.REG_INIT, RV32.REG_VALUE_SIZE), "function argument 6"),
                        RegisterContainer.Register(Variable.Value.UDec("17", RV32.REG_ADDRESS_SIZE), listOf("x17"), listOf("a7"), Variable(RV32.REG_INIT, RV32.REG_VALUE_SIZE), "function argument 7"),
                        RegisterContainer.Register(Variable.Value.UDec("18", RV32.REG_ADDRESS_SIZE), listOf("x18"), listOf("s2"), Variable(RV32.REG_INIT, RV32.REG_VALUE_SIZE), "saved register 2"),
                        RegisterContainer.Register(Variable.Value.UDec("19", RV32.REG_ADDRESS_SIZE), listOf("x19"), listOf("s3"), Variable(RV32.REG_INIT, RV32.REG_VALUE_SIZE), "saved register 3"),
                        RegisterContainer.Register(Variable.Value.UDec("20", RV32.REG_ADDRESS_SIZE), listOf("x20"), listOf("s4"), Variable(RV32.REG_INIT, RV32.REG_VALUE_SIZE), "saved register 4"),
                        RegisterContainer.Register(Variable.Value.UDec("21", RV32.REG_ADDRESS_SIZE), listOf("x21"), listOf("s5"), Variable(RV32.REG_INIT, RV32.REG_VALUE_SIZE), "saved register 5"),
                        RegisterContainer.Register(Variable.Value.UDec("22", RV32.REG_ADDRESS_SIZE), listOf("x22"), listOf("s6"), Variable(RV32.REG_INIT, RV32.REG_VALUE_SIZE), "saved register 6"),
                        RegisterContainer.Register(Variable.Value.UDec("23", RV32.REG_ADDRESS_SIZE), listOf("x23"), listOf("s7"), Variable(RV32.REG_INIT, RV32.REG_VALUE_SIZE), "saved register 7"),
                        RegisterContainer.Register(Variable.Value.UDec("24", RV32.REG_ADDRESS_SIZE), listOf("x24"), listOf("s8"), Variable(RV32.REG_INIT, RV32.REG_VALUE_SIZE), "saved register 8"),
                        RegisterContainer.Register(Variable.Value.UDec("25", RV32.REG_ADDRESS_SIZE), listOf("x25"), listOf("s9"), Variable(RV32.REG_INIT, RV32.REG_VALUE_SIZE), "saved register 9"),
                        RegisterContainer.Register(Variable.Value.UDec("26", RV32.REG_ADDRESS_SIZE), listOf("x26"), listOf("s10"), Variable(RV32.REG_INIT, RV32.REG_VALUE_SIZE), "saved register 10"),
                        RegisterContainer.Register(Variable.Value.UDec("27", RV32.REG_ADDRESS_SIZE), listOf("x27"), listOf("s11"), Variable(RV32.REG_INIT, RV32.REG_VALUE_SIZE), "saved register 11"),
                        RegisterContainer.Register(Variable.Value.UDec("28", RV32.REG_ADDRESS_SIZE), listOf("x28"), listOf("t3"), Variable(RV32.REG_INIT, RV32.REG_VALUE_SIZE), "temporary register 3"),
                        RegisterContainer.Register(Variable.Value.UDec("29", RV32.REG_ADDRESS_SIZE), listOf("x29"), listOf("t4"), Variable(RV32.REG_INIT, RV32.REG_VALUE_SIZE), "temporary register 4"),
                        RegisterContainer.Register(Variable.Value.UDec("30", RV32.REG_ADDRESS_SIZE), listOf("x30"), listOf("t5"), Variable(RV32.REG_INIT, RV32.REG_VALUE_SIZE), "temporary register 5"),
                        RegisterContainer.Register(Variable.Value.UDec("31", RV32.REG_ADDRESS_SIZE), listOf("x31"), listOf("t6"), Variable(RV32.REG_INIT, RV32.REG_VALUE_SIZE), "temporary register 6")
                    )
                )
            ),
            pcSize = Variable.Size.Bit32()
        ),
        Memory(Variable.Size.Bit32(), "0", Variable.Size.Bit8(), Memory.Endianess.LittleEndian),
        Transcript()
    )

}