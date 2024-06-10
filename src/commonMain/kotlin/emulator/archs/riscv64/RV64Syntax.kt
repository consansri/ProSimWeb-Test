package emulator.archs.riscv64

import emulator.archs.ArchRV64
import emulator.archs.riscv64.RV64BinMapper.MaskLabel
import emulator.kit.assembler.InstrTypeInterface
import emulator.kit.assembler.gas.GASNodeType
import emulator.kit.assembler.syntax.Component.*
import emulator.kit.assembler.syntax.Rule
import emulator.kit.common.memory.Memory
import emulator.kit.nativeLog
import emulator.kit.types.Variable
import emulator.kit.types.Variable.Value.Bin
import kotlin.time.measureTime


class RV64Syntax {
    enum class ParamType(val pseudo: Boolean, val exampleString: String, val tokenSeq: Rule?) {
        // NORMAL INSTRUCTIONS
        RD_I20(
            false, "rd, imm20",
            Rule {
                Seq(
                    Reg(RV64.standardRegFile),
                    Specific(","),
                    SpecNode(GASNodeType.INT_EXPR)
                )
            }
        ), // rd, imm
        RD_Off12(
            false, "rd, imm12(rs)",
            Rule {
                Seq(
                    Reg(RV64.standardRegFile),
                    Specific(","),
                    SpecNode(GASNodeType.INT_EXPR),
                    Specific("("),
                    Reg(RV64.standardRegFile),
                    Specific(")")
                )
            }
        ), // rd, imm12(rs)
        RS2_Off12(
            false, "rs2, imm12(rs1)",
            Rule {
                Seq(
                    Reg(RV64.standardRegFile),
                    Specific(","),
                    SpecNode(GASNodeType.INT_EXPR),
                    Specific("("),
                    Reg(RV64.standardRegFile),
                    Specific(")")
                )
            }
        ), // rs2, imm5(rs1)
        RD_RS1_RS2(
            false, "rd, rs1, rs2",
            Rule {
                Seq(
                    Reg(RV64.standardRegFile),
                    Specific(","),
                    Reg(RV64.standardRegFile),
                    Specific(","),
                    Reg(RV64.standardRegFile)
                )
            }
        ), // rd, rs1, rs2
        RD_RS1_I12(
            false, "rd, rs1, imm12",
            Rule {
                Seq(
                    Reg(RV64.standardRegFile),
                    Specific(","),
                    Reg(RV64.standardRegFile),
                    Specific(","),
                    SpecNode(GASNodeType.INT_EXPR)
                )
            }
        ), // rd, rs, imm
        RD_RS1_SHAMT6(
            false, "rd, rs1, shamt6",
            Rule {
                Seq(
                    Reg(RV64.standardRegFile),
                    Specific(","),
                    Reg(RV64.standardRegFile),
                    Specific(","),
                    SpecNode(GASNodeType.INT_EXPR)
                )
            }
        ), // rd, rs, shamt
        RS1_RS2_I12(
            false, "rs1, rs2, imm12",
            Rule {
                Seq(Reg(RV64.standardRegFile), Specific(","), Reg(RV64.standardRegFile), Specific(","), SpecNode(GASNodeType.INT_EXPR))
            }
        ), // rs1, rs2, imm
        RS1_RS2_LBL(false, "rs1, rs2, lbl", Rule {
            Seq(
                Reg(RV64.standardRegFile),
                Specific(","),
                Reg(RV64.standardRegFile),
                Specific(","),
                SpecNode(GASNodeType.INT_EXPR)
            )
        }),
        CSR_RD_OFF12_RS1(
            false, "rd, csr12, rs1",
            Rule {
                Seq(
                    Reg(RV64.standardRegFile),
                    Specific(","),
                    Reg(notInRegFile = RV64.standardRegFile),
                    Specific(","),
                    Reg(RV64.standardRegFile)
                )
            }
        ),
        CSR_RD_OFF12_UIMM5(
            false, "rd, offset, uimm5",
            Rule {
                Seq(
                    Reg(RV64.standardRegFile),
                    Specific(","),
                    Reg(notInRegFile = RV64.standardRegFile),
                    Specific(","),
                    SpecNode(GASNodeType.INT_EXPR)
                )
            }
        ),

        // PSEUDO INSTRUCTIONS
        PS_RD_LI_I64(
            true, "rd, imm64",
            Rule {
                Seq(
                    Reg(RV64.standardRegFile),
                    Specific(","),
                    SpecNode(GASNodeType.INT_EXPR)
                )
            }
        ), // rd, imm64
        PS_RS1_Jlbl(
            true, "rs, jlabel",
            Rule {
                Seq(
                    Reg(RV64.standardRegFile),
                    Specific(","),
                    SpecNode(GASNodeType.INT_EXPR)
                )
            }
        ), // rs, label
        PS_RD_Albl(
            true, "rd, alabel",
            Rule {
                Seq(
                    Reg(RV64.standardRegFile),
                    Specific(","),
                    SpecNode(GASNodeType.INT_EXPR)
                )
            }
        ), // rd, label
        PS_lbl(true, "jlabel", Rule {
            Seq(SpecNode(GASNodeType.INT_EXPR))
        }),  // label
        PS_RD_RS1(
            true, "rd, rs",
            Rule {
                Seq(
                    Reg(RV64.standardRegFile),
                    Specific(","),
                    Reg(RV64.standardRegFile)
                )
            }
        ), // rd, rs
        PS_RS1(true, "rs1",
            Rule {
                Seq(Reg(RV64.standardRegFile))
            }
        ),
        PS_CSR_RS1(
            true, "csr, rs1",
            Rule {
                Seq(
                    Reg(notInRegFile = RV64.standardRegFile),
                    Specific(","),
                    Reg(RV64.standardRegFile)
                )
            }
        ),
        PS_RD_CSR(
            true, "rd, csr",
            Rule {
                Seq(
                    Reg(RV64.standardRegFile),
                    Specific(","),
                    Reg(notInRegFile = RV64.standardRegFile)
                )
            }
        ),

        // NONE PARAM INSTR
        NONE(false, "none", null),
        PS_NONE(true, "none", null);

        fun getContentString(instr: RV64Assembler.RV64Instr): String {
            return when (this) {
                RD_I20 -> "${instr.regs[0]},${if (instr.label == null) instr.immediate.toString() else instr.label.evaluate(false).toHex().toRawZeroTrimmedString()}"
                RD_Off12 -> "${instr.regs[0]},${instr.immediate}(${instr.regs[1]})"
                RS2_Off12 -> "${instr.regs[0]},${instr.immediate}(${instr.regs[1]})"
                RD_RS1_RS2 -> instr.regs.joinToString { it.toString() }
                RD_RS1_I12 -> "${instr.regs.joinToString { it.toString() }},${instr.immediate}"
                RD_RS1_SHAMT6 -> "${instr.regs.joinToString { it.toString() }},${instr.immediate.toHex()}"
                RS1_RS2_I12 -> "${instr.regs.joinToString { it.toString() }},${instr.immediate}"
                RS1_RS2_LBL -> "${instr.regs.joinToString { it.toString() }},${if (instr.label != null) "${instr.label.evaluate(false).toHex().toRawZeroTrimmedString()} ${instr.label.print("")}" else instr.immediate.toString()}"
                CSR_RD_OFF12_RS1 -> instr.regs.joinToString { it.toString() }
                CSR_RD_OFF12_UIMM5 -> "${instr.regs.joinToString { it.toString() }},${instr.immediate}"
                PS_RD_LI_I64 -> "${instr.regs.joinToString { it.toString() }},${instr.immediate}"
                PS_RS1_Jlbl -> "${instr.regs.joinToString { it.toString() }},${if (instr.label != null) "${instr.label.evaluate(false).toHex().toRawZeroTrimmedString()} ${instr.label.print("")}" else instr.immediate.toString()}"
                PS_RD_Albl -> "${instr.regs.joinToString { it.toString() }},${if (instr.label != null) "${instr.label.evaluate(false).toHex().toRawZeroTrimmedString()} ${instr.label.print("")}" else instr.immediate.toString()}"
                PS_lbl -> if (instr.label != null) "${instr.label.evaluate(false).toHex().toRawZeroTrimmedString()} ${instr.label.print("")}" else instr.immediate.toString()
                PS_RD_RS1 -> instr.regs.joinToString { it.toString() }
                PS_RS1 -> instr.regs.joinToString { it.toString() }
                PS_CSR_RS1 -> instr.regs.joinToString { it.toString() }
                PS_RD_CSR -> instr.regs.joinToString { it.toString() }
                NONE -> ""
                PS_NONE -> ""
            }
        }
    }

