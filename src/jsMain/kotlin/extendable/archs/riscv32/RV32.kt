package extendable.archs.riscv32

import StyleConst
import emotion.react.css
import extendable.components.*
import extendable.components.connected.*
import extendable.components.types.*
import react.FC
import react.dom.html.ReactHTML
import react.dom.html.ReactHTML.h1
import react.dom.html.ReactHTML.h2
import react.dom.html.ReactHTML.li
import react.dom.html.ReactHTML.table
import react.dom.html.ReactHTML.tbody
import react.dom.html.ReactHTML.td
import react.dom.html.ReactHTML.th
import react.dom.html.ReactHTML.thead
import react.dom.html.ReactHTML.tr
import react.dom.html.ReactHTML.ul
import web.cssom.ClassName
import web.cssom.Float

object RV32 {

    // PROCESSOR
    val MEM_INIT: String = "0"

    val REG_INIT: String = "0"
    val REG_VALUE_SIZE = MutVal.Size.Bit32()
    val REG_ADDRESS_SIZE = MutVal.Size.Bit5()

    val MEM_VALUE_WIDTH = MutVal.Size.Bit8()
    val MEM_ADDRESS_WIDTH = MutVal.Size.Bit32()

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

    // Assembler CONFIG
    val asmConfig = AsmConfig(
        RV32Grammar(),
        RV32Assembly(RV32BinMapper(), MutVal.Value.Hex("00001000", MutVal.Size.Bit32()), MutVal.Value.Hex("00002000", MutVal.Size.Bit32()), MutVal.Value.Hex("00003000", MutVal.Size.Bit32()))
    )

