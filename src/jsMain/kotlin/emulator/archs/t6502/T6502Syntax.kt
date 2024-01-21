package emulator.archs.t6502

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
        while (remainingTokens.isNotEmpty()) {
            val tempFirstToken = remainingTokens.first()

            // Skip Whitespaces
            if (remainingTokens.first() is Compiler.Token.Space || remainingTokens.first() is Compiler.Token.NewLine) {
                remainingTokens.removeFirst()
                continue
            }

            // Resolve ESetPC
            val eSetPC = getESetPC(remainingTokens, errors, warnings)
            if (eSetPC != null) {
                elements.add(eSetPC)
                continue
            }

            // Resolve EInstr
            val eInstr = getEInstr(remainingTokens, errors, warnings)
            if (eInstr != null) {
                elements.add(eInstr)
                continue
            }

            // Resolve ELabel
            val eLabel = getELabel(remainingTokens, errors, warnings)
            if (eLabel != null) {
                elements.add(eLabel)
                continue
            }

            // Add first Token to errors if not already resolved
            if (remainingTokens.first() == tempFirstToken) {
                errors.add(Error("Faulty Syntax!", remainingTokens.first()))
                remainingTokens.removeFirst()
            }
        }


        // Link Labels to Instructions
        linkLabels(elements, errors, warnings)

        remainingTokens.removeAll { it is Compiler.Token.Space || it is Compiler.Token.NewLine }
        if (remainingTokens.isNotEmpty()) {
            errors.add(Error("Faulty Syntax!", *remainingTokens.toTypedArray()))
        }

        return SyntaxTree(
            TreeNode.RootNode(
                errors,
                warnings,
                TreeNode.ContainerNode(NAMES.C_PRES, *preElements.toTypedArray()),
                *getSections(elements, errors, warnings).toTypedArray()
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

    data object Seqs {
        val labelSeq = TokenSeq(InSpecific.Word, Specific(":"))
        val setaddrSeq = TokenSeq(Specific("*"), Specific("="), InSpecific.Constant, ignoreSpaces = true)
    }

    data object NAMES {
        const val PRE_COMMENT = "comment"

        const val E_INSTR = "e_instr"
        const val E_LABEL = "e_label"
        const val E_SETADDR = "e_setaddr"

        const val S_TEXT = "text"

        const val C_PRES = "pre"
    }

    /**
     * Addressing Modes
     * IMPORTANT: The order of the enums determines the order in which the modes will be checked!
     */
    enum class AModes(val tokenSequence: TokenSeq, val immSize: Variable.Size? = null, val hasLabelVariant: AModes? = null) {

        ZP_X(
            TokenSeq(InSpecific.Word, InSpecific.Space, InSpecific.Constant, Specific(","), Specific("X"), ignoreSpaces = true),
            immSize = BYTE_SIZE
        ), // Zero Page Indexed with X: zp,x
        ZP_Y(
            TokenSeq(InSpecific.Word, InSpecific.Space, InSpecific.Constant, Specific(","), Specific("Y"), ignoreSpaces = true),
            immSize = BYTE_SIZE
        ), // Zero Page Indexed with Y: zp,y

        ABS_X_LBLD(TokenSeq(InSpecific.Word, InSpecific.Space, InSpecific.Word, Specific(","), Specific("X"), ignoreSpaces = true)),
        ABS_Y_LBLD(TokenSeq(InSpecific.Word, InSpecific.Space, InSpecific.Word, Specific(","), Specific("Y"), ignoreSpaces = true)),

        ABS_X(
            TokenSeq(InSpecific.Word, InSpecific.Space, InSpecific.Constant, Specific(","), Specific("X"), ignoreSpaces = true),
            immSize = T6502.WORD_SIZE,
            hasLabelVariant = ABS_X_LBLD
        ), // Absolute Indexed with X: a,x
        ABS_Y(
            TokenSeq(InSpecific.Word, InSpecific.Space, InSpecific.Constant, Specific(","), Specific("Y"), ignoreSpaces = true),
            immSize = T6502.WORD_SIZE,
            hasLabelVariant = ABS_Y_LBLD
        ), // Absolute Indexed with Y: a,y

        ZP_X_IND(
            TokenSeq(
                InSpecific.Word,
                InSpecific.Space,
                Specific("("),
                InSpecific.Constant,
                Specific(","),
                Specific("X"),
                Specific(")"),
                ignoreSpaces = true
            ),
            immSize = BYTE_SIZE
        ), // Zero Page Indexed Indirect: (zp,x)

        ZPIND_Y(
            TokenSeq(
                InSpecific.Word, InSpecific.Space,
                Specific("("),
                InSpecific.Constant,
                Specific(")"),
                Specific(","),
                Specific("Y"),
                ignoreSpaces = true
            ),
            immSize = BYTE_SIZE
        ), // Zero Page Indirect Indexed with Y: (zp),y

        IND_LBLD(TokenSeq(InSpecific.Word, InSpecific.Space, Specific("("), InSpecific.Word, Specific(")"), ignoreSpaces = true)),
        IND(
            TokenSeq(InSpecific.Word, InSpecific.Space, Specific("("), InSpecific.Constant, Specific(")"), ignoreSpaces = true),
            immSize = T6502.WORD_SIZE,
            hasLabelVariant = IND_LBLD
        ), // Absolute Indirect: (a)

        ACCUMULATOR(TokenSeq(InSpecific.Word, InSpecific.Space, Specific("A"))), // Accumulator: A
        IMM(TokenSeq(InSpecific.Word, InSpecific.Space, Specific("#"), InSpecific.Constant), immSize = BYTE_SIZE), // Immediate: #
        REL(TokenSeq(InSpecific.Word, InSpecific.Space, InSpecific.Constant), immSize = BYTE_SIZE), // Relative: r
        ZP(TokenSeq(InSpecific.Word, InSpecific.Space, InSpecific.Constant), immSize = BYTE_SIZE), // Zero Page: zp
        ABS_LBLD(TokenSeq(InSpecific.Word, InSpecific.Space, InSpecific.Word)),
        ABS(TokenSeq(InSpecific.Word, InSpecific.Space, InSpecific.Constant), immSize = T6502.WORD_SIZE, hasLabelVariant = ABS_LBLD), // Absolute: a

        IMPLIED(TokenSeq(InSpecific.Word)), // Implied: i
    }

    enum class InstrType(val opCode: Map<AModes, Hex>) {
        // load, store, interregister transfer
        LDA(
            mapOf(
                ABS to Hex("AD", BYTE_SIZE),
                ABS_X to Hex("BD", BYTE_SIZE),
                ABS_Y to Hex("B9", BYTE_SIZE),
                IMM to Hex("A9", BYTE_SIZE),
                ZP to Hex("A5", BYTE_SIZE),
                ZP_X_IND to Hex("A1", BYTE_SIZE),
                ZP_X to Hex("B5", BYTE_SIZE),
                ZPIND_Y to Hex("B1", BYTE_SIZE)
            )
        ),
        LDX(
            mapOf(
                ABS to Hex("AE", BYTE_SIZE),
                ABS_Y to Hex("BE", BYTE_SIZE),
                IMM to Hex("A2", BYTE_SIZE),
                ZP to Hex("A6", BYTE_SIZE),
                ZP_Y to Hex("B6", BYTE_SIZE)
            )
        ),
        LDY(
            mapOf(
                ABS to Hex("AC", BYTE_SIZE),
                ABS_X to Hex("BC", BYTE_SIZE),
                IMM to Hex("A0", BYTE_SIZE),
                ZP to Hex("A4", BYTE_SIZE),
                ZP_X to Hex("B4", BYTE_SIZE)
            )
        ),
        STA(
            mapOf(
                ABS to Hex("8D", BYTE_SIZE),
                ABS_X to Hex("9D", BYTE_SIZE),
                ABS_Y to Hex("99", BYTE_SIZE),
                ZP to Hex("85", BYTE_SIZE),
                ZP_X_IND to Hex("81", BYTE_SIZE),
                ZP_X to Hex("95", BYTE_SIZE),
                ZPIND_Y to Hex("91", BYTE_SIZE)
            )
        ),
        STX(
            mapOf(
                ABS to Hex("8E", BYTE_SIZE),
                ZP to Hex("86", BYTE_SIZE),
                ZP_Y to Hex("96", BYTE_SIZE)
            )
        ),
        STY(
            mapOf(
                ABS to Hex("8C", BYTE_SIZE),
                ZP to Hex("84", BYTE_SIZE),
                ZP_X to Hex("94", BYTE_SIZE)
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
                ABS to Hex("CE", BYTE_SIZE),
                ABS_X to Hex("DE", BYTE_SIZE),
                ZP to Hex("C6", BYTE_SIZE),
                ZP_X to Hex("D6", BYTE_SIZE)
            )
        ),
        DEX(mapOf(IMPLIED to Hex("CA", BYTE_SIZE))),
        DEY(mapOf(IMPLIED to Hex("88", BYTE_SIZE))),
        INC(
            mapOf(
                ABS to Hex("EE", BYTE_SIZE),
                ABS_X to Hex("FE", BYTE_SIZE),
                ZP to Hex("E6", BYTE_SIZE),
                ZP_X to Hex("F6", BYTE_SIZE)
            )
        ),
        INX(mapOf(IMPLIED to Hex("E8", BYTE_SIZE))),
        INY(mapOf(IMPLIED to Hex("C8", BYTE_SIZE))),

        // arithmetic operations
        ADC(
            mapOf(
                ABS to Hex("6D", BYTE_SIZE),
                ABS_X to Hex("7D", BYTE_SIZE),
                ABS_Y to Hex("79", BYTE_SIZE),
                IMM to Hex("69", BYTE_SIZE),
                ZP to Hex("65", BYTE_SIZE),
                ZP_X_IND to Hex("61", BYTE_SIZE),
                ZP_X to Hex("75", BYTE_SIZE),
                ZPIND_Y to Hex("71", BYTE_SIZE)
            )
        ),
        SBC(
            mapOf(
                ABS to Hex("ED", BYTE_SIZE),
                ABS_X to Hex("FD", BYTE_SIZE),
                ABS_Y to Hex("F9", BYTE_SIZE),
                IMM to Hex("E9", BYTE_SIZE),
                ZP to Hex("E5", BYTE_SIZE),
                ZP_X_IND to Hex("E1", BYTE_SIZE),
                ZP_X to Hex("F5", BYTE_SIZE),
                ZPIND_Y to Hex("F1", BYTE_SIZE)
            )
        ),

        // logical operations
        AND(
            mapOf(
                ABS to Hex("2D", BYTE_SIZE),
                ABS_X to Hex("3D", BYTE_SIZE),
                ABS_Y to Hex("39", BYTE_SIZE),
                IMM to Hex("29", BYTE_SIZE),
                ZP to Hex("25", BYTE_SIZE),
                ZP_X_IND to Hex("21", BYTE_SIZE),
                ZP_X to Hex("35", BYTE_SIZE),
                ZPIND_Y to Hex("31", BYTE_SIZE)
            )
        ),
        EOR(
            mapOf(
                ABS to Hex("0D", BYTE_SIZE),
                ABS_X to Hex("1D", BYTE_SIZE),
                ABS_Y to Hex("19", BYTE_SIZE),
                IMM to Hex("09", BYTE_SIZE),
                ZP to Hex("05", BYTE_SIZE),
                ZP_X_IND to Hex("01", BYTE_SIZE),
                ZP_X to Hex("15", BYTE_SIZE),
                ZPIND_Y to Hex("11", BYTE_SIZE)
            )
        ),
        ORA(
            mapOf(
                ABS to Hex("4D", BYTE_SIZE),
                ABS_X to Hex("5D", BYTE_SIZE),
                ABS_Y to Hex("59", BYTE_SIZE),
                IMM to Hex("49", BYTE_SIZE),
                ZP to Hex("45", BYTE_SIZE),
                ZP_X_IND to Hex("41", BYTE_SIZE),
                ZP_X to Hex("55", BYTE_SIZE),
                ZPIND_Y to Hex("51", BYTE_SIZE)
            )
        ),

        // shift & rotate
        ASL(
            mapOf(
                ABS to Hex("0E", BYTE_SIZE),
                ABS_X to Hex("1E", BYTE_SIZE),
                ACCUMULATOR to Hex("0A", BYTE_SIZE),
                ZP to Hex("06", BYTE_SIZE),
                ZP_X to Hex("16", BYTE_SIZE)
            )
        ),
        LSR(
            mapOf(
                ABS to Hex("4E", BYTE_SIZE),
                ABS_X to Hex("5E", BYTE_SIZE),
                ACCUMULATOR to Hex("4A", BYTE_SIZE),
                ZP to Hex("46", BYTE_SIZE),
                ZP_X to Hex("56", BYTE_SIZE)
            )
        ),
        ROL(
            mapOf(
                ABS to Hex("2E", BYTE_SIZE),
                ABS_X to Hex("3E", BYTE_SIZE),
                ACCUMULATOR to Hex("2A", BYTE_SIZE),
                ZP to Hex("26", BYTE_SIZE),
                ZP_X to Hex("36", BYTE_SIZE)
            )
        ),
        ROR(
            mapOf(
                ABS to Hex("6E", BYTE_SIZE),
                ABS_X to Hex("7E", BYTE_SIZE),
                ACCUMULATOR to Hex("6A", BYTE_SIZE),
                ZP to Hex("66", BYTE_SIZE),
                ZP_X to Hex("76", BYTE_SIZE)
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
                ABS to Hex("CD", BYTE_SIZE),
                ABS_X to Hex("DD", BYTE_SIZE),
                ABS_Y to Hex("D9", BYTE_SIZE),
                IMM to Hex("C9", BYTE_SIZE),
                ZP to Hex("C5", BYTE_SIZE),
                ZP_X_IND to Hex("C1", BYTE_SIZE),
                ZP_X to Hex("D5", BYTE_SIZE),
                ZPIND_Y to Hex("D1", BYTE_SIZE)
            )
        ),
        CPX(
            mapOf(
                ABS to Hex("EC", BYTE_SIZE),
                IMM to Hex("E0", BYTE_SIZE),
                ZP to Hex("E4", BYTE_SIZE)
            )
        ),
        CPY(
            mapOf(
                ABS to Hex("CC", BYTE_SIZE),
                IMM to Hex("C0", BYTE_SIZE),
                ZP to Hex("C4", BYTE_SIZE)
            )
        ),

        // conditional branches
        BCC(mapOf(REL to Hex("90", BYTE_SIZE))),
        BCS(mapOf(REL to Hex("B0", BYTE_SIZE))),
        BEQ(mapOf(REL to Hex("F0", BYTE_SIZE))),
        BMI(mapOf(REL to Hex("30", BYTE_SIZE))),
        BNE(mapOf(REL to Hex("D0", BYTE_SIZE))),
        BPL(mapOf(REL to Hex("10", BYTE_SIZE))),
        BVC(mapOf(REL to Hex("50", BYTE_SIZE))),
        BVS(mapOf(REL to Hex("70", BYTE_SIZE))),

        // jumps, subroutines
        JMP(mapOf(ABS to Hex("4C", BYTE_SIZE), IND to Hex("6C", BYTE_SIZE))),
        JSR(mapOf(ABS to Hex("20", BYTE_SIZE))),
        RTS(mapOf(IMPLIED to Hex("60", BYTE_SIZE))),

        // interrupts
        BRK(mapOf(IMPLIED to Hex("00", BYTE_SIZE))),
        RTI(mapOf(IMPLIED to Hex("40", BYTE_SIZE))),

        // other
        BIT(
            mapOf(
                ABS to Hex("2C", BYTE_SIZE),
                IMM to Hex("89", BYTE_SIZE),
                ZP to Hex("24", BYTE_SIZE)
            )
        ),
        NOP(mapOf(IMPLIED to Hex("EA", BYTE_SIZE)));

        open fun execute(arch: Architecture, nextByte: Hex, nextWord: Hex) {
            arch.getConsole().log("executing ${this.name}...")
        }
    }

    class PREComment(vararg tokens: Compiler.Token) :
        TreeNode.ElementNode(ConnectedHL(T6502Flags.comment), NAMES.PRE_COMMENT, *tokens)


    private fun getEInstr(remainingTokens: MutableList<Compiler.Token>, errors: MutableList<Error>, warnings: MutableList<Warning>): EInstr? {
        for (amode in AModes.entries) {
            val amodeResult = amode.tokenSequence.matchStart(*remainingTokens.toTypedArray())

            if (amodeResult.matches) {
                val type = InstrType.entries.firstOrNull { it.name.uppercase() == amodeResult.sequenceMap[0].token.content.uppercase() } ?: return null

                val relAmode = type.opCode.keys.firstOrNull { it.hasLabelVariant == amode }

                if (!type.opCode.keys.contains(amode) && !type.opCode.keys.mapNotNull { it.hasLabelVariant }.contains(amode)) continue
                val imm = amode.immSize?.let { amodeResult.sequenceMap.map { it.token }.filterIsInstance<Compiler.Token.Constant>().firstOrNull() }

                if (imm != null) {
                    if (imm.getValue().size.bitWidth != amode.immSize.bitWidth) {
                        continue
                    }
                }

                val eInstr = when (amode) {
                    ACCUMULATOR -> {
                        EInstr(type, amode, relAmode, nameTokens = listOf(amodeResult.sequenceMap[0].token), regTokens = listOf(amodeResult.sequenceMap[2].token))
                    }

                    IMM -> {
                        if (imm == null) continue
                        EInstr(type, amode, relAmode, imm.getValue(amode.immSize), listOf(imm), listOf(amodeResult.sequenceMap[0].token), listOf(amodeResult.sequenceMap[2].token))
                    }

                    REL, ZP -> {
                        if (imm == null) continue
                        EInstr(type, amode, relAmode, imm.getValue(amode.immSize), listOf(imm), listOf(amodeResult.sequenceMap[0].token))
                    }

                    ZP_X, ZP_Y -> {
                        if (imm == null) continue
                        EInstr(type, amode, relAmode, imm.getValue(amode.immSize), listOf(imm), listOf(amodeResult.sequenceMap[0].token), listOf(amodeResult.sequenceMap[3].token), listOf(amodeResult.sequenceMap[4].token))
                    }

                    IND -> {
                        if (imm == null) continue
                        EInstr(type, amode, relAmode, imm.getValue(amode.immSize), listOf(imm), listOf(amodeResult.sequenceMap[0].token), listOf(amodeResult.sequenceMap[2].token, amodeResult.sequenceMap[4].token))
                    }

                    IND_LBLD -> {
                        EInstr(type, amode, relAmode, nameTokens = listOf(amodeResult.sequenceMap[0].token), symbolTokens = listOf(amodeResult.sequenceMap[2].token, amodeResult.sequenceMap[4].token), labelLink = listOf(amodeResult.sequenceMap[3].token))
                    }

                    ABS -> {
                        if (imm == null) continue
                        EInstr(type, amode, relAmode, imm.getValue(amode.immSize), listOf(imm), listOf(amodeResult.sequenceMap[0].token))
                    }

                    ABS_LBLD -> {
                        EInstr(type, amode, relAmode, nameTokens = listOf(amodeResult.sequenceMap[0].token), labelLink = listOf(amodeResult.sequenceMap[2].token))
                    }

                    ABS_X, ABS_Y -> {
                        if (imm == null) continue
                        EInstr(type, amode, relAmode, imm.getValue(amode.immSize), listOf(imm), listOf(amodeResult.sequenceMap[0].token), listOf(amodeResult.sequenceMap[3].token), listOf(amodeResult.sequenceMap[4].token))
                    }

                    ABS_X_LBLD, ABS_Y_LBLD -> {
                        EInstr(type, amode, relAmode, nameTokens = listOf(amodeResult.sequenceMap[0].token), symbolTokens = listOf(amodeResult.sequenceMap[3].token), regTokens = listOf(amodeResult.sequenceMap[4].token), labelLink = listOf(amodeResult.sequenceMap[2].token))
                    }

                    ZP_X_IND -> {
                        if (imm == null) continue
                        EInstr(
                            type,
                            amode,
                            relAmode,
                            imm.getValue(amode.immSize),
                            constantToken = listOf(imm),
                            nameTokens = listOf(amodeResult.sequenceMap[0].token),
                            symbolTokens = listOf(amodeResult.sequenceMap[2].token, amodeResult.sequenceMap[4].token, amodeResult.sequenceMap[6].token),
                            regTokens = listOf(amodeResult.sequenceMap[5].token)
                        )
                    }

                    ZPIND_Y -> {
                        if (imm == null) continue
                        EInstr(
                            type,
                            amode,
                            relAmode,
                            imm.getValue(amode.immSize),
                            constantToken = listOf(imm),
                            nameTokens = listOf(amodeResult.sequenceMap[0].token),
                            symbolTokens = listOf(amodeResult.sequenceMap[2].token, amodeResult.sequenceMap[4].token, amodeResult.sequenceMap[5].token),
                            regTokens = listOf(amodeResult.sequenceMap[6].token)
                        )
                    }

                    IMPLIED -> {
                        EInstr(type, amode, relAmode, nameTokens = listOf(amodeResult.sequenceMap[0].token))
                    }
                }

                remainingTokens.removeAll(eInstr.tokens.toSet())
                return eInstr
            }
        }
        return null
    }

    private fun getELabel(remainingTokens: MutableList<Compiler.Token>, errors: MutableList<Error>, warnings: MutableList<Warning>): ELabel? {
        val labelResult = Seqs.labelSeq.matchStart(*remainingTokens.toTypedArray())
        if (labelResult.matches) {
            val tokens = labelResult.sequenceMap.map { it.token }
            val name = tokens.joinToString("") { it.content }.removeSuffix(":")
            if (InstrType.entries.firstOrNull { it.name.uppercase() == name.uppercase() } != null) {
                errors.add(Error("Illegal label name!", *tokens.toTypedArray()))
                remainingTokens.removeAll(tokens)
                return null
            }
            val eLabel = ELabel(name, *tokens.toTypedArray())
            remainingTokens.removeAll(tokens)
            return eLabel
        }
        return null
    }

    private fun getESetPC(remainingTokens: MutableList<Compiler.Token>, errors: MutableList<Error>, warnings: MutableList<Warning>): ESetAddr? {
        val setpcResult = Seqs.setaddrSeq.matchStart(*remainingTokens.toTypedArray())
        if (setpcResult.matches) {
            val tokens = setpcResult.sequenceMap.map { it.token }
            val constantToken = tokens.firstOrNull { it is Compiler.Token.Constant } as Compiler.Token.Constant?

            if (constantToken == null) {
                warnings.add(Warning("Set PC value missing: *=[8Bit or 16Bit]", *tokens.toTypedArray()))
                return null
            }

            val constant = constantToken.getValue(Variable.Size.Bit16())
            if (!constant.checkResult.valid) {
                errors.add(Error("Immediate value is not 8 Bit or 16 Bit!", *tokens.toTypedArray()))
                remainingTokens.removeAll(tokens)
                return null
            }

            val eSetAddr = ESetAddr(constant, *tokens.toTypedArray())
            remainingTokens.removeAll(tokens)
            return eSetAddr
        }
        return null
    }

    private fun linkLabels(elements: MutableList<TreeNode.ElementNode>, errors: MutableList<Error>, warnings: MutableList<Warning>) {
        val labels = elements.filterIsInstance<ELabel>()
        val instrToLink = elements.filterIsInstance<EInstr>().filter { it.labelName != null }

        for (instr in instrToLink) {
            val matchingLabel = labels.firstOrNull { it.labelName == instr.labelName }
            if (matchingLabel == null) {
                elements.remove(instr)
                errors.add(Error("${instr.labelName} couldn't get linked to any label!", instr))
                continue
            }

            instr.linkedLabel = matchingLabel
        }
    }

    private fun getSections(elements: MutableList<TreeNode.ElementNode>, errors: MutableList<Error>, warnings: MutableList<Warning>): List<TreeNode.ContainerNode> {
        return listOf(SText(*elements.toTypedArray()))
    }

    class EInstr(
        val instrType: InstrType,
        val addressingMode: AModes,
        amodeIsLabelVariantOf: AModes? = null,
        val imm: Variable.Value? = null,
        constantToken: List<Compiler.Token.Constant> = listOf(),
        nameTokens: List<Compiler.Token> = listOf(),
        symbolTokens: List<Compiler.Token> = listOf(),
        regTokens: List<Compiler.Token> = listOf(),
        labelLink: List<Compiler.Token> = listOf()
    ) : TreeNode.ElementNode(
        highlighting = T6502Flags.getInstrHL(nameTokens, symbolTokens, constantToken, regTokens, labelLink),
        name = NAMES.E_INSTR,
        *constantToken.toTypedArray(),
        *nameTokens.toTypedArray(),
        *labelLink.toTypedArray(),
        *symbolTokens.toTypedArray(),
        *regTokens.toTypedArray(),
    ) {
        val labelName = if (labelLink.isNotEmpty()) labelLink.joinToString("") { it.content } else null
        val opCodeRelevantAMode = amodeIsLabelVariantOf ?: addressingMode
        var linkedLabel: ELabel? = null
    }

    class ELabel(val labelName: String, vararg tokens: Compiler.Token) : TreeNode.ElementNode(highlighting = ConnectedHL(T6502Flags.label), NAMES.E_LABEL, *tokens)

    class ESetAddr(val value: Variable.Value, vararg tokens: Compiler.Token) : TreeNode.ElementNode(ConnectedHL(T6502Flags.setpc to tokens.filter { it !is Compiler.Token.Constant }, T6502Flags.constant to tokens.filterIsInstance<Compiler.Token.Constant>()), NAMES.E_SETADDR, *tokens)

    class SText(vararg val elements: TreeNode.ElementNode) : TreeNode.ContainerNode(NAMES.S_TEXT, *elements)

}