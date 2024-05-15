package emulator.archs.riscv64

import debug.DebugTools
import emulator.kit.assembler.DirTypeInterface
import emulator.kit.assembler.InstrTypeInterface
import emulator.kit.assembler.DefinedAssembly
import emulator.kit.assembler.gas.GASParser
import emulator.kit.assembler.gas.GASNode
import emulator.kit.assembler.lexer.Lexer
import emulator.kit.assembler.lexer.Token
import emulator.kit.assembler.parser.Parser
import emulator.kit.common.Memory
import emulator.kit.common.RegContainer
import emulator.kit.nativeLog
import emulator.kit.optional.Feature
import emulator.kit.types.Variable
import emulator.kit.types.Variable.Value.*
import emulator.kit.types.Variable.Size.*

class RV64Assembler : DefinedAssembly {
    override val MEM_ADDRESS_SIZE: Variable.Size = RV64.XLEN
    override val WORD_SIZE: Variable.Size = RV64.WORD_WIDTH
    override val INSTRS_ARE_WORD_ALIGNED: Boolean = true
    override val detectRegistersByName: Boolean = true
    override val prefices: Lexer.Prefices = object : Lexer.Prefices {
        override val hex: String = "0x"
        override val bin: String = "0b"
        override val dec: String = ""
        override val oct: String = "0"
        override val comment: String = "#"
        override val symbol: Regex = Regex("""^[a-zA-Z$._][a-zA-Z0-9$._]*""")
    }

    override fun getInstrs(features: List<Feature>): List<InstrTypeInterface> {
        val instrList = RV64Syntax.InstrType.entries.toMutableList()

        for (type in ArrayList(instrList)) {
            type.needFeatures.forEach { featureID ->
                val featureIsActive = features.firstOrNull { it.id == featureID }?.isActive()
                if (featureIsActive == false) {
                    instrList.remove(type)
                    return@forEach
                }
            }
        }

        return instrList
    }

    override fun getAdditionalDirectives(): List<DirTypeInterface> = GASRVDirType.entries

    override fun parseInstrParams(rawInstr: GASNode.RawInstr, tempContainer: GASParser.TempContainer): List<GASParser.SecContent> {
        val types = RV64Syntax.InstrType.entries.filter { it.getDetectionName() == rawInstr.instrName.instr?.getDetectionName() }

        for (type in types) {
            val paramType = type.paramType
            val result = paramType.tokenSeq?.matchStart(rawInstr.remainingTokens, listOf(), this, tempContainer.symbols)
            if (result == null) {
                return getNonPseudoInstructions(rawInstr, type, arrayOf(), null, null)
            }
            if (!result.matches) continue

            val expr = result.matchingNodes.filterIsInstance<GASNode.NumericExpr>().firstOrNull()
            val immExpr = if (expr != null && expr.isDefined()) expr else null
            val labelExpr = if (expr != null && !expr.isDefined()) expr else null
            val regs = result.matchingTokens.mapNotNull { it.reg }

            return getNonPseudoInstructions(rawInstr, type, regs.toTypedArray(), immExpr, labelExpr)
        }

        throw Parser.ParserError(rawInstr.instrName, "Invalid Arguments for ${rawInstr.instrName.instr?.getDetectionName() ?: rawInstr.instrName} ${rawInstr.remainingTokens.joinToString("") { it.toString() }}")
    }

    class RV64Instr(val rawInstr: GASNode.RawInstr, val type: RV64Syntax.InstrType, val regs: Array<RegContainer.Register> = emptyArray(), val immediate: Variable.Value = Dec("0", Bit32()), val label: GASNode.NumericExpr? = null) : GASParser.SecContent {
        override val bytesNeeded: Int = type.memWords * 4
        override fun getFirstToken(): Token = rawInstr.instrName
        override fun getMark(): Memory.InstanceType = Memory.InstanceType.PROGRAM
        override fun getBinaryArray(yourAddr: Variable.Value, labels: List<Pair<GASParser.Label, Hex>>): Array<Bin> {
            label?.assignLabels(labels)
            val labelAddr = label?.evaluate(true)?.toHex()
            return RV64BinMapper.getBinaryFromInstrDef(this, yourAddr.toHex(), labelAddr ?: Hex("0", yourAddr.size), immediate)
        }

        override fun getContentString(): String = "${type.id.lowercase()} ${type.paramType.getContentString(this)}"
    }

