package extendable.archs.riscv

import extendable.ArchConst
import extendable.archs.riscv.RISCVGrammarV1.R_INSTR.InstrType.*
import extendable.components.types.MutVal
import tools.DebugTools

class RISCVBinMapper {

    var labelAddrMap = mapOf<RISCVGrammarV1.E_LABEL, String>()

    fun setLabelLinks(labelAddrMap: Map<RISCVGrammarV1.E_LABEL, String>) {
        this.labelAddrMap = labelAddrMap
    }

    fun getBinaryFromInstrDef(instrDef: RISCVGrammarV1.R_INSTR, instrStartAddress: MutVal.Value.Hex): Array<MutVal.Value.Binary> {
        val binaryArray = mutableListOf<MutVal.Value.Binary>()
        val values = instrDef.paramcoll?.getValues()
        val labels = mutableListOf<RISCVGrammarV1.E_LABEL>()
        instrDef.paramcoll?.getILabels()?.forEach { labels.add(it.label) }
        instrDef.paramcoll?.getULabels()?.forEach { labels.add(it.label) }
        instrDef.paramcoll?.getJLabels()?.forEach { labels.add(it.label) }

        if (DebugTools.RISCV_showBinMapperInfo) {
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
                        val imm20 = MutVal.Value.Binary(values[1].getRawBinaryStr().substring(0, 20), MutVal.Size.Bit20())
                        val opCode = instrDef.instrType.opCode?.getOpCode(mapOf(MaskLabel.RD to values[0], MaskLabel.IMM20 to imm20))
                        opCode?.let {
                            binaryArray.add(opCode)
                        }
                    }
                }

                JAL -> {
                    values?.let {
                        val imm20toWork = values[1].getResized(MutVal.Size.Bit20()).getRawBinaryStr()

                        /**
                         *      RV32IDOC Index   20 19 18 17 16 15 14 13 12 11 10  9  8  7  6  5  4  3  2  1
                         *        String Index    0  1  2  3  4  5  6  7  8  9 10 11 12 13 14 15 16 17 18 19
                         */

                        val imm20 = MutVal.Value.Binary(imm20toWork[0].toString() + imm20toWork.substring(10) + imm20toWork[9] + imm20toWork.substring(1, 9), MutVal.Size.Bit20())

                        val opCode = instrDef.instrType.opCode?.getOpCode(mapOf(MaskLabel.RD to values[0], MaskLabel.IMM20 to imm20))
                        opCode?.let {
                            binaryArray.add(opCode)
                        }
                    }
                }

                JALR -> {
                    values?.let {
                        val opCode = instrDef.instrType.opCode?.getOpCode(mapOf(MaskLabel.RD to values[0], MaskLabel.IMM12 to values[1], MaskLabel.RS1 to values[2]))
                        opCode?.let {
                            binaryArray.add(opCode)
                        }
                    }
                }

                EBREAK, ECALL -> {
                    values?.let {
                        val opCode = instrDef.instrType.opCode?.getOpCode(mapOf())
                        opCode?.let {
                            binaryArray.add(opCode)
                        }
                    }
                }

                BEQ, BNE, BLT, BGE, BLTU, BGEU -> {
                    values?.let {
                        val imm12 = values[2].getResized(MutVal.Size.Bit12()).getRawBinaryStr()
                        val imm5 = MutVal.Value.Binary(imm12.substring(8) + imm12[1], MutVal.Size.Bit5())
                        val imm7 = MutVal.Value.Binary(imm12[0] + imm12.substring(2, 8), MutVal.Size.Bit7())

                        val opCode = instrDef.instrType.opCode?.getOpCode(mapOf(MaskLabel.RS1 to values[0], MaskLabel.RS2 to values[1], MaskLabel.IMM5 to imm5, MaskLabel.IMM7 to imm7))
                        opCode?.let {
                            binaryArray.add(opCode)
                        }
                    }
                }

