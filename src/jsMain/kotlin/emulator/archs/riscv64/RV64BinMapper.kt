package emulator.archs.riscv64

import debug.DebugTools
import emulator.kit.Architecture
import emulator.kit.Settings
import emulator.kit.types.Variable
import emulator.kit.types.Variable.Value.*
import emulator.kit.types.Variable.Size.*
import emulator.archs.riscv64.RV64Syntax.InstrType
import emulator.archs.riscv64.RV64Syntax.InstrType.*

class RV64BinMapper {

    fun getBinaryFromNewInstrDef(instr: RV64Syntax.EInstr, architecture: Architecture): Array<Bin> {
        val binArray = mutableListOf<Bin>()
        val instrAddr = instr.address ?: return emptyArray()
        val regs = instr.registers.map { it.reg.address.toBin() }
        val labels = instr.linkedLabels.mapNotNull { it.address?.toBin() }
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
                    val imm = instr.constants.first().getValue(Bit20()).toBin()
                    val imm20toWork = imm.getRawBinStr()

                    /**
                     *      RV64IDOC Index   20 19 18 17 16 15 14 13 12 11 10  9  8  7  6  5  4  3  2  1
                     *        String Index    0  1  2  3  4  5  6  7  8  9 10 11 12 13 14 15 16 17 18 19
                     */

                    val imm20 = Bin(imm20toWork[0].toString() + imm20toWork.substring(10) + imm20toWork[9] + imm20toWork.substring(1, 9), Bit20())
                    val opCode = instr.type.opCode?.getOpCode(mapOf(MaskLabel.RD to regs[0], MaskLabel.IMM20 to imm20))
                    opCode?.let {
                        binArray.add(opCode)
                    }
                }

                JALR -> {
                    val imm = instr.constants.first().getValue(Bit12()).toBin()
                    val opCode = instr.type.opCode?.getOpCode(mapOf(MaskLabel.RD to regs[0], MaskLabel.IMM12 to imm, MaskLabel.RS1 to regs[1]))
                    opCode?.let {
                        binArray.add(opCode)
                    }
                }

                ECALL, EBREAK -> {
                    val opCode = instr.type.opCode?.getOpCode(mapOf())
                    opCode?.let {
                        binArray.add(opCode)
                    }
                }

                BEQ, BNE, BLT, BGE, BLTU, BGEU -> {
                    val imm = instr.constants.first().getValue(Bit12()).toBin()
                    val imm12 = imm.getRawBinStr()
                    val imm5 = Bin(imm12.substring(8) + imm12[1], Bit5())
                    val imm7 = Bin(imm12[0] + imm12.substring(2, 8), Bit7())

                    val opCode = instr.type.opCode?.getOpCode(mapOf(MaskLabel.RS1 to regs[0], MaskLabel.RS2 to regs[1], MaskLabel.IMM5 to imm5, MaskLabel.IMM7 to imm7))
                    opCode?.let {
                        binArray.add(opCode)
                    }
                }

                BEQ1, BNE1, BLT1, BGE1, BLTU1, BGEU1 -> {
                    val labelAddr = labels.first()
                    val offset = labelAddr - instrAddr
                    val imm12offset = offset.toBin().getResized(Bit12()).shr(1).getRawBinStr()
                    val imm5 = Bin(imm12offset.substring(8) + imm12offset[1], Bit5())
                    val imm7 = Bin(imm12offset[0] + imm12offset.substring(2, 8), Bit7())

                    val opCode = instr.type.relative?.opCode?.getOpCode(mapOf(MaskLabel.RS1 to regs[0], MaskLabel.RS2 to regs[1], MaskLabel.IMM5 to imm5, MaskLabel.IMM7 to imm7))
                    opCode?.let {
                        binArray.add(opCode)
                    }
                }

                LB, LH, LW, LD, LBU, LHU, LWU -> {
                    val imm = instr.constants.first().getValue(Bit12()).toBin()
                    val opCode = instr.type.opCode?.getOpCode(mapOf(MaskLabel.RD to regs[0], MaskLabel.IMM12 to imm, MaskLabel.RS1 to regs[1]))
                    opCode?.let {
                        binArray.add(opCode)
                    }
                }

