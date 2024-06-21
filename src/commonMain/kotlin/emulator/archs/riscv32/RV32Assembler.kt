package emulator.archs.riscv32

import debug.DebugTools
import emulator.archs.riscv32.RV32Syntax.ParamType.*
import emulator.archs.riscv64.RVDirType
import emulator.core.Size
import emulator.core.Size.*
import emulator.core.Value
import emulator.core.Value.*
import emulator.kit.assembler.AsmHeader
import emulator.kit.assembler.DirTypeInterface
import emulator.kit.assembler.InstrTypeInterface
import emulator.kit.assembler.gas.GASNode
import emulator.kit.assembler.gas.GASParser
import emulator.kit.assembler.lexer.Lexer
import emulator.kit.assembler.lexer.Token
import emulator.kit.assembler.parser.Parser
import emulator.kit.common.RegContainer
import emulator.kit.memory.Memory
import emulator.kit.nativeLog
import emulator.kit.optional.Feature

object RV32Assembler : AsmHeader {
    override val memAddrSize: Size = RV32.MEM_ADDRESS_WIDTH
    override val wordSize: Size = RV32.WORD_WIDTH
    override val detectRegistersByName: Boolean = true
    override val addrShift: Int = 0
    override val prefices: Lexer.Prefices = object : Lexer.Prefices {
        override val hex: String = "0x"
        override val bin: String = "0b"
        override val dec: String = ""
        override val oct: String = "0"
        override val comment: String = "#"
        override val symbol: Regex = Regex("""^[a-zA-Z$._][a-zA-Z0-9$._]*""")
    }