                BEQ1, BNE1, BLT1, BGE1, BLTU1, BGEU1 -> {
                    if (values != null && labels != null) {
                        val lblAddr = labelAddrMap.get(labels.first())
                        if (lblAddr != null) {
                            val labelAddr = MutVal.Value.Binary(lblAddr, MutVal.Size.Bit32())
                            val imm12offset = (labelAddr - instrStartAddress).toBin().getResized(MutVal.Size.Bit12()).shr(1).getRawBinaryStr()
                            val imm5 = MutVal.Value.Binary(imm12offset.substring(8) + imm12offset[1], MutVal.Size.Bit5())
                            val imm7 = MutVal.Value.Binary(imm12offset[0] + imm12offset.substring(2, 8), MutVal.Size.Bit7())

                            val opCode = instrDefType.relative?.opCode?.getOpCode(mapOf(MaskLabel.RS1 to values[0], MaskLabel.RS2 to values[1], MaskLabel.IMM5 to imm5, MaskLabel.IMM7 to imm7))
                            opCode?.let {
                                binaryArray.add(opCode)
                            }
                        }
                    }
                }

                LB, LH, LW, LBU, LHU -> {
                    values?.let {
                        val opCode = instrDef.instrType.opCode?.getOpCode(mapOf(MaskLabel.RD to values[0], MaskLabel.IMM12 to values[1], MaskLabel.RS1 to values[2]))
                        opCode?.let {
                            binaryArray.add(opCode)
                        }
                    }
                }

                SB, SH, SW -> {
                    if (values != null && values.size == 3) {
                        val imm12 = values[1].getRawBinaryStr()
                        val imm5 = MutVal.Value.Binary(imm12.substring(imm12.length - 5))
                        val imm7 = MutVal.Value.Binary(imm12.substring(imm12.length - 12, imm12.length - 5))

                        val opCode = instrDef.instrType.opCode?.getOpCode(mapOf(MaskLabel.RS2 to values[0], MaskLabel.IMM7 to imm7, MaskLabel.IMM5 to imm5, MaskLabel.RS1 to values[2]))
                        opCode?.let {
                            binaryArray.add(opCode)
                        }
                    }
                }

                ADDI, SLTI, SLTIU, XORI, ORI, ANDI -> {
                    values?.let {
                        val opCode = instrDef.instrType.opCode?.getOpCode(mapOf(MaskLabel.RD to values[0], MaskLabel.RS1 to values[1], MaskLabel.IMM12 to values[2]))
                        opCode?.let {
                            binaryArray.add(opCode)
                        }
                    }
                }

                SLLI, SRLI, SRAI -> {
                    values?.let {
                        val opCode = instrDef.instrType.opCode?.getOpCode(mapOf(MaskLabel.RD to values[0], MaskLabel.RS1 to values[1], MaskLabel.SHAMT to values[2]))
                        opCode?.let {
                            binaryArray.add(opCode)
                        }
                    }
                }

                ADD, SUB, SLL, SLT, SLTU, XOR, SRL, SRA, OR, AND -> {
                    values?.let {
                        val opCode = instrDef.instrType.opCode?.getOpCode(mapOf(MaskLabel.RD to values[0], MaskLabel.RS1 to values[1], MaskLabel.RS2 to values[2]))
                        opCode?.let {
                            binaryArray.add(opCode)
                        }
                    }
                }

                Li -> {
                    values?.let {
                        val regBin = values[0]
                        val immediate = values[1]
                        val imm32 = immediate.getUResized(MutVal.Size.Bit32())

                        val hi20 = imm32.getRawBinaryStr().substring(0, 20)
                        val low12 = imm32.getRawBinaryStr().substring(20)

                        val imm12 = MutVal.Value.Binary(low12, MutVal.Size.Bit12())

                        val imm20temp = (MutVal.Value.Binary(hi20, MutVal.Size.Bit20())).toBin() // more performant
                        val imm20 = if (imm12.toDec().isNegative()) {
                            (imm20temp + MutVal.Value.Binary("1")).toBin()
                        } else {
                            imm20temp
                        }

                        val luiOpCode = LUI.opCode?.getOpCode(mapOf(MaskLabel.RD to regBin, MaskLabel.IMM20 to imm20))
                        val addiOpCode = ADDI.opCode?.getOpCode(mapOf(MaskLabel.RD to regBin, MaskLabel.RS1 to regBin, MaskLabel.IMM12 to imm12))

                        if (luiOpCode != null && addiOpCode != null) {
                            binaryArray.add(luiOpCode)
                            binaryArray.add(addiOpCode)
                        }

                    }
                }

