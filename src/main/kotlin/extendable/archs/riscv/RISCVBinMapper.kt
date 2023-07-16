package extendable.archs.riscv

import extendable.ArchConst
import extendable.archs.riscv.RISCVGrammar.T1Instr.Type.*
import extendable.components.types.MutVal
import tools.DebugTools

class RISCVBinMapper {

    var labelAddrMap = mapOf<RISCVGrammar.T1Label, String>()

    fun setLabelLinks(labelAddrMap: Map<RISCVGrammar.T1Label, String>) {
        this.labelAddrMap = labelAddrMap
    }

    fun getBinaryFromInstrDef(instrDef: RISCVGrammar.T2InstrDef, instrStartAddress: MutVal.Value.Hex): Array<MutVal.Value.Binary> {
        val binaryArray = mutableListOf<MutVal.Value.Binary>()
        val values = instrDef.t1ParamColl?.getValues()
        val labels = instrDef.t1ParamColl?.getLabels()

        if (DebugTools.RISCV_showBinMapperInfo) {
            console.log("BinMapper.getBinaryFromInstrDef(): values -> ${values?.joinToString { it.toHex().getRawHexStr() }}")
            console.log("BinMapper.getBinaryFromInstrDef(): labels -> ${labels?.joinToString { it.wholeName }}")
        }

        if (!labels.isNullOrEmpty()) {
            for (label in labels) {
                val linkedAddress = labelAddrMap.get(label)
                if (linkedAddress == null) {
                    console.warn("BinMapper.getBinaryFromInstrDef(): missing label address entry for [${label.wholeName}]!")
                }
            }
        }

        try {
            when (instrDef.type) {
                LUI, AUIPC, JAL -> {
                    values?.let {
                        val opCode = instrDef.type.opCode?.getOpCode(mapOf(MaskLabel.RD to values[0].toBin(), MaskLabel.IMM20 to values[1].toBin()))
                        opCode?.let {
                            binaryArray.add(opCode)
                        }
                    }
                }

                JALR -> {
                    values?.let {
                        val opCode = instrDef.type.opCode?.getOpCode(mapOf(MaskLabel.RD to values[0].toBin(), MaskLabel.IMM12 to values[1].toBin(), MaskLabel.RS1 to values[2].toBin()))
                        opCode?.let {
                            binaryArray.add(opCode)
                        }
                    }
                }

                EBREAK, ECALL -> {
                    values?.let {
                        val opCode = instrDef.type.opCode?.getOpCode(mapOf())
                        opCode?.let {
                            binaryArray.add(opCode)
                        }
                    }
                }

                BEQ, BNE, BLT, BGE, BLTU, BGEU -> {
                    values?.let {
                        val imm12 = values[2].toBin().getRawBinaryStr()
                        val imm5 = MutVal.Value.Binary(imm12.substring(imm12.length - 5), MutVal.Size.Bit5())
                        val imm7 = MutVal.Value.Binary(imm12.substring(0, 7), MutVal.Size.Bit7())
                        val opCode = instrDef.type.opCode?.getOpCode(mapOf(MaskLabel.RS1 to values[0].toBin(), MaskLabel.RS2 to values[1].toBin(), MaskLabel.IMM5 to imm5, MaskLabel.IMM7 to imm7))
                        opCode?.let {
                            binaryArray.add(opCode)
                        }
                    }
                }


                LB, LH, LW, LBU, LHU -> {
                    values?.let {
                        val opCode = instrDef.type.opCode?.getOpCode(mapOf(MaskLabel.RD to values[0].toBin(), MaskLabel.IMM12 to values[1].toBin(), MaskLabel.RS1 to values[2].toBin()))
                        opCode?.let {
                            binaryArray.add(opCode)
                        }
                    }
                }

                SB, SH, SW -> {
                    values?.let {
                        val imm12 = values[1].toBin().getRawBinaryStr()
                        val imm5 = MutVal.Value.Binary(imm12.substring(imm12.length - 5))
                        val imm7 = MutVal.Value.Binary(imm12.substring(imm12.length - 12, imm12.length - 5))

                        val opCode = instrDef.type.opCode?.getOpCode(mapOf(MaskLabel.RS2 to values[0].toBin(), MaskLabel.IMM7 to imm7, MaskLabel.IMM5 to imm5, MaskLabel.RS1 to values[2].toBin()))
                        opCode?.let {
                            binaryArray.add(opCode)
                        }
                    }
                }

                ADDI, SLTI, SLTIU, XORI, ORI, ANDI -> {
                    values?.let {
                        val opCode = instrDef.type.opCode?.getOpCode(mapOf(MaskLabel.RD to values[0].toBin(), MaskLabel.RS1 to values[1].toBin(), MaskLabel.IMM12 to values[2].toBin()))
                        opCode?.let {
                            binaryArray.add(opCode)
                        }
                    }
                }

                SLLI, SRLI, SRAI -> {
                    values?.let {
                        val opCode = instrDef.type.opCode?.getOpCode(mapOf(MaskLabel.RD to values[0].toBin(), MaskLabel.RS1 to values[1].toBin(), MaskLabel.SHAMT to values[2].toBin()))
                        opCode?.let {
                            binaryArray.add(opCode)
                        }
                    }
                }

                ADD, SUB, SLL, SLT, SLTU, XOR, SRL, SRA, OR, AND -> {
                    values?.let {
                        val opCode = instrDef.type.opCode?.getOpCode(mapOf(MaskLabel.RD to values[0].toBin(), MaskLabel.RS1 to values[1].toBin(), MaskLabel.RS2 to values[2].toBin()))
                        opCode?.let {
                            binaryArray.add(opCode)
                        }
                    }
                }

                Li -> {
                    values?.let {
                        val regBin = values[0].toBin()
                        val immediate = values[1]
                        val imm32 = when (immediate) {
                            is MutVal.Value.Binary -> {
                                immediate.getUResized(MutVal.Size.Bit32())
                            }

                            is MutVal.Value.Dec -> {
                                immediate.getResized(MutVal.Size.Bit32()).toBin()
                            }

                            is MutVal.Value.Hex -> {
                                immediate.toBin().getResized(MutVal.Size.Bit32())
                            }

                            is MutVal.Value.UDec -> {
                                immediate.getUResized(MutVal.Size.Bit32()).toBin()
                            }
                        }

                        val hi20 = imm32.getRawBinaryStr().substring(0, 20)
                        val low12 = imm32.getRawBinaryStr().substring(20)

                        val imm12 = MutVal.Value.Binary(low12, MutVal.Size.Bit12())

                        var imm20 = when (immediate) {
                            is MutVal.Value.Binary -> {
                                val imm20temp = (MutVal.Value.Binary(hi20, MutVal.Size.Bit20())).toBin() // more performant
                                if (imm12.toDec().isNegative()) {
                                    (imm20temp + MutVal.Value.Binary("1")).toBin()
                                } else {
                                    imm20temp
                                }
                                /*(imm32 - imm12).toBin() ushr 12*/
                            }

                            is MutVal.Value.Dec -> {
                                (imm32 - imm12).toBin() ushr 12
                            }

                            is MutVal.Value.Hex -> {
                                val imm20temp = (MutVal.Value.Binary(hi20, MutVal.Size.Bit20())).toBin() // more performant
                                if (imm12.toDec().isNegative()) {
                                    (imm20temp + MutVal.Value.Binary("1")).toBin()
                                } else {
                                    imm20temp
                                }
                                /*(imm32 - imm12).toBin() ushr 12*/
                            }

                            is MutVal.Value.UDec -> {
                                val imm20temp = (MutVal.Value.Binary(hi20, MutVal.Size.Bit20())).toBin() // more performant
                                if (imm12.toDec().isNegative()) {
                                    (imm20temp + MutVal.Value.Binary("1")).toBin()
                                } else {
                                    imm20temp
                                }
                            }
                        }


                        val luiOpCode = LUI.opCode?.getOpCode(mapOf(MaskLabel.RD to regBin, MaskLabel.IMM20 to imm20))
                        val addiOpCode = ADDI.opCode?.getOpCode(mapOf(MaskLabel.RD to regBin, MaskLabel.RS1 to regBin, MaskLabel.IMM12 to imm12))

                        val shouldResult = (imm20.getResized(MutVal.Size.Bit32()).shl(12) + imm12).toBin()
                        console.log("PseudoInstr.Li: orig 0b$imm32 -> imm20 ${imm20.getBinaryStr()} + imm12 ${imm12.getBinaryStr()} toDec ${imm12.toDec().getRawDecStr()} -> imm32 ${shouldResult.getRawBinaryStr()} toDec ${shouldResult.toDec().getRawDecStr()} {same: ${shouldResult == imm32}}")

                        if (luiOpCode != null && addiOpCode != null) {
                            binaryArray.add(luiOpCode)
                            binaryArray.add(addiOpCode)
                        }

                    }
                }

                BEQ1, BNE1, BLT1, BGE1, BLTU1, BGEU1 -> {
                    if (values != null && labels != null) {
                        val lblAddr = labelAddrMap.get(labels.first())
                        if (lblAddr != null) {
                            val labelAddr = MutVal.Value.Binary(lblAddr, MutVal.Size.Bit32())
                            val imm12offset = (labelAddr - instrStartAddress.toBin()).toBin().getResized(MutVal.Size.Bit12()).shr(1).getRawBinaryStr()
                            val imm5 = MutVal.Value.Binary(imm12offset.substring(imm12offset.length - 5), MutVal.Size.Bit5())
                            val imm7 = MutVal.Value.Binary(imm12offset.substring(0, 7), MutVal.Size.Bit7())

                            val thisType = when (instrDef.type) {
                                BEQ1 -> BEQ
                                BNE1 -> BNE
                                BLT1 -> BLT
                                BGE1 -> BGE
                                BLTU1 -> BLTU
                                BGEU1 -> BGEU
                                else -> {
                                    null
                                }
                            }

                            val opCode = thisType?.opCode?.getOpCode(mapOf(MaskLabel.RS1 to values[0].toBin(), MaskLabel.RS2 to values[1].toBin(), MaskLabel.IMM5 to imm5, MaskLabel.IMM7 to imm7))
                            opCode?.let {
                                binaryArray.add(opCode)
                            }
                        }
                    }
                }


                JAL1 -> {
                    if (!values.isNullOrEmpty() && !labels.isNullOrEmpty()) {
                        val lblAddr = labelAddrMap.get(labels.first())
                        if (lblAddr != null) {
                            val rd = values[0].toBin()
                            val imm20 = ((MutVal.Value.Binary(lblAddr, MutVal.Size.Bit32()) - instrStartAddress.toBin()).toBin() shr 1).getResized(MutVal.Size.Bit20())

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
                            val imm20 = ((MutVal.Value.Binary(lblAddr, MutVal.Size.Bit32()) - instrStartAddress.toBin()).toBin() shr 1).getResized(MutVal.Size.Bit20())

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
                            val imm20 = ((MutVal.Value.Binary(lblAddr, MutVal.Size.Bit32()) - instrStartAddress.toBin()).toBin() shr 1).getResized(MutVal.Size.Bit20())

                            val jalOpCode = JAL.opCode?.getOpCode(mapOf(MaskLabel.RD to rd, MaskLabel.IMM20 to imm20))

                            if (jalOpCode != null) {
                                binaryArray.add(jalOpCode)
                            }
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
                        val rd = values[0].toBin()
                        val rs1 = values[1].toBin()
                        val zero = MutVal.Value.Binary("0", MutVal.Size.Bit12())

                        val addiOpCode = ADDI.opCode?.getOpCode(mapOf(MaskLabel.RD to rd, MaskLabel.RS1 to rs1, MaskLabel.IMM12 to zero))

                        if (addiOpCode != null) {
                            binaryArray.add(addiOpCode)
                        }
                    }
                }

                Nop -> {
                    val zero = MutVal.Value.Binary("0", MutVal.Size.Bit5())
                    val addiOpCode = ADDI.opCode?.getOpCode(mapOf(MaskLabel.RD to zero, MaskLabel.RS1 to zero, MaskLabel.RS2 to zero))

                    if (addiOpCode != null) {
                        binaryArray.add(addiOpCode)
                    }
                }

                Not -> {
                    values?.let {
                        val rd = values[0].toBin()
                        val rs1 = values[1].toBin()

                        val xoriOpCode = XORI.opCode?.getOpCode(mapOf(MaskLabel.RD to rd, MaskLabel.RS1 to rs1, MaskLabel.IMM12 to MutVal.Value.Binary("1".repeat(12), MutVal.Size.Bit12())))

                        if (xoriOpCode != null) {
                            binaryArray.add(xoriOpCode)
                        }
                    }
                }

                Neg -> {
                    values?.let {
                        val rd = values[0].toBin()
                        val rs1 = MutVal.Value.Binary("0", MutVal.Size.Bit5())
                        val rs2 = values[1].toBin()

                        val subOpCode = SUB.opCode?.getOpCode(mapOf(MaskLabel.RD to rd, MaskLabel.RS1 to rs1, MaskLabel.RS2 to rs2))

                        if (subOpCode != null) {
                            binaryArray.add(subOpCode)
                        }
                    }
                }

                Seqz -> {
                    values?.let {
                        val rd = values[0].toBin()
                        val rs1 = values[1].toBin()
                        val imm12 = MutVal.Value.Binary("1", MutVal.Size.Bit12())

                        val sltiuOpCode = SLTIU.opCode?.getOpCode(mapOf(MaskLabel.RD to rd, MaskLabel.RS1 to rs1, MaskLabel.IMM12 to imm12))

                        if (sltiuOpCode != null) {
                            binaryArray.add(sltiuOpCode)
                        }
                    }
                }

                Snez -> {
                    values?.let {
                        val rd = values[0].toBin()
                        val rs1 = MutVal.Value.Binary("0", MutVal.Size.Bit5())
                        val rs2 = values[1].toBin()

                        val sltuOpCode = SLTU.opCode?.getOpCode(mapOf(MaskLabel.RD to rd, MaskLabel.RS1 to rs1, MaskLabel.RS2 to rs2))

                        if (sltuOpCode != null) {
                            binaryArray.add(sltuOpCode)
                        }
                    }
                }

                Sltz -> {
                    values?.let {
                        val rd = values[0].toBin()
                        val rs1 = values[1].toBin()
                        val zero = MutVal.Value.Binary("0", MutVal.Size.Bit12())

                        val sltOpCode = SLT.opCode?.getOpCode(mapOf(MaskLabel.RD to rd, MaskLabel.RS1 to rs1, MaskLabel.RS2 to zero))

                        if (sltOpCode != null) {
                            binaryArray.add(sltOpCode)
                        }
                    }
                }

                Sgtz -> {
                    values?.let {
                        val rd = values[0].toBin()
                        val rs1 = MutVal.Value.Binary("0", MutVal.Size.Bit5())
                        val rs2 = values[1].toBin()

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
                            val rs1 = values[0].toBin()
                            val x0 = MutVal.Value.Binary("0", MutVal.Size.Bit5())
                            val labelAddr = MutVal.Value.Binary(lblAddr, MutVal.Size.Bit32())
                            val imm12offset = (labelAddr - instrStartAddress.toBin()).toBin().getResized(MutVal.Size.Bit12()).shr(1).getRawBinaryStr()
                            val imm5 = MutVal.Value.Binary(imm12offset.substring(imm12offset.length - 5), MutVal.Size.Bit5())
                            val imm7 = MutVal.Value.Binary(imm12offset.substring(0, 7), MutVal.Size.Bit7())
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
                            val rs1 = values[0].toBin()
                            val x0 = MutVal.Value.Binary("0", MutVal.Size.Bit5())
                            val labelAddr = MutVal.Value.Binary(lblAddr, MutVal.Size.Bit32())
                            val imm12offset = (labelAddr - instrStartAddress.toBin()).toBin().getResized(MutVal.Size.Bit12()).shr(1).getRawBinaryStr()
                            val imm5 = MutVal.Value.Binary(imm12offset.substring(imm12offset.length - 5), MutVal.Size.Bit5())
                            val imm7 = MutVal.Value.Binary(imm12offset.substring(0, 7), MutVal.Size.Bit7())
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
                            val rs1 = values[0].toBin()
                            val x0 = MutVal.Value.Binary("0", MutVal.Size.Bit5())
                            val labelAddr = MutVal.Value.Binary(lblAddr, MutVal.Size.Bit32())
                            val imm12offset = (labelAddr - instrStartAddress.toBin()).toBin().getResized(MutVal.Size.Bit12()).shr(1).getRawBinaryStr()
                            val imm5 = MutVal.Value.Binary(imm12offset.substring(imm12offset.length - 5), MutVal.Size.Bit5())
                            val imm7 = MutVal.Value.Binary(imm12offset.substring(0, 7), MutVal.Size.Bit7())
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
                            val rs1 = values[0].toBin()
                            val x0 = MutVal.Value.Binary("0", MutVal.Size.Bit5())
                            val labelAddr = MutVal.Value.Binary(lblAddr, MutVal.Size.Bit32())
                            val imm12offset = (labelAddr - instrStartAddress.toBin()).toBin().getResized(MutVal.Size.Bit12()).shr(1).getRawBinaryStr()
                            val imm5 = MutVal.Value.Binary(imm12offset.substring(imm12offset.length - 5), MutVal.Size.Bit5())
                            val imm7 = MutVal.Value.Binary(imm12offset.substring(0, 7), MutVal.Size.Bit7())
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
                            val rs1 = values[0].toBin()
                            val x0 = MutVal.Value.Binary("0", MutVal.Size.Bit5())
                            val labelAddr = MutVal.Value.Binary(lblAddr, MutVal.Size.Bit32())
                            val imm12offset = (labelAddr - instrStartAddress.toBin()).toBin().getResized(MutVal.Size.Bit12()).shr(1).getRawBinaryStr()
                            val imm5 = MutVal.Value.Binary(imm12offset.substring(imm12offset.length - 5), MutVal.Size.Bit5())
                            val imm7 = MutVal.Value.Binary(imm12offset.substring(0, 7), MutVal.Size.Bit7())
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
                            val rs1 = values[0].toBin()
                            val x0 = MutVal.Value.Binary("0", MutVal.Size.Bit5())
                            val labelAddr = MutVal.Value.Binary(lblAddr, MutVal.Size.Bit32())
                            val imm12offset = (labelAddr - instrStartAddress.toBin()).toBin().getResized(MutVal.Size.Bit12()).shr(1).getRawBinaryStr()
                            val imm5 = MutVal.Value.Binary(imm12offset.substring(imm12offset.length - 5), MutVal.Size.Bit5())
                            val imm7 = MutVal.Value.Binary(imm12offset.substring(0, 7), MutVal.Size.Bit7())
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
                            val rs1 = values[0].toBin()
                            val rs2 = values[1].toBin()

                            val labelAddr = MutVal.Value.Binary(lblAddr, MutVal.Size.Bit32())
                            val imm12offset = (labelAddr - instrStartAddress.toBin()).toBin().getResized(MutVal.Size.Bit12()).shr(1).getRawBinaryStr()
                            val imm5 = MutVal.Value.Binary(imm12offset.substring(imm12offset.length - 5), MutVal.Size.Bit5())
                            val imm7 = MutVal.Value.Binary(imm12offset.substring(0, 7), MutVal.Size.Bit7())

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
                            val rs1 = values[0].toBin()
                            val rs2 = values[1].toBin()

                            val labelAddr = MutVal.Value.Binary(lblAddr, MutVal.Size.Bit32())
                            val imm12offset = (labelAddr - instrStartAddress.toBin()).toBin().getResized(MutVal.Size.Bit12()).shr(1).getRawBinaryStr()
                            val imm5 = MutVal.Value.Binary(imm12offset.substring(imm12offset.length - 5), MutVal.Size.Bit5())
                            val imm7 = MutVal.Value.Binary(imm12offset.substring(0, 7), MutVal.Size.Bit7())

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
                            val rs1 = values[0].toBin()
                            val rs2 = values[1].toBin()

                            val labelAddr = MutVal.Value.Binary(lblAddr, MutVal.Size.Bit32())
                            val imm12offset = (labelAddr - instrStartAddress.toBin()).toBin().getResized(MutVal.Size.Bit12()).shr(1).getRawBinaryStr()
                            val imm5 = MutVal.Value.Binary(imm12offset.substring(imm12offset.length - 5), MutVal.Size.Bit5())
                            val imm7 = MutVal.Value.Binary(imm12offset.substring(0, 7), MutVal.Size.Bit7())

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
                            val rs1 = values[0].toBin()
                            val rs2 = values[1].toBin()

                            val labelAddr = MutVal.Value.Binary(lblAddr, MutVal.Size.Bit32())
                            val imm12offset = (labelAddr - instrStartAddress.toBin()).toBin().getResized(MutVal.Size.Bit12()).shr(1).getRawBinaryStr()
                            val imm5 = MutVal.Value.Binary(imm12offset.substring(imm12offset.length - 5), MutVal.Size.Bit5())
                            val imm7 = MutVal.Value.Binary(imm12offset.substring(0, 7), MutVal.Size.Bit7())

                            val bgeuOpCode = BGEU.opCode?.getOpCode(mapOf(MaskLabel.RS1 to rs2, MaskLabel.RS2 to rs1, MaskLabel.IMM7 to imm7, MaskLabel.IMM5 to imm5))

                            if (bgeuOpCode != null) {
                                binaryArray.add(bgeuOpCode)
                            }
                        }
                    }
                }

                Jr -> {
                    if (!values.isNullOrEmpty()) {
                        val rs1 = values[0].toBin()
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
                        val rs1 = values[0].toBin()
                        val x1 = MutVal.Value.Binary("1", MutVal.Size.Bit5())
                        val zero = MutVal.Value.Binary("0", MutVal.Size.Bit12())

                        val jalrOpCode = JALR.opCode?.getOpCode(mapOf(MaskLabel.RD to x1, MaskLabel.RS1 to rs1, MaskLabel.IMM12 to zero))

                        if (jalrOpCode != null) {
                            binaryArray.add(jalrOpCode)
                        }
                    }
                }
            }
        } catch (e: IndexOutOfBoundsException) {
            console.error("IndexOutOfBoundsException: $e")
        }

        return binaryArray.toTypedArray()
    }

