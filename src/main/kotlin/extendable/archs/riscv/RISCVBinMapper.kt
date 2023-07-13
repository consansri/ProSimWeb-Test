package extendable.archs.riscv

import extendable.ArchConst
import extendable.archs.riscv.RISCVGrammar.T1Instr.Type.*
import extendable.components.types.ByteValue
import tools.DebugTools

class RISCVBinMapper {

    var labelAddrMap = mapOf<RISCVGrammar.T1Label, String>()

    fun setLabelLinks(labelAddrMap: Map<RISCVGrammar.T1Label, String>) {
        this.labelAddrMap = labelAddrMap
    }

    fun getBinaryFromInstrDef(instrDef: RISCVGrammar.T2InstrDef, instrStartAddress: ByteValue.Type.Hex): Array<ByteValue.Type.Binary> {
        val binaryArray = mutableListOf<ByteValue.Type.Binary>()
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
                        val imm5 = ByteValue.Type.Binary(imm12.substring(imm12.length - 5), ByteValue.Size.Bit5())
                        val imm7 = ByteValue.Type.Binary(imm12.substring(0, 7), ByteValue.Size.Bit7())
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
                        val imm5 = ByteValue.Type.Binary(imm12.substring(imm12.length - 5))
                        val imm7 = ByteValue.Type.Binary(imm12.substring(imm12.length - 12, imm12.length - 5))

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
                        val imm32 = values[1].toBin().getUResized(ByteValue.Size.Bit32()).getRawBinaryStr()
                        val hi20 = imm32.substring(0, 20)
                        val low12 = imm32.substring(20)

                        val luiOpCode = LUI.opCode?.getOpCode(mapOf(MaskLabel.RD to regBin, MaskLabel.IMM20 to ByteValue.Type.Binary(hi20, ByteValue.Size.Bit20())))
                        val addiOpCode = ADDI.opCode?.getOpCode(mapOf(MaskLabel.RD to regBin, MaskLabel.RS1 to regBin, MaskLabel.IMM12 to ByteValue.Type.Binary(low12, ByteValue.Size.Bit12())))

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
                            val labelAddr =ByteValue.Type.Binary(lblAddr, ByteValue.Size.Bit32())
                            val imm12offset = (labelAddr - instrStartAddress.toBin()).toBin().getResized(ByteValue.Size.Bit12()).shr(1).getRawBinaryStr()
                            val imm5 = ByteValue.Type.Binary(imm12offset.substring(imm12offset.length - 5), ByteValue.Size.Bit5())
                            val imm7 = ByteValue.Type.Binary(imm12offset.substring(0, 7), ByteValue.Size.Bit7())

                            val thisType = when(instrDef.type){
                                BEQ1 -> BEQ
                                BNE1 -> BNE
                                BLT1 -> BLT
                                BGE1 -> BGE
                                BLTU1 -> BLTU
                                BGEU1 -> BGEU
                                else -> {null}
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
                            val imm20 = ((ByteValue.Type.Binary(lblAddr, ByteValue.Size.Bit32()) - instrStartAddress.toBin()).toBin() shr 1).getResized(ByteValue.Size.Bit20())

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
                            val rd = ByteValue.Type.Binary("1", ByteValue.Size.Bit5())
                            val imm20 = ((ByteValue.Type.Binary(lblAddr, ByteValue.Size.Bit32()) - instrStartAddress.toBin()).toBin() shr 1).getResized(ByteValue.Size.Bit20())

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
                            val rd = ByteValue.Type.Binary("0", ByteValue.Size.Bit5())
                            val imm20 = ((ByteValue.Type.Binary(lblAddr, ByteValue.Size.Bit32()) - instrStartAddress.toBin()).toBin() shr 1).getResized(ByteValue.Size.Bit20())

                            val jalOpCode = JAL.opCode?.getOpCode(mapOf(MaskLabel.RD to rd, MaskLabel.IMM20 to imm20))

                            if (jalOpCode != null) {
                                binaryArray.add(jalOpCode)
                            }
                        }
                    }
                }

                Ret -> {
                    val zero = ByteValue.Type.Binary("0", ByteValue.Size.Bit5())
                    val ra = ByteValue.Type.Binary("1", ByteValue.Size.Bit5())
                    val imm12 = ByteValue.Type.Binary("0", ByteValue.Size.Bit12())

                    val jalrOpCode = JALR.opCode?.getOpCode(mapOf(MaskLabel.RD to zero, MaskLabel.IMM12 to imm12, MaskLabel.RS1 to ra))

                    if (jalrOpCode != null) {
                        binaryArray.add(jalrOpCode)
                    }
                }

                Mv -> {
                    values?.let {
                        val rd = values[0].toBin()
                        val rs1 = values[1].toBin()
                        val zero = ByteValue.Type.Binary("0", ByteValue.Size.Bit12())

                        val addiOpCode = ADDI.opCode?.getOpCode(mapOf(MaskLabel.RD to rd, MaskLabel.RS1 to rs1, MaskLabel.IMM12 to zero))

                        if (addiOpCode != null) {
                            binaryArray.add(addiOpCode)
                        }
                    }
                }

                Nop -> {
                    val zero = ByteValue.Type.Binary("0", ByteValue.Size.Bit5())
                    val addiOpCode = ADDI.opCode?.getOpCode(mapOf(MaskLabel.RD to zero, MaskLabel.RS1 to zero, MaskLabel.RS2 to zero))

                    if (addiOpCode != null) {
                        binaryArray.add(addiOpCode)
                    }
                }

                Not -> {
                    values?.let {
                        val rd = values[0].toBin()
                        val rs1 = values[1].toBin()

                        val xoriOpCode = XORI.opCode?.getOpCode(mapOf(MaskLabel.RD to rd, MaskLabel.RS1 to rs1, MaskLabel.IMM12 to ByteValue.Type.Binary("1".repeat(12), ByteValue.Size.Bit12())))

                        if (xoriOpCode != null) {
                            binaryArray.add(xoriOpCode)
                        }
                    }
                }

                Neg -> {
                    values?.let {
                        val rd = values[0].toBin()
                        val rs1 = ByteValue.Type.Binary("0", ByteValue.Size.Bit5())
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
                        val imm12 = ByteValue.Type.Binary("1", ByteValue.Size.Bit12())

                        val sltiuOpCode = SLTIU.opCode?.getOpCode(mapOf(MaskLabel.RD to rd, MaskLabel.RS1 to rs1, MaskLabel.IMM12 to imm12))

                        if (sltiuOpCode != null) {
                            binaryArray.add(sltiuOpCode)
                        }
                    }
                }

                Snez -> {
                    values?.let {
                        val rd = values[0].toBin()
                        val rs1 = ByteValue.Type.Binary("0", ByteValue.Size.Bit5())
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
                        val zero = ByteValue.Type.Binary("0", ByteValue.Size.Bit12())

                        val sltOpCode = SLT.opCode?.getOpCode(mapOf(MaskLabel.RD to rd, MaskLabel.RS1 to rs1, MaskLabel.RS2 to zero))

                        if (sltOpCode != null) {
                            binaryArray.add(sltOpCode)
                        }
                    }
                }

                Sgtz -> {
                    values?.let {
                        val rd = values[0].toBin()
                        val rs1 = ByteValue.Type.Binary("0", ByteValue.Size.Bit5())
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
                            val x0 = ByteValue.Type.Binary("0", ByteValue.Size.Bit5())
                            val labelAddr =ByteValue.Type.Binary(lblAddr, ByteValue.Size.Bit32())
                            val imm12offset = (labelAddr - instrStartAddress.toBin()).toBin().getResized(ByteValue.Size.Bit12()).shr(1).getRawBinaryStr()
                            val imm5 = ByteValue.Type.Binary(imm12offset.substring(imm12offset.length - 5), ByteValue.Size.Bit5())
                            val imm7 = ByteValue.Type.Binary(imm12offset.substring(0, 7), ByteValue.Size.Bit7())
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
                            val x0 = ByteValue.Type.Binary("0", ByteValue.Size.Bit5())
                            val labelAddr =ByteValue.Type.Binary(lblAddr, ByteValue.Size.Bit32())
                            val imm12offset = (labelAddr - instrStartAddress.toBin()).toBin().getResized(ByteValue.Size.Bit12()).shr(1).getRawBinaryStr()
                            val imm5 = ByteValue.Type.Binary(imm12offset.substring(imm12offset.length - 5), ByteValue.Size.Bit5())
                            val imm7 = ByteValue.Type.Binary(imm12offset.substring(0, 7), ByteValue.Size.Bit7())
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
                            val x0 = ByteValue.Type.Binary("0", ByteValue.Size.Bit5())
                            val labelAddr =ByteValue.Type.Binary(lblAddr, ByteValue.Size.Bit32())
                            val imm12offset = (labelAddr - instrStartAddress.toBin()).toBin().getResized(ByteValue.Size.Bit12()).shr(1).getRawBinaryStr()
                            val imm5 = ByteValue.Type.Binary(imm12offset.substring(imm12offset.length - 5), ByteValue.Size.Bit5())
                            val imm7 = ByteValue.Type.Binary(imm12offset.substring(0, 7), ByteValue.Size.Bit7())
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
                            val x0 = ByteValue.Type.Binary("0", ByteValue.Size.Bit5())
                            val labelAddr =ByteValue.Type.Binary(lblAddr, ByteValue.Size.Bit32())
                            val imm12offset = (labelAddr - instrStartAddress.toBin()).toBin().getResized(ByteValue.Size.Bit12()).shr(1).getRawBinaryStr()
                            val imm5 = ByteValue.Type.Binary(imm12offset.substring(imm12offset.length - 5), ByteValue.Size.Bit5())
                            val imm7 = ByteValue.Type.Binary(imm12offset.substring(0, 7), ByteValue.Size.Bit7())
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
                            val x0 = ByteValue.Type.Binary("0", ByteValue.Size.Bit5())
                            val labelAddr =ByteValue.Type.Binary(lblAddr, ByteValue.Size.Bit32())
                            val imm12offset = (labelAddr - instrStartAddress.toBin()).toBin().getResized(ByteValue.Size.Bit12()).shr(1).getRawBinaryStr()
                            val imm5 = ByteValue.Type.Binary(imm12offset.substring(imm12offset.length - 5), ByteValue.Size.Bit5())
                            val imm7 = ByteValue.Type.Binary(imm12offset.substring(0, 7), ByteValue.Size.Bit7())
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
                            val x0 = ByteValue.Type.Binary("0", ByteValue.Size.Bit5())
                            val labelAddr =ByteValue.Type.Binary(lblAddr, ByteValue.Size.Bit32())
                            val imm12offset = (labelAddr - instrStartAddress.toBin()).toBin().getResized(ByteValue.Size.Bit12()).shr(1).getRawBinaryStr()
                            val imm5 = ByteValue.Type.Binary(imm12offset.substring(imm12offset.length - 5), ByteValue.Size.Bit5())
                            val imm7 = ByteValue.Type.Binary(imm12offset.substring(0, 7), ByteValue.Size.Bit7())
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

                            val labelAddr =ByteValue.Type.Binary(lblAddr, ByteValue.Size.Bit32())
                            val imm12offset = (labelAddr - instrStartAddress.toBin()).toBin().getResized(ByteValue.Size.Bit12()).shr(1).getRawBinaryStr()
                            val imm5 = ByteValue.Type.Binary(imm12offset.substring(imm12offset.length - 5), ByteValue.Size.Bit5())
                            val imm7 = ByteValue.Type.Binary(imm12offset.substring(0, 7), ByteValue.Size.Bit7())

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

                            val labelAddr =ByteValue.Type.Binary(lblAddr, ByteValue.Size.Bit32())
                            val imm12offset = (labelAddr - instrStartAddress.toBin()).toBin().getResized(ByteValue.Size.Bit12()).shr(1).getRawBinaryStr()
                            val imm5 = ByteValue.Type.Binary(imm12offset.substring(imm12offset.length - 5), ByteValue.Size.Bit5())
                            val imm7 = ByteValue.Type.Binary(imm12offset.substring(0, 7), ByteValue.Size.Bit7())

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

                            val labelAddr =ByteValue.Type.Binary(lblAddr, ByteValue.Size.Bit32())
                            val imm12offset = (labelAddr - instrStartAddress.toBin()).toBin().getResized(ByteValue.Size.Bit12()).shr(1).getRawBinaryStr()
                            val imm5 = ByteValue.Type.Binary(imm12offset.substring(imm12offset.length - 5), ByteValue.Size.Bit5())
                            val imm7 = ByteValue.Type.Binary(imm12offset.substring(0, 7), ByteValue.Size.Bit7())

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

                            val labelAddr =ByteValue.Type.Binary(lblAddr, ByteValue.Size.Bit32())
                            val imm12offset = (labelAddr - instrStartAddress.toBin()).toBin().getResized(ByteValue.Size.Bit12()).shr(1).getRawBinaryStr()
                            val imm5 = ByteValue.Type.Binary(imm12offset.substring(imm12offset.length - 5), ByteValue.Size.Bit5())
                            val imm7 = ByteValue.Type.Binary(imm12offset.substring(0, 7), ByteValue.Size.Bit7())

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
                        val x0 = ByteValue.Type.Binary("0", ByteValue.Size.Bit5())
                        val zero = ByteValue.Type.Binary("0", ByteValue.Size.Bit12())

                        val jalrOpCode = JALR.opCode?.getOpCode(mapOf(MaskLabel.RD to x0, MaskLabel.RS1 to rs1, MaskLabel.IMM12 to zero))

                        if (jalrOpCode != null) {
                            binaryArray.add(jalrOpCode)
                        }
                    }
                }

                JALR1 -> {
                    if (!values.isNullOrEmpty()) {
                        val rs1 = values[0].toBin()
                        val x1 = ByteValue.Type.Binary("1", ByteValue.Size.Bit5())
                        val zero = ByteValue.Type.Binary("0", ByteValue.Size.Bit12())

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

    fun getInstrFromBinary(binary: ByteValue.Type.Binary): InstrResult? {
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

    data class InstrResult(val type: RISCVGrammar.T1Instr.Type, val binaryMap: Map<MaskLabel, ByteValue.Type.Binary> = mapOf())

    class OpCode(val opMask: String, val maskLabels: Array<MaskLabel>) {

        val opMaskList = opMask.removePrefix(ArchConst.PRESTRING_BINARY).split(" ")

        fun checkOpCode(binary: ByteValue.Type.Binary): CheckResult {
            if (binary.size != ByteValue.Size.Bit32()) {
                return CheckResult(false)
            }
            // Check OpCode
            val binaryString = binary.getRawBinaryStr()
            val binaryOpCode = binaryString.substring(binaryString.length - 7)
            val originalOpCode = getMaskString(MaskLabel.OPCODE)
            if (originalOpCode.isNotEmpty()) {
                if (binaryOpCode == originalOpCode) {
                    // check static labels
                    val binaryMap = mutableMapOf<MaskLabel, ByteValue.Type.Binary>()
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
                                binaryMap.set(label, ByteValue.Type.Binary(substring, label.maxSize))
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

        fun getOpCode(parameterMap: Map<MaskLabel, ByteValue.Type.Binary>): ByteValue.Type.Binary? {
            val opCode = opMaskList.toMutableList()
            var length = 0
            opCode.forEach { length += it.length }
            if (length != ByteValue.Size.Bit32().bitWidth) {
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

            return ByteValue.Type.Binary(opCode.joinToString("") { it }, ByteValue.Size.Bit32())
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

        data class CheckResult(val matches: Boolean, val binaryMap: Map<MaskLabel, ByteValue.Type.Binary> = mapOf())
    }

    enum class MaskLabel(val static: Boolean, val maxSize: ByteValue.Size? = null) {
        OPCODE(true, ByteValue.Size.Bit7()),
        RD(false, ByteValue.Size.Bit5()),
        FUNCT3(true, ByteValue.Size.Bit3()),
        RS1(false, ByteValue.Size.Bit5()),
        RS2(false, ByteValue.Size.Bit5()),
        SHAMT(false, ByteValue.Size.Bit5()),
        FUNCT7(true, ByteValue.Size.Bit7()),
        IMM5(false, ByteValue.Size.Bit5()),
        IMM7(false, ByteValue.Size.Bit7()),
        IMM12(false, ByteValue.Size.Bit12()),
        IMM20(false, ByteValue.Size.Bit20()),
        NONE(true)
    }


}