                La -> {
                    if (values != null && labels != null) {
                        val regBin = values[0]
                        val address = labelAddrMap.get(labels.first())
                        if (address != null) {
                            val imm32 = MutVal.Value.Binary(address)
                            val hi20 = imm32.getRawBinaryStr().substring(0, 20)
                            val low12 = imm32.getRawBinaryStr().substring(20)

                            val imm12 = MutVal.Value.Binary(low12, MutVal.Size.Bit12())

                            val imm20temp = (MutVal.Value.Binary(hi20, MutVal.Size.Bit20())).toBin() // more performant
                            val imm20 = if (imm12.toDec().isNegative()) {
                                (imm20temp + MutVal.Value.Binary("1")).toBin()
                            } else {
                                imm20temp
                            }

                            val luiOpCode = LUI.opCode?.getOpCode(mapOf(MaskLabel.RD to regBin, MaskLabel.IMM20 to imm20))
                            val addiOpCode = ADDI.opCode?.getOpCode(mapOf(MaskLabel.RD to regBin, MaskLabel.RS1 to regBin, MaskLabel.IMM12 to imm12))

                            if (luiOpCode != null && addiOpCode != null) {
                                binaryArray.add(luiOpCode)
                                binaryArray.add(addiOpCode)
                            }
                        }
                    }
                }

                JAL1 -> {
                    if (!values.isNullOrEmpty() && !labels.isNullOrEmpty()) {
                        val lblAddr = labelAddrMap.get(labels.first())
                        if (lblAddr != null) {
                            val rd = values[0]
                            val imm20toWork = ((MutVal.Value.Binary(lblAddr, MutVal.Size.Bit32()) - instrStartAddress).toBin() shr 1).getResized(MutVal.Size.Bit20()).getRawBinaryStr()

                            /**
                             *      RV32IDOC Index   20 19 18 17 16 15 14 13 12 11 10  9  8  7  6  5  4  3  2  1
                             *        String Index    0  1  2  3  4  5  6  7  8  9 10 11 12 13 14 15 16 17 18 19
                             */
                            val imm20 = MutVal.Value.Binary(imm20toWork[0].toString() + imm20toWork.substring(10) + imm20toWork[9] + imm20toWork.substring(1, 9), MutVal.Size.Bit20())

                            val jalOpCode = JAL.opCode?.getOpCode(mapOf(MaskLabel.RD to rd, MaskLabel.IMM20 to imm20))

                            if (jalOpCode != null) {
                                binaryArray.add(jalOpCode)
                            }
                        }
                    }
                }

                JAL2 -> {
                    if (!labels.isNullOrEmpty()) {
                        val lblAddr = labelAddrMap.get(labels.first())
                        if (lblAddr != null) {
                            val rd = MutVal.Value.Binary("1", MutVal.Size.Bit5())
                            val imm20toWork = ((MutVal.Value.Binary(lblAddr, MutVal.Size.Bit32()) - instrStartAddress).toBin() shr 1).getResized(MutVal.Size.Bit20()).getRawBinaryStr()

                            /**
                             *      RV32IDOC Index   20 19 18 17 16 15 14 13 12 11 10  9  8  7  6  5  4  3  2  1
                             *        String Index    0  1  2  3  4  5  6  7  8  9 10 11 12 13 14 15 16 17 18 19
                             */
                            val imm20 = MutVal.Value.Binary(imm20toWork[0].toString() + imm20toWork.substring(10) + imm20toWork[9] + imm20toWork.substring(1, 9), MutVal.Size.Bit20())

                            val jalOpCode = JAL.opCode?.getOpCode(mapOf(MaskLabel.RD to rd, MaskLabel.IMM20 to imm20))

                            if (jalOpCode != null) {
                                binaryArray.add(jalOpCode)
                            }
                        }
                    }
                }

                J -> {
                    if (!labels.isNullOrEmpty()) {
                        val lblAddr = labelAddrMap.get(labels.first())
                        if (lblAddr != null) {
                            val rd = MutVal.Value.Binary("0", MutVal.Size.Bit5())
                            val imm20toWork = ((MutVal.Value.Binary(lblAddr, MutVal.Size.Bit32()) - instrStartAddress).toBin() shr 1).getResized(MutVal.Size.Bit20()).getRawBinaryStr()

                            /**
                             *      RV32IDOC Index   20 19 18 17 16 15 14 13 12 11 10  9  8  7  6  5  4  3  2  1
                             *        String Index    0  1  2  3  4  5  6  7  8  9 10 11 12 13 14 15 16 17 18 19
                             */
                            val imm20 = MutVal.Value.Binary(imm20toWork[0].toString() + imm20toWork.substring(10) + imm20toWork[9] + imm20toWork.substring(1, 9), MutVal.Size.Bit20())

                            val jalOpCode = JAL.opCode?.getOpCode(mapOf(MaskLabel.RD to rd, MaskLabel.IMM20 to imm20))

                            if (jalOpCode != null) {
                                binaryArray.add(jalOpCode)
                            }
                        }
                    }
                }