    private fun GASNode.NumericExpr.checkInstrType(type: RV64Syntax.InstrType): Dec {
        when (type.paramType) {
            RV64Syntax.ParamType.RD_I20 -> {
                val immediate = this.evaluate(false)
                val check = immediate.check(Bit20())
                if (!check.valid) throw Parser.ParserError(this.getAllTokens().first(), "Numeric Expression exceeds 20 Bits!")

                return immediate.getResized(Bit20())
            }

            RV64Syntax.ParamType.RD_Off12 -> {
                val immediate = this.evaluate(false)
                val check = immediate.check(Bit12())
                if (!check.valid) throw Parser.ParserError(this.getAllTokens().first(), "Numeric Expression exceeds 12 Bits!")

                return immediate.getResized(Bit12())
            }

            RV64Syntax.ParamType.RS2_Off12 -> {
                val immediate = this.evaluate(false)
                val check = immediate.check(Bit12())
                if (!check.valid) throw Parser.ParserError(this.getAllTokens().first(), "Numeric Expression exceeds 12 Bits!")

                return immediate.getResized(Bit12())
            }

            RV64Syntax.ParamType.RD_RS1_RS2 -> {
                throw Parser.ParserError(this.getAllTokens().first(), "Numeric Expression wasn't expected!")
            }

            RV64Syntax.ParamType.RD_RS1_I12 -> {
                val immediate = this.evaluate(false)
                val check = immediate.check(Bit12())
                if (!check.valid) throw Parser.ParserError(this.getAllTokens().first(), "Numeric Expression exceeds 12 Bits!")

                return immediate.getResized(Bit12())
            }

            RV64Syntax.ParamType.RD_RS1_SHAMT6 -> {
                val immediate = this.evaluate(false)
                val check = immediate.check(Bit6())
                if (!check.valid) throw Parser.ParserError(this.getAllTokens().first(), "Numeric Expression exceeds 6 Bits!")

                return immediate.toBin().getResized(Bit6()).toDec()
            }

            RV64Syntax.ParamType.RS1_RS2_I12 -> {
                val immediate = this.evaluate(false)
                val check = immediate.check(Bit12())
                if (!check.valid) throw Parser.ParserError(this.getAllTokens().first(), "Numeric Expression exceeds 12 Bits!")

                return immediate.getResized(Bit12())
            }

            RV64Syntax.ParamType.RS1_RS2_LBL -> {
                val immediate = this.evaluate(false).toBin()
                val check = immediate.check(Bit64())
                if (!check.valid) throw Parser.ParserError(this.getAllTokens().first(), "Label Expression exceeds 64 Bits!")

                return immediate.toBin().getUResized(Bit64()).toDec()
            }

            RV64Syntax.ParamType.CSR_RD_OFF12_RS1 -> {
                throw Parser.ParserError(this.getAllTokens().first(), "Numeric Expression wasn't expected!")
            }

            RV64Syntax.ParamType.CSR_RD_OFF12_UIMM5 -> {
                val immediate = this.evaluate(false).toBin()
                val check = immediate.check(Bit5())
                if (!check.valid) throw Parser.ParserError(this.getAllTokens().first(), "Numeric Expression exceeds 5 Bits!")

                return immediate.toBin().getUResized(Bit5()).toDec()
            }

            RV64Syntax.ParamType.PS_RD_LI_I64 -> {
                return this.evaluate(false)
            }

            RV64Syntax.ParamType.PS_RS1_Jlbl -> {
                throw Parser.ParserError(this.getAllTokens().first(), "Numeric Expression wasn't expected!")
            }

            RV64Syntax.ParamType.PS_RD_Albl -> {
                throw Parser.ParserError(this.getAllTokens().first(), "Numeric Expression wasn't expected!")
            }

            RV64Syntax.ParamType.PS_lbl -> {
                throw Parser.ParserError(this.getAllTokens().first(), "Numeric Expression wasn't expected!")
            }

            RV64Syntax.ParamType.PS_RD_RS1 -> {
                throw Parser.ParserError(this.getAllTokens().first(), "Numeric Expression wasn't expected!")
            }

            RV64Syntax.ParamType.PS_RS1 -> {
                throw Parser.ParserError(this.getAllTokens().first(), "Numeric Expression wasn't expected!")
            }

            RV64Syntax.ParamType.PS_CSR_RS1 -> {
                throw Parser.ParserError(this.getAllTokens().first(), "Numeric Expression wasn't expected!")
            }

            RV64Syntax.ParamType.PS_RD_CSR -> {
                throw Parser.ParserError(this.getAllTokens().first(), "Numeric Expression wasn't expected!")
            }

            RV64Syntax.ParamType.NONE -> {
                throw Parser.ParserError(this.getAllTokens().first(), "Numeric Expression wasn't expected!")
            }

            RV64Syntax.ParamType.PS_NONE -> {
                throw Parser.ParserError(this.getAllTokens().first(), "Numeric Expression wasn't expected!")
            }
        }
    }

