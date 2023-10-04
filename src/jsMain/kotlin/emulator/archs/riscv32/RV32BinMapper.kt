package emulator.archs.riscv32

import emulator.kit.Settings
import emulator.archs.riscv32.RV32Syntax.R_INSTR.InstrType.*
import emulator.kit.types.Variable
import debug.DebugTools


/**
 *  Converts instructions with its parameters between Text and binary format.
 */
class RV32BinMapper {
    var labelAddrMap = mapOf<RV32Syntax.E_LABEL, String>()
    fun setLabelLinks(labelAddrMap: Map<RV32Syntax.E_LABEL, String>) {
        this.labelAddrMap = labelAddrMap
    }
    fun getBinaryFromInstrDef(instrDef: RV32Syntax.R_INSTR, instrStartAddress: Variable.Value.Hex): Array<Variable.Value.Bin> {
        val binArray = mutableListOf<Variable.Value.Bin>()
        val values = instrDef.paramcoll?.getValues()
        val labels = mutableListOf<RV32Syntax.E_LABEL>()
        instrDef.paramcoll?.getILabels()?.forEach { labels.add(it.label) }
        instrDef.paramcoll?.getULabels()?.forEach { labels.add(it.label) }
        instrDef.paramcoll?.getJLabels()?.forEach { labels.add(it.label) }

        if (DebugTools.RV32_showBinMapperInfo) {
            console.log("BinMapper.getBinaryFromInstrDef(): \t${instrDef.instrType.id} -> values: ${values?.joinToString(",") { it.toHex().getRawHexStr() }} labels: ${labels.joinToString(",") { it.wholeName }}")
        }

        if (labels.isNotEmpty()) {
            for (label in labels) {
                val linkedAddress = labelAddrMap.get(label)
                if (linkedAddress == null) {
                    console.warn("BinMapper.getBinaryFromInstrDef(): missing label address entry for [${label.wholeName}]!")
                }
            }
        }

        try {
            val instrDefType = instrDef.instrType
            when (instrDefType) {
                LUI, AUIPC -> {
                    values?.let {

                        val imm20 = Variable.Value.Bin(values[1].getRawBinaryStr().substring(0, 20), Variable.Size.Bit20())
                        val opCode = instrDef.instrType.opCode?.getOpCode(mapOf(MaskLabel.RD to values[0], MaskLabel.IMM20 to imm20))
                        console.log("imm20: $imm20, values[1]: ${values[1]}")
                        opCode?.let {
                            binArray.add(opCode)
                        }
                    }
                }

                JAL -> {
                    values?.let {
                        val imm20toWork = values[1].getResized(Variable.Size.Bit20()).getRawBinaryStr()

                        /**
                         *      RV32IDOC Index   20 19 18 17 16 15 14 13 12 11 10  9  8  7  6  5  4  3  2  1
                         *        String Index    0  1  2  3  4  5  6  7  8  9 10 11 12 13 14 15 16 17 18 19
                         */

                        val imm20 = Variable.Value.Bin(imm20toWork[0].toString() + imm20toWork.substring(10) + imm20toWork[9] + imm20toWork.substring(1, 9), Variable.Size.Bit20())

                        val opCode = instrDef.instrType.opCode?.getOpCode(mapOf(MaskLabel.RD to values[0], MaskLabel.IMM20 to imm20))
                        opCode?.let {
                            binArray.add(opCode)
                        }
                    }
                }

                JALR -> {
                    values?.let {
                        val opCode = instrDef.instrType.opCode?.getOpCode(mapOf(MaskLabel.RD to values[0], MaskLabel.IMM12 to values[1], MaskLabel.RS1 to values[2]))
                        opCode?.let {
                            binArray.add(opCode)
                        }
                    }
                }

                EBREAK, ECALL -> {
                    values?.let {
                        val opCode = instrDef.instrType.opCode?.getOpCode(mapOf())
                        opCode?.let {
                            binArray.add(opCode)
                        }
                    }
                }

                BEQ, BNE, BLT, BGE, BLTU, BGEU -> {
                    values?.let {
                        val imm12 = values[2].getResized(Variable.Size.Bit12()).getRawBinaryStr()
                        val imm5 = Variable.Value.Bin(imm12.substring(8) + imm12[1], Variable.Size.Bit5())
                        val imm7 = Variable.Value.Bin(imm12[0] + imm12.substring(2, 8), Variable.Size.Bit7())

                        val opCode = instrDef.instrType.opCode?.getOpCode(mapOf(MaskLabel.RS1 to values[0], MaskLabel.RS2 to values[1], MaskLabel.IMM5 to imm5, MaskLabel.IMM7 to imm7))
                        opCode?.let {
                            binArray.add(opCode)
                        }
                    }
                }

                BEQ1, BNE1, BLT1, BGE1, BLTU1, BGEU1 -> {
                    if (values != null && labels.isNotEmpty()) {
                        val lblAddr = labelAddrMap.get(labels.first())
                        if (lblAddr != null) {
                            val labelAddr = Variable.Value.Bin(lblAddr, Variable.Size.Bit32())
                            val imm12offset = (labelAddr - instrStartAddress).toBin().getResized(Variable.Size.Bit12()).shr(1).getRawBinaryStr()
                            val imm5 = Variable.Value.Bin(imm12offset.substring(8) + imm12offset[1], Variable.Size.Bit5())
                            val imm7 = Variable.Value.Bin(imm12offset[0] + imm12offset.substring(2, 8), Variable.Size.Bit7())

                            val opCode = instrDefType.relative?.opCode?.getOpCode(mapOf(MaskLabel.RS1 to values[0], MaskLabel.RS2 to values[1], MaskLabel.IMM5 to imm5, MaskLabel.IMM7 to imm7))
                            opCode?.let {
                                binArray.add(opCode)
                            }
                        }
                    }
                }

                LB, LH, LW, LBU, LHU -> {
                    values?.let {
                        val opCode = instrDef.instrType.opCode?.getOpCode(mapOf(MaskLabel.RD to values[0], MaskLabel.IMM12 to values[1], MaskLabel.RS1 to values[2]))
                        opCode?.let {
                            binArray.add(opCode)
                        }
                    }
                }

                SB, SH, SW -> {
                    if (values != null && values.size == 3) {
                        val imm12 = values[1].getRawBinaryStr()
                        val imm5 = Variable.Value.Bin(imm12.substring(imm12.length - 5))
                        val imm7 = Variable.Value.Bin(imm12.substring(imm12.length - 12, imm12.length - 5))

                        val opCode = instrDef.instrType.opCode?.getOpCode(mapOf(MaskLabel.RS2 to values[0], MaskLabel.IMM7 to imm7, MaskLabel.IMM5 to imm5, MaskLabel.RS1 to values[2]))
                        opCode?.let {
                            binArray.add(opCode)
                        }
                    }
                }

                ADDI, SLTI, SLTIU, XORI, ORI, ANDI -> {
                    values?.let {
                        val opCode = instrDef.instrType.opCode?.getOpCode(mapOf(MaskLabel.RD to values[0], MaskLabel.RS1 to values[1], MaskLabel.IMM12 to values[2]))
                        opCode?.let {
                            binArray.add(opCode)
                        }
                    }
                }

                SLLI, SRLI, SRAI -> {
                    values?.let {
                        val opCode = instrDef.instrType.opCode?.getOpCode(mapOf(MaskLabel.RD to values[0], MaskLabel.RS1 to values[1], MaskLabel.SHAMT to values[2]))
                        opCode?.let {
                            binArray.add(opCode)
                        }
                    }
                }

                ADD, SUB, SLL, SLT, SLTU, XOR, SRL, SRA, OR, AND -> {
                    values?.let {
                        val opCode = instrDef.instrType.opCode?.getOpCode(mapOf(MaskLabel.RD to values[0], MaskLabel.RS1 to values[1], MaskLabel.RS2 to values[2]))
                        opCode?.let {
                            binArray.add(opCode)
                        }
                    }
                }

                Li -> {
                    values?.let {
                        val regBin = values[0]
                        val immediate = values[1]
                        val imm32 = immediate.getUResized(Variable.Size.Bit32())

                        val hi20 = imm32.getRawBinaryStr().substring(0, 20)
                        val low12 = imm32.getRawBinaryStr().substring(20)

                        val imm12 = Variable.Value.Bin(low12, Variable.Size.Bit12())

                        val imm20temp = (Variable.Value.Bin(hi20, Variable.Size.Bit20())).toBin() // more performant
                        val imm20 = if (imm12.toDec().isNegative()) {
                            (imm20temp + Variable.Value.Bin("1")).toBin()
                        } else {
                            imm20temp
                        }

                        val luiOpCode = LUI.opCode?.getOpCode(mapOf(MaskLabel.RD to regBin, MaskLabel.IMM20 to imm20))
                        val addiOpCode = ADDI.opCode?.getOpCode(mapOf(MaskLabel.RD to regBin, MaskLabel.RS1 to regBin, MaskLabel.IMM12 to imm12))

                        if (luiOpCode != null && addiOpCode != null) {
                            binArray.add(luiOpCode)
                            binArray.add(addiOpCode)
                        }

                    }
                }

                La -> {
                    if (values != null && labels.isNotEmpty()) {
                        val regBin = values[0]
                        val address = labelAddrMap.get(labels.first())
                        if (address != null) {
                            val imm32 = Variable.Value.Bin(address)
                            val hi20 = imm32.getRawBinaryStr().substring(0, 20)
                            val low12 = imm32.getRawBinaryStr().substring(20)

                            val imm12 = Variable.Value.Bin(low12, Variable.Size.Bit12())

                            val imm20temp = (Variable.Value.Bin(hi20, Variable.Size.Bit20())).toBin() // more performant
                            val imm20 = if (imm12.toDec().isNegative()) {
                                (imm20temp + Variable.Value.Bin("1")).toBin()
                            } else {
                                imm20temp
                            }

                            val luiOpCode = LUI.opCode?.getOpCode(mapOf(MaskLabel.RD to regBin, MaskLabel.IMM20 to imm20))
                            val addiOpCode = ADDI.opCode?.getOpCode(mapOf(MaskLabel.RD to regBin, MaskLabel.RS1 to regBin, MaskLabel.IMM12 to imm12))

                            if (luiOpCode != null && addiOpCode != null) {
                                binArray.add(luiOpCode)
                                binArray.add(addiOpCode)
                            }
                        }
                    }
                }

                JAL1 -> {
                    if (!values.isNullOrEmpty() && labels.isNotEmpty()) {
                        val lblAddr = labelAddrMap.get(labels.first())
                        if (lblAddr != null) {
                            val rd = values[0]
                            val imm20toWork = ((Variable.Value.Bin(lblAddr, Variable.Size.Bit32()) - instrStartAddress).toBin() shr 1).getResized(Variable.Size.Bit20()).getRawBinaryStr()

                            /**
                             *      RV32IDOC Index   20 19 18 17 16 15 14 13 12 11 10  9  8  7  6  5  4  3  2  1
                             *        String Index    0  1  2  3  4  5  6  7  8  9 10 11 12 13 14 15 16 17 18 19
                             */
                            val imm20 = Variable.Value.Bin(imm20toWork[0].toString() + imm20toWork.substring(10) + imm20toWork[9] + imm20toWork.substring(1, 9), Variable.Size.Bit20())

                            val jalOpCode = JAL.opCode?.getOpCode(mapOf(MaskLabel.RD to rd, MaskLabel.IMM20 to imm20))

                            if (jalOpCode != null) {
                                binArray.add(jalOpCode)
                            }
                        }
                    }
                }

                JAL2 -> {
                    if (labels.isNotEmpty()) {
                        val lblAddr = labelAddrMap.get(labels.first())
                        if (lblAddr != null) {
                            val rd = Variable.Value.Bin("1", Variable.Size.Bit5())
                            val imm20toWork = ((Variable.Value.Bin(lblAddr, Variable.Size.Bit32()) - instrStartAddress).toBin() shr 1).getResized(Variable.Size.Bit20()).getRawBinaryStr()

                            /**
                             *      RV32IDOC Index   20 19 18 17 16 15 14 13 12 11 10  9  8  7  6  5  4  3  2  1
                             *        String Index    0  1  2  3  4  5  6  7  8  9 10 11 12 13 14 15 16 17 18 19
                             */
                            val imm20 = Variable.Value.Bin(imm20toWork[0].toString() + imm20toWork.substring(10) + imm20toWork[9] + imm20toWork.substring(1, 9), Variable.Size.Bit20())

                            val jalOpCode = JAL.opCode?.getOpCode(mapOf(MaskLabel.RD to rd, MaskLabel.IMM20 to imm20))

                            if (jalOpCode != null) {
                                binArray.add(jalOpCode)
                            }
                        }
                    }
                }

                J -> {
                    if (labels.isNotEmpty()) {
                        val lblAddr = labelAddrMap.get(labels.first())
                        if (lblAddr != null) {
                            val rd = Variable.Value.Bin("0", Variable.Size.Bit5())
                            val imm20toWork = ((Variable.Value.Bin(lblAddr, Variable.Size.Bit32()) - instrStartAddress).toBin() shr 1).getResized(Variable.Size.Bit20()).getRawBinaryStr()

                            /**
                             *      RV32IDOC Index   20 19 18 17 16 15 14 13 12 11 10  9  8  7  6  5  4  3  2  1
                             *        String Index    0  1  2  3  4  5  6  7  8  9 10 11 12 13 14 15 16 17 18 19
                             */
                            val imm20 = Variable.Value.Bin(imm20toWork[0].toString() + imm20toWork.substring(10) + imm20toWork[9] + imm20toWork.substring(1, 9), Variable.Size.Bit20())

                            val jalOpCode = JAL.opCode?.getOpCode(mapOf(MaskLabel.RD to rd, MaskLabel.IMM20 to imm20))

                            if (jalOpCode != null) {
                                binArray.add(jalOpCode)
                            }
                        }
                    }
                }

                Jr -> {
                    if (!values.isNullOrEmpty()) {
                        val rs1 = values[0]
                        val x0 = Variable.Value.Bin("0", Variable.Size.Bit5())
                        val zero = Variable.Value.Bin("0", Variable.Size.Bit12())

                        val jalrOpCode = JALR.opCode?.getOpCode(mapOf(MaskLabel.RD to x0, MaskLabel.RS1 to rs1, MaskLabel.IMM12 to zero))

                        if (jalrOpCode != null) {
                            binArray.add(jalrOpCode)
                        }
                    }
                }

                JALR1 -> {
                    if (!values.isNullOrEmpty()) {
                        val rs1 = values[0]
                        val x1 = Variable.Value.Bin("1", Variable.Size.Bit5())
                        val zero = Variable.Value.Bin("0", Variable.Size.Bit12())

                        val jalrOpCode = JALR.opCode?.getOpCode(mapOf(MaskLabel.RD to x1, MaskLabel.RS1 to rs1, MaskLabel.IMM12 to zero))

                        if (jalrOpCode != null) {
                            binArray.add(jalrOpCode)
                        }
                    }
                }

                Ret -> {
                    val zero = Variable.Value.Bin("0", Variable.Size.Bit5())
                    val ra = Variable.Value.Bin("1", Variable.Size.Bit5())
                    val imm12 = Variable.Value.Bin("0", Variable.Size.Bit12())

                    val jalrOpCode = JALR.opCode?.getOpCode(mapOf(MaskLabel.RD to zero, MaskLabel.IMM12 to imm12, MaskLabel.RS1 to ra))

                    if (jalrOpCode != null) {
                        binArray.add(jalrOpCode)
                    }
                }

                Call -> {
                    if (labels.isNotEmpty()) {
                        val lblAddr = labelAddrMap.get(labels.first())
                        if (lblAddr != null) {
                            val x1 = Variable.Value.Bin("1", Variable.Size.Bit5())
                            val imm32 = (Variable.Value.Bin(lblAddr, Variable.Size.Bit32()) - instrStartAddress).toBin().getRawBinaryStr()
                            val auipcOff = (Variable.Value.Bin(imm32.substring(0, 20), Variable.Size.Bit20()) + Variable.Value.Bin(imm32[20].toString(), Variable.Size.Bit1())).toBin()
                            val jalrOff = Variable.Value.Bin(imm32.substring(20), Variable.Size.Bit12())

                            val auipcOpCode = AUIPC.opCode?.getOpCode(mapOf(MaskLabel.RD to x1, MaskLabel.IMM20 to auipcOff))
                            val jalrOpCode = JALR.opCode?.getOpCode(mapOf(MaskLabel.RD to x1, MaskLabel.IMM12 to jalrOff, MaskLabel.RS1 to x1))

                            if (auipcOpCode != null && jalrOpCode != null) {
                                binArray.add(auipcOpCode)
                                binArray.add(jalrOpCode)
                            }
                        }
                    }
                }

                Tail -> {
                    if (labels.isNotEmpty()) {
                        val lblAddr = labelAddrMap.get(labels.first())
                        if (lblAddr != null) {
                            val x0 = Variable.Value.Bin("0", Variable.Size.Bit5())
                            val x6 = Variable.Value.Hex("6", Variable.Size.Bit5()).toBin()
                            val imm32 = (Variable.Value.Bin(lblAddr, Variable.Size.Bit32()) - instrStartAddress).toBin().getRawBinaryStr()
                            val auipcOff = (Variable.Value.Bin(imm32.substring(0, 20), Variable.Size.Bit20()) + Variable.Value.Bin(imm32[20].toString(), Variable.Size.Bit1())).toBin()
                            val jalrOff = Variable.Value.Bin(imm32.substring(20), Variable.Size.Bit12())

                            val auipcOpCode = AUIPC.opCode?.getOpCode(mapOf(MaskLabel.RD to x6, MaskLabel.IMM20 to auipcOff))
                            val jalrOpCode = JALR.opCode?.getOpCode(mapOf(MaskLabel.RD to x0, MaskLabel.IMM12 to jalrOff, MaskLabel.RS1 to x6))

                            if (auipcOpCode != null && jalrOpCode != null) {
                                binArray.add(auipcOpCode)
                                binArray.add(jalrOpCode)
                            }
                        }
                    }
                }

                Mv -> {
                    values?.let {
                        val rd = values[0]
                        val rs1 = values[1]
                        val zero = Variable.Value.Bin("0", Variable.Size.Bit12())

                        val addiOpCode = ADDI.opCode?.getOpCode(mapOf(MaskLabel.RD to rd, MaskLabel.RS1 to rs1, MaskLabel.IMM12 to zero))

                        if (addiOpCode != null) {
                            binArray.add(addiOpCode)
                        }
                    }
                }

                Nop -> {
                    val zero = Variable.Value.Bin("0", Variable.Size.Bit5())
                    val imm12 = Variable.Value.Bin("0", Variable.Size.Bit12())
                    val addiOpCode = ADDI.opCode?.getOpCode(mapOf(MaskLabel.RD to zero, MaskLabel.RS1 to zero, MaskLabel.IMM12 to imm12))

                    if (addiOpCode != null) {
                        binArray.add(addiOpCode)
                    }
                }

                Not -> {
                    values?.let {
                        val rd = values[0]
                        val rs1 = values[1]

                        val xoriOpCode = XORI.opCode?.getOpCode(mapOf(MaskLabel.RD to rd, MaskLabel.RS1 to rs1, MaskLabel.IMM12 to Variable.Value.Bin("1".repeat(12), Variable.Size.Bit12())))

                        if (xoriOpCode != null) {
                            binArray.add(xoriOpCode)
                        }
                    }
                }

                Neg -> {
                    values?.let {
                        val rd = values[0]
                        val rs1 = Variable.Value.Bin("0", Variable.Size.Bit5())
                        val rs2 = values[1]

                        val subOpCode = SUB.opCode?.getOpCode(mapOf(MaskLabel.RD to rd, MaskLabel.RS1 to rs1, MaskLabel.RS2 to rs2))

                        if (subOpCode != null) {
                            binArray.add(subOpCode)
                        }
                    }
                }

                Seqz -> {
                    values?.let {
                        val rd = values[0]
                        val rs1 = values[1]
                        val imm12 = Variable.Value.Bin("1", Variable.Size.Bit12())

                        val sltiuOpCode = SLTIU.opCode?.getOpCode(mapOf(MaskLabel.RD to rd, MaskLabel.RS1 to rs1, MaskLabel.IMM12 to imm12))

                        if (sltiuOpCode != null) {
                            binArray.add(sltiuOpCode)
                        }
                    }
                }

                Snez -> {
                    values?.let {
                        val rd = values[0]
                        val rs1 = Variable.Value.Bin("0", Variable.Size.Bit5())
                        val rs2 = values[1]

                        val sltuOpCode = SLTU.opCode?.getOpCode(mapOf(MaskLabel.RD to rd, MaskLabel.RS1 to rs1, MaskLabel.RS2 to rs2))

                        if (sltuOpCode != null) {
                            binArray.add(sltuOpCode)
                        }
                    }
                }

                Sltz -> {
                    values?.let {
                        val rd = values[0]
                        val rs1 = values[1]
                        val zero = Variable.Value.Bin("0", Variable.Size.Bit12())

                        val sltOpCode = SLT.opCode?.getOpCode(mapOf(MaskLabel.RD to rd, MaskLabel.RS1 to rs1, MaskLabel.RS2 to zero))

                        if (sltOpCode != null) {
                            binArray.add(sltOpCode)
                        }
                    }
                }

                Sgtz -> {
                    values?.let {
                        val rd = values[0]
                        val rs1 = Variable.Value.Bin("0", Variable.Size.Bit5())
                        val rs2 = values[1]

                        val sltOpCode = SLT.opCode?.getOpCode(mapOf(MaskLabel.RD to rd, MaskLabel.RS1 to rs1, MaskLabel.RS2 to rs2))

                        if (sltOpCode != null) {
                            binArray.add(sltOpCode)
                        }
                    }
                }

                Beqz -> {
                    if (!values.isNullOrEmpty() && labels.isNotEmpty()) {
                        val lblAddr = labelAddrMap.get(labels.first())
                        if (lblAddr != null) {
                            val rs1 = values[0]
                            val x0 = Variable.Value.Bin("0", Variable.Size.Bit5())
                            val labelAddr = Variable.Value.Bin(lblAddr, Variable.Size.Bit32())
                            val imm12 = (labelAddr - instrStartAddress).toBin().getResized(Variable.Size.Bit12()).shr(1).getRawBinaryStr()
                            val imm5 = Variable.Value.Bin(imm12.substring(8) + imm12[1], Variable.Size.Bit5())
                            val imm7 = Variable.Value.Bin(imm12[0] + imm12.substring(2, 8), Variable.Size.Bit7())
                            val beqOpCode = BEQ.opCode?.getOpCode(mapOf(MaskLabel.RS1 to rs1, MaskLabel.RS2 to x0, MaskLabel.IMM7 to imm7, MaskLabel.IMM5 to imm5))

                            if (beqOpCode != null) {
                                binArray.add(beqOpCode)
                            }
                        }
                    }
                }

                Bnez -> {
                    if (!values.isNullOrEmpty() && labels.isNotEmpty()) {
                        val lblAddr = labelAddrMap.get(labels.first())
                        if (lblAddr != null) {
                            val rs1 = values[0]
                            val x0 = Variable.Value.Bin("0", Variable.Size.Bit5())
                            val labelAddr = Variable.Value.Bin(lblAddr, Variable.Size.Bit32())
                            val imm12 = (labelAddr - instrStartAddress).toBin().getResized(Variable.Size.Bit12()).shr(1).getRawBinaryStr()
                            val imm5 = Variable.Value.Bin(imm12.substring(8) + imm12[1], Variable.Size.Bit5())
                            val imm7 = Variable.Value.Bin(imm12[0] + imm12.substring(2, 8), Variable.Size.Bit7())
                            val bneOpCode = BNE.opCode?.getOpCode(mapOf(MaskLabel.RS1 to rs1, MaskLabel.RS2 to x0, MaskLabel.IMM7 to imm7, MaskLabel.IMM5 to imm5))

                            if (bneOpCode != null) {
                                binArray.add(bneOpCode)
                            }
                        }
                    }
                }

                Blez -> {
                    if (!values.isNullOrEmpty() && labels.isNotEmpty()) {
                        val lblAddr = labelAddrMap.get(labels.first())
                        if (lblAddr != null) {
                            val rs1 = values[0]
                            val x0 = Variable.Value.Bin("0", Variable.Size.Bit5())
                            val labelAddr = Variable.Value.Bin(lblAddr, Variable.Size.Bit32())
                            val imm12 = (labelAddr - instrStartAddress).toBin().getResized(Variable.Size.Bit12()).shr(1).getRawBinaryStr()
                            val imm5 = Variable.Value.Bin(imm12.substring(8) + imm12[1], Variable.Size.Bit5())
                            val imm7 = Variable.Value.Bin(imm12[0] + imm12.substring(2, 8), Variable.Size.Bit7())
                            val bgeOpCode = BGE.opCode?.getOpCode(mapOf(MaskLabel.RS1 to x0, MaskLabel.RS2 to rs1, MaskLabel.IMM7 to imm7, MaskLabel.IMM5 to imm5))

                            if (bgeOpCode != null) {
                                binArray.add(bgeOpCode)
                            }
                        }
                    }
                }

                Bgez -> {
                    if (!values.isNullOrEmpty() && labels.isNotEmpty()) {
                        val lblAddr = labelAddrMap.get(labels.first())
                        if (lblAddr != null) {
                            val rs1 = values[0]
                            val x0 = Variable.Value.Bin("0", Variable.Size.Bit5())
                            val labelAddr = Variable.Value.Bin(lblAddr, Variable.Size.Bit32())
                            val imm12 = (labelAddr - instrStartAddress).toBin().getResized(Variable.Size.Bit12()).shr(1).getRawBinaryStr()
                            val imm5 = Variable.Value.Bin(imm12.substring(8) + imm12[1], Variable.Size.Bit5())
                            val imm7 = Variable.Value.Bin(imm12[0] + imm12.substring(2, 8), Variable.Size.Bit7())
                            val bgeOpCode = BGE.opCode?.getOpCode(mapOf(MaskLabel.RS1 to rs1, MaskLabel.RS2 to x0, MaskLabel.IMM7 to imm7, MaskLabel.IMM5 to imm5))

                            if (bgeOpCode != null) {
                                binArray.add(bgeOpCode)
                            }
                        }
                    }
                }

                Bltz -> {
                    if (!values.isNullOrEmpty() && labels.isNotEmpty()) {
                        val lblAddr = labelAddrMap.get(labels.first())
                        if (lblAddr != null) {
                            val rs1 = values[0]
                            val x0 = Variable.Value.Bin("0", Variable.Size.Bit5())
                            val labelAddr = Variable.Value.Bin(lblAddr, Variable.Size.Bit32())
                            val imm12 = (labelAddr - instrStartAddress).toBin().getResized(Variable.Size.Bit12()).shr(1).getRawBinaryStr()
                            val imm5 = Variable.Value.Bin(imm12.substring(8) + imm12[1], Variable.Size.Bit5())
                            val imm7 = Variable.Value.Bin(imm12[0] + imm12.substring(2, 8), Variable.Size.Bit7())
                            val bltOpCode = BLT.opCode?.getOpCode(mapOf(MaskLabel.RS1 to rs1, MaskLabel.RS2 to x0, MaskLabel.IMM7 to imm7, MaskLabel.IMM5 to imm5))

                            if (bltOpCode != null) {
                                binArray.add(bltOpCode)
                            }
                        }
                    }
                }

                BGTZ -> {
                    if (!values.isNullOrEmpty() && labels.isNotEmpty()) {
                        val lblAddr = labelAddrMap.get(labels.first())
                        if (lblAddr != null) {
                            val rs1 = values[0]
                            val x0 = Variable.Value.Bin("0", Variable.Size.Bit5())
                            val labelAddr = Variable.Value.Bin(lblAddr, Variable.Size.Bit32())
                            val imm12 = (labelAddr - instrStartAddress).toBin().getResized(Variable.Size.Bit12()).shr(1).getRawBinaryStr()
                            val imm5 = Variable.Value.Bin(imm12.substring(8) + imm12[1], Variable.Size.Bit5())
                            val imm7 = Variable.Value.Bin(imm12[0] + imm12.substring(2, 8), Variable.Size.Bit7())
                            val bltOpCode = BLT.opCode?.getOpCode(mapOf(MaskLabel.RS1 to x0, MaskLabel.RS2 to rs1, MaskLabel.IMM7 to imm7, MaskLabel.IMM5 to imm5))

                            if (bltOpCode != null) {
                                binArray.add(bltOpCode)
                            }
                        }
                    }
                }

                Bgt -> {
                    if (!values.isNullOrEmpty() && labels.isNotEmpty()) {
                        val lblAddr = labelAddrMap.get(labels.first())
                        if (lblAddr != null) {
                            val rs1 = values[0]
                            val rs2 = values[1]

                            val labelAddr = Variable.Value.Bin(lblAddr, Variable.Size.Bit32())
                            val imm12 = (labelAddr - instrStartAddress).toBin().getResized(Variable.Size.Bit12()).shr(1).getRawBinaryStr()
                            val imm5 = Variable.Value.Bin(imm12.substring(8) + imm12[1], Variable.Size.Bit5())
                            val imm7 = Variable.Value.Bin(imm12[0] + imm12.substring(2, 8), Variable.Size.Bit7())

                            val bltOpCode = BLT.opCode?.getOpCode(mapOf(MaskLabel.RS1 to rs2, MaskLabel.RS2 to rs1, MaskLabel.IMM7 to imm7, MaskLabel.IMM5 to imm5))

                            if (bltOpCode != null) {
                                binArray.add(bltOpCode)
                            }
                        }
                    }
                }

                Ble -> {
                    if (!values.isNullOrEmpty() && labels.isNotEmpty()) {
                        val lblAddr = labelAddrMap.get(labels.first())
                        if (lblAddr != null) {
                            val rs1 = values[0]
                            val rs2 = values[1]

                            val labelAddr = Variable.Value.Bin(lblAddr, Variable.Size.Bit32())
                            val imm12 = (labelAddr - instrStartAddress).toBin().getResized(Variable.Size.Bit12()).shr(1).getRawBinaryStr()
                            val imm5 = Variable.Value.Bin(imm12.substring(8) + imm12[1], Variable.Size.Bit5())
                            val imm7 = Variable.Value.Bin(imm12[0] + imm12.substring(2, 8), Variable.Size.Bit7())

                            val bgeOpCode = BGE.opCode?.getOpCode(mapOf(MaskLabel.RS1 to rs2, MaskLabel.RS2 to rs1, MaskLabel.IMM7 to imm7, MaskLabel.IMM5 to imm5))

                            if (bgeOpCode != null) {
                                binArray.add(bgeOpCode)
                            }
                        }
                    }
                }

                Bgtu -> {
                    if (!values.isNullOrEmpty() && labels.isNotEmpty()) {
                        val lblAddr = labelAddrMap.get(labels.first())
                        if (lblAddr != null) {
                            val rs1 = values[0]
                            val rs2 = values[1]

                            val labelAddr = Variable.Value.Bin(lblAddr, Variable.Size.Bit32())
                            val imm12 = (labelAddr - instrStartAddress).toBin().getResized(Variable.Size.Bit12()).shr(1).getRawBinaryStr()
                            val imm5 = Variable.Value.Bin(imm12.substring(8) + imm12[1], Variable.Size.Bit5())
                            val imm7 = Variable.Value.Bin(imm12[0] + imm12.substring(2, 8), Variable.Size.Bit7())

                            val bltuOpCode = BLTU.opCode?.getOpCode(mapOf(MaskLabel.RS1 to rs2, MaskLabel.RS2 to rs1, MaskLabel.IMM7 to imm7, MaskLabel.IMM5 to imm5))

                            if (bltuOpCode != null) {
                                binArray.add(bltuOpCode)
                            }
                        }
                    }
                }

                Bleu -> {
                    if (!values.isNullOrEmpty() && labels.isNotEmpty()) {
                        val lblAddr = labelAddrMap.get(labels.first())
                        if (lblAddr != null) {
                            val rs1 = values[0]
                            val rs2 = values[1]

                            val labelAddr = Variable.Value.Bin(lblAddr, Variable.Size.Bit32())
                            val imm12 = (labelAddr - instrStartAddress).toBin().getResized(Variable.Size.Bit12()).shr(1).getRawBinaryStr()
                            val imm5 = Variable.Value.Bin(imm12.substring(8) + imm12[1], Variable.Size.Bit5())
                            val imm7 = Variable.Value.Bin(imm12[0] + imm12.substring(2, 8), Variable.Size.Bit7())

                            val bgeuOpCode = BGEU.opCode?.getOpCode(mapOf(MaskLabel.RS1 to rs2, MaskLabel.RS2 to rs1, MaskLabel.IMM7 to imm7, MaskLabel.IMM5 to imm5))

                            if (bgeuOpCode != null) {
                                binArray.add(bgeuOpCode)
                            }
                        }
                    }
                }


            }
        } catch (e: IndexOutOfBoundsException) {
            console.error("IndexOutOfBoundsException: $e")
        }

        if (binArray.isEmpty()) {
            console.error("RISCVBinMapper: values and labels not matching for ${instrDef.instrType.name}!")
        }

        return binArray.toTypedArray()
    }
    fun getInstrFromBinary(bin: Variable.Value.Bin): InstrResult? {
        for (instrType in entries) {
            val checkResult = instrType.opCode?.checkOpCode(bin)
            checkResult?.let {
                if (it.matches) {
                    return InstrResult(instrType, it.binMap)
                }
            }
        }
        return null
    }
    data class InstrResult(val type: RV32Syntax.R_INSTR.InstrType, val binMap: Map<MaskLabel, Variable.Value.Bin> = mapOf())
    class OpCode(val opMask: String, val maskLabels: Array<MaskLabel>) {