    enum class InstrType(val id: String, val pseudo: Boolean, val paramType: ParamType, val opCode: RV64BinMapper.OpCode? = null, val memWords: Int = 1, val relative: InstrType? = null, val needFeatures: List<Int> = emptyList()) : InstrTypeInterface {
        LUI("LUI", false, ParamType.RD_I20, RV64BinMapper.OpCode("00000000000000000000 00000 0110111", arrayOf(MaskLabel.IMM20, MaskLabel.RD, MaskLabel.OPCODE))) {
            override fun execute(arch: ArchRV64, paramMap: Map<MaskLabel, Bin>, tracker: Memory.AccessTracker) {
                super.execute(arch, paramMap, tracker) // only for console information
                // get relevant parameters from binary map
                val rdAddr = paramMap[MaskLabel.RD]
                val imm20 = paramMap[MaskLabel.IMM20]
                if (rdAddr == null || imm20 == null) return

                // get relevant registers
                val rd = arch.getRegByAddr(rdAddr)
                val pc = arch.regContainer.pc
                if (rd == null) return

                // calculate
                val shiftedIMM = imm20.getResized(RV64.XLEN) shl 12 // from imm20 to imm32
                // change states
                rd.set(shiftedIMM)    // set register to imm32 value
                pc.set(pc.get() + Variable.Value.Hex("4"))
            }
        },
        AUIPC("AUIPC", false, ParamType.RD_I20, RV64BinMapper.OpCode("00000000000000000000 00000 0010111", arrayOf(MaskLabel.IMM20, MaskLabel.RD, MaskLabel.OPCODE))) {
            override fun execute(arch: ArchRV64, paramMap: Map<MaskLabel, Bin>, tracker: Memory.AccessTracker) {
                super.execute(arch, paramMap, tracker)
                val rdAddr = paramMap[MaskLabel.RD]
                if (rdAddr != null) {
                    val rd = arch.getRegByAddr(rdAddr)
                    val imm20 = paramMap[MaskLabel.IMM20]
                    val pc = arch.regContainer.pc
                    if (rd != null && imm20 != null) {
                        val shiftedIMM = imm20.getUResized(RV64.XLEN) shl 12
                        val sum = pc.get() + shiftedIMM
                        rd.set(sum)
                        pc.set(pc.get() + Variable.Value.Hex("4"))
                    }
                }
            }
        },
        JAL("JAL", false, ParamType.RD_I20, RV64BinMapper.OpCode("00000000000000000000 00000 1101111", arrayOf(MaskLabel.IMM20, MaskLabel.RD, MaskLabel.OPCODE))) {
            override fun execute(arch: ArchRV64, paramMap: Map<MaskLabel, Bin>, tracker: Memory.AccessTracker) {
                super.execute(arch, paramMap, tracker)
                val rdAddr = paramMap[MaskLabel.RD]
                if (rdAddr != null) {
                    val rd = arch.getRegByAddr(rdAddr)
                    val imm20 = paramMap[MaskLabel.IMM20]
                    val pc = arch.regContainer.pc
                    if (rd != null && imm20 != null) {
                        val imm20str = imm20.getRawBinStr()

                        /**
                         *      RV64IDOC Index   20 19 18 17 16 15 14 13 12 11 10  9  8  7  6  5  4  3  2  1
                         *        String Index    0  1  2  3  4  5  6  7  8  9 10 11 12 13 14 15 16 17 18 19
                         *        Location       20 [      10 : 1               ] 11 [ 19 : 12             ]
                         */

                        val shiftedImm = Variable.Value.Bin(imm20str[0].toString() + imm20str.substring(12) + imm20str[11] + imm20str.substring(1, 11), Variable.Size.Bit20()).getResized(RV64.XLEN) shl 1

                        rd.set(pc.get() + Variable.Value.Hex("4"))
                        pc.set(pc.get() + shiftedImm)
                    }
                }
            }
        },
        JALR("JALR", false, ParamType.RD_RS1_I12, RV64BinMapper.OpCode("000000000000 00000 000 00000 1100111", arrayOf(MaskLabel.IMM12, MaskLabel.RS1, MaskLabel.FUNCT3, MaskLabel.RD, MaskLabel.OPCODE))) {
            override fun execute(arch: ArchRV64, paramMap: Map<MaskLabel, Bin>, tracker: Memory.AccessTracker) {
                super.execute(arch, paramMap, tracker)
                val rdAddr = paramMap[MaskLabel.RD]
                val rs1Addr = paramMap[MaskLabel.RS1]
                if (rdAddr != null && rs1Addr != null) {
                    val rd = arch.getRegByAddr(rdAddr)
                    val rs1 = arch.getRegByAddr(rs1Addr)
                    val imm12 = paramMap[MaskLabel.IMM12]
                    val pc = arch.regContainer.pc
                    if (rd != null && imm12 != null && rs1 != null) {
                        val jumpAddr = rs1.get() + imm12.getResized(RV64.XLEN)
                        rd.set(pc.get() + Variable.Value.Hex("4"))
                        pc.set(jumpAddr)
                    }
                }
            }
        },
        ECALL("ECALL", false, ParamType.NONE, RV64BinMapper.OpCode("000000000000 00000 000 00000 1110011", arrayOf(MaskLabel.NONE, MaskLabel.NONE, MaskLabel.NONE, MaskLabel.NONE, MaskLabel.OPCODE))),
        EBREAK("EBREAK", false, ParamType.NONE, RV64BinMapper.OpCode("000000000001 00000 000 00000 1110011", arrayOf(MaskLabel.NONE, MaskLabel.NONE, MaskLabel.NONE, MaskLabel.NONE, MaskLabel.OPCODE))),
        BEQ(
            "BEQ", false, ParamType.RS1_RS2_LBL,
            RV64BinMapper.OpCode("0000000 00000 00000 000 00000 1100011", arrayOf(MaskLabel.IMM7, MaskLabel.RS2, MaskLabel.RS1, MaskLabel.FUNCT3, MaskLabel.IMM5, MaskLabel.OPCODE))
        ) {
            override fun execute(arch: ArchRV64, paramMap: Map<MaskLabel, Bin>, tracker: Memory.AccessTracker) {
                super.execute(arch, paramMap, tracker)
                val rs1Addr = paramMap[MaskLabel.RS1]
                val rs2Addr = paramMap[MaskLabel.RS2]
                if (rs2Addr != null && rs1Addr != null) {
                    val rs2 = arch.getRegByAddr(rs2Addr)
                    val rs1 = arch.getRegByAddr(rs1Addr)
                    val imm7 = paramMap[MaskLabel.IMM7]
                    val imm5 = paramMap[MaskLabel.IMM5]
                    val pc = arch.regContainer.pc
                    if (rs2 != null && imm5 != null && imm7 != null && rs1 != null) {
                        val imm7str = imm7.getResized(Variable.Size.Bit7()).getRawBinStr()
                        val imm5str = imm5.getResized(Variable.Size.Bit5()).getRawBinStr()
                        val imm12 = Variable.Value.Bin(imm7str[0].toString() + imm5str[4] + imm7str.substring(1) + imm5str.substring(0, 4), Variable.Size.Bit12())

                        val offset = imm12.toBin().getResized(RV64.XLEN) shl 1
                        if (rs1.get().toBin() == rs2.get().toBin()) {
                            pc.set(pc.get() + offset)
                        } else {
                            pc.set(pc.get() + Variable.Value.Hex("4"))
                        }
                    }
                }
            }
        },
        BNE(
            "BNE", false, ParamType.RS1_RS2_LBL,
            RV64BinMapper.OpCode("0000000 00000 00000 001 00000 1100011", arrayOf(MaskLabel.IMM7, MaskLabel.RS2, MaskLabel.RS1, MaskLabel.FUNCT3, MaskLabel.IMM5, MaskLabel.OPCODE))
        ) {
            override fun execute(arch: ArchRV64, paramMap: Map<MaskLabel, Bin>, tracker: Memory.AccessTracker) {
                super.execute(arch, paramMap, tracker)
                val rs1Addr = paramMap[MaskLabel.RS1]
                val rs2Addr = paramMap[MaskLabel.RS2]
                if (rs2Addr != null && rs1Addr != null) {
                    val rs2 = arch.getRegByAddr(rs2Addr)
                    val rs1 = arch.getRegByAddr(rs1Addr)
                    val imm7 = paramMap[MaskLabel.IMM7]
                    val imm5 = paramMap[MaskLabel.IMM5]
                    val pc = arch.regContainer.pc
                    if (rs2 != null && imm5 != null && imm7 != null && rs1 != null) {
                        val imm7str = imm7.getResized(Variable.Size.Bit7()).getRawBinStr()
                        val imm5str = imm5.getResized(Variable.Size.Bit5()).getRawBinStr()
                        val imm12 = Variable.Value.Bin(imm7str[0].toString() + imm5str[4] + imm7str.substring(1) + imm5str.substring(0, 4), Variable.Size.Bit12())
                        val offset = imm12.toBin().getResized(RV64.XLEN) shl 1
                        if (rs1.get().toBin() != rs2.get().toBin()) {
                            pc.set(pc.get() + offset)
                        } else {
                            pc.set(pc.get() + Variable.Value.Hex("4"))
                        }
                    }
                }
            }
        },
        BLT(
            "BLT", false, ParamType.RS1_RS2_LBL,
            RV64BinMapper.OpCode("0000000 00000 00000 100 00000 1100011", arrayOf(MaskLabel.IMM7, MaskLabel.RS2, MaskLabel.RS1, MaskLabel.FUNCT3, MaskLabel.IMM5, MaskLabel.OPCODE))
        ) {
            override fun execute(arch: ArchRV64, paramMap: Map<MaskLabel, Bin>, tracker: Memory.AccessTracker) {
                super.execute(arch, paramMap, tracker)
                val rs1Addr = paramMap[MaskLabel.RS1]
                val rs2Addr = paramMap[MaskLabel.RS2]
                if (rs2Addr != null && rs1Addr != null) {
                    val rs2 = arch.getRegByAddr(rs2Addr)
                    val rs1 = arch.getRegByAddr(rs1Addr)
                    val imm7 = paramMap[MaskLabel.IMM7]
                    val imm5 = paramMap[MaskLabel.IMM5]
                    val pc = arch.regContainer.pc
                    if (rs2 != null && imm5 != null && imm7 != null && rs1 != null) {
                        val imm7str = imm7.getResized(Variable.Size.Bit7()).getRawBinStr()
                        val imm5str = imm5.getResized(Variable.Size.Bit5()).getRawBinStr()
                        val imm12 = Variable.Value.Bin(imm7str[0].toString() + imm5str[4] + imm7str.substring(1) + imm5str.substring(0, 4), Variable.Size.Bit12())
                        val offset = imm12.toBin().getResized(RV64.XLEN) shl 1
                        if (rs1.get().toDec() < rs2.get().toDec()) {
                            pc.set(pc.get() + offset)
                        } else {
                            pc.set(pc.get() + Variable.Value.Hex("4"))
                        }
                    }
                }
            }
        },
        BGE(
            "BGE", false, ParamType.RS1_RS2_LBL,
            RV64BinMapper.OpCode("0000000 00000 00000 101 00000 1100011", arrayOf(MaskLabel.IMM7, MaskLabel.RS2, MaskLabel.RS1, MaskLabel.FUNCT3, MaskLabel.IMM5, MaskLabel.OPCODE))
        ) {
            override fun execute(arch: ArchRV64, paramMap: Map<MaskLabel, Bin>, tracker: Memory.AccessTracker) {
                super.execute(arch, paramMap, tracker)
                val rs1Addr = paramMap[MaskLabel.RS1]
                val rs2Addr = paramMap[MaskLabel.RS2]
                if (rs2Addr != null && rs1Addr != null) {
                    val rs2 = arch.getRegByAddr(rs2Addr)
                    val rs1 = arch.getRegByAddr(rs1Addr)
                    val imm7 = paramMap[MaskLabel.IMM7]
                    val imm5 = paramMap[MaskLabel.IMM5]
                    val pc = arch.regContainer.pc
                    if (rs2 != null && imm5 != null && imm7 != null && rs1 != null) {
                        val imm7str = imm7.getResized(Variable.Size.Bit7()).getRawBinStr()
                        val imm5str = imm5.getResized(Variable.Size.Bit5()).getRawBinStr()
                        val imm12 = Variable.Value.Bin(imm7str[0].toString() + imm5str[4] + imm7str.substring(1) + imm5str.substring(0, 4), Variable.Size.Bit12())
                        val offset = imm12.toBin().getResized(RV64.XLEN) shl 1
                        if (rs1.get().toDec() >= rs2.get().toDec()) {
                            pc.set(pc.get() + offset)
                        } else {
                            pc.set(pc.get() + Variable.Value.Hex("4"))
                        }
                    }
                }
            }
        },
        BLTU(
            "BLTU", false, ParamType.RS1_RS2_LBL,
            RV64BinMapper.OpCode("0000000 00000 00000 110 00000 1100011", arrayOf(MaskLabel.IMM7, MaskLabel.RS2, MaskLabel.RS1, MaskLabel.FUNCT3, MaskLabel.IMM5, MaskLabel.OPCODE))
        ) {
            override fun execute(arch: ArchRV64, paramMap: Map<MaskLabel, Bin>, tracker: Memory.AccessTracker) {
                super.execute(arch, paramMap, tracker)
                val rs1Addr = paramMap[MaskLabel.RS1]
                val rs2Addr = paramMap[MaskLabel.RS2]
                if (rs2Addr != null && rs1Addr != null) {
                    val rs2 = arch.getRegByAddr(rs2Addr)
                    val rs1 = arch.getRegByAddr(rs1Addr)
                    val imm7 = paramMap[MaskLabel.IMM7]
                    val imm5 = paramMap[MaskLabel.IMM5]
                    val pc = arch.regContainer.pc
                    if (rs2 != null && imm5 != null && imm7 != null && rs1 != null) {
                        val imm7str = imm7.getResized(Variable.Size.Bit7()).getRawBinStr()
                        val imm5str = imm5.getResized(Variable.Size.Bit5()).getRawBinStr()
                        val imm12 = Variable.Value.Bin(imm7str[0].toString() + imm5str[4] + imm7str.substring(1) + imm5str.substring(0, 4), Variable.Size.Bit12())
                        val offset = imm12.toBin().getResized(RV64.XLEN) shl 1
                        if (rs1.get().toUDec() < rs2.get().toUDec()) {
                            pc.set(pc.get() + offset)
                        } else {
                            pc.set(pc.get() + Variable.Value.Hex("4"))
                        }
                    }
                }
            }
        },
        BGEU(
            "BGEU", false, ParamType.RS1_RS2_LBL,
            RV64BinMapper.OpCode("0000000 00000 00000 111 00000 1100011", arrayOf(MaskLabel.IMM7, MaskLabel.RS2, MaskLabel.RS1, MaskLabel.FUNCT3, MaskLabel.IMM5, MaskLabel.OPCODE))
        ) {
            override fun execute(arch: ArchRV64, paramMap: Map<MaskLabel, Bin>, tracker: Memory.AccessTracker) {
                super.execute(arch, paramMap, tracker)
                val rs1Addr = paramMap[MaskLabel.RS1]
                val rs2Addr = paramMap[MaskLabel.RS2]
                if (rs2Addr != null && rs1Addr != null) {
                    val rs2 = arch.getRegByAddr(rs2Addr)
                    val rs1 = arch.getRegByAddr(rs1Addr)
                    val imm7 = paramMap[MaskLabel.IMM7]
                    val imm5 = paramMap[MaskLabel.IMM5]
                    val pc = arch.regContainer.pc
                    if (rs2 != null && imm5 != null && imm7 != null && rs1 != null) {
                        val imm7str = imm7.getResized(Variable.Size.Bit7()).getRawBinStr()
                        val imm5str = imm5.getResized(Variable.Size.Bit5()).getRawBinStr()
                        val imm12 = Variable.Value.Bin(imm7str[0].toString() + imm5str[4] + imm7str.substring(1) + imm5str.substring(0, 4), Variable.Size.Bit12())
                        val offset = imm12.toBin().getResized(RV64.XLEN) shl 1
                        if (rs1.get().toUDec() >= rs2.get().toUDec()) {
                            pc.set(pc.get() + offset)
                        } else {
                            pc.set(pc.get() + Variable.Value.Hex("4"))
                        }
                    }
                }
            }
        },
        LB("LB", false, ParamType.RD_Off12, RV64BinMapper.OpCode("000000000000 00000 000 00000 0000011", arrayOf(MaskLabel.IMM12, MaskLabel.RS1, MaskLabel.FUNCT3, MaskLabel.RD, MaskLabel.OPCODE))) {
            override fun execute(arch: ArchRV64, paramMap: Map<MaskLabel, Bin>, tracker: Memory.AccessTracker) {
                super.execute(arch, paramMap, tracker)
                val rdAddr = paramMap[MaskLabel.RD]
                val rs1Addr = paramMap[MaskLabel.RS1]
                val imm12 = paramMap[MaskLabel.IMM12]
                if (rdAddr != null && rs1Addr != null && imm12 != null) {
                    val rd = arch.getRegByAddr(rdAddr)
                    val rs1 = arch.getRegByAddr(rs1Addr)
                    val pc = arch.regContainer.pc
                    if (rd != null && rs1 != null) {
                        val memAddr = rs1.get().toBin() + imm12.getResized(RV64.XLEN)
                        val loadedByte = arch.dataMemory.load(memAddr.toHex(), tracker =  tracker).toBin().getResized(RV64.XLEN)
                        rd.set(loadedByte)
                        pc.set(pc.get() + Variable.Value.Hex("4"))
                    }
                }
            }
        },
        LH("LH", false, ParamType.RD_Off12, RV64BinMapper.OpCode("000000000000 00000 001 00000 0000011", arrayOf(MaskLabel.IMM12, MaskLabel.RS1, MaskLabel.FUNCT3, MaskLabel.RD, MaskLabel.OPCODE))) {
            override fun execute(arch: ArchRV64, paramMap: Map<MaskLabel, Bin>, tracker: Memory.AccessTracker) {
                super.execute(arch, paramMap, tracker)
                val rdAddr = paramMap[MaskLabel.RD]
                val rs1Addr = paramMap[MaskLabel.RS1]
                val imm12 = paramMap[MaskLabel.IMM12]
                if (rdAddr != null && rs1Addr != null && imm12 != null) {
                    val rd = arch.getRegByAddr(rdAddr)
                    val rs1 = arch.getRegByAddr(rs1Addr)
                    val pc = arch.regContainer.pc
                    if (rd != null && rs1 != null) {
                        val memAddr = rs1.get().toBin() + imm12.getResized(RV64.XLEN)
                        val loadedHalfWord = arch.dataMemory.load(memAddr.toHex(), 2, tracker =  tracker).toBin().getResized(RV64.XLEN)
                        rd.set(loadedHalfWord)
                        pc.set(pc.get() + Variable.Value.Hex("4"))
                    }
                }
            }
        },
        LW("LW", false, ParamType.RD_Off12, RV64BinMapper.OpCode("000000000000 00000 010 00000 0000011", arrayOf(MaskLabel.IMM12, MaskLabel.RS1, MaskLabel.FUNCT3, MaskLabel.RD, MaskLabel.OPCODE))) {
            override fun execute(arch: ArchRV64, paramMap: Map<MaskLabel, Bin>, tracker: Memory.AccessTracker) {
                super.execute(arch, paramMap, tracker)
                val rdAddr = paramMap[MaskLabel.RD]
                val rs1Addr = paramMap[MaskLabel.RS1]
                val imm12 = paramMap[MaskLabel.IMM12]
                if (rdAddr != null && rs1Addr != null && imm12 != null) {
                    val rd = arch.getRegByAddr(rdAddr)
                    val rs1 = arch.getRegByAddr(rs1Addr)
                    val pc = arch.regContainer.pc
                    if (rd != null && rs1 != null) {
                        val memAddr = rs1.get().toBin() + imm12.getResized(RV64.XLEN)
                        val loadedWord = arch.dataMemory.load(memAddr.toHex(), 4, tracker =  tracker).toBin().getResized(RV64.XLEN)
                        rd.set(loadedWord)
                        pc.set(pc.get() + Variable.Value.Hex("4"))
                    }
                }
            }
        },
        LD("LD", false, ParamType.RD_Off12, RV64BinMapper.OpCode("000000000000 00000 011 00000 0000011", arrayOf(MaskLabel.IMM12, MaskLabel.RS1, MaskLabel.FUNCT3, MaskLabel.RD, MaskLabel.OPCODE))) {
            override fun execute(arch: ArchRV64, paramMap: Map<MaskLabel, Bin>, tracker: Memory.AccessTracker) {
                super.execute(arch, paramMap, tracker)
                val rdAddr = paramMap[MaskLabel.RD]
                val rs1Addr = paramMap[MaskLabel.RS1]
                val imm12 = paramMap[MaskLabel.IMM12]
                if (rdAddr != null && rs1Addr != null && imm12 != null) {
                    val rd = arch.getRegByAddr(rdAddr)
                    val rs1 = arch.getRegByAddr(rs1Addr)
                    val pc = arch.regContainer.pc
                    if (rd != null && rs1 != null) {
                        val memAddr = rs1.get().toBin() + imm12.getResized(RV64.XLEN)
                        val loadedWord = arch.dataMemory.load(memAddr.toHex(), 8, tracker =  tracker).toBin().getResized(RV64.XLEN)
                        rd.set(loadedWord)
                        pc.set(pc.get() + Variable.Value.Hex("4"))
                    }
                }
            }
        },
        LBU("LBU", false, ParamType.RD_Off12, RV64BinMapper.OpCode("000000000000 00000 100 00000 0000011", arrayOf(MaskLabel.IMM12, MaskLabel.RS1, MaskLabel.FUNCT3, MaskLabel.RD, MaskLabel.OPCODE))) {
            override fun execute(arch: ArchRV64, paramMap: Map<MaskLabel, Bin>, tracker: Memory.AccessTracker) {
                super.execute(arch, paramMap, tracker)
                val rdAddr = paramMap[MaskLabel.RD]
                val rs1Addr = paramMap[MaskLabel.RS1]
                val imm12 = paramMap[MaskLabel.IMM12]
                if (rdAddr != null && rs1Addr != null && imm12 != null) {
                    val rd = arch.getRegByAddr(rdAddr)
                    val rs1 = arch.getRegByAddr(rs1Addr)
                    val pc = arch.regContainer.pc
                    if (rd != null && rs1 != null) {
                        val memAddr = rs1.get().toBin() + imm12.getResized(RV64.XLEN)
                        val loadedByte = arch.dataMemory.load(memAddr.toHex(), tracker =  tracker).toBin()
                        rd.set(loadedByte.toBin().getUResized(RV64.XLEN))
                        pc.set(pc.get() + Variable.Value.Hex("4"))
                    }
                }
            }
        },
        LHU("LHU", false, ParamType.RD_Off12, RV64BinMapper.OpCode("000000000000 00000 101 00000 0000011", arrayOf(MaskLabel.IMM12, MaskLabel.RS1, MaskLabel.FUNCT3, MaskLabel.RD, MaskLabel.OPCODE))) {
            override fun execute(arch: ArchRV64, paramMap: Map<MaskLabel, Bin>, tracker: Memory.AccessTracker) {
                super.execute(arch, paramMap, tracker)
                val rdAddr = paramMap[MaskLabel.RD]
                val rs1Addr = paramMap[MaskLabel.RS1]
                val imm12 = paramMap[MaskLabel.IMM12]
                if (rdAddr != null && rs1Addr != null && imm12 != null) {
                    val rd = arch.getRegByAddr(rdAddr)
                    val rs1 = arch.getRegByAddr(rs1Addr)
                    val pc = arch.regContainer.pc
                    if (rd != null && rs1 != null) {
                        val memAddr = rs1.get().toBin() + imm12.getResized(RV64.XLEN)
                        val loadedByte = arch.dataMemory.load(memAddr.toHex(), 2, tracker =  tracker)
                        rd.set(loadedByte.getUResized(RV64.XLEN))
                        pc.set(pc.get() + Variable.Value.Hex("4"))
                    }
                }
            }
        },
        LWU("LWU", false, ParamType.RD_Off12, RV64BinMapper.OpCode("000000000000 00000 110 00000 0000011", arrayOf(MaskLabel.IMM12, MaskLabel.RS1, MaskLabel.FUNCT3, MaskLabel.RD, MaskLabel.OPCODE))) {
            override fun execute(arch: ArchRV64, paramMap: Map<MaskLabel, Bin>, tracker: Memory.AccessTracker) {
                super.execute(arch, paramMap, tracker)
                val rdAddr = paramMap[MaskLabel.RD]
                val rs1Addr = paramMap[MaskLabel.RS1]
                val imm12 = paramMap[MaskLabel.IMM12]
                if (rdAddr != null && rs1Addr != null && imm12 != null) {
                    val rd = arch.getRegByAddr(rdAddr)
                    val rs1 = arch.getRegByAddr(rs1Addr)
                    val pc = arch.regContainer.pc
                    if (rd != null && rs1 != null) {
                        val memAddr = rs1.get().toBin() + imm12.getResized(RV64.XLEN)
                        val loadedWord = arch.dataMemory.load(memAddr.toHex(), 4, tracker =  tracker)
                        rd.set(loadedWord.getUResized(RV64.XLEN))
                        pc.set(pc.get() + Variable.Value.Hex("4"))
                    }
                }
            }
        },
        SB("SB", false, ParamType.RS2_Off12, RV64BinMapper.OpCode("0000000 00000 00000 000 00000 0100011", arrayOf(MaskLabel.IMM7, MaskLabel.RS2, MaskLabel.RS1, MaskLabel.FUNCT3, MaskLabel.IMM5, MaskLabel.OPCODE))) {
            override fun execute(arch: ArchRV64, paramMap: Map<MaskLabel, Bin>, tracker: Memory.AccessTracker) {
                super.execute(arch, paramMap, tracker)
                val rs1Addr = paramMap[MaskLabel.RS1]
                val rs2Addr = paramMap[MaskLabel.RS2]
                val imm5 = paramMap[MaskLabel.IMM5]
                val imm7 = paramMap[MaskLabel.IMM7]
                if (rs1Addr != null && rs2Addr != null && imm5 != null && imm7 != null) {
                    val rs1 = arch.getRegByAddr(rs1Addr)
                    val rs2 = arch.getRegByAddr(rs2Addr)
                    val pc = arch.regContainer.pc
                    if (rs1 != null && rs2 != null) {
                        val off64 = (imm7.getResized(RV64.XLEN) shl 5) + imm5
                        val memAddr = rs1.get().toBin().getResized(RV64.XLEN) + off64
                        arch.dataMemory.store(memAddr.toHex(), rs2.get().toBin().getResized(Variable.Size.Bit8()), tracker =  tracker)
                        pc.set(pc.get() + Variable.Value.Hex("4"))
                    }
                }
            }
        },
        SH("SH", false, ParamType.RS2_Off12, RV64BinMapper.OpCode("0000000 00000 00000 001 00000 0100011", arrayOf(MaskLabel.IMM7, MaskLabel.RS2, MaskLabel.RS1, MaskLabel.FUNCT3, MaskLabel.IMM5, MaskLabel.OPCODE))) {
            override fun execute(arch: ArchRV64, paramMap: Map<MaskLabel, Bin>, tracker: Memory.AccessTracker) {
                super.execute(arch, paramMap, tracker)
                val rs1Addr = paramMap[MaskLabel.RS1]
                val rs2Addr = paramMap[MaskLabel.RS2]
                val imm5 = paramMap[MaskLabel.IMM5]
                val imm7 = paramMap[MaskLabel.IMM7]
                if (rs1Addr != null && rs2Addr != null && imm5 != null && imm7 != null) {
                    val rs1 = arch.getRegByAddr(rs1Addr)
                    val rs2 = arch.getRegByAddr(rs2Addr)
                    val pc = arch.regContainer.pc
                    if (rs1 != null && rs2 != null) {
                        val off64 = (imm7.getResized(RV64.XLEN) shl 5) + imm5
                        val memAddr = rs1.get().toBin().getResized(RV64.XLEN) + off64
                        arch.dataMemory.store(memAddr.toHex(), rs2.get().toBin().getResized(Variable.Size.Bit16()), tracker =  tracker)
                        pc.set(pc.get() + Variable.Value.Hex("4"))
                    }
                }
            }
        },
        SW("SW", false, ParamType.RS2_Off12, RV64BinMapper.OpCode("0000000 00000 00000 010 00000 0100011", arrayOf(MaskLabel.IMM7, MaskLabel.RS2, MaskLabel.RS1, MaskLabel.FUNCT3, MaskLabel.IMM5, MaskLabel.OPCODE))) {
            override fun execute(arch: ArchRV64, paramMap: Map<MaskLabel, Bin>, tracker: Memory.AccessTracker) {
                super.execute(arch, paramMap, tracker)
                val rs1Addr = paramMap[MaskLabel.RS1]
                val rs2Addr = paramMap[MaskLabel.RS2]
                val imm5 = paramMap[MaskLabel.IMM5]
                val imm7 = paramMap[MaskLabel.IMM7]
                if (rs1Addr != null && rs2Addr != null && imm5 != null && imm7 != null) {
                    val rs1 = arch.getRegByAddr(rs1Addr)
                    val rs2 = arch.getRegByAddr(rs2Addr)
                    val pc = arch.regContainer.pc
                    if (rs1 != null && rs2 != null) {
                        val off64 = (imm7.getResized(RV64.XLEN) shl 5) + imm5
                        val memAddr = rs1.variable.get().toBin().getResized(RV64.XLEN) + off64
                        arch.dataMemory.store(memAddr.toHex(), rs2.get().toBin().getResized(Variable.Size.Bit32()), tracker =  tracker)
                        pc.set(pc.get() + Variable.Value.Hex("4"))
                    }
                }
            }
        },
        SD("SD", false, ParamType.RS2_Off12, RV64BinMapper.OpCode("0000000 00000 00000 011 00000 0100011", arrayOf(MaskLabel.IMM7, MaskLabel.RS2, MaskLabel.RS1, MaskLabel.FUNCT3, MaskLabel.IMM5, MaskLabel.OPCODE))) {
            override fun execute(arch: ArchRV64, paramMap: Map<MaskLabel, Bin>, tracker: Memory.AccessTracker) {
                super.execute(arch, paramMap, tracker)
                val rs1Addr = paramMap[MaskLabel.RS1]
                val rs2Addr = paramMap[MaskLabel.RS2]
                val imm5 = paramMap[MaskLabel.IMM5]
                val imm7 = paramMap[MaskLabel.IMM7]
                if (rs1Addr != null && rs2Addr != null && imm5 != null && imm7 != null) {
                    val rs1 = arch.getRegByAddr(rs1Addr)
                    val rs2 = arch.getRegByAddr(rs2Addr)
                    val pc = arch.regContainer.pc
                    if (rs1 != null && rs2 != null) {
                        val off64 = (imm7.getResized(RV64.XLEN) shl 5) + imm5
                        val memAddr = rs1.variable.get().toBin().getResized(RV64.XLEN) + off64
                        arch.dataMemory.store(memAddr.toHex(), rs2.get().toBin().getResized(RV64.XLEN), tracker =  tracker)
                        pc.set(pc.get() + Variable.Value.Hex("4"))
                    }
                }
            }
        },
        ADDI("ADDI", false, ParamType.RD_RS1_I12, RV64BinMapper.OpCode("000000000000 00000 000 00000 0010011", arrayOf(MaskLabel.IMM12, MaskLabel.RS1, MaskLabel.FUNCT3, MaskLabel.RD, MaskLabel.OPCODE))) {
            override fun execute(arch: ArchRV64, paramMap: Map<MaskLabel, Bin>, tracker: Memory.AccessTracker) {
                super.execute(arch, paramMap, tracker)
                val rdAddr = paramMap[MaskLabel.RD]
                val rs1Addr = paramMap[MaskLabel.RS1]
                if (rdAddr != null && rs1Addr != null) {
                    val rd = arch.getRegByAddr(rdAddr)
                    val rs1 = arch.getRegByAddr(rs1Addr)
                    val imm12 = paramMap[MaskLabel.IMM12]
                    val pc = arch.regContainer.pc
                    if (rd != null && imm12 != null && rs1 != null) {
                        val paddedImm64 = imm12.getResized(RV64.XLEN)
                        val sum = rs1.get().toBin() + paddedImm64
                        rd.set(sum)
                        pc.set(pc.get() + Variable.Value.Hex("4"))
                    }
                }
            }
        },
        ADDIW("ADDIW", false, ParamType.RD_RS1_I12, RV64BinMapper.OpCode("000000000000 00000 000 00000 0011011", arrayOf(MaskLabel.IMM12, MaskLabel.RS1, MaskLabel.FUNCT3, MaskLabel.RD, MaskLabel.OPCODE))) {
            override fun execute(arch: ArchRV64, paramMap: Map<MaskLabel, Bin>, tracker: Memory.AccessTracker) {
                super.execute(arch, paramMap, tracker)
                val rdAddr = paramMap[MaskLabel.RD]
                val rs1Addr = paramMap[MaskLabel.RS1]
                if (rdAddr != null && rs1Addr != null) {
                    val rd = arch.getRegByAddr(rdAddr)
                    val rs1 = arch.getRegByAddr(rs1Addr)
                    val imm12 = paramMap[MaskLabel.IMM12]
                    val pc = arch.regContainer.pc
                    if (rd != null && imm12 != null && rs1 != null) {
                        val paddedImm32 = imm12.getResized(Variable.Size.Bit32())
                        val sum = rs1.get().toBin().getResized(Variable.Size.Bit32()) + paddedImm32
                        rd.set(sum.toBin().getResized(RV64.XLEN))
                        pc.set(pc.get() + Variable.Value.Hex("4"))
                    }
                }
            }
        },
        SLTI("SLTI", false, ParamType.RD_RS1_I12, RV64BinMapper.OpCode("000000000000 00000 010 00000 0010011", arrayOf(MaskLabel.IMM12, MaskLabel.RS1, MaskLabel.FUNCT3, MaskLabel.RD, MaskLabel.OPCODE))) {
            override fun execute(arch: ArchRV64, paramMap: Map<MaskLabel, Bin>, tracker: Memory.AccessTracker) {
                super.execute(arch, paramMap, tracker)
                val rdAddr = paramMap[MaskLabel.RD]
                val rs1Addr = paramMap[MaskLabel.RS1]
                if (rdAddr != null && rs1Addr != null) {
                    val rd = arch.getRegByAddr(rdAddr)
                    val rs1 = arch.getRegByAddr(rs1Addr)
                    val imm12 = paramMap[MaskLabel.IMM12]
                    val pc = arch.regContainer.pc
                    if (rd != null && imm12 != null && rs1 != null) {
                        val paddedImm64 = imm12.getResized(RV64.XLEN)
                        rd.set(if (rs1.get().toDec() < paddedImm64.toDec()) Variable.Value.Bin("1", RV64.XLEN) else Variable.Value.Bin("0", RV64.XLEN))
                        pc.set(pc.get() + Variable.Value.Hex("4"))
                    }
                }
            }
        },
        SLTIU("SLTIU", false, ParamType.RD_RS1_I12, RV64BinMapper.OpCode("000000000000 00000 011 00000 0010011", arrayOf(MaskLabel.IMM12, MaskLabel.RS1, MaskLabel.FUNCT3, MaskLabel.RD, MaskLabel.OPCODE))) {
            override fun execute(arch: ArchRV64, paramMap: Map<MaskLabel, Bin>, tracker: Memory.AccessTracker) {
                super.execute(arch, paramMap, tracker)
                val rdAddr = paramMap[MaskLabel.RD]
                val rs1Addr = paramMap[MaskLabel.RS1]
                if (rdAddr != null && rs1Addr != null) {
                    val rd = arch.getRegByAddr(rdAddr)
                    val rs1 = arch.getRegByAddr(rs1Addr)
                    val imm12 = paramMap[MaskLabel.IMM12]
                    val pc = arch.regContainer.pc
                    if (rd != null && imm12 != null && rs1 != null) {
                        val paddedImm64 = imm12.getUResized(RV64.XLEN)
                        rd.set(if (rs1.get().toBin() < paddedImm64) Variable.Value.Bin("1", RV64.XLEN) else Variable.Value.Bin("0", RV64.XLEN))
                        pc.set(pc.get() + Variable.Value.Hex("4"))
                    }
                }
            }
        },
        XORI("XORI", false, ParamType.RD_RS1_I12, RV64BinMapper.OpCode("000000000000 00000 100 00000 0010011", arrayOf(MaskLabel.IMM12, MaskLabel.RS1, MaskLabel.FUNCT3, MaskLabel.RD, MaskLabel.OPCODE))) {
            override fun execute(arch: ArchRV64, paramMap: Map<MaskLabel, Bin>, tracker: Memory.AccessTracker) {
                super.execute(arch, paramMap, tracker)
                val rdAddr = paramMap[MaskLabel.RD]
                val rs1Addr = paramMap[MaskLabel.RS1]
                if (rdAddr != null && rs1Addr != null) {
                    val rd = arch.getRegByAddr(rdAddr)
                    val rs1 = arch.getRegByAddr(rs1Addr)
                    val imm12 = paramMap[MaskLabel.IMM12]
                    val pc = arch.regContainer.pc
                    if (rd != null && imm12 != null && rs1 != null) {
                        val paddedImm64 = imm12.getResized(RV64.XLEN)
                        rd.set(rs1.get().toBin() xor paddedImm64)
                        pc.set(pc.get() + Variable.Value.Hex("4"))
                    }
                }
            }
        },
        ORI("ORI", false, ParamType.RD_RS1_I12, RV64BinMapper.OpCode("000000000000 00000 110 00000 0010011", arrayOf(MaskLabel.IMM12, MaskLabel.RS1, MaskLabel.FUNCT3, MaskLabel.RD, MaskLabel.OPCODE))) {
            override fun execute(arch: ArchRV64, paramMap: Map<MaskLabel, Bin>, tracker: Memory.AccessTracker) {
                super.execute(arch, paramMap, tracker)
                val rdAddr = paramMap[MaskLabel.RD]
                val rs1Addr = paramMap[MaskLabel.RS1]
                if (rdAddr != null && rs1Addr != null) {
                    val rd = arch.getRegByAddr(rdAddr)
                    val rs1 = arch.getRegByAddr(rs1Addr)
                    val imm12 = paramMap[MaskLabel.IMM12]
                    val pc = arch.regContainer.pc
                    if (rd != null && imm12 != null && rs1 != null) {
                        val paddedImm64 = imm12.getResized(RV64.XLEN)
                        rd.set(rs1.get().toBin() or paddedImm64)
                        pc.set(pc.get() + Variable.Value.Hex("4"))
                    }
                }
            }
        },
        ANDI("ANDI", false, ParamType.RD_RS1_I12, RV64BinMapper.OpCode("000000000000 00000 111 00000 0010011", arrayOf(MaskLabel.IMM12, MaskLabel.RS1, MaskLabel.FUNCT3, MaskLabel.RD, MaskLabel.OPCODE))) {
            override fun execute(arch: ArchRV64, paramMap: Map<MaskLabel, Bin>, tracker: Memory.AccessTracker) {
                super.execute(arch, paramMap, tracker)
                val rdAddr = paramMap[MaskLabel.RD]
                val rs1Addr = paramMap[MaskLabel.RS1]
                if (rdAddr != null && rs1Addr != null) {
                    val rd = arch.getRegByAddr(rdAddr)
                    val rs1 = arch.getRegByAddr(rs1Addr)
                    val imm12 = paramMap[MaskLabel.IMM12]
                    val pc = arch.regContainer.pc
                    if (rd != null && imm12 != null && rs1 != null) {
                        val paddedImm64 = imm12.getResized(RV64.XLEN)
                        rd.set(rs1.get().toBin() and paddedImm64)
                        pc.set(pc.get() + Variable.Value.Hex("4"))
                    }
                }
            }
        },
        SLLI(
            "SLLI", false, ParamType.RD_RS1_SHAMT6,
            RV64BinMapper.OpCode("000000 000000 00000 001 00000 0010011", arrayOf(MaskLabel.FUNCT6, MaskLabel.SHAMT6, MaskLabel.RS1, MaskLabel.FUNCT3, MaskLabel.RD, MaskLabel.OPCODE))
        ) {
            override fun execute(arch: ArchRV64, paramMap: Map<MaskLabel, Bin>, tracker: Memory.AccessTracker) {
                super.execute(arch, paramMap, tracker)
                val rdAddr = paramMap[MaskLabel.RD]
                val rs1Addr = paramMap[MaskLabel.RS1]
                if (rdAddr != null && rs1Addr != null) {
                    val rd = arch.getRegByAddr(rdAddr)
                    val rs1 = arch.getRegByAddr(rs1Addr)
                    val shamt6 = paramMap[MaskLabel.SHAMT6]
                    val pc = arch.regContainer.pc
                    if (rd != null && shamt6 != null && rs1 != null) {
                        rd.set(rs1.get().toBin() ushl (shamt6.toUDec().toIntOrNull() ?: 0))
                        pc.set(pc.get() + Variable.Value.Hex("4"))
                    }
                }
            }
        },
        SLLIW(
            "SLLIW", false, ParamType.RD_RS1_SHAMT6,
            RV64BinMapper.OpCode("000000 000000 00000 001 00000 0011011", arrayOf(MaskLabel.FUNCT6, MaskLabel.SHAMT6, MaskLabel.RS1, MaskLabel.FUNCT3, MaskLabel.RD, MaskLabel.OPCODE))
        ) {
            override fun execute(arch: ArchRV64, paramMap: Map<MaskLabel, Bin>, tracker: Memory.AccessTracker) {
                super.execute(arch, paramMap, tracker)
                val rdAddr = paramMap[MaskLabel.RD]
                val rs1Addr = paramMap[MaskLabel.RS1]
                if (rdAddr != null && rs1Addr != null) {
                    val rd = arch.getRegByAddr(rdAddr)
                    val rs1 = arch.getRegByAddr(rs1Addr)
                    val shamt6 = paramMap[MaskLabel.SHAMT6]
                    val pc = arch.regContainer.pc
                    if (rd != null && shamt6 != null && rs1 != null) {
                        rd.set((rs1.get().toBin().getUResized(Variable.Size.Bit32()) ushl shamt6.getUResized(Variable.Size.Bit5()).getRawBinStr().toInt(2)).getResized(RV64.XLEN))
                        pc.set(pc.get() + Variable.Value.Hex("4"))
                    }
                }
            }
        },
        SRLI(
            "SRLI", false, ParamType.RD_RS1_SHAMT6,
            RV64BinMapper.OpCode("000000 000000 00000 101 00000 0010011", arrayOf(MaskLabel.FUNCT6, MaskLabel.SHAMT6, MaskLabel.RS1, MaskLabel.FUNCT3, MaskLabel.RD, MaskLabel.OPCODE))
        ) {
            override fun execute(arch: ArchRV64, paramMap: Map<MaskLabel, Bin>, tracker: Memory.AccessTracker) {
                super.execute(arch, paramMap, tracker)
                val rdAddr = paramMap[MaskLabel.RD]
                val rs1Addr = paramMap[MaskLabel.RS1]
                if (rdAddr != null && rs1Addr != null) {
                    val rd = arch.getRegByAddr(rdAddr)
                    val rs1 = arch.getRegByAddr(rs1Addr)
                    val shamt6 = paramMap[MaskLabel.SHAMT6]
                    val pc = arch.regContainer.pc
                    if (rd != null && shamt6 != null && rs1 != null) {
                        rd.set(rs1.get().toBin() ushr shamt6.getRawBinStr().toInt(2))
                        pc.set(pc.get() + Variable.Value.Hex("4"))
                    }
                }
            }
        },
        SRLIW(
            "SRLIW", false, ParamType.RD_RS1_SHAMT6,
            RV64BinMapper.OpCode("000000 000000 00000 101 00000 0011011", arrayOf(MaskLabel.FUNCT6, MaskLabel.SHAMT6, MaskLabel.RS1, MaskLabel.FUNCT3, MaskLabel.RD, MaskLabel.OPCODE))
        ) {
            override fun execute(arch: ArchRV64, paramMap: Map<MaskLabel, Bin>, tracker: Memory.AccessTracker) {
                super.execute(arch, paramMap, tracker)
                val rdAddr = paramMap[MaskLabel.RD]
                val rs1Addr = paramMap[MaskLabel.RS1]
                if (rdAddr != null && rs1Addr != null) {
                    val rd = arch.getRegByAddr(rdAddr)
                    val rs1 = arch.getRegByAddr(rs1Addr)
                    val shamt6 = paramMap[MaskLabel.SHAMT6]
                    val pc = arch.regContainer.pc
                    if (rd != null && shamt6 != null && rs1 != null) {
                        rd.set((rs1.get().toBin().getUResized(Variable.Size.Bit32()) ushr shamt6.getUResized(Variable.Size.Bit5()).getRawBinStr().toInt(2)).getResized(RV64.XLEN))
                        pc.set(pc.get() + Variable.Value.Hex("4"))
                    }
                }
            }
        },
        SRAI(
            "SRAI", false, ParamType.RD_RS1_SHAMT6,
            RV64BinMapper.OpCode("010000 000000 00000 101 00000 0010011", arrayOf(MaskLabel.FUNCT6, MaskLabel.SHAMT6, MaskLabel.RS1, MaskLabel.FUNCT3, MaskLabel.RD, MaskLabel.OPCODE))
        ) {
            override fun execute(arch: ArchRV64, paramMap: Map<MaskLabel, Bin>, tracker: Memory.AccessTracker) {
                super.execute(arch, paramMap, tracker)
                val rdAddr = paramMap[MaskLabel.RD]
                val rs1Addr = paramMap[MaskLabel.RS1]
                if (rdAddr != null && rs1Addr != null) {
                    val rd = arch.getRegByAddr(rdAddr)
                    val rs1 = arch.getRegByAddr(rs1Addr)
                    val shamt6 = paramMap[MaskLabel.SHAMT6]
                    val pc = arch.regContainer.pc
                    if (rd != null && shamt6 != null && rs1 != null) {
                        rd.set(rs1.get().toBin() shr shamt6.getRawBinStr().toInt(2))
                        pc.set(pc.get() + Variable.Value.Hex("4"))
                    }
                }
            }
        },
        SRAIW(
            "SRAIW", false, ParamType.RD_RS1_SHAMT6,
            RV64BinMapper.OpCode("010000 000000 00000 101 00000 0011011", arrayOf(MaskLabel.FUNCT6, MaskLabel.SHAMT6, MaskLabel.RS1, MaskLabel.FUNCT3, MaskLabel.RD, MaskLabel.OPCODE))
        ) {
            override fun execute(arch: ArchRV64, paramMap: Map<MaskLabel, Bin>, tracker: Memory.AccessTracker) {
                super.execute(arch, paramMap, tracker)
                val rdAddr = paramMap[MaskLabel.RD]
                val rs1Addr = paramMap[MaskLabel.RS1]
                if (rdAddr != null && rs1Addr != null) {
                    val rd = arch.getRegByAddr(rdAddr)
                    val rs1 = arch.getRegByAddr(rs1Addr)
                    val shamt6 = paramMap[MaskLabel.SHAMT6]
                    val pc = arch.regContainer.pc
                    if (rd != null && shamt6 != null && rs1 != null) {
                        rd.set((rs1.get().toBin().getUResized(Variable.Size.Bit32()) shr shamt6.getUResized(Variable.Size.Bit5()).getRawBinStr().toInt(2)).getResized(RV64.XLEN))
                        pc.set(pc.get() + Variable.Value.Hex("4"))
                    }
                }
            }
        },
        ADD(
            "ADD", false, ParamType.RD_RS1_RS2,
            RV64BinMapper.OpCode("0000000 00000 00000 000 00000 0110011", arrayOf(MaskLabel.FUNCT7, MaskLabel.RS2, MaskLabel.RS1, MaskLabel.FUNCT3, MaskLabel.RD, MaskLabel.OPCODE))
        ) {
            override fun execute(arch: ArchRV64, paramMap: Map<MaskLabel, Bin>, tracker: Memory.AccessTracker) {
                super.execute(arch, paramMap, tracker)
                val rdAddr = paramMap[MaskLabel.RD]
                val rs1Addr = paramMap[MaskLabel.RS1]
                val rs2Addr = paramMap[MaskLabel.RS2]
                if (rdAddr != null && rs1Addr != null && rs2Addr != null) {
                    val rd = arch.getRegByAddr(rdAddr)
                    val rs1 = arch.getRegByAddr(rs1Addr)
                    val rs2 = arch.getRegByAddr(rs2Addr)
                    val pc = arch.regContainer.pc
                    if (rd != null && rs1 != null && rs2 != null) {
                        rd.set(rs1.get().toBin() + rs2.get().toBin())
                        pc.set(pc.get() + Variable.Value.Hex("4"))
                    }
                }
            }
        },
        ADDW(
            "ADDW", false, ParamType.RD_RS1_RS2,
            RV64BinMapper.OpCode("0000000 00000 00000 000 00000 0111011", arrayOf(MaskLabel.FUNCT7, MaskLabel.RS2, MaskLabel.RS1, MaskLabel.FUNCT3, MaskLabel.RD, MaskLabel.OPCODE))
        ) {
            override fun execute(arch: ArchRV64, paramMap: Map<MaskLabel, Bin>, tracker: Memory.AccessTracker) {
                super.execute(arch, paramMap, tracker)
                val rdAddr = paramMap[MaskLabel.RD]
                val rs1Addr = paramMap[MaskLabel.RS1]
                val rs2Addr = paramMap[MaskLabel.RS2]
                if (rdAddr != null && rs1Addr != null && rs2Addr != null) {
                    val rd = arch.getRegByAddr(rdAddr)
                    val rs1 = arch.getRegByAddr(rs1Addr)
                    val rs2 = arch.getRegByAddr(rs2Addr)
                    val pc = arch.regContainer.pc
                    if (rd != null && rs1 != null && rs2 != null) {
                        rd.set((rs1.get().toBin().getResized(Variable.Size.Bit32()) + rs2.get().toBin().getResized(Variable.Size.Bit32())).toBin().getResized(RV64.XLEN))
                        pc.set(pc.get() + Variable.Value.Hex("4"))
                    }
                }
            }
        },
        SUB(
            "SUB", false, ParamType.RD_RS1_RS2,
            RV64BinMapper.OpCode("0100000 00000 00000 000 00000 0110011", arrayOf(MaskLabel.FUNCT7, MaskLabel.RS2, MaskLabel.RS1, MaskLabel.FUNCT3, MaskLabel.RD, MaskLabel.OPCODE))
        ) {
            override fun execute(arch: ArchRV64, paramMap: Map<MaskLabel, Bin>, tracker: Memory.AccessTracker) {
                super.execute(arch, paramMap, tracker)
                val rdAddr = paramMap[MaskLabel.RD]
                val rs1Addr = paramMap[MaskLabel.RS1]
                val rs2Addr = paramMap[MaskLabel.RS2]
                if (rdAddr != null && rs1Addr != null && rs2Addr != null) {
                    val rd = arch.getRegByAddr(rdAddr)
                    val rs1 = arch.getRegByAddr(rs1Addr)
                    val rs2 = arch.getRegByAddr(rs2Addr)
                    val pc = arch.regContainer.pc
                    if (rd != null && rs1 != null && rs2 != null) {
                        rd.set(rs1.get().toBin() - rs2.get().toBin())
                        pc.set(pc.get() + Variable.Value.Hex("4"))
                    }
                }
            }
        },
        SUBW(
            "SUBW", false, ParamType.RD_RS1_RS2,
            RV64BinMapper.OpCode("0100000 00000 00000 000 00000 0111011", arrayOf(MaskLabel.FUNCT7, MaskLabel.RS2, MaskLabel.RS1, MaskLabel.FUNCT3, MaskLabel.RD, MaskLabel.OPCODE))
        ) {
            override fun execute(arch: ArchRV64, paramMap: Map<MaskLabel, Bin>, tracker: Memory.AccessTracker) {
                super.execute(arch, paramMap, tracker)
                val rdAddr = paramMap[MaskLabel.RD]
                val rs1Addr = paramMap[MaskLabel.RS1]
                val rs2Addr = paramMap[MaskLabel.RS2]
                if (rdAddr != null && rs1Addr != null && rs2Addr != null) {
                    val rd = arch.getRegByAddr(rdAddr)
                    val rs1 = arch.getRegByAddr(rs1Addr)
                    val rs2 = arch.getRegByAddr(rs2Addr)
                    val pc = arch.regContainer.pc
                    if (rd != null && rs1 != null && rs2 != null) {
                        rd.set((rs1.get().toBin().getResized(Variable.Size.Bit32()) - rs2.get().toBin().getResized(Variable.Size.Bit32())).toBin().getResized(RV64.XLEN))
                        pc.set(pc.get() + Variable.Value.Hex("4"))
                    }
                }
            }
        },
        SLL(
            "SLL", false, ParamType.RD_RS1_RS2,
            RV64BinMapper.OpCode("0000000 00000 00000 001 00000 0110011", arrayOf(MaskLabel.FUNCT7, MaskLabel.RS2, MaskLabel.RS1, MaskLabel.FUNCT3, MaskLabel.RD, MaskLabel.OPCODE))
        ) {
            override fun execute(arch: ArchRV64, paramMap: Map<MaskLabel, Bin>, tracker: Memory.AccessTracker) {
                super.execute(arch, paramMap, tracker)
                val rdAddr = paramMap[MaskLabel.RD]
                val rs1Addr = paramMap[MaskLabel.RS1]
                val rs2Addr = paramMap[MaskLabel.RS2]
                if (rdAddr != null && rs1Addr != null && rs2Addr != null) {
                    val rd = arch.getRegByAddr(rdAddr)
                    val rs1 = arch.getRegByAddr(rs1Addr)
                    val rs2 = arch.getRegByAddr(rs2Addr)
                    val pc = arch.regContainer.pc
                    if (rd != null && rs1 != null && rs2 != null) {
                        rd.set(rs1.get().toBin() ushl rs2.get().toBin().getUResized(Variable.Size.Bit6()).getRawBinStr().toInt(2))
                        pc.set(pc.get() + Variable.Value.Hex("4"))
                    }
                }
            }
        },
        SLLW(
            "SLLW", false, ParamType.RD_RS1_RS2,
            RV64BinMapper.OpCode("0000000 00000 00000 001 00000 0111011", arrayOf(MaskLabel.FUNCT7, MaskLabel.RS2, MaskLabel.RS1, MaskLabel.FUNCT3, MaskLabel.RD, MaskLabel.OPCODE))
        ) {
            override fun execute(arch: ArchRV64, paramMap: Map<MaskLabel, Bin>, tracker: Memory.AccessTracker) {
                super.execute(arch, paramMap, tracker)
                val rdAddr = paramMap[MaskLabel.RD]
                val rs1Addr = paramMap[MaskLabel.RS1]
                val rs2Addr = paramMap[MaskLabel.RS2]
                if (rdAddr != null && rs1Addr != null && rs2Addr != null) {
                    val rd = arch.getRegByAddr(rdAddr)
                    val rs1 = arch.getRegByAddr(rs1Addr)
                    val rs2 = arch.getRegByAddr(rs2Addr)
                    val pc = arch.regContainer.pc
                    if (rd != null && rs1 != null && rs2 != null) {
                        rd.set(rs1.get().toBin().getUResized(Variable.Size.Bit32()) ushl rs2.get().toBin().getUResized(Variable.Size.Bit5()).getRawBinStr().toInt(2))
                        pc.set(pc.get() + Variable.Value.Hex("4"))
                    }
                }
            }
        },
        SLT(
            "SLT", false, ParamType.RD_RS1_RS2,
            RV64BinMapper.OpCode("0000000 00000 00000 010 00000 0110011", arrayOf(MaskLabel.FUNCT7, MaskLabel.RS2, MaskLabel.RS1, MaskLabel.FUNCT3, MaskLabel.RD, MaskLabel.OPCODE))
        ) {
            override fun execute(arch: ArchRV64, paramMap: Map<MaskLabel, Bin>, tracker: Memory.AccessTracker) {
                super.execute(arch, paramMap, tracker)
                val rdAddr = paramMap[MaskLabel.RD]
                val rs1Addr = paramMap[MaskLabel.RS1]
                val rs2Addr = paramMap[MaskLabel.RS2]
                if (rdAddr != null && rs1Addr != null && rs2Addr != null) {
                    val rd = arch.getRegByAddr(rdAddr)
                    val rs1 = arch.getRegByAddr(rs1Addr)
                    val rs2 = arch.getRegByAddr(rs2Addr)
                    val pc = arch.regContainer.pc
                    if (rd != null && rs1 != null && rs2 != null) {
                        rd.set(if (rs1.get().toDec() < rs2.get().toDec()) Variable.Value.Bin("1", Variable.Size.Bit32()) else Variable.Value.Bin("0", Variable.Size.Bit32()))
                        pc.set(pc.get() + Variable.Value.Hex("4"))
                    }
                }
            }
        },
        SLTU(
            "SLTU", false, ParamType.RD_RS1_RS2,
            RV64BinMapper.OpCode("0000000 00000 00000 011 00000 0110011", arrayOf(MaskLabel.FUNCT7, MaskLabel.RS2, MaskLabel.RS1, MaskLabel.FUNCT3, MaskLabel.RD, MaskLabel.OPCODE))
        ) {
            override fun execute(arch: ArchRV64, paramMap: Map<MaskLabel, Bin>, tracker: Memory.AccessTracker) {
                super.execute(arch, paramMap, tracker)
                val rdAddr = paramMap[MaskLabel.RD]
                val rs1Addr = paramMap[MaskLabel.RS1]
                val rs2Addr = paramMap[MaskLabel.RS2]
                if (rdAddr != null && rs1Addr != null && rs2Addr != null) {
                    val rd = arch.getRegByAddr(rdAddr)
                    val rs1 = arch.getRegByAddr(rs1Addr)
                    val rs2 = arch.getRegByAddr(rs2Addr)
                    val pc = arch.regContainer.pc
                    if (rd != null && rs1 != null && rs2 != null) {
                        rd.set(if (rs1.get().toBin() < rs2.get().toBin()) Variable.Value.Bin("1", Variable.Size.Bit32()) else Variable.Value.Bin("0", Variable.Size.Bit32()))
                        pc.set(pc.get() + Variable.Value.Hex("4"))
                    }
                }
            }
        },
        XOR(
            "XOR", false, ParamType.RD_RS1_RS2,
            RV64BinMapper.OpCode("0000000 00000 00000 100 00000 0110011", arrayOf(MaskLabel.FUNCT7, MaskLabel.RS2, MaskLabel.RS1, MaskLabel.FUNCT3, MaskLabel.RD, MaskLabel.OPCODE))
        ) {
            override fun execute(arch: ArchRV64, paramMap: Map<MaskLabel, Bin>, tracker: Memory.AccessTracker) {
                super.execute(arch, paramMap, tracker)
                val rdAddr = paramMap[MaskLabel.RD]
                val rs1Addr = paramMap[MaskLabel.RS1]
                val rs2Addr = paramMap[MaskLabel.RS2]
                if (rdAddr != null && rs1Addr != null && rs2Addr != null) {
                    val rd = arch.getRegByAddr(rdAddr)
                    val rs1 = arch.getRegByAddr(rs1Addr)
                    val rs2 = arch.getRegByAddr(rs2Addr)
                    val pc = arch.regContainer.pc
                    if (rd != null && rs1 != null && rs2 != null) {
                        rd.set(rs1.get().toBin() xor rs2.get().toBin())
                        pc.set(pc.get() + Variable.Value.Hex("4"))
                    }
                }
            }
        },
        SRL(
            "SRL", false, ParamType.RD_RS1_RS2,
            RV64BinMapper.OpCode("0000000 00000 00000 101 00000 0110011", arrayOf(MaskLabel.FUNCT7, MaskLabel.RS2, MaskLabel.RS1, MaskLabel.FUNCT3, MaskLabel.RD, MaskLabel.OPCODE))
        ) {
            override fun execute(arch: ArchRV64, paramMap: Map<MaskLabel, Bin>, tracker: Memory.AccessTracker) {
                super.execute(arch, paramMap, tracker)
                val rdAddr = paramMap[MaskLabel.RD]
                val rs1Addr = paramMap[MaskLabel.RS1]
                val rs2Addr = paramMap[MaskLabel.RS2]
                if (rdAddr != null && rs1Addr != null && rs2Addr != null) {
                    val rd = arch.getRegByAddr(rdAddr)
                    val rs1 = arch.getRegByAddr(rs1Addr)
                    val rs2 = arch.getRegByAddr(rs2Addr)
                    val pc = arch.regContainer.pc
                    if (rd != null && rs1 != null && rs2 != null) {
                        rd.set(rs1.get().toBin() ushr rs2.get().toBin().getUResized(Variable.Size.Bit6()).getRawBinStr().toInt(2))
                        pc.set(pc.get() + Variable.Value.Hex("4"))
                    }
                }
            }
        },
        SRLW(
            "SRLW", false, ParamType.RD_RS1_RS2,
            RV64BinMapper.OpCode("0000000 00000 00000 101 00000 0111011", arrayOf(MaskLabel.FUNCT7, MaskLabel.RS2, MaskLabel.RS1, MaskLabel.FUNCT3, MaskLabel.RD, MaskLabel.OPCODE))
        ) {
            override fun execute(arch: ArchRV64, paramMap: Map<MaskLabel, Bin>, tracker: Memory.AccessTracker) {
                super.execute(arch, paramMap, tracker)
                val rdAddr = paramMap[MaskLabel.RD]
                val rs1Addr = paramMap[MaskLabel.RS1]
                val rs2Addr = paramMap[MaskLabel.RS2]
                if (rdAddr != null && rs1Addr != null && rs2Addr != null) {
                    val rd = arch.getRegByAddr(rdAddr)
                    val rs1 = arch.getRegByAddr(rs1Addr)
                    val rs2 = arch.getRegByAddr(rs2Addr)
                    val pc = arch.regContainer.pc
                    if (rd != null && rs1 != null && rs2 != null) {
                        rd.set(rs1.get().toBin().getUResized(Variable.Size.Bit32()) ushr rs2.get().toBin().getUResized(Variable.Size.Bit5()).getRawBinStr().toInt(2))
                        pc.set(pc.get() + Variable.Value.Hex("4"))
                    }
                }
            }
        },
        SRA(
            "SRA", false, ParamType.RD_RS1_RS2,
            RV64BinMapper.OpCode("0100000 00000 00000 101 00000 0110011", arrayOf(MaskLabel.FUNCT7, MaskLabel.RS2, MaskLabel.RS1, MaskLabel.FUNCT3, MaskLabel.RD, MaskLabel.OPCODE))
        ) {
            override fun execute(arch: ArchRV64, paramMap: Map<MaskLabel, Bin>, tracker: Memory.AccessTracker) {
                super.execute(arch, paramMap, tracker)
                val rdAddr = paramMap[MaskLabel.RD]
                val rs1Addr = paramMap[MaskLabel.RS1]
                val rs2Addr = paramMap[MaskLabel.RS2]
                if (rdAddr != null && rs1Addr != null && rs2Addr != null) {
                    val rd = arch.getRegByAddr(rdAddr)
                    val rs1 = arch.getRegByAddr(rs1Addr)
                    val rs2 = arch.getRegByAddr(rs2Addr)
                    val pc = arch.regContainer.pc
                    if (rd != null && rs1 != null && rs2 != null) {
                        rd.set(rs1.get().toBin() shr rs2.get().toBin().getUResized(Variable.Size.Bit6()).getRawBinStr().toInt(2))
                        pc.set(pc.get() + Variable.Value.Hex("4"))
                    }
                }
            }
        },
        SRAW(
            "SRAW", false, ParamType.RD_RS1_RS2,
            RV64BinMapper.OpCode("0100000 00000 00000 101 00000 0111011", arrayOf(MaskLabel.FUNCT7, MaskLabel.RS2, MaskLabel.RS1, MaskLabel.FUNCT3, MaskLabel.RD, MaskLabel.OPCODE))
        ) {
            override fun execute(arch: ArchRV64, paramMap: Map<MaskLabel, Bin>, tracker: Memory.AccessTracker) {
                super.execute(arch, paramMap, tracker)
                val rdAddr = paramMap[MaskLabel.RD]
                val rs1Addr = paramMap[MaskLabel.RS1]
                val rs2Addr = paramMap[MaskLabel.RS2]
                if (rdAddr != null && rs1Addr != null && rs2Addr != null) {
                    val rd = arch.getRegByAddr(rdAddr)
                    val rs1 = arch.getRegByAddr(rs1Addr)
                    val rs2 = arch.getRegByAddr(rs2Addr)
                    val pc = arch.regContainer.pc
                    if (rd != null && rs1 != null && rs2 != null) {
                        rd.set(rs1.get().toBin().getUResized(Variable.Size.Bit32()) shr rs2.get().toBin().getUResized(Variable.Size.Bit5()).getRawBinStr().toInt(2))
                        pc.set(pc.get() + Variable.Value.Hex("4"))
                    }
                }
            }
        },
        OR(
            "OR",
            false,
            ParamType.RD_RS1_RS2,
            RV64BinMapper.OpCode("0000000 00000 00000 110 00000 0110011", arrayOf(MaskLabel.FUNCT7, MaskLabel.RS2, MaskLabel.RS1, MaskLabel.FUNCT3, MaskLabel.RD, MaskLabel.OPCODE))
        ) {
            override fun execute(arch: ArchRV64, paramMap: Map<MaskLabel, Bin>, tracker: Memory.AccessTracker) {
                super.execute(arch, paramMap, tracker)
                val rdAddr = paramMap[MaskLabel.RD]
                val rs1Addr = paramMap[MaskLabel.RS1]
                val rs2Addr = paramMap[MaskLabel.RS2]
                if (rdAddr != null && rs1Addr != null && rs2Addr != null) {
                    val rd = arch.getRegByAddr(rdAddr)
                    val rs1 = arch.getRegByAddr(rs1Addr)
                    val rs2 = arch.getRegByAddr(rs2Addr)
                    val pc = arch.regContainer.pc
                    if (rd != null && rs1 != null && rs2 != null) {
                        rd.set(rs1.get().toBin() or rs2.get().toBin())
                        pc.set(pc.get() + Variable.Value.Hex("4"))
                    }
                }
            }
        },
        AND(
            "AND", false, ParamType.RD_RS1_RS2,
            RV64BinMapper.OpCode("0000000 00000 00000 111 00000 0110011", arrayOf(MaskLabel.FUNCT7, MaskLabel.RS2, MaskLabel.RS1, MaskLabel.FUNCT3, MaskLabel.RD, MaskLabel.OPCODE))
        ) {
            override fun execute(arch: ArchRV64, paramMap: Map<MaskLabel, Bin>, tracker: Memory.AccessTracker) {
                super.execute(arch, paramMap, tracker)
                val rdAddr = paramMap[MaskLabel.RD]
                val rs1Addr = paramMap[MaskLabel.RS1]
                val rs2Addr = paramMap[MaskLabel.RS2]
                if (rdAddr != null && rs1Addr != null && rs2Addr != null) {
                    val rd = arch.getRegByAddr(rdAddr)
                    val rs1 = arch.getRegByAddr(rs1Addr)
                    val rs2 = arch.getRegByAddr(rs2Addr)
                    val pc = arch.regContainer.pc
                    if (rd != null && rs1 != null && rs2 != null) {
                        rd.set(rs1.get().toBin() and rs2.get().toBin())
                        pc.set(pc.get() + Variable.Value.Hex("4"))
                    }
                }
            }
        },

