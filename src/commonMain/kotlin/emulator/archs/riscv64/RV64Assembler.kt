package emulator.archs.riscv64

import emulator.kit.assembler.DirTypeInterface
import emulator.kit.assembler.InstrTypeInterface
import emulator.kit.assembler.gas.DefinedAssembly
import emulator.kit.assembler.gas.GASParser
import emulator.kit.assembler.gas.nodes.GASNode
import emulator.kit.assembler.gas.riscv.GASRVDirType
import emulator.kit.assembler.lexer.Token
import emulator.kit.assembler.parser.Parser
import emulator.kit.common.Memory
import emulator.kit.common.RegContainer
import emulator.kit.optional.Feature
import emulator.kit.types.Variable
import emulator.kit.types.Variable.Value.*
import emulator.kit.types.Variable.Size.*

class RV64Assembler : DefinedAssembly {
    override val MEM_ADDRESS_SIZE: Variable.Size = RV64.XLEN
    override val WORD_SIZE: Variable.Size = RV64.WORD_WIDTH
    override val INSTRS_ARE_WORD_ALIGNED: Boolean = true
    override val detectRegistersByName: Boolean = true
    override val numberPrefixes: DefinedAssembly.NumberPrefixes = object : DefinedAssembly.NumberPrefixes {
        override val hex: String = "0x"
        override val bin: String = "0b"
        override val dec: String = ""
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

        throw Parser.ParserError(rawInstr.instrName, "Invalid Arguments for ${rawInstr.instrName.instr?.getDetectionName() ?: rawInstr.instrName} ${rawInstr.remainingTokens.joinToString { it.toString() }}")
    }

    class RV64Instr(val rawInstr: GASNode.RawInstr, val type: RV64Syntax.InstrType, val regs: Array<RegContainer.Register> = emptyArray(), val immediate: Variable.Value = Dec("0", Bit32()), val label: GASNode.NumericExpr? = null) : GASParser.SecContent {
        override val bytesNeeded: Int = type.memWords * 4
        override fun getFirstToken(): Token = rawInstr.instrName
        override fun getMark(): Memory.InstanceType = Memory.InstanceType.PROGRAM
        override fun getBinaryArray(yourAddr: Variable.Value, labels: List<Pair<GASParser.Label, Hex>>): Array<Bin> {
            label?.assignIdentifier(labels)
            val labelAddr = label?.evaluate(true)?.toHex()
            return RV64BinMapper.getBinaryFromInstrDef(this, yourAddr.toHex(), labelAddr ?: Hex("0", yourAddr.size), immediate)
        }

        override fun getContentString(): String = "${type.id.lowercase()} ${type.paramType.getContentString(this)}"
    }

    private fun GASNode.NumericExpr.checkInstrType(type: RV64Syntax.InstrType): Variable.Value {
        when (type.paramType) {
            RV64Syntax.ParamType.RD_I20 -> {
                val immediate = this.evaluate(false).toDec()
                val check = immediate.check(Bit20())
                if (!check.valid) throw Parser.ParserError(this.getAllTokens().first(), "Numeric Expression exceeds 20 Bits!")

                return immediate.getResized(Bit20())
            }

            RV64Syntax.ParamType.RD_Off12 -> {
                val immediate = this.evaluate(false).toDec()
                val check = immediate.check(Bit12())
                if (!check.valid) throw Parser.ParserError(this.getAllTokens().first(), "Numeric Expression exceeds 12 Bits!")

                return immediate.getResized(Bit12())
            }

            RV64Syntax.ParamType.RS2_Off12 -> {
                val immediate = this.evaluate(false).toDec()
                val check = immediate.check(Bit12())
                if (!check.valid) throw Parser.ParserError(this.getAllTokens().first(), "Numeric Expression exceeds 12 Bits!")

                return immediate.getResized(Bit12())
            }

            RV64Syntax.ParamType.RD_RS1_RS2 -> {
                throw Parser.ParserError(this.getAllTokens().first(), "Numeric Expression wasn't expected!")
            }

            RV64Syntax.ParamType.RD_RS1_I12 -> {
                val immediate = this.evaluate(false).toDec()
                val check = immediate.check(Bit12())
                if (!check.valid) throw Parser.ParserError(this.getAllTokens().first(), "Numeric Expression exceeds 12 Bits!")

                return immediate.getResized(Bit12())
            }

            RV64Syntax.ParamType.RD_RS1_SHAMT6 -> {
                val immediate = this.evaluate(false).toUDec()
                val check = immediate.check(Bit6())
                if (!check.valid) throw Parser.ParserError(this.getAllTokens().first(), "Numeric Expression exceeds 6 Bits!")

                return immediate.toBin().getResized(Bit6())
            }

            RV64Syntax.ParamType.RS1_RS2_I12 -> {
                val immediate = this.evaluate(false).toDec()
                val check = immediate.check(Bit12())
                if (!check.valid) throw Parser.ParserError(this.getAllTokens().first(), "Numeric Expression exceeds 12 Bits!")

                return immediate.getResized(Bit12())
            }

            RV64Syntax.ParamType.RS1_RS2_LBL -> {
                val immediate = this.evaluate(false)
                val check = immediate.check(Bit64())
                if (!check.valid) throw Parser.ParserError(this.getAllTokens().first(), "Label Expression exceeds 64 Bits!")

                return immediate.toUDec().getUResized(Bit64())
            }

            RV64Syntax.ParamType.CSR_RD_OFF12_RS1 -> {
                throw Parser.ParserError(this.getAllTokens().first(), "Numeric Expression wasn't expected!")
            }

            RV64Syntax.ParamType.CSR_RD_OFF12_UIMM5 -> {
                val immediate = this.evaluate(false).toUDec()
                val check = immediate.check(Bit5())
                if (!check.valid) throw Parser.ParserError(this.getAllTokens().first(), "Numeric Expression exceeds 5 Bits!")

                return immediate.toBin().getResized(Bit12())
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

                when (imm) {
                    is Bin, is Hex, is Oct, is UDec -> {
                        val bin = imm.toBin()

                        var result = bin.check(Bit12())
                        if (result.valid && result.corrected.first() == '0') {
                            return listOf(RV64Instr(rawInstr, RV64Syntax.InstrType.ADDI, arrayOf(regs[0], regs[0]), bin.getUResized(Bit12())))
                        }

                        result = bin.check(Bit32())
                        if (result.valid && result.corrected.first() == '0') {

                        }

                        listOf()
                    }

                    is Dec -> {
                        if (imm.check(Bit12()).valid) {
                            return listOf(RV64Instr(rawInstr, RV64Syntax.InstrType.ADDI, arrayOf(regs[0], regs[0]), imm.getResized(Bit12())))
                        }

                        if (imm.check(Bit32()).valid) {

                        }


                        listOf()
                    }
                }
            }

            else -> {
                val imm = immediate?.checkInstrType(type)
                listOf(RV64Instr(rawInstr, type, regs, imm ?: Dec("0", Bit32()), label = label))
            }
        }
    }

}