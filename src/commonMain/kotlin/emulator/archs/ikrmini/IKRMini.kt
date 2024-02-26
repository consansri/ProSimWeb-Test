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
        Docs(usingStandard = true, /*Docs.HtmlFile.DefinedFile("Implemented", FC {
            ReactHTML.h1 {
                +"IKR Mini Implemented"
            }
            ReactHTML.h2 {
                +"General"
            }
            ReactHTML.strong {
                +"Memory"
            }
            ReactHTML.ul {
                ReactHTML.li {
                    +"address-width: ${MEM_ADDRESS_WIDTH}"
                }
                ReactHTML.li {
                    +"value-width: ${BYTESIZE}"
                }
            }
            ReactHTML.h2 {
                +"Instructions"
            }
            ReactHTML.table {
                ReactHTML.thead {
                    ReactHTML.tr {

                        ReactHTML.th {
                            className = ClassName(StyleAttr.Main.Table.CLASS_TXT_CENTER)
                            colSpan = 2
                            +"instruction"
                        }
                        ReactHTML.th {
                            className = ClassName(StyleAttr.Main.Table.CLASS_TXT_CENTER)
                            +"opcode"
                        }
                        ReactHTML.th {
                            className = ClassName(StyleAttr.Main.Table.CLASS_TXT_CENTER)
                            +"description"
                        }
                    }
                }
                ReactHTML.tbody {
                    className = ClassName(StyleAttr.Main.Table.CLASS_STRIPED)
                    for (instr in IKRMiniSyntax.InstrType.entries) {
                        ReactHTML.tr {

                            ReactHTML.td {
                                className = ClassName(StyleAttr.Main.Table.CLASS_TXT_LEFT)
                                +instr.name
                            }
                            ReactHTML.td {
                                className = ClassName(StyleAttr.Main.Table.CLASS_TXT_LEFT)
                                +instr.paramMap.entries.map { it.key }.joinToString("\n") { it.exampleString }
                                +"\t"
                            }
                            ReactHTML.td {
                                className = ClassName(StyleAttr.Main.Table.CLASS_TXT_LEFT)
                                +instr.paramMap.entries.joinToString("\n") { "${it.value} <- ${it.key.name}" }
                            }
                            ReactHTML.td {
                                className = ClassName(StyleAttr.Main.Table.CLASS_TXT_RIGHT)
                                +instr.descr
                            }
                        }
                    }
                }
            }


        }),*/ Docs.HtmlFile.SourceFile("Syntax Examples", "/documents/ikrmini/syntaxexamples.html"))
    )

    val config = Config(
        descr, FileHandler(".s"), RegContainer(
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