        // CSR Extension
        CSRRW(
            "CSRRW",
            false,
            ParamType.CSR_RD_OFF12_RS1,
            RV64BinMapper.OpCode("000000000000 00000 001 00000 1110011", arrayOf(MaskLabel.CSR, MaskLabel.RS1, MaskLabel.FUNCT3, MaskLabel.RD, MaskLabel.OPCODE)),
            needFeatures = listOf(RV64.EXTENSION.CSR.ordinal)
        ) {
            override fun execute(arch: ArchRV64, paramMap: Map<MaskLabel, Bin>, tracker: Memory.AccessTracker) {
                super.execute(arch, paramMap, tracker)
                val rdAddr = paramMap[MaskLabel.RD]
                val rs1Addr = paramMap[MaskLabel.RS1]
                val csrAddr = paramMap[MaskLabel.CSR]
                if (rdAddr != null && rs1Addr != null && csrAddr != null) {
                    val rd = arch.getRegByAddr(rdAddr)
                    val rs1 = arch.getRegByAddr(rs1Addr)
                    val csr = arch.getRegByAddr(csrAddr, RV64.CSR_REGFILE_NAME)
                    val pc = arch.regContainer.pc
                    if (rd != null && rs1 != null && csr != null) {
                        if (rd.address.toHex().getRawHexStr() != "00000") {
                            val t = csr.get().toBin().getUResized(RV64.XLEN)
                            rd.set(t)
                        }

                        csr.set(rs1.get())

                        pc.set(pc.get() + Variable.Value.Hex("4"))
                    }
                }
            }
        },
        CSRRS(
            "CSRRS",
            false,
            ParamType.CSR_RD_OFF12_RS1,
            RV64BinMapper.OpCode("000000000000 00000 010 00000 1110011", arrayOf(MaskLabel.CSR, MaskLabel.RS1, MaskLabel.FUNCT3, MaskLabel.RD, MaskLabel.OPCODE)),
            needFeatures = listOf(RV64.EXTENSION.CSR.ordinal)
        ) {
            override fun execute(arch: ArchRV64, paramMap: Map<MaskLabel, Bin>, tracker: Memory.AccessTracker) {
                super.execute(arch, paramMap, tracker)
                val rdAddr = paramMap[MaskLabel.RD]
                val rs1Addr = paramMap[MaskLabel.RS1]
                val csrAddr = paramMap[MaskLabel.CSR]
                if (rdAddr != null && rs1Addr != null && csrAddr != null) {
                    val rd = arch.getRegByAddr(rdAddr)
                    val rs1 = arch.getRegByAddr(rs1Addr)
                    val csr = arch.getRegByAddr(csrAddr, RV64.CSR_REGFILE_NAME)
                    val pc = arch.regContainer.pc
                    if (rd != null && rs1 != null && csr != null) {
                        if (rd.address.toHex().getRawHexStr() != "00000") {
                            val t = csr.get().toBin().getUResized(RV64.XLEN)
                            rd.set(t)
                        }

                        csr.set(rs1.get().toBin() or csr.get().toBin())

                        pc.set(pc.get() + Variable.Value.Hex("4"))
                    }
                }
            }
        },
        CSRRC(
            "CSRRC",
            false,
            ParamType.CSR_RD_OFF12_RS1,
            RV64BinMapper.OpCode("000000000000 00000 011 00000 1110011", arrayOf(MaskLabel.CSR, MaskLabel.RS1, MaskLabel.FUNCT3, MaskLabel.RD, MaskLabel.OPCODE)),
            needFeatures = listOf(RV64.EXTENSION.CSR.ordinal)
        ) {
            override fun execute(arch: ArchRV64, paramMap: Map<MaskLabel, Bin>, tracker: Memory.AccessTracker) {
                super.execute(arch, paramMap, tracker)
                val rdAddr = paramMap[MaskLabel.RD]
                val rs1Addr = paramMap[MaskLabel.RS1]
                val csrAddr = paramMap[MaskLabel.CSR]
                if (rdAddr != null && rs1Addr != null && csrAddr != null) {
                    val rd = arch.getRegByAddr(rdAddr)
                    val rs1 = arch.getRegByAddr(rs1Addr)
                    val csr = arch.getRegByAddr(csrAddr, RV64.CSR_REGFILE_NAME)
                    val pc = arch.regContainer.pc
                    if (rd != null && rs1 != null && csr != null) {
                        if (rd.address.toHex().getRawHexStr() != "00000") {
                            val t = csr.get().toBin().getUResized(RV64.XLEN)
                            rd.set(t)
                        }

                        csr.set(csr.get().toBin() and rs1.get().toBin().inv())

                        pc.set(pc.get() + Variable.Value.Hex("4"))
                    }
                }
            }
        },
        CSRRWI(
            "CSRRWI",
            false,
            ParamType.CSR_RD_OFF12_UIMM5,
            RV64BinMapper.OpCode("000000000000 00000 101 00000 1110011", arrayOf(MaskLabel.CSR, MaskLabel.UIMM5, MaskLabel.FUNCT3, MaskLabel.RD, MaskLabel.OPCODE)),
            needFeatures = listOf(RV64.EXTENSION.CSR.ordinal)
        ) {
            override fun execute(arch: ArchRV64, paramMap: Map<MaskLabel, Bin>, tracker: Memory.AccessTracker) {
                super.execute(arch, paramMap, tracker)
                val rdAddr = paramMap[MaskLabel.RD]
                val uimm5 = paramMap[MaskLabel.UIMM5]
                val csrAddr = paramMap[MaskLabel.CSR]
                if (rdAddr != null && uimm5 != null && csrAddr != null) {
                    val rd = arch.getRegByAddr(rdAddr)
                    val csr = arch.getRegByAddr(csrAddr, RV64.CSR_REGFILE_NAME)
                    val pc = arch.regContainer.pc
                    if (rd != null && csr != null) {
                        if (rd.address.toHex().getRawHexStr() != "00000") {
                            val t = csr.get().toBin().getUResized(RV64.XLEN)
                            rd.set(t)
                        }

                        csr.set(uimm5.getUResized(RV64.XLEN))

                        pc.set(pc.get() + Variable.Value.Hex("4"))
                    }
                }
            }
        },
        CSRRSI(
            "CSRRSI",
            false,
            ParamType.CSR_RD_OFF12_UIMM5,
            RV64BinMapper.OpCode("000000000000 00000 110 00000 1110011", arrayOf(MaskLabel.CSR, MaskLabel.UIMM5, MaskLabel.FUNCT3, MaskLabel.RD, MaskLabel.OPCODE)),
            needFeatures = listOf(RV64.EXTENSION.CSR.ordinal)
        ) {
            override fun execute(arch: ArchRV64, paramMap: Map<MaskLabel, Bin>, tracker: Memory.AccessTracker) {
                super.execute(arch, paramMap, tracker)
                val rdAddr = paramMap[MaskLabel.RD]
                val uimm5 = paramMap[MaskLabel.UIMM5]
                val csrAddr = paramMap[MaskLabel.CSR]
                if (rdAddr != null && uimm5 != null && csrAddr != null) {
                    val rd = arch.getRegByAddr(rdAddr)
                    val csr = arch.getRegByAddr(csrAddr, RV64.CSR_REGFILE_NAME)
                    val pc = arch.regContainer.pc
                    if (rd != null && csr != null) {
                        if (rd.address.toHex().getRawHexStr() != "00000") {
                            val t = csr.get().toBin().getUResized(RV64.XLEN)
                            rd.set(t)
                        }

                        csr.set(csr.get().toBin() or uimm5.getUResized(RV64.XLEN))

                        pc.set(pc.get() + Variable.Value.Hex("4"))
                    }
                }
            }
        },
        CSRRCI(
            "CSRRCI",
            false,
            ParamType.CSR_RD_OFF12_UIMM5,
            RV64BinMapper.OpCode("000000000000 00000 111 00000 1110011", arrayOf(MaskLabel.CSR, MaskLabel.UIMM5, MaskLabel.FUNCT3, MaskLabel.RD, MaskLabel.OPCODE)),
            needFeatures = listOf(RV64.EXTENSION.CSR.ordinal)
        ) {
            override fun execute(arch: ArchRV64, paramMap: Map<MaskLabel, Bin>, tracker: Memory.AccessTracker) {
                super.execute(arch, paramMap, tracker)
                val rdAddr = paramMap[MaskLabel.RD]
                val uimm5 = paramMap[MaskLabel.UIMM5]
                val csrAddr = paramMap[MaskLabel.CSR]
                if (rdAddr != null && uimm5 != null && csrAddr != null) {
                    val rd = arch.getRegByAddr(rdAddr)
                    val csr = arch.getRegByAddr(csrAddr, RV64.CSR_REGFILE_NAME)
                    val pc = arch.regContainer.pc
                    if (rd != null && csr != null) {
                        if (rd.address.toHex().getRawHexStr() != "00000") {
                            val t = csr.get().toBin().getUResized(RV64.XLEN)
                            rd.set(t)
                        }

                        csr.set(csr.get().toBin() and uimm5.getUResized(RV64.XLEN).inv())

                        pc.set(pc.get() + Variable.Value.Hex("4"))
                    }
                }
            }
        },

