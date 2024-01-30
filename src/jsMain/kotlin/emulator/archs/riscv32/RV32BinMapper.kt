package emulator.archs.riscv32

import emulator.kit.Settings
import emulator.archs.riscv32.RV32Syntax.InstrType.*
import emulator.kit.types.Variable
import debug.DebugTools
import emulator.kit.Architecture
import emulator.kit.types.Variable.Value.*
import emulator.kit.types.Variable.Size.*


/**
 *  Converts instructions with its parameters between Text and binary format.
 */
class RV32BinMapper {

    fun getBinaryFromNewInstrDef(instr: RV32Syntax.EInstr, architecture: Architecture): Array<Bin> {
        val binArray = mutableListOf<Bin>()
        val instrAddr = instr.address ?: return emptyArray()
        val regs = instr.registers.map { it.reg.address.toBin() }
        val labels = instr.linkedLabels.mapNotNull { it.address?.toBin() }

        if (DebugTools.RV32_showBinMapperInfo) {
            console.log("BinMapper.getBinaryFromInstrDef(): \t${instr.type.id} -> values: ${instr.params.joinToString { it.content }}")
        }

        try {
            when (instr.type) {
                LUI, AUIPC -> {
                    val imm20 = instr.constants.first().getValue(Bit20()).toBin()
                    val opCode = instr.type.opCode?.getOpCode(mapOf(MaskLabel.RD to regs[0], MaskLabel.IMM20 to imm20))
                    opCode?.let {
                        binArray.add(opCode)
                    }
                }

                JAL -> {
                    val immediate = instr.constants.first().getValue(Bit20())
                    val imm20toWork = immediate.toBin().getRawBinStr()

                    /**
                     *      RV32IDOC Index   20 19 18 17 16 15 14 13 12 11 10  9  8  7  6  5  4  3  2  1
                     *        String Index    0  1  2  3  4  5  6  7  8  9 10 11 12 13 14 15 16 17 18 19
                     */

                    val imm20 = Bin(imm20toWork[0].toString() + imm20toWork.substring(10) + imm20toWork[9] + imm20toWork.substring(1, 9), Bit20())
                    val opCode = instr.type.opCode?.getOpCode(mapOf(MaskLabel.RD to regs[0], MaskLabel.IMM20 to imm20))
                    opCode?.let {
                        binArray.add(opCode)
                    }
                }

                JALR -> {
                    val immediate = instr.constants.first().getValue(Bit12())
                    val opCode = instr.type.opCode?.getOpCode(mapOf(MaskLabel.RD to regs[0], MaskLabel.IMM12 to immediate.toBin(), MaskLabel.RS1 to regs[1]))
                    opCode?.let {
                        binArray.add(opCode)
                    }
                }

                EBREAK, ECALL -> {
                    val opCode = instr.type.opCode?.getOpCode(mapOf())
                    opCode?.let {
                        binArray.add(opCode)
                    }
                }

                BEQ, BNE, BLT, BGE, BLTU, BGEU -> {
                    val immediate = instr.constants.first().getValue(Bit12())
                    val imm12 = immediate.toBin().getRawBinStr()
                    val imm5 = Bin(imm12.substring(8) + imm12[1], Bit5())
                    val imm7 = Bin(imm12[0] + imm12.substring(2, 8), Bit7())

                    val opCode = instr.type.opCode?.getOpCode(mapOf(MaskLabel.RS1 to regs[0], MaskLabel.RS2 to regs[1], MaskLabel.IMM5 to imm5, MaskLabel.IMM7 to imm7))
                    opCode?.let {
                        binArray.add(opCode)
                    }
                }

                BEQ1, BNE1, BLT1, BGE1, BLTU1, BGEU1 -> {
                    val lblAddr = labels.first()
                    val imm12offset = (lblAddr - instrAddr).toBin().getResized(Bit12()).shr(1).getRawBinStr()
                    val imm5 = Bin(imm12offset.substring(8) + imm12offset[1], Bit5())
                    val imm7 = Bin(imm12offset[0] + imm12offset.substring(2, 8), Bit7())

                    val opCode = instr.type.relative?.opCode?.getOpCode(mapOf(MaskLabel.RS1 to regs[0], MaskLabel.RS2 to regs[1], MaskLabel.IMM5 to imm5, MaskLabel.IMM7 to imm7))
                    opCode?.let {
                        binArray.add(opCode)
                    }
                }

                LB, LH, LW, LBU, LHU -> {
                    val immediate = instr.constants.first().getValue(Bit12())
                    val opCode = instr.type.opCode?.getOpCode(mapOf(MaskLabel.RD to regs[0], MaskLabel.IMM12 to immediate.toBin(), MaskLabel.RS1 to regs[1]))
                    opCode?.let {
                        binArray.add(opCode)
                    }
                }

                SB, SH, SW -> {
                    val immediate = instr.constants.first().getValue(Bit12())
                    val imm12 = immediate.toBin().getRawBinStr()
                    val imm5 = Bin(imm12.substring(imm12.length - 5))
                    val imm7 = Bin(imm12.substring(imm12.length - 12, imm12.length - 5))

                    val opCode = instr.type.opCode?.getOpCode(mapOf(MaskLabel.RS2 to regs[0], MaskLabel.IMM7 to imm7, MaskLabel.IMM5 to imm5, MaskLabel.RS1 to regs[1]))
                    opCode?.let {
                        binArray.add(opCode)
                    }
                }

                ADDI, SLTI, SLTIU, XORI, ORI, ANDI -> {
                    val imm12 = instr.constants.first().getValue(Bit12()).toBin()
                    val opCode = instr.type.opCode?.getOpCode(mapOf(MaskLabel.RD to regs[0], MaskLabel.RS1 to regs[1], MaskLabel.IMM12 to imm12))
                    opCode?.let {
                        binArray.add(opCode)
                    }
                }

                SLLI, SRLI, SRAI -> {
                    val imm5 = instr.constants.first().getValue(Bit5()).toBin()
                    val opCode = instr.type.opCode?.getOpCode(mapOf(MaskLabel.RD to regs[0], MaskLabel.RS1 to regs[1], MaskLabel.SHAMT to imm5))
                    opCode?.let {
                        binArray.add(opCode)
                    }
                }

                ADD, SUB, SLL, SLT, SLTU, XOR, SRL, SRA, OR, AND, MUL, MULH, MULHSU, MULHU, DIV, DIVU, REM, REMU -> {
                    val opCode = instr.type.opCode?.getOpCode(mapOf(MaskLabel.RD to regs[0], MaskLabel.RS1 to regs[1], MaskLabel.RS2 to regs[2]))
                    opCode?.let {
                        binArray.add(opCode)
                    }
                }

                CSRRW, CSRRS, CSRRC -> {
                    val csrAddr = if (instr.constants.size == 1) {
                        instr.constants.first().getValue(Bit12()).toBin()
                    } else {
                        regs[1].toBin()
                    }
                    val opCode = instr.type.opCode?.getOpCode(mapOf(MaskLabel.RD to regs[0], MaskLabel.CSR to csrAddr, MaskLabel.RS1 to regs.last()))
                    opCode?.let {
                        binArray.add(opCode)
                    }
                }

                CSRRWI, CSRRSI, CSRRCI -> {
                    val csrAddr = if (instr.constants.size == 1) {
                        instr.constants.first().getValue(Bit12()).toBin()
                    } else {
                        regs[1].toBin()
                    }
                    val immediate = instr.constants.toList()[1].getValue(Bit5())
                    val uimm5 = immediate.toBin()
                    val opCode = instr.type.opCode?.getOpCode(mapOf(MaskLabel.RD to regs[0], MaskLabel.CSR to csrAddr, MaskLabel.UIMM5 to uimm5))
                    opCode?.let {
                        binArray.add(opCode)
                    }
                }

                CSRW -> {
                    val csrAddr = if (instr.constants.size == 1) {
                        instr.constants.first().getValue(Bit12()).toBin()
                    } else {
                        regs[0].toBin()
                    }
                    val zero = Bin("0", Bit5())
                    val opCode = CSRRW.opCode?.getOpCode(mapOf(MaskLabel.RD to zero, MaskLabel.CSR to csrAddr, MaskLabel.RS1 to regs.last()))
                    opCode?.let {
                        binArray.add(opCode)
                    }
                }

                CSRR -> {
                    val csrAddr = if (instr.constants.size == 1) {
                        instr.constants.first().getValue(Bit12()).toBin()
                    } else {
                        regs[1].toBin()
                    }
                    val zero = Bin("0", Bit5())
                    val opCode = CSRRS.opCode?.getOpCode(mapOf(MaskLabel.RD to regs[0], MaskLabel.CSR to csrAddr, MaskLabel.RS1 to zero))
                    opCode?.let {
                        binArray.add(opCode)
                    }
                }

                Li -> {
                    val imm = instr.constants.first().getValue(Bit32())
                    val hi20 = imm.toBin().getRawBinStr().substring(0, 20)
                    val low12 = imm.toBin().getRawBinStr().substring(20)

                    val imm12 = Bin(low12, Bit12())

                    val imm20temp = (Bin(hi20, Bit20())).toBin() // more performant
                    val imm20 = if (imm12.toDec().isNegative()) {
                        (imm20temp + Bin("1")).toBin()
                    } else {
                        imm20temp
                    }

                    val luiOpCode = LUI.opCode?.getOpCode(mapOf(MaskLabel.RD to regs[0], MaskLabel.IMM20 to imm20))
                    val addiOpCode = ADDI.opCode?.getOpCode(mapOf(MaskLabel.RD to regs[0], MaskLabel.RS1 to regs[0], MaskLabel.IMM12 to imm12))

                    if (luiOpCode != null && addiOpCode != null) {
                        binArray.add(luiOpCode)
                        binArray.add(addiOpCode)
                    }
                }

                La -> {
                    val regBin = regs[0]
                    val address = labels.first()
                    val hi20 = address.getRawBinStr().substring(0, 20)
                    val low12 = address.getRawBinStr().substring(20)

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

                JAL1 -> {
                    val lblAddr = labels.first()
                    val rd = regs[0]
                    val imm20toWork = ((lblAddr - instrAddr).toBin() shr 1).getResized(Bit20()).getRawBinStr()

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

                JAL2 -> {
                    val lblAddr = labels.first()
                    val rd = Bin("1", Bit5())
                    val imm20toWork = ((lblAddr - instrAddr).toBin() shr 1).getResized(Bit20()).getRawBinStr()

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

                J -> {
                    val lblAddr = labels.first()
                    val rd = Bin("0", Bit5())
                    val imm20toWork = ((lblAddr - instrAddr).toBin() shr 1).getResized(Bit20()).getRawBinStr()

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

                Jr -> {
                    val rs1 = regs[0]
                    val x0 = Bin("0", Bit5())
                    val zero = Bin("0", Bit12())

                    val jalrOpCode = JALR.opCode?.getOpCode(mapOf(MaskLabel.RD to x0, MaskLabel.RS1 to rs1, MaskLabel.IMM12 to zero))

                    if (jalrOpCode != null) {
                        binArray.add(jalrOpCode)
                    }
                }

                JALR1 -> {
                    val rs1 = regs[0]
                    val x1 = Bin("1", Bit5())
                    val zero = Bin("0", Bit12())

                    val jalrOpCode = JALR.opCode?.getOpCode(mapOf(MaskLabel.RD to x1, MaskLabel.RS1 to rs1, MaskLabel.IMM12 to zero))

                    if (jalrOpCode != null) {
                        binArray.add(jalrOpCode)
                    }
                }

                JALR2 -> {
                    val opCode = JALR.opCode?.getOpCode(mapOf(MaskLabel.RD to regs[0], MaskLabel.IMM12 to regs[1], MaskLabel.RS1 to regs[2]))
                    opCode?.let {
                        binArray.add(opCode)
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
                    val lblAddr = labels.first()
                    val x1 = Bin("1", Bit5())

                    val pcRelAddress32 = (lblAddr - instrAddr).toBin()
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

                Tail -> {
                    val lblAddr = labels.first()
                    val x0 = Bin("0", Bit5())
                    val x6 = Hex("6", Bit5()).toBin()

                    val pcRelAddress32 = (lblAddr - instrAddr).toBin()
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

                Mv -> {
                    val rd = regs[0]
                    val rs1 = regs[1]
                    val zero = Bin("0", Bit12())

                    val addiOpCode = ADDI.opCode?.getOpCode(mapOf(MaskLabel.RD to rd, MaskLabel.RS1 to rs1, MaskLabel.IMM12 to zero))

                    if (addiOpCode != null) {
                        binArray.add(addiOpCode)
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
                    val rd = regs[0]
                    val rs1 = regs[1]

                    val xoriOpCode = XORI.opCode?.getOpCode(mapOf(MaskLabel.RD to rd, MaskLabel.RS1 to rs1, MaskLabel.IMM12 to Bin("1".repeat(12), Bit12())))

                    if (xoriOpCode != null) {
                        binArray.add(xoriOpCode)
                    }
                }

                Neg -> {
                    val rd = regs[0]
                    val rs1 = Bin("0", Bit5())
                    val rs2 = regs[1]

                    val subOpCode = SUB.opCode?.getOpCode(mapOf(MaskLabel.RD to rd, MaskLabel.RS1 to rs1, MaskLabel.RS2 to rs2))

                    if (subOpCode != null) {
                        binArray.add(subOpCode)
                    }
                }

                Seqz -> {
                    val rd = regs[0]
                    val rs1 = regs[1]
                    val imm12 = Bin("1", Bit12())

                    val sltiuOpCode = SLTIU.opCode?.getOpCode(mapOf(MaskLabel.RD to rd, MaskLabel.RS1 to rs1, MaskLabel.IMM12 to imm12))

                    if (sltiuOpCode != null) {
                        binArray.add(sltiuOpCode)
                    }
                }

                Snez -> {
                    val rd = regs[0]
                    val rs1 = Bin("0", Bit5())
                    val rs2 = regs[1]

                    val sltuOpCode = SLTU.opCode?.getOpCode(mapOf(MaskLabel.RD to rd, MaskLabel.RS1 to rs1, MaskLabel.RS2 to rs2))

                    if (sltuOpCode != null) {
                        binArray.add(sltuOpCode)
                    }
                }

                Sltz -> {
                    val rd = regs[0]
                    val rs1 = regs[1]
                    val zero = Bin("0", Bit12())

                    val sltOpCode = SLT.opCode?.getOpCode(mapOf(MaskLabel.RD to rd, MaskLabel.RS1 to rs1, MaskLabel.RS2 to zero))

                    if (sltOpCode != null) {
                        binArray.add(sltOpCode)
                    }
                }

                Sgtz -> {
                    val rd = regs[0]
                    val rs1 = Bin("0", Bit5())
                    val rs2 = regs[1]

                    val sltOpCode = SLT.opCode?.getOpCode(mapOf(MaskLabel.RD to rd, MaskLabel.RS1 to rs1, MaskLabel.RS2 to rs2))

                    if (sltOpCode != null) {
                        binArray.add(sltOpCode)
                    }
                }

                Beqz -> {
                    val lblAddr = labels.first()
                    val rs1 = regs[0]
                    val x0 = Bin("0", Bit5())
                    val imm12 = (lblAddr - instrAddr).toBin().getResized(Bit12()).shr(1).getRawBinStr()
                    val imm5 = Bin(imm12.substring(8) + imm12[1], Bit5())
                    val imm7 = Bin(imm12[0] + imm12.substring(2, 8), Bit7())
                    val beqOpCode = BEQ.opCode?.getOpCode(mapOf(MaskLabel.RS1 to rs1, MaskLabel.RS2 to x0, MaskLabel.IMM7 to imm7, MaskLabel.IMM5 to imm5))

                    if (beqOpCode != null) {
                        binArray.add(beqOpCode)
                    }
                }

                Bnez -> {
                    val lblAddr = labels.first()
                    val rs1 = regs[0]
                    val x0 = Bin("0", Bit5())
                    val imm12 = (lblAddr - instrAddr).toBin().getResized(Bit12()).shr(1).getRawBinStr()
                    val imm5 = Bin(imm12.substring(8) + imm12[1], Bit5())
                    val imm7 = Bin(imm12[0] + imm12.substring(2, 8), Bit7())
                    val bneOpCode = BNE.opCode?.getOpCode(mapOf(MaskLabel.RS1 to rs1, MaskLabel.RS2 to x0, MaskLabel.IMM7 to imm7, MaskLabel.IMM5 to imm5))

                    if (bneOpCode != null) {
                        binArray.add(bneOpCode)
                    }
                }

                Blez -> {
                    val lblAddr = labels.first()
                    val rs1 = regs[0]
                    val x0 = Bin("0", Bit5())
                    val imm12 = (lblAddr - instrAddr).toBin().getResized(Bit12()).shr(1).getRawBinStr()
                    val imm5 = Bin(imm12.substring(8) + imm12[1], Bit5())
                    val imm7 = Bin(imm12[0] + imm12.substring(2, 8), Bit7())
                    val bgeOpCode = BGE.opCode?.getOpCode(mapOf(MaskLabel.RS1 to x0, MaskLabel.RS2 to rs1, MaskLabel.IMM7 to imm7, MaskLabel.IMM5 to imm5))

                    if (bgeOpCode != null) {
                        binArray.add(bgeOpCode)
                    }
                }

                Bgez -> {
                    val lblAddr = labels.first()
                    val rs1 = regs[0]
                    val x0 = Bin("0", Bit5())

                    val imm12 = (lblAddr - instrAddr).toBin().getResized(Bit12()).shr(1).getRawBinStr()
                    val imm5 = Bin(imm12.substring(8) + imm12[1], Bit5())
                    val imm7 = Bin(imm12[0] + imm12.substring(2, 8), Bit7())
                    val bgeOpCode = BGE.opCode?.getOpCode(mapOf(MaskLabel.RS1 to rs1, MaskLabel.RS2 to x0, MaskLabel.IMM7 to imm7, MaskLabel.IMM5 to imm5))

                    if (bgeOpCode != null) {
                        binArray.add(bgeOpCode)
                    }
                }

                Bltz -> {
                    val lblAddr = labels.first()
                    val rs1 = regs[0]
                    val x0 = Bin("0", Bit5())
                    val imm12 = (lblAddr - instrAddr).toBin().getResized(Bit12()).shr(1).getRawBinStr()
                    val imm5 = Bin(imm12.substring(8) + imm12[1], Bit5())
                    val imm7 = Bin(imm12[0] + imm12.substring(2, 8), Bit7())
                    val bltOpCode = BLT.opCode?.getOpCode(mapOf(MaskLabel.RS1 to rs1, MaskLabel.RS2 to x0, MaskLabel.IMM7 to imm7, MaskLabel.IMM5 to imm5))

                    if (bltOpCode != null) {
                        binArray.add(bltOpCode)
                    }
                }

                BGTZ -> {
                    val lblAddr = labels.first()
                    val rs1 = regs[0]
                    val x0 = Bin("0", Bit5())
                    val imm12 = (lblAddr - instrAddr).toBin().getResized(Bit12()).shr(1).getRawBinStr()
                    val imm5 = Bin(imm12.substring(8) + imm12[1], Bit5())
                    val imm7 = Bin(imm12[0] + imm12.substring(2, 8), Bit7())
                    val bltOpCode = BLT.opCode?.getOpCode(mapOf(MaskLabel.RS1 to x0, MaskLabel.RS2 to rs1, MaskLabel.IMM7 to imm7, MaskLabel.IMM5 to imm5))

                    if (bltOpCode != null) {
                        binArray.add(bltOpCode)
                    }
                }

                Bgt -> {
                    val lblAddr = labels.first()
                    val rs1 = regs[0]
                    val rs2 = regs[1]
                    val imm12 = (lblAddr - instrAddr).toBin().getResized(Bit12()).shr(1).getRawBinStr()
                    val imm5 = Bin(imm12.substring(8) + imm12[1], Bit5())
                    val imm7 = Bin(imm12[0] + imm12.substring(2, 8), Bit7())

                    val bltOpCode = BLT.opCode?.getOpCode(mapOf(MaskLabel.RS1 to rs2, MaskLabel.RS2 to rs1, MaskLabel.IMM7 to imm7, MaskLabel.IMM5 to imm5))

                    if (bltOpCode != null) {
                        binArray.add(bltOpCode)
                    }
                }

                Ble -> {
                    val lblAddr = labels.first()
                    val rs1 = regs[0]
                    val rs2 = regs[1]
                    val imm12 = (lblAddr - instrAddr).toBin().getResized(Bit12()).shr(1).getRawBinStr()
                    val imm5 = Bin(imm12.substring(8) + imm12[1], Bit5())
                    val imm7 = Bin(imm12[0] + imm12.substring(2, 8), Bit7())

                    val bgeOpCode = BGE.opCode?.getOpCode(mapOf(MaskLabel.RS1 to rs2, MaskLabel.RS2 to rs1, MaskLabel.IMM7 to imm7, MaskLabel.IMM5 to imm5))

                    if (bgeOpCode != null) {
                        binArray.add(bgeOpCode)
                    }
                }

                Bgtu -> {
                    val lblAddr = labels.first()
                    val rs1 = regs[0]
                    val rs2 = regs[1]
                    val imm12 = (lblAddr - instrAddr).toBin().getResized(Bit12()).shr(1).getRawBinStr()
                    val imm5 = Bin(imm12.substring(8) + imm12[1], Bit5())
                    val imm7 = Bin(imm12[0] + imm12.substring(2, 8), Bit7())

                    val bltuOpCode = BLTU.opCode?.getOpCode(mapOf(MaskLabel.RS1 to rs2, MaskLabel.RS2 to rs1, MaskLabel.IMM7 to imm7, MaskLabel.IMM5 to imm5))

                    if (bltuOpCode != null) {
                        binArray.add(bltuOpCode)
                    }
                }

                Bleu -> {
                    val lblAddr = labels.first()
                    val rs1 = regs[0]
                    val rs2 = regs[1]
                    val imm12 = (lblAddr - instrAddr).toBin().getResized(Bit12()).shr(1).getRawBinStr()
                    val imm5 = Bin(imm12.substring(8) + imm12[1], Bit5())
                    val imm7 = Bin(imm12[0] + imm12.substring(2, 8), Bit7())

                    val bgeuOpCode = BGEU.opCode?.getOpCode(mapOf(MaskLabel.RS1 to rs2, MaskLabel.RS2 to rs1, MaskLabel.IMM7 to imm7, MaskLabel.IMM5 to imm5))

                    if (bgeuOpCode != null) {
                        binArray.add(bgeuOpCode)
                    }
                }
            }
        } catch (e: Exception) {
            architecture.getConsole().error(e.message.toString())
        }

        if (binArray.isEmpty()) {
            architecture.getConsole().error("BinMapper: values and labels not matching for ${instr.type::class.simpleName}!")
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

    data class InstrResult(val type: RV32Syntax.InstrType, val binMap: Map<MaskLabel, Bin> = mapOf())
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