package emulator.archs.riscv64

import Settings
import cengine.util.integer.Bin
import cengine.util.integer.Hex
import cengine.util.integer.Size
import cengine.util.integer.Size.*
import cengine.util.integer.Value
import debug.DebugTools
import emulator.archs.riscv64.RV64Syntax.InstrType
import emulator.kit.assembler.parser.Parser
import emulator.kit.nativeWarn

object RV64BinMapper {

    fun getBinaryFromInstrDef(instr: RV64Assembler.RV64Instr, addr: Hex, labelAddr: Hex, immediate: Value): Array<Bin> {
        val binArray = mutableListOf<Bin>()
        val regs = instr.regs.map { it.address.toBin() }

        when (instr.type) {
            InstrType.LUI, InstrType.AUIPC -> {
                val imm20 = immediate.toBin().getResized(Bit20)
                val opCode = instr.type.opCode?.getOpCode(mapOf(MaskLabel.RD to regs[0], MaskLabel.IMM20 to imm20))
                opCode?.let {
                    binArray.add(opCode)
                }
            }

            InstrType.JAL -> {
                val targetAddr = if (instr.label == null) {
                    immediate
                }else{
                    instr.label.evaluate(true)
                }

                val offset = (targetAddr - addr).toBin()
                if (!offset.checkSizeSigned(Bit20)) {
                    throw Parser.ParserError(instr.rawInstr.instrName, "Calculated offset exceeds ${Bit20} with ${offset}!")
                }

                val imm20toWork = offset.shr(1).getResized(Bit20).rawInput

                /**
                 *      RV64IDOC Index   20 19 18 17 16 15 14 13 12 11 10  9  8  7  6  5  4  3  2  1
                 *        String Index    0  1  2  3  4  5  6  7  8  9 10 11 12 13 14 15 16 17 18 19
                 */

                val imm20 = Bin(imm20toWork[0].toString() + imm20toWork.substring(10) + imm20toWork[9] + imm20toWork.substring(1, 9), Bit20)
                val opCode = instr.type.opCode?.getOpCode(mapOf(MaskLabel.RD to regs[0], MaskLabel.IMM20 to imm20))
                opCode?.let {
                    binArray.add(opCode)
                }
            }

            InstrType.JALR -> {
                val imm = immediate.toBin().getResized(Bit12)
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
                val targetAddr = if (instr.label == null) {
                    immediate
                }else{
                    instr.label.evaluate(true)
                }

                val offset = (targetAddr - addr).toBin()
                if (!offset.checkSizeSigned(Bit12)) {
                    throw Parser.ParserError(instr.rawInstr.instrName, "Calculated offset exceeds ${Bit12} with ${offset}!")
                }
                if (!offset.checkSizeSigned(Bit12)) {
                    throw Parser.ParserError(instr.rawInstr.instrName, "Calculated offset exceeds ${Bit12} with ${offset}!")
                }

                val imm12 = offset.shr(1).getResized(Bit12).rawInput

                val imm5 = Bin(imm12.substring(8) + imm12[1], Bit5)
                val imm7 = Bin(imm12[0] + imm12.substring(2, 8), Bit7)

                val opCode = instr.type.opCode?.getOpCode(mapOf(MaskLabel.RS1 to regs[0], MaskLabel.RS2 to regs[1], MaskLabel.IMM5 to imm5, MaskLabel.IMM7 to imm7))
                opCode?.let {
                    binArray.add(opCode)
                }
            }

            /*InstrType.BEQ1, InstrType.BNE1, InstrType.BLT1, InstrType.BGE1, InstrType.BLTU1, InstrType.BGEU1 -> {
                if (instr.label == null) throw Parser.ParserError(instr.rawInstr.instrName, "Label is missing!")

                val offset = (labelAddr - addr).toBin()
                offset.checkSizeSigned(Size.Bit12)?.let {
                    throw Parser.ParserError(instr.rawInstr.instrName, "Calculated offset exceeds ${it.expectedSize} with ${offset}!")
                }

                val imm12 = offset.shr(1).getResized(Size.Bit12).rawInput

                val imm5 = Bin(imm12.substring(8) + imm12[1], Size.Bit5)
                val imm7 = Bin(imm12[0] + imm12.substring(2, 8), Size.Bit7)

                val opCode = instr.type.relative?.opCode?.getOpCode(mapOf(MaskLabel.RS1 to regs[0], MaskLabel.RS2 to regs[1], MaskLabel.IMM5 to imm5, MaskLabel.IMM7 to imm7))
                opCode?.let {
                    binArray.add(opCode)
                }
            }*/

            InstrType.LB, InstrType.LH, InstrType.LW, InstrType.LD, InstrType.LBU, InstrType.LHU, InstrType.LWU -> {
                val imm = immediate.toDec().getResized(Bit12).toBin()
                val opCode = instr.type.opCode?.getOpCode(mapOf(MaskLabel.RD to regs[0], MaskLabel.IMM12 to imm, MaskLabel.RS1 to regs[1]))
                opCode?.let {
                    binArray.add(opCode)
                }
            }

            InstrType.SB, InstrType.SH, InstrType.SW, InstrType.SD -> {
                val imm = immediate.toDec().getResized(Bit12).toBin()
                val imm12 = imm.rawInput
                val imm5 = Bin(imm12.substring(imm12.length - 5))
                val imm7 = Bin(imm12.substring(imm12.length - 12, imm12.length - 5))

                val opCode = instr.type.opCode?.getOpCode(mapOf(MaskLabel.RS2 to regs[0], MaskLabel.IMM7 to imm7, MaskLabel.IMM5 to imm5, MaskLabel.RS1 to regs[1]))
                opCode?.let {
                    binArray.add(opCode)
                }
            }

            InstrType.ADDI, InstrType.ADDIW, InstrType.SLTI, InstrType.SLTIU, InstrType.XORI, InstrType.ORI, InstrType.ANDI -> {
                val imm = immediate.toDec().getResized(Bit12).toBin()
                val opCode = instr.type.opCode?.getOpCode(mapOf(MaskLabel.RD to regs[0], MaskLabel.RS1 to regs[1], MaskLabel.IMM12 to imm))
                opCode?.let {
                    binArray.add(opCode)
                }
            }

            InstrType.SLLI, InstrType.SLLIW, InstrType.SRLI, InstrType.SRLIW, InstrType.SRAI, InstrType.SRAIW -> {
                val uimm6 = immediate.toBin().getUResized(Bit6)
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
                val uimm5 = immediate.toBin().getUResized(Bit5)
                val opCode = instr.type.opCode?.getOpCode(mapOf(MaskLabel.RD to regs[0], MaskLabel.CSR to csrAddr, MaskLabel.UIMM5 to uimm5))
                opCode?.let {
                    binArray.add(opCode)
                }
            }

            InstrType.CSRW -> {
                val csrAddr = regs[0].toBin()
                val zero = Bin("0", Bit5)
                val opCode = InstrType.CSRRW.opCode?.getOpCode(mapOf(MaskLabel.RD to zero, MaskLabel.CSR to csrAddr, MaskLabel.RS1 to regs[1]))
                opCode?.let {
                    binArray.add(opCode)
                }
            }

            InstrType.CSRR -> {
                val csrAddr = regs[1].toBin()
                val zero = Bin("0", Bit5)
                val opCode = InstrType.CSRRS.opCode?.getOpCode(mapOf(MaskLabel.RD to regs[0], MaskLabel.CSR to csrAddr, MaskLabel.RS1 to zero))
                opCode?.let {
                    binArray.add(opCode)
                }
            }

            InstrType.Nop -> {
                val zero = Bin("0", Bit5)
                val imm12 = Bin("0", Bit12)
                val addiOpCode = InstrType.ADDI.opCode?.getOpCode(mapOf(MaskLabel.RD to zero, MaskLabel.RS1 to zero, MaskLabel.IMM12 to imm12))

                if (addiOpCode != null) {
                    binArray.add(addiOpCode)
                }
            }

            InstrType.Mv -> {
                val zero = Bin("0", Bit12)

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
                val imm64 = immediate.toBin().getResized(Bit64).toBin()

                if (!imm64.valid) {
                    throw Parser.ParserError(instr.rawInstr.instrName, "RV64 Syntax Issue - value exceeds maximum size! [Instr: ${instr.type.name}]")
                }

                val lui16 = imm64.rawInput.substring(0, 16)
                val ori12first = imm64.rawInput.substring(16, 28)
                val ori12sec = imm64.rawInput.substring(28, 40)
                val ori12third = imm64.rawInput.substring(40, 52)
                val ori12fourth = imm64.rawInput.substring(52, 64)

                val lui16_imm20 = Bin(lui16, Bit16).getUResized(Bit20)
                val ori12first_imm = Bin(ori12first, Bit12)
                val ori12sec_imm = Bin(ori12sec, Bit12)
                val ori12third_imm = Bin(ori12third, Bit12)
                val ori12fourth_imm = Bin(ori12fourth, Bit12)

                val luiOpCode = InstrType.LUI.opCode?.getOpCode(mapOf(MaskLabel.RD to regs[0], MaskLabel.IMM20 to lui16_imm20))
                val oriFirstOpCode = InstrType.ORI.opCode?.getOpCode(mapOf(MaskLabel.RD to regs[0], MaskLabel.RS1 to regs[0], MaskLabel.IMM12 to ori12first_imm))
                val oriSecOpCode = InstrType.ORI.opCode?.getOpCode(mapOf(MaskLabel.RD to regs[0], MaskLabel.RS1 to regs[0], MaskLabel.IMM12 to ori12sec_imm))
                val oriThirdOpCode = InstrType.ORI.opCode?.getOpCode(mapOf(MaskLabel.RD to regs[0], MaskLabel.RS1 to regs[0], MaskLabel.IMM12 to ori12third_imm))
                val oriFourthOpCode = InstrType.ORI.opCode?.getOpCode(mapOf(MaskLabel.RD to regs[0], MaskLabel.RS1 to regs[0], MaskLabel.IMM12 to ori12fourth_imm))


                val slli12Bit = InstrType.SLLI.opCode?.getOpCode(mapOf(MaskLabel.RD to regs[0], MaskLabel.RS1 to regs[0], MaskLabel.SHAMT6 to Bin("001100", Bit6)))

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

                val offset = (labelAddr - addr).toBin().getUResized(Bit64)
                if (!offset.valid) {
                    throw Parser.ParserError(instr.rawInstr.instrName, "RV64 Syntax Issue - value exceeds maximum size! [Instr: ${instr.type.name}]\n${offset}")
                }

                val hi20 = offset.rawInput.substring(0, 20)
                val low12 = offset.rawInput.substring(20)

                val imm12 = Bin(low12, Bit12)

                val imm20temp = (Bin(hi20, Bit20)).toBin() // more performant
                val imm20 = if (imm12.toDec().isNegative()) {
                    (imm20temp + Bin("1")).toBin()
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
                val xoriOpCode = InstrType.XORI.opCode?.getOpCode(mapOf(MaskLabel.RD to regs[0], MaskLabel.RS1 to regs[1], MaskLabel.IMM12 to Bin("1".repeat(12), Bit12)))

                if (xoriOpCode != null) {
                    binArray.add(xoriOpCode)
                }
            }

            InstrType.Neg -> {
                val rs1 = Bin("0", Bit5)

                val subOpCode = InstrType.SUB.opCode?.getOpCode(mapOf(MaskLabel.RD to regs[0], MaskLabel.RS1 to rs1, MaskLabel.RS2 to regs[1]))

                if (subOpCode != null) {
                    binArray.add(subOpCode)
                }
            }

            InstrType.Seqz -> {
                val imm12 = Bin("1", Bit12)

                val sltiuOpCode = InstrType.SLTIU.opCode?.getOpCode(mapOf(MaskLabel.RD to regs[0], MaskLabel.RS1 to regs[1], MaskLabel.IMM12 to imm12))

                if (sltiuOpCode != null) {
                    binArray.add(sltiuOpCode)
                }
            }

            InstrType.Snez -> {
                val rs1 = Bin("0", Bit5)

                val sltuOpCode = InstrType.SLTU.opCode?.getOpCode(mapOf(MaskLabel.RD to regs[0], MaskLabel.RS1 to rs1, MaskLabel.RS2 to regs[1]))

                if (sltuOpCode != null) {
                    binArray.add(sltuOpCode)
                }
            }

            InstrType.Sltz -> {
                val zero = Bin("0", Bit12)

                val sltOpCode = InstrType.SLT.opCode?.getOpCode(mapOf(MaskLabel.RD to regs[0], MaskLabel.RS1 to regs[1], MaskLabel.RS2 to zero))

                if (sltOpCode != null) {
                    binArray.add(sltOpCode)
                }
            }

            InstrType.Sgtz -> {
                val rs1 = Bin("0", Bit5)

                val sltOpCode = InstrType.SLT.opCode?.getOpCode(mapOf(MaskLabel.RD to regs[0], MaskLabel.RS1 to rs1, MaskLabel.RS2 to regs[1]))

                if (sltOpCode != null) {
                    binArray.add(sltOpCode)
                }
            }

            InstrType.Beqz -> {
                val x0 = Bin("0", Bit5)
                val targetAddr = if (instr.label == null) {
                    immediate
                }else{
                    instr.label.evaluate(true)
                }

                val offset = (targetAddr - addr).toBin()
                if (!offset.checkSizeSigned(Bit12)) {
                    throw Parser.ParserError(instr.rawInstr.instrName, "Calculated offset exceeds ${Bit12} with ${offset}!")
                }

                val imm12 = offset.shr(1).getResized(Bit12).rawInput
                val imm5 = Bin(imm12.substring(8) + imm12[1], Bit5)
                val imm7 = Bin(imm12[0] + imm12.substring(2, 8), Bit7)
                val beqOpCode = InstrType.BEQ.opCode?.getOpCode(mapOf(MaskLabel.RS1 to regs[0], MaskLabel.RS2 to x0, MaskLabel.IMM7 to imm7, MaskLabel.IMM5 to imm5))

                if (beqOpCode != null) {
                    binArray.add(beqOpCode)
                }
            }

            InstrType.Bnez -> {
                val x0 = Bin("0", Bit5)

                val targetAddr = if (instr.label == null) {
                    immediate
                }else{
                    instr.label.evaluate(true)
                }

                val offset = (targetAddr - addr).toBin()
                if (!offset.checkSizeSigned(Bit12)) {
                    throw Parser.ParserError(instr.rawInstr.instrName, "Calculated offset exceeds ${Bit12} with ${offset}!")
                }

                val imm12 = offset.shr(1).getResized(Bit12).rawInput
                val imm5 = Bin(imm12.substring(8) + imm12[1], Bit5)
                val imm7 = Bin(imm12[0] + imm12.substring(2, 8), Bit7)
                val bneOpCode = InstrType.BNE.opCode?.getOpCode(mapOf(MaskLabel.RS1 to regs[0], MaskLabel.RS2 to x0, MaskLabel.IMM7 to imm7, MaskLabel.IMM5 to imm5))

                if (bneOpCode != null) {
                    binArray.add(bneOpCode)
                }
            }

            InstrType.Blez -> {
                val x0 = Bin("0", Bit5)

                val targetAddr = if (instr.label == null) {
                    immediate
                }else{
                    instr.label.evaluate(true)
                }

                val offset = (targetAddr - addr).toBin()
                if (!offset.checkSizeSigned(Bit12)) {
                    throw Parser.ParserError(instr.rawInstr.instrName, "Calculated offset exceeds ${Bit12} with ${offset}!")
                }

                val imm12 = offset.shr(1).getResized(Bit12).rawInput
                val imm5 = Bin(imm12.substring(8) + imm12[1], Bit5)
                val imm7 = Bin(imm12[0] + imm12.substring(2, 8), Bit7)
                val bgeOpCode = InstrType.BGE.opCode?.getOpCode(mapOf(MaskLabel.RS1 to x0, MaskLabel.RS2 to regs[0], MaskLabel.IMM7 to imm7, MaskLabel.IMM5 to imm5))

                if (bgeOpCode != null) {
                    binArray.add(bgeOpCode)
                }
            }

            InstrType.Bgez -> {
                val x0 = Bin("0", Bit5)

                val targetAddr = if (instr.label == null) {
                    immediate
                }else{
                    instr.label.evaluate(true)
                }

                val offset = (targetAddr - addr).toBin()
                if (!offset.checkSizeSigned(Bit12)) {
                    throw Parser.ParserError(instr.rawInstr.instrName, "Calculated offset exceeds ${Bit12} with ${offset}!")
                }

                val imm12 = offset.shr(1).getResized(Bit12).rawInput
                val imm5 = Bin(imm12.substring(8) + imm12[1], Bit5)
                val imm7 = Bin(imm12[0] + imm12.substring(2, 8), Bit7)
                val bgeOpCode = InstrType.BGE.opCode?.getOpCode(mapOf(MaskLabel.RS1 to regs[0], MaskLabel.RS2 to x0, MaskLabel.IMM7 to imm7, MaskLabel.IMM5 to imm5))

                if (bgeOpCode != null) {
                    binArray.add(bgeOpCode)
                }
            }

            InstrType.Bltz -> {
                val x0 = Bin("0", Bit5)

                val targetAddr = if (instr.label == null) {
                    immediate
                }else{
                    instr.label.evaluate(true)
                }

                val offset = (targetAddr - addr).toBin()
                if (!offset.checkSizeSigned(Bit12)) {
                    throw Parser.ParserError(instr.rawInstr.instrName, "Calculated offset exceeds ${Bit12} with ${offset}!")
                }

                val imm12 = offset.shr(1).getResized(Bit12).rawInput
                val imm5 = Bin(imm12.substring(8) + imm12[1], Bit5)
                val imm7 = Bin(imm12[0] + imm12.substring(2, 8), Bit7)
                val bltOpCode = InstrType.BLT.opCode?.getOpCode(mapOf(MaskLabel.RS1 to regs[0], MaskLabel.RS2 to x0, MaskLabel.IMM7 to imm7, MaskLabel.IMM5 to imm5))

                if (bltOpCode != null) {
                    binArray.add(bltOpCode)
                }
            }

            InstrType.BGTZ -> {
                val x0 = Bin("0", Bit5)

                val targetAddr = if (instr.label == null) {
                    immediate
                }else{
                    instr.label.evaluate(true)
                }

                val offset = (targetAddr - addr).toBin()
                if (!offset.checkSizeSigned(Bit12)) {
                    throw Parser.ParserError(instr.rawInstr.instrName, "Calculated offset exceeds ${Bit12} with ${offset}!")
                }

                val imm12 = offset.shr(1).getResized(Bit12).rawInput
                val imm5 = Bin(imm12.substring(8) + imm12[1], Bit5)
                val imm7 = Bin(imm12[0] + imm12.substring(2, 8), Bit7)
                val bltOpCode = InstrType.BLT.opCode?.getOpCode(mapOf(MaskLabel.RS1 to x0, MaskLabel.RS2 to regs[0], MaskLabel.IMM7 to imm7, MaskLabel.IMM5 to imm5))

                if (bltOpCode != null) {
                    binArray.add(bltOpCode)
                }
            }

            InstrType.Bgt -> {
                val targetAddr = if (instr.label == null) {
                    immediate
                }else{
                    instr.label.evaluate(true)
                }

                val offset = (targetAddr - addr).toBin()
                if (!offset.checkSizeSigned(Bit12)) {
                    throw Parser.ParserError(instr.rawInstr.instrName, "Calculated offset exceeds ${Bit12} with ${offset}!")
                }

                val imm12 = offset.shr(1).getResized(Bit12).rawInput
                val imm5 = Bin(imm12.substring(8) + imm12[1], Bit5)
                val imm7 = Bin(imm12[0] + imm12.substring(2, 8), Bit7)

                val bltOpCode = InstrType.BLT.opCode?.getOpCode(mapOf(MaskLabel.RS1 to regs[1], MaskLabel.RS2 to regs[0], MaskLabel.IMM7 to imm7, MaskLabel.IMM5 to imm5))

                if (bltOpCode != null) {
                    binArray.add(bltOpCode)
                }
            }

            InstrType.Ble -> {
                val targetAddr = if (instr.label == null) {
                    immediate
                }else{
                    instr.label.evaluate(true)
                }

                val offset = (targetAddr - addr).toBin()
                if (!offset.checkSizeSigned(Bit12)) {
                    throw Parser.ParserError(instr.rawInstr.instrName, "Calculated offset exceeds ${Bit12} with ${offset}!")
                }

                val imm12 = offset.shr(1).getResized(Bit12).rawInput
                val imm5 = Bin(imm12.substring(8) + imm12[1], Bit5)
                val imm7 = Bin(imm12[0] + imm12.substring(2, 8), Bit7)

                val bgeOpCode = InstrType.BGE.opCode?.getOpCode(mapOf(MaskLabel.RS1 to regs[1], MaskLabel.RS2 to regs[0], MaskLabel.IMM7 to imm7, MaskLabel.IMM5 to imm5))

                if (bgeOpCode != null) {
                    binArray.add(bgeOpCode)
                }
            }

            InstrType.Bgtu -> {
                val targetAddr = if (instr.label == null) {
                    immediate
                }else{
                    instr.label.evaluate(true)
                }

                val offset = (targetAddr - addr).toBin()
                if (!offset.checkSizeSigned(Bit12)) {
                    throw Parser.ParserError(instr.rawInstr.instrName, "Calculated offset exceeds ${Bit12} with ${offset}!")
                }

                val imm12 = offset.shr(1).getResized(Bit12).rawInput
                val imm5 = Bin(imm12.substring(8) + imm12[1], Bit5)
                val imm7 = Bin(imm12[0] + imm12.substring(2, 8), Bit7)

                val bltuOpCode = InstrType.BLTU.opCode?.getOpCode(mapOf(MaskLabel.RS1 to regs[1], MaskLabel.RS2 to regs[0], MaskLabel.IMM7 to imm7, MaskLabel.IMM5 to imm5))

                if (bltuOpCode != null) {
                    binArray.add(bltuOpCode)
                }
            }

            InstrType.Bleu -> {
                val targetAddr = if (instr.label == null) {
                    immediate
                }else{
                    instr.label.evaluate(true)
                }

                val offset = (targetAddr - addr).toBin()
                if (!offset.checkSizeSigned(Bit12)) {
                    throw Parser.ParserError(instr.rawInstr.instrName, "Calculated offset exceeds ${Bit12} with ${offset}!")
                }

                val imm12 = offset.shr(1).getResized(Bit12).rawInput
                val imm5 = Bin(imm12.substring(8) + imm12[1], Bit5)
                val imm7 = Bin(imm12[0] + imm12.substring(2, 8), Bit7)

                val bgeuOpCode = InstrType.BGEU.opCode?.getOpCode(mapOf(MaskLabel.RS1 to regs[1], MaskLabel.RS2 to regs[0], MaskLabel.IMM7 to imm7, MaskLabel.IMM5 to imm5))

                if (bgeuOpCode != null) {
                    binArray.add(bgeuOpCode)
                }
            }

            InstrType.J -> {
                val rd = Bin("0", Bit5)

                val targetAddr = if (instr.label == null) {
                    immediate
                }else{
                    instr.label.evaluate(true)
                }

                val offset = (targetAddr - addr).toBin()
                if (!offset.checkSizeSigned(Bit20)) {
                    throw Parser.ParserError(instr.rawInstr.instrName, "Calculated offset exceeds ${Bit20} with ${offset}!")
                }

                val imm20toWork = offset.shr(1).getResized(Bit20).rawInput

                /**
                 *      RV64IDOC Index   20 19 18 17 16 15 14 13 12 11 10  9  8  7  6  5  4  3  2  1
                 *        String Index    0  1  2  3  4  5  6  7  8  9 10 11 12 13 14 15 16 17 18 19
                 */
                val imm20 = Bin(imm20toWork[0].toString() + imm20toWork.substring(10) + imm20toWork[9] + imm20toWork.substring(1, 9), Bit20)

                val jalOpCode = InstrType.JAL.opCode?.getOpCode(mapOf(MaskLabel.RD to rd, MaskLabel.IMM20 to imm20))

                if (jalOpCode != null) {
                    binArray.add(jalOpCode)
                }
            }

            InstrType.JAL1 -> {
                val rd = Bin("1", Bit5)

                val targetAddr = if (instr.label == null) {
                    immediate
                }else{
                    instr.label.evaluate(true)
                }

                val offset = (targetAddr - addr).toBin()

                if (!offset.checkSizeSigned(Bit20)) {
                    throw Parser.ParserError(instr.rawInstr.instrName, "Calculated offset exceeds ${Bit20} with ${offset}!")
                }

                val imm20toWork = offset.shr(1).getResized(Bit20).rawInput

                /**
                 *      RV64IDOC Index   20 19 18 17 16 15 14 13 12 11 10  9  8  7  6  5  4  3  2  1
                 *        String Index    0  1  2  3  4  5  6  7  8  9 10 11 12 13 14 15 16 17 18 19
                 */
                val imm20 = Bin(imm20toWork[0].toString() + imm20toWork.substring(10) + imm20toWork[9] + imm20toWork.substring(1, 9), Bit20)

                val jalOpCode = InstrType.JAL.opCode?.getOpCode(mapOf(MaskLabel.RD to rd, MaskLabel.IMM20 to imm20))

                if (jalOpCode != null) {
                    binArray.add(jalOpCode)
                }
            }

            InstrType.Jr -> {
                val x0 = Bin("0", Bit5)
                val imm12 = Bin("0", Bit12)

                val jalrOpCode = InstrType.JALR.opCode?.getOpCode(mapOf(MaskLabel.RD to x0, MaskLabel.RS1 to regs[0], MaskLabel.IMM12 to imm12))

                if (jalrOpCode != null) {
                    binArray.add(jalrOpCode)
                }
            }

            InstrType.JALR1 -> {
                val x1 = Bin("1", Bit5)
                val imm12 = Bin("0", Bit12)

                val jalrOpCode = InstrType.JALR.opCode?.getOpCode(mapOf(MaskLabel.RD to x1, MaskLabel.RS1 to regs[0], MaskLabel.IMM12 to imm12))

                if (jalrOpCode != null) {
                    binArray.add(jalrOpCode)
                }
            }

            InstrType.Ret -> {
                val zero = Bin("0", Bit5)
                val ra = Bin("1", Bit5)
                val imm12 = Bin("0", Bit12)

                val jalrOpCode = InstrType.JALR.opCode?.getOpCode(mapOf(MaskLabel.RD to zero, MaskLabel.IMM12 to imm12, MaskLabel.RS1 to ra))

                if (jalrOpCode != null) {
                    binArray.add(jalrOpCode)
                }
            }

            InstrType.Call -> {
                val x1 = Bin("1", Bit5)

                val pcRelAddress32 = (labelAddr - addr).toBin()
                val imm32 = pcRelAddress32.rawInput

                val jalrOff = Bin(imm32.substring(20), Bit12)
                val auipcOff = (pcRelAddress32 - jalrOff.getResized(Bit32)).toBin().ushr(12).getUResized(Bit20)

                val auipcOpCode = InstrType.AUIPC.opCode?.getOpCode(mapOf(MaskLabel.RD to x1, MaskLabel.IMM20 to auipcOff))
                val jalrOpCode = InstrType.JALR.opCode?.getOpCode(mapOf(MaskLabel.RD to x1, MaskLabel.IMM12 to jalrOff, MaskLabel.RS1 to x1))

                if (auipcOpCode != null && jalrOpCode != null) {
                    binArray.add(auipcOpCode)
                    binArray.add(jalrOpCode)
                }
            }

            InstrType.Tail -> {
                val x0 = Bin("0", Bit5)
                val x6 = Hex("6", Bit5).toBin()

                val pcRelAddress32 = (labelAddr - addr).toBin()
                val imm32 = pcRelAddress32.rawInput

                val jalrOff = Bin(imm32.substring(20), Bit12)
                val auipcOff = (pcRelAddress32 - jalrOff.getResized(Bit32)).toBin().ushr(12).getUResized(Bit20)

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

        return binArray.toTypedArray()
    }

    fun getInstrFromBinary(bin: Bin): InstrResult? {
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

    data class InstrResult(val type: InstrType, val binMap: Map<MaskLabel, Bin> = mapOf())
    class OpCode(private val opMask: String, val maskLabels: Array<MaskLabel>) {

        val opMaskList = opMask.removePrefix(Settings.PRESTRING_BINARY).split(" ")
        fun checkOpCode(bin: Bin): CheckResult {
            if (bin.size != Bit32) {
                return CheckResult(false)
            }
            // Check OpCode
            val binaryString = bin.rawInput
            val binaryOpCode = binaryString.substring(binaryString.length - 7)
            val originalOpCode = getMaskString(MaskLabel.OPCODE)
            if (originalOpCode.isNotEmpty()) {
                if (binaryOpCode == originalOpCode) {
                    // check static labels
                    val binMap = mutableMapOf<MaskLabel, Bin>()
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
            if (length != Bit32.bitWidth) {
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
                            opCode[labelID] = param.getUResized(size).rawInput
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

            return Bin(opCode.joinToString("") { it }, Bit32)
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

    enum class MaskLabel(val static: Boolean, val maxSize: Size? = null) {
        OPCODE(true, Bit7),
        RD(false, Bit5),
        FUNCT3(true, Bit3),
        RS1(false, Bit5),
        RS2(false, Bit5),
        CSR(false, Bit12),
        SHAMT6(false, Bit6),
        FUNCT6(true, Bit6),
        FUNCT7(true, Bit7),
        UIMM5(false, Bit5),
        IMM5(false, Bit5),
        IMM7(false, Bit7),
        IMM12(false, Bit12),
        IMM20(false, Bit20),
        NONE(true)
    }

}