package emulator.archs.t6502

import emulator.archs.riscv64.RV64Syntax
import emulator.kit.Architecture
import emulator.kit.assembly.Compiler
import emulator.kit.assembly.Syntax
import emulator.kit.common.FileHandler
import emulator.kit.common.Transcript
import emulator.kit.types.Variable.Value.*
import emulator.archs.t6502.T6502Syntax.AModes.*
import emulator.archs.t6502.T6502.BYTE_SIZE
import emulator.kit.assembly.Syntax.TokenSeq.Component.*
import emulator.kit.types.Variable
import emulator.kit.types.Variable.Size.*
import emulator.kit.types.Variable.Value.*
import js.core.toPrimitiveSymbolHolder


/**
 * T6502 Syntax
 *
 */
class T6502Syntax : Syntax() {
    override val applyStandardHLForRest: Boolean = false

    override fun clear() {
        // nothing to do here
    }

    override fun check(
        arch: Architecture,
        compiler: Compiler,
        tokens: List<Compiler.Token>,
        tokenLines: List<List<Compiler.Token>>,
        others: List<FileHandler.File>,
        transcript: Transcript
    ): SyntaxTree {

        val remainingTokens = tokens.toMutableList()

        // For Root Node
        val errors: MutableList<Error> = mutableListOf()
        val warnings: MutableList<Warning> = mutableListOf()

        val preElements = mutableListOf<TreeNode.ElementNode>()

        val elements = mutableListOf<TreeNode.ElementNode>()

        // RESOLVE PRE ELEMENTS
        remainingTokens.removeComments(preElements)

        // RESOLVE ELEMENTS
        var lastInstrName: EInstrName? = null
        while (true) {
            if (remainingTokens.isEmpty()) break

            // Skip Whitespaces
            if (remainingTokens.first() is Compiler.Token.Space || remainingTokens.first() is Compiler.Token.NewLine) {
                remainingTokens.removeFirst()
                continue
            }


            // Resolve EInstr
            getEInstr(remainingTokens, errors, warnings)?.let {
                elements.add(it)
            }


            // Add first Token to errors
            errors.add(Error("Faulty Syntax!", remainingTokens.first()))
            remainingTokens.removeFirst()
        }

        console.log("Resolved elements: ${elements.joinToString(",") { it.name }}")

        if (remainingTokens.isNotEmpty()) {
            errors.add(Error("Faulty Syntax!", *remainingTokens.toTypedArray()))
        }

        val sections = TreeNode.ContainerNode(NAMES.C_SECTIONS, *elements.toTypedArray())

        return SyntaxTree(
            TreeNode.RootNode(
                errors,
                warnings,
                TreeNode.ContainerNode(NAMES.C_PRES, *preElements.toTypedArray()),
                sections
            )
        )
    }

    private fun MutableList<Compiler.Token>.removeComments(preElements: MutableList<TreeNode.ElementNode>): MutableList<Compiler.Token> {
        while (true) {
            val commentStart = this.firstOrNull { it.content == ";" } ?: break
            val startIndex = this.indexOf(commentStart)
            val commentEnd =
                this.firstOrNull { it is Compiler.Token.NewLine && this.indexOf(commentStart) < this.indexOf(it) }
            val endIndex = commentEnd?.let { this.indexOf(it) } ?: this.size

            val commentTokens = this.subList(startIndex, endIndex).toList()
            this.subList(startIndex, endIndex).clear()
            preElements.add(PREComment(*commentTokens.toTypedArray()))
        }
        return this
    }

    data object Patterns {
        val INSTRNAME_SEQUENCE = TokenSeq(InSpecific.Word)
    }

    data object NAMES {
        const val PRE_COMMENT = "comment"

        const val E_INSTR = "e_instr"
        const val E_INSTRNAME = "e_instrname"
        const val E_EXTENSION = "e_extension"

        const val S_TEXT = "text"