                SB, SH, SW, SD -> {
                    val imm = instr.constants.first().getValue(Bit12()).toBin()
                    val imm12 = imm.getRawBinStr()
                    val imm5 = Bin(imm12.substring(imm12.length - 5))
                    val imm7 = Bin(imm12.substring(imm12.length - 12, imm12.length - 5))

                    val opCode = instr.type.opCode?.getOpCode(mapOf(MaskLabel.RS2 to regs[0], MaskLabel.IMM7 to imm7, MaskLabel.IMM5 to imm5, MaskLabel.RS1 to regs[1]))
                    opCode?.let {
                        binArray.add(opCode)
                    }
                }

                ADDI, ADDIW, SLTI, SLTIU, XORI, ORI, ANDI -> {
                    val imm = instr.constants.first().getValue(Bit12()).toBin()
                    val opCode = instr.type.opCode?.getOpCode(mapOf(MaskLabel.RD to regs[0], MaskLabel.RS1 to regs[1], MaskLabel.IMM12 to imm))
                    opCode?.let {
                        binArray.add(opCode)
                    }
                }

                SLLI, SLLIW, SRLI, SRLIW, SRAI, SRAIW -> {
                    val imm = instr.constants.first().getValue(Bit6()).toBin()
                    val opCode = instr.type.opCode?.getOpCode(mapOf(MaskLabel.RD to regs[0], MaskLabel.RS1 to regs[1], MaskLabel.SHAMT6 to imm))
                    opCode?.let {
                        binArray.add(opCode)
                    }
                }

