package emulator.archs.ikrmini

import emulator.kit.Architecture
import emulator.kit.assembly.Compiler
import emulator.kit.assembly.Syntax.TokenSeq.Component.*
import emulator.kit.assembly.Syntax.TokenSeq.Component.InSpecific.*
import emulator.kit.types.Variable.Value.*
import emulator.archs.ikrmini.IKRMini.WORDSIZE
import emulator.kit.assembly.standards.StandardSyntax

class IKRMiniSyntax : StandardSyntax(IKRMini.MEM_ADDRESS_WIDTH, commentStartSymbol = ';', instrParamsCanContainWordsBesideLabels = false) {


    /**
     * Checks beginning for a Instruction
     */
    override fun MutableList<Compiler.Token>.checkInstr(elements: MutableList<TreeNode.ElementNode>, errors: MutableList<Error>, warnings: MutableList<Warning>, currentLabel: StandardSyntax.ELabel?): Boolean {
        for (amode in ParamType.entries) {
            val amodeResult = amode.tokenSeq.matchStart(*this.toTypedArray())

            if (amodeResult.matches) {
                val type = InstrType.entries.firstOrNull { it.name.uppercase() == amodeResult.sequenceMap[0].token.content.uppercase() } ?: return false

                if (!type.paramMap.entries.map { it.key }.contains(amode)) continue
                val imm = amodeResult.sequenceMap.map { it.token }.firstOrNull { it is Compiler.Token.Constant } as Compiler.Token.Constant?

                val eInstr = IKRMiniInstr(type, amode, imm, currentLabel, *amodeResult.sequenceMap.map { it.token }.toTypedArray())

                this.removeAll(eInstr.tokens.toSet())
                elements.add(eInstr)
                return true
            }
        }
        return false
    }

    enum class ParamType(val tokenSeq: TokenSeq, val wordAmount: Int, val exampleString: String) {
        INDIRECT(TokenSeq(Word, Space, Specific("("), Specific("("), WordOrSpecConst(WORDSIZE), Specific(")"), Specific(")"), NewLine, ignoreSpaces = true), 2, "(([16 Bit]))"),
        DIRECT(TokenSeq(Word, Space, Specific("("), WordOrSpecConst(WORDSIZE), Specific(")"), NewLine, ignoreSpaces = true), 2, "([16 Bit])"),
        IMMEDIATE(TokenSeq(Word, Space, Specific("#"), SpecConst(WORDSIZE), NewLine, ignoreSpaces = true), 2, "#[16 Bit]"),
        DESTINATION(TokenSeq(Word, Space, WordOrSpecConst(WORDSIZE), NewLine, ignoreSpaces = true), 2, "[label]"),
        IMPLIED(TokenSeq(Word, NewLine, ignoreSpaces = true), 1, ""),
    }

    enum class InstrType(val paramMap: Map<ParamType, Hex>, val descr: String) {
        // Data Transport
        LOAD(mapOf(ParamType.IMMEDIATE to Hex("010C", WORDSIZE), ParamType.DIRECT to Hex("020C", WORDSIZE), ParamType.INDIRECT to Hex("030C", WORDSIZE)), "load AC"),
        LOADI(mapOf(ParamType.IMPLIED to Hex("200C", WORDSIZE)), "load indirect"),
        STORE(mapOf(ParamType.DIRECT to Hex("3200", WORDSIZE), ParamType.INDIRECT to Hex("3300", WORDSIZE)), "store AC at address"),

        // Data Manipulation
        AND(mapOf(ParamType.IMMEDIATE to Hex("018A", WORDSIZE), ParamType.DIRECT to Hex("028A", WORDSIZE), ParamType.INDIRECT to Hex("038A", WORDSIZE)), "and (logic)"),
        OR(mapOf(ParamType.IMMEDIATE to Hex("0188", WORDSIZE), ParamType.DIRECT to Hex("0288", WORDSIZE), ParamType.INDIRECT to Hex("0388", WORDSIZE)), "or (logic)"),
        XOR(mapOf(ParamType.IMMEDIATE to Hex("0189", WORDSIZE), ParamType.DIRECT to Hex("0289", WORDSIZE), ParamType.INDIRECT to Hex("0389", WORDSIZE)), "xor (logic)"),
        ADD(mapOf(ParamType.IMMEDIATE to Hex("018D", WORDSIZE), ParamType.DIRECT to Hex("028D", WORDSIZE), ParamType.INDIRECT to Hex("038D", WORDSIZE)), "add"),
        ADDC(mapOf(ParamType.IMMEDIATE to Hex("01AD", WORDSIZE), ParamType.DIRECT to Hex("02AD", WORDSIZE), ParamType.INDIRECT to Hex("03AD", WORDSIZE)), "add with carry"),
        SUB(mapOf(ParamType.IMMEDIATE to Hex("018E", WORDSIZE), ParamType.DIRECT to Hex("028E", WORDSIZE), ParamType.INDIRECT to Hex("038E", WORDSIZE)), "sub"),
        SUBC(mapOf(ParamType.IMMEDIATE to Hex("01AE", WORDSIZE), ParamType.DIRECT to Hex("02AE", WORDSIZE), ParamType.INDIRECT to Hex("03AE", WORDSIZE)), "sub with carry"),