        const val C_PRES = "pre"
        const val C_SECTIONS = "sections"

    }

    enum class AModes(val extByteCount: Int, val tokenSequence: TokenSeq, val immSize: Variable.Size? = null) {
        ACCUMULATOR(0, TokenSeq(InSpecific.Word, InSpecific.Space, Specific("A"))), // Accumulator: A
        IMMEDIATE(1, TokenSeq(InSpecific.Word, InSpecific.Space, Specific("#"), InSpecific.Constant), immSize = BYTE_SIZE), // Immediate: #
        RELATIVE(1, TokenSeq(InSpecific.Word, InSpecific.Space, InSpecific.Constant), immSize = BYTE_SIZE), // Relative: r
        ZEROPAGE(1, TokenSeq(InSpecific.Word, InSpecific.Space, InSpecific.Constant), immSize = BYTE_SIZE), // Zero Page: zp
        ZEROPAGE_X(
            1,
            TokenSeq(InSpecific.Word, InSpecific.Space, InSpecific.Constant, Specific(","), Specific("X"), ignoreSpaces = false),
            immSize = BYTE_SIZE
        ), // Zero Page Indexed with X: zp,x
        ZEROPAGE_Y(
            1,
            TokenSeq(InSpecific.Word, InSpecific.Space, InSpecific.Constant, Specific(","), Specific("Y"), ignoreSpaces = false),
            immSize = BYTE_SIZE
        ), // Zero Page Indexed with Y: zp,y
        INDIRECT(
            2,
            TokenSeq(InSpecific.Word, InSpecific.Space, Specific("("), InSpecific.Constant, Specific(")"), ignoreSpaces = false),
            immSize = T6502.WORD_SIZE
        ), // Absolute Indirect: (a)
        ABSOLUTE(2, TokenSeq(InSpecific.Constant), immSize = T6502.WORD_SIZE), // Absolute: a
        ABSOLUTE_X(
            2,
            TokenSeq(InSpecific.Word, InSpecific.Space, InSpecific.Constant, Specific(","), Specific("X"), ignoreSpaces = false),
            immSize = T6502.WORD_SIZE
        ), // Absolute Indexed with X: a,x
        ABSOLUTE_Y(
            2,
            TokenSeq(InSpecific.Word, InSpecific.Space, InSpecific.Constant, Specific(","), Specific("Y"), ignoreSpaces = false),
            immSize = T6502.WORD_SIZE
        ), // Absolute Indexed with Y: a,y
        ZEROPAGE_X_INDIRECT(
            2,
            TokenSeq(
                InSpecific.Word, InSpecific.Space,
                Specific("("),
                InSpecific.Constant,
                Specific(","),
                Specific("X"),
                Specific(")"),
                ignoreSpaces = false
            ),
            immSize = BYTE_SIZE
        ), // Zero Page Indexed Indirect: (zp,x)
        ZPINDIRECT_Y(
            2,
            TokenSeq(
                InSpecific.Word, InSpecific.Space,
                Specific("("),
                InSpecific.Constant,
                Specific(")"),
                Specific(","),
                Specific("Y"),
                ignoreSpaces = false
            ),
            immSize = BYTE_SIZE
        ), // Zero Page Indirect Indexed with Y: (zp),y
        IMPLIED(0, TokenSeq(InSpecific.Word)), // Implied: i
    }

