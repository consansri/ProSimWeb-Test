package emulator.archs.ikrrisc2

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
import emulator.kit.optional.Feature
import emulator.core.*

object IKRRisc2Assembler : AsmHeader {
    override val memAddrSize: Size = IKRRisc2.WORD_WIDTH
    override val wordSize: Size = IKRRisc2.WORD_WIDTH
    override val detectRegistersByName: Boolean = true
    override val addrShift: Int = 2

    override val prefices: Lexer.Prefices = object : Lexer.Prefices {
        override val hex: String = "0x"
        override val bin: String = "0b"
        override val dec: String = ""
        override val oct: String = "0"
        override val comment: String = ";"
        override val symbol: Regex = Regex("""^[a-zA-Z$._][a-zA-Z0-9$._]*""")
    }

    override fun instrTypes(features: List<Feature>): List<InstrTypeInterface> = InstrType.entries

    override fun additionalDirectives(): List<DirTypeInterface> = listOf()

    override fun parseInstrParams(rawInstr: GASNode.RawInstr, tempContainer: GASParser.TempContainer): List<GASParser.SecContent> {
        val types = InstrType.entries.filter { it.getDetectionName() == rawInstr.instrName.instr?.getDetectionName() }

        for (type in types) {
            val paramType = type.paramType
            val result = paramType.rule.matchStart(rawInstr.remainingTokens, listOf(), this, tempContainer.symbols)

            if (!result.matches) continue

            val expr = result.matchingNodes.filterIsInstance<GASNode.NumericExpr>().firstOrNull()
            val regs = result.matchingTokens.mapNotNull { it.reg }

            return getNonPseudoInstructions(rawInstr, type, regs.toTypedArray(), expr)
        }

        throw Parser.ParserError(
            rawInstr.instrName,
            "Invalid Arguments for ${rawInstr.instrName.instr?.getDetectionName() ?: rawInstr.instrName} ${rawInstr.remainingTokens.joinToString("") { it.toString() }} expected ${rawInstr.instrName.instr?.getDetectionName() ?: rawInstr.instrName} ${types.firstOrNull()?.paramType?.exampleString}"
        )
    }

    class IKRRisc2Instr(val rawInstr: GASNode.RawInstr, val type: InstrType, val regs: Array<RegContainer.Register> = emptyArray(), val immediate: Value = Value.Dec("0", Size.Bit32), val label: GASNode.NumericExpr? = null) : GASParser.SecContent {
        var displacement: Value.Dec? = null
        override val bytesNeeded: Int = 4
        override fun getFirstToken(): Token = rawInstr.instrName

        override fun allTokensIncludingPseudo(): List<Token> = rawInstr.tokensIncludingReferences()

        override fun getMark(): Memory.InstanceType = Memory.InstanceType.PROGRAM

        override fun getBinaryArray(yourAddr: Value, labels: List<Pair<GASParser.Label, Value.Hex>>): Array<Value.Bin> {
            label?.assignLabels(labels)

            displacement = if (label != null) {
                (label.evaluate(true).toHex().getUResized(IKRRisc2.WORD_WIDTH) - yourAddr.toHex()).toDec()
            } else null

            val bin = IKRRisc2BinMapper.generateBinary(this, displacement)

            return arrayOf(bin)
        }

        override fun getContentString(): String = "${type.id} ${type.paramType.getContentString(this)}"
    }

    private fun getNonPseudoInstructions(rawInstr: GASNode.RawInstr, type: InstrType, regs: Array<RegContainer.Register>, expr: GASNode.NumericExpr?): List<IKRRisc2Instr> {
        return when (type) {

            else -> {
                val imm = expr?.checkInstrType(type)
                listOf(IKRRisc2Instr(rawInstr, type, regs, imm ?: Value.Dec("0", Size.Bit32), if (imm == null) expr else null))
            }
        }
    }

    private fun GASNode.NumericExpr.checkInstrType(type: InstrType): Value.Dec? {
        when (type.paramType) {
            ParamType.I_TYPE -> {
                val immediate = this.evaluate(true)
                val check = immediate.check(Size.Bit16)
                if (!check.valid) throw Parser.ParserError(this.tokens().first(), "Numeric Expression exceeds ${Size.Bit16}!")

                return immediate.getResized(IKRRisc2.WORD_WIDTH)
            }

            ParamType.R2_TYPE -> throw Parser.ParserError(this.tokens().first(), "Numeric Expression wasn't expected!")
            ParamType.R1_TYPE -> throw Parser.ParserError(this.tokens().first(), "Numeric Expression wasn't expected!")
            ParamType.L_OFF_TYPE -> {
                val immediate = this.evaluate(true)
                val check = immediate.check(Size.Bit16)
                if (!check.valid) throw Parser.ParserError(this.tokens().first(), "Numeric Expression exceeds ${Size.Bit16}!")

                return immediate.getResized(IKRRisc2.WORD_WIDTH)
            }

            ParamType.L_INDEX_TYPE -> throw Parser.ParserError(this.tokens().first(), "Numeric Expression wasn't expected!")
            ParamType.S_OFF_TYPE -> {
                val immediate = this.evaluate(true)
                val check = immediate.check(Size.Bit16)
                if (!check.valid) throw Parser.ParserError(this.tokens().first(), "Numeric Expression exceeds ${Size.Bit16}!")

                return immediate.getResized(IKRRisc2.WORD_WIDTH)
            }

            ParamType.S_INDEX_TYPE -> throw Parser.ParserError(this.tokens().first(), "Numeric Expression wasn't expected!")
            ParamType.B_DISP18_TYPE -> return null
            ParamType.B_DISP26_TYPE -> return null
            ParamType.B_REG_TYPE -> throw Parser.ParserError(this.tokens().first(), "Numeric Expression wasn't expected!")
        }
    }

}