    override fun instrTypes(features: List<Feature>): List<InstrTypeInterface> {
        val instrList = RV32Syntax.InstrType.entries.toMutableList()

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

    override fun additionalDirectives(): List<DirTypeInterface> = RVDirType.entries

    override fun parseInstrParams(rawInstr: GASNode.RawInstr, tempContainer: GASParser.TempContainer): List<GASParser.SecContent> {
        val types = RV32Syntax.InstrType.entries.filter { it.getDetectionName() == rawInstr.instrName.instr?.getDetectionName() }

        for (type in types) {
            val paramType = type.paramType
            val result = paramType.rule?.matchStart(rawInstr.remainingTokens, listOf(), this, tempContainer.symbols)
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

    class RV32Instr(val rawInstr: GASNode.RawInstr, val type: RV32Syntax.InstrType, val regs: Array<RegContainer.Register> = emptyArray(), val immediate: Value = Dec("0", Bit32), val label: GASNode.NumericExpr? = null) : GASParser.SecContent {
        override val bytesNeeded: Int = type.memWords * 4
        override fun getFirstToken(): Token = rawInstr.instrName
        override fun allTokensIncludingPseudo(): List<Token> = rawInstr.tokensIncludingReferences()
        override fun getMark(): Memory.InstanceType = Memory.InstanceType.PROGRAM
        override fun getBinaryArray(yourAddr: Value, labels: List<Pair<GASParser.Label, Hex>>): Array<Bin> {
            label?.assignLabels(labels)
            val labelAddr = label?.evaluate(true)?.toHex()

            return RV32BinMapper.getBinaryFromInstrDef(this, yourAddr.toHex(), labelAddr ?: Hex("0", yourAddr.size), immediate)
        }

        override fun getContentString(): String = "${type.id} ${type.paramType.getContentString(this)}"
    }

    private fun GASNode.NumericExpr.checkInstrType(type: RV32Syntax.InstrType): Dec {
        when (type.paramType) {
            RD_I20 -> {
                val immediate = this.evaluate(false)
                if (!immediate.checkSizeSignedOrUnsigned(Bit20)) throw Parser.ParserError(this.tokens().first(), "Numeric Expression exceeds 20 Bits!")

                return immediate.getResized(Bit20)
            }

            RD_OFF12 -> {
                val immediate = this.evaluate(false)
                if (!immediate.checkSizeSignedOrUnsigned(Bit12)) throw Parser.ParserError(this.tokens().first(), "Numeric Expression exceeds 12 Bits!")

                return immediate.getResized(Bit12)
            }

            RS2_OFF12 -> {
                val immediate = this.evaluate(false)
                if (!immediate.checkSizeSignedOrUnsigned(Bit12)) throw Parser.ParserError(this.tokens().first(), "Numeric Expression exceeds 12 Bits!")

                return immediate.getResized(Bit12)
            }

            RD_RS1_SHAMT5 -> {
                val immediate = this.evaluate(false).toBin()
                if (!immediate.checkSizeUnsigned(Bit5)) throw Parser.ParserError(this.tokens().first(), "Numeric Expression exceeds 5 Bits!")

                return immediate.getUResized(Bit5).toDec()
            }

            RD_RS1_RS2 -> {
                throw Parser.ParserError(this.tokens().first(), "Numeric Expression wasn't expected!")
            }

            RD_RS1_I12 -> {
                val immediate = this.evaluate(false)
                if (!immediate.checkSizeSignedOrUnsigned(Bit12)) throw Parser.ParserError(this.tokens().first(), "Numeric Expression exceeds 12 Bits!")

                return immediate.getResized(Bit12)
            }

            RS1_RS2_I12 -> {
                val immediate = this.evaluate(false)
                if (!immediate.checkSizeSignedOrUnsigned(Bit12)) throw Parser.ParserError(this.tokens().first(), "Numeric Expression exceeds 12 Bits!")

                return immediate.getResized(Bit12)
            }

            CSR_RD_OFF12_RS1 -> {
                throw Parser.ParserError(this.tokens().first(), "Numeric Expression wasn't expected!")
            }

            CSR_RD_OFF12_UIMM5 -> {
                val immediate = this.evaluate(false).toBin()
                if (!immediate.checkSizeUnsigned(Bit5)) throw Parser.ParserError(this.tokens().first(), "Numeric Expression exceeds 5 Bits!")

                return immediate.getUResized(Bit5).toDec()
            }

            PS_RD_I32 -> {
                return this.evaluate(false)
            }

            PS_RD_RS1 -> throw Parser.ParserError(this.tokens().first(), "Numeric Expression wasn't expected!")
            PS_RS1 -> throw Parser.ParserError(this.tokens().first(), "Numeric Expression wasn't expected!")
            PS_CSR_RS1 -> throw Parser.ParserError(this.tokens().first(), "Numeric Expression wasn't expected!")
            PS_RD_CSR -> throw Parser.ParserError(this.tokens().first(), "Numeric Expression wasn't expected!")
            NONE -> throw Parser.ParserError(this.tokens().first(), "Numeric Expression wasn't expected!")
            PS_NONE -> throw Parser.ParserError(this.tokens().first(), "Numeric Expression wasn't expected!")
            RS1_RS2_LBL -> throw Parser.ParserError(this.tokens().first(), "Numeric Expression wasn't expected!")
            PS_RS1_JLBL -> throw Parser.ParserError(this.tokens().first(), "Numeric Expression wasn't expected!")
            PS_RD_ALBL -> throw Parser.ParserError(this.tokens().first(), "Numeric Expression wasn't expected!")
            PS_JLBL -> throw Parser.ParserError(this.tokens().first(), "Numeric Expression wasn't expected!")
        }
    }

    private fun getNonPseudoInstructions(rawInstr: GASNode.RawInstr, type: RV32Syntax.InstrType, regs: Array<RegContainer.Register>, immediate: GASNode.NumericExpr? = null, label: GASNode.NumericExpr? = null): List<RV32Instr> {
        return when (type) {
            RV32Syntax.InstrType.Li -> {
                val imm = immediate?.checkInstrType(type) ?: throw Parser.ParserError(rawInstr.tokens().first(), "Numeric Expression is Missing!")

                if (imm.checkSizeSigned(Bit12)) {
                    if (DebugTools.RV64_showLIDecisions) nativeLog("Decided 12 Bit Signed")
                    val zeroReg = RV32.standardRegFile.unsortedRegisters.first { it.names.contains("x0") }
                    return listOf(RV32Instr(rawInstr, RV32Syntax.InstrType.ADDI, arrayOf(regs[0], zeroReg), imm.getResized(Bit12)))
                }

                if (imm.checkSizeSignedOrUnsigned(Bit32)) {
                    val resized = imm.toBin().getUResized(Bit32).toBin()
                    if (DebugTools.RV64_showLIDecisions) nativeLog("Decided 32 Bit Signed")
                    // resized = upper + lower
                    // upper = resized - lower
                    val lower = resized.getResized(Bit12).getResized(Bit32)
                    val upper = (resized - lower).toBin()

                    val imm20 = Bin(upper.toRawString().substring(0, 20), Bit20).toHex()
                    val imm12 = Bin(lower.toRawString().substring(20), Bit12).toHex().toDec()

                    return listOf(
                        RV32Instr(rawInstr, RV32Syntax.InstrType.LUI, arrayOf(regs[0]), imm20),
                        RV32Instr(rawInstr, RV32Syntax.InstrType.ADDI, arrayOf(regs[0], regs[0]), imm12)
                    )
                }

                throw Parser.ParserError(immediate.tokens().first(), "Expression (${immediate.print("")}) exceeds 32 Bits!")
            }

            else -> {
                val imm = immediate?.checkInstrType(type)
                listOf(RV32Instr(rawInstr, type, regs, imm ?: Dec("0", Bit32), label = label))
            }
        }
    }


}