        CSRW("CSRW", true, ParamType.PS_CSR_RS1, needFeatures = listOf(RV64.EXTENSION.CSR.ordinal)),
        CSRR("CSRR", true, ParamType.PS_RD_CSR, needFeatures = listOf(RV64.EXTENSION.CSR.ordinal)),

        // M Extension
        MUL(
            "MUL",
            false,
            ParamType.RD_RS1_RS2,
            RV64BinMapper.OpCode("0000001 00000 00000 000 00000 0110011", arrayOf(MaskLabel.FUNCT7, MaskLabel.RS2, MaskLabel.RS1, MaskLabel.FUNCT3, MaskLabel.RD, MaskLabel.OPCODE)),
            needFeatures = listOf(RV64.EXTENSION.M.ordinal)
        ) {
            override fun execute(arch: ArchRV64, paramMap: Map<MaskLabel, Bin>, tracker: Memory.AccessTracker) {
                super.execute(arch, paramMap, tracker)
                val rdAddr = paramMap[MaskLabel.RD]
                val rs1Addr = paramMap[MaskLabel.RS1]
                val rs2Addr = paramMap[MaskLabel.RS2]

                if (rdAddr != null && rs1Addr != null && rs2Addr != null) {
                    val rd = arch.getRegByAddr(rdAddr)
                    val rs1 = arch.getRegByAddr(rs1Addr)
                    val rs2 = arch.getRegByAddr(rs2Addr)
                    val pc = arch.regContainer.pc
                    if (rd != null && rs1 != null && rs2 != null) {
                        val factor1 = rs1.get().toBin()
                        val factor2 = rs2.get().toBin()
                        val result = factor1.flexTimesSigned(factor2)
                        rd.set(result)
                        pc.set(pc.get() + Variable.Value.Hex("4"))
                    }
                }
            }
        },
        MULH(
            "MULH",
            false,
            ParamType.RD_RS1_RS2,
            RV64BinMapper.OpCode("0000001 00000 00000 001 00000 0110011", arrayOf(MaskLabel.FUNCT7, MaskLabel.RS2, MaskLabel.RS1, MaskLabel.FUNCT3, MaskLabel.RD, MaskLabel.OPCODE)),
            needFeatures = listOf(RV64.EXTENSION.M.ordinal)
        ) {
            override fun execute(arch: ArchRV64, paramMap: Map<MaskLabel, Bin>, tracker: Memory.AccessTracker) {
                super.execute(arch, paramMap, tracker)
                val rdAddr = paramMap[MaskLabel.RD]
                val rs1Addr = paramMap[MaskLabel.RS1]
                val rs2Addr = paramMap[MaskLabel.RS2]

                if (rdAddr != null && rs1Addr != null && rs2Addr != null) {
                    val rd = arch.getRegByAddr(rdAddr)
                    val rs1 = arch.getRegByAddr(rs1Addr)
                    val rs2 = arch.getRegByAddr(rs2Addr)
                    val pc = arch.regContainer.pc
                    if (rd != null && rs1 != null && rs2 != null) {
                        val factor1 = rs1.get().toBin()
                        val factor2 = rs2.get().toBin()
                        val result = factor1.flexTimesSigned(factor2, false).shr(RV64.XLEN.bitWidth).getResized(RV64.XLEN)
                        rd.set(result)
                        pc.set(pc.get() + Variable.Value.Hex("4"))
                    }
                }
            }
        },
        MULHSU(
            "MULHSU",
            false,
            ParamType.RD_RS1_RS2,
            RV64BinMapper.OpCode("0000001 00000 00000 010 00000 0110011", arrayOf(MaskLabel.FUNCT7, MaskLabel.RS2, MaskLabel.RS1, MaskLabel.FUNCT3, MaskLabel.RD, MaskLabel.OPCODE)),
            needFeatures = listOf(RV64.EXTENSION.M.ordinal)
        ) {
            override fun execute(arch: ArchRV64, paramMap: Map<MaskLabel, Bin>, tracker: Memory.AccessTracker) {
                super.execute(arch, paramMap, tracker)
                val rdAddr = paramMap[MaskLabel.RD]
                val rs1Addr = paramMap[MaskLabel.RS1]
                val rs2Addr = paramMap[MaskLabel.RS2]

                if (rdAddr != null && rs1Addr != null && rs2Addr != null) {
                    val rd = arch.getRegByAddr(rdAddr)
                    val rs1 = arch.getRegByAddr(rs1Addr)
                    val rs2 = arch.getRegByAddr(rs2Addr)
                    val pc = arch.regContainer.pc
                    if (rd != null && rs1 != null && rs2 != null) {
                        val factor1 = rs1.get().toBin()
                        val factor2 = rs2.get().toBin()
                        val result = factor1.flexTimesSigned(factor2, resizeToLargestParamSize = false, true).ushr(RV64.XLEN.bitWidth).getResized(RV64.XLEN)
                        rd.set(result)
                        pc.set(pc.get() + Variable.Value.Hex("4"))
                    }
                }
            }
        },
        MULHU(
            "MULHU",
            false,
            ParamType.RD_RS1_RS2,
            RV64BinMapper.OpCode("0000001 00000 00000 011 00000 0110011", arrayOf(MaskLabel.FUNCT7, MaskLabel.RS2, MaskLabel.RS1, MaskLabel.FUNCT3, MaskLabel.RD, MaskLabel.OPCODE)),
            needFeatures = listOf(RV64.EXTENSION.M.ordinal)
        ) {
            override fun execute(arch: ArchRV64, paramMap: Map<MaskLabel, Bin>, tracker: Memory.AccessTracker) {
                super.execute(arch, paramMap, tracker)
                val rdAddr = paramMap[MaskLabel.RD]
                val rs1Addr = paramMap[MaskLabel.RS1]
                val rs2Addr = paramMap[MaskLabel.RS2]

                if (rdAddr != null && rs1Addr != null && rs2Addr != null) {
                    val rd = arch.getRegByAddr(rdAddr)
                    val rs1 = arch.getRegByAddr(rs1Addr)
                    val rs2 = arch.getRegByAddr(rs2Addr)
                    val pc = arch.regContainer.pc
                    if (rd != null && rs1 != null && rs2 != null) {
                        val factor1 = rs1.get().toBin()
                        val factor2 = rs2.get().toBin()
                        val result = (factor1 * factor2).toBin().ushr(RV64.XLEN.bitWidth).getUResized(RV64.XLEN)
                        rd.set(result)
                        pc.set(pc.get() + Variable.Value.Hex("4"))
                    }
                }
            }
        },
        DIV(
            "DIV",
            false,
            ParamType.RD_RS1_RS2,
            RV64BinMapper.OpCode("0000001 00000 00000 100 00000 0110011", arrayOf(MaskLabel.FUNCT7, MaskLabel.RS2, MaskLabel.RS1, MaskLabel.FUNCT3, MaskLabel.RD, MaskLabel.OPCODE)),
            needFeatures = listOf(RV64.EXTENSION.M.ordinal)
        ) {
            override fun execute(arch: ArchRV64, paramMap: Map<MaskLabel, Bin>, tracker: Memory.AccessTracker) {
                super.execute(arch, paramMap, tracker)
                val rdAddr = paramMap[MaskLabel.RD]
                val rs1Addr = paramMap[MaskLabel.RS1]
                val rs2Addr = paramMap[MaskLabel.RS2]

                if (rdAddr != null && rs1Addr != null && rs2Addr != null) {
                    val rd = arch.getRegByAddr(rdAddr)
                    val rs1 = arch.getRegByAddr(rs1Addr)
                    val rs2 = arch.getRegByAddr(rs2Addr)
                    val pc = arch.regContainer.pc
                    if (rd != null && rs1 != null && rs2 != null) {
                        val factor1 = rs1.get().toBin()
                        val factor2 = rs2.get().toBin()
                        val result = factor1.flexDivSigned(factor2, dividendIsUnsigned = true)
                        rd.set(result)
                        pc.set(pc.get() + Variable.Value.Hex("4"))
                    }
                }
            }
        },
        DIVU(
            "DIVU",
            false,
            ParamType.RD_RS1_RS2,
            RV64BinMapper.OpCode("0000001 00000 00000 101 00000 0110011", arrayOf(MaskLabel.FUNCT7, MaskLabel.RS2, MaskLabel.RS1, MaskLabel.FUNCT3, MaskLabel.RD, MaskLabel.OPCODE)),
            needFeatures = listOf(RV64.EXTENSION.M.ordinal)
        ) {
            override fun execute(arch: ArchRV64, paramMap: Map<MaskLabel, Bin>, tracker: Memory.AccessTracker) {
                super.execute(arch, paramMap, tracker)
                val rdAddr = paramMap[MaskLabel.RD]
                val rs1Addr = paramMap[MaskLabel.RS1]
                val rs2Addr = paramMap[MaskLabel.RS2]

                if (rdAddr != null && rs1Addr != null && rs2Addr != null) {
                    val rd = arch.getRegByAddr(rdAddr)
                    val rs1 = arch.getRegByAddr(rs1Addr)
                    val rs2 = arch.getRegByAddr(rs2Addr)
                    val pc = arch.regContainer.pc
                    if (rd != null && rs1 != null && rs2 != null) {
                        val factor1 = rs1.get().toBin()
                        val factor2 = rs2.get().toBin()
                        val result = factor1 / factor2
                        rd.set(result)
                        pc.set(pc.get() + Variable.Value.Hex("4"))
                    }
                }
            }
        },
        REM(
            "REM",
            false,
            ParamType.RD_RS1_RS2,
            RV64BinMapper.OpCode("0000001 00000 00000 110 00000 0110011", arrayOf(MaskLabel.FUNCT7, MaskLabel.RS2, MaskLabel.RS1, MaskLabel.FUNCT3, MaskLabel.RD, MaskLabel.OPCODE)),
            needFeatures = listOf(RV64.EXTENSION.M.ordinal)
        ) {
            override fun execute(arch: ArchRV64, paramMap: Map<MaskLabel, Bin>, tracker: Memory.AccessTracker) {
                super.execute(arch, paramMap, tracker)
                val rdAddr = paramMap[MaskLabel.RD]
                val rs1Addr = paramMap[MaskLabel.RS1]
                val rs2Addr = paramMap[MaskLabel.RS2]

                if (rdAddr != null && rs1Addr != null && rs2Addr != null) {
                    val rd = arch.getRegByAddr(rdAddr)
                    val rs1 = arch.getRegByAddr(rs1Addr)
                    val rs2 = arch.getRegByAddr(rs2Addr)
                    val pc = arch.regContainer.pc
                    if (rd != null && rs1 != null && rs2 != null) {
                        val factor1 = rs1.get().toBin()
                        val factor2 = rs2.get().toBin()
                        val result = factor1.flexRemSigned(factor2)
                        rd.set(result)
                        pc.set(pc.get() + Variable.Value.Hex("4"))
                    }
                }
            }
        },
        REMU(
            "REMU",
            false,
            ParamType.RD_RS1_RS2,
            RV64BinMapper.OpCode("0000001 00000 00000 111 00000 0110011", arrayOf(MaskLabel.FUNCT7, MaskLabel.RS2, MaskLabel.RS1, MaskLabel.FUNCT3, MaskLabel.RD, MaskLabel.OPCODE)),
            needFeatures = listOf(RV64.EXTENSION.M.ordinal)
        ) {
            override fun execute(arch: ArchRV64, paramMap: Map<MaskLabel, Bin>, tracker: Memory.AccessTracker) {
                super.execute(arch, paramMap, tracker)
                val rdAddr = paramMap[MaskLabel.RD]
                val rs1Addr = paramMap[MaskLabel.RS1]
                val rs2Addr = paramMap[MaskLabel.RS2]

                if (rdAddr != null && rs1Addr != null && rs2Addr != null) {
                    val rd = arch.getRegByAddr(rdAddr)
                    val rs1 = arch.getRegByAddr(rs1Addr)
                    val rs2 = arch.getRegByAddr(rs2Addr)
                    val pc = arch.regContainer.pc
                    if (rd != null && rs1 != null && rs2 != null) {
                        val factor1 = rs1.get().toBin()
                        val factor2 = rs2.get().toBin()
                        val result = factor1 % factor2
                        rd.set(result)
                        pc.set(pc.get() + Variable.Value.Hex("4"))
                    }
                }
            }
        },

