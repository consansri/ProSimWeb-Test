package emulator.archs.riscv64

import emotion.react.css
import emulator.kit.common.*
import emulator.kit.configs.AsmConfig
import emulator.kit.configs.Config
import emulator.kit.types.Variable
import react.FC
import react.dom.html.ReactHTML
import web.cssom.ClassName
import web.cssom.Float

object RV64 {
   
    
    val MEM_INIT: String = "0"
    val REG_INIT: String = "0"
    val REG_VALUE_SIZE = Variable.Size.Bit64()
    val REG_ADDRESS_SIZE = Variable.Size.Bit5()
    val MEM_VALUE_WIDTH = Variable.Size.Bit8()
    val MEM_ADDRESS_WIDTH = Variable.Size.Bit32()

    enum class TS_COMPILED_HEADERS {
        Address,
        Label,
        Instruction,
        Parameters
    }

    enum class TS_DISASSEMBLED_HEADERS {
        Address,
        Label,
        Instruction,
        Parameters
    }

    enum class FEATURE(val initialValue: Boolean) {
        // I(true), /*Integrated*/
        /**
         * TODO("Integrated more architecture extension packages")
         */

        /* M(true),
         A(false),
         F(false),
         D(false),
         C(false),
         V(false),
         B(false),
         J(false),
         Z(false)*/
    }
    val riscVDocs = Docs(
        Docs.HtmlFile.SourceFile(
            "Syntax Examples",
            "../documents/rv64/syntaxexamples.html"
        ),
        Docs.HtmlFile.DefinedFile(
            "Implemented",
            FC {
                ReactHTML.h1 {
                    +"RV64 Implemented"
                }
                ReactHTML.h2 {
                    +"General"
                }
                ReactHTML.strong {
                    +"Registers"
                }
                ReactHTML.ul {
                    ReactHTML.li {
                        +"address-width: ${REG_ADDRESS_SIZE.bitWidth}bit"
                    }
                    ReactHTML.li {
                        +"value-width: ${REG_VALUE_SIZE.bitWidth}bit"
                    }
                }
                ReactHTML.strong {
                    +"Memory"
                }
                ReactHTML.ul {
                    ReactHTML.li {
                        +"address-width: ${MEM_ADDRESS_WIDTH.bitWidth}bit"
                    }
                    ReactHTML.li {
                        +"value-width: ${MEM_VALUE_WIDTH.bitWidth}bit"
                    }
                }
                ReactHTML.h2 {
                    +"Directives"
                }

                for (majorDir in RV64Syntax.E_DIRECTIVE.MajorType.entries) {
                    ReactHTML.strong {
                        +majorDir.docName
                    }
                    ReactHTML.ul {
                        for (dir in RV64Syntax.E_DIRECTIVE.DirType.entries.filter { it.majorType == majorDir }) {
                            ReactHTML.li { +".${dir.dirname}" }
                        }
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
                        }
                    }
                    ReactHTML.tbody {
                        for (instr in RV64Syntax.R_INSTR.InstrType.entries) {
                            ReactHTML.tr {

                                ReactHTML.td {
                                    className = ClassName(StyleAttr.Main.Table.CLASS_TXT_LEFT)
                                    +instr.id
                                }
                                ReactHTML.td {
                                    className = ClassName(StyleAttr.Main.Table.CLASS_TXT_LEFT)
                                    +"${instr.paramType.exampleString}\t"
                                }
                                ReactHTML.td {
                                    css {
                                        float = Float.right
                                    }
                                    instr.opCode?.let {
                                        ReactHTML.table {
                                            ReactHTML.thead {
                                                ReactHTML.tr {
                                                    for (mask in it.maskLabels) {
                                                        ReactHTML.th {
                                                            className = ClassName(StyleAttr.Main.Table.CLASS_TXT_CENTER)
                                                            +mask.name
                                                        }
                                                    }
                                                }
                                            }
                                            ReactHTML.tbody {
                                                ReactHTML.tr {
                                                    for (opcode in it.opMaskList) {
                                                        ReactHTML.td {
                                                            className = ClassName(StyleAttr.Main.Table.CLASS_TXT_CENTER)
                                                            +opcode
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                    if (instr.pseudo) +"pseudo" else +""
                                }
                            }
                        }
                    }
                }
            }
        )
    )

    val asmConfig = AsmConfig(RV64Syntax(), RV64Assembly(RV64BinMapper(), Variable.Value.Hex("00010000", Variable.Size.Bit32()), Variable.Value.Hex("00020000", Variable.Size.Bit32()), Variable.Value.Hex("00030000", Variable.Size.Bit32())))
    
    val config = Config(
        Config.Description("RV64I", "RISC-V 64Bit", riscVDocs),
        FileHandler("s"),
        RegContainer(
            listOf(
                RegContainer.RegisterFile(
                    "main", arrayOf(
                        RegContainer.Register(Variable.Value.UDec("0", REG_ADDRESS_SIZE), listOf("x0"), listOf("zero"), Variable(REG_INIT, REG_VALUE_SIZE), "hardwired zero", hardwire = true),
                        RegContainer.Register(Variable.Value.UDec("1", REG_ADDRESS_SIZE), listOf("x1"), listOf("ra"), Variable(REG_INIT, REG_VALUE_SIZE), "return address"),
                        RegContainer.Register(Variable.Value.UDec("2", REG_ADDRESS_SIZE), listOf("x2"), listOf("sp"), Variable(REG_INIT, REG_VALUE_SIZE), "stack pointer"),
                        RegContainer.Register(Variable.Value.UDec("3", REG_ADDRESS_SIZE), listOf("x3"), listOf("gp"), Variable(REG_INIT, REG_VALUE_SIZE), "global pointer"),
                        RegContainer.Register(Variable.Value.UDec("4", REG_ADDRESS_SIZE), listOf("x4"), listOf("tp"), Variable(REG_INIT, REG_VALUE_SIZE), "thread pointer"),
                        RegContainer.Register(Variable.Value.UDec("5", REG_ADDRESS_SIZE), listOf("x5"), listOf("t0"), Variable(REG_INIT, REG_VALUE_SIZE), "temporary register 0"),
                        RegContainer.Register(Variable.Value.UDec("6", REG_ADDRESS_SIZE), listOf("x6"), listOf("t1"), Variable(REG_INIT, REG_VALUE_SIZE), "temporary register 1"),
                        RegContainer.Register(Variable.Value.UDec("7", REG_ADDRESS_SIZE), listOf("x7"), listOf("t2"), Variable(REG_INIT, REG_VALUE_SIZE), "temporary register 2"),
                        RegContainer.Register(Variable.Value.UDec("8", REG_ADDRESS_SIZE), listOf("x8"), listOf("s0", "fp"), Variable(REG_INIT, REG_VALUE_SIZE), "saved register 0 / frame pointer"),
                        RegContainer.Register(Variable.Value.UDec("9", REG_ADDRESS_SIZE), listOf("x9"), listOf("s1"), Variable(REG_INIT, REG_VALUE_SIZE), "saved register 1"),
                        RegContainer.Register(Variable.Value.UDec("10", REG_ADDRESS_SIZE), listOf("x10"), listOf("a0"), Variable(REG_INIT, REG_VALUE_SIZE), "function argument 0 / return value 0"),
                        RegContainer.Register(Variable.Value.UDec("11", REG_ADDRESS_SIZE), listOf("x11"), listOf("a1"), Variable(REG_INIT, REG_VALUE_SIZE), "function argument 1 / return value 1"),
                        RegContainer.Register(Variable.Value.UDec("12", REG_ADDRESS_SIZE), listOf("x12"), listOf("a2"), Variable(REG_INIT, REG_VALUE_SIZE), "function argument 2"),
                        RegContainer.Register(Variable.Value.UDec("13", REG_ADDRESS_SIZE), listOf("x13"), listOf("a3"), Variable(REG_INIT, REG_VALUE_SIZE), "function argument 3"),
                        RegContainer.Register(Variable.Value.UDec("14", REG_ADDRESS_SIZE), listOf("x14"), listOf("a4"), Variable(REG_INIT, REG_VALUE_SIZE), "function argument 4"),
                        RegContainer.Register(Variable.Value.UDec("15", REG_ADDRESS_SIZE), listOf("x15"), listOf("a5"), Variable(REG_INIT, REG_VALUE_SIZE), "function argument 5"),
                        RegContainer.Register(Variable.Value.UDec("16", REG_ADDRESS_SIZE), listOf("x16"), listOf("a6"), Variable(REG_INIT, REG_VALUE_SIZE), "function argument 6"),
                        RegContainer.Register(Variable.Value.UDec("17", REG_ADDRESS_SIZE), listOf("x17"), listOf("a7"), Variable(REG_INIT, REG_VALUE_SIZE), "function argument 7"),
                        RegContainer.Register(Variable.Value.UDec("18", REG_ADDRESS_SIZE), listOf("x18"), listOf("s2"), Variable(REG_INIT, REG_VALUE_SIZE), "saved register 2"),
                        RegContainer.Register(Variable.Value.UDec("19", REG_ADDRESS_SIZE), listOf("x19"), listOf("s3"), Variable(REG_INIT, REG_VALUE_SIZE), "saved register 3"),
                        RegContainer.Register(Variable.Value.UDec("20", REG_ADDRESS_SIZE), listOf("x20"), listOf("s4"), Variable(REG_INIT, REG_VALUE_SIZE), "saved register 4"),
                        RegContainer.Register(Variable.Value.UDec("21", REG_ADDRESS_SIZE), listOf("x21"), listOf("s5"), Variable(REG_INIT, REG_VALUE_SIZE), "saved register 5"),
                        RegContainer.Register(Variable.Value.UDec("22", REG_ADDRESS_SIZE), listOf("x22"), listOf("s6"), Variable(REG_INIT, REG_VALUE_SIZE), "saved register 6"),
                        RegContainer.Register(Variable.Value.UDec("23", REG_ADDRESS_SIZE), listOf("x23"), listOf("s7"), Variable(REG_INIT, REG_VALUE_SIZE), "saved register 7"),
                        RegContainer.Register(Variable.Value.UDec("24", REG_ADDRESS_SIZE), listOf("x24"), listOf("s8"), Variable(REG_INIT, REG_VALUE_SIZE), "saved register 8"),
                        RegContainer.Register(Variable.Value.UDec("25", REG_ADDRESS_SIZE), listOf("x25"), listOf("s9"), Variable(REG_INIT, REG_VALUE_SIZE), "saved register 9"),
                        RegContainer.Register(Variable.Value.UDec("26", REG_ADDRESS_SIZE), listOf("x26"), listOf("s10"), Variable(REG_INIT, REG_VALUE_SIZE), "saved register 10"),
                        RegContainer.Register(Variable.Value.UDec("27", REG_ADDRESS_SIZE), listOf("x27"), listOf("s11"), Variable(REG_INIT, REG_VALUE_SIZE), "saved register 11"),
                        RegContainer.Register(Variable.Value.UDec("28", REG_ADDRESS_SIZE), listOf("x28"), listOf("t3"), Variable(REG_INIT, REG_VALUE_SIZE), "temporary register 3"),
                        RegContainer.Register(Variable.Value.UDec("29", REG_ADDRESS_SIZE), listOf("x29"), listOf("t4"), Variable(REG_INIT, REG_VALUE_SIZE), "temporary register 4"),
                        RegContainer.Register(Variable.Value.UDec("30", REG_ADDRESS_SIZE), listOf("x30"), listOf("t5"), Variable(REG_INIT, REG_VALUE_SIZE), "temporary register 5"),
                        RegContainer.Register(Variable.Value.UDec("31", REG_ADDRESS_SIZE), listOf("x31"), listOf("t6"), Variable(REG_INIT, REG_VALUE_SIZE), "temporary register 6")
                    )
                )
            ),
            pcSize = Variable.Size.Bit32()
        ),
        Memory(MEM_ADDRESS_WIDTH, MEM_INIT, MEM_VALUE_WIDTH, Memory.Endianess.LittleEndian),
        Transcript()
    )

}