    enum class InstrType(val opCode: Map<AModes, Hex>) {
        // load, store, interregister transfer
        LDA(
            mapOf(
                ABSOLUTE to Hex("AD", BYTE_SIZE),
                ABSOLUTE_X to Hex("BD", BYTE_SIZE),
                ABSOLUTE_Y to Hex("B9", BYTE_SIZE),
                IMMEDIATE to Hex("A9", BYTE_SIZE),
                ZEROPAGE to Hex("A5", BYTE_SIZE),
                ZEROPAGE_X_INDIRECT to Hex("A1", BYTE_SIZE),
                ZEROPAGE_X to Hex("B5", BYTE_SIZE),
                ZPINDIRECT_Y to Hex("B1", BYTE_SIZE)
            )
        ),
        LDX(
            mapOf(
                ABSOLUTE to Hex("AE", BYTE_SIZE),
                ABSOLUTE_Y to Hex("BE", BYTE_SIZE),
                IMMEDIATE to Hex("A2", BYTE_SIZE),
                ZEROPAGE to Hex("A6", BYTE_SIZE),
                ZEROPAGE_Y to Hex("B6", BYTE_SIZE)
            )
        ),
        LDY(
            mapOf(
                ABSOLUTE to Hex("AC", BYTE_SIZE),
                ABSOLUTE_X to Hex("BC", BYTE_SIZE),
                IMMEDIATE to Hex("A0", BYTE_SIZE),
                ZEROPAGE to Hex("A4", BYTE_SIZE),
                ZEROPAGE_X to Hex("B4", BYTE_SIZE)
            )
        ),
        STA(
            mapOf(
                ABSOLUTE to Hex("8D", BYTE_SIZE),
                ABSOLUTE_X to Hex("9D", BYTE_SIZE),
                ABSOLUTE_Y to Hex("99", BYTE_SIZE),
                ZEROPAGE to Hex("85", BYTE_SIZE),
                ZEROPAGE_X_INDIRECT to Hex("81", BYTE_SIZE),
                ZEROPAGE_X to Hex("95", BYTE_SIZE),
                ZPINDIRECT_Y to Hex("91", BYTE_SIZE)
            )
        ),
        STX(
            mapOf(
                ABSOLUTE to Hex("8E", BYTE_SIZE),
                ZEROPAGE to Hex("86", BYTE_SIZE),
                ZEROPAGE_Y to Hex("96", BYTE_SIZE)
            )
        ),
        STY(
            mapOf(
                ABSOLUTE to Hex("8C", BYTE_SIZE),
                ZEROPAGE to Hex("84", BYTE_SIZE),
                ZEROPAGE_X to Hex("94", BYTE_SIZE)
            )
        ),
        TAX(mapOf(IMPLIED to Hex("AA", BYTE_SIZE))),
        TAY(mapOf(IMPLIED to Hex("A8", BYTE_SIZE))),
        TSX(mapOf(IMPLIED to Hex("BA", BYTE_SIZE))),
        TXA(mapOf(IMPLIED to Hex("8A", BYTE_SIZE))),
        TXS(mapOf(IMPLIED to Hex("9A", BYTE_SIZE))),
        TYA(mapOf(IMPLIED to Hex("98", BYTE_SIZE))),

        // stack
        PHA(mapOf(IMPLIED to Hex("48", BYTE_SIZE))),
        PHP(mapOf(IMPLIED to Hex("08", BYTE_SIZE))),
        PLA(mapOf(IMPLIED to Hex("68", BYTE_SIZE))),
        PLP(mapOf(IMPLIED to Hex("28", BYTE_SIZE))),

        // decrements, increments
        DEC(
            mapOf(
                ABSOLUTE to Hex("CE", BYTE_SIZE),
                ABSOLUTE_X to Hex("DE", BYTE_SIZE),
                ZEROPAGE to Hex("C6", BYTE_SIZE),
                ZEROPAGE_X to Hex("D6", BYTE_SIZE)
            )
        ),
        DEX(mapOf(IMPLIED to Hex("CA", BYTE_SIZE))),
        DEY(mapOf(IMPLIED to Hex("88", BYTE_SIZE))),
        INC(
            mapOf(
                ABSOLUTE to Hex("EE", BYTE_SIZE),
                ABSOLUTE_X to Hex("FE", BYTE_SIZE),
                ZEROPAGE to Hex("E6", BYTE_SIZE),
                ZEROPAGE_X to Hex("F6", BYTE_SIZE)
            )
        ),
        INX(mapOf(IMPLIED to Hex("E8", BYTE_SIZE))),
        INY(mapOf(IMPLIED to Hex("C8", BYTE_SIZE))),