        // RV64 M Extension
        MULW(
            "MULW",
            false,
            ParamType.RD_RS1_RS2,
            RV64BinMapper.OpCode("0000001 00000 00000 000 00000 0111011", arrayOf(MaskLabel.FUNCT7, MaskLabel.RS2, MaskLabel.RS1, MaskLabel.FUNCT3, MaskLabel.RD, MaskLabel.OPCODE)),
            needFeatures = listOf(RV64.EXTENSION.M.ordinal)
        ) {
            override fun execute(arch: ArchRV64, paramMap: Map<MaskLabel, Bin>, tracker: Memory.AccessTracker) {
                super.execute(arch, paramMap, tracker)
                val rdAddr = paramMap[MaskLabel.RD]
                val rs1Addr = paramMap[MaskLabel.RS1]
                val rs2Addr = paramMap[MaskLabel.RS2]

                if (rdAddr != null && rs1Addr != null && rs2Addr != null) {
                    val rd = arch.getRegByAddr(rdAddr)
                    val rs1 = arch.getRegByAddr(rs1Addr)
                    val rs2 = arch.getRegByAddr(rs2Addr)
                    val pc = arch.regContainer.pc
                    if (rd != null && rs1 != null && rs2 != null) {
                        val factor1 = rs1.get().toBin()
                        val factor2 = rs2.get().toBin()
                        val result = factor1.flexTimesSigned(factor2).getUResized(Variable.Size.Bit32()).getResized(RV64.XLEN)
                        rd.set(result)
                        pc.set(pc.get() + Variable.Value.Hex("4"))
                    }
                }
            }
        },
        DIVW(
            "DIVW",
            false,
            ParamType.RD_RS1_RS2,
            RV64BinMapper.OpCode("0000001 00000 00000 100 00000 0111011", arrayOf(MaskLabel.FUNCT7, MaskLabel.RS2, MaskLabel.RS1, MaskLabel.FUNCT3, MaskLabel.RD, MaskLabel.OPCODE)),
            needFeatures = listOf(RV64.EXTENSION.M.ordinal)
        ) {
            override fun execute(arch: ArchRV64, paramMap: Map<MaskLabel, Bin>, tracker: Memory.AccessTracker) {
                super.execute(arch, paramMap, tracker)
                val rdAddr = paramMap[MaskLabel.RD]
                val rs1Addr = paramMap[MaskLabel.RS1]
                val rs2Addr = paramMap[MaskLabel.RS2]

                if (rdAddr != null && rs1Addr != null && rs2Addr != null) {
                    val rd = arch.getRegByAddr(rdAddr)
                    val rs1 = arch.getRegByAddr(rs1Addr)
                    val rs2 = arch.getRegByAddr(rs2Addr)
                    val pc = arch.regContainer.pc
                    if (rd != null && rs1 != null && rs2 != null) {
                        val factor1 = rs1.get().toBin().getUResized(Variable.Size.Bit32())
                        val factor2 = rs2.get().toBin().getUResized(Variable.Size.Bit32())
                        val result = factor1.flexDivSigned(factor2, dividendIsUnsigned = true).getUResized(RV64.XLEN)
                        rd.set(result)
                        pc.set(pc.get() + Variable.Value.Hex("4"))
                    }
                }
            }
        },
        DIVUW(
            "DIVUW",
            false,
            ParamType.RD_RS1_RS2,
            RV64BinMapper.OpCode("0000001 00000 00000 101 00000 0111011", arrayOf(MaskLabel.FUNCT7, MaskLabel.RS2, MaskLabel.RS1, MaskLabel.FUNCT3, MaskLabel.RD, MaskLabel.OPCODE)),
            needFeatures = listOf(RV64.EXTENSION.M.ordinal)
        ) {
            override fun execute(arch: ArchRV64, paramMap: Map<MaskLabel, Bin>, tracker: Memory.AccessTracker) {
                super.execute(arch, paramMap, tracker)
                val rdAddr = paramMap[MaskLabel.RD]
                val rs1Addr = paramMap[MaskLabel.RS1]
                val rs2Addr = paramMap[MaskLabel.RS2]

                if (rdAddr != null && rs1Addr != null && rs2Addr != null) {
                    val rd = arch.getRegByAddr(rdAddr)
                    val rs1 = arch.getRegByAddr(rs1Addr)
                    val rs2 = arch.getRegByAddr(rs2Addr)
                    val pc = arch.regContainer.pc
                    if (rd != null && rs1 != null && rs2 != null) {
                        val factor1 = rs1.get().toBin().getUResized(Variable.Size.Bit32())
                        val factor2 = rs2.get().toBin().getUResized(Variable.Size.Bit32())
                        val result = (factor1 / factor2).toBin().getUResized(RV64.XLEN)
                        rd.set(result)
                        pc.set(pc.get() + Variable.Value.Hex("4"))
                    }
                }
            }
        },
        REMW(
            "REMW",
            false,
            ParamType.RD_RS1_RS2,
            RV64BinMapper.OpCode("0000001 00000 00000 110 00000 0111011", arrayOf(MaskLabel.FUNCT7, MaskLabel.RS2, MaskLabel.RS1, MaskLabel.FUNCT3, MaskLabel.RD, MaskLabel.OPCODE)),
            needFeatures = listOf(RV64.EXTENSION.M.ordinal)
        ) {
            override fun execute(arch: ArchRV64, paramMap: Map<MaskLabel, Bin>, tracker: Memory.AccessTracker) {
                super.execute(arch, paramMap, tracker)
                val rdAddr = paramMap[MaskLabel.RD]
                val rs1Addr = paramMap[MaskLabel.RS1]
                val rs2Addr = paramMap[MaskLabel.RS2]

                if (rdAddr != null && rs1Addr != null && rs2Addr != null) {
                    val rd = arch.getRegByAddr(rdAddr)
                    val rs1 = arch.getRegByAddr(rs1Addr)
                    val rs2 = arch.getRegByAddr(rs2Addr)
                    val pc = arch.regContainer.pc
                    if (rd != null && rs1 != null && rs2 != null) {
                        val factor1 = rs1.get().toBin().getUResized(Variable.Size.Bit32())
                        val factor2 = rs2.get().toBin().getUResized(Variable.Size.Bit32())
                        val result = factor1.flexRemSigned(factor2).getUResized(RV64.XLEN)
                        rd.set(result)
                        pc.set(pc.get() + Variable.Value.Hex("4"))
                    }
                }
            }
        },
        REMUW(
            "REMUW",
            false,
            ParamType.RD_RS1_RS2,
            RV64BinMapper.OpCode("0000001 00000 00000 111 00000 0111011", arrayOf(MaskLabel.FUNCT7, MaskLabel.RS2, MaskLabel.RS1, MaskLabel.FUNCT3, MaskLabel.RD, MaskLabel.OPCODE)),
            needFeatures = listOf(RV64.EXTENSION.M.ordinal)
        ) {
            override fun execute(arch: ArchRV64, paramMap: Map<MaskLabel, Bin>, tracker: Memory.AccessTracker) {
                super.execute(arch, paramMap, tracker)
                val rdAddr = paramMap[MaskLabel.RD]
                val rs1Addr = paramMap[MaskLabel.RS1]
                val rs2Addr = paramMap[MaskLabel.RS2]

                if (rdAddr != null && rs1Addr != null && rs2Addr != null) {
                    val rd = arch.getRegByAddr(rdAddr)
                    val rs1 = arch.getRegByAddr(rs1Addr)
                    val rs2 = arch.getRegByAddr(rs2Addr)
                    val pc = arch.regContainer.pc
                    if (rd != null && rs1 != null && rs2 != null) {
                        val factor1 = rs1.get().toBin().getUResized(Variable.Size.Bit32())
                        val factor2 = rs2.get().toBin().getUResized(Variable.Size.Bit32())
                        val result = (factor1 % factor2).toBin().getUResized(RV64.XLEN)
                        rd.set(result)
                        pc.set(pc.get() + Variable.Value.Hex("4"))
                    }
                }
            }
        },

