package emulator.archs.riscv64

import emotion.react.css
import emulator.kit.common.*
import emulator.kit.configs.AsmConfig
import emulator.kit.configs.Config
import emulator.kit.types.Variable
import emulator.kit.types.Variable.Size.*
import react.FC
import react.dom.html.ReactHTML
import web.cssom.ClassName
import web.cssom.Float
import emulator.kit.common.RegContainer.Register
import emulator.kit.common.RegContainer.RegisterFile
import emulator.kit.common.RegContainer.CallingConvention
import emulator.kit.types.Variable.Value.*
import emulator.archs.riscv64.CSRegister.Privilege

object RV64 {

    val MEM_INIT: String = "0"
    val REG_INIT: String = "0"
    val XLEN = Bit64()
    val REG_VALUE_SIZE = XLEN
    val REG_ADDRESS_SIZE = Bit5()
    val CSR_REG_ADDRESS_SIZE = Bit12()
    val MEM_VALUE_WIDTH = Bit8()
    val MEM_ADDRESS_WIDTH = XLEN

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
         * TODO("Integrate more architecture extension packages")
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

    val asmConfig = AsmConfig(RV64Syntax(), RV64Assembly(RV64BinMapper(), Hex("00010000", MEM_ADDRESS_WIDTH), Hex("00020000", MEM_ADDRESS_WIDTH), Hex("00030000", MEM_ADDRESS_WIDTH)))

    val standardRegFile =RegisterFile(
        "main", arrayOf(
            Register(UDec("0", REG_ADDRESS_SIZE), listOf("x0"), listOf("zero"), Variable(REG_INIT, REG_VALUE_SIZE), description = "hardwired zero", hardwire = true),
            Register(UDec("1", REG_ADDRESS_SIZE), listOf("x1"), listOf("ra"), Variable(REG_INIT, REG_VALUE_SIZE), CallingConvention.CALLER, "return address, caller"),
            Register(UDec("2", REG_ADDRESS_SIZE), listOf("x2"), listOf("sp"), Variable(REG_INIT, REG_VALUE_SIZE), CallingConvention.CALLEE, "stack pointer, callee"),
            Register(UDec("3", REG_ADDRESS_SIZE), listOf("x3"), listOf("gp"), Variable(REG_INIT, REG_VALUE_SIZE), description = "global pointer"),
            Register(UDec("4", REG_ADDRESS_SIZE), listOf("x4"), listOf("tp"), Variable(REG_INIT, REG_VALUE_SIZE), description = "thread pointer"),
            Register(UDec("5", REG_ADDRESS_SIZE), listOf("x5"), listOf("t0"), Variable(REG_INIT, REG_VALUE_SIZE), CallingConvention.CALLER, "temporary register 0, caller"),
            Register(UDec("6", REG_ADDRESS_SIZE), listOf("x6"), listOf("t1"), Variable(REG_INIT, REG_VALUE_SIZE), CallingConvention.CALLER, "temporary register 1, caller"),
            Register(UDec("7", REG_ADDRESS_SIZE), listOf("x7"), listOf("t2"), Variable(REG_INIT, REG_VALUE_SIZE), CallingConvention.CALLER, "temporary register 2, caller"),
            Register(UDec("8", REG_ADDRESS_SIZE), listOf("x8"), listOf("s0", "fp"), Variable(REG_INIT, REG_VALUE_SIZE), CallingConvention.CALLEE, "saved register 0 / frame pointer, callee"),
            Register(UDec("9", REG_ADDRESS_SIZE), listOf("x9"), listOf("s1"), Variable(REG_INIT, REG_VALUE_SIZE), CallingConvention.CALLEE, "saved register 1, callee"),
            Register(UDec("10", REG_ADDRESS_SIZE), listOf("x10"), listOf("a0"), Variable(REG_INIT, REG_VALUE_SIZE), CallingConvention.CALLER, "function argument 0 / return value 0, caller"),
            Register(UDec("11", REG_ADDRESS_SIZE), listOf("x11"), listOf("a1"), Variable(REG_INIT, REG_VALUE_SIZE), CallingConvention.CALLER, "function argument 1 / return value 1, caller"),
            Register(UDec("12", REG_ADDRESS_SIZE), listOf("x12"), listOf("a2"), Variable(REG_INIT, REG_VALUE_SIZE), CallingConvention.CALLER, "function argument 2, caller"),
            Register(UDec("13", REG_ADDRESS_SIZE), listOf("x13"), listOf("a3"), Variable(REG_INIT, REG_VALUE_SIZE), CallingConvention.CALLER, "function argument 3, caller"),
            Register(UDec("14", REG_ADDRESS_SIZE), listOf("x14"), listOf("a4"), Variable(REG_INIT, REG_VALUE_SIZE), CallingConvention.CALLER, "function argument 4, caller"),
            Register(UDec("15", REG_ADDRESS_SIZE), listOf("x15"), listOf("a5"), Variable(REG_INIT, REG_VALUE_SIZE), CallingConvention.CALLER, "function argument 5, caller"),
            Register(UDec("16", REG_ADDRESS_SIZE), listOf("x16"), listOf("a6"), Variable(REG_INIT, REG_VALUE_SIZE), CallingConvention.CALLER, "function argument 6, caller"),
            Register(UDec("17", REG_ADDRESS_SIZE), listOf("x17"), listOf("a7"), Variable(REG_INIT, REG_VALUE_SIZE), CallingConvention.CALLER, "function argument 7, caller"),
            Register(UDec("18", REG_ADDRESS_SIZE), listOf("x18"), listOf("s2"), Variable(REG_INIT, REG_VALUE_SIZE), CallingConvention.CALLEE, "saved register 2, callee"),
            Register(UDec("19", REG_ADDRESS_SIZE), listOf("x19"), listOf("s3"), Variable(REG_INIT, REG_VALUE_SIZE), CallingConvention.CALLEE, "saved register 3, callee"),
            Register(UDec("20", REG_ADDRESS_SIZE), listOf("x20"), listOf("s4"), Variable(REG_INIT, REG_VALUE_SIZE), CallingConvention.CALLEE, "saved register 4, callee"),
            Register(UDec("21", REG_ADDRESS_SIZE), listOf("x21"), listOf("s5"), Variable(REG_INIT, REG_VALUE_SIZE), CallingConvention.CALLEE, "saved register 5, callee"),
            Register(UDec("22", REG_ADDRESS_SIZE), listOf("x22"), listOf("s6"), Variable(REG_INIT, REG_VALUE_SIZE), CallingConvention.CALLEE, "saved register 6, callee"),
            Register(UDec("23", REG_ADDRESS_SIZE), listOf("x23"), listOf("s7"), Variable(REG_INIT, REG_VALUE_SIZE), CallingConvention.CALLEE, "saved register 7, callee"),
            Register(UDec("24", REG_ADDRESS_SIZE), listOf("x24"), listOf("s8"), Variable(REG_INIT, REG_VALUE_SIZE), CallingConvention.CALLEE, "saved register 8, callee"),
            Register(UDec("25", REG_ADDRESS_SIZE), listOf("x25"), listOf("s9"), Variable(REG_INIT, REG_VALUE_SIZE), CallingConvention.CALLEE, "saved register 9, callee"),
            Register(UDec("26", REG_ADDRESS_SIZE), listOf("x26"), listOf("s10"), Variable(REG_INIT, REG_VALUE_SIZE), CallingConvention.CALLEE, "saved register 10, callee"),
            Register(UDec("27", REG_ADDRESS_SIZE), listOf("x27"), listOf("s11"), Variable(REG_INIT, REG_VALUE_SIZE), CallingConvention.CALLEE, "saved register 11, callee"),
            Register(UDec("28", REG_ADDRESS_SIZE), listOf("x28"), listOf("t3"), Variable(REG_INIT, REG_VALUE_SIZE), CallingConvention.CALLER, "temporary register 3, caller"),
            Register(UDec("29", REG_ADDRESS_SIZE), listOf("x29"), listOf("t4"), Variable(REG_INIT, REG_VALUE_SIZE), CallingConvention.CALLER, "temporary register 4, caller"),
            Register(UDec("30", REG_ADDRESS_SIZE), listOf("x30"), listOf("t5"), Variable(REG_INIT, REG_VALUE_SIZE), CallingConvention.CALLER, "temporary register 5, caller"),
            Register(UDec("31", REG_ADDRESS_SIZE), listOf("x31"), listOf("t6"), Variable(REG_INIT, REG_VALUE_SIZE), CallingConvention.CALLER, "temporary register 6, caller")
        )
    )

