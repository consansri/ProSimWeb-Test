package emulator.archs.riscv32

import debug.DebugTools
import emulator.archs.riscv32.RV32Syntax.ParamType.*
import emulator.archs.riscv64.RVDirType
import emulator.kit.assembler.AsmHeader
import emulator.kit.assembler.DirTypeInterface
import emulator.kit.assembler.InstrTypeInterface
import emulator.kit.assembler.gas.GASNode
import emulator.kit.assembler.gas.GASParser
import emulator.kit.assembler.lexer.Lexer
import emulator.kit.assembler.lexer.Token
import emulator.kit.assembler.parser.Parser
import emulator.kit.common.RegContainer
import emulator.kit.common.memory.Memory
import emulator.kit.nativeLog
import emulator.kit.optional.Feature
import emulator.kit.types.Variable
import emulator.kit.types.Variable.Size.*
import emulator.kit.types.Variable.Value.*

class RV32Assembler : AsmHeader {
    override val memAddrSize: Variable.Size = RV32.MEM_ADDRESS_WIDTH
    override val wordSize: Variable.Size = RV32.WORD_WIDTH
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

    class RV32Instr(val rawInstr: GASNode.RawInstr, val type: RV32Syntax.InstrType, val regs: Array<RegContainer.Register> = emptyArray(), val immediate: Variable.Value = Dec("0", Bit32()), val label: GASNode.NumericExpr? = null) : GASParser.SecContent {
        override val bytesNeeded: Int = type.memWords * 4
        override fun getFirstToken(): Token = rawInstr.instrName
        override fun allTokensIncludingPseudo(): List<Token> = rawInstr.tokensIncludingReferences()
        override fun getMark(): Memory.InstanceType = Memory.InstanceType.PROGRAM
        override fun getBinaryArray(yourAddr: Variable.Value, labels: List<Pair<GASParser.Label, Hex>>): Array<Bin> {
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
                val check = immediate.check(Bit20())
                if (!check.valid) throw Parser.ParserError(this.tokens().first(), "Numeric Expression exceeds 20 Bits!")

                return immediate.getResized(Bit20())
            }

            RD_OFF12 -> {
                val immediate = this.evaluate(false)
                val check = immediate.check(Bit12())
                if (!check.valid) throw Parser.ParserError(this.tokens().first(), "Numeric Expression exceeds 12 Bits!")

                return immediate.getResized(Bit12())
            }

            RS2_OFF12 -> {
                val immediate = this.evaluate(false)
                val check = immediate.check(Bit12())
                if (!check.valid) throw Parser.ParserError(this.tokens().first(), "Numeric Expression exceeds 12 Bits!")

                return immediate.getResized(Bit12())
            }

            RD_RS1_SHAMT5 -> {
                val immediate = this.evaluate(false).toBin()
                val check = immediate.checkSizeUnsigned(Bit5())
                if (check != null) throw Parser.ParserError(this.tokens().first(), "Numeric Expression exceeds 5 Bits!")

                return immediate.getUResized(Bit5()).toDec()
            }

            RD_RS1_RS2 -> {
                throw Parser.ParserError(this.tokens().first(), "Numeric Expression wasn't expected!")
            }

            RD_RS1_I12 -> {
                val immediate = this.evaluate(false)
                val check = immediate.check(Bit12())
                if (!check.valid) throw Parser.ParserError(this.tokens().first(), "Numeric Expression exceeds 12 Bits!")

                return immediate.getResized(Bit12())
            }

            RS1_RS2_I12 -> {
                val immediate = this.evaluate(false)
                val check = immediate.check(Bit12())
                if (!check.valid) throw Parser.ParserError(this.tokens().first(), "Numeric Expression exceeds 12 Bits!")

                return immediate.getResized(Bit12())
            }

            CSR_RD_OFF12_RS1 -> {
                throw Parser.ParserError(this.tokens().first(), "Numeric Expression wasn't expected!")
            }

            CSR_RD_OFF12_UIMM5 -> {
                val immediate = this.evaluate(false).toBin()
                val check = immediate.checkSizeSigned(Bit5())
                if (check != null) throw Parser.ParserError(this.tokens().first(), "Numeric Expression exceeds 5 Bits!")

                return immediate.getUResized(Bit5()).toDec()
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

                var result = imm.check(Bit12())
                if (result.valid) {
                    if (DebugTools.RV64_showLIDecisions) nativeLog("Decided 12 Bit Signed")
                    return listOf(RV32Instr(rawInstr, RV32Syntax.InstrType.ADDI, arrayOf(regs[0], regs[0]), imm.getResized(Bit12())))
                }

                result = imm.check(Bit32())
                val unsignedResult = imm.toBin().checkSizeUnsigned(Bit32()) == null
                if (result.valid || unsignedResult) {
                    val resized = imm.toBin().getUResized(Bit32()).toBin()
                    if (DebugTools.RV64_showLIDecisions) nativeLog("Decided 32 Bit Signed")
                    // resized = upper + lower
                    // upper = resized - lower
                    val lower = resized.getResized(Bit12()).getResized(Bit32())
                    val upper = (resized - lower).toBin()

                    val imm20 = Bin(upper.getRawBinStr().substring(0, 20), Bit20()).toHex()
                    val imm12 = Bin(lower.getRawBinStr().substring(20), Bit12()).toHex().toDec()

                    return listOf(
                        RV32Instr(rawInstr, RV32Syntax.InstrType.LUI, arrayOf(regs[0]), imm20),
                        RV32Instr(rawInstr, RV32Syntax.InstrType.ADDI, arrayOf(regs[0], regs[0]), imm12)
                    )
                }

                throw Parser.ParserError(immediate.tokens().first(), "Expression (${immediate.print("")}) exceeds 32 Bits!")
            }

            else -> {
                val imm = immediate?.checkInstrType(type)
                listOf(RV32Instr(rawInstr, type, regs, imm ?: Dec("0", Bit32()), label = label))
            }
        }
    }


}