    fun getInstrFromBinary(binary: MutVal.Value.Binary): InstrResult? {
        for (instrType in RISCVGrammar.T1Instr.Type.values()) {
            val checkResult = instrType.opCode?.checkOpCode(binary)
            checkResult?.let {
                if (it.matches) {
                    return InstrResult(instrType, it.binaryMap)
                }
            }
        }
        return null
    }

    data class InstrResult(val type: RISCVGrammar.T1Instr.Type, val binaryMap: Map<MaskLabel, MutVal.Value.Binary> = mapOf())

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
                if (DebugTools.RISCV_showOpCodeInfo) {
                    console.warn("BinMapper.OpCode: OpMask isn't 32Bit Binary! -> returning null")
                }
                return null
            }
            if (opCode.size != maskLabels.size) {
                if (DebugTools.RISCV_showOpCodeInfo) {
                    console.warn("BinMapper.OpCode: OpMask [$opMask] and Labels [${maskLabels.joinToString { it.name }}] aren't the same size! -> returning null")
                }
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
                            if (DebugTools.RISCV_showOpCodeInfo) {
                                console.warn("BinMapper.OpCode.getOpCode(): can't insert ByteValue in OpMask without a maxSize! -> returning null")
                            }
                            return null
                        }
                    } else {
                        if (DebugTools.RISCV_showOpCodeInfo) {
                            console.warn("BinMapper.OpCode.getOpCode(): parameter [${maskLabel.name}] not found! -> inserting zeros")
                        }
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