                Jr -> {
                    if (!values.isNullOrEmpty()) {
                        val rs1 = values[0]
                        val x0 = MutVal.Value.Binary("0", MutVal.Size.Bit5())
                        val zero = MutVal.Value.Binary("0", MutVal.Size.Bit12())

                        val jalrOpCode = JALR.opCode?.getOpCode(mapOf(MaskLabel.RD to x0, MaskLabel.RS1 to rs1, MaskLabel.IMM12 to zero))

                        if (jalrOpCode != null) {
                            binaryArray.add(jalrOpCode)
                        }
                    }
                }

                JALR1 -> {
                    if (!values.isNullOrEmpty()) {
                        val rs1 = values[0]
                        val x1 = MutVal.Value.Binary("1", MutVal.Size.Bit5())
                        val zero = MutVal.Value.Binary("0", MutVal.Size.Bit12())

                        val jalrOpCode = JALR.opCode?.getOpCode(mapOf(MaskLabel.RD to x1, MaskLabel.RS1 to rs1, MaskLabel.IMM12 to zero))

                        if (jalrOpCode != null) {
                            binaryArray.add(jalrOpCode)
                        }
                    }
                }

                Ret -> {
                    val zero = MutVal.Value.Binary("0", MutVal.Size.Bit5())
                    val ra = MutVal.Value.Binary("1", MutVal.Size.Bit5())
                    val imm12 = MutVal.Value.Binary("0", MutVal.Size.Bit12())

                    val jalrOpCode = JALR.opCode?.getOpCode(mapOf(MaskLabel.RD to zero, MaskLabel.IMM12 to imm12, MaskLabel.RS1 to ra))

                    if (jalrOpCode != null) {
                        binaryArray.add(jalrOpCode)
                    }
                }

                Mv -> {
                    values?.let {
                        val rd = values[0]
                        val rs1 = values[1]
                        val zero = MutVal.Value.Binary("0", MutVal.Size.Bit12())

                        val addiOpCode = ADDI.opCode?.getOpCode(mapOf(MaskLabel.RD to rd, MaskLabel.RS1 to rs1, MaskLabel.IMM12 to zero))

                        if (addiOpCode != null) {
                            binaryArray.add(addiOpCode)
                        }
                    }
                }

                Nop -> {
                    val zero = MutVal.Value.Binary("0", MutVal.Size.Bit5())
                    val imm12 = MutVal.Value.Binary("0", MutVal.Size.Bit12())
                    val addiOpCode = ADDI.opCode?.getOpCode(mapOf(MaskLabel.RD to zero, MaskLabel.RS1 to zero, MaskLabel.IMM12 to imm12))

                    if (addiOpCode != null) {
                        binaryArray.add(addiOpCode)
                    }
                }

                Not -> {
                    values?.let {
                        val rd = values[0]
                        val rs1 = values[1]

                        val xoriOpCode = XORI.opCode?.getOpCode(mapOf(MaskLabel.RD to rd, MaskLabel.RS1 to rs1, MaskLabel.IMM12 to MutVal.Value.Binary("1".repeat(12), MutVal.Size.Bit12())))

                        if (xoriOpCode != null) {
                            binaryArray.add(xoriOpCode)
                        }
                    }
                }

                Neg -> {
                    values?.let {
                        val rd = values[0]
                        val rs1 = MutVal.Value.Binary("0", MutVal.Size.Bit5())
                        val rs2 = values[1]

                        val subOpCode = SUB.opCode?.getOpCode(mapOf(MaskLabel.RD to rd, MaskLabel.RS1 to rs1, MaskLabel.RS2 to rs2))

                        if (subOpCode != null) {
                            binaryArray.add(subOpCode)
                        }
                    }
                }