    private fun getNonPseudoInstructions(rawInstr: GASNode.RawInstr, type: RV64Syntax.InstrType, regs: Array<RegContainer.Register>, immediate: GASNode.NumericExpr? = null, label: GASNode.NumericExpr? = null): List<RV64Instr> {
        return when (type) {
            RV64Syntax.InstrType.Li64 -> {
                val imm = immediate?.checkInstrType(type) ?: throw Parser.ParserError(rawInstr.getAllTokens().first(), "Numeric Expression is Missing!")

                var result = imm.check(Bit12())
                if (result.valid) {
                    if (DebugTools.RV64_showLIDecisions) nativeLog("Decided 12 Bit Signed")
                    return listOf(RV64Instr(rawInstr, RV64Syntax.InstrType.ADDI, arrayOf(regs[0], regs[0]), imm.getResized(Bit12())))
                }

                result = imm.check(Bit32())
                if (result.valid) {
                    val resized = imm.getResized(Bit32()).toBin()
                    if (DebugTools.RV64_showLIDecisions) nativeLog("Decided 32 Bit Signed")
                    // resized = upper + lower
                    // upper = resized - lower
                    val lower = resized.getResized(Bit12()).getResized(Bit32())
                    val upper = (resized - lower).toBin()

                    val imm20 = Bin(upper.getRawBinStr().substring(0, 20), Bit20()).toHex()
                    val imm12 = Bin(lower.getRawBinStr().substring(20), Bit12()).toHex()

                    return listOf(
                        RV64Instr(rawInstr, RV64Syntax.InstrType.LUI, arrayOf(regs[0]), imm20),
                        RV64Instr(rawInstr, RV64Syntax.InstrType.ADDI, arrayOf(regs[0], regs[0]), imm12)
                    )
                }

                result = imm.check(Bit44())
                if (result.valid) {
                    val resized = imm.toBin().getResized(Bit44())
                    if (DebugTools.RV64_showLIDecisions) nativeLog("Decided 44 Bit Signed for ${resized.toHex()}")
                    /**
                     *  val64 = lui + addiw + addi3 + addi2 + addi1
                     *
                     *  LUI
                     *  ADDIW
                     *  SLLI 12
                     *  ADDI
                     */
                    val l1 = resized.getResized(Bit12()).getResized(Bit44())
                    val l2 = resized.shr(12).getResized(Bit12()).getResized(Bit44()) + (l1.toBin().getBit(0) ?: Bin("0", Bit1()))
                    val l3 = resized.shr(12 + 12).getResized(Bit20()).getResized(Bit44()) + (l2.toBin().getBit(0) ?: Bin("0", Bit1()))

                    return listOf(
                        RV64Instr(rawInstr, RV64Syntax.InstrType.LUI, arrayOf(regs[0]), l3.toBin().getUResized(Bit20()).toHex()),
                        RV64Instr(rawInstr, RV64Syntax.InstrType.ADDIW, arrayOf(regs[0], regs[0]), l2.toBin().getUResized(Bit12()).toDec()),
                        RV64Instr(rawInstr, RV64Syntax.InstrType.SLLI, arrayOf(regs[0], regs[0]), Hex("C", Bit6())),
                        RV64Instr(rawInstr, RV64Syntax.InstrType.ADDI, arrayOf(regs[0], regs[0]), l1.toBin().getUResized(Bit12()).toDec()),
                    )
                }

                result = imm.check(Bit56())
                if (result.valid) {
                    val resized = imm.toBin().getResized(Bit56())
                    if (DebugTools.RV64_showLIDecisions) nativeLog("Decided 56 Bit Signed for ${resized.toHex()}")
                    /**
                     *  val64 = lui + addiw + addi3 + addi2 + addi1
                     *
                     *  LUI
                     *  ADDIW
                     *  SLLI 12
                     *  ADDI
                     *  SLLI 12
                     *  ADDI
                     */
                    val l1 = resized.getResized(Bit12()).getResized(Bit56())
                    val l2 = resized.shr(12).getResized(Bit12()).getResized(Bit56()) + (l1.toBin().getBit(0) ?: Bin("0", Bit1()))
                    val l3 = resized.shr(12 + 12).getResized(Bit12()).getResized(Bit56()) + (l2.toBin().getBit(0) ?: Bin("0", Bit1()))
                    val l4 = resized.shr(12 + 12 + 12).getResized(Bit20()).getResized(Bit56()) + (l3.toBin().getBit(0) ?: Bin("0", Bit1()))

                    return listOf(
                        RV64Instr(rawInstr, RV64Syntax.InstrType.LUI, arrayOf(regs[0]), l4.toBin().getUResized(Bit20()).toHex()),
                        RV64Instr(rawInstr, RV64Syntax.InstrType.ADDIW, arrayOf(regs[0], regs[0]), l3.toBin().getUResized(Bit12()).toDec()),
                        RV64Instr(rawInstr, RV64Syntax.InstrType.SLLI, arrayOf(regs[0], regs[0]), Hex("C", Bit6())),
                        RV64Instr(rawInstr, RV64Syntax.InstrType.ADDI, arrayOf(regs[0], regs[0]), l2.toBin().getUResized(Bit12()).toDec()),
                        RV64Instr(rawInstr, RV64Syntax.InstrType.SLLI, arrayOf(regs[0], regs[0]), Hex("C", Bit6())),
                        RV64Instr(rawInstr, RV64Syntax.InstrType.ADDI, arrayOf(regs[0], regs[0]), l1.toBin().getUResized(Bit12()).toDec()),
                    )
                }

                result = imm.check(Bit64())
                val unsignedResult = imm.toBin().checkSizeUnsigned(Bit64()) == null
                if (result.valid || unsignedResult) {
                    val resized = imm.toBin().getUResized(Bit64())
                    if (DebugTools.RV64_showLIDecisions) nativeLog("Decided 64 Bit Signed")
                    /**
                     *  val64 = lui + addiw + addi3 + addi2 + addi1
                     *
                     *  LUI
                     *  ADDIW
                     *  SLLI 12
                     *  ADDI
                     *  SLLI 13
                     *  ADDI
                     *  SLLI 12
                     *  ADDI
                     */

                    val l1 = resized.getResized(Bit12()).getResized(Bit64())
                    val l2 = resized.shr(12).getResized(Bit12()).getResized(Bit64()) + (l1.toBin().getBit(0) ?: Bin("0", Bit1()))
                    val l3 = resized.shr(12 + 13).getResized(Bit12()).getResized(Bit64()) + (l2.toBin().getBit(0) ?: Bin("0", Bit1()))
                    val l4 = resized.shr(12 + 13 + 12).getResized(Bit12()).getResized(Bit64()) + (l3.toBin().getBit(0) ?: Bin("0", Bit1()))
                    val l5 = resized.shr(12 + 13 + 12 + 12) + (l4.toBin().getBit(0) ?: Bin("0", Bit1()))

                    return listOf(
                        RV64Instr(rawInstr, RV64Syntax.InstrType.LUI, arrayOf(regs[0]), l5.toBin().getUResized(Bit20()).toHex()),
                        RV64Instr(rawInstr, RV64Syntax.InstrType.ADDIW, arrayOf(regs[0], regs[0]), l4.toBin().getUResized(Bit12()).toDec()),
                        RV64Instr(rawInstr, RV64Syntax.InstrType.SLLI, arrayOf(regs[0], regs[0]), Hex("C", Bit6())),
                        RV64Instr(rawInstr, RV64Syntax.InstrType.ADDI, arrayOf(regs[0], regs[0]), l3.toBin().getUResized(Bit12()).toDec()),
                        RV64Instr(rawInstr, RV64Syntax.InstrType.SLLI, arrayOf(regs[0], regs[0]), Hex("D", Bit6())),
                        RV64Instr(rawInstr, RV64Syntax.InstrType.ADDI, arrayOf(regs[0], regs[0]), l2.toBin().getUResized(Bit12()).toDec()),
                        RV64Instr(rawInstr, RV64Syntax.InstrType.SLLI, arrayOf(regs[0], regs[0]), Hex("C", Bit6())),
                        RV64Instr(rawInstr, RV64Syntax.InstrType.ADDI, arrayOf(regs[0], regs[0]), l1.getUResized(Bit12()).toDec())
                    )
                }

                throw Parser.ParserError(immediate.getAllTokens().first(), "Expression (${immediate.print("")}) exceeds 64 Bits!")
            }

            else -> {
                val imm = immediate?.checkInstrType(type)
                listOf(RV64Instr(rawInstr, type, regs, imm ?: Dec("0", Bit32()), label = label))
            }
        }
    }

}