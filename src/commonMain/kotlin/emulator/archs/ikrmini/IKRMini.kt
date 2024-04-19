package emulator.archs.ikrmini

import emulator.kit.assembly.Compiler
import emulator.kit.common.Docs
import emulator.kit.common.Memory
import emulator.kit.common.RegContainer
import emulator.kit.configs.AsmConfig
import emulator.kit.configs.Config
import emulator.kit.types.Variable
import emulator.kit.types.Variable.Size.*
import emulator.kit.types.Variable.Value.*
import emulator.kit.common.Docs.DocComponent.*

data object IKRMini {


    val BYTESIZE = Bit8()
    val WORDSIZE = Bit16()
    val INSTRWITHEXT = Bit32()
    val MEM_ADDRESS_WIDTH = WORDSIZE

    val descr = Config.Description(
        "IKR Mini",
        "IKR Minimalprozessor",
        Docs(
            usingStandard = true,
            Docs.DocFile.DefinedFile(
                "IKR Mini Implemented",
                Chapter(
                    "Memory",
                    Text("address-width: ${MEM_ADDRESS_WIDTH}"),
                    Text("value-width: ${BYTESIZE}")
                ),
                Chapter(
                    "Instructions",
                    Table(
                        listOf("instruction", "param", "opcode", "description"),
                        *IKRMiniSyntax.InstrType.entries.map { instr ->
                            listOf(
                                Text(instr.name),
                                Text(instr.paramMap.entries.map { it.key }.joinToString("\n") { it.exampleString }),
                                Text(instr.paramMap.entries.joinToString("\n") { "${it.value} <- ${it.key.name}" }),
                                Text(instr.descr)
                            )
                        }.toTypedArray()
                    )
                )
            ),
            Docs.DocFile.SourceFile(
                "Syntax Examples", "/documents/ikrmini/syntaxexamples.html"
            )
        )
    )

    val config = Config(
        descr, fileEnding = "s", RegContainer(
            listOf(
                RegContainer.RegisterFile(
                    "common", arrayOf(
                        RegContainer.Register(Hex("0", Bit1()), listOf("AC"), listOf(), Variable("0", WORDSIZE), description = "Accumulator"),
                        RegContainer.Register(Hex("1", Bit1()), listOf("NZVC"), listOf(), Variable("0", Bit4()), description = "NZVC ALU flags", containsFlags = true)
                    )
                )
            ),
            WORDSIZE,
            "common"
        ), Memory(WORDSIZE, "0", BYTESIZE, Memory.Endianess.BigEndian)
    )

    val asmConfig = AsmConfig(IKRMiniSyntax(), IKRMiniAssembly(), false, numberSystemPrefixes = Compiler.ConstantPrefixes("$", "%", "", "u"))


}