                Seqz -> {
                    values?.let {
                        val rd = values[0]
                        val rs1 = values[1]
                        val imm12 = MutVal.Value.Binary("1", MutVal.Size.Bit12())

                        val sltiuOpCode = SLTIU.opCode?.getOpCode(mapOf(MaskLabel.RD to rd, MaskLabel.RS1 to rs1, MaskLabel.IMM12 to imm12))

                        if (sltiuOpCode != null) {
                            binaryArray.add(sltiuOpCode)
                        }
                    }
                }

                Snez -> {
                    values?.let {
                        val rd = values[0]
                        val rs1 = MutVal.Value.Binary("0", MutVal.Size.Bit5())
                        val rs2 = values[1]

                        val sltuOpCode = SLTU.opCode?.getOpCode(mapOf(MaskLabel.RD to rd, MaskLabel.RS1 to rs1, MaskLabel.RS2 to rs2))

                        if (sltuOpCode != null) {
                            binaryArray.add(sltuOpCode)
                        }
                    }
                }

                Sltz -> {
                    values?.let {
                        val rd = values[0]
                        val rs1 = values[1]
                        val zero = MutVal.Value.Binary("0", MutVal.Size.Bit12())

                        val sltOpCode = SLT.opCode?.getOpCode(mapOf(MaskLabel.RD to rd, MaskLabel.RS1 to rs1, MaskLabel.RS2 to zero))

                        if (sltOpCode != null) {
                            binaryArray.add(sltOpCode)
                        }
                    }
                }

                Sgtz -> {
                    values?.let {
                        val rd = values[0]
                        val rs1 = MutVal.Value.Binary("0", MutVal.Size.Bit5())
                        val rs2 = values[1]

                        val sltOpCode = SLT.opCode?.getOpCode(mapOf(MaskLabel.RD to rd, MaskLabel.RS1 to rs1, MaskLabel.RS2 to rs2))

                        if (sltOpCode != null) {
                            binaryArray.add(sltOpCode)
                        }
                    }
                }

                Beqz -> {
                    if (!values.isNullOrEmpty() && !labels.isNullOrEmpty()) {
                        val lblAddr = labelAddrMap.get(labels.first())
                        if (lblAddr != null) {
                            val rs1 = values[0]
                            val x0 = MutVal.Value.Binary("0", MutVal.Size.Bit5())
                            val labelAddr = MutVal.Value.Binary(lblAddr, MutVal.Size.Bit32())
                            val imm12 = (labelAddr - instrStartAddress).toBin().getResized(MutVal.Size.Bit12()).shr(1).getRawBinaryStr()
                            val imm5 = MutVal.Value.Binary(imm12.substring(8) + imm12[1], MutVal.Size.Bit5())
                            val imm7 = MutVal.Value.Binary(imm12[0] + imm12.substring(2, 8), MutVal.Size.Bit7())
                            val beqOpCode = BEQ.opCode?.getOpCode(mapOf(MaskLabel.RS1 to rs1, MaskLabel.RS2 to x0, MaskLabel.IMM7 to imm7, MaskLabel.IMM5 to imm5))

                            if (beqOpCode != null) {
                                binaryArray.add(beqOpCode)
                            }
                        }
                    }
                }

                Bnez -> {
                    if (!values.isNullOrEmpty() && !labels.isNullOrEmpty()) {
                        val lblAddr = labelAddrMap.get(labels.first())
                        if (lblAddr != null) {
                            val rs1 = values[0]
                            val x0 = MutVal.Value.Binary("0", MutVal.Size.Bit5())
                            val labelAddr = MutVal.Value.Binary(lblAddr, MutVal.Size.Bit32())
                            val imm12 = (labelAddr - instrStartAddress).toBin().getResized(MutVal.Size.Bit12()).shr(1).getRawBinaryStr()
                            val imm5 = MutVal.Value.Binary(imm12.substring(8) + imm12[1], MutVal.Size.Bit5())
                            val imm7 = MutVal.Value.Binary(imm12[0] + imm12.substring(2, 8), MutVal.Size.Bit7())
                            val bneOpCode = BNE.opCode?.getOpCode(mapOf(MaskLabel.RS1 to rs1, MaskLabel.RS2 to x0, MaskLabel.IMM7 to imm7, MaskLabel.IMM5 to imm5))

                            if (bneOpCode != null) {
                                binaryArray.add(bneOpCode)
                            }
                        }
                    }
                }