    val riscVDocs = Docs(
        Docs.HtmlFile.SourceFile(
            "Handbook",
            "../documents/riscv/handbook.html"
        ),
        Docs.HtmlFile.DefinedFile(
            "Implemented",
            FC {
                h1 {
                    +"Implemented"
                }
                h2 {
                    +"General"
                }
                ReactHTML.strong {
                    +"Registers"
                }
                ul {
                    li {
                        +"address-width: ${REG_ADDRESS_SIZE.bitWidth}bit"
                    }
                    li {
                        +"value-width: ${REG_VALUE_SIZE.bitWidth}bit"
                    }
                }
                ReactHTML.strong {
                    +"Memory"
                }
                ul {
                    li {
                        +"address-width: ${MEM_ADDRESS_WIDTH.bitWidth}bit"
                    }
                    li {
                        +"value-width: ${MEM_VALUE_WIDTH.bitWidth}bit"
                    }
                }
                h2 {
                    +"Instructions"
                }
                table {
                    thead {
                        tr {

                            th {
                                className = ClassName(StyleConst.Main.Table.CLASS_TXT_CENTER)
                                colSpan = 2
                                +"instruction"
                            }
                            th {
                                className = ClassName(StyleConst.Main.Table.CLASS_TXT_CENTER)
                                +"opcode"
                            }
                        }
                    }
                    tbody {
                        for (instr in RV32Grammar.R_INSTR.InstrType.entries) {
                            tr {

                                td {
                                    className = ClassName(StyleConst.Main.Table.CLASS_TXT_LEFT)
                                    +instr.id
                                }
                                td {
                                    className = ClassName(StyleConst.Main.Table.CLASS_TXT_LEFT)
                                    +"${instr.paramType.exampleString}\t"
                                }
                                td {
                                    css {
                                        float = Float.right
                                    }
                                    instr.opCode?.let {
                                        table {
                                            thead {
                                                tr {
                                                    for (mask in it.maskLabels) {
                                                        th {
                                                            className = ClassName(StyleConst.Main.Table.CLASS_TXT_CENTER)
                                                            +mask.name
                                                        }
                                                    }
                                                }
                                            }
                                            tbody {
                                                tr {
                                                    for (opcode in it.opMaskList) {
                                                        td {
                                                            className = ClassName(StyleConst.Main.Table.CLASS_TXT_CENTER)
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
                h2 {
                    +"Syntax Examples"
                }



            }
        )
    )

    // PROCESSOR CONFIG
    val config = Config(
        """RV32I""",
        riscVDocs,
        FileHandler("s"),
        RegisterContainer(
            listOf(
                RegisterContainer.RegisterFile(
                    "main", arrayOf(
                        RegisterContainer.Register(MutVal.Value.Dec("0", REG_ADDRESS_SIZE), listOf("x0"), listOf("zero"), MutVal(REG_INIT, REG_VALUE_SIZE), "hardwired zero", hardwire = true),
                        RegisterContainer.Register(MutVal.Value.Dec("1", REG_ADDRESS_SIZE), listOf("x1"), listOf("ra"), MutVal(REG_INIT, REG_VALUE_SIZE), "return address"),
                        RegisterContainer.Register(MutVal.Value.Dec("2", REG_ADDRESS_SIZE), listOf("x2"), listOf("sp"), MutVal(REG_INIT, REG_VALUE_SIZE), "stack pointer"),
                        RegisterContainer.Register(MutVal.Value.Dec("3", REG_ADDRESS_SIZE), listOf("x3"), listOf("gp"), MutVal(REG_INIT, REG_VALUE_SIZE), "global pointer"),
                        RegisterContainer.Register(MutVal.Value.Dec("4", REG_ADDRESS_SIZE), listOf("x4"), listOf("tp"), MutVal(REG_INIT, REG_VALUE_SIZE), "thread pointer"),
                        RegisterContainer.Register(MutVal.Value.Dec("5", REG_ADDRESS_SIZE), listOf("x5"), listOf("t0"), MutVal(REG_INIT, REG_VALUE_SIZE), "temporary register 0"),
                        RegisterContainer.Register(MutVal.Value.Dec("6", REG_ADDRESS_SIZE), listOf("x6"), listOf("t1"), MutVal(REG_INIT, REG_VALUE_SIZE), "temporary register 1"),
                        RegisterContainer.Register(MutVal.Value.Dec("7", REG_ADDRESS_SIZE), listOf("x7"), listOf("t2"), MutVal(REG_INIT, REG_VALUE_SIZE), "temporary register 2"),
                        RegisterContainer.Register(MutVal.Value.Dec("8", REG_ADDRESS_SIZE), listOf("x8"), listOf("s0", "fp"), MutVal(REG_INIT, REG_VALUE_SIZE), "saved register 0 / frame pointer"),
                        RegisterContainer.Register(MutVal.Value.Dec("9", REG_ADDRESS_SIZE), listOf("x9"), listOf("s1"), MutVal(REG_INIT, REG_VALUE_SIZE), "saved register 1"),
                        RegisterContainer.Register(MutVal.Value.Dec("10", REG_ADDRESS_SIZE), listOf("x10"), listOf("a0"), MutVal(REG_INIT, REG_VALUE_SIZE), "function argument 0 / return value 0"),
                        RegisterContainer.Register(MutVal.Value.Dec("11", REG_ADDRESS_SIZE), listOf("x11"), listOf("a1"), MutVal(REG_INIT, REG_VALUE_SIZE), "function argument 1 / return value 1"),
                        RegisterContainer.Register(MutVal.Value.Dec("12", REG_ADDRESS_SIZE), listOf("x12"), listOf("a2"), MutVal(REG_INIT, REG_VALUE_SIZE), "function argument 2"),
                        RegisterContainer.Register(MutVal.Value.Dec("13", REG_ADDRESS_SIZE), listOf("x13"), listOf("a3"), MutVal(REG_INIT, REG_VALUE_SIZE), "function argument 3"),
                        RegisterContainer.Register(MutVal.Value.Dec("14", REG_ADDRESS_SIZE), listOf("x14"), listOf("a4"), MutVal(REG_INIT, REG_VALUE_SIZE), "function argument 4"),
                        RegisterContainer.Register(MutVal.Value.Dec("15", REG_ADDRESS_SIZE), listOf("x15"), listOf("a5"), MutVal(REG_INIT, REG_VALUE_SIZE), "function argument 5"),
                        RegisterContainer.Register(MutVal.Value.Dec("16", REG_ADDRESS_SIZE), listOf("x16"), listOf("a6"), MutVal(REG_INIT, REG_VALUE_SIZE), "function argument 6"),
                        RegisterContainer.Register(MutVal.Value.Dec("17", REG_ADDRESS_SIZE), listOf("x17"), listOf("a7"), MutVal(REG_INIT, REG_VALUE_SIZE), "function argument 7"),
                        RegisterContainer.Register(MutVal.Value.Dec("18", REG_ADDRESS_SIZE), listOf("x18"), listOf("s2"), MutVal(REG_INIT, REG_VALUE_SIZE), "saved register 2"),
                        RegisterContainer.Register(MutVal.Value.Dec("19", REG_ADDRESS_SIZE), listOf("x19"), listOf("s3"), MutVal(REG_INIT, REG_VALUE_SIZE), "saved register 3"),
                        RegisterContainer.Register(MutVal.Value.Dec("20", REG_ADDRESS_SIZE), listOf("x20"), listOf("s4"), MutVal(REG_INIT, REG_VALUE_SIZE), "saved register 4"),
                        RegisterContainer.Register(MutVal.Value.Dec("21", REG_ADDRESS_SIZE), listOf("x21"), listOf("s5"), MutVal(REG_INIT, REG_VALUE_SIZE), "saved register 5"),
                        RegisterContainer.Register(MutVal.Value.Dec("22", REG_ADDRESS_SIZE), listOf("x22"), listOf("s6"), MutVal(REG_INIT, REG_VALUE_SIZE), "saved register 6"),
                        RegisterContainer.Register(MutVal.Value.Dec("23", REG_ADDRESS_SIZE), listOf("x23"), listOf("s7"), MutVal(REG_INIT, REG_VALUE_SIZE), "saved register 7"),
                        RegisterContainer.Register(MutVal.Value.Dec("24", REG_ADDRESS_SIZE), listOf("x24"), listOf("s8"), MutVal(REG_INIT, REG_VALUE_SIZE), "saved register 8"),
                        RegisterContainer.Register(MutVal.Value.Dec("25", REG_ADDRESS_SIZE), listOf("x25"), listOf("s9"), MutVal(REG_INIT, REG_VALUE_SIZE), "saved register 9"),
                        RegisterContainer.Register(MutVal.Value.Dec("26", REG_ADDRESS_SIZE), listOf("x26"), listOf("s10"), MutVal(REG_INIT, REG_VALUE_SIZE), "saved register 10"),
                        RegisterContainer.Register(MutVal.Value.Dec("27", REG_ADDRESS_SIZE), listOf("x27"), listOf("s11"), MutVal(REG_INIT, REG_VALUE_SIZE), "saved register 11"),
                        RegisterContainer.Register(MutVal.Value.Dec("28", REG_ADDRESS_SIZE), listOf("x28"), listOf("t3"), MutVal(REG_INIT, REG_VALUE_SIZE), "temporary register 3"),
                        RegisterContainer.Register(MutVal.Value.Dec("29", REG_ADDRESS_SIZE), listOf("x29"), listOf("t4"), MutVal(REG_INIT, REG_VALUE_SIZE), "temporary register 4"),
                        RegisterContainer.Register(MutVal.Value.Dec("30", REG_ADDRESS_SIZE), listOf("x30"), listOf("t5"), MutVal(REG_INIT, REG_VALUE_SIZE), "temporary register 5"),
                        RegisterContainer.Register(MutVal.Value.Dec("31", REG_ADDRESS_SIZE), listOf("x31"), listOf("t6"), MutVal(REG_INIT, REG_VALUE_SIZE), "temporary register 6")
                    )
                )
            ),
            pcSize = MutVal.Size.Bit32()
        ),
        Memory(MEM_ADDRESS_WIDTH, MEM_INIT, MEM_VALUE_WIDTH, Memory.Endianess.LittleEndian),
        Transcript(TS_COMPILED_HEADERS.entries.map { it.name }, TS_DISASSEMBLED_HEADERS.entries.map { it.name })
    )

    // EDITOR

    val FileExtension = "rvasm"
    val FileTypeDescription = "RISC-V Assembler"


}