package emulator.archs.t6502

import emulator.kit.Architecture
import emulator.kit.assembly.Compiler
import emulator.kit.assembly.Syntax
import emulator.kit.common.FileHandler
import emulator.kit.common.Transcript
import emulator.kit.types.Variable.Value.*
import emulator.archs.t6502.T6502Syntax.AModes.*
import emulator.archs.t6502.T6502.BYTE_SIZE
import emulator.archs.t6502.T6502.WORD_SIZE
import emulator.kit.assembly.Syntax.TokenSeq.Component.InSpecific.*
import emulator.kit.assembly.Syntax.TokenSeq.Component.Specific
import emulator.kit.assembly.Syntax.TokenSeq.Component.SpecConst
import emulator.kit.assembly.Syntax.TokenSeq.Component.RegOrSpecConst
import emulator.kit.types.Variable
import emulator.kit.types.Variable.Size.*

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
        console.log(this.joinToString { it::class.simpleName.toString() })
        while (true) {
            val commentStart = this.firstOrNull { it.content == ";" } ?: break
            val startIndex = this.indexOf(commentStart)
            val commentEnd = this.firstOrNull { it is Compiler.Token.NewLine && this.indexOf(commentStart) < this.indexOf(it) }
            console.log("Comment End: ${commentEnd?.lineLoc}")
            val endIndex = commentEnd?.let { this.indexOf(it) } ?: this.size

            val commentTokens = this.subList(startIndex, endIndex).toList()
            this.subList(startIndex, endIndex).clear()
            preElements.add(PREComment(*commentTokens.toTypedArray()))
        }
        return this
    }

    data object Seqs {
        val labelSeq = TokenSeq(Word, Specific(":"))
        val setaddrSeq = TokenSeq(Specific("*"), Specific("="), Constant, ignoreSpaces = true)
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
    enum class AModes(val tokenSequence: TokenSeq, val immSize: Variable.Size? = null, val hasLabelVariant: AModes? = null, val exampleString: String, val description: String) {

        ZP_X(TokenSeq(Word, Space, Constant, Specific(","), Specific("X"), NewLine, ignoreSpaces = true), immSize = BYTE_SIZE, exampleString = "$00, X", description = "zeropage, X-indexed"), // Zero Page Indexed with X: zp,x
        ZP_Y(TokenSeq(Word, Space, Constant, Specific(","), Specific("Y"), NewLine, ignoreSpaces = true), immSize = BYTE_SIZE, exampleString = "$00, Y", description = "zeropage, Y-indexed"), // Zero Page Indexed with Y: zp,y

        ABS_X_LBLD(TokenSeq(Word, Space, Word, Specific(","), Specific("X"), NewLine, ignoreSpaces = true), exampleString = "[labelname], X", description = "absolute (from label), X-indexed"),
        ABS_Y_LBLD(TokenSeq(Word, Space, Word, Specific(","), Specific("Y"), NewLine, ignoreSpaces = true), exampleString = "[labelname], Y", description = "absolute (from label), Y-indexed"),

        ABS_X(TokenSeq(Word, Space, Constant, Specific(","), Specific("X"), NewLine, ignoreSpaces = true), immSize = WORD_SIZE, hasLabelVariant = ABS_X_LBLD, exampleString = "$0000, X", description = "absolute, X-indexed"), // Absolute Indexed with X: a,x
        ABS_Y(TokenSeq(Word, Space, Constant, Specific(","), Specific("Y"), NewLine, ignoreSpaces = true), immSize = WORD_SIZE, hasLabelVariant = ABS_Y_LBLD, exampleString = "$0000, Y", description = "absolute, Y-indexed"), // Absolute Indexed with Y: a,y
        ZP_X_IND(TokenSeq(Word, Space, Specific("("), Constant, Specific(","), Specific("X"), Specific(")"), NewLine, ignoreSpaces = true), immSize = BYTE_SIZE, exampleString = "($00, X)", description = "X-indexed, indirect"), // Zero Page Indexed Indirect: (zp,x)

        ZPIND_Y(TokenSeq(Word, Space, Specific("("), Constant, Specific(")"), Specific(","), Specific("Y"), NewLine, ignoreSpaces = true), immSize = BYTE_SIZE, exampleString = "($00), Y", description = "indirect, Y-indexed"), // Zero Page Indirect Indexed with Y: (zp),y

        IND_LBLD(TokenSeq(Word, Space, Specific("("), Word, Specific(")"), NewLine, ignoreSpaces = true), exampleString = "([labelname])", description = "indirect (from label)"),
        IND(TokenSeq(Word, Space, Specific("("), Constant, Specific(")"), NewLine, ignoreSpaces = true), immSize = WORD_SIZE, hasLabelVariant = IND_LBLD, exampleString = "($0000)", description = "indirect"), // Absolute Indirect: (a)

        ACCUMULATOR(TokenSeq(Word, Space, Specific("A"), NewLine, ignoreSpaces = true), exampleString = "A", description = "Accumulator"), // Accumulator: A
        IMM(TokenSeq(Word, Space, Specific("#"), Constant, NewLine, ignoreSpaces = true), immSize = BYTE_SIZE, exampleString = "#$00", description = "immediate"), // Immediate: #
        REL(TokenSeq(Word, Space, Constant, NewLine, ignoreSpaces = true), immSize = BYTE_SIZE, exampleString = "$00", description = "relative"), // Relative: r
        ZP(TokenSeq(Word, Space, Constant, NewLine, ignoreSpaces = true), immSize = BYTE_SIZE, exampleString = "$00", description = "zeropage"), // Zero Page: zp
        ABS_LBLD(TokenSeq(Word, Space, Word, NewLine, ignoreSpaces = true), exampleString = "[labelname]", description = "absolute (from label)"),
        ABS(TokenSeq(Word, Space, Constant, NewLine, ignoreSpaces = true), immSize = WORD_SIZE, hasLabelVariant = ABS_LBLD, exampleString = "$0000", description = "absolute"), // Absolute: a

        IMPLIED(TokenSeq(Word, NewLine, ignoreSpaces = true), exampleString = "", description = "implied"); // Implied: i

        fun getString(nextByte: Hex, nextWord: Hex, labelName: String? = null): String {
            return when (this) {
                ZP_X -> "$${nextByte.getRawHexStr()}, X"
                ZP_Y -> "$${nextByte.getRawHexStr()}, Y"
                ABS_X_LBLD -> "$labelName, X"
                ABS_Y_LBLD -> "$labelName, Y"
                ABS_X -> "$${nextWord.getRawHexStr()}, X"
                ABS_Y -> "$${nextWord.getRawHexStr()}, Y"
                ZP_X_IND -> "($${nextByte.getRawHexStr()}, X)"
                ZPIND_Y -> "($${nextByte.getRawHexStr()}), Y"
                IND_LBLD -> "($labelName)"
                IND -> "(${nextWord.getRawHexStr()})"
                ACCUMULATOR -> "A"
                IMM -> "#$${nextByte.getRawHexStr()}"
                REL -> "$${nextByte.getRawHexStr()}"
                ZP -> "$${nextByte.getRawHexStr()}"
                ABS_LBLD -> "$labelName"
                ABS -> "$${nextWord.getRawHexStr()}"
                IMPLIED -> ""
            }
        }
    }

    enum class InstrType(val opCode: Map<AModes, Hex>, val description: String) {
        // load, store, interregister transfer
        LDA(mapOf(ABS to Hex("AD", BYTE_SIZE), ABS_X to Hex("BD", BYTE_SIZE), ABS_Y to Hex("B9", BYTE_SIZE), IMM to Hex("A9", BYTE_SIZE), ZP to Hex("A5", BYTE_SIZE), ZP_X_IND to Hex("A1", BYTE_SIZE), ZP_X to Hex("B5", BYTE_SIZE), ZPIND_Y to Hex("B1", BYTE_SIZE)), "load accumulator"),
        LDX(mapOf(ABS to Hex("AE", BYTE_SIZE), ABS_Y to Hex("BE", BYTE_SIZE), IMM to Hex("A2", BYTE_SIZE), ZP to Hex("A6", BYTE_SIZE), ZP_Y to Hex("B6", BYTE_SIZE)), "load X"),
        LDY(mapOf(ABS to Hex("AC", BYTE_SIZE), ABS_X to Hex("BC", BYTE_SIZE), IMM to Hex("A0", BYTE_SIZE), ZP to Hex("A4", BYTE_SIZE), ZP_X to Hex("B4", BYTE_SIZE)), "load Y"),
        STA(mapOf(ABS to Hex("8D", BYTE_SIZE), ABS_X to Hex("9D", BYTE_SIZE), ABS_Y to Hex("99", BYTE_SIZE), ZP to Hex("85", BYTE_SIZE), ZP_X_IND to Hex("81", BYTE_SIZE), ZP_X to Hex("95", BYTE_SIZE), ZPIND_Y to Hex("91", BYTE_SIZE)), "store accumulator"),
        STX(mapOf(ABS to Hex("8E", BYTE_SIZE), ZP to Hex("86", BYTE_SIZE), ZP_Y to Hex("96", BYTE_SIZE)), "store X"),
        STY(mapOf(ABS to Hex("8C", BYTE_SIZE), ZP to Hex("84", BYTE_SIZE), ZP_X to Hex("94", BYTE_SIZE)), "store Y"),
        TAX(mapOf(IMPLIED to Hex("AA", BYTE_SIZE)), "transfer accumulator to X"),
        TAY(mapOf(IMPLIED to Hex("A8", BYTE_SIZE)), "transfer accumulator to Y"),
        TSX(mapOf(IMPLIED to Hex("BA", BYTE_SIZE)), "transfer stack pointer to X"),
        TXA(mapOf(IMPLIED to Hex("8A", BYTE_SIZE)), "transfer X to accumulator"),
        TXS(mapOf(IMPLIED to Hex("9A", BYTE_SIZE)), "transfer X to stack pointer"),
        TYA(mapOf(IMPLIED to Hex("98", BYTE_SIZE)), "transfer Y to accumulator"),

        // stack
        PHA(mapOf(IMPLIED to Hex("48", BYTE_SIZE)), "push accumulator"),
        PHP(mapOf(IMPLIED to Hex("08", BYTE_SIZE)), "push processor status (SR)"),
        PLA(mapOf(IMPLIED to Hex("68", BYTE_SIZE)), "pull accumulator"),
        PLP(mapOf(IMPLIED to Hex("28", BYTE_SIZE)), "pull processor status (SR)"),

        // decrements, increments
        DEC(mapOf(ABS to Hex("CE", BYTE_SIZE), ABS_X to Hex("DE", BYTE_SIZE), ZP to Hex("C6", BYTE_SIZE), ZP_X to Hex("D6", BYTE_SIZE)), "decrement"),
        DEX(mapOf(IMPLIED to Hex("CA", BYTE_SIZE)), "decrement X"),
        DEY(mapOf(IMPLIED to Hex("88", BYTE_SIZE)), "decrement Y"),
        INC(mapOf(ABS to Hex("EE", BYTE_SIZE), ABS_X to Hex("FE", BYTE_SIZE), ZP to Hex("E6", BYTE_SIZE), ZP_X to Hex("F6", BYTE_SIZE)), "increment"),
        INX(mapOf(IMPLIED to Hex("E8", BYTE_SIZE)), "increment X"),
        INY(mapOf(IMPLIED to Hex("C8", BYTE_SIZE)), "increment Y"),

        // arithmetic operations
        /**
         *
         */
        ADC(mapOf(ABS to Hex("6D", BYTE_SIZE), ABS_X to Hex("7D", BYTE_SIZE), ABS_Y to Hex("79", BYTE_SIZE), IMM to Hex("69", BYTE_SIZE), ZP to Hex("65", BYTE_SIZE), ZP_X_IND to Hex("61", BYTE_SIZE), ZP_X to Hex("75", BYTE_SIZE), ZPIND_Y to Hex("71", BYTE_SIZE)), "add with carry"),

        /**
         *
         */
        SBC(mapOf(ABS to Hex("ED", BYTE_SIZE), ABS_X to Hex("FD", BYTE_SIZE), ABS_Y to Hex("F9", BYTE_SIZE), IMM to Hex("E9", BYTE_SIZE), ZP to Hex("E5", BYTE_SIZE), ZP_X_IND to Hex("E1", BYTE_SIZE), ZP_X to Hex("F5", BYTE_SIZE), ZPIND_Y to Hex("F1", BYTE_SIZE)), "subtract with carry"),

        // logical operations
        AND(mapOf(ABS to Hex("2D", BYTE_SIZE), ABS_X to Hex("3D", BYTE_SIZE), ABS_Y to Hex("39", BYTE_SIZE), IMM to Hex("29", BYTE_SIZE), ZP to Hex("25", BYTE_SIZE), ZP_X_IND to Hex("21", BYTE_SIZE), ZP_X to Hex("35", BYTE_SIZE), ZPIND_Y to Hex("31", BYTE_SIZE)), "and (with accumulator)"),
        EOR(mapOf(ABS to Hex("0D", BYTE_SIZE), ABS_X to Hex("1D", BYTE_SIZE), ABS_Y to Hex("19", BYTE_SIZE), IMM to Hex("09", BYTE_SIZE), ZP to Hex("05", BYTE_SIZE), ZP_X_IND to Hex("01", BYTE_SIZE), ZP_X to Hex("15", BYTE_SIZE), ZPIND_Y to Hex("11", BYTE_SIZE)), "exclusive or (with accumulator)"),
        ORA(mapOf(ABS to Hex("4D", BYTE_SIZE), ABS_X to Hex("5D", BYTE_SIZE), ABS_Y to Hex("59", BYTE_SIZE), IMM to Hex("49", BYTE_SIZE), ZP to Hex("45", BYTE_SIZE), ZP_X_IND to Hex("41", BYTE_SIZE), ZP_X to Hex("55", BYTE_SIZE), ZPIND_Y to Hex("51", BYTE_SIZE)), "or (with accumulator)"),

        // shift & rotate
        ASL(mapOf(ABS to Hex("0E", BYTE_SIZE), ABS_X to Hex("1E", BYTE_SIZE), ACCUMULATOR to Hex("0A", BYTE_SIZE), ZP to Hex("06", BYTE_SIZE), ZP_X to Hex("16", BYTE_SIZE)), "arithmetic shift left"),
        LSR(mapOf(ABS to Hex("4E", BYTE_SIZE), ABS_X to Hex("5E", BYTE_SIZE), ACCUMULATOR to Hex("4A", BYTE_SIZE), ZP to Hex("46", BYTE_SIZE), ZP_X to Hex("56", BYTE_SIZE)), "logical shift right"),
        ROL(mapOf(ABS to Hex("2E", BYTE_SIZE), ABS_X to Hex("3E", BYTE_SIZE), ACCUMULATOR to Hex("2A", BYTE_SIZE), ZP to Hex("26", BYTE_SIZE), ZP_X to Hex("36", BYTE_SIZE)), "rotate left"),
        ROR(mapOf(ABS to Hex("6E", BYTE_SIZE), ABS_X to Hex("7E", BYTE_SIZE), ACCUMULATOR to Hex("6A", BYTE_SIZE), ZP to Hex("66", BYTE_SIZE), ZP_X to Hex("76", BYTE_SIZE)), "rotate right"),

        // flag
        CLC(mapOf(IMPLIED to Hex("18", BYTE_SIZE)), "clear carry"),
        CLD(mapOf(IMPLIED to Hex("D8", BYTE_SIZE)), "clear decimal"),
        CLI(mapOf(IMPLIED to Hex("58", BYTE_SIZE)), "clear interrupt disable"),
        CLV(mapOf(IMPLIED to Hex("B8", BYTE_SIZE)), "clear overflow"),
        SEC(mapOf(IMPLIED to Hex("38", BYTE_SIZE)), "set carry"),
        SED(mapOf(IMPLIED to Hex("F8", BYTE_SIZE)), "set decimal"),
        SEI(mapOf(IMPLIED to Hex("78", BYTE_SIZE)), "set interrupt disable"),

        // comparison
        CMP(mapOf(ABS to Hex("CD", BYTE_SIZE), ABS_X to Hex("DD", BYTE_SIZE), ABS_Y to Hex("D9", BYTE_SIZE), IMM to Hex("C9", BYTE_SIZE), ZP to Hex("C5", BYTE_SIZE), ZP_X_IND to Hex("C1", BYTE_SIZE), ZP_X to Hex("D5", BYTE_SIZE), ZPIND_Y to Hex("D1", BYTE_SIZE)), "compare (with accumulator)"),
        CPX(mapOf(ABS to Hex("EC", BYTE_SIZE), IMM to Hex("E0", BYTE_SIZE), ZP to Hex("E4", BYTE_SIZE)), "compare with X"),
        CPY(mapOf(ABS to Hex("CC", BYTE_SIZE), IMM to Hex("C0", BYTE_SIZE), ZP to Hex("C4", BYTE_SIZE)), "compare with Y"),

        // conditional branches
        BCC(mapOf(REL to Hex("90", BYTE_SIZE)), "branch on carry clear"),
        BCS(mapOf(REL to Hex("B0", BYTE_SIZE)), "branch on carry set"),
        BEQ(mapOf(REL to Hex("F0", BYTE_SIZE)), "branch on equal (zero set)"),
        BMI(mapOf(REL to Hex("30", BYTE_SIZE)), "branch on minus (negative set)"),
        BNE(mapOf(REL to Hex("D0", BYTE_SIZE)), "branch on not equal (zero clear)"),
        BPL(mapOf(REL to Hex("10", BYTE_SIZE)), "branch on plus (negative clear)"),
        BVC(mapOf(REL to Hex("50", BYTE_SIZE)), "branch on overflow clear"),
        BVS(mapOf(REL to Hex("70", BYTE_SIZE)), "branch on overflow set"),

        // jumps, subroutines
        JMP(mapOf(ABS to Hex("4C", BYTE_SIZE), IND to Hex("6C", BYTE_SIZE)), "jump"),
        JSR(mapOf(ABS to Hex("20", BYTE_SIZE)), "jump subroutine"),
        RTS(mapOf(IMPLIED to Hex("60", BYTE_SIZE)), "return from subroutine"),

        // interrupts
        BRK(mapOf(IMPLIED to Hex("00", BYTE_SIZE)), "break / interrupt"),
        RTI(mapOf(IMPLIED to Hex("40", BYTE_SIZE)), "return from interrupt"),

        // other
        BIT(mapOf(ABS to Hex("2C", BYTE_SIZE), IMM to Hex("89", BYTE_SIZE), ZP to Hex("24", BYTE_SIZE)), "bit test"),
        NOP(mapOf(IMPLIED to Hex("EA", BYTE_SIZE)), "no operation");

        fun execute(arch: Architecture, amode: AModes, nextByte: Hex, nextWord: Hex) {

            val flags = this.getFlags(arch)
            val pc = arch.getRegContainer().pc
            val ac = arch.getRegByName("AC")
            val x = arch.getRegByName("X")
            val y = arch.getRegByName("Y")
            val sr = arch.getRegByName("SR")
            val sp = arch.getRegByName("SP")

            if (ac == null || x == null || y == null || flags == null || sp == null || sr == null) {
                arch.getConsole().error("Register missing!")
                return
            }
            val operand = this.getOperand(arch, amode, nextByte, nextWord)
            val address = this.getAddress(arch, amode, nextByte, nextWord, x.get().toHex(), y.get().toHex())

            // EXECUTION TYPE SPECIFIC
            when (this) {
                LDA -> {
                    if (operand == null) {
                        arch.getConsole().error("Couldn't load operand for ${this.name} ${amode.name}!")
                        return
                    }
                    ac.set(operand)
                    setFlags(arch, n = ac.get().toBin().getBit(0), checkZero = ac.get().toBin())
                }

                LDX -> {
                    if (operand == null) {
                        arch.getConsole().error("Couldn't load operand for ${this.name} ${amode.name}!")
                        return
                    }
                    x.set(operand)
                    setFlags(arch, n = x.get().toBin().getBit(0), checkZero = x.get().toBin())
                }

                LDY -> {
                    if (operand == null) {
                        arch.getConsole().error("Couldn't load operand for ${this.name} ${amode.name}!")
                        return
                    }
                    y.set(operand)
                    setFlags(arch, n = y.get().toBin().getBit(0), checkZero = y.get().toBin())
                }

                STA -> address?.let { arch.getMemory().store(it, ac.get()) } ?: {
                    arch.getConsole().error("Couldn't load address for ${this.name} ${amode.name}!")
                }

                STX -> address?.let { arch.getMemory().store(it, x.get()) } ?: {
                    arch.getConsole().error("Couldn't load address for ${this.name} ${amode.name}!")
                }

                STY -> address?.let { arch.getMemory().store(it, y.get()) } ?: {
                    arch.getConsole().error("Couldn't load address for ${this.name} ${amode.name}!")
                }

                TAX -> {
                    x.set(ac.get())
                    setFlags(arch, n = ac.get().toBin().getBit(0), checkZero = ac.get().toBin())
                }

                TAY -> {
                    y.set(ac.get())
                    setFlags(arch, n = ac.get().toBin().getBit(0), checkZero = ac.get().toBin())
                }

                TSX -> {
                    x.set(sp.get())
                    setFlags(arch, n = sp.get().toBin().getBit(0), checkZero = sp.get().toBin())
                }

                TXA -> {
                    ac.set(x.get())
                    setFlags(arch, n = x.get().toBin().getBit(0), checkZero = x.get().toBin())
                }

                TXS -> sp.set(x.get())
                TYA -> {
                    ac.set(y.get())
                    setFlags(arch, n = y.get().toBin().getBit(0), checkZero = y.get().toBin())
                }

                PHA -> {
                    arch.getMemory().store(sp.get().toBin().getUResized(WORD_SIZE), ac.get())
                    sp.set(sp.get().toBin() - Bin("1", BYTE_SIZE))
                }

                PHP -> {
                    arch.getMemory().store(sp.get().toBin().getUResized(WORD_SIZE), sr.get())
                    sp.set(sp.get().toBin() - Bin("1", BYTE_SIZE))
                }

                PLA -> {
                    sp.set(sp.get().toBin() + Bin("1", BYTE_SIZE))
                    val loaded = arch.getMemory().load(sp.get().toHex().getUResized(WORD_SIZE))
                    ac.set(loaded)
                    setFlags(arch, n = loaded.getBit(0), checkZero = loaded)
                }

                PLP -> {
                    sp.set(sp.get().toBin() + Bin("1", BYTE_SIZE))
                    val loaded = arch.getMemory().load(sp.get().toHex().getUResized(WORD_SIZE))
                    setFlags(arch, n = loaded.getBit(0), v = loaded.getBit(1), d = loaded.getBit(4), i = loaded.getBit(5), z = loaded.getBit(6), c = loaded.getBit(7))
                }

                DEC -> {
                    address?.let {
                        val dec = arch.getMemory().load(it) - Bin("1", BYTE_SIZE)
                        arch.getMemory().store(it, dec)
                        setFlags(arch, n = dec.toBin().getBit(0), checkZero = dec.toBin())
                    } ?: {
                        arch.getConsole().error("Couldn't load address for ${this.name} ${amode.name}!")
                    }
                }

                DEX -> {
                    val dec = x.get() - Bin("1", BYTE_SIZE)
                    x.set(dec)
                    setFlags(arch, n = dec.toBin().getBit(0), checkZero = dec.toBin())
                }

                DEY -> {
                    val dec = y.get() - Bin("1", BYTE_SIZE)
                    y.set(dec)
                    setFlags(arch, n = dec.toBin().getBit(0), checkZero = dec.toBin())
                }

                INC -> {
                    address?.let {
                        val inc = arch.getMemory().load(it) + Bin("1", BYTE_SIZE)
                        arch.getMemory().store(it, inc)
                        setFlags(arch, n = inc.toBin().getBit(0), checkZero = inc.toBin())
                    } ?: {
                        arch.getConsole().error("Couldn't load address for ${this.name} ${amode.name}!")
                    }
                }

                INX -> {
                    val inc = x.get() + Bin("1", BYTE_SIZE)
                    x.set(inc)
                    setFlags(arch, n = inc.toBin().getBit(0), checkZero = inc.toBin())
                }

                INY -> {
                    val inc = y.get() + Bin("1", BYTE_SIZE)
                    y.set(inc)
                    setFlags(arch, n = inc.toBin().getBit(0), checkZero = inc.toBin())
                }

                ADC -> {
                    if (operand == null) {
                        arch.getConsole().error("Couldn't load operand for ${this.name} ${amode.name}!")
                        return
                    }

                    // Binary Add
                    val binSumBit9 = ac.get().toBin() + operand + flags.c
                    val sum = Bin(binSumBit9.toBin().getRawBinStr().substring(1), BYTE_SIZE)
                    val acBit0 = ac.get().toBin().getBit(0) ?: Bin("0", Bit1())
                    val operandBit0 = operand.toBin().getBit(0)
                    val sumBit0 = sum.getBit(0) ?: Bin("0", Bit1())
                    val v = if (acBit0 == operandBit0) sumBit0 xor acBit0 else Bin("0", Bit1())
                    val c = binSumBit9.toBin().getBit(0)
                    setFlags(arch, v = v, c = c)

                    ac.set(sum)
                    setFlags(arch, checkZero = sum, n = sum.getBit(0))
                }

                SBC -> {
                    if (operand == null) {
                        arch.getConsole().error("Couldn't load operand for ${this.name} ${amode.name}!")
                        return
                    }

                    // Binary SBC
                    val opInv = operand.toBin().inv()

                    val binSumBit9 = ac.get().toBin() + opInv + flags.c
                    val subBit8 = Bin(binSumBit9.toBin().getRawBinStr().substring(1), BYTE_SIZE)
                    val acBit0 = ac.get().toBin().getBit(0) ?: Bin("0", Bit1())
                    val opInvBit0 = opInv.toBin().getBit(0)
                    val subBit0 = subBit8.getBit(0) ?: Bin("0", Bit1())
                    val v = if (acBit0 == opInvBit0) subBit0 xor acBit0 else Bin("0", Bit1())
                    val c = binSumBit9.toBin().getBit(0)
                    setFlags(arch, v = v, c = c)

                    ac.set(subBit8)
                    setFlags(arch, checkZero = subBit8, n = subBit8.getBit(0))
                }

                AND -> {
                    if (operand == null) {
                        arch.getConsole().error("Couldn't load operand for ${this.name} ${amode.name}!")
                        return
                    }

                    val and = ac.get().toBin() and operand.toBin()
                    ac.set(and)
                    setFlags(arch, n = and.getBit(0), checkZero = and)
                }

                EOR -> {
                    if (operand == null) {
                        arch.getConsole().error("Couldn't load operand for ${this.name} ${amode.name}!")
                        return
                    }

                    val xor = ac.get().toBin() xor operand.toBin()
                    ac.set(xor)
                    setFlags(arch, n = xor.getBit(0), checkZero = xor)
                }

                ORA -> {
                    if (operand == null) {
                        arch.getConsole().error("Couldn't load operand for ${this.name} ${amode.name}!")
                        return
                    }

                    val or = ac.get().toBin() or operand.toBin()
                    ac.set(or)
                    setFlags(arch, n = or.getBit(0), checkZero = or)
                }

                ASL -> {
                    when (amode) {
                        ACCUMULATOR -> {
                            val acBin = ac.get().toBin()
                            val shiftRes = acBin shl 1
                            ac.set(shiftRes)
                            setFlags(arch, c = acBin.getBit(0), checkZero = shiftRes, n = shiftRes.getBit(0))
                        }

                        else -> {
                            address?.let {
                                val memBin = arch.getMemory().load(it)
                                val shiftRes = memBin shl 1
                                arch.getMemory().store(it, shiftRes)
                                setFlags(arch, c = memBin.getBit(0), checkZero = shiftRes, n = shiftRes.getBit(0))
                            } ?: arch.getConsole().error("Couldn't load address for ${this.name} ${amode.name}!")
                        }
                    }
                }

                LSR -> {
                    when (amode) {
                        ACCUMULATOR -> {
                            val shifted = ac.get().toBin().ushr(1)
                            setFlags(arch, n = Bin("0", Bit1()), checkZero = shifted, c = ac.get().toBin().getBit(7))
                            ac.set(shifted)
                        }

                        else -> {
                            address?.let {
                                val loaded = arch.getMemory().load(it)
                                val shifted = loaded ushr 1
                                setFlags(arch, n = Bin("0", Bit1()), checkZero = shifted, c = loaded.getBit(7))
                                arch.getMemory().store(it, shifted)
                            } ?: arch.getConsole().error("Couldn't load address for ${this.name} ${amode.name}!")
                        }
                    }
                }

                ROL -> {
                    when (amode) {
                        ACCUMULATOR -> {
                            val acBin = ac.get().toBin()
                            val rotated = acBin.ushl(1) and flags.c.getUResized(BYTE_SIZE)
                            setFlags(arch, n = rotated.getBit(0), checkZero = rotated, c = acBin.getBit(0))
                            ac.set(rotated)
                        }

                        else -> {
                            address?.let {
                                val loaded = arch.getMemory().load(it)
                                val rotated = loaded.ushl(1) and flags.c.getUResized(BYTE_SIZE)
                                setFlags(arch, n = rotated.getBit(0), checkZero = rotated, c = loaded.getBit(0))
                                arch.getMemory().store(it, rotated)
                            } ?: arch.getConsole().error("Couldn't load address for ${this.name} ${amode.name}!")
                        }
                    }
                }

                ROR -> {
                    when (amode) {
                        ACCUMULATOR -> {
                            val acBin = ac.get().toBin()
                            val rotated = acBin.ushr(1) and Bin(flags.c.getRawBinStr() + "0000000", BYTE_SIZE)
                            setFlags(arch, n = rotated.getBit(0), checkZero = rotated, c = acBin.getBit(7))
                            ac.set(rotated)
                        }

                        else -> {
                            address?.let {
                                val loaded = arch.getMemory().load(it)
                                val rotated = loaded.ushr(1) and Bin(flags.c.getRawBinStr() + "0000000", BYTE_SIZE)
                                setFlags(arch, n = rotated.getBit(0), checkZero = rotated, c = loaded.getBit(7))
                                arch.getMemory().store(it, rotated)
                            } ?: arch.getConsole().error("Couldn't load address for ${this.name} ${amode.name}!")
                        }
                    }
                }

                CLC -> setFlags(arch, c = Bin("0", Bit1()))
                CLD -> setFlags(arch, d = Bin("0", Bit1()))
                CLI -> setFlags(arch, i = Bin("0", Bit1()))
                CLV -> setFlags(arch, v = Bin("0", Bit1()))

                SEC -> setFlags(arch, c = Bin("1", Bit1()))
                SED -> setFlags(arch, d = Bin("1", Bit1()))
                SEI -> setFlags(arch, i = Bin("1", Bit1()))

                CMP -> {
                    if (operand == null) {
                        arch.getConsole().error("Couldn't load operand for ${this.name} ${amode.name}!")
                        return
                    }

                    val cmpResult = (ac.get().toBin() - operand).toBin()
                    val c = if (operand.toDec() <= ac.get().toDec()) Bin("1", Bit1()) else Bin("0", Bit1())
                    setFlags(arch, checkZero = cmpResult, n = cmpResult.getBit(0), c = c)
                }

                CPX -> {
                    if (operand == null) {
                        arch.getConsole().error("Couldn't load operand for ${this.name} ${amode.name}!")
                        return
                    }

                    val cmpResult = (x.get().toBin() - operand).toBin()
                    val c = if (operand.toDec() <= x.get().toDec()) Bin("1", Bit1()) else Bin("0", Bit1())
                    setFlags(arch, checkZero = cmpResult, n = cmpResult.getBit(0), c = c)
                }

                CPY -> {
                    if (operand == null) {
                        arch.getConsole().error("Couldn't load operand for ${this.name} ${amode.name}!")
                        return
                    }

                    val cmpResult = (y.get().toBin() - operand).toBin()
                    val c = if (operand.toDec() <= y.get().toDec()) Bin("1", Bit1()) else Bin("0", Bit1())
                    setFlags(arch, checkZero = cmpResult, n = cmpResult.getBit(0), c = c)
                }

                BCC -> {
                    if (operand == null) {
                        arch.getConsole().error("Couldn't load operand for ${this.name} ${amode.name}!")
                        return
                    }
                    if (flags.c == Bin("0", Bit1())) {
                        // Carry Clear
                        pc.set(operand)
                    } else {
                        // Carry not clear
                        val nextPC = pc.get() + if (amode.immSize == null) Hex("1", WORD_SIZE) else Hex((amode.immSize.getByteCount() + 1).toString(16), WORD_SIZE)
                        arch.getRegContainer().pc.set(nextPC)
                    }
                }

                BCS -> {
                    if (operand == null) {
                        arch.getConsole().error("Couldn't load operand for ${this.name} ${amode.name}!")
                        return
                    }
                    if (flags.c == Bin("1", Bit1())) {
                        // Carry Set
                        pc.set(operand)
                    } else {
                        // Carry not set
                        val nextPC = pc.get() + if (amode.immSize == null) Hex("1", WORD_SIZE) else Hex((amode.immSize.getByteCount() + 1).toString(16), WORD_SIZE)
                        arch.getRegContainer().pc.set(nextPC)
                    }
                }

                BEQ -> {
                    if (operand == null) {
                        arch.getConsole().error("Couldn't load operand for ${this.name} ${amode.name}!")
                        return
                    }
                    if (flags.z == Bin("1", Bit1())) {
                        // Zero
                        pc.set(operand)
                    } else {
                        // not zero
                        val nextPC = pc.get() + if (amode.immSize == null) Hex("1", WORD_SIZE) else Hex((amode.immSize.getByteCount() + 1).toString(16), WORD_SIZE)
                        arch.getRegContainer().pc.set(nextPC)
                    }
                }

                BMI -> {
                    if (operand == null) {
                        arch.getConsole().error("Couldn't load operand for ${this.name} ${amode.name}!")
                        return
                    }
                    if (flags.n == Bin("1", Bit1())) {
                        // negative
                        pc.set(operand)
                    } else {
                        // not negative
                        val nextPC = pc.get() + if (amode.immSize == null) Hex("1", WORD_SIZE) else Hex((amode.immSize.getByteCount() + 1).toString(16), WORD_SIZE)
                        arch.getRegContainer().pc.set(nextPC)
                    }
                }

                BNE -> {
                    if (operand == null) {
                        arch.getConsole().error("Couldn't load operand for ${this.name} ${amode.name}!")
                        return
                    }
                    if (flags.z == Bin("0", Bit1())) {
                        // Not Zero
                        pc.set(operand)
                    } else {
                        // zero
                        val nextPC = pc.get() + if (amode.immSize == null) Hex("1", WORD_SIZE) else Hex((amode.immSize.getByteCount() + 1).toString(16), WORD_SIZE)
                        arch.getRegContainer().pc.set(nextPC)
                    }
                }

                BPL -> {
                    if (operand == null) {
                        arch.getConsole().error("Couldn't load operand for ${this.name} ${amode.name}!")
                        return
                    }
                    if (flags.n == Bin("0", Bit1())) {
                        // positive
                        pc.set(operand)
                    } else {
                        // not positive
                        val nextPC = pc.get() + if (amode.immSize == null) Hex("1", WORD_SIZE) else Hex((amode.immSize.getByteCount() + 1).toString(16), WORD_SIZE)
                        arch.getRegContainer().pc.set(nextPC)
                    }
                }

                BVC -> {
                    if (operand == null) {
                        arch.getConsole().error("Couldn't load operand for ${this.name} ${amode.name}!")
                        return
                    }
                    if (flags.v == Bin("0", Bit1())) {
                        // overflow clear
                        pc.set(operand)
                    } else {
                        // overflow set
                        val nextPC = pc.get() + if (amode.immSize == null) Hex("1", WORD_SIZE) else Hex((amode.immSize.getByteCount() + 1).toString(16), WORD_SIZE)
                        arch.getRegContainer().pc.set(nextPC)
                    }
                }

                BVS -> {
                    if (operand == null) {
                        arch.getConsole().error("Couldn't load operand for ${this.name} ${amode.name}!")
                        return
                    }
                    if (flags.v == Bin("1", Bit1())) {
                        // overflow set
                        pc.set(operand)
                    } else {
                        // overflow clear
                        val nextPC = pc.get() + if (amode.immSize == null) Hex("1", WORD_SIZE) else Hex((amode.immSize.getByteCount() + 1).toString(16), WORD_SIZE)
                        arch.getRegContainer().pc.set(nextPC)
                    }
                }

                JMP -> {
                    when (amode) {
                        ABS -> {
                            pc.set(nextWord)
                        }

                        IND -> {
                            pc.set(Bin(pc.get().toBin().getRawBinStr().substring(0, 8) + arch.getMemory().load(nextWord).getRawBinStr(), WORD_SIZE))
                        }

                        else -> {}
                    }
                }

                JSR -> {
                    val nextPC = pc.get() + if (amode.immSize == null) Hex("1", WORD_SIZE) else Hex((amode.immSize.getByteCount() + 1).toString(16), WORD_SIZE)
                    arch.getMemory().store(sp.get().toBin().getUResized(WORD_SIZE) - Bin("1", WORD_SIZE), nextPC)
                    sp.set(sp.get().toBin() - Bin("10", BYTE_SIZE))
                    pc.set(nextWord)
                }

                RTS -> {
                    val loadedPC = arch.getMemory().load((sp.get().toBin().getUResized(WORD_SIZE) + Bin("1", WORD_SIZE)).toHex(), 2)
                    sp.set(sp.get().toBin() + Bin("10", BYTE_SIZE))
                    pc.set(loadedPC)
                }

                BRK -> {
                    // Store Return Address
                    val nextPC = pc.get() + if (amode.immSize == null) Hex("1", WORD_SIZE) else Hex((amode.immSize.getByteCount() + 1).toString(16), WORD_SIZE)
                    arch.getMemory().store(sp.get().toBin().getUResized(WORD_SIZE) - Bin("1", WORD_SIZE), nextPC)
                    sp.set(sp.get().toBin() - Bin("10", BYTE_SIZE))

                    // Store Status Register
                    arch.getMemory().store(sp.get().toBin().getUResized(WORD_SIZE), sr.get())
                    sp.set(sp.get().toBin() - Bin("1", BYTE_SIZE))

                    // Load Interrupt Vendor
                    val lsb = arch.getMemory().load(Hex("FFFE", WORD_SIZE)).toHex()
                    val msb = arch.getMemory().load(Hex("FFFF", WORD_SIZE)).toHex()

                    // Jump to Interrupt Handler Address
                    pc.set(Hex(msb.getRawHexStr() + lsb.getRawHexStr(), WORD_SIZE))
                }

                RTI -> {
                    // Load Status Register
                    sp.set(sp.get().toBin() + Bin("1", BYTE_SIZE))
                    val loadedSR = arch.getMemory().load(sp.get().toHex().getUResized(WORD_SIZE))
                    setFlags(arch, n = loadedSR.getBit(0), v = loadedSR.getBit(1), d = loadedSR.getBit(4), i = loadedSR.getBit(5), z = loadedSR.getBit(6), c = loadedSR.getBit(7))

                    // Load Return Address
                    sp.set(sp.get().toBin() + Bin("10", BYTE_SIZE))
                    val retAddr = arch.getMemory().load((sp.get().toHex().getUResized(WORD_SIZE) - Hex("1", WORD_SIZE)).toHex(), 2)

                    // Jump To Return Address
                    pc.set(retAddr)
                }

                BIT -> {
                    if (operand == null) {
                        arch.getConsole().error("Couldn't load operand for ${this.name} ${amode.name}!")
                        return
                    }
                    val result = ac.get().toBin() and operand.toBin()
                    setFlags(arch, n = operand.toBin().getBit(0), v = operand.toBin().getBit(1), checkZero = result)
                }

                NOP -> {

                }
            }


            // Increment PC
            when (this) {
                LDA, LDX, LDY, STA, STX, STY, TAX, TAY, TSX, TXA, TXS, TYA, PHA, PHP, PLA, PLP, DEC, DEX, DEY, INC, INX, INY, ADC, SBC, AND, EOR, ORA, ASL, LSR, ROL, ROR, CLC, CLD, CLI, CLV, SEC, SED, SEI, CMP, CPX, CPY, BIT, NOP -> {
                    // Standard PC Increment
                    val nextPC = pc.get() + if (amode.immSize == null) Hex("1", WORD_SIZE) else Hex((amode.immSize.getByteCount() + 1).toString(16), WORD_SIZE)
                    arch.getRegContainer().pc.set(nextPC)
                }

                else -> {
                    // Nothing to do for branches and jumps
                }
            }

        }

        private fun getOperand(arch: Architecture, amode: AModes, nextByte: Hex, nextWord: Hex): Hex? {
            val pc = arch.getRegContainer().pc
            val ac = arch.getRegByName("AC")
            val x = arch.getRegByName("X")
            val y = arch.getRegByName("Y")
            val sr = arch.getRegByName("SR")
            val sp = arch.getRegByName("SP")

            if (ac == null || x == null || y == null || sr == null || sp == null) return null

            return when (amode) {
                IMM -> {
                    nextByte
                }

                ZP -> {
                    arch.getMemory().load(Hex( "00${nextByte.getRawHexStr()}", WORD_SIZE)).toHex()
                }

                ZP_X -> {
                    val addr = Hex("00${(nextByte + x.get()).toHex().getRawHexStr()}", WORD_SIZE)
                    arch.getMemory().load(addr.toHex()).toHex()
                }

                ZP_Y -> {
                    val addr = Hex("00${(nextByte + y.get()).toHex().getRawHexStr()}", WORD_SIZE)
                    arch.getMemory().load(addr.toHex()).toHex()
                }

                ABS -> {
                    arch.getMemory().load(nextWord).toHex()
                }

                ABS_X -> {
                    val addr = nextWord + x.get().toHex()
                    arch.getMemory().load(addr.toHex()).toHex()
                }

                ABS_Y -> {
                    val addr = nextWord + y.get().toHex()
                    arch.getMemory().load(addr.toHex()).toHex()
                }

                IND -> {
                    val loadedAddr = arch.getMemory().load(nextWord).toHex()
                    arch.getMemory().load(loadedAddr).toHex()
                }

                ZP_X_IND -> {
                    val addr = Hex("00${(nextByte + x.get()).toHex().getRawHexStr()}", WORD_SIZE)
                    val loadedAddr = arch.getMemory().load(addr.toHex()).toHex()
                    arch.getMemory().load(loadedAddr).toHex()
                }

                ZPIND_Y -> {
                    val loadedAddr = arch.getMemory().load(Hex("00${nextByte.getRawHexStr()}", WORD_SIZE))
                    val incAddr = loadedAddr + y.get()
                    arch.getMemory().load(incAddr.toHex()).toHex()
                }

                ACCUMULATOR -> {
                    ac.get().toHex()
                }

                REL -> {
                    (pc.get() + nextByte.toBin().getResized(WORD_SIZE)).toHex()
                }

                else -> null
            }
        }

        private fun getAddress(arch: Architecture, amode: AModes, nextByte: Hex, nextWord: Hex, x: Hex, y: Hex): Hex? {
            return when (amode) {
                ABS -> nextWord
                ABS_X -> (nextWord + x.toBin().getResized(WORD_SIZE)).toHex()
                ABS_Y -> (nextWord + y.toBin().getResized(WORD_SIZE)).toHex()
                ZP -> Hex("00${nextByte.getRawHexStr()}", WORD_SIZE)
                ZP_X ->Hex("00${(nextByte + x).toHex().getRawHexStr()}", WORD_SIZE)
                ZP_Y -> Hex("00${(nextByte + y).toHex().getRawHexStr()}", WORD_SIZE)
                ZP_X_IND -> arch.getMemory().load(Hex("00${(nextByte + x).toHex().getRawHexStr()}", WORD_SIZE), 2).toHex()
                ZPIND_Y -> (arch.getMemory().load(Hex("00${nextByte.getRawHexStr()}", WORD_SIZE), 2) + y).toHex()
                else -> null
            }
        }

        private fun getFlags(arch: Architecture): Flags? {
            val sr = arch.getRegByName("SR") ?: return null
            val nflag = sr.get().toBin().getBit(0) ?: return null
            val vflag = sr.get().toBin().getBit(1) ?: return null
            val bflag = sr.get().toBin().getBit(3) ?: return null
            val dflag = sr.get().toBin().getBit(4) ?: return null
            val iflag = sr.get().toBin().getBit(5) ?: return null
            val zflag = sr.get().toBin().getBit(6) ?: return null
            val cflag = sr.get().toBin().getBit(7) ?: return null
            return Flags(nflag, vflag, zflag, cflag, dflag, bflag, iflag)
        }

        private fun setFlags(arch: Architecture, n: Bin? = null, v: Bin? = null, b: Bin? = null, d: Bin? = null, i: Bin? = null, z: Bin? = null, c: Bin? = null, checkZero: Bin? = null, seti: Boolean? = null, setd: Boolean? = null, setb: Boolean? = null) {
            val sr = arch.getRegByName("SR") ?: return

            var nflag = sr.get().toBin().getBit(0) ?: return
            var vflag = sr.get().toBin().getBit(1) ?: return
            var bflag = sr.get().toBin().getBit(3) ?: return
            var dflag = sr.get().toBin().getBit(4) ?: return
            var iflag = sr.get().toBin().getBit(5) ?: return
            var zflag = sr.get().toBin().getBit(6) ?: return
            var cflag = sr.get().toBin().getBit(7) ?: return

            if (n != null) nflag = n
            if (v != null) vflag = v
            if (b != null) bflag = b
            if (d != null) dflag = d
            if (i != null) iflag = i
            if (z != null) zflag = z
            if (c != null) cflag = c

            if (checkZero != null) {
                zflag = if (checkZero == Bin("0", BYTE_SIZE)) {
                    Bin("1", Bit1())
                } else {
                    Bin("0", Bit1())
                }
            }
            if (seti != null) {
                iflag = if (seti) Bin("1", Bit1()) else Bin("0", Bit1())
            }
            if (setd != null) {
                dflag = if (setd) Bin("1", Bit1()) else Bin("0", Bit1())
            }
            if (setb != null) {
                bflag = if (setb) Bin("1", Bit1()) else Bin("0", Bit1())
            }


            sr.set(Bin("${nflag.getRawBinStr()}${vflag.getRawBinStr()}1${bflag.getRawBinStr()}${dflag.getRawBinStr()}${iflag.getRawBinStr()}${zflag.getRawBinStr()}${cflag.getRawBinStr()}", BYTE_SIZE))
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
                    if (imm.getValue().size.bitWidth > amode.immSize.bitWidth) {
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

            val constant = constantToken.getValue(Bit16())
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

    class SText(vararg val elements: ElementNode) : TreeNode.ContainerNode(NAMES.S_TEXT, *elements)

    data class Flags(
        val n: Bin,
        val v: Bin,
        val z: Bin,
        val c: Bin,
        val d: Bin,
        val b: Bin,
        val i: Bin,
    )

}