        // Pseudo
        FENCEI("FENCE.I", true, ParamType.PS_NONE),
        Nop("NOP", true, ParamType.PS_NONE),
        Mv("MV", true, ParamType.PS_RD_RS1),
        Li64("LI", true, ParamType.PS_RD_LI_I64, memWords = 8),
        La("LA", true, ParamType.PS_RD_Albl, memWords = 2),
        Not("NOT", true, ParamType.PS_RD_RS1),
        Neg("NEG", true, ParamType.PS_RD_RS1),
        Seqz("SEQZ", true, ParamType.PS_RD_RS1),
        Snez("SNEZ", true, ParamType.PS_RD_RS1),
        Sltz("SLTZ", true, ParamType.PS_RD_RS1),
        Sgtz("SGTZ", true, ParamType.PS_RD_RS1),
        Beqz("BEQZ", true, ParamType.PS_RS1_Jlbl),
        Bnez("BNEZ", true, ParamType.PS_RS1_Jlbl),
        Blez("BLEZ", true, ParamType.PS_RS1_Jlbl),
        Bgez("BGEZ", true, ParamType.PS_RS1_Jlbl),
        Bltz("BLTZ", true, ParamType.PS_RS1_Jlbl),
        BGTZ("BGTZ", true, ParamType.PS_RS1_Jlbl),
        Bgt("BGT", true, ParamType.RS1_RS2_LBL),
        Ble("BLE", true, ParamType.RS1_RS2_LBL),
        Bgtu("BGTU", true, ParamType.RS1_RS2_LBL),
        Bleu("BLEU", true, ParamType.RS1_RS2_LBL),
        J("J", true, ParamType.PS_lbl),
        JAL1("JAL", true, ParamType.PS_lbl, relative = JAL),
        Jr("JR", true, ParamType.PS_RS1),
        JALR1("JALR", true, ParamType.PS_RS1, relative = JALR),
        JALR2("JALR", true, ParamType.RD_Off12, relative = JALR),
        Ret("RET", true, ParamType.PS_NONE),
        Call("CALL", true, ParamType.PS_lbl, memWords = 2),
        Tail("TAIL", true, ParamType.PS_lbl, memWords = 2);

        override fun getDetectionName(): String = this.id

        open fun execute(arch: ArchRV64, paramMap: Map<MaskLabel, Variable.Value.Bin>, tracker: Memory.AccessTracker) {
            arch.console.log("> $id {...}")
        }

        fun executeMeasured(arch: ArchRV64, paramMap: Map<MaskLabel, Variable.Value.Bin>, tracker: Memory.AccessTracker) {
            val time = measureTime {
                execute(arch, paramMap, tracker)
            }
            nativeLog("Executed: ${this.id} in ${time.inWholeMicroseconds}μs")
        }

    }


}