                Blez -> {
                    if (!values.isNullOrEmpty() && !labels.isNullOrEmpty()) {
                        val lblAddr = labelAddrMap.get(labels.first())
                        if (lblAddr != null) {
                            val rs1 = values[0]
                            val x0 = MutVal.Value.Binary("0", MutVal.Size.Bit5())
                            val labelAddr = MutVal.Value.Binary(lblAddr, MutVal.Size.Bit32())
                            val imm12 = (labelAddr - instrStartAddress).toBin().getResized(MutVal.Size.Bit12()).shr(1).getRawBinaryStr()
                            val imm5 = MutVal.Value.Binary(imm12.substring(8) + imm12[1], MutVal.Size.Bit5())
                            val imm7 = MutVal.Value.Binary(imm12[0] + imm12.substring(2, 8), MutVal.Size.Bit7())
                            val bgeOpCode = BGE.opCode?.getOpCode(mapOf(MaskLabel.RS1 to x0, MaskLabel.RS2 to rs1, MaskLabel.IMM7 to imm7, MaskLabel.IMM5 to imm5))

                            if (bgeOpCode != null) {
                                binaryArray.add(bgeOpCode)
                            }
                        }
                    }
                }

                Bgez -> {
                    if (!values.isNullOrEmpty() && !labels.isNullOrEmpty()) {
                        val lblAddr = labelAddrMap.get(labels.first())
                        if (lblAddr != null) {
                            val rs1 = values[0]
                            val x0 = MutVal.Value.Binary("0", MutVal.Size.Bit5())
                            val labelAddr = MutVal.Value.Binary(lblAddr, MutVal.Size.Bit32())
                            val imm12 = (labelAddr - instrStartAddress).toBin().getResized(MutVal.Size.Bit12()).shr(1).getRawBinaryStr()
                            val imm5 = MutVal.Value.Binary(imm12.substring(8) + imm12[1], MutVal.Size.Bit5())
                            val imm7 = MutVal.Value.Binary(imm12[0] + imm12.substring(2, 8), MutVal.Size.Bit7())
                            val bgeOpCode = BGE.opCode?.getOpCode(mapOf(MaskLabel.RS1 to rs1, MaskLabel.RS2 to x0, MaskLabel.IMM7 to imm7, MaskLabel.IMM5 to imm5))

                            if (bgeOpCode != null) {
                                binaryArray.add(bgeOpCode)
                            }
                        }
                    }
                }

                Bltz -> {
                    if (!values.isNullOrEmpty() && !labels.isNullOrEmpty()) {
                        val lblAddr = labelAddrMap.get(labels.first())
                        if (lblAddr != null) {
                            val rs1 = values[0]
                            val x0 = MutVal.Value.Binary("0", MutVal.Size.Bit5())
                            val labelAddr = MutVal.Value.Binary(lblAddr, MutVal.Size.Bit32())
                            val imm12 = (labelAddr - instrStartAddress).toBin().getResized(MutVal.Size.Bit12()).shr(1).getRawBinaryStr()
                            val imm5 = MutVal.Value.Binary(imm12.substring(8) + imm12[1], MutVal.Size.Bit5())
                            val imm7 = MutVal.Value.Binary(imm12[0] + imm12.substring(2, 8), MutVal.Size.Bit7())
                            val bltOpCode = BLT.opCode?.getOpCode(mapOf(MaskLabel.RS1 to rs1, MaskLabel.RS2 to x0, MaskLabel.IMM7 to imm7, MaskLabel.IMM5 to imm5))

                            if (bltOpCode != null) {
                                binaryArray.add(bltOpCode)
                            }
                        }
                    }
                }

