package emulator.archs.riscv64

import debug.DebugTools
import Settings
import emulator.kit.types.Variable
import emulator.archs.riscv64.RV64Syntax.InstrType
import emulator.kit.assembler.parser.Parser
import emulator.kit.common.Memory
import emulator.kit.nativeWarn

object RV64BinMapper {

    fun getBinaryFromInstrDef(instr: RV64Assembler.RV64Instr, addr: Variable.Value.Hex, labelAddr: Variable.Value.Hex, immediate: Variable.Value): Array<Variable.Value.Bin> {
        val binArray = mutableListOf<Variable.Value.Bin>()
        val regs = instr.regs.map { it.address.toBin() }

        when (instr.type) {
            InstrType.LUI, InstrType.AUIPC -> {
                val imm20 = immediate.toBin().getResized(Variable.Size.Bit20())
                val opCode = instr.type.opCode?.getOpCode(mapOf(MaskLabel.RD to regs[0], MaskLabel.IMM20 to imm20))
                opCode?.let {
                    binArray.add(opCode)
                }
            }

            InstrType.JAL -> {
                val imm = immediate.toBin().getResized(Variable.Size.Bit20())
                val imm20toWork = imm.getRawBinStr()

                /**
                 *      RV64IDOC Index   20 19 18 17 16 15 14 13 12 11 10  9  8  7  6  5  4  3  2  1
                 *        String Index    0  1  2  3  4  5  6  7  8  9 10 11 12 13 14 15 16 17 18 19
                 */

                val imm20 = Variable.Value.Bin(imm20toWork[0].toString() + imm20toWork.substring(10) + imm20toWork[9] + imm20toWork.substring(1, 9), Variable.Size.Bit20())
                val opCode = instr.type.opCode?.getOpCode(mapOf(MaskLabel.RD to regs[0], MaskLabel.IMM20 to imm20))
                opCode?.let {
                    binArray.add(opCode)
                }
            }

            InstrType.JALR -> {
                val imm = immediate.toBin().getResized(Variable.Size.Bit12())
                val opCode = instr.type.opCode?.getOpCode(mapOf(MaskLabel.RD to regs[0], MaskLabel.IMM12 to imm, MaskLabel.RS1 to regs[1]))
                opCode?.let {
                    binArray.add(opCode)
                }
            }

            InstrType.ECALL, InstrType.EBREAK -> {
                val opCode = instr.type.opCode?.getOpCode(mapOf())
                opCode?.let {
                    binArray.add(opCode)
                }
            }

            InstrType.BEQ, InstrType.BNE, InstrType.BLT, InstrType.BGE, InstrType.BLTU, InstrType.BGEU -> {
                val imm = immediate.toDec().getResized(Variable.Size.Bit12()).toBin()
                val imm12 = imm.getRawBinStr()
                val imm5 = Variable.Value.Bin(imm12.substring(8) + imm12[1], Variable.Size.Bit5())
                val imm7 = Variable.Value.Bin(imm12[0] + imm12.substring(2, 8), Variable.Size.Bit7())

                val opCode = instr.type.opCode?.getOpCode(mapOf(MaskLabel.RS1 to regs[0], MaskLabel.RS2 to regs[1], MaskLabel.IMM5 to imm5, MaskLabel.IMM7 to imm7))
                opCode?.let {
                    binArray.add(opCode)
                }
            }

            InstrType.BEQ1, InstrType.BNE1, InstrType.BLT1, InstrType.BGE1, InstrType.BLTU1, InstrType.BGEU1 -> {
                if (instr.label == null) throw Parser.ParserError(instr.rawInstr.instrName, "Label is missing!")

                val offset = (labelAddr - addr).toBin()
                offset.checkSizeSigned(Variable.Size.Bit12())?.let {
                    throw Parser.ParserError(instr.rawInstr.instrName, "Calculated offset exceeds ${it.expectedSize} with ${offset}!")
                }

                val imm12 = offset.shr(1).getResized(Variable.Size.Bit12()).getRawBinStr()

                val imm5 = Variable.Value.Bin(imm12.substring(8) + imm12[1], Variable.Size.Bit5())
                val imm7 = Variable.Value.Bin(imm12[0] + imm12.substring(2, 8), Variable.Size.Bit7())

                val opCode = instr.type.relative?.opCode?.getOpCode(mapOf(MaskLabel.RS1 to regs[0], MaskLabel.RS2 to regs[1], MaskLabel.IMM5 to imm5, MaskLabel.IMM7 to imm7))
                opCode?.let {
                    binArray.add(opCode)
                }
            }

            InstrType.LB, InstrType.LH, InstrType.LW, InstrType.LD, InstrType.LBU, InstrType.LHU, InstrType.LWU -> {
                val imm = immediate.toDec().getResized(Variable.Size.Bit12()).toBin()
                val opCode = instr.type.opCode?.getOpCode(mapOf(MaskLabel.RD to regs[0], MaskLabel.IMM12 to imm, MaskLabel.RS1 to regs[1]))
                opCode?.let {
                    binArray.add(opCode)
                }
            }

            InstrType.SB, InstrType.SH, InstrType.SW, InstrType.SD -> {
                val imm = immediate.toDec().getResized(Variable.Size.Bit12()).toBin()
                val imm12 = imm.getRawBinStr()
                val imm5 = Variable.Value.Bin(imm12.substring(imm12.length - 5))
                val imm7 = Variable.Value.Bin(imm12.substring(imm12.length - 12, imm12.length - 5))

                val opCode = instr.type.opCode?.getOpCode(mapOf(MaskLabel.RS2 to regs[0], MaskLabel.IMM7 to imm7, MaskLabel.IMM5 to imm5, MaskLabel.RS1 to regs[1]))
                opCode?.let {
                    binArray.add(opCode)
                }
            }

            InstrType.ADDI, InstrType.ADDIW, InstrType.SLTI, InstrType.SLTIU, InstrType.XORI, InstrType.ORI, InstrType.ANDI -> {
                val imm = immediate.toDec().getResized(Variable.Size.Bit12()).toBin()
                val opCode = instr.type.opCode?.getOpCode(mapOf(MaskLabel.RD to regs[0], MaskLabel.RS1 to regs[1], MaskLabel.IMM12 to imm))
                opCode?.let {
                    binArray.add(opCode)
                }
            }

            InstrType.SLLI, InstrType.SLLIW, InstrType.SRLI, InstrType.SRLIW, InstrType.SRAI, InstrType.SRAIW -> {
                val uimm6 = immediate.toBin().getUResized(Variable.Size.Bit6())
                val opCode = instr.type.opCode?.getOpCode(mapOf(MaskLabel.RD to regs[0], MaskLabel.RS1 to regs[1], MaskLabel.SHAMT6 to uimm6))
                opCode?.let {
                    binArray.add(opCode)
                }
            }

            InstrType.ADD, InstrType.ADDW, InstrType.SUB, InstrType.SUBW, InstrType.SLL, InstrType.SLLW, InstrType.SLT, InstrType.SLTU, InstrType.XOR, InstrType.SRL, InstrType.SRLW, InstrType.SRA, InstrType.SRAW, InstrType.OR, InstrType.AND, InstrType.MUL, InstrType.MULH, InstrType.MULHSU, InstrType.MULHU, InstrType.DIV, InstrType.DIVU, InstrType.REM, InstrType.REMU, InstrType.MULW, InstrType.DIVW, InstrType.DIVUW, InstrType.REMW, InstrType.REMUW -> {
                val opCode = instr.type.opCode?.getOpCode(mapOf(MaskLabel.RD to regs[0], MaskLabel.RS1 to regs[1], MaskLabel.RS2 to regs[2]))
                opCode?.let {
                    binArray.add(opCode)
                }
            }

            InstrType.CSRRW, InstrType.CSRRS, InstrType.CSRRC -> {
                val csrAddr = regs[1].toBin()

                val opCode = instr.type.opCode?.getOpCode(mapOf(MaskLabel.RD to regs[0], MaskLabel.CSR to csrAddr, MaskLabel.RS1 to regs.last()))
                opCode?.let {
                    binArray.add(opCode)
                }
            }

            InstrType.CSRRWI, InstrType.CSRRSI, InstrType.CSRRCI -> {
                val csrAddr = regs[1].toBin()
                val uimm5 = immediate.toBin().getUResized(Variable.Size.Bit5())
                val opCode = instr.type.opCode?.getOpCode(mapOf(MaskLabel.RD to regs[0], MaskLabel.CSR to csrAddr, MaskLabel.UIMM5 to uimm5))
                opCode?.let {
                    binArray.add(opCode)
                }
            }

            InstrType.CSRW -> {
                val csrAddr = regs[0].toBin()
                val zero = Variable.Value.Bin("0", Variable.Size.Bit5())
                val opCode = InstrType.CSRRW.opCode?.getOpCode(mapOf(MaskLabel.RD to zero, MaskLabel.CSR to csrAddr, MaskLabel.RS1 to regs[1]))
                opCode?.let {
                    binArray.add(opCode)
                }
            }

            InstrType.CSRR -> {
                val csrAddr = regs[1].toBin()
                val zero = Variable.Value.Bin("0", Variable.Size.Bit5())
                val opCode = InstrType.CSRRS.opCode?.getOpCode(mapOf(MaskLabel.RD to regs[0], MaskLabel.CSR to csrAddr, MaskLabel.RS1 to zero))
                opCode?.let {
                    binArray.add(opCode)
                }
            }

            InstrType.Nop -> {
                val zero = Variable.Value.Bin("0", Variable.Size.Bit5())
                val imm12 = Variable.Value.Bin("0", Variable.Size.Bit12())
                val addiOpCode = InstrType.ADDI.opCode?.getOpCode(mapOf(MaskLabel.RD to zero, MaskLabel.RS1 to zero, MaskLabel.IMM12 to imm12))

                if (addiOpCode != null) {
                    binArray.add(addiOpCode)
                }
            }

            InstrType.Mv -> {
                val zero = Variable.Value.Bin("0", Variable.Size.Bit12())

                val addiOpCode = InstrType.ADDI.opCode?.getOpCode(mapOf(MaskLabel.RD to regs[0], MaskLabel.RS1 to regs[1], MaskLabel.IMM12 to zero))

                if (addiOpCode != null) {
                    binArray.add(addiOpCode)
                }
            }

            InstrType.Li64 -> {
                /**
                 * split into 4 bit
                 *
                 *  SIGN-C----A----F----E----A----F----F----E----D----E----A----D----B----E----E----F---
                 *  xxxx-xxxx-xxxx-xxxx-xxxx-xxxx-xxxx-xxxx-xxxx-xxxx-xxxx-xxxx-xxxx-xxxx-xxxx-xxxx-xxxx
                 * |0000-xxxx-xxxx-xxxx-xxxx|xxxx-xxxx-xxxx|xxxx-xxxx-xxxx|xxxx-xxxx-xxxx|xxxx-xxxx-xxxx|
                 *            LUI                 ORI            ORI            ORI            ORI
                 *
                 */
                val imm64 = immediate.toBin().getResized(Variable.Size.Bit64()).toBin()

                if (!imm64.checkResult.valid) {
                    throw Parser.ParserError(instr.rawInstr.instrName, "RV64 Syntax Issue - value exceeds maximum size! [Instr: ${instr.type.name}]\n${imm64.checkResult.message}")
                }

                val lui16 = imm64.getRawBinStr().substring(0, 16)
                val ori12first = imm64.getRawBinStr().substring(16, 28)
                val ori12sec = imm64.getRawBinStr().substring(28, 40)
                val ori12third = imm64.getRawBinStr().substring(40, 52)
                val ori12fourth = imm64.getRawBinStr().substring(52, 64)

                val lui16_imm20 = Variable.Value.Bin(lui16, Variable.Size.Bit16()).getUResized(Variable.Size.Bit20())
                val ori12first_imm = Variable.Value.Bin(ori12first, Variable.Size.Bit12())
                val ori12sec_imm = Variable.Value.Bin(ori12sec, Variable.Size.Bit12())
                val ori12third_imm = Variable.Value.Bin(ori12third, Variable.Size.Bit12())
                val ori12fourth_imm = Variable.Value.Bin(ori12fourth, Variable.Size.Bit12())

                val luiOpCode = InstrType.LUI.opCode?.getOpCode(mapOf(MaskLabel.RD to regs[0], MaskLabel.IMM20 to lui16_imm20))
                val oriFirstOpCode = InstrType.ORI.opCode?.getOpCode(mapOf(MaskLabel.RD to regs[0], MaskLabel.RS1 to regs[0], MaskLabel.IMM12 to ori12first_imm))
                val oriSecOpCode = InstrType.ORI.opCode?.getOpCode(mapOf(MaskLabel.RD to regs[0], MaskLabel.RS1 to regs[0], MaskLabel.IMM12 to ori12sec_imm))
                val oriThirdOpCode = InstrType.ORI.opCode?.getOpCode(mapOf(MaskLabel.RD to regs[0], MaskLabel.RS1 to regs[0], MaskLabel.IMM12 to ori12third_imm))
                val oriFourthOpCode = InstrType.ORI.opCode?.getOpCode(mapOf(MaskLabel.RD to regs[0], MaskLabel.RS1 to regs[0], MaskLabel.IMM12 to ori12fourth_imm))


                val slli12Bit = InstrType.SLLI.opCode?.getOpCode(mapOf(MaskLabel.RD to regs[0], MaskLabel.RS1 to regs[0], MaskLabel.SHAMT6 to Variable.Value.Bin("001100", Variable.Size.Bit6())))

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

            InstrType.La -> {
                val regBin = regs[0]
                val offset = (labelAddr - addr).toBin().getUResized(RV64.WORD_WIDTH)
                if (!offset.checkResult.valid) {
                    throw Parser.ParserError(instr.rawInstr.instrName, "RV64 Syntax Issue - value exceeds maximum size! [Instr: ${instr.type.name}]\n${offset.checkResult.message}")
                }

                val hi20 = offset.getRawBinStr().substring(0, 20)
                val low12 = offset.getRawBinStr().substring(20)

                val imm12 = Variable.Value.Bin(low12, Variable.Size.Bit12())

                val imm20temp = (Variable.Value.Bin(hi20, Variable.Size.Bit20())).toBin() // more performant
                val imm20 = if (imm12.toDec().isNegative()) {
                    (imm20temp + Variable.Value.Bin("1")).toBin()
                } else {
                    imm20temp
                }

                val auipcOpCode = InstrType.AUIPC.opCode?.getOpCode(mapOf(MaskLabel.RD to regBin, MaskLabel.IMM20 to imm20))
                val addiOpCode = InstrType.ADDI.opCode?.getOpCode(mapOf(MaskLabel.RD to regBin, MaskLabel.RS1 to regBin, MaskLabel.IMM12 to imm12))

                if (auipcOpCode != null && addiOpCode != null) {
                    binArray.add(auipcOpCode)
                    binArray.add(addiOpCode)
                }
            }

            InstrType.Not -> {
                val xoriOpCode = InstrType.XORI.opCode?.getOpCode(mapOf(MaskLabel.RD to regs[0], MaskLabel.RS1 to regs[1], MaskLabel.IMM12 to Variable.Value.Bin("1".repeat(12), Variable.Size.Bit12())))

                if (xoriOpCode != null) {
                    binArray.add(xoriOpCode)
                }
            }

            InstrType.Neg -> {
                val rs1 = Variable.Value.Bin("0", Variable.Size.Bit5())

                val subOpCode = InstrType.SUB.opCode?.getOpCode(mapOf(MaskLabel.RD to regs[0], MaskLabel.RS1 to rs1, MaskLabel.RS2 to regs[1]))

                if (subOpCode != null) {
                    binArray.add(subOpCode)
                }
            }

            InstrType.Seqz -> {
                val imm12 = Variable.Value.Bin("1", Variable.Size.Bit12())

                val sltiuOpCode = InstrType.SLTIU.opCode?.getOpCode(mapOf(MaskLabel.RD to regs[0], MaskLabel.RS1 to regs[1], MaskLabel.IMM12 to imm12))

                if (sltiuOpCode != null) {
                    binArray.add(sltiuOpCode)
                }
            }

            InstrType.Snez -> {
                val rs1 = Variable.Value.Bin("0", Variable.Size.Bit5())

                val sltuOpCode = InstrType.SLTU.opCode?.getOpCode(mapOf(MaskLabel.RD to regs[0], MaskLabel.RS1 to rs1, MaskLabel.RS2 to regs[1]))

                if (sltuOpCode != null) {
                    binArray.add(sltuOpCode)
                }
            }

            InstrType.Sltz -> {
                val zero = Variable.Value.Bin("0", Variable.Size.Bit12())

                val sltOpCode = InstrType.SLT.opCode?.getOpCode(mapOf(MaskLabel.RD to regs[0], MaskLabel.RS1 to regs[1], MaskLabel.RS2 to zero))

                if (sltOpCode != null) {
                    binArray.add(sltOpCode)
                }
            }

            InstrType.Sgtz -> {
                val rs1 = Variable.Value.Bin("0", Variable.Size.Bit5())

                val sltOpCode = InstrType.SLT.opCode?.getOpCode(mapOf(MaskLabel.RD to regs[0], MaskLabel.RS1 to rs1, MaskLabel.RS2 to regs[1]))

                if (sltOpCode != null) {
                    binArray.add(sltOpCode)
                }
            }

            InstrType.Beqz -> {
                val x0 = Variable.Value.Bin("0", Variable.Size.Bit5())
                val offset = (labelAddr - addr).toBin()
                offset.checkSizeSigned(Variable.Size.Bit12())?.let {
                    throw Parser.ParserError(instr.rawInstr.instrName, "Calculated offset exceeds ${it.expectedSize} with ${offset}!")
                }

                val imm12 = offset.shr(1).getResized(Variable.Size.Bit12()).getRawBinStr()
                val imm5 = Variable.Value.Bin(imm12.substring(8) + imm12[1], Variable.Size.Bit5())
                val imm7 = Variable.Value.Bin(imm12[0] + imm12.substring(2, 8), Variable.Size.Bit7())
                val beqOpCode = InstrType.BEQ.opCode?.getOpCode(mapOf(MaskLabel.RS1 to regs[0], MaskLabel.RS2 to x0, MaskLabel.IMM7 to imm7, MaskLabel.IMM5 to imm5))

                if (beqOpCode != null) {
                    binArray.add(beqOpCode)
                }
            }

            InstrType.Bnez -> {
                val x0 = Variable.Value.Bin("0", Variable.Size.Bit5())
                val offset = (labelAddr - addr).toBin()
                offset.checkSizeSigned(Variable.Size.Bit12())?.let {
                    throw Parser.ParserError(instr.rawInstr.instrName, "Calculated offset exceeds ${it.expectedSize} with ${offset}!")
                }

                val imm12 = offset.shr(1).getResized(Variable.Size.Bit12()).getRawBinStr()
                val imm5 = Variable.Value.Bin(imm12.substring(8) + imm12[1], Variable.Size.Bit5())
                val imm7 = Variable.Value.Bin(imm12[0] + imm12.substring(2, 8), Variable.Size.Bit7())
                val bneOpCode = InstrType.BNE.opCode?.getOpCode(mapOf(MaskLabel.RS1 to regs[0], MaskLabel.RS2 to x0, MaskLabel.IMM7 to imm7, MaskLabel.IMM5 to imm5))

                if (bneOpCode != null) {
                    binArray.add(bneOpCode)
                }
            }

            InstrType.Blez -> {
                val x0 = Variable.Value.Bin("0", Variable.Size.Bit5())
                val offset = (labelAddr - addr).toBin()
                offset.checkSizeSigned(Variable.Size.Bit12())?.let {
                    throw Parser.ParserError(instr.rawInstr.instrName, "Calculated offset exceeds ${it.expectedSize} with ${offset}!")
                }

                val imm12 = offset.shr(1).getResized(Variable.Size.Bit12()).getRawBinStr()
                val imm5 = Variable.Value.Bin(imm12.substring(8) + imm12[1], Variable.Size.Bit5())
                val imm7 = Variable.Value.Bin(imm12[0] + imm12.substring(2, 8), Variable.Size.Bit7())
                val bgeOpCode = InstrType.BGE.opCode?.getOpCode(mapOf(MaskLabel.RS1 to x0, MaskLabel.RS2 to regs[0], MaskLabel.IMM7 to imm7, MaskLabel.IMM5 to imm5))

                if (bgeOpCode != null) {
                    binArray.add(bgeOpCode)
                }
            }

            InstrType.Bgez -> {
                val x0 = Variable.Value.Bin("0", Variable.Size.Bit5())
                val offset = (labelAddr - addr).toBin()
                offset.checkSizeSigned(Variable.Size.Bit12())?.let {
                    throw Parser.ParserError(instr.rawInstr.instrName, "Calculated offset exceeds ${it.expectedSize} with ${offset}!")
                }

                val imm12 = offset.shr(1).getResized(Variable.Size.Bit12()).getRawBinStr()
                val imm5 = Variable.Value.Bin(imm12.substring(8) + imm12[1], Variable.Size.Bit5())
                val imm7 = Variable.Value.Bin(imm12[0] + imm12.substring(2, 8), Variable.Size.Bit7())
                val bgeOpCode = InstrType.BGE.opCode?.getOpCode(mapOf(MaskLabel.RS1 to regs[0], MaskLabel.RS2 to x0, MaskLabel.IMM7 to imm7, MaskLabel.IMM5 to imm5))

                if (bgeOpCode != null) {
                    binArray.add(bgeOpCode)
                }
            }

            InstrType.Bltz -> {
                val x0 = Variable.Value.Bin("0", Variable.Size.Bit5())
                val offset = (labelAddr - addr).toBin()
                offset.checkSizeSigned(Variable.Size.Bit12())?.let {
                    throw Parser.ParserError(instr.rawInstr.instrName, "Calculated offset exceeds ${it.expectedSize} with ${offset}!")
                }

                val imm12 = offset.shr(1).getResized(Variable.Size.Bit12()).getRawBinStr()
                val imm5 = Variable.Value.Bin(imm12.substring(8) + imm12[1], Variable.Size.Bit5())
                val imm7 = Variable.Value.Bin(imm12[0] + imm12.substring(2, 8), Variable.Size.Bit7())
                val bltOpCode = InstrType.BLT.opCode?.getOpCode(mapOf(MaskLabel.RS1 to regs[0], MaskLabel.RS2 to x0, MaskLabel.IMM7 to imm7, MaskLabel.IMM5 to imm5))

                if (bltOpCode != null) {
                    binArray.add(bltOpCode)
                }
            }

            InstrType.BGTZ -> {
                val x0 = Variable.Value.Bin("0", Variable.Size.Bit5())
                val offset = (labelAddr - addr).toBin()
                offset.checkSizeSigned(Variable.Size.Bit12())?.let {
                    throw Parser.ParserError(instr.rawInstr.instrName, "Calculated offset exceeds ${it.expectedSize} with ${offset}!")
                }

                val imm12 = offset.shr(1).getResized(Variable.Size.Bit12()).getRawBinStr()
                val imm5 = Variable.Value.Bin(imm12.substring(8) + imm12[1], Variable.Size.Bit5())
                val imm7 = Variable.Value.Bin(imm12[0] + imm12.substring(2, 8), Variable.Size.Bit7())
                val bltOpCode = InstrType.BLT.opCode?.getOpCode(mapOf(MaskLabel.RS1 to x0, MaskLabel.RS2 to regs[0], MaskLabel.IMM7 to imm7, MaskLabel.IMM5 to imm5))

                if (bltOpCode != null) {
                    binArray.add(bltOpCode)
                }
            }

            InstrType.Bgt -> {
                val offset = (labelAddr - addr).toBin()
                offset.checkSizeSigned(Variable.Size.Bit12())?.let {
                    throw Parser.ParserError(instr.rawInstr.instrName, "Calculated offset exceeds ${it.expectedSize} with ${offset}!")
                }

                val imm12 = offset.shr(1).getResized(Variable.Size.Bit12()).getRawBinStr()
                val imm5 = Variable.Value.Bin(imm12.substring(8) + imm12[1], Variable.Size.Bit5())
                val imm7 = Variable.Value.Bin(imm12[0] + imm12.substring(2, 8), Variable.Size.Bit7())

                val bltOpCode = InstrType.BLT.opCode?.getOpCode(mapOf(MaskLabel.RS1 to regs[1], MaskLabel.RS2 to regs[0], MaskLabel.IMM7 to imm7, MaskLabel.IMM5 to imm5))

                if (bltOpCode != null) {
                    binArray.add(bltOpCode)
                }
            }

            InstrType.Ble -> {
                val offset = (labelAddr - addr).toBin()
                offset.checkSizeSigned(Variable.Size.Bit12())?.let {
                    throw Parser.ParserError(instr.rawInstr.instrName, "Calculated offset exceeds ${it.expectedSize} with ${offset}!")
                }

                val imm12 = offset.shr(1).getResized(Variable.Size.Bit12()).getRawBinStr()
                val imm5 = Variable.Value.Bin(imm12.substring(8) + imm12[1], Variable.Size.Bit5())
                val imm7 = Variable.Value.Bin(imm12[0] + imm12.substring(2, 8), Variable.Size.Bit7())

                val bgeOpCode = InstrType.BGE.opCode?.getOpCode(mapOf(MaskLabel.RS1 to regs[1], MaskLabel.RS2 to regs[0], MaskLabel.IMM7 to imm7, MaskLabel.IMM5 to imm5))

                if (bgeOpCode != null) {
                    binArray.add(bgeOpCode)
                }
            }

            InstrType.Bgtu -> {
                val offset = (labelAddr - addr).toBin()
                offset.checkSizeSigned(Variable.Size.Bit12())?.let {
                    throw Parser.ParserError(instr.rawInstr.instrName, "Calculated offset exceeds ${it.expectedSize} with ${offset}!")
                }

                val imm12 = offset.shr(1).getResized(Variable.Size.Bit12()).getRawBinStr()
                val imm5 = Variable.Value.Bin(imm12.substring(8) + imm12[1], Variable.Size.Bit5())
                val imm7 = Variable.Value.Bin(imm12[0] + imm12.substring(2, 8), Variable.Size.Bit7())

                val bltuOpCode = InstrType.BLTU.opCode?.getOpCode(mapOf(MaskLabel.RS1 to regs[1], MaskLabel.RS2 to regs[0], MaskLabel.IMM7 to imm7, MaskLabel.IMM5 to imm5))

                if (bltuOpCode != null) {
                    binArray.add(bltuOpCode)
                }
            }

            InstrType.Bleu -> {
                val offset = (labelAddr - addr).toBin()
                offset.checkSizeSigned(Variable.Size.Bit12())?.let {
                    throw Parser.ParserError(instr.rawInstr.instrName, "Calculated offset exceeds ${it.expectedSize} with ${offset}!")
                }

                val imm12 = offset.shr(1).getResized(Variable.Size.Bit12()).getRawBinStr()
                val imm5 = Variable.Value.Bin(imm12.substring(8) + imm12[1], Variable.Size.Bit5())
                val imm7 = Variable.Value.Bin(imm12[0] + imm12.substring(2, 8), Variable.Size.Bit7())

                val bgeuOpCode = InstrType.BGEU.opCode?.getOpCode(mapOf(MaskLabel.RS1 to regs[1], MaskLabel.RS2 to regs[0], MaskLabel.IMM7 to imm7, MaskLabel.IMM5 to imm5))

                if (bgeuOpCode != null) {
                    binArray.add(bgeuOpCode)
                }
            }

            InstrType.J -> {
                val rd = Variable.Value.Bin("0", Variable.Size.Bit5())

                val offset = (labelAddr - addr).toBin()
                offset.checkSizeSigned(Variable.Size.Bit20())?.let {
                    throw Parser.ParserError(instr.rawInstr.instrName, "Calculated offset exceeds ${it.expectedSize} with ${offset}!")
                }

                val imm20toWork = offset.shr(1).getResized(Variable.Size.Bit20()).getRawBinStr()

                /**
                 *      RV64IDOC Index   20 19 18 17 16 15 14 13 12 11 10  9  8  7  6  5  4  3  2  1
                 *        String Index    0  1  2  3  4  5  6  7  8  9 10 11 12 13 14 15 16 17 18 19
                 */
                val imm20 = Variable.Value.Bin(imm20toWork[0].toString() + imm20toWork.substring(10) + imm20toWork[9] + imm20toWork.substring(1, 9), Variable.Size.Bit20())

                val jalOpCode = InstrType.JAL.opCode?.getOpCode(mapOf(MaskLabel.RD to rd, MaskLabel.IMM20 to imm20))

                if (jalOpCode != null) {
                    binArray.add(jalOpCode)
                }
            }

            InstrType.JAL1 -> {
                val offset = (labelAddr - addr).toBin()
                offset.checkSizeSigned(Variable.Size.Bit20())?.let {
                    throw Parser.ParserError(instr.rawInstr.instrName, "Calculated offset exceeds ${it.expectedSize} with ${offset}!")
                }

                val imm20toWork = offset.shr(1).getResized(Variable.Size.Bit20()).getRawBinStr()

                /**
                 *      RV64IDOC Index   20 19 18 17 16 15 14 13 12 11 10  9  8  7  6  5  4  3  2  1
                 *        String Index    0  1  2  3  4  5  6  7  8  9 10 11 12 13 14 15 16 17 18 19
                 */
                val imm20 = Variable.Value.Bin(imm20toWork[0].toString() + imm20toWork.substring(10) + imm20toWork[9] + imm20toWork.substring(1, 9), Variable.Size.Bit20())

                val jalOpCode = InstrType.JAL.opCode?.getOpCode(mapOf(MaskLabel.RD to regs[0], MaskLabel.IMM20 to imm20))

                if (jalOpCode != null) {
                    binArray.add(jalOpCode)
                }
            }

            InstrType.JAL2 -> {
                val rd = Variable.Value.Bin("1", Variable.Size.Bit5())
                val offset = (labelAddr - addr).toBin()

                offset.checkSizeSigned(Variable.Size.Bit20())?.let {
                    throw Parser.ParserError(instr.rawInstr.instrName, "Calculated offset exceeds ${it.expectedSize} with ${offset}!")
                }

                val imm20toWork = offset.shr(1).getResized(Variable.Size.Bit20()).getRawBinStr()

                /**
                 *      RV64IDOC Index   20 19 18 17 16 15 14 13 12 11 10  9  8  7  6  5  4  3  2  1
                 *        String Index    0  1  2  3  4  5  6  7  8  9 10 11 12 13 14 15 16 17 18 19
                 */
                val imm20 = Variable.Value.Bin(imm20toWork[0].toString() + imm20toWork.substring(10) + imm20toWork[9] + imm20toWork.substring(1, 9), Variable.Size.Bit20())

                val jalOpCode = InstrType.JAL.opCode?.getOpCode(mapOf(MaskLabel.RD to rd, MaskLabel.IMM20 to imm20))

                if (jalOpCode != null) {
                    binArray.add(jalOpCode)
                }
            }

            InstrType.Jr -> {
                val x0 = Variable.Value.Bin("0", Variable.Size.Bit5())
                val zero = Variable.Value.Bin("0", Variable.Size.Bit12())

                val jalrOpCode = InstrType.JALR.opCode?.getOpCode(mapOf(MaskLabel.RD to x0, MaskLabel.RS1 to regs[0], MaskLabel.IMM12 to zero))

                if (jalrOpCode != null) {
                    binArray.add(jalrOpCode)
                }
            }

            InstrType.JALR1 -> {
                val x1 = Variable.Value.Bin("1", Variable.Size.Bit5())
                val zero = Variable.Value.Bin("0", Variable.Size.Bit5())

                val jalrOpCode = InstrType.JALR.opCode?.getOpCode(mapOf(MaskLabel.RD to x1, MaskLabel.RS1 to regs[0], MaskLabel.IMM12 to zero))

                if (jalrOpCode != null) {
                    binArray.add(jalrOpCode)
                }
            }

            InstrType.JALR2 -> {
                val imm12 =immediate.toBin().getUResized(Variable.Size.Bit12())
                val opCode = InstrType.JALR.opCode?.getOpCode(mapOf(MaskLabel.RD to regs[0], MaskLabel.IMM12 to imm12, MaskLabel.RS1 to regs[1]))
                opCode?.let {
                    binArray.add(opCode)
                }
            }

            InstrType.Ret -> {
                val zero = Variable.Value.Bin("0", Variable.Size.Bit5())
                val ra = Variable.Value.Bin("1", Variable.Size.Bit5())
                val imm12 = Variable.Value.Bin("0", Variable.Size.Bit12())

                val jalrOpCode = InstrType.JALR.opCode?.getOpCode(mapOf(MaskLabel.RD to zero, MaskLabel.IMM12 to imm12, MaskLabel.RS1 to ra))

                if (jalrOpCode != null) {
                    binArray.add(jalrOpCode)
                }
            }

            InstrType.Call -> {
                val x1 = Variable.Value.Bin("1", Variable.Size.Bit5())

                val pcRelAddress32 = (labelAddr - addr).toBin()
                val imm32 = pcRelAddress32.getRawBinStr()

                val jalrOff = Variable.Value.Bin(imm32.substring(20), Variable.Size.Bit12())
                val auipcOff = (pcRelAddress32 - jalrOff.getResized(Variable.Size.Bit32())).toBin().ushr(12).getUResized(Variable.Size.Bit20())

                val auipcOpCode = InstrType.AUIPC.opCode?.getOpCode(mapOf(MaskLabel.RD to x1, MaskLabel.IMM20 to auipcOff))
                val jalrOpCode = InstrType.JALR.opCode?.getOpCode(mapOf(MaskLabel.RD to x1, MaskLabel.IMM12 to jalrOff, MaskLabel.RS1 to x1))

                if (auipcOpCode != null && jalrOpCode != null) {
                    binArray.add(auipcOpCode)
                    binArray.add(jalrOpCode)
                }
            }

            InstrType.Tail -> {
                val x0 = Variable.Value.Bin("0", Variable.Size.Bit5())
                val x6 = Variable.Value.Hex("6", Variable.Size.Bit5()).toBin()

                val pcRelAddress32 = (labelAddr - addr).toBin()
                val imm32 = pcRelAddress32.getRawBinStr()

                val jalrOff = Variable.Value.Bin(imm32.substring(20), Variable.Size.Bit12())
                val auipcOff = (pcRelAddress32 - jalrOff.getResized(Variable.Size.Bit32())).toBin().ushr(12).getUResized(Variable.Size.Bit20())

                val auipcOpCode = InstrType.AUIPC.opCode?.getOpCode(mapOf(MaskLabel.RD to x6, MaskLabel.IMM20 to auipcOff))
                val jalrOpCode = InstrType.JALR.opCode?.getOpCode(mapOf(MaskLabel.RD to x0, MaskLabel.IMM12 to jalrOff, MaskLabel.RS1 to x6))

                if (auipcOpCode != null && jalrOpCode != null) {
                    binArray.add(auipcOpCode)
                    binArray.add(jalrOpCode)
                }
            }

            InstrType.FENCEI -> {
                // Ignoring
            }
        }

        return binArray.flatMap { it.splitToByteArray().toList()}.toTypedArray()
    }

    fun getInstrFromBinary(bin: Variable.Value.Bin): InstrResult? {
        for (type in InstrType.entries) {
            val checkResult = type.opCode?.checkOpCode(bin)
            checkResult?.let {
                if (it.matches) {
                    return InstrResult(type, it.binMap)
                }
            }
        }
        return null
    }

    data class InstrResult(val type: InstrType, val binMap: Map<MaskLabel, Variable.Value.Bin> = mapOf())
    class OpCode(private val opMask: String, val maskLabels: Array<MaskLabel>) {

        val opMaskList = opMask.removePrefix(Settings.PRESTRING_BINARY).split(" ")
        fun checkOpCode(bin: Variable.Value.Bin): CheckResult {
            if (bin.size != Variable.Size.Bit32()) {
                return CheckResult(false)
            }
            // Check OpCode
            val binaryString = bin.getRawBinStr()
            val binaryOpCode = binaryString.substring(binaryString.length - 7)
            val originalOpCode = getMaskString(MaskLabel.OPCODE)
            if (originalOpCode.isNotEmpty()) {
                if (binaryOpCode == originalOpCode) {
                    // check static labels
                    val binMap = mutableMapOf<MaskLabel, Variable.Value.Bin>()
                    if (DebugTools.RV64_showBinMapperInfo) {
                        println("BinMapper.OpCode.checkOpCode(): found instr $binaryOpCode")
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
                                binMap[label] = Variable.Value.Bin(substring, label.maxSize)
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
                nativeWarn("BinMapper.OpCode: OpMask isn't 32Bit Binary! -> returning null")
                return null
            }
            if (opCode.size != maskLabels.size) {
                nativeWarn("BinMapper.OpCode: OpMask [$opMask] and Labels [${maskLabels.joinToString { it.name }}] aren't the same size! -> returning null")
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
                            nativeWarn("BinMapper.OpCode.getOpCode(): can't insert ByteValue in OpMask without a maxSize! -> returning null")
                            return null
                        }
                    } else {
                        nativeWarn("BinMapper.OpCode.getOpCode(): parameter [${maskLabel.name}] not found! -> inserting zeros")
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
        CSR(false, Variable.Size.Bit12()),
        SHAMT6(false, Variable.Size.Bit6()),
        FUNCT6(true, Variable.Size.Bit6()),
        FUNCT7(true, Variable.Size.Bit7()),
        UIMM5(false, Variable.Size.Bit5()),
        IMM5(false, Variable.Size.Bit5()),
        IMM7(false, Variable.Size.Bit7()),
        IMM12(false, Variable.Size.Bit12()),
        IMM20(false, Variable.Size.Bit20()),
        NONE(true)
    }

}