        val opMaskList = opMask.removePrefix(Settings.PRESTRING_BINARY).split(" ")
        fun checkOpCode(bin: Variable.Value.Bin): CheckResult {
            if (bin.size != Variable.Size.Bit32()) {
                return CheckResult(false)
            }
            // Check OpCode
            val binaryString = bin.getRawBinaryStr()
            val binaryOpCode = binaryString.substring(binaryString.length - 7)
            val originalOpCode = getMaskString(MaskLabel.OPCODE)
            if (originalOpCode.isNotEmpty()) {
                if (binaryOpCode == originalOpCode) {
                    // check static labels
                    val binMap = mutableMapOf<MaskLabel, Variable.Value.Bin>()
                    if (DebugTools.RV32_showBinMapperInfo) {
                        console.log("BinMapper.OpCode.checkOpCode(): found instr $binaryOpCode")
                    }
                    for (labelID in maskLabels.indices) {
                        val label = maskLabels[labelID]
                        if (label.static) {
                            val substring = getSubString(binaryString, label)
                            if (substring != opMaskList[labelID]) {
                                return CheckResult(false)
                            }
                        }
                    }

                    for (labelID in maskLabels.indices) {
                        val label = maskLabels[labelID]
                        if (!label.static) {
                            val substring = getSubString(binaryString, label)
                            if (label.maxSize != null) {
                                binMap.set(label, Variable.Value.Bin(substring, label.maxSize))
                            }
                        }
                    }

                    return CheckResult(true, binMap)
                } else {
                    return CheckResult(false)
                }
            } else {
                return CheckResult(false)
            }
        }
        fun getOpCode(parameterMap: Map<MaskLabel, Variable.Value.Bin>): Variable.Value.Bin? {
            val opCode = opMaskList.toMutableList()
            var length = 0
            opCode.forEach { length += it.length }
            if (length != Variable.Size.Bit32().bitWidth) {
                console.warn("BinMapper.OpCode: OpMask isn't 32Bit Binary! -> returning null")
                return null
            }
            if (opCode.size != maskLabels.size) {
                console.warn("BinMapper.OpCode: OpMask [$opMask] and Labels [${maskLabels.joinToString { it.name }}] aren't the same size! -> returning null")
                return null
            }

            for (labelID in maskLabels.indices) {
                val maskLabel = maskLabels[labelID]
                if (!maskLabel.static) {
                    val param = parameterMap.get(maskLabel)
                    if (param != null) {
                        val size = maskLabel.maxSize
                        if (size != null) {
                            opCode[labelID] = param.getUResized(size).getRawBinaryStr()
                        } else {
                            console.warn("BinMapper.OpCode.getOpCode(): can't insert ByteValue in OpMask without a maxSize! -> returning null")
                            return null
                        }
                    } else {
                        console.warn("BinMapper.OpCode.getOpCode(): parameter [${maskLabel.name}] not found! -> inserting zeros")
                        val bitWidth = maskLabel.maxSize?.bitWidth
                        bitWidth?.let {
                            opCode[labelID] = "0".repeat(it)
                        }
                    }

                }
            }

            return Variable.Value.Bin(opCode.joinToString("") { it }, Variable.Size.Bit32())
        }
        private fun getSubString(binary: String, maskLabel: MaskLabel): String {
            var startIndex = 0
            for (maskID in opMaskList.indices) {
                val maskString = opMaskList[maskID]
                if (maskLabels[maskID] == maskLabel) {
                    return binary.substring(startIndex, startIndex + maskString.length)
                }
                startIndex += maskString.length
            }
            return ""
        }
        private fun getMaskString(maskLabel: MaskLabel): String {
            for (labelID in maskLabels.indices) {
                val label = maskLabels[labelID]
                if (label == maskLabel) {
                    return opMaskList[labelID]
                }
            }
            return ""
        }
        data class CheckResult(val matches: Boolean, val binMap: Map<MaskLabel, Variable.Value.Bin> = mapOf())
    }
    enum class MaskLabel(val static: Boolean, val maxSize: Variable.Size? = null) {
        OPCODE(true, Variable.Size.Bit7()),
        RD(false, Variable.Size.Bit5()),
        FUNCT3(true, Variable.Size.Bit3()),
        RS1(false, Variable.Size.Bit5()),
        RS2(false, Variable.Size.Bit5()),
        SHAMT(false, Variable.Size.Bit5()),
        FUNCT7(true, Variable.Size.Bit7()),
        IMM5(false, Variable.Size.Bit5()),
        IMM7(false, Variable.Size.Bit7()),
        IMM12(false, Variable.Size.Bit12()),
        IMM20(false, Variable.Size.Bit20()),
        NONE(true)
    }
}