        LSL(mapOf(ParamType.IMPLIED to Hex("00A0", WORDSIZE), ParamType.DIRECT to Hex("0220", WORDSIZE), ParamType.INDIRECT to Hex("0320", WORDSIZE)), "logic shift left"),
        LSR(mapOf(ParamType.IMPLIED to Hex("00A1", WORDSIZE), ParamType.DIRECT to Hex("0221", WORDSIZE), ParamType.INDIRECT to Hex("0321", WORDSIZE)), "logic shift right"),
        ROL(mapOf(ParamType.IMPLIED to Hex("00A2", WORDSIZE), ParamType.DIRECT to Hex("0222", WORDSIZE), ParamType.INDIRECT to Hex("0322", WORDSIZE)), "rotate left"),
        ROR(mapOf(ParamType.IMPLIED to Hex("00A3", WORDSIZE), ParamType.DIRECT to Hex("0223", WORDSIZE), ParamType.INDIRECT to Hex("0323", WORDSIZE)), "rotate right"),
        ASL(mapOf(ParamType.IMPLIED to Hex("00A4", WORDSIZE), ParamType.DIRECT to Hex("0224", WORDSIZE), ParamType.INDIRECT to Hex("0324", WORDSIZE)), "arithmetic shift left"),
        ASR(mapOf(ParamType.IMPLIED to Hex("00A5", WORDSIZE), ParamType.DIRECT to Hex("0225", WORDSIZE), ParamType.INDIRECT to Hex("0325", WORDSIZE)), "arithmetic shift right"),

        RCL(mapOf(ParamType.IMPLIED to Hex("00A6", WORDSIZE), ParamType.IMMEDIATE to Hex("0126", WORDSIZE), ParamType.DIRECT to Hex("0226", WORDSIZE), ParamType.INDIRECT to Hex("0326", WORDSIZE)), "rotate left with carry"),
        RCR(mapOf(ParamType.IMPLIED to Hex("00A7", WORDSIZE), ParamType.IMMEDIATE to Hex("0127", WORDSIZE), ParamType.DIRECT to Hex("0227", WORDSIZE), ParamType.INDIRECT to Hex("0327", WORDSIZE)), "rotate right with carry"),
        NOT(mapOf(ParamType.IMPLIED to Hex("008B", WORDSIZE), ParamType.DIRECT to Hex("020B", WORDSIZE), ParamType.INDIRECT to Hex("030B", WORDSIZE)), "invert (logic not)"),

        NEG(mapOf(ParamType.DIRECT to Hex("024E", WORDSIZE), ParamType.INDIRECT to Hex("034E", WORDSIZE)), "negotiate"),

        CLR(mapOf(ParamType.IMPLIED to Hex("004C", WORDSIZE)), "clear"),

        INC(mapOf(ParamType.IMPLIED to Hex("009C", WORDSIZE), ParamType.DIRECT to Hex("021C", WORDSIZE), ParamType.INDIRECT to Hex("031C", WORDSIZE)), "increment (+1)"),
        DEC(mapOf(ParamType.IMPLIED to Hex("009F", WORDSIZE), ParamType.DIRECT to Hex("021F", WORDSIZE), ParamType.INDIRECT to Hex("031F", WORDSIZE)), "decrement (-1)"),

        // Unconditional Branches
        BSR(mapOf(ParamType.DESTINATION to Hex("510C", WORDSIZE)), "branch and save return address in AC"),
        JMP(mapOf(ParamType.IMPLIED to Hex("4000", WORDSIZE)), "jump to address in AC"),
        BRA(mapOf(ParamType.DESTINATION to Hex("6101", WORDSIZE)), "branch"),