                ADD, ADDW, SUB, SUBW, SLL, SLLW, SLT, SLTU, XOR, SRL, SRLW, SRA, SRAW, OR, AND, MUL, MULH, MULHSU, MULHU, DIV, DIVU, REM, REMU, MULW, DIVW, DIVUW, REMW, REMUW -> {
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
                    val uimm5 = instr.constants.last().getValue(Bit5()).toBin()
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
                    val opCode = CSRRW.opCode?.getOpCode(mapOf(MaskLabel.RD to zero, MaskLabel.CSR to csrAddr, MaskLabel.RS1 to regs[1]))
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

                Nop -> {
                    val zero = Bin("0", Bit5())
                    val imm12 = Bin("0", Bit12())
                    val addiOpCode = ADDI.opCode?.getOpCode(mapOf(MaskLabel.RD to zero, MaskLabel.RS1 to zero, MaskLabel.IMM12 to imm12))

                    if (addiOpCode != null) {
                        binArray.add(addiOpCode)
                    }
                }

                Mv -> {
                    val zero = Bin("0", Bit12())

                    val addiOpCode = ADDI.opCode?.getOpCode(mapOf(MaskLabel.RD to regs[0], MaskLabel.RS1 to regs[1], MaskLabel.IMM12 to zero))

                    if (addiOpCode != null) {
                        binArray.add(addiOpCode)
                    }
                }

                Li28Unsigned -> {
                    /**
                     * split into 4 bit
                     *
                     *  SIGN-C----A----F----E----A----F----F---
                     *  xxxx-xxxx-xxxx-xxxx-xxxx-xxxx-xxxx-xxxx
                     * |0000-xxxx-xxxx-xxxx-xxxx|xxxx-xxxx-xxxx|
                     *            LUI                ORI
                     *
                     */


                    val imm28 = instr.constants.first().getValue(Bit28()).toBin()
                    if (!imm28.checkResult.valid) {
                        architecture.getConsole().error("RV64 Syntax Issue - value exceeds maximum size! [Instr: ${instr.type.name}]\n${imm28.checkResult.message}")
                    }

                    val lui16 = imm28.getRawBinStr().substring(0, 16)
                    val ori12first = imm28.getRawBinStr().substring(16, 28)

                    val lui16_imm20 = Bin(lui16, Bit16()).getUResized(Bit20())
                    val ori12first_imm = Bin(ori12first, Bit12())

                    val luiOpCode = LUI.opCode?.getOpCode(mapOf(MaskLabel.RD to regs[0], MaskLabel.IMM20 to lui16_imm20))
                    val oriOpCode = ORI.opCode?.getOpCode(mapOf(MaskLabel.RD to regs[0], MaskLabel.RS1 to regs[0], MaskLabel.IMM12 to ori12first_imm))

                    if (luiOpCode != null && oriOpCode != null) {
                        binArray.add(luiOpCode)
                        binArray.add(oriOpCode)
                    }
                }

                Li32Signed -> {
                    val immediate = instr.constants.first().getValue(Bit32()).toBin()
                    val imm32 = immediate.getUResized(Bit32())
                    if (!imm32.checkResult.valid) {
                        architecture.getConsole().error("RV64 Syntax Issue - value exceeds maximum size! [Instr: ${instr.type.name}]\n${imm32.checkResult.message}")
                    }

                    val hi20 = imm32.getRawBinStr().substring(0, 20)
                    val low12 = imm32.getRawBinStr().substring(20)

                    val imm12 = Bin(low12, Bit12())
                    val imm20 = Bin(hi20, Bit20())

                    val luiOpCode = LUI.opCode?.getOpCode(mapOf(MaskLabel.RD to regs[0], MaskLabel.IMM20 to imm20))
                    val oriOpCode = ORI.opCode?.getOpCode(mapOf(MaskLabel.RD to regs[0], MaskLabel.RS1 to regs[0], MaskLabel.IMM12 to imm12))

                    if (luiOpCode != null && oriOpCode != null) {
                        binArray.add(luiOpCode)
                        binArray.add(oriOpCode)
                    }
                }

                Li40Unsigned -> {
                    /**
                     * split into 4 bit
                     *
                     *  SIGN-C----A----F----E----A----F----F----E----D----E---
                     *  xxxx-xxxx-xxxx-xxxx-xxxx-xxxx-xxxx-xxxx-xxxx-xxxx-xxxx
                     * |0000-xxxx-xxxx-xxxx-xxxx|xxxx-xxxx-xxxx|xxxx-xxxx-xxxx|
                     *            LUI                 ORI            ORI
                     *
                     */
                    val imm40 = instr.constants.first().getValue(Bit40()).toBin()
                    if (!imm40.checkResult.valid) {
                        architecture.getConsole().error("RV64 Syntax Issue - value exceeds maximum size! [Instr: ${instr.type.name}]\n${imm40.checkResult.message}")
                    }

                    val lui16 = imm40.getRawBinStr().substring(0, 16)
                    val ori12first = imm40.getRawBinStr().substring(16, 28)
                    val ori12sec = imm40.getRawBinStr().substring(28, 40)

                    val lui16_imm20 = Bin(lui16, Bit16()).getUResized(Bit20())
                    val ori12first_imm = Bin(ori12first, Bit12())
                    val ori12sec_imm = Bin(ori12sec, Bit12())

                    val luiOpCode = LUI.opCode?.getOpCode(mapOf(MaskLabel.RD to regs[0], MaskLabel.IMM20 to lui16_imm20))
                    val oriFirstOpCode = ORI.opCode?.getOpCode(mapOf(MaskLabel.RD to regs[0], MaskLabel.RS1 to regs[0], MaskLabel.IMM12 to ori12first_imm))
                    val oriSecOpCode = ORI.opCode?.getOpCode(mapOf(MaskLabel.RD to regs[0], MaskLabel.RS1 to regs[0], MaskLabel.IMM12 to ori12sec_imm))


                    val slli12Bit = SLLI.opCode?.getOpCode(mapOf(MaskLabel.RD to regs[0], MaskLabel.RS1 to regs[0], MaskLabel.SHAMT6 to Bin("001100", Bit6())))

                    if (luiOpCode != null && oriFirstOpCode != null && slli12Bit != null && oriSecOpCode != null) {
                        binArray.add(luiOpCode)
                        binArray.add(oriFirstOpCode)
                        binArray.add(slli12Bit)
                        binArray.add(oriSecOpCode)
                    }
                }

                Li52Unsigned -> {
                    /**
                     * split into 4 bit
                     *
                     *  SIGN-C----A----F----E----A----F----F----E----D----E----A----D----B---
                     *  xxxx-xxxx-xxxx-xxxx-xxxx-xxxx-xxxx-xxxx-xxxx-xxxx-xxxx-xxxx-xxxx-xxxx
                     * |0000-xxxx-xxxx-xxxx-xxxx|xxxx-xxxx-xxxx|xxxx-xxxx-xxxx|xxxx-xxxx-xxxx
                     *            LUI                 ORI            ORI            ORI
                     *
                     */
                    val imm52 = instr.constants.first().getValue(Bit52()).toBin()
                    if (!imm52.checkResult.valid) {
                        architecture.getConsole().error("RV64 Syntax Issue - value exceeds maximum size! [Instr: ${instr.type.name}]\n${imm52.checkResult.message}")
                    }


                    val lui16 = imm52.getRawBinStr().substring(0, 16)
                    val ori12first = imm52.getRawBinStr().substring(16, 28)
                    val ori12sec = imm52.getRawBinStr().substring(28, 40)
                    val ori12third = imm52.getRawBinStr().substring(40, 52)

                    val lui16_imm20 = Bin(lui16, Bit16()).getUResized(Bit20())
                    val ori12first_imm = Bin(ori12first, Bit12())
                    val ori12sec_imm = Bin(ori12sec, Bit12())
                    val ori12third_imm = Bin(ori12third, Bit12())

                    val luiOpCode = LUI.opCode?.getOpCode(mapOf(MaskLabel.RD to regs[0], MaskLabel.IMM20 to lui16_imm20))
                    val oriFirstOpCode = ORI.opCode?.getOpCode(mapOf(MaskLabel.RD to regs[0], MaskLabel.RS1 to regs[0], MaskLabel.IMM12 to ori12first_imm))
                    val oriSecOpCode = ORI.opCode?.getOpCode(mapOf(MaskLabel.RD to regs[0], MaskLabel.RS1 to regs[0], MaskLabel.IMM12 to ori12sec_imm))
                    val oriThirdOpCode = ORI.opCode?.getOpCode(mapOf(MaskLabel.RD to regs[0], MaskLabel.RS1 to regs[0], MaskLabel.IMM12 to ori12third_imm))


                    val slli12Bit = SLLI.opCode?.getOpCode(mapOf(MaskLabel.RD to regs[0], MaskLabel.RS1 to regs[0], MaskLabel.SHAMT6 to Bin("001100", Bit6())))

                    if (luiOpCode != null && oriFirstOpCode != null && slli12Bit != null && oriSecOpCode != null && oriThirdOpCode != null) {
                        binArray.add(luiOpCode)
                        binArray.add(oriFirstOpCode)
                        binArray.add(slli12Bit)
                        binArray.add(oriSecOpCode)
                        binArray.add(slli12Bit)
                        binArray.add(oriThirdOpCode)
                    }
                }

                Li64 -> {
                    /**
                     * split into 4 bit
                     *
                     *  SIGN-C----A----F----E----A----F----F----E----D----E----A----D----B----E----E----F---
                     *  xxxx-xxxx-xxxx-xxxx-xxxx-xxxx-xxxx-xxxx-xxxx-xxxx-xxxx-xxxx-xxxx-xxxx-xxxx-xxxx-xxxx
                     * |0000-xxxx-xxxx-xxxx-xxxx|xxxx-xxxx-xxxx|xxxx-xxxx-xxxx|xxxx-xxxx-xxxx|xxxx-xxxx-xxxx|
                     *            LUI                 ORI            ORI            ORI            ORI
                     *
                     */
                    val imm64 = instr.constants.first().getValue(Bit64()).toBin()
                    if (!imm64.checkResult.valid) {
                        architecture.getConsole().error("RV64 Syntax Issue - value exceeds maximum size! [Instr: ${instr.type.name}]\n${imm64.checkResult.message}")
                    }

                    val lui16 = imm64.getRawBinStr().substring(0, 16)
                    val ori12first = imm64.getRawBinStr().substring(16, 28)
                    val ori12sec = imm64.getRawBinStr().substring(28, 40)
                    val ori12third = imm64.getRawBinStr().substring(40, 52)
                    val ori12fourth = imm64.getRawBinStr().substring(52, 64)

                    val lui16_imm20 = Bin(lui16, Bit16()).getUResized(Bit20())
                    val ori12first_imm = Bin(ori12first, Bit12())
                    val ori12sec_imm = Bin(ori12sec, Bit12())
                    val ori12third_imm = Bin(ori12third, Bit12())
                    val ori12fourth_imm = Bin(ori12fourth, Bit12())

                    val luiOpCode = LUI.opCode?.getOpCode(mapOf(MaskLabel.RD to regs[0], MaskLabel.IMM20 to lui16_imm20))
                    val oriFirstOpCode = ORI.opCode?.getOpCode(mapOf(MaskLabel.RD to regs[0], MaskLabel.RS1 to regs[0], MaskLabel.IMM12 to ori12first_imm))
                    val oriSecOpCode = ORI.opCode?.getOpCode(mapOf(MaskLabel.RD to regs[0], MaskLabel.RS1 to regs[0], MaskLabel.IMM12 to ori12sec_imm))
                    val oriThirdOpCode = ORI.opCode?.getOpCode(mapOf(MaskLabel.RD to regs[0], MaskLabel.RS1 to regs[0], MaskLabel.IMM12 to ori12third_imm))
                    val oriFourthOpCode = ORI.opCode?.getOpCode(mapOf(MaskLabel.RD to regs[0], MaskLabel.RS1 to regs[0], MaskLabel.IMM12 to ori12fourth_imm))


                    val slli12Bit = SLLI.opCode?.getOpCode(mapOf(MaskLabel.RD to regs[0], MaskLabel.RS1 to regs[0], MaskLabel.SHAMT6 to Bin("001100", Bit6())))

                    if (luiOpCode != null && oriFirstOpCode != null && slli12Bit != null && oriSecOpCode != null && oriThirdOpCode != null && oriFourthOpCode != null) {
                        binArray.add(luiOpCode)
                        binArray.add(oriFirstOpCode)
                        binArray.add(slli12Bit)
                        binArray.add(oriSecOpCode)
                        binArray.add(slli12Bit)
                        binArray.add(oriThirdOpCode)
                        binArray.add(slli12Bit)
                        binArray.add(oriFourthOpCode)
                    }
                }

                La64 -> {
                    val imm64 = labels.first()
                    if (!imm64.checkResult.valid) {
                        architecture.getConsole().error("RV64 Syntax Issue - value exceeds maximum size! [Instr: ${instr.type.name}]\n${imm64.checkResult.message}")
                    }

                    val lui16 = imm64.getRawBinStr().substring(0, 16)
                    val ori12first = imm64.getRawBinStr().substring(16, 28)
                    val ori12sec = imm64.getRawBinStr().substring(28, 40)
                    val ori12third = imm64.getRawBinStr().substring(40, 52)
                    val ori12fourth = imm64.getRawBinStr().substring(52, 64)

                    val lui16_imm20 = Bin(lui16, Bit16()).getUResized(Bit20())
                    val ori12first_imm = Bin(ori12first, Bit12())
                    val ori12sec_imm = Bin(ori12sec, Bit12())
                    val ori12third_imm = Bin(ori12third, Bit12())
                    val ori12fourth_imm = Bin(ori12fourth, Bit12())

                    val luiOpCode = LUI.opCode?.getOpCode(mapOf(MaskLabel.RD to regs[0], MaskLabel.IMM20 to lui16_imm20))
                    val oriFirstOpCode = ORI.opCode?.getOpCode(mapOf(MaskLabel.RD to regs[0], MaskLabel.RS1 to regs[0], MaskLabel.IMM12 to ori12first_imm))
                    val oriSecOpCode = ORI.opCode?.getOpCode(mapOf(MaskLabel.RD to regs[0], MaskLabel.RS1 to regs[0], MaskLabel.IMM12 to ori12sec_imm))
                    val oriThirdOpCode = ORI.opCode?.getOpCode(mapOf(MaskLabel.RD to regs[0], MaskLabel.RS1 to regs[0], MaskLabel.IMM12 to ori12third_imm))
                    val oriFourthOpCode = ORI.opCode?.getOpCode(mapOf(MaskLabel.RD to regs[0], MaskLabel.RS1 to regs[0], MaskLabel.IMM12 to ori12fourth_imm))


                    val slli12Bit = SLLI.opCode?.getOpCode(mapOf(MaskLabel.RD to regs[0], MaskLabel.RS1 to regs[0], MaskLabel.SHAMT6 to Bin("001100", Bit6())))

                    if (luiOpCode != null && oriFirstOpCode != null && slli12Bit != null && oriSecOpCode != null && oriThirdOpCode != null && oriFourthOpCode != null) {
                        binArray.add(luiOpCode)
                        binArray.add(oriFirstOpCode)
                        binArray.add(slli12Bit)
                        binArray.add(oriSecOpCode)
                        binArray.add(slli12Bit)
                        binArray.add(oriThirdOpCode)
                        binArray.add(slli12Bit)
                        binArray.add(oriFourthOpCode)
                    }
                }

                Not -> {
                    val xoriOpCode = XORI.opCode?.getOpCode(mapOf(MaskLabel.RD to regs[0], MaskLabel.RS1 to regs[1], MaskLabel.IMM12 to Bin("1".repeat(12), Bit12())))

                    if (xoriOpCode != null) {
                        binArray.add(xoriOpCode)
                    }
                }

                Neg -> {
                    val rs1 = Bin("0", Bit5())

                    val subOpCode = SUB.opCode?.getOpCode(mapOf(MaskLabel.RD to regs[0], MaskLabel.RS1 to rs1, MaskLabel.RS2 to regs[1]))

                    if (subOpCode != null) {
                        binArray.add(subOpCode)
                    }
                }

                Seqz -> {
                    val imm12 = Bin("1", Bit12())

                    val sltiuOpCode = SLTIU.opCode?.getOpCode(mapOf(MaskLabel.RD to regs[0], MaskLabel.RS1 to regs[1], MaskLabel.IMM12 to imm12))

                    if (sltiuOpCode != null) {
                        binArray.add(sltiuOpCode)
                    }
                }

                Snez -> {
                    val rs1 = Bin("0", Bit5())

                    val sltuOpCode = SLTU.opCode?.getOpCode(mapOf(MaskLabel.RD to regs[0], MaskLabel.RS1 to rs1, MaskLabel.RS2 to regs[1]))

                    if (sltuOpCode != null) {
                        binArray.add(sltuOpCode)
                    }
                }

                Sltz -> {
                    val zero = Bin("0", Bit12())

                    val sltOpCode = SLT.opCode?.getOpCode(mapOf(MaskLabel.RD to regs[0], MaskLabel.RS1 to regs[1], MaskLabel.RS2 to zero))

                    if (sltOpCode != null) {
                        binArray.add(sltOpCode)
                    }
                }

                Sgtz -> {
                    val rs1 = Bin("0", Bit5())

                    val sltOpCode = SLT.opCode?.getOpCode(mapOf(MaskLabel.RD to regs[0], MaskLabel.RS1 to rs1, MaskLabel.RS2 to regs[1]))

                    if (sltOpCode != null) {
                        binArray.add(sltOpCode)
                    }
                }

                Beqz -> {
                    val lblAddr = labels.first()
                    val x0 = Bin("0", Bit5())
                    val imm12 = (lblAddr - instrAddr).toBin().getResized(Bit12()).shr(1).getRawBinStr()
                    val imm5 = Bin(imm12.substring(8) + imm12[1], Bit5())
                    val imm7 = Bin(imm12[0] + imm12.substring(2, 8), Bit7())
                    val beqOpCode = BEQ.opCode?.getOpCode(mapOf(MaskLabel.RS1 to regs[0], MaskLabel.RS2 to x0, MaskLabel.IMM7 to imm7, MaskLabel.IMM5 to imm5))

                    if (beqOpCode != null) {
                        binArray.add(beqOpCode)
                    }
                }

                Bnez -> {
                    val x0 = Bin("0", Bit5())
                    val labelAddr = labels.first()
                    val imm12 = (labelAddr - instrAddr).toBin().getResized(Bit12()).shr(1).getRawBinStr()
                    val imm5 = Bin(imm12.substring(8) + imm12[1], Bit5())
                    val imm7 = Bin(imm12[0] + imm12.substring(2, 8), Bit7())
                    val bneOpCode = BNE.opCode?.getOpCode(mapOf(MaskLabel.RS1 to regs[0], MaskLabel.RS2 to x0, MaskLabel.IMM7 to imm7, MaskLabel.IMM5 to imm5))

                    if (bneOpCode != null) {
                        binArray.add(bneOpCode)
                    }
                }

                Blez -> {
                    val x0 = Bin("0", Bit5())
                    val labelAddr = labels.first()
                    val imm12 = (labelAddr - instrAddr).toBin().getResized(Bit12()).shr(1).getRawBinStr()
                    val imm5 = Bin(imm12.substring(8) + imm12[1], Bit5())
                    val imm7 = Bin(imm12[0] + imm12.substring(2, 8), Bit7())
                    val bgeOpCode = BGE.opCode?.getOpCode(mapOf(MaskLabel.RS1 to x0, MaskLabel.RS2 to regs[0], MaskLabel.IMM7 to imm7, MaskLabel.IMM5 to imm5))

                    if (bgeOpCode != null) {
                        binArray.add(bgeOpCode)
                    }
                }

                Bgez -> {
                    val x0 = Bin("0", Bit5())
                    val labelAddr = labels.first()
                    val imm12 = (labelAddr - instrAddr).toBin().getResized(Bit12()).shr(1).getRawBinStr()
                    val imm5 = Bin(imm12.substring(8) + imm12[1], Bit5())
                    val imm7 = Bin(imm12[0] + imm12.substring(2, 8), Bit7())
                    val bgeOpCode = BGE.opCode?.getOpCode(mapOf(MaskLabel.RS1 to regs[0], MaskLabel.RS2 to x0, MaskLabel.IMM7 to imm7, MaskLabel.IMM5 to imm5))

                    if (bgeOpCode != null) {
                        binArray.add(bgeOpCode)
                    }
                }

                Bltz -> {
                    val x0 = Bin("0", Bit5())
                    val labelAddr = labels.first()
                    val imm12 = (labelAddr - instrAddr).toBin().getResized(Bit12()).shr(1).getRawBinStr()
                    val imm5 = Bin(imm12.substring(8) + imm12[1], Bit5())
                    val imm7 = Bin(imm12[0] + imm12.substring(2, 8), Bit7())
                    val bltOpCode = BLT.opCode?.getOpCode(mapOf(MaskLabel.RS1 to regs[0], MaskLabel.RS2 to x0, MaskLabel.IMM7 to imm7, MaskLabel.IMM5 to imm5))

                    if (bltOpCode != null) {
                        binArray.add(bltOpCode)
                    }
                }

                BGTZ -> {
                    val x0 = Bin("0", Bit5())
                    val labelAddr = labels.first()
                    val imm12 = (labelAddr - instrAddr).toBin().getResized(Bit12()).shr(1).getRawBinStr()
                    val imm5 = Bin(imm12.substring(8) + imm12[1], Bit5())
                    val imm7 = Bin(imm12[0] + imm12.substring(2, 8), Bit7())
                    val bltOpCode = BLT.opCode?.getOpCode(mapOf(MaskLabel.RS1 to x0, MaskLabel.RS2 to regs[0], MaskLabel.IMM7 to imm7, MaskLabel.IMM5 to imm5))

                    if (bltOpCode != null) {
                        binArray.add(bltOpCode)
                    }
                }

                Bgt -> {
                    val labelAddr = labels.first()
                    val imm12 = (labelAddr - instrAddr).toBin().getResized(Bit12()).shr(1).getRawBinStr()
                    val imm5 = Bin(imm12.substring(8) + imm12[1], Bit5())
                    val imm7 = Bin(imm12[0] + imm12.substring(2, 8), Bit7())

                    val bltOpCode = BLT.opCode?.getOpCode(mapOf(MaskLabel.RS1 to regs[1], MaskLabel.RS2 to regs[0], MaskLabel.IMM7 to imm7, MaskLabel.IMM5 to imm5))

                    if (bltOpCode != null) {
                        binArray.add(bltOpCode)
                    }
                }

                Ble -> {
                    val labelAddr = labels.first()
                    val imm12 = (labelAddr - instrAddr).toBin().getResized(Bit12()).shr(1).getRawBinStr()
                    val imm5 = Bin(imm12.substring(8) + imm12[1], Bit5())
                    val imm7 = Bin(imm12[0] + imm12.substring(2, 8), Bit7())

                    val bgeOpCode = BGE.opCode?.getOpCode(mapOf(MaskLabel.RS1 to regs[1], MaskLabel.RS2 to regs[0], MaskLabel.IMM7 to imm7, MaskLabel.IMM5 to imm5))

                    if (bgeOpCode != null) {
                        binArray.add(bgeOpCode)
                    }
                }

                Bgtu -> {
                    val labelAddr = labels.first()
                    val imm12 = (labelAddr - instrAddr).toBin().getResized(Bit12()).shr(1).getRawBinStr()
                    val imm5 = Bin(imm12.substring(8) + imm12[1], Bit5())
                    val imm7 = Bin(imm12[0] + imm12.substring(2, 8), Bit7())

                    val bltuOpCode = BLTU.opCode?.getOpCode(mapOf(MaskLabel.RS1 to regs[1], MaskLabel.RS2 to regs[0], MaskLabel.IMM7 to imm7, MaskLabel.IMM5 to imm5))

                    if (bltuOpCode != null) {
                        binArray.add(bltuOpCode)
                    }
                }

                Bleu -> {
                    val labelAddr = labels.first()
                    val imm12 = (labelAddr - instrAddr).toBin().getResized(Bit12()).shr(1).getRawBinStr()
                    val imm5 = Bin(imm12.substring(8) + imm12[1], Bit5())
                    val imm7 = Bin(imm12[0] + imm12.substring(2, 8), Bit7())

                    val bgeuOpCode = BGEU.opCode?.getOpCode(mapOf(MaskLabel.RS1 to regs[1], MaskLabel.RS2 to regs[0], MaskLabel.IMM7 to imm7, MaskLabel.IMM5 to imm5))

                    if (bgeuOpCode != null) {
                        binArray.add(bgeuOpCode)
                    }
                }

                J -> {
                    val rd = Bin("0", Bit5())
                    val imm20toWork = ((labels.first() - instrAddr).toBin() shr 1).getResized(Bit20()).getRawBinStr()

                    /**
                     *      RV64IDOC Index   20 19 18 17 16 15 14 13 12 11 10  9  8  7  6  5  4  3  2  1
                     *        String Index    0  1  2  3  4  5  6  7  8  9 10 11 12 13 14 15 16 17 18 19
                     */
                    val imm20 = Bin(imm20toWork[0].toString() + imm20toWork.substring(10) + imm20toWork[9] + imm20toWork.substring(1, 9), Bit20())

                    val jalOpCode = JAL.opCode?.getOpCode(mapOf(MaskLabel.RD to rd, MaskLabel.IMM20 to imm20))

                    if (jalOpCode != null) {
                        binArray.add(jalOpCode)
                    }
                }

                JAL1 -> {
                    val imm20toWork = ((labels.first() - instrAddr).toBin() shr 1).getResized(Bit20()).getRawBinStr()

                    /**
                     *      RV64IDOC Index   20 19 18 17 16 15 14 13 12 11 10  9  8  7  6  5  4  3  2  1
                     *        String Index    0  1  2  3  4  5  6  7  8  9 10 11 12 13 14 15 16 17 18 19
                     */
                    val imm20 = Bin(imm20toWork[0].toString() + imm20toWork.substring(10) + imm20toWork[9] + imm20toWork.substring(1, 9), Bit20())

                    val jalOpCode = JAL.opCode?.getOpCode(mapOf(MaskLabel.RD to regs[0], MaskLabel.IMM20 to imm20))

                    if (jalOpCode != null) {
                        binArray.add(jalOpCode)
                    }
                }

                JAL2 -> {
                    val rd = Bin("1", Bit5())
                    val imm20toWork = ((labels.first() - instrAddr).toBin() shr 1).getResized(Bit20()).getRawBinStr()

                    /**
                     *      RV64IDOC Index   20 19 18 17 16 15 14 13 12 11 10  9  8  7  6  5  4  3  2  1
                     *        String Index    0  1  2  3  4  5  6  7  8  9 10 11 12 13 14 15 16 17 18 19
                     */
                    val imm20 = Bin(imm20toWork[0].toString() + imm20toWork.substring(10) + imm20toWork[9] + imm20toWork.substring(1, 9), Bit20())

                    val jalOpCode = JAL.opCode?.getOpCode(mapOf(MaskLabel.RD to rd, MaskLabel.IMM20 to imm20))

                    if (jalOpCode != null) {
                        binArray.add(jalOpCode)
                    }
                }

                Jr -> {
                    val x0 = Bin("0", Bit5())
                    val zero = Bin("0", Bit12())

                    val jalrOpCode = JALR.opCode?.getOpCode(mapOf(MaskLabel.RD to x0, MaskLabel.RS1 to regs[0], MaskLabel.IMM12 to zero))

                    if (jalrOpCode != null) {
                        binArray.add(jalrOpCode)
                    }
                }

                JALR1 -> {
                    val x1 = Bin("1", Bit5())
                    val zero = Bin("0", Bit5())

                    val jalrOpCode = JALR.opCode?.getOpCode(mapOf(MaskLabel.RD to x1, MaskLabel.RS1 to regs[0], MaskLabel.IMM12 to zero))

                    if (jalrOpCode != null) {
                        binArray.add(jalrOpCode)
                    }
                }

                JALR2 -> {
                    val opCode = JALR.opCode?.getOpCode(mapOf(MaskLabel.RD to regs[0], MaskLabel.IMM12 to instr.constants.first().getValue(Bit12()).toBin(), MaskLabel.RS1 to regs[1]))
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
                    val x1 = Bin("1", Bit5())

                    val pcRelAddress32 = (labels.first() - instrAddr).toBin()
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
                    val x0 = Bin("0", Bit5())
                    val x6 = Hex("6", Bit5()).toBin()

                    val pcRelAddress32 = (labels.first() - instrAddr).toBin()
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


        } catch (e: Exception) {
            architecture.getConsole().error(e.message.toString())
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

    data class InstrResult(val type: InstrType, val binMap: Map<MaskLabel, Bin> = mapOf())
    class OpCode(private val opMask: String, val maskLabels: Array<MaskLabel>) {

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
                    if (DebugTools.RV64_showBinMapperInfo) {
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
        CSR(false, Bit12()),
        SHAMT6(false, Bit6()),
        FUNCT6(true, Bit6()),
        FUNCT7(true, Bit7()),
        UIMM5(false, Bit5()),
        IMM5(false, Bit5()),
        IMM7(false, Bit7()),
        IMM12(false, Bit12()),
        IMM20(false, Bit20()),
        NONE(true)
    }
}