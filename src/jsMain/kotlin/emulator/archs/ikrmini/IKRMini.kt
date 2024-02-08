package emulator.archs.ikrmini

import emulator.kit.assembly.Compiler
import emulator.kit.common.Docs
import emulator.kit.common.FileHandler
import emulator.kit.common.Memory
import emulator.kit.common.RegContainer
import emulator.kit.configs.AsmConfig
import emulator.kit.configs.Config
import emulator.kit.types.Variable
import emulator.kit.types.Variable.Size.*
import emulator.kit.types.Variable.Value.*

data object IKRMini {


    val BYTESIZE = Bit8()
    val WORDSIZE = Bit16()
    val INSTRWITHEXT = Bit32()
    val MEM_ADDRESS_WIDTH = WORDSIZE

    val descr = Config.Description(
        "IKR Mini",
        "IKR Minimalprozessor",
        Docs()
    )

    val config = Config(
        descr, FileHandler(".s"), RegContainer(
            listOf(
                RegContainer.RegisterFile(
                    "common", arrayOf(
                        RegContainer.Register(Hex("0", Bit3()), listOf("AA"), listOf(), Variable("0", WORDSIZE), description = "Operand to address memory"),
                        RegContainer.Register(Hex("1", Bit3()), listOf("IR"), listOf(), Variable("0", WORDSIZE), description = "Instruction Register"),
                        RegContainer.Register(Hex("2", Bit3()), listOf("MX"), listOf(), Variable("0", WORDSIZE), description = "Operand for ALU and PC"),
                        RegContainer.Register(Hex("3", Bit3()), listOf("AC"), listOf(), Variable("0", WORDSIZE), description = "Accumulator"),
                        RegContainer.Register(Hex("4", Bit3()), listOf("NZVC"), listOf(), Variable("0", Bit4()), description = "NZVC ALU flags")
                    )
                )
            ),
            WORDSIZE,
            "common"
        ), Memory(WORDSIZE, "0", BYTESIZE, Memory.Endianess.BigEndian)
    )

    val asmConfig = AsmConfig(IKRMiniSyntax(), IKRMiniAssembly(), false, numberSystemPrefixes = Compiler.ConstantPrefixes("$","%","","u"))


}