    val csrRegFile = RegisterFile(
        "csr", arrayOf(
            // Unprivileged Floating-Point CSRs
            CSRegister(Hex("001", CSR_REG_ADDRESS_SIZE), Privilege.URW, listOf("x001"), listOf("fflags"), Variable(REG_INIT, XLEN), "Floating-Point Accrued Exceptions."),
            CSRegister(Hex("002", CSR_REG_ADDRESS_SIZE), Privilege.URW, listOf("x002"), listOf("frm"), Variable(REG_INIT, XLEN), "Floating-Point Dynamic Rounding Mode."),
            CSRegister(Hex("003", CSR_REG_ADDRESS_SIZE), Privilege.URW, listOf("x003"), listOf("fcsr"), Variable(REG_INIT, XLEN), "Floating-Point Control and Status Register (frm + fflags)."),

            // Supervisor Trap Setup
            CSRegister(Hex("100", CSR_REG_ADDRESS_SIZE), Privilege.SRW, listOf("x100"), listOf("sstatus"), Variable(REG_INIT, XLEN), "Supervisor status register."),
            CSRegister(Hex("104", CSR_REG_ADDRESS_SIZE), Privilege.SRW, listOf("x104"), listOf("sie"), Variable(REG_INIT, XLEN), "Supervisor interrupt-enable register."),
            CSRegister(Hex("105", CSR_REG_ADDRESS_SIZE), Privilege.SRW, listOf("x105"), listOf("stvec"), Variable(REG_INIT, XLEN), "Supervisor trap handler base address."),
            CSRegister(Hex("106", CSR_REG_ADDRESS_SIZE), Privilege.SRW, listOf("x106"), listOf("scounteren"), Variable(REG_INIT, XLEN), "Supervisor counter enable."),
            // Supervisor Configuration
            CSRegister(Hex("10A", CSR_REG_ADDRESS_SIZE), Privilege.SRW, listOf("x10A"), listOf("senvcfg"), Variable(REG_INIT, XLEN), "Supervisor environment configuration register."),
            // Supervisor Trap Handling
            CSRegister(Hex("140", CSR_REG_ADDRESS_SIZE), Privilege.SRW, listOf("x140"), listOf("sscratch"), Variable(REG_INIT, XLEN), "Scratch register for supervisor trap handlers."),
            CSRegister(Hex("141", CSR_REG_ADDRESS_SIZE), Privilege.SRW, listOf("x141"), listOf("sepc"), Variable(REG_INIT, XLEN), "Supervisor exception program counter."),
            CSRegister(Hex("142", CSR_REG_ADDRESS_SIZE), Privilege.SRW, listOf("x142"), listOf("scause"), Variable(REG_INIT, XLEN), "Supervisor trap cause."),
            CSRegister(Hex("143", CSR_REG_ADDRESS_SIZE), Privilege.SRW, listOf("x143"), listOf("stval"), Variable(REG_INIT, XLEN), "Supervisor bad address or instruction."),
            CSRegister(Hex("144", CSR_REG_ADDRESS_SIZE), Privilege.SRW, listOf("x144"), listOf("sip"), Variable(REG_INIT, XLEN), "Supervisor interrupt pending."),
            // Supervisor Protection and Translation
            CSRegister(Hex("180", CSR_REG_ADDRESS_SIZE), Privilege.SRW, listOf("x180"), listOf("satp"), Variable(REG_INIT, XLEN), "Supervisor address translation and protection."),
            // Debug/Trace Registers
            CSRegister(Hex("5A8", CSR_REG_ADDRESS_SIZE), Privilege.SRW, listOf("x5A8"), listOf("scontext"), Variable(REG_INIT, XLEN), "Supervisor-mode context register."),

            // TODO("Add Hypervisor Registers")

            // Machine-Level CSR
            // Machine Information Registers
            CSRegister(Hex("F11", CSR_REG_ADDRESS_SIZE), Privilege.MRO, listOf("xF11"), listOf("mvendorid"), Variable(REG_INIT, XLEN), "Vendor ID."),
            CSRegister(Hex("F12", CSR_REG_ADDRESS_SIZE), Privilege.MRO, listOf("xF12"), listOf("marchid"), Variable(REG_INIT, XLEN), "Architecture ID."),
            CSRegister(Hex("F13", CSR_REG_ADDRESS_SIZE), Privilege.MRO, listOf("xF13"), listOf("mimpid"), Variable(REG_INIT, XLEN), "Implementation ID."),
            CSRegister(Hex("F14", CSR_REG_ADDRESS_SIZE), Privilege.MRO, listOf("xF14"), listOf("mhartid"), Variable(REG_INIT, XLEN), "Hardware thread ID."),
            CSRegister(Hex("F15", CSR_REG_ADDRESS_SIZE), Privilege.MRO, listOf("xF15"), listOf("mconfigptr"), Variable(REG_INIT, XLEN), "Pointer to configuration data structure."),
            // Machine Trap Setup
            CSRegister(Hex("300", CSR_REG_ADDRESS_SIZE), Privilege.MRW, listOf("x300"), listOf("mstatus"), Variable(REG_INIT, XLEN), "Machine status register."),
            CSRegister(Hex("301", CSR_REG_ADDRESS_SIZE), Privilege.MRW, listOf("x301"), listOf("misa"), Variable(REG_INIT, XLEN), "ISA and extensions."),
            CSRegister(Hex("302", CSR_REG_ADDRESS_SIZE), Privilege.MRW, listOf("x302"), listOf("medeleg"), Variable(REG_INIT, XLEN), "Machine exception delegation register."),
            CSRegister(Hex("303", CSR_REG_ADDRESS_SIZE), Privilege.MRW, listOf("x303"), listOf("mideleg"), Variable(REG_INIT, XLEN), "Machine interrupt delegation register."),
            CSRegister(Hex("304", CSR_REG_ADDRESS_SIZE), Privilege.MRW, listOf("x304"), listOf("mie"), Variable(REG_INIT, XLEN), "Machine interrupt-enable register."),
            CSRegister(Hex("305", CSR_REG_ADDRESS_SIZE), Privilege.MRW, listOf("x305"), listOf("mtvec"), Variable(REG_INIT, XLEN), "Machine trap-handler base address."),
            CSRegister(Hex("306", CSR_REG_ADDRESS_SIZE), Privilege.MRW, listOf("x306"), listOf("mcounteren"), Variable(REG_INIT, XLEN), "Machine counter enable."),
            // RV32 Only CSRegister(Hex("310", CSR_REG_ADDRESS_SIZE), Privilege.MRW, listOf("x310"), listOf("mstatush"), Variable(REG_INIT, XLEN), "Additional machine status register. (RV32)"),
            // Machine Trap Handling
            CSRegister(Hex("340", CSR_REG_ADDRESS_SIZE), Privilege.MRW, listOf("x340"), listOf("mscratch"), Variable(REG_INIT, XLEN), "Scratch register for machine trap handlers."),
            CSRegister(Hex("341", CSR_REG_ADDRESS_SIZE), Privilege.MRW, listOf("x341"), listOf("mepc"), Variable(REG_INIT, XLEN), "Machine exception program counter."),
            CSRegister(Hex("342", CSR_REG_ADDRESS_SIZE), Privilege.MRW, listOf("x342"), listOf("mcause"), Variable(REG_INIT, XLEN), "Machine trap cause."),
            CSRegister(Hex("343", CSR_REG_ADDRESS_SIZE), Privilege.MRW, listOf("x343"), listOf("mtval"), Variable(REG_INIT, XLEN), "Machine bad address or instruction."),
            CSRegister(Hex("344", CSR_REG_ADDRESS_SIZE), Privilege.MRW, listOf("x344"), listOf("mip"), Variable(REG_INIT, XLEN), "Machine interrupt pending."),
            CSRegister(Hex("34A", CSR_REG_ADDRESS_SIZE), Privilege.MRW, listOf("x34A"), listOf("mtinst"), Variable(REG_INIT, XLEN), "Machine trap instruction (transformed)."),
            CSRegister(Hex("34B", CSR_REG_ADDRESS_SIZE), Privilege.MRW, listOf("x34B"), listOf("mtval2"), Variable(REG_INIT, XLEN), "Machine bad guest physical address."),
            // Machine Configuration
            CSRegister(Hex("30A", CSR_REG_ADDRESS_SIZE), Privilege.MRW, listOf("x30A"), listOf("menvcfg"), Variable(REG_INIT, XLEN), "Machine environment configuration register."),
            CSRegister(Hex("747", CSR_REG_ADDRESS_SIZE), Privilege.MRW, listOf("x747"), listOf("mseccfg"), Variable(REG_INIT, XLEN), "Machine security configuration register."),
            // Machine Memory Protection
            CSRegister(Hex("3A0", CSR_REG_ADDRESS_SIZE), Privilege.MRW, listOf("x3A0"), listOf("pmpcfg0"), Variable(REG_INIT, XLEN), "Physical memory protection configuration."),
            CSRegister(Hex("3A2", CSR_REG_ADDRESS_SIZE), Privilege.MRW, listOf("x3A2"), listOf("pmpcfg2"), Variable(REG_INIT, XLEN), "Physical memory protection configuration."),
            CSRegister(Hex("3A4", CSR_REG_ADDRESS_SIZE), Privilege.MRW, listOf("x3A4"), listOf("pmpcfg4"), Variable(REG_INIT, XLEN), "Physical memory protection configuration."),
            CSRegister(Hex("3A6", CSR_REG_ADDRESS_SIZE), Privilege.MRW, listOf("x3A6"), listOf("pmpcfg6"), Variable(REG_INIT, XLEN), "Physical memory protection configuration."),
            CSRegister(Hex("3A8", CSR_REG_ADDRESS_SIZE), Privilege.MRW, listOf("x3A8"), listOf("pmpcfg8"), Variable(REG_INIT, XLEN), "Physical memory protection configuration."),
            CSRegister(Hex("3AA", CSR_REG_ADDRESS_SIZE), Privilege.MRW, listOf("x3AA"), listOf("pmpcfg10"), Variable(REG_INIT, XLEN), "Physical memory protection configuration."),
            CSRegister(Hex("3AC", CSR_REG_ADDRESS_SIZE), Privilege.MRW, listOf("x3AC"), listOf("pmpcfg12"), Variable(REG_INIT, XLEN), "Physical memory protection configuration."),
            CSRegister(Hex("3AE", CSR_REG_ADDRESS_SIZE), Privilege.MRW, listOf("x3AE"), listOf("pmpcfg14"), Variable(REG_INIT, XLEN), "Physical memory protection configuration."),

            CSRegister(Hex("3B0", CSR_REG_ADDRESS_SIZE), Privilege.MRW, listOf("x3B0"), listOf("pmpaddr0"), Variable(REG_INIT, XLEN), "Physical memory protection address register."),
            CSRegister(Hex("3B1", CSR_REG_ADDRESS_SIZE), Privilege.MRW, listOf("x3B1"), listOf("pmpaddr1"), Variable(REG_INIT, XLEN), "Physical memory protection address register."),
            CSRegister(Hex("3B2", CSR_REG_ADDRESS_SIZE), Privilege.MRW, listOf("x3B2"), listOf("pmpaddr2"), Variable(REG_INIT, XLEN), "Physical memory protection address register."),
            CSRegister(Hex("3B3", CSR_REG_ADDRESS_SIZE), Privilege.MRW, listOf("x3B3"), listOf("pmpaddr3"), Variable(REG_INIT, XLEN), "Physical memory protection address register."),
            CSRegister(Hex("3B4", CSR_REG_ADDRESS_SIZE), Privilege.MRW, listOf("x3B4"), listOf("pmpaddr4"), Variable(REG_INIT, XLEN), "Physical memory protection address register."),
            CSRegister(Hex("3B5", CSR_REG_ADDRESS_SIZE), Privilege.MRW, listOf("x3B5"), listOf("pmpaddr5"), Variable(REG_INIT, XLEN), "Physical memory protection address register."),
            CSRegister(Hex("3B6", CSR_REG_ADDRESS_SIZE), Privilege.MRW, listOf("x3B6"), listOf("pmpaddr6"), Variable(REG_INIT, XLEN), "Physical memory protection address register."),
            CSRegister(Hex("3B7", CSR_REG_ADDRESS_SIZE), Privilege.MRW, listOf("x3B7"), listOf("pmpaddr7"), Variable(REG_INIT, XLEN), "Physical memory protection address register."),
            CSRegister(Hex("3B8", CSR_REG_ADDRESS_SIZE), Privilege.MRW, listOf("x3B8"), listOf("pmpaddr8"), Variable(REG_INIT, XLEN), "Physical memory protection address register."),
            CSRegister(Hex("3B9", CSR_REG_ADDRESS_SIZE), Privilege.MRW, listOf("x3B9"), listOf("pmpaddr9"), Variable(REG_INIT, XLEN), "Physical memory protection address register."),
            CSRegister(Hex("3BA", CSR_REG_ADDRESS_SIZE), Privilege.MRW, listOf("x3BA"), listOf("pmpaddr10"), Variable(REG_INIT, XLEN), "Physical memory protection address register."),
            CSRegister(Hex("3BB", CSR_REG_ADDRESS_SIZE), Privilege.MRW, listOf("x3BB"), listOf("pmpaddr11"), Variable(REG_INIT, XLEN), "Physical memory protection address register."),
            CSRegister(Hex("3BC", CSR_REG_ADDRESS_SIZE), Privilege.MRW, listOf("x3BC"), listOf("pmpaddr12"), Variable(REG_INIT, XLEN), "Physical memory protection address register."),
            CSRegister(Hex("3BD", CSR_REG_ADDRESS_SIZE), Privilege.MRW, listOf("x3BD"), listOf("pmpaddr13"), Variable(REG_INIT, XLEN), "Physical memory protection address register."),
            CSRegister(Hex("3BE", CSR_REG_ADDRESS_SIZE), Privilege.MRW, listOf("x3BE"), listOf("pmpaddr14"), Variable(REG_INIT, XLEN), "Physical memory protection address register."),
            CSRegister(Hex("3BF", CSR_REG_ADDRESS_SIZE), Privilege.MRW, listOf("x3BF"), listOf("pmpaddr15"), Variable(REG_INIT, XLEN), "Physical memory protection address register."),

            CSRegister(Hex("3C0", CSR_REG_ADDRESS_SIZE), Privilege.MRW, listOf("x3C0"), listOf("pmpaddr16"), Variable(REG_INIT, XLEN), "Physical memory protection address register."),
            CSRegister(Hex("3C1", CSR_REG_ADDRESS_SIZE), Privilege.MRW, listOf("x3C1"), listOf("pmpaddr17"), Variable(REG_INIT, XLEN), "Physical memory protection address register."),
            CSRegister(Hex("3C2", CSR_REG_ADDRESS_SIZE), Privilege.MRW, listOf("x3C2"), listOf("pmpaddr18"), Variable(REG_INIT, XLEN), "Physical memory protection address register."),
            CSRegister(Hex("3C3", CSR_REG_ADDRESS_SIZE), Privilege.MRW, listOf("x3C3"), listOf("pmpaddr19"), Variable(REG_INIT, XLEN), "Physical memory protection address register."),
            CSRegister(Hex("3C4", CSR_REG_ADDRESS_SIZE), Privilege.MRW, listOf("x3C4"), listOf("pmpaddr20"), Variable(REG_INIT, XLEN), "Physical memory protection address register."),
            CSRegister(Hex("3C5", CSR_REG_ADDRESS_SIZE), Privilege.MRW, listOf("x3C5"), listOf("pmpaddr21"), Variable(REG_INIT, XLEN), "Physical memory protection address register."),
            CSRegister(Hex("3C6", CSR_REG_ADDRESS_SIZE), Privilege.MRW, listOf("x3C6"), listOf("pmpaddr22"), Variable(REG_INIT, XLEN), "Physical memory protection address register."),
            CSRegister(Hex("3C7", CSR_REG_ADDRESS_SIZE), Privilege.MRW, listOf("x3C7"), listOf("pmpaddr23"), Variable(REG_INIT, XLEN), "Physical memory protection address register."),
            CSRegister(Hex("3C8", CSR_REG_ADDRESS_SIZE), Privilege.MRW, listOf("x3C8"), listOf("pmpaddr24"), Variable(REG_INIT, XLEN), "Physical memory protection address register."),
            CSRegister(Hex("3C9", CSR_REG_ADDRESS_SIZE), Privilege.MRW, listOf("x3C9"), listOf("pmpaddr25"), Variable(REG_INIT, XLEN), "Physical memory protection address register."),
            CSRegister(Hex("3CA", CSR_REG_ADDRESS_SIZE), Privilege.MRW, listOf("x3CA"), listOf("pmpaddr26"), Variable(REG_INIT, XLEN), "Physical memory protection address register."),
            CSRegister(Hex("3CB", CSR_REG_ADDRESS_SIZE), Privilege.MRW, listOf("x3CB"), listOf("pmpaddr27"), Variable(REG_INIT, XLEN), "Physical memory protection address register."),
            CSRegister(Hex("3CC", CSR_REG_ADDRESS_SIZE), Privilege.MRW, listOf("x3CC"), listOf("pmpaddr28"), Variable(REG_INIT, XLEN), "Physical memory protection address register."),
            CSRegister(Hex("3CD", CSR_REG_ADDRESS_SIZE), Privilege.MRW, listOf("x3CD"), listOf("pmpaddr29"), Variable(REG_INIT, XLEN), "Physical memory protection address register."),
            CSRegister(Hex("3CE", CSR_REG_ADDRESS_SIZE), Privilege.MRW, listOf("x3CE"), listOf("pmpaddr30"), Variable(REG_INIT, XLEN), "Physical memory protection address register."),
            CSRegister(Hex("3CF", CSR_REG_ADDRESS_SIZE), Privilege.MRW, listOf("x3CF"), listOf("pmpaddr31"), Variable(REG_INIT, XLEN), "Physical memory protection address register."),

            CSRegister(Hex("3D0", CSR_REG_ADDRESS_SIZE), Privilege.MRW, listOf("x3D0"), listOf("pmpaddr32"), Variable(REG_INIT, XLEN), "Physical memory protection address register."),
            CSRegister(Hex("3D1", CSR_REG_ADDRESS_SIZE), Privilege.MRW, listOf("x3D1"), listOf("pmpaddr33"), Variable(REG_INIT, XLEN), "Physical memory protection address register."),
            CSRegister(Hex("3D2", CSR_REG_ADDRESS_SIZE), Privilege.MRW, listOf("x3D2"), listOf("pmpaddr34"), Variable(REG_INIT, XLEN), "Physical memory protection address register."),
            CSRegister(Hex("3D3", CSR_REG_ADDRESS_SIZE), Privilege.MRW, listOf("x3D3"), listOf("pmpaddr35"), Variable(REG_INIT, XLEN), "Physical memory protection address register."),
            CSRegister(Hex("3D4", CSR_REG_ADDRESS_SIZE), Privilege.MRW, listOf("x3D4"), listOf("pmpaddr36"), Variable(REG_INIT, XLEN), "Physical memory protection address register."),
            CSRegister(Hex("3D5", CSR_REG_ADDRESS_SIZE), Privilege.MRW, listOf("x3D5"), listOf("pmpaddr37"), Variable(REG_INIT, XLEN), "Physical memory protection address register."),
            CSRegister(Hex("3D6", CSR_REG_ADDRESS_SIZE), Privilege.MRW, listOf("x3D6"), listOf("pmpaddr38"), Variable(REG_INIT, XLEN), "Physical memory protection address register."),
            CSRegister(Hex("3D7", CSR_REG_ADDRESS_SIZE), Privilege.MRW, listOf("x3D7"), listOf("pmpaddr39"), Variable(REG_INIT, XLEN), "Physical memory protection address register."),
            CSRegister(Hex("3D8", CSR_REG_ADDRESS_SIZE), Privilege.MRW, listOf("x3D8"), listOf("pmpaddr40"), Variable(REG_INIT, XLEN), "Physical memory protection address register."),
            CSRegister(Hex("3D9", CSR_REG_ADDRESS_SIZE), Privilege.MRW, listOf("x3D9"), listOf("pmpaddr41"), Variable(REG_INIT, XLEN), "Physical memory protection address register."),
            CSRegister(Hex("3DA", CSR_REG_ADDRESS_SIZE), Privilege.MRW, listOf("x3DA"), listOf("pmpaddr42"), Variable(REG_INIT, XLEN), "Physical memory protection address register."),
            CSRegister(Hex("3DB", CSR_REG_ADDRESS_SIZE), Privilege.MRW, listOf("x3DB"), listOf("pmpaddr43"), Variable(REG_INIT, XLEN), "Physical memory protection address register."),
            CSRegister(Hex("3DC", CSR_REG_ADDRESS_SIZE), Privilege.MRW, listOf("x3DC"), listOf("pmpaddr44"), Variable(REG_INIT, XLEN), "Physical memory protection address register."),
            CSRegister(Hex("3DD", CSR_REG_ADDRESS_SIZE), Privilege.MRW, listOf("x3DD"), listOf("pmpaddr45"), Variable(REG_INIT, XLEN), "Physical memory protection address register."),
            CSRegister(Hex("3DE", CSR_REG_ADDRESS_SIZE), Privilege.MRW, listOf("x3DE"), listOf("pmpaddr46"), Variable(REG_INIT, XLEN), "Physical memory protection address register."),
            CSRegister(Hex("3DF", CSR_REG_ADDRESS_SIZE), Privilege.MRW, listOf("x3DF"), listOf("pmpaddr47"), Variable(REG_INIT, XLEN), "Physical memory protection address register."),

            CSRegister(Hex("3E0", CSR_REG_ADDRESS_SIZE), Privilege.MRW, listOf("x3E0"), listOf("pmpaddr48"), Variable(REG_INIT, XLEN), "Physical memory protection address register."),
            CSRegister(Hex("3E1", CSR_REG_ADDRESS_SIZE), Privilege.MRW, listOf("x3E1"), listOf("pmpaddr49"), Variable(REG_INIT, XLEN), "Physical memory protection address register."),
            CSRegister(Hex("3E2", CSR_REG_ADDRESS_SIZE), Privilege.MRW, listOf("x3E2"), listOf("pmpaddr50"), Variable(REG_INIT, XLEN), "Physical memory protection address register."),
            CSRegister(Hex("3E3", CSR_REG_ADDRESS_SIZE), Privilege.MRW, listOf("x3E3"), listOf("pmpaddr51"), Variable(REG_INIT, XLEN), "Physical memory protection address register."),
            CSRegister(Hex("3E4", CSR_REG_ADDRESS_SIZE), Privilege.MRW, listOf("x3E4"), listOf("pmpaddr52"), Variable(REG_INIT, XLEN), "Physical memory protection address register."),
            CSRegister(Hex("3E5", CSR_REG_ADDRESS_SIZE), Privilege.MRW, listOf("x3E5"), listOf("pmpaddr53"), Variable(REG_INIT, XLEN), "Physical memory protection address register."),
            CSRegister(Hex("3E6", CSR_REG_ADDRESS_SIZE), Privilege.MRW, listOf("x3E6"), listOf("pmpaddr54"), Variable(REG_INIT, XLEN), "Physical memory protection address register."),
            CSRegister(Hex("3E7", CSR_REG_ADDRESS_SIZE), Privilege.MRW, listOf("x3E7"), listOf("pmpaddr55"), Variable(REG_INIT, XLEN), "Physical memory protection address register."),
            CSRegister(Hex("3E8", CSR_REG_ADDRESS_SIZE), Privilege.MRW, listOf("x3E8"), listOf("pmpaddr56"), Variable(REG_INIT, XLEN), "Physical memory protection address register."),
            CSRegister(Hex("3E9", CSR_REG_ADDRESS_SIZE), Privilege.MRW, listOf("x3E9"), listOf("pmpaddr57"), Variable(REG_INIT, XLEN), "Physical memory protection address register."),
            CSRegister(Hex("3EA", CSR_REG_ADDRESS_SIZE), Privilege.MRW, listOf("x3EA"), listOf("pmpaddr58"), Variable(REG_INIT, XLEN), "Physical memory protection address register."),
            CSRegister(Hex("3EB", CSR_REG_ADDRESS_SIZE), Privilege.MRW, listOf("x3EB"), listOf("pmpaddr59"), Variable(REG_INIT, XLEN), "Physical memory protection address register."),
            CSRegister(Hex("3EC", CSR_REG_ADDRESS_SIZE), Privilege.MRW, listOf("x3EC"), listOf("pmpaddr60"), Variable(REG_INIT, XLEN), "Physical memory protection address register."),
            CSRegister(Hex("3ED", CSR_REG_ADDRESS_SIZE), Privilege.MRW, listOf("x3ED"), listOf("pmpaddr61"), Variable(REG_INIT, XLEN), "Physical memory protection address register."),
            CSRegister(Hex("3EE", CSR_REG_ADDRESS_SIZE), Privilege.MRW, listOf("x3EE"), listOf("pmpaddr62"), Variable(REG_INIT, XLEN), "Physical memory protection address register."),
            CSRegister(Hex("3EF", CSR_REG_ADDRESS_SIZE), Privilege.MRW, listOf("x3EF"), listOf("pmpaddr63"), Variable(REG_INIT, XLEN), "Physical memory protection address register."),
            // TODO("FF Machine Counter/Timers Page 25")



            // Unprivileged Counter/Timers
            CSRegister(Hex("C00", CSR_REG_ADDRESS_SIZE), Privilege.URO, listOf("xC00"), listOf("cycle"), Variable(REG_INIT, XLEN), "Cycle counter for RDCYCLE instruction."),
            CSRegister(Hex("C01", CSR_REG_ADDRESS_SIZE), Privilege.URO, listOf("xC01"), listOf("time"), Variable(REG_INIT, XLEN), "Timer for RDTIME instruction."),
            CSRegister(Hex("C02", CSR_REG_ADDRESS_SIZE), Privilege.URO, listOf("xC02"), listOf("instret"), Variable(REG_INIT, XLEN), "Instructions-retired counter for RDINSTRET instruction."),
            CSRegister(Hex("C03", CSR_REG_ADDRESS_SIZE), Privilege.URO, listOf("xC03"), listOf("hpmcounter3"), Variable(REG_INIT, XLEN), "Performance-monitoring counter."),
            CSRegister(Hex("C04", CSR_REG_ADDRESS_SIZE), Privilege.URO, listOf("xC04"), listOf("hpmcounter4"), Variable(REG_INIT, XLEN), "Performance-monitoring coutner."),
            CSRegister(Hex("C05", CSR_REG_ADDRESS_SIZE), Privilege.URO, listOf("xC05"), listOf("hpmcounter5"), Variable(REG_INIT, XLEN), "Performance-monitoring coutner."),
            CSRegister(Hex("C06", CSR_REG_ADDRESS_SIZE), Privilege.URO, listOf("xC06"), listOf("hpmcounter6"), Variable(REG_INIT, XLEN), "Performance-monitoring coutner."),
            CSRegister(Hex("C07", CSR_REG_ADDRESS_SIZE), Privilege.URO, listOf("xC07"), listOf("hpmcounter7"), Variable(REG_INIT, XLEN), "Performance-monitoring coutner."),
            CSRegister(Hex("C08", CSR_REG_ADDRESS_SIZE), Privilege.URO, listOf("xC08"), listOf("hpmcounter8"), Variable(REG_INIT, XLEN), "Performance-monitoring coutner."),
            CSRegister(Hex("C09", CSR_REG_ADDRESS_SIZE), Privilege.URO, listOf("xC09"), listOf("hpmcounter9"), Variable(REG_INIT, XLEN), "Performance-monitoring coutner."),
            CSRegister(Hex("C0A", CSR_REG_ADDRESS_SIZE), Privilege.URO, listOf("xC0A"), listOf("hpmcounter10"), Variable(REG_INIT, XLEN), "Performance-monitoring coutner."),
            CSRegister(Hex("C0B", CSR_REG_ADDRESS_SIZE), Privilege.URO, listOf("xC0B"), listOf("hpmcounter11"), Variable(REG_INIT, XLEN), "Performance-monitoring coutner."),
            CSRegister(Hex("C0C", CSR_REG_ADDRESS_SIZE), Privilege.URO, listOf("xC0C"), listOf("hpmcounter12"), Variable(REG_INIT, XLEN), "Performance-monitoring coutner."),
            CSRegister(Hex("C0D", CSR_REG_ADDRESS_SIZE), Privilege.URO, listOf("xC0D"), listOf("hpmcounter13"), Variable(REG_INIT, XLEN), "Performance-monitoring coutner."),
            CSRegister(Hex("C0E", CSR_REG_ADDRESS_SIZE), Privilege.URO, listOf("xC0E"), listOf("hpmcounter14"), Variable(REG_INIT, XLEN), "Performance-monitoring coutner."),
            CSRegister(Hex("C0F", CSR_REG_ADDRESS_SIZE), Privilege.URO, listOf("xC0F"), listOf("hpmcounter15"), Variable(REG_INIT, XLEN), "Performance-monitoring coutner."),
            CSRegister(Hex("C10", CSR_REG_ADDRESS_SIZE), Privilege.URO, listOf("xC10"), listOf("hpmcounter16"), Variable(REG_INIT, XLEN), "Performance-monitoring coutner."),
            CSRegister(Hex("C11", CSR_REG_ADDRESS_SIZE), Privilege.URO, listOf("xC11"), listOf("hpmcounter17"), Variable(REG_INIT, XLEN), "Performance-monitoring coutner."),
            CSRegister(Hex("C12", CSR_REG_ADDRESS_SIZE), Privilege.URO, listOf("xC12"), listOf("hpmcounter18"), Variable(REG_INIT, XLEN), "Performance-monitoring coutner."),
            CSRegister(Hex("C13", CSR_REG_ADDRESS_SIZE), Privilege.URO, listOf("xC13"), listOf("hpmcounter19"), Variable(REG_INIT, XLEN), "Performance-monitoring coutner."),
            CSRegister(Hex("C14", CSR_REG_ADDRESS_SIZE), Privilege.URO, listOf("xC14"), listOf("hpmcounter20"), Variable(REG_INIT, XLEN), "Performance-monitoring coutner."),
            CSRegister(Hex("C15", CSR_REG_ADDRESS_SIZE), Privilege.URO, listOf("xC15"), listOf("hpmcounter21"), Variable(REG_INIT, XLEN), "Performance-monitoring coutner."),
            CSRegister(Hex("C16", CSR_REG_ADDRESS_SIZE), Privilege.URO, listOf("xC16"), listOf("hpmcounter22"), Variable(REG_INIT, XLEN), "Performance-monitoring coutner."),
            CSRegister(Hex("C17", CSR_REG_ADDRESS_SIZE), Privilege.URO, listOf("xC17"), listOf("hpmcounter23"), Variable(REG_INIT, XLEN), "Performance-monitoring coutner."),
            CSRegister(Hex("C18", CSR_REG_ADDRESS_SIZE), Privilege.URO, listOf("xC18"), listOf("hpmcounter24"), Variable(REG_INIT, XLEN), "Performance-monitoring coutner."),
            CSRegister(Hex("C19", CSR_REG_ADDRESS_SIZE), Privilege.URO, listOf("xC19"), listOf("hpmcounter25"), Variable(REG_INIT, XLEN), "Performance-monitoring coutner."),
            CSRegister(Hex("C1A", CSR_REG_ADDRESS_SIZE), Privilege.URO, listOf("xC1A"), listOf("hpmcounter26"), Variable(REG_INIT, XLEN), "Performance-monitoring coutner."),
            CSRegister(Hex("C1B", CSR_REG_ADDRESS_SIZE), Privilege.URO, listOf("xC1B"), listOf("hpmcounter27"), Variable(REG_INIT, XLEN), "Performance-monitoring coutner."),
            CSRegister(Hex("C1C", CSR_REG_ADDRESS_SIZE), Privilege.URO, listOf("xC1C"), listOf("hpmcounter28"), Variable(REG_INIT, XLEN), "Performance-monitoring coutner."),
            CSRegister(Hex("C1D", CSR_REG_ADDRESS_SIZE), Privilege.URO, listOf("xC1D"), listOf("hpmcounter29"), Variable(REG_INIT, XLEN), "Performance-monitoring coutner."),
            CSRegister(Hex("C1E", CSR_REG_ADDRESS_SIZE), Privilege.URO, listOf("xC1E"), listOf("hpmcounter30"), Variable(REG_INIT, XLEN), "Performance-monitoring coutner."),
            CSRegister(Hex("C1F", CSR_REG_ADDRESS_SIZE), Privilege.URO, listOf("xC1F"), listOf("hpmcounter31"), Variable(REG_INIT, XLEN), "Performance-monitoring coutner."), // Needs to be extended for RV32I



        )
    )

    val regFileList = mutableListOf(standardRegFile, csrRegFile)

    val config = Config(
        Config.Description("RV64I", "RISC-V 64Bit", riscVDocs),
        FileHandler("s"),
        RegContainer(
            regFileList,
            pcSize = REG_VALUE_SIZE
        ),
        Memory(MEM_ADDRESS_WIDTH, MEM_INIT, MEM_VALUE_WIDTH, Memory.Endianess.LittleEndian),
        Transcript()
    )

}