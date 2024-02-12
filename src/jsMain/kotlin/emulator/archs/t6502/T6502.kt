package emulator.archs.t6502

import StyleAttr
import emulator.kit.assembly.Compiler
import emulator.kit.common.*
import emulator.kit.configs.AsmConfig
import emulator.kit.configs.Config
import emulator.kit.types.Variable
import emulator.kit.types.Variable.Value.*
import emulator.kit.types.Variable.Size.*
import react.FC
import react.dom.html.ReactHTML
import web.cssom.ClassName


/**
 * MOS Technology 6502 Configuration
 *
 * Sources:
 * - https://en.wikibooks.org/wiki/6502_Assembly
 * - https://en.wikipedia.org/wiki/MOS_Technology_6502
 * - https://www.masswerk.at/6502/6502_instruction_set.html
 *
 *
 * Comparison:
 * - https://www.masswerk.at/6502/assembler.html
 */
object T6502 {

    val WORD_SIZE = Bit16()
    val BYTE_SIZE = Bit8()

    val MEM_ADDR_SIZE = WORD_SIZE

    enum class TSCompiledRow {
        ADDRESS,
        LABEL,
        INSTRUCTION,
        EXTENSION
    }

    enum class TSDisassembledRow {
        ADDRESS,
        INSTRUCTION,
        EXTENSION
    }

    val commonRegFile = RegContainer.RegisterFile(
        name = "common",
        unsortedRegisters = arrayOf(
            RegContainer.Register(Hex("00", WORD_SIZE), listOf("AC"), listOf(), Variable("00000000", BYTE_SIZE), description = "accumulator"),
            RegContainer.Register(Hex("01", WORD_SIZE), listOf("X"), listOf(), Variable("00000000", BYTE_SIZE), description = "X register"),
            RegContainer.Register(Hex("02", WORD_SIZE), listOf("Y"), listOf(), Variable("00000000", BYTE_SIZE), description = "Y register"),
            RegContainer.Register(Hex("03", WORD_SIZE), listOf("SR"), listOf(), Variable("00100000", BYTE_SIZE), description = "status register [NV-BDIZC]"),
            RegContainer.Register(Hex("04", WORD_SIZE), listOf("SP"), listOf(), Variable("11111111", BYTE_SIZE), description = "stack pointer")
        )
    )

    val description = Config.Description(
        "T6502",
        "MOS Technology 6502",
        Docs(usingStandard = true,Docs.HtmlFile.DefinedFile("Implemented", FC {
            ReactHTML.h1 {
                +"6502 Implemented"
            }
            ReactHTML.h2 {
                +"General"
            }
            ReactHTML.strong {
                +"Memory"
            }
            ReactHTML.ul {
                ReactHTML.li {
                    +"address-width: $MEM_ADDR_SIZE"
                }
                ReactHTML.li {
                    +"value-width: $BYTE_SIZE"
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
                    for (instr in T6502Syntax.InstrType.entries) {
                        ReactHTML.tr {

                            ReactHTML.td {
                                className = ClassName(StyleAttr.Main.Table.CLASS_TXT_LEFT)
                                +instr.name
                            }
                            ReactHTML.td {
                                className = ClassName(StyleAttr.Main.Table.CLASS_TXT_LEFT)
                                +instr.opCode.entries.map { it.key }.joinToString("\n") { it.exampleString }
                                +"\t"
                            }
                            ReactHTML.td {
                                className = ClassName(StyleAttr.Main.Table.CLASS_TXT_LEFT)
                                +instr.opCode.entries.joinToString("\n") { "${it.value} <- ${it.key.description}" }
                            }
                            ReactHTML.td{
                                className = ClassName(StyleAttr.Main.Table.CLASS_TXT_RIGHT)
                                +instr.description
                            }
                        }
                    }
                }
            }
        }), Docs.HtmlFile.SourceFile("Syntax Examples", "/documents/t6502/syntaxexamples.html"))
    )

    val config = Config(
        description,
        FileHandler("s"),
        RegContainer(listOf(commonRegFile), WORD_SIZE, "common"),
        Memory(MEM_ADDR_SIZE, initBin = "0".repeat(BYTE_SIZE.bitWidth), BYTE_SIZE, Memory.Endianess.LittleEndian),
        Transcript(TSCompiledRow.entries.map { it.name }, TSDisassembledRow.entries.map { it.name })
    )

    val asmConfig = AsmConfig(
        syntax = T6502Syntax(),
        assembly = T6502Assembly(),
        compilerDetectRegistersByNames = false,
        numberSystemPrefixes = Compiler.ConstantPrefixes("$", "%", "", "u")
    )


}