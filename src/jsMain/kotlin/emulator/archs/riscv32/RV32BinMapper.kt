package emulator.archs.riscv32

import emulator.kit.Settings
import emulator.archs.riscv32.RV32Syntax.R_INSTR.InstrType.*
import emulator.kit.types.Variable
import debug.DebugTools
import emulator.kit.Architecture
import emulator.kit.types.Variable.Value.*
import emulator.kit.types.Variable.Size.*


/**
 *  Converts instructions with its parameters between Text and binary format.
 */
class RV32BinMapper {
    private var labelAddrMap = mapOf<RV32Syntax.E_LABEL, String>()
    fun setLabelLinks(labelAddrMap: Map<RV32Syntax.E_LABEL, String>) {
        this.labelAddrMap = labelAddrMap
    }

    fun getBinaryFromInstrDef(instrDef: RV32Syntax.R_INSTR, instrStartAddress: Hex, architecture: Architecture): Array<Bin> {
        val binArray = mutableListOf<Bin>()
        val values = instrDef.paramcoll?.getValues(null)
        val binValues = instrDef.paramcoll?.getValues(null)?.map { it.toBin() }
        val labels = mutableListOf<RV32Syntax.E_LABEL>()
        instrDef.paramcoll?.getILabels()?.forEach { labels.add(it.label) }
        instrDef.paramcoll?.getULabels()?.forEach { labels.add(it.label) }
        instrDef.paramcoll?.getJLabels()?.forEach { labels.add(it.label) }

        if (DebugTools.RV32_showBinMapperInfo) {
            console.log("BinMapper.getBinaryFromInstrDef(): \t${instrDef.instrType.id} -> values: ${values?.joinToString(",") { it.toHex().getRawHexStr() }} labels: ${labels.joinToString(",") { it.wholeName }}")
        }

        if (labels.isNotEmpty()) {
            for (label in labels) {
                val linkedAddress = labelAddrMap[label]
                if (linkedAddress == null) {
                    console.warn("BinMapper.getBinaryFromInstrDef(): missing label address entry for [${label.wholeName}]!")
                }
            }
        }
        try {
            when (val instrDefType = instrDef.instrType) {
                LUI, AUIPC -> {
                    if(binValues != null) {
                        val imm20 = Bin(binValues[1].toBin().getRawBinStr().substring(0, 20), Bit20())
                        val opCode = instrDef.instrType.opCode?.getOpCode(mapOf(MaskLabel.RD to binValues[0], MaskLabel.IMM20 to imm20))
                        opCode?.let {
                            binArray.add(opCode)
                        }
                    }
                }

                JAL -> {
                    val immediate = instrDef.paramcoll?.getValues(Bit20())?.getOrNull(1)
                    if(immediate != null && binValues != null) {
                        val imm20toWork = immediate.toBin().getRawBinStr()

                        /**
                         *      RV32IDOC Index   20 19 18 17 16 15 14 13 12 11 10  9  8  7  6  5  4  3  2  1
                         *        String Index    0  1  2  3  4  5  6  7  8  9 10 11 12 13 14 15 16 17 18 19
                         */

                        val imm20 = Bin(imm20toWork[0].toString() + imm20toWork.substring(10) + imm20toWork[9] + imm20toWork.substring(1, 9), Bit20())
                        val opCode = instrDef.instrType.opCode?.getOpCode(mapOf(MaskLabel.RD to binValues[0], MaskLabel.IMM20 to imm20))
                        opCode?.let {
                            binArray.add(opCode)
                        }
                    }
                }

                JALR -> {
                    val immediate = instrDef.paramcoll?.getValues(Bit12())?.getOrNull(2)
                    if(immediate != null && binValues != null) {
                        val opCode = instrDef.instrType.opCode?.getOpCode(mapOf(MaskLabel.RD to binValues[0], MaskLabel.IMM12 to immediate.toBin(), MaskLabel.RS1 to binValues[1]))
                        opCode?.let {
                            binArray.add(opCode)
                        }
                    }
                }

                EBREAK, ECALL -> {
                    binValues?.let {
                        val opCode = instrDef.instrType.opCode?.getOpCode(mapOf())
                        opCode?.let {
                            binArray.add(opCode)
                        }
                    }
                }

                BEQ, BNE, BLT, BGE, BLTU, BGEU -> {
                    val immediate = instrDef.paramcoll?.getValues(Bit12())?.getOrNull(2)
                    if(immediate != null && binValues != null){
                        val imm12 = immediate.toBin().getRawBinStr()
                        val imm5 = Bin(imm12.substring(8) + imm12[1], Bit5())
                        val imm7 = Bin(imm12[0] + imm12.substring(2, 8), Bit7())

                        val opCode = instrDef.instrType.opCode?.getOpCode(mapOf(MaskLabel.RS1 to binValues[0], MaskLabel.RS2 to binValues[1], MaskLabel.IMM5 to imm5, MaskLabel.IMM7 to imm7))
                        opCode?.let {
                            binArray.add(opCode)
                        }
                    }
                }

                BEQ1, BNE1, BLT1, BGE1, BLTU1, BGEU1 -> {
                    if (binValues != null && labels.isNotEmpty()) {
                        val lblAddr = labelAddrMap[labels.first()]
                        if (lblAddr != null) {
                            val labelAddr = Bin(lblAddr, Bit32())
                            val imm12offset = (labelAddr - instrStartAddress).toBin().getResized(Bit12()).shr(1).getRawBinStr()
                            val imm5 = Bin(imm12offset.substring(8) + imm12offset[1], Bit5())
                            val imm7 = Bin(imm12offset[0] + imm12offset.substring(2, 8), Bit7())

                            val opCode = instrDefType.relative?.opCode?.getOpCode(mapOf(MaskLabel.RS1 to binValues[0], MaskLabel.RS2 to binValues[1], MaskLabel.IMM5 to imm5, MaskLabel.IMM7 to imm7))
                            opCode?.let {
                                binArray.add(opCode)
                            }
                        }
                    }
                }

                LB, LH, LW, LBU, LHU -> {
                    val immediate = instrDef.paramcoll?.getValues(Bit12())?.getOrNull(1)
                    if(immediate != null && binValues != null){
                        val opCode = instrDef.instrType.opCode?.getOpCode(mapOf(MaskLabel.RD to binValues[0], MaskLabel.IMM12 to immediate.toBin(), MaskLabel.RS1 to binValues[2]))
                        opCode?.let {
                            binArray.add(opCode)
                        }
                    }
                }

                SB, SH, SW -> {
                    val immediate = instrDef.paramcoll?.getValues(Bit12())?.getOrNull(1)
                    if (binValues != null && binValues.size == 3 && immediate != null) {
                        val imm12 = immediate.toBin().getRawBinStr()
                        val imm5 = Bin(imm12.substring(imm12.length - 5))
                        val imm7 = Bin(imm12.substring(imm12.length - 12, imm12.length - 5))

                        val opCode = instrDef.instrType.opCode?.getOpCode(mapOf(MaskLabel.RS2 to binValues[0], MaskLabel.IMM7 to imm7, MaskLabel.IMM5 to imm5, MaskLabel.RS1 to binValues[2]))
                        opCode?.let {
                            binArray.add(opCode)
                        }
                    }
                }

                ADDI, SLTI, SLTIU, XORI, ORI, ANDI -> {
                    val imm12 = instrDef.paramcoll?.getValues(Bit12())?.getOrNull(2)?.toBin()
                    if (binValues != null && imm12 != null) {
                        val opCode = instrDef.instrType.opCode?.getOpCode(mapOf(MaskLabel.RD to binValues[0], MaskLabel.RS1 to binValues[1], MaskLabel.IMM12 to imm12))
                        opCode?.let {
                            binArray.add(opCode)
                        }
                    }
                }

                SLLI, SRLI, SRAI -> {
                    val imm5 = instrDef.paramcoll?.getValues(Bit5())?.getOrNull(2)?.toBin()
                    if (binValues != null && imm5 != null) {
                        val opCode = instrDef.instrType.opCode?.getOpCode(mapOf(MaskLabel.RD to binValues[0], MaskLabel.RS1 to binValues[1], MaskLabel.SHAMT to imm5))
                        opCode?.let {
                            binArray.add(opCode)
                        }
                    }
                }

                ADD, SUB, SLL, SLT, SLTU, XOR, SRL, SRA, OR, AND, MUL, MULH, MULHSU, MULHU, DIV, DIVU, REM, REMU -> {
                    binValues?.let {
                        val opCode = instrDef.instrType.opCode?.getOpCode(mapOf(MaskLabel.RD to binValues[0], MaskLabel.RS1 to binValues[1], MaskLabel.RS2 to binValues[2]))
                        opCode?.let {
                            binArray.add(opCode)
                        }
                    }
                }

                CSRRW, CSRRS, CSRRC -> {
                    binValues?.let {
                        val csrAddr = binValues[1].getUResized(Bit12())
                        val opCode = instrDef.instrType.opCode?.getOpCode(mapOf(MaskLabel.RD to binValues[0], MaskLabel.CSR to csrAddr, MaskLabel.RS1 to binValues[2]))
                        opCode?.let {
                            binArray.add(opCode)
                        }
                    }
                }

                CSRRWI, CSRRSI, CSRRCI -> {
                    val immediate = instrDef.paramcoll?.getValues(Bit5())?.getOrNull(2)
                    if (immediate != null && binValues != null){
                        val csrAddr = binValues[1].getUResized(Bit12())
                        val uimm5 = immediate.toBin()
                        val opCode = instrDef.instrType.opCode?.getOpCode(mapOf(MaskLabel.RD to binValues[0], MaskLabel.CSR to csrAddr, MaskLabel.UIMM5 to uimm5))
                        opCode?.let {
                            binArray.add(opCode)
                        }
                    }
                }

                CSRW -> {
                    binValues?.let {
                        val csrAddr = binValues[0].getUResized(Bit12())
                        val zero = Bin("0", Bit5())
                        val opCode = CSRRW.opCode?.getOpCode(mapOf(MaskLabel.RD to zero, MaskLabel.CSR to csrAddr, MaskLabel.RS1 to binValues[1]))
                        opCode?.let {
                            binArray.add(opCode)
                        }
                    }
                }
                CSRR -> {
                    binValues?.let {
                        val csrAddr = binValues[1].getUResized(Bit12())
                        val zero = Bin("0", Bit5())
                        val opCode = CSRRS.opCode?.getOpCode(mapOf(MaskLabel.RD to binValues[0], MaskLabel.CSR to csrAddr, MaskLabel.RS1 to zero))
                        opCode?.let {
                            binArray.add(opCode)
                        }
                    }
                }

                Li -> {
                    val imm = instrDef.paramcoll?.getValues(Bit32())?.getOrNull(2)
                    if(imm != null && values != null) {
                        val regBin = values[0].toBin()
                        val hi20 = imm.toBin().getRawBinStr().substring(0, 20)
                        val low12 = imm.toBin().getRawBinStr().substring(20)

                        val imm12 = Bin(low12, Bit12())

                        val imm20temp = (Bin(hi20, Bit20())).toBin() // more performant
                        val imm20 = if (imm12.toDec().isNegative()) {
                            (imm20temp + Bin("1")).toBin()
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
                    if (binValues != null && labels.isNotEmpty()) {
                        val regBin = binValues[0]
                        val address = labelAddrMap[labels.first()]
                        if (address != null) {
                            val imm32 = Bin(address, RV32.XLEN)
                            val hi20 = imm32.getRawBinStr().substring(0, 20)
                            val low12 = imm32.getRawBinStr().substring(20)

                            val imm12 = Bin(low12, Bit12())

                            val imm20temp = (Bin(hi20, Bit20())).toBin() // more performant
                            val imm20 = if (imm12.toDec().isNegative()) {
                                (imm20temp + Bin("1")).toBin()
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
                    if (!binValues.isNullOrEmpty() && labels.isNotEmpty()) {
                        val lblAddr = labelAddrMap[labels.first()]
                        if (lblAddr != null) {
                            val rd = binValues[0]
                            val imm20toWork = ((Bin(lblAddr, Bit32()) - instrStartAddress).toBin() shr 1).getResized(Bit20()).getRawBinStr()

                            /**
                             *      RV32IDOC Index   20 19 18 17 16 15 14 13 12 11 10  9  8  7  6  5  4  3  2  1
                             *        String Index    0  1  2  3  4  5  6  7  8  9 10 11 12 13 14 15 16 17 18 19
                             */
                            val imm20 = Bin(imm20toWork[0].toString() + imm20toWork.substring(10) + imm20toWork[9] + imm20toWork.substring(1, 9), Bit20())

                            val jalOpCode = JAL.opCode?.getOpCode(mapOf(MaskLabel.RD to rd, MaskLabel.IMM20 to imm20))

                            if (jalOpCode != null) {
                                binArray.add(jalOpCode)
                            }
                        }
                    }
                }

                JAL2 -> {
                    if (labels.isNotEmpty()) {
                        val lblAddr = labelAddrMap[labels.first()]
                        if (lblAddr != null) {
                            val rd = Bin("1", Bit5())
                            val imm20toWork = ((Bin(lblAddr, Bit32()) - instrStartAddress).toBin() shr 1).getResized(Bit20()).getRawBinStr()

                            /**
                             *      RV32IDOC Index   20 19 18 17 16 15 14 13 12 11 10  9  8  7  6  5  4  3  2  1
                             *        String Index    0  1  2  3  4  5  6  7  8  9 10 11 12 13 14 15 16 17 18 19
                             */
                            val imm20 = Bin(imm20toWork[0].toString() + imm20toWork.substring(10) + imm20toWork[9] + imm20toWork.substring(1, 9), Bit20())

                            val jalOpCode = JAL.opCode?.getOpCode(mapOf(MaskLabel.RD to rd, MaskLabel.IMM20 to imm20))

                            if (jalOpCode != null) {
                                binArray.add(jalOpCode)
                            }
                        }
                    }
                }

                J -> {
                    if (labels.isNotEmpty()) {
                        val lblAddr = labelAddrMap[labels.first()]
                        if (lblAddr != null) {
                            val rd = Bin("0", Bit5())
                            val imm20toWork = ((Bin(lblAddr, Bit32()) - instrStartAddress).toBin() shr 1).getResized(Bit20()).getRawBinStr()

                            /**
                             *      RV32IDOC Index   20 19 18 17 16 15 14 13 12 11 10  9  8  7  6  5  4  3  2  1
                             *        String Index    0  1  2  3  4  5  6  7  8  9 10 11 12 13 14 15 16 17 18 19
                             */
                            val imm20 = Bin(imm20toWork[0].toString() + imm20toWork.substring(10) + imm20toWork[9] + imm20toWork.substring(1, 9), Bit20())

                            val jalOpCode = JAL.opCode?.getOpCode(mapOf(MaskLabel.RD to rd, MaskLabel.IMM20 to imm20))

                            if (jalOpCode != null) {
                                binArray.add(jalOpCode)
                            }
                        }
                    }
                }

                Jr -> {
                    if (!binValues.isNullOrEmpty()) {
                        val rs1 = binValues[0]
                        val x0 = Bin("0", Bit5())
                        val zero = Bin("0", Bit12())

                        val jalrOpCode = JALR.opCode?.getOpCode(mapOf(MaskLabel.RD to x0, MaskLabel.RS1 to rs1, MaskLabel.IMM12 to zero))

                        if (jalrOpCode != null) {
                            binArray.add(jalrOpCode)
                        }
                    }
                }

                JALR1 -> {
                    if (!binValues.isNullOrEmpty()) {
                        val rs1 = binValues[0]
                        val x1 = Bin("1", Bit5())
                        val zero = Bin("0", Bit12())

                        val jalrOpCode = JALR.opCode?.getOpCode(mapOf(MaskLabel.RD to x1, MaskLabel.RS1 to rs1, MaskLabel.IMM12 to zero))

                        if (jalrOpCode != null) {
                            binArray.add(jalrOpCode)
                        }
                    }
                }

                JALR2 -> {
                    binValues?.let {
                        val opCode = JALR.opCode?.getOpCode(mapOf(MaskLabel.RD to binValues[0], MaskLabel.IMM12 to binValues[1], MaskLabel.RS1 to binValues[2]))
                        opCode?.let {
                            binArray.add(opCode)
                        }
                    }
                }

                Ret -> {
                    val zero = Bin("0", Bit5())
                    val ra = Bin("1", Bit5())
                    val imm12 = Bin("0", Bit12())

                    val jalrOpCode = JALR.opCode?.getOpCode(mapOf(MaskLabel.RD to zero, MaskLabel.IMM12 to imm12, MaskLabel.RS1 to ra))

                    if (jalrOpCode != null) {
                        binArray.add(jalrOpCode)
                    }
                }

                Call -> {
                    if (labels.isNotEmpty()) {
                        val lblAddr = labelAddrMap[labels.first()]
                        if (lblAddr != null) {
                            val x1 = Bin("1", Bit5())

                            val pcRelAddress32 = (Bin(lblAddr, Bit32()) - instrStartAddress).toBin()
                            val imm32 = pcRelAddress32.getRawBinStr()

                            val jalrOff = Bin(imm32.substring(20), Bit12())
                            val auipcOff = (pcRelAddress32 - jalrOff.getResized(Bit32())).toBin().ushr(12).getUResized(Bit20())

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
                        val lblAddr = labelAddrMap[labels.first()]
                        if (lblAddr != null) {
                            val x0 = Bin("0", Bit5())
                            val x6 = Hex("6", Bit5()).toBin()

                            val pcRelAddress32 = (Bin(lblAddr, Bit32()) - instrStartAddress).toBin()
                            val imm32 = pcRelAddress32.getRawBinStr()

                            val jalrOff = Bin(imm32.substring(20), Bit12())
                            val auipcOff = (pcRelAddress32 - jalrOff.getResized(Bit32())).toBin().ushr(12).getUResized(Bit20())

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
                    binValues?.let {
                        val rd = binValues[0]
                        val rs1 = binValues[1]
                        val zero = Bin("0", Bit12())

                        val addiOpCode = ADDI.opCode?.getOpCode(mapOf(MaskLabel.RD to rd, MaskLabel.RS1 to rs1, MaskLabel.IMM12 to zero))

                        if (addiOpCode != null) {
                            binArray.add(addiOpCode)
                        }
                    }
                }

                Nop -> {
                    val zero = Bin("0", Bit5())
                    val imm12 = Bin("0", Bit12())
                    val addiOpCode = ADDI.opCode?.getOpCode(mapOf(MaskLabel.RD to zero, MaskLabel.RS1 to zero, MaskLabel.IMM12 to imm12))

                    if (addiOpCode != null) {
                        binArray.add(addiOpCode)
                    }
                }

                Not -> {
                    binValues?.let {
                        val rd = binValues[0]
                        val rs1 = binValues[1]

                        val xoriOpCode = XORI.opCode?.getOpCode(mapOf(MaskLabel.RD to rd, MaskLabel.RS1 to rs1, MaskLabel.IMM12 to Bin("1".repeat(12), Bit12())))

                        if (xoriOpCode != null) {
                            binArray.add(xoriOpCode)
                        }
                    }
                }

                Neg -> {
                    binValues?.let {
                        val rd = binValues[0]
                        val rs1 = Bin("0", Bit5())
                        val rs2 = binValues[1]

                        val subOpCode = SUB.opCode?.getOpCode(mapOf(MaskLabel.RD to rd, MaskLabel.RS1 to rs1, MaskLabel.RS2 to rs2))

                        if (subOpCode != null) {
                            binArray.add(subOpCode)
                        }
                    }
                }

                Seqz -> {
                    binValues?.let {
                        val rd = binValues[0]
                        val rs1 = binValues[1]
                        val imm12 = Bin("1", Bit12())

                        val sltiuOpCode = SLTIU.opCode?.getOpCode(mapOf(MaskLabel.RD to rd, MaskLabel.RS1 to rs1, MaskLabel.IMM12 to imm12))

                        if (sltiuOpCode != null) {
                            binArray.add(sltiuOpCode)
                        }
                    }
                }

                Snez -> {
                    binValues?.let {
                        val rd = binValues[0]
                        val rs1 = Bin("0", Bit5())
                        val rs2 = binValues[1]

                        val sltuOpCode = SLTU.opCode?.getOpCode(mapOf(MaskLabel.RD to rd, MaskLabel.RS1 to rs1, MaskLabel.RS2 to rs2))

                        if (sltuOpCode != null) {
                            binArray.add(sltuOpCode)
                        }
                    }
                }

                Sltz -> {
                    binValues?.let {
                        val rd = binValues[0]
                        val rs1 = binValues[1]
                        val zero = Bin("0", Bit12())

                        val sltOpCode = SLT.opCode?.getOpCode(mapOf(MaskLabel.RD to rd, MaskLabel.RS1 to rs1, MaskLabel.RS2 to zero))

                        if (sltOpCode != null) {
                            binArray.add(sltOpCode)
                        }
                    }
                }

                Sgtz -> {
                    binValues?.let {
                        val rd = binValues[0]
                        val rs1 = Bin("0", Bit5())
                        val rs2 = binValues[1]

                        val sltOpCode = SLT.opCode?.getOpCode(mapOf(MaskLabel.RD to rd, MaskLabel.RS1 to rs1, MaskLabel.RS2 to rs2))

                        if (sltOpCode != null) {
                            binArray.add(sltOpCode)
                        }
                    }
                }

                Beqz -> {
                    if (!binValues.isNullOrEmpty() && labels.isNotEmpty()) {
                        val lblAddr = labelAddrMap[labels.first()]
                        if (lblAddr != null) {
                            val rs1 = binValues[0]
                            val x0 = Bin("0", Bit5())
                            val labelAddr = Bin(lblAddr, Bit32())
                            val imm12 = (labelAddr - instrStartAddress).toBin().getResized(Bit12()).shr(1).getRawBinStr()
                            val imm5 = Bin(imm12.substring(8) + imm12[1], Bit5())
                            val imm7 = Bin(imm12[0] + imm12.substring(2, 8), Bit7())
                            val beqOpCode = BEQ.opCode?.getOpCode(mapOf(MaskLabel.RS1 to rs1, MaskLabel.RS2 to x0, MaskLabel.IMM7 to imm7, MaskLabel.IMM5 to imm5))

                            if (beqOpCode != null) {
                                binArray.add(beqOpCode)
                            }
                        }
                    }
                }

                Bnez -> {
                    if (!binValues.isNullOrEmpty() && labels.isNotEmpty()) {
                        val lblAddr = labelAddrMap[labels.first()]
                        if (lblAddr != null) {
                            val rs1 = binValues[0]
                            val x0 = Bin("0", Bit5())
                            val labelAddr = Bin(lblAddr, Bit32())
                            val imm12 = (labelAddr - instrStartAddress).toBin().getResized(Bit12()).shr(1).getRawBinStr()
                            val imm5 = Bin(imm12.substring(8) + imm12[1], Bit5())
                            val imm7 = Bin(imm12[0] + imm12.substring(2, 8), Bit7())
                            val bneOpCode = BNE.opCode?.getOpCode(mapOf(MaskLabel.RS1 to rs1, MaskLabel.RS2 to x0, MaskLabel.IMM7 to imm7, MaskLabel.IMM5 to imm5))

                            if (bneOpCode != null) {
                                binArray.add(bneOpCode)
                            }
                        }
                    }
                }

                Blez -> {
                    if (!binValues.isNullOrEmpty() && labels.isNotEmpty()) {
                        val lblAddr = labelAddrMap[labels.first()]
                        if (lblAddr != null) {
                            val rs1 = binValues[0]
                            val x0 = Bin("0", Bit5())
                            val labelAddr = Bin(lblAddr, Bit32())
                            val imm12 = (labelAddr - instrStartAddress).toBin().getResized(Bit12()).shr(1).getRawBinStr()
                            val imm5 = Bin(imm12.substring(8) + imm12[1], Bit5())
                            val imm7 = Bin(imm12[0] + imm12.substring(2, 8), Bit7())
                            val bgeOpCode = BGE.opCode?.getOpCode(mapOf(MaskLabel.RS1 to x0, MaskLabel.RS2 to rs1, MaskLabel.IMM7 to imm7, MaskLabel.IMM5 to imm5))

                            if (bgeOpCode != null) {
                                binArray.add(bgeOpCode)
                            }
                        }
                    }
                }

                Bgez -> {
                    if (!binValues.isNullOrEmpty() && labels.isNotEmpty()) {
                        val lblAddr = labelAddrMap[labels.first()]
                        if (lblAddr != null) {
                            val rs1 = binValues[0]
                            val x0 = Bin("0", Bit5())
                            val labelAddr = Bin(lblAddr, Bit32())
                            val imm12 = (labelAddr - instrStartAddress).toBin().getResized(Bit12()).shr(1).getRawBinStr()
                            val imm5 = Bin(imm12.substring(8) + imm12[1], Bit5())
                            val imm7 = Bin(imm12[0] + imm12.substring(2, 8), Bit7())
                            val bgeOpCode = BGE.opCode?.getOpCode(mapOf(MaskLabel.RS1 to rs1, MaskLabel.RS2 to x0, MaskLabel.IMM7 to imm7, MaskLabel.IMM5 to imm5))

                            if (bgeOpCode != null) {
                                binArray.add(bgeOpCode)
                            }
                        }
                    }
                }

                Bltz -> {
                    if (!binValues.isNullOrEmpty() && labels.isNotEmpty()) {
                        val lblAddr = labelAddrMap[labels.first()]
                        if (lblAddr != null) {
                            val rs1 = binValues[0]
                            val x0 = Bin("0", Bit5())
                            val labelAddr = Bin(lblAddr, Bit32())
                            val imm12 = (labelAddr - instrStartAddress).toBin().getResized(Bit12()).shr(1).getRawBinStr()
                            val imm5 = Bin(imm12.substring(8) + imm12[1], Bit5())
                            val imm7 = Bin(imm12[0] + imm12.substring(2, 8), Bit7())
                            val bltOpCode = BLT.opCode?.getOpCode(mapOf(MaskLabel.RS1 to rs1, MaskLabel.RS2 to x0, MaskLabel.IMM7 to imm7, MaskLabel.IMM5 to imm5))

                            if (bltOpCode != null) {
                                binArray.add(bltOpCode)
                            }
                        }
                    }
                }

                BGTZ -> {
                    if (!binValues.isNullOrEmpty() && labels.isNotEmpty()) {
                        val lblAddr = labelAddrMap[labels.first()]
                        if (lblAddr != null) {
                            val rs1 = binValues[0]
                            val x0 = Bin("0", Bit5())
                            val labelAddr = Bin(lblAddr, Bit32())
                            val imm12 = (labelAddr - instrStartAddress).toBin().getResized(Bit12()).shr(1).getRawBinStr()
                            val imm5 = Bin(imm12.substring(8) + imm12[1], Bit5())
                            val imm7 = Bin(imm12[0] + imm12.substring(2, 8), Bit7())
                            val bltOpCode = BLT.opCode?.getOpCode(mapOf(MaskLabel.RS1 to x0, MaskLabel.RS2 to rs1, MaskLabel.IMM7 to imm7, MaskLabel.IMM5 to imm5))

                            if (bltOpCode != null) {
                                binArray.add(bltOpCode)
                            }
                        }
                    }
                }

                Bgt -> {
                    if (!binValues.isNullOrEmpty() && labels.isNotEmpty()) {
                        val lblAddr = labelAddrMap[labels.first()]
                        if (lblAddr != null) {
                            val rs1 = binValues[0]
                            val rs2 = binValues[1]

                            val labelAddr = Bin(lblAddr, Bit32())
                            val imm12 = (labelAddr - instrStartAddress).toBin().getResized(Bit12()).shr(1).getRawBinStr()
                            val imm5 = Bin(imm12.substring(8) + imm12[1], Bit5())
                            val imm7 = Bin(imm12[0] + imm12.substring(2, 8), Bit7())

                            val bltOpCode = BLT.opCode?.getOpCode(mapOf(MaskLabel.RS1 to rs2, MaskLabel.RS2 to rs1, MaskLabel.IMM7 to imm7, MaskLabel.IMM5 to imm5))

                            if (bltOpCode != null) {
                                binArray.add(bltOpCode)
                            }
                        }
                    }
                }

                Ble -> {
                    if (!binValues.isNullOrEmpty() && labels.isNotEmpty()) {
                        val lblAddr = labelAddrMap[labels.first()]
                        if (lblAddr != null) {
                            val rs1 = binValues[0]
                            val rs2 = binValues[1]

                            val labelAddr = Bin(lblAddr, Bit32())
                            val imm12 = (labelAddr - instrStartAddress).toBin().getResized(Bit12()).shr(1).getRawBinStr()
                            val imm5 = Bin(imm12.substring(8) + imm12[1], Bit5())
                            val imm7 = Bin(imm12[0] + imm12.substring(2, 8), Bit7())

                            val bgeOpCode = BGE.opCode?.getOpCode(mapOf(MaskLabel.RS1 to rs2, MaskLabel.RS2 to rs1, MaskLabel.IMM7 to imm7, MaskLabel.IMM5 to imm5))

                            if (bgeOpCode != null) {
                                binArray.add(bgeOpCode)
                            }
                        }
                    }
                }

                Bgtu -> {
                    if (!binValues.isNullOrEmpty() && labels.isNotEmpty()) {
                        val lblAddr = labelAddrMap[labels.first()]
                        if (lblAddr != null) {
                            val rs1 = binValues[0]
                            val rs2 = binValues[1]

                            val labelAddr = Bin(lblAddr, Bit32())
                            val imm12 = (labelAddr - instrStartAddress).toBin().getResized(Bit12()).shr(1).getRawBinStr()
                            val imm5 = Bin(imm12.substring(8) + imm12[1], Bit5())
                            val imm7 = Bin(imm12[0] + imm12.substring(2, 8), Bit7())

                            val bltuOpCode = BLTU.opCode?.getOpCode(mapOf(MaskLabel.RS1 to rs2, MaskLabel.RS2 to rs1, MaskLabel.IMM7 to imm7, MaskLabel.IMM5 to imm5))

                            if (bltuOpCode != null) {
                                binArray.add(bltuOpCode)
                            }
                        }
                    }
                }

                Bleu -> {
                    if (!binValues.isNullOrEmpty() && labels.isNotEmpty()) {
                        val lblAddr = labelAddrMap[labels.first()]
                        if (lblAddr != null) {
                            val rs1 = binValues[0]
                            val rs2 = binValues[1]

                            val labelAddr = Bin(lblAddr, Bit32())
                            val imm12 = (labelAddr - instrStartAddress).toBin().getResized(Bit12()).shr(1).getRawBinStr()
                            val imm5 = Bin(imm12.substring(8) + imm12[1], Bit5())
                            val imm7 = Bin(imm12[0] + imm12.substring(2, 8), Bit7())

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

    fun getInstrFromBinary(bin: Bin): InstrResult? {
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

    data class InstrResult(val type: RV32Syntax.R_INSTR.InstrType, val binMap: Map<MaskLabel, Bin> = mapOf())
    class OpCode(val opMask: String, val maskLabels: Array<MaskLabel>) {

        val opMaskList = opMask.removePrefix(Settings.PRESTRING_BINARY).split(" ")
        fun checkOpCode(bin: Bin): CheckResult {
            if (bin.size != Bit32()) {
                return CheckResult(false)
            }
            // Check OpCode
            val binaryString = bin.getRawBinStr()
            val binaryOpCode = binaryString.substring(binaryString.length - 7)
            val originalOpCode = getMaskString(MaskLabel.OPCODE)
            if (originalOpCode.isNotEmpty()) {
                if (binaryOpCode == originalOpCode) {
                    // check static labels
                    val binMap = mutableMapOf<MaskLabel, Bin>()
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
                                binMap[label] = Bin(substring, label.maxSize)
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

        fun getOpCode(parameterMap: Map<MaskLabel, Bin>): Bin? {
            val opCode = opMaskList.toMutableList()
            var length = 0
            opCode.forEach { length += it.length }
            if (length != Bit32().bitWidth) {
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
                    val param = parameterMap[maskLabel]
                    if (param != null) {
                        val size = maskLabel.maxSize
                        if (size != null) {
                            opCode[labelID] = param.getUResized(size).getRawBinStr()
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

            return Bin(opCode.joinToString("") { it }, Bit32())
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

        data class CheckResult(val matches: Boolean, val binMap: Map<MaskLabel, Bin> = mapOf())
    }

    enum class MaskLabel(val static: Boolean, val maxSize: Variable.Size? = null) {
        OPCODE(true, Bit7()),
        RD(false, Bit5()),
        FUNCT3(true, Bit3()),
        RS1(false, Bit5()),
        RS2(false, Bit5()),
        SHAMT(false, Bit5()),
        FUNCT7(true, Bit7()),
        IMM5(false, Bit5()),
        UIMM5(false, Bit5()),
        IMM7(false, Bit7()),
        IMM12(false, Bit12()),
        IMM20(false, Bit20()),
        CSR(false, Bit12()),
        NONE(true)
    }
}