        // Conditional Branches
        BHI(mapOf(ParamType.DESTINATION to Hex("6102", WORDSIZE)), "branch if higher"),
        BLS(mapOf(ParamType.DESTINATION to Hex("6103", WORDSIZE)), "branch if lower or same"),
        BCC(mapOf(ParamType.DESTINATION to Hex("6104", WORDSIZE)), "branch if carry clear"),
        BCS(mapOf(ParamType.DESTINATION to Hex("6105", WORDSIZE)), "branch if carry set"),
        BNE(mapOf(ParamType.DESTINATION to Hex("6106", WORDSIZE)), "branch if not equal"),
        BEQ(mapOf(ParamType.DESTINATION to Hex("6107", WORDSIZE)), "branch if equal"),
        BVC(mapOf(ParamType.DESTINATION to Hex("6108", WORDSIZE)), "branch if overflow clear"),
        BVS(mapOf(ParamType.DESTINATION to Hex("6109", WORDSIZE)), "branch if overflow set"),
        BPL(mapOf(ParamType.DESTINATION to Hex("610A", WORDSIZE)), "branch if positive"),
        BMI(mapOf(ParamType.DESTINATION to Hex("610B", WORDSIZE)), "branch if negative"),
        BGE(mapOf(ParamType.DESTINATION to Hex("610C", WORDSIZE)), "branch if greater or equal"),
        BLT(mapOf(ParamType.DESTINATION to Hex("610D", WORDSIZE)), "branch if less than"),
        BGT(mapOf(ParamType.DESTINATION to Hex("610E", WORDSIZE)), "branch if greater than"),
        BLE(mapOf(ParamType.DESTINATION to Hex("610F", WORDSIZE)), "branch if less or equal");

    }

    /**
     * TREE NODES
     */
    class IKRMiniInstr(val type: InstrType, val paramType: ParamType, val imm: Compiler.Token.Constant?, parentLabel: ELabel?, vararg tokens: Compiler.Token) : EInstr(tokens.first(), tokens.drop(1), parentLabel) {
        fun getOpBin(arch: Architecture): Array<Bin> {
            val opCode = type.paramMap[paramType]
            val addr = address
            val lblAddr = linkedLabels.firstOrNull()?.address
            if (opCode == null) {
                arch.getConsole().error("Couldn't resolve opcode for the following combination: ${type.name} and ${paramType.name}")
                return emptyArray()
            }
            val opCodeArray = mutableListOf(*opCode.splitToByteArray().map { it.toBin() }.toTypedArray())

            when (paramType) {
                ParamType.IMMEDIATE, ParamType.DIRECT, ParamType.INDIRECT -> {
                    if(linkedLabels.size == 1 ){
                        if (lblAddr == null) {
                            arch.getConsole().error("Missing imm/address for the following combination: ${type.name} and ${paramType.name}")
                            return emptyArray()
                        }
                        opCodeArray.addAll(lblAddr.toHex().splitToByteArray().map { it.toBin() })
                    }else{
                        if (imm == null) {
                            arch.getConsole().error("Missing imm/address for the following combination: ${type.name} and ${paramType.name}")
                            return emptyArray()
                        }
                        opCodeArray.addAll(imm.getValue(WORDSIZE).toHex().splitToByteArray().map { it.toBin() })
                    }
                }

                ParamType.DESTINATION -> {
                    if(linkedLabels.size == 1){
                        if (lblAddr == null) {
                            arch.getConsole().error("Missing branch destination for the following combination: ${type.name} and ${paramType.name}")
                            return emptyArray()
                        }
                        if (addr == null) {
                            arch.getConsole().error("Missing instruction address for the following combination: ${type.name} and ${paramType.name}")
                            return emptyArray()
                        }
                        val labelOffset = addr - lblAddr
                        opCodeArray.addAll(labelOffset.toHex().splitToByteArray().map { it.toBin() })
                    }else{
                        if (imm == null) {
                            arch.getConsole().error("Missing branch destination for the following combination: ${type.name} and ${paramType.name}")
                            return emptyArray()
                        }
                        if (addr == null) {
                            arch.getConsole().error("Missing instruction address for the following combination: ${type.name} and ${paramType.name}")
                            return emptyArray()
                        }
                        opCodeArray.addAll(imm.getValue(WORDSIZE).toHex().splitToByteArray().map { it.toBin() })
                    }
                }

                ParamType.IMPLIED -> {}
            }
            return opCodeArray.reversed().toTypedArray()
        }
    }


}