package emulator.archs.riscv32

import emulator.kit.assembler.DirTypeInterface
import emulator.kit.assembler.InstrTypeInterface
import emulator.kit.assembler.gas.DefinedAssembly
import emulator.kit.assembler.gas.GASParser
import emulator.kit.assembler.gas.nodes.GASNode
import emulator.kit.assembler.gas.riscv.GASRVDirType
import emulator.kit.assembler.parser.Parser
import emulator.kit.common.RegContainer
import emulator.kit.optional.Feature
import emulator.kit.types.Variable
import emulator.archs.riscv32.RV32Syntax.ParamType.*
import emulator.kit.assembler.lexer.Token
import emulator.kit.common.Memory

class RV32Assembler : DefinedAssembly {
    override val MEM_ADDRESS_SIZE: Variable.Size = RV32.MEM_ADDRESS_WIDTH
    override val WORD_SIZE: Variable.Size = RV32.WORD_WIDTH
    override val INSTRS_ARE_WORD_ALIGNED: Boolean = true
    override val detectRegistersByName: Boolean = true
    override val numberPrefixes: DefinedAssembly.NumberPrefixes = object : DefinedAssembly.NumberPrefixes {
        override val bin: String = "0b"
        override val dec: String = ""
        override val hex: String = "0x"
    }