        // arithmetic operations
        ADC(
            mapOf(
                ABSOLUTE to Hex("6D", BYTE_SIZE),
                ABSOLUTE_X to Hex("7D", BYTE_SIZE),
                ABSOLUTE_Y to Hex("79", BYTE_SIZE),
                IMMEDIATE to Hex("69", BYTE_SIZE),
                ZEROPAGE to Hex("65", BYTE_SIZE),
                ZEROPAGE_X_INDIRECT to Hex("61", BYTE_SIZE),
                ZEROPAGE_X to Hex("75", BYTE_SIZE),
                ZPINDIRECT_Y to Hex("71", BYTE_SIZE)
            )
        ),
        SBC(
            mapOf(
                ABSOLUTE to Hex("ED", BYTE_SIZE),
                ABSOLUTE_X to Hex("FD", BYTE_SIZE),
                ABSOLUTE_Y to Hex("F9", BYTE_SIZE),
                IMMEDIATE to Hex("E9", BYTE_SIZE),
                ZEROPAGE to Hex("E5", BYTE_SIZE),
                ZEROPAGE_X_INDIRECT to Hex("E1", BYTE_SIZE),
                ZEROPAGE_X to Hex("F5", BYTE_SIZE),
                ZPINDIRECT_Y to Hex("F1", BYTE_SIZE)
            )
        ),

        // logical operations
        AND(
            mapOf(
                ABSOLUTE to Hex("2D", BYTE_SIZE),
                ABSOLUTE_X to Hex("3D", BYTE_SIZE),
                ABSOLUTE_Y to Hex("39", BYTE_SIZE),
                IMMEDIATE to Hex("29", BYTE_SIZE),
                ZEROPAGE to Hex("25", BYTE_SIZE),
                ZEROPAGE_X_INDIRECT to Hex("21", BYTE_SIZE),
                ZEROPAGE_X to Hex("35", BYTE_SIZE),
                ZPINDIRECT_Y to Hex("31", BYTE_SIZE)
            )
        ),
        EOR(
            mapOf(
                ABSOLUTE to Hex("0D", BYTE_SIZE),
                ABSOLUTE_X to Hex("1D", BYTE_SIZE),
                ABSOLUTE_Y to Hex("19", BYTE_SIZE),
                IMMEDIATE to Hex("09", BYTE_SIZE),
                ZEROPAGE to Hex("05", BYTE_SIZE),
                ZEROPAGE_X_INDIRECT to Hex("01", BYTE_SIZE),
                ZEROPAGE_X to Hex("15", BYTE_SIZE),
                ZPINDIRECT_Y to Hex("11", BYTE_SIZE)
            )
        ),
        ORA(
            mapOf(
                ABSOLUTE to Hex("4D", BYTE_SIZE),
                ABSOLUTE_X to Hex("5D", BYTE_SIZE),
                ABSOLUTE_Y to Hex("59", BYTE_SIZE),
                IMMEDIATE to Hex("49", BYTE_SIZE),
                ZEROPAGE to Hex("45", BYTE_SIZE),
                ZEROPAGE_X_INDIRECT to Hex("41", BYTE_SIZE),
                ZEROPAGE_X to Hex("55", BYTE_SIZE),
                ZPINDIRECT_Y to Hex("51", BYTE_SIZE)
            )
        ),

        // shift & rotate
        ASL(
            mapOf(
                ABSOLUTE to Hex("0E", BYTE_SIZE),
                ABSOLUTE_X to Hex("1E", BYTE_SIZE),
                ACCUMULATOR to Hex("0A", BYTE_SIZE),
                ZEROPAGE to Hex("06", BYTE_SIZE),
                ZEROPAGE_X to Hex("16", BYTE_SIZE)
            )
        ),
        LSR(
            mapOf(
                ABSOLUTE to Hex("4E", BYTE_SIZE),
                ABSOLUTE_X to Hex("5E", BYTE_SIZE),
                ACCUMULATOR to Hex("4A", BYTE_SIZE),
                ZEROPAGE to Hex("46", BYTE_SIZE),
                ZEROPAGE_X to Hex("56", BYTE_SIZE)
            )
        ),
        ROL(
            mapOf(
                ABSOLUTE to Hex("2E", BYTE_SIZE),
                ABSOLUTE_X to Hex("3E", BYTE_SIZE),
                ACCUMULATOR to Hex("2A", BYTE_SIZE),
                ZEROPAGE to Hex("26", BYTE_SIZE),
                ZEROPAGE_X to Hex("36", BYTE_SIZE)
            )
        ),
        ROR(
            mapOf(
                ABSOLUTE to Hex("6E", BYTE_SIZE),
                ABSOLUTE_X to Hex("7E", BYTE_SIZE),
                ACCUMULATOR to Hex("6A", BYTE_SIZE),
                ZEROPAGE to Hex("66", BYTE_SIZE),
                ZEROPAGE_X to Hex("76", BYTE_SIZE)
            )
        ),

        // flag
        CLC(mapOf(IMPLIED to Hex("18", BYTE_SIZE))),
        CLD(mapOf(IMPLIED to Hex("D8", BYTE_SIZE))),
        CLI(mapOf(IMPLIED to Hex("58", BYTE_SIZE))),
        CLV(mapOf(IMPLIED to Hex("B8", BYTE_SIZE))),
        SEC(mapOf(IMPLIED to Hex("38", BYTE_SIZE))),
        SED(mapOf(IMPLIED to Hex("F8", BYTE_SIZE))),
        SEI(mapOf(IMPLIED to Hex("78", BYTE_SIZE))),

        // comparison
        CMP(
            mapOf(
                ABSOLUTE to Hex("CD", BYTE_SIZE),
                ABSOLUTE_X to Hex("DD", BYTE_SIZE),
                ABSOLUTE_Y to Hex("D9", BYTE_SIZE),
                IMMEDIATE to Hex("C9", BYTE_SIZE),
                ZEROPAGE to Hex("C5", BYTE_SIZE),
                ZEROPAGE_X_INDIRECT to Hex("C1", BYTE_SIZE),
                ZEROPAGE_X to Hex("D5", BYTE_SIZE),
                ZPINDIRECT_Y to Hex("D1", BYTE_SIZE)
            )
        ),
        CPX(
            mapOf(
                ABSOLUTE to Hex("EC", BYTE_SIZE),
                IMMEDIATE to Hex("E0", BYTE_SIZE),
                ZEROPAGE to Hex("E4", BYTE_SIZE)
            )
        ),
        CPY(
            mapOf(
                ABSOLUTE to Hex("CC", BYTE_SIZE),
                IMMEDIATE to Hex("C0", BYTE_SIZE),
                ZEROPAGE to Hex("C4", BYTE_SIZE)
            )
        ),

        // conditional branches
        BCC(mapOf(RELATIVE to Hex("90", BYTE_SIZE))),
        BCS(mapOf(RELATIVE to Hex("B0", BYTE_SIZE))),
        BEQ(mapOf(RELATIVE to Hex("F0", BYTE_SIZE))),
        BMI(mapOf(RELATIVE to Hex("30", BYTE_SIZE))),
        BNE(mapOf(RELATIVE to Hex("D0", BYTE_SIZE))),
        BPL(mapOf(RELATIVE to Hex("10", BYTE_SIZE))),
        BVC(mapOf(RELATIVE to Hex("50", BYTE_SIZE))),
        BVS(mapOf(RELATIVE to Hex("70", BYTE_SIZE))),

        // jumps, subroutines
        JMP(mapOf(ABSOLUTE to Hex("4C", BYTE_SIZE), INDIRECT to Hex("6C", BYTE_SIZE))),
        JSR(mapOf(ABSOLUTE to Hex("20", BYTE_SIZE))),
        RTS(mapOf(IMPLIED to Hex("60", BYTE_SIZE))),

        // interrupts
        BRK(mapOf(IMPLIED to Hex("00", BYTE_SIZE))),
        RTI(mapOf(IMPLIED to Hex("40", BYTE_SIZE))),

        // other
        BIT(
            mapOf(
                ABSOLUTE to Hex("2C", BYTE_SIZE),
                IMMEDIATE to Hex("89", BYTE_SIZE),
                ZEROPAGE to Hex("24", BYTE_SIZE)
            )
        ),
        NOP(mapOf(IMPLIED to Hex("EA", BYTE_SIZE)));

        open fun execute(arch: Architecture) {
            arch.getConsole().log("executing ${this.name}...")
        }
    }

    class PREComment(vararg tokens: Compiler.Token) :
        TreeNode.ElementNode(ConnectedHL(T6502Flags.comment), NAMES.PRE_COMMENT, *tokens)


    fun getEInstr(remainingTokens: MutableList<Compiler.Token>, errors: MutableList<Error>, warnings: MutableList<Warning>): EInstr? {
        for (amode in AModes.entries) {
            val amodeResult = amode.tokenSequence.matchStart(*remainingTokens.toTypedArray())

            if (amodeResult.matches) {
                console.log("match found!: ${amodeResult.sequenceMap}")
                val type = InstrType.entries.firstOrNull { it.name.uppercase() == amodeResult.sequenceMap[0].token.content.uppercase() } ?: return null
                console.log("type found: ${type.name}")
                if (!type.opCode.keys.contains(amode)) continue
                console.log("instrtype contains addressing mode!")
                val imm = amode.immSize?.let { amodeResult.sequenceMap.map { it.token }.filterIsInstance<Compiler.Token.Constant>().firstOrNull() }

                console.log("checking ${type.name} and ${amode.name}")

                if (imm != null) {
                    if (imm.getValue().size.bitWidth != amode.immSize.bitWidth) {
                        warnings.add(Warning("Instruction immediate size mismatch! (${type.name} ${amode.name})", *amodeResult.sequenceMap.map { it.token }.toTypedArray()))
                        continue
                    }
                }

                console.log("immediate matches size!")

                val eInstr = when (amode) {
                    ACCUMULATOR -> {
                        EInstr(type, amode, null, listOf(), listOf(amodeResult.sequenceMap[0].token), listOf(), listOf(amodeResult.sequenceMap[2].token))
                    }

                    IMMEDIATE -> {
                        if (imm == null) continue
                        EInstr(type, amode, imm.getValue(amode.immSize), listOf(imm), listOf(amodeResult.sequenceMap[0].token), listOf(amodeResult.sequenceMap[2].token), listOf())
                    }

                    RELATIVE -> {
                        if (imm == null) continue
                        EInstr(type, amode, imm.getValue(amode.immSize), listOf(imm), listOf(amodeResult.sequenceMap[0].token), listOf(), listOf())
                    }

                    ZEROPAGE -> {
                        if (imm == null) continue
                        EInstr(type, amode, imm.getValue(amode.immSize), listOf(imm), listOf(amodeResult.sequenceMap[0].token), listOf(), listOf())
                    }

                    ZEROPAGE_X -> {
                        if (imm == null) continue
                        EInstr(type, amode, imm.getValue(amode.immSize), listOf(imm), listOf(amodeResult.sequenceMap[0].token), listOf(amodeResult.sequenceMap[3].token), listOf(amodeResult.sequenceMap[4].token))
                    }

                    ZEROPAGE_Y -> {
                        if (imm == null) continue
                        if (amodeResult.sequenceMap.size != 3) continue
                        EInstr(type, amode, imm.getValue(amode.immSize), listOf(imm), listOf(amodeResult.sequenceMap[0].token), listOf(amodeResult.sequenceMap[3].token), listOf(amodeResult.sequenceMap[4].token))
                    }

                    INDIRECT -> {
                        if (imm == null) continue
                        EInstr(type, amode, imm.getValue(amode.immSize), listOf(imm), listOf(amodeResult.sequenceMap[0].token), listOf(amodeResult.sequenceMap[2].token, amodeResult.sequenceMap[4].token), listOf())
                    }

                    ABSOLUTE -> {
                        if (imm == null) continue
                        EInstr(type, amode, imm.getValue(amode.immSize), listOf(imm), listOf(amodeResult.sequenceMap[0].token), listOf(), listOf())
                    }

                    ABSOLUTE_X -> {
                        if (imm == null) continue
                        EInstr(type, amode, imm.getValue(amode.immSize), listOf(imm), listOf(amodeResult.sequenceMap[0].token), listOf(amodeResult.sequenceMap[3].token), listOf(amodeResult.sequenceMap[4].token))
                    }

                    ABSOLUTE_Y -> {
                        if (imm == null) continue
                        EInstr(type, amode, imm.getValue(amode.immSize), listOf(imm), listOf(amodeResult.sequenceMap[0].token), listOf(amodeResult.sequenceMap[3].token), listOf(amodeResult.sequenceMap[4].token))
                    }

                    ZEROPAGE_X_INDIRECT -> {
                        if (imm == null) continue
                        EInstr(
                            type,
                            amode,
                            imm.getValue(amode.immSize),
                            listOf(imm),
                            listOf(amodeResult.sequenceMap[0].token),
                            listOf(amodeResult.sequenceMap[2].token, amodeResult.sequenceMap[4].token, amodeResult.sequenceMap[6].token),
                            listOf(amodeResult.sequenceMap[5].token)
                        )
                    }

                    ZPINDIRECT_Y -> {
                        if (imm == null) continue
                        EInstr(
                            type,
                            amode,
                            imm.getValue(amode.immSize),
                            listOf(imm),
                            listOf(amodeResult.sequenceMap[0].token),
                            listOf(amodeResult.sequenceMap[2].token, amodeResult.sequenceMap[4].token, amodeResult.sequenceMap[5].token),
                            listOf(amodeResult.sequenceMap[6].token)
                        )
                    }

                    IMPLIED -> {
                        EInstr(type, amode, null, listOf(), listOf(amodeResult.sequenceMap[0].token), listOf(), listOf())
                    }
                }

                remainingTokens.removeAll(eInstr.tokens.toSet())
                warnings.add(Warning("Found instruction ${type.name} ${amode.name}", eInstr))
                return eInstr
            }
        }
        return null
    }

    class EInstrName(val type: InstrType, vararg val nameTokens: Compiler.Token.Word) {

    }

    class EInstr(
        val instrType: InstrType,
        val addressingMode: AModes,
        val imm: Variable.Value? = null,
        val constantToken: List<Compiler.Token.Constant>,
        nameTokens: List<Compiler.Token>,
        symbolTokens: List<Compiler.Token>,
        regTokens: List<Compiler.Token>
    ) :
        TreeNode.ElementNode(
            highlighting = T6502Flags.getInstrHL(nameTokens, symbolTokens, constantToken, regTokens),
            name = NAMES.E_INSTR,
            *constantToken.toTypedArray(),
            *nameTokens.toTypedArray(),
            *symbolTokens.toTypedArray(),
            *regTokens.toTypedArray()
        ) {
        init {
            console.log("found instr: ${instrType.name} ${addressingMode.name}")
        }


    }

}