                BGTZ -> {
                    if (!values.isNullOrEmpty() && !labels.isNullOrEmpty()) {
                        val lblAddr = labelAddrMap.get(labels.first())
                        if (lblAddr != null) {
                            val rs1 = values[0]
                            val x0 = MutVal.Value.Binary("0", MutVal.Size.Bit5())
                            val labelAddr = MutVal.Value.Binary(lblAddr, MutVal.Size.Bit32())
                            val imm12 = (labelAddr - instrStartAddress).toBin().getResized(MutVal.Size.Bit12()).shr(1).getRawBinaryStr()
                            val imm5 = MutVal.Value.Binary(imm12.substring(8) + imm12[1], MutVal.Size.Bit5())
                            val imm7 = MutVal.Value.Binary(imm12[0] + imm12.substring(2, 8), MutVal.Size.Bit7())
                            val bltOpCode = BLT.opCode?.getOpCode(mapOf(MaskLabel.RS1 to x0, MaskLabel.RS2 to rs1, MaskLabel.IMM7 to imm7, MaskLabel.IMM5 to imm5))

                            if (bltOpCode != null) {
                                binaryArray.add(bltOpCode)
                            }
                        }
                    }
                }

                Bgt -> {
                    if (!values.isNullOrEmpty() && !labels.isNullOrEmpty()) {
                        val lblAddr = labelAddrMap.get(labels.first())
                        if (lblAddr != null) {
                            val rs1 = values[0]
                            val rs2 = values[1]

                            val labelAddr = MutVal.Value.Binary(lblAddr, MutVal.Size.Bit32())
                            val imm12 = (labelAddr - instrStartAddress).toBin().getResized(MutVal.Size.Bit12()).shr(1).getRawBinaryStr()
                            val imm5 = MutVal.Value.Binary(imm12.substring(8) + imm12[1], MutVal.Size.Bit5())
                            val imm7 = MutVal.Value.Binary(imm12[0] + imm12.substring(2, 8), MutVal.Size.Bit7())

                            val bltOpCode = BLT.opCode?.getOpCode(mapOf(MaskLabel.RS1 to rs2, MaskLabel.RS2 to rs1, MaskLabel.IMM7 to imm7, MaskLabel.IMM5 to imm5))

                            if (bltOpCode != null) {
                                binaryArray.add(bltOpCode)
                            }
                        }
                    }
                }

                Ble -> {
                    if (!values.isNullOrEmpty() && !labels.isNullOrEmpty()) {
                        val lblAddr = labelAddrMap.get(labels.first())
                        if (lblAddr != null) {
                            val rs1 = values[0]
                            val rs2 = values[1]

                            val labelAddr = MutVal.Value.Binary(lblAddr, MutVal.Size.Bit32())
                            val imm12 = (labelAddr - instrStartAddress).toBin().getResized(MutVal.Size.Bit12()).shr(1).getRawBinaryStr()
                            val imm5 = MutVal.Value.Binary(imm12.substring(8) + imm12[1], MutVal.Size.Bit5())
                            val imm7 = MutVal.Value.Binary(imm12[0] + imm12.substring(2, 8), MutVal.Size.Bit7())

                            val bgeOpCode = BGE.opCode?.getOpCode(mapOf(MaskLabel.RS1 to rs2, MaskLabel.RS2 to rs1, MaskLabel.IMM7 to imm7, MaskLabel.IMM5 to imm5))

                            if (bgeOpCode != null) {
                                binaryArray.add(bgeOpCode)
                            }
                        }
                    }
                }

                Bgtu -> {
                    if (!values.isNullOrEmpty() && !labels.isNullOrEmpty()) {
                        val lblAddr = labelAddrMap.get(labels.first())
                        if (lblAddr != null) {
                            val rs1 = values[0]
                            val rs2 = values[1]

                            val labelAddr = MutVal.Value.Binary(lblAddr, MutVal.Size.Bit32())
                            val imm12 = (labelAddr - instrStartAddress).toBin().getResized(MutVal.Size.Bit12()).shr(1).getRawBinaryStr()
                            val imm5 = MutVal.Value.Binary(imm12.substring(8) + imm12[1], MutVal.Size.Bit5())
                            val imm7 = MutVal.Value.Binary(imm12[0] + imm12.substring(2, 8), MutVal.Size.Bit7())

                            val bltuOpCode = BLTU.opCode?.getOpCode(mapOf(MaskLabel.RS1 to rs2, MaskLabel.RS2 to rs1, MaskLabel.IMM7 to imm7, MaskLabel.IMM5 to imm5))

                            if (bltuOpCode != null) {
                                binaryArray.add(bltuOpCode)
                            }
                        }
                    }
                }

                Bleu -> {
                    if (!values.isNullOrEmpty() && !labels.isNullOrEmpty()) {
                        val lblAddr = labelAddrMap.get(labels.first())
                        if (lblAddr != null) {
                            val rs1 = values[0]
                            val rs2 = values[1]

                            val labelAddr = MutVal.Value.Binary(lblAddr, MutVal.Size.Bit32())
                            val imm12 = (labelAddr - instrStartAddress).toBin().getResized(MutVal.Size.Bit12()).shr(1).getRawBinaryStr()
                            val imm5 = MutVal.Value.Binary(imm12.substring(8) + imm12[1], MutVal.Size.Bit5())
                            val imm7 = MutVal.Value.Binary(imm12[0] + imm12.substring(2, 8), MutVal.Size.Bit7())

                            val bgeuOpCode = BGEU.opCode?.getOpCode(mapOf(MaskLabel.RS1 to rs2, MaskLabel.RS2 to rs1, MaskLabel.IMM7 to imm7, MaskLabel.IMM5 to imm5))

                            if (bgeuOpCode != null) {
                                binaryArray.add(bgeuOpCode)
                            }
                        }
                    }
                }


            }
        } catch (e: IndexOutOfBoundsException) {
            console.error("IndexOutOfBoundsException: $e")
        }

        if (binaryArray.isEmpty()) {
            console.error("RISCVBinMapper: values and labels not matching for ${instrDef.instrType.name}!")
        }

        return binaryArray.toTypedArray()
    }

    fun getInstrFromBinary(binary: MutVal.Value.Binary): InstrResult? {
        for (instrType in entries) {
            val checkResult = instrType.opCode?.checkOpCode(binary)
            checkResult?.let {
                if (it.matches) {
                    return InstrResult(instrType, it.binaryMap)
                }
            }
        }
        return null
    }

    data class InstrResult(val type: RISCVGrammarV1.R_INSTR.InstrType, val binaryMap: Map<MaskLabel, MutVal.Value.Binary> = mapOf())

    class OpCode(val opMask: String, val maskLabels: Array<MaskLabel>) {

        val opMaskList = opMask.removePrefix(ArchConst.PRESTRING_BINARY).split(" ")

        fun checkOpCode(binary: MutVal.Value.Binary): CheckResult {
            if (binary.size != MutVal.Size.Bit32()) {
                return CheckResult(false)
            }
            // Check OpCode
            val binaryString = binary.getRawBinaryStr()
            val binaryOpCode = binaryString.substring(binaryString.length - 7)
            val originalOpCode = getMaskString(MaskLabel.OPCODE)
            if (originalOpCode.isNotEmpty()) {
                if (binaryOpCode == originalOpCode) {
                    // check static labels
                    val binaryMap = mutableMapOf<MaskLabel, MutVal.Value.Binary>()
                    if (DebugTools.RISCV_showBinMapperInfo) {
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
                                binaryMap.set(label, MutVal.Value.Binary(substring, label.maxSize))
                            }
                        }
                    }

                    return CheckResult(true, binaryMap)
                } else {
                    return CheckResult(false)
                }
            } else {
                return CheckResult(false)
            }
        }

        fun getOpCode(parameterMap: Map<MaskLabel, MutVal.Value.Binary>): MutVal.Value.Binary? {
            val opCode = opMaskList.toMutableList()
            var length = 0
            opCode.forEach { length += it.length }
            if (length != MutVal.Size.Bit32().bitWidth) {
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

            return MutVal.Value.Binary(opCode.joinToString("") { it }, MutVal.Size.Bit32())
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

        data class CheckResult(val matches: Boolean, val binaryMap: Map<MaskLabel, MutVal.Value.Binary> = mapOf())
    }

    enum class MaskLabel(val static: Boolean, val maxSize: MutVal.Size? = null) {
        OPCODE(true, MutVal.Size.Bit7()),
        RD(false, MutVal.Size.Bit5()),
        FUNCT3(true, MutVal.Size.Bit3()),
        RS1(false, MutVal.Size.Bit5()),
        RS2(false, MutVal.Size.Bit5()),
        SHAMT(false, MutVal.Size.Bit5()),
        FUNCT7(true, MutVal.Size.Bit7()),
        IMM5(false, MutVal.Size.Bit5()),
        IMM7(false, MutVal.Size.Bit7()),
        IMM12(false, MutVal.Size.Bit12()),
        IMM20(false, MutVal.Size.Bit20()),
        NONE(true)
    }


}