    override fun getInstrs(features: List<Feature>): List<InstrTypeInterface> {
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

    override fun getAdditionalDirectives(): List<DirTypeInterface> = GASRVDirType.entries

    override fun parseInstrParams(rawInstr: GASNode.RawInstr, tempContainer: GASParser.TempContainer): List<GASParser.SecContent> {
        val types = RV32Syntax.InstrType.entries.filter { it.getDetectionName() == rawInstr.instrName.instr?.getDetectionName() }

        for (type in types) {
            val paramType = type.paramType
            val result = paramType.tokenSeq?.matchStart(rawInstr.remainingTokens, listOf(), this, tempContainer.symbols) ?: continue
            if (!result.matches) continue

            val expr = result.matchingNodes.filterIsInstance<GASNode.NumericExpr>().firstOrNull()
            val immExpr = if(expr != null && expr.isDefined()) expr else null
            val labelExpr = if(expr !=null && !expr.isDefined()) expr else null
            val regs = result.matchingTokens.mapNotNull { it.reg }

            return getNonPseudoInstructions(rawInstr, type, regs.toTypedArray(), immExpr, labelExpr)
        }

        throw Parser.ParserError(rawInstr.instrName, "Invalid Arguments for ${rawInstr.instrName.instr?.getDetectionName() ?: rawInstr.instrName} ${rawInstr.remainingTokens.joinToString { it.toString() }}")
    }

    class RV32Instr(val rawInstr: GASNode.RawInstr, val type: RV32Syntax.InstrType, val regs: Array<RegContainer.Register> = emptyArray(), val immediate: Variable.Value = Variable.Value.Dec("0", Variable.Size.Bit32()), val label: GASNode.NumericExpr? = null) : GASParser.SecContent {
        override val bytesNeeded: Int = type.memWords * 4
        override fun getFirstToken(): Token = rawInstr.instrName
        override fun getMark(): Memory.InstanceType = Memory.InstanceType.PROGRAM
        override fun getBinaryArray(yourAddr: Variable.Value, labels: List<Pair<GASParser.Label, Variable.Value.Hex>>): Array<Variable.Value.Bin> {
            label?.assignIdentifier(labels)
            val labelAddr = label?.evaluate(true)?.toHex()

            return RV32BinMapper.getBinaryFromInstrDef(this, yourAddr.toHex(), labelAddr ?: Variable.Value.Hex("0", yourAddr.size), immediate)
        }

        override fun getContentString(): String = "${type.id} ${type.paramType}"
    }

    private fun GASNode.NumericExpr.checkInstrType(type: RV32Syntax.InstrType): Variable.Value {
        when (type.paramType) {
            RD_I20 -> {
                val immediate = this.evaluate(false).toDec()
                val check = immediate.check(Variable.Size.Bit20())
                if (!check.valid) throw Parser.ParserError(this.getAllTokens().first(), "Numeric Expression exceeds 20 Bits!")

                return immediate.getResized(Variable.Size.Bit20())
            }

            RD_OFF12 -> {
                val immediate = this.evaluate(false).toDec()
                val check = immediate.check(Variable.Size.Bit12())
                if (!check.valid) throw Parser.ParserError(this.getAllTokens().first(), "Numeric Expression exceeds 12 Bits!")

                return immediate.getResized(Variable.Size.Bit12())
            }

            RS2_OFF12 -> {
                val immediate = this.evaluate(false).toDec()
                val check = immediate.check(Variable.Size.Bit12())
                if (!check.valid) throw Parser.ParserError(this.getAllTokens().first(), "Numeric Expression exceeds 12 Bits!")

                return immediate.getResized(Variable.Size.Bit12())
            }

            RD_RS1_SHAMT5 -> {
                val immediate = this.evaluate(false).toBin()
                val check = immediate.check(Variable.Size.Bit5())
                if (!check.valid) throw Parser.ParserError(this.getAllTokens().first(), "Numeric Expression exceeds 5 Bits!")

                return immediate.getUResized(Variable.Size.Bit5())
            }

            RD_RS1_RS2 -> {
                throw Parser.ParserError(this.getAllTokens().first(), "Numeric Expression wasn't expected!")
            }

            RD_RS1_I12 -> {
                val immediate = this.evaluate(false).toDec()
                val check = immediate.check(Variable.Size.Bit12())
                if (!check.valid) throw Parser.ParserError(this.getAllTokens().first(), "Numeric Expression exceeds 12 Bits!")

                return immediate.getResized(Variable.Size.Bit12())
            }

            RS1_RS2_I12 -> {
                val immediate = this.evaluate(false).toDec()
                val check = immediate.check(Variable.Size.Bit12())
                if (!check.valid) throw Parser.ParserError(this.getAllTokens().first(), "Numeric Expression exceeds 12 Bits!")

                return immediate.getResized(Variable.Size.Bit12())
            }

            CSR_RD_OFF12_RS1 -> {
                throw Parser.ParserError(this.getAllTokens().first(), "Numeric Expression wasn't expected!")
            }

            CSR_RD_OFF12_UIMM5 -> {
                val immediate = this.evaluate(false).toBin()
                val check = immediate.check(Variable.Size.Bit5())
                if (!check.valid) throw Parser.ParserError(this.getAllTokens().first(), "Numeric Expression exceeds 5 Bits!")

                return immediate.getResized(Variable.Size.Bit12())
            }

            PS_RD_I32 -> {
                val immediate = this.evaluate(false).toDec()
                val check = immediate.check(Variable.Size.Bit32())
                if (!check.valid) throw Parser.ParserError(this.getAllTokens().first(), "Numeric Expression exceeds 32 Bits!")

                return immediate.getResized(Variable.Size.Bit32())
            }

            PS_RD_RS1 -> throw Parser.ParserError(this.getAllTokens().first(), "Numeric Expression wasn't expected!")
            PS_RS1 -> throw Parser.ParserError(this.getAllTokens().first(), "Numeric Expression wasn't expected!")
            PS_CSR_RS1 -> throw Parser.ParserError(this.getAllTokens().first(), "Numeric Expression wasn't expected!")
            PS_RD_CSR -> throw Parser.ParserError(this.getAllTokens().first(), "Numeric Expression wasn't expected!")
            NONE -> throw Parser.ParserError(this.getAllTokens().first(), "Numeric Expression wasn't expected!")
            PS_NONE -> throw Parser.ParserError(this.getAllTokens().first(), "Numeric Expression wasn't expected!")
            RS1_RS2_LBL -> throw Parser.ParserError(this.getAllTokens().first(), "Numeric Expression wasn't expected!")
            PS_RS1_JLBL -> throw Parser.ParserError(this.getAllTokens().first(), "Numeric Expression wasn't expected!")
            PS_RD_ALBL -> throw Parser.ParserError(this.getAllTokens().first(), "Numeric Expression wasn't expected!")
            PS_JLBL -> throw Parser.ParserError(this.getAllTokens().first(), "Numeric Expression wasn't expected!")
        }
    }

    private fun getNonPseudoInstructions(rawInstr: GASNode.RawInstr, type: RV32Syntax.InstrType, regs: Array<RegContainer.Register>, immediate: GASNode.NumericExpr? = null, label: GASNode.NumericExpr? = null): List<RV32Instr> {
        return when (type) {
            RV32Syntax.InstrType.Li -> {
                val imm = immediate?.checkInstrType(type) ?: throw Parser.ParserError(rawInstr.getAllTokens().first(), "Numeric Expression is Missing!")
                when (imm) {
                    is Variable.Value.Bin, is Variable.Value.Hex, is Variable.Value.Oct, is Variable.Value.UDec -> {
                        val imm = imm.toBin()
                    }

                    is Variable.Value.Dec -> {


                    }
                }


                listOf()
            }

            else -> {
                val imm = immediate?.checkInstrType(type)
                listOf(RV32Instr(rawInstr, type, regs, imm ?: Variable.Value.Dec("0", Variable.Size.Bit32()), label = label))
            }
        }
    }


}