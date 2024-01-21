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
import emulator.kit.assembly.Syntax.TokenSeq.Component.*
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
        LDA(mapOf(ABS to Hex("AD", BYTE_SIZE), ABS_X to Hex("BD", BYTE_SIZE), ABS_Y to Hex("B9", BYTE_SIZE), IMM to Hex("A9", BYTE_SIZE), ZP to Hex("A5", BYTE_SIZE), ZP_X_IND to Hex("A1", BYTE_SIZE), ZP_X to Hex("B5", BYTE_SIZE), ZPIND_Y to Hex("B1", BYTE_SIZE))) {
            override fun execute(arch: Architecture, amode: AModes, nextByte: Hex, nextWord: Hex) {
                TODO("Not yet implemented")
            }
        },
        LDX(mapOf(ABS to Hex("AE", BYTE_SIZE), ABS_Y to Hex("BE", BYTE_SIZE), IMM to Hex("A2", BYTE_SIZE), ZP to Hex("A6", BYTE_SIZE), ZP_Y to Hex("B6", BYTE_SIZE))) {
            override fun execute(arch: Architecture, amode: AModes, nextByte: Hex, nextWord: Hex) {
                TODO("Not yet implemented")
            }
        },
        LDY(mapOf(ABS to Hex("AC", BYTE_SIZE), ABS_X to Hex("BC", BYTE_SIZE), IMM to Hex("A0", BYTE_SIZE), ZP to Hex("A4", BYTE_SIZE), ZP_X to Hex("B4", BYTE_SIZE))) {
            override fun execute(arch: Architecture, amode: AModes, nextByte: Hex, nextWord: Hex) {
                TODO("Not yet implemented")
            }
        },
        STA(mapOf(ABS to Hex("8D", BYTE_SIZE), ABS_X to Hex("9D", BYTE_SIZE), ABS_Y to Hex("99", BYTE_SIZE), ZP to Hex("85", BYTE_SIZE), ZP_X_IND to Hex("81", BYTE_SIZE), ZP_X to Hex("95", BYTE_SIZE), ZPIND_Y to Hex("91", BYTE_SIZE))) {
            override fun execute(arch: Architecture, amode: AModes, nextByte: Hex, nextWord: Hex) {
                TODO("Not yet implemented")
            }
        },
        STX(mapOf(ABS to Hex("8E", BYTE_SIZE), ZP to Hex("86", BYTE_SIZE), ZP_Y to Hex("96", BYTE_SIZE))) {
            override fun execute(arch: Architecture, amode: AModes, nextByte: Hex, nextWord: Hex) {
                TODO("Not yet implemented")
            }
        },
        STY(mapOf(ABS to Hex("8C", BYTE_SIZE), ZP to Hex("84", BYTE_SIZE), ZP_X to Hex("94", BYTE_SIZE))) {
            override fun execute(arch: Architecture, amode: AModes, nextByte: Hex, nextWord: Hex) {
                TODO("Not yet implemented")
            }
        },
        TAX(mapOf(IMPLIED to Hex("AA", BYTE_SIZE))) {
            override fun execute(arch: Architecture, amode: AModes, nextByte: Hex, nextWord: Hex) {
                TODO("Not yet implemented")
            }
        },
        TAY(mapOf(IMPLIED to Hex("A8", BYTE_SIZE))) {
            override fun execute(arch: Architecture, amode: AModes, nextByte: Hex, nextWord: Hex) {
                TODO("Not yet implemented")
            }
        },
        TSX(mapOf(IMPLIED to Hex("BA", BYTE_SIZE))) {
            override fun execute(arch: Architecture, amode: AModes, nextByte: Hex, nextWord: Hex) {
                TODO("Not yet implemented")
            }
        },
        TXA(mapOf(IMPLIED to Hex("8A", BYTE_SIZE))) {
            override fun execute(arch: Architecture, amode: AModes, nextByte: Hex, nextWord: Hex) {
                TODO("Not yet implemented")
            }
        },
        TXS(mapOf(IMPLIED to Hex("9A", BYTE_SIZE))) {
            override fun execute(arch: Architecture, amode: AModes, nextByte: Hex, nextWord: Hex) {
                TODO("Not yet implemented")
            }
        },
        TYA(mapOf(IMPLIED to Hex("98", BYTE_SIZE))) {
            override fun execute(arch: Architecture, amode: AModes, nextByte: Hex, nextWord: Hex) {
                TODO("Not yet implemented")
            }
        },

        // stack
        PHA(mapOf(IMPLIED to Hex("48", BYTE_SIZE))) {
            override fun execute(arch: Architecture, amode: AModes, nextByte: Hex, nextWord: Hex) {
                TODO("Not yet implemented")
            }
        },
        PHP(mapOf(IMPLIED to Hex("08", BYTE_SIZE))) {
            override fun execute(arch: Architecture, amode: AModes, nextByte: Hex, nextWord: Hex) {
                TODO("Not yet implemented")
            }
        },
        PLA(mapOf(IMPLIED to Hex("68", BYTE_SIZE))) {
            override fun execute(arch: Architecture, amode: AModes, nextByte: Hex, nextWord: Hex) {
                TODO("Not yet implemented")
            }
        },
        PLP(mapOf(IMPLIED to Hex("28", BYTE_SIZE))) {
            override fun execute(arch: Architecture, amode: AModes, nextByte: Hex, nextWord: Hex) {
                TODO("Not yet implemented")
            }
        },

        // decrements, increments
        DEC(mapOf(ABS to Hex("CE", BYTE_SIZE), ABS_X to Hex("DE", BYTE_SIZE), ZP to Hex("C6", BYTE_SIZE), ZP_X to Hex("D6", BYTE_SIZE))) {
            override fun execute(arch: Architecture, amode: AModes, nextByte: Hex, nextWord: Hex) {
                TODO("Not yet implemented")
            }
        },
        DEX(mapOf(IMPLIED to Hex("CA", BYTE_SIZE))) {
            override fun execute(arch: Architecture, amode: AModes, nextByte: Hex, nextWord: Hex) {
                TODO("Not yet implemented")
            }
        },
        DEY(mapOf(IMPLIED to Hex("88", BYTE_SIZE))) {
            override fun execute(arch: Architecture, amode: AModes, nextByte: Hex, nextWord: Hex) {
                TODO("Not yet implemented")
            }
        },
        INC(mapOf(ABS to Hex("EE", BYTE_SIZE), ABS_X to Hex("FE", BYTE_SIZE), ZP to Hex("E6", BYTE_SIZE), ZP_X to Hex("F6", BYTE_SIZE))) {
            override fun execute(arch: Architecture, amode: AModes, nextByte: Hex, nextWord: Hex) {
                TODO("Not yet implemented")
            }
        },
        INX(mapOf(IMPLIED to Hex("E8", BYTE_SIZE))) {
            override fun execute(arch: Architecture, amode: AModes, nextByte: Hex, nextWord: Hex) {
                TODO("Not yet implemented")
            }
        },
        INY(mapOf(IMPLIED to Hex("C8", BYTE_SIZE))) {
            override fun execute(arch: Architecture, amode: AModes, nextByte: Hex, nextWord: Hex) {
                TODO("Not yet implemented")
            }
        },

        // arithmetic operations
        ADC(mapOf(ABS to Hex("6D", BYTE_SIZE), ABS_X to Hex("7D", BYTE_SIZE), ABS_Y to Hex("79", BYTE_SIZE), IMM to Hex("69", BYTE_SIZE), ZP to Hex("65", BYTE_SIZE), ZP_X_IND to Hex("61", BYTE_SIZE), ZP_X to Hex("75", BYTE_SIZE), ZPIND_Y to Hex("71", BYTE_SIZE))) {
            override fun execute(arch: Architecture, amode: AModes, nextByte: Hex, nextWord: Hex) {
                val operand = this.getOperand(arch, amode, nextByte, nextWord)
                val ac = arch.getRegByName("AC")
                val x = arch.getRegByName("X")
                val y = arch.getRegByName("Y")
                val sr = arch.getRegByName("SR")
                val sp = arch.getRegByName("SP")

                if (ac == null || x == null || y == null || sr == null || sp == null) {
                    arch.getConsole().error("Register missing!")
                    return
                }

                if (operand == null) {
                    arch.getConsole().error("Couldn't calculate operand!")
                    return
                }

                val nflag = sr.get().toBin().getBit(0) ?: return
                val vflag = sr.get().toBin().getBit(1) ?: return
                val bflag = sr.get().toBin().getBit(3) ?: return
                val dflag = sr.get().toBin().getBit(4) ?: return
                val iflag = sr.get().toBin().getBit(5) ?: return
                val zflag = sr.get().toBin().getBit(6) ?: return
                val cflag = sr.get().toBin().getBit(7) ?: return


                val result = ac.get().toBin().getUResized(WORD_SIZE) + operand + cflag
                ac.set(result.toBin().getUResized(BYTE_SIZE))


                val nextPC = arch.getRegContainer().pc.get() + if (amode.immSize == null) Hex("1", WORD_SIZE) else Hex((amode.immSize.getByteCount() + 1).toString(16), WORD_SIZE)
                arch.getRegContainer().pc.set(nextPC)
            }
        },
        SBC(mapOf(ABS to Hex("ED", BYTE_SIZE), ABS_X to Hex("FD", BYTE_SIZE), ABS_Y to Hex("F9", BYTE_SIZE), IMM to Hex("E9", BYTE_SIZE), ZP to Hex("E5", BYTE_SIZE), ZP_X_IND to Hex("E1", BYTE_SIZE), ZP_X to Hex("F5", BYTE_SIZE), ZPIND_Y to Hex("F1", BYTE_SIZE))) {
            override fun execute(arch: Architecture, amode: AModes, nextByte: Hex, nextWord: Hex) {
                TODO("Not yet implemented")
            }
        },

        // logical operations
        AND(mapOf(ABS to Hex("2D", BYTE_SIZE), ABS_X to Hex("3D", BYTE_SIZE), ABS_Y to Hex("39", BYTE_SIZE), IMM to Hex("29", BYTE_SIZE), ZP to Hex("25", BYTE_SIZE), ZP_X_IND to Hex("21", BYTE_SIZE), ZP_X to Hex("35", BYTE_SIZE), ZPIND_Y to Hex("31", BYTE_SIZE))) {
            override fun execute(arch: Architecture, amode: AModes, nextByte: Hex, nextWord: Hex) {
                TODO("Not yet implemented")
            }
        },
        EOR(mapOf(ABS to Hex("0D", BYTE_SIZE), ABS_X to Hex("1D", BYTE_SIZE), ABS_Y to Hex("19", BYTE_SIZE), IMM to Hex("09", BYTE_SIZE), ZP to Hex("05", BYTE_SIZE), ZP_X_IND to Hex("01", BYTE_SIZE), ZP_X to Hex("15", BYTE_SIZE), ZPIND_Y to Hex("11", BYTE_SIZE))) {
            override fun execute(arch: Architecture, amode: AModes, nextByte: Hex, nextWord: Hex) {
                TODO("Not yet implemented")
            }
        },
        ORA(mapOf(ABS to Hex("4D", BYTE_SIZE), ABS_X to Hex("5D", BYTE_SIZE), ABS_Y to Hex("59", BYTE_SIZE), IMM to Hex("49", BYTE_SIZE), ZP to Hex("45", BYTE_SIZE), ZP_X_IND to Hex("41", BYTE_SIZE), ZP_X to Hex("55", BYTE_SIZE), ZPIND_Y to Hex("51", BYTE_SIZE))) {
            override fun execute(arch: Architecture, amode: AModes, nextByte: Hex, nextWord: Hex) {
                TODO("Not yet implemented")
            }
        },

        // shift & rotate
        ASL(mapOf(ABS to Hex("0E", BYTE_SIZE), ABS_X to Hex("1E", BYTE_SIZE), ACCUMULATOR to Hex("0A", BYTE_SIZE), ZP to Hex("06", BYTE_SIZE), ZP_X to Hex("16", BYTE_SIZE))) {
            override fun execute(arch: Architecture, amode: AModes, nextByte: Hex, nextWord: Hex) {
                TODO("Not yet implemented")
            }
        },
        LSR(mapOf(ABS to Hex("4E", BYTE_SIZE), ABS_X to Hex("5E", BYTE_SIZE), ACCUMULATOR to Hex("4A", BYTE_SIZE), ZP to Hex("46", BYTE_SIZE), ZP_X to Hex("56", BYTE_SIZE))) {
            override fun execute(arch: Architecture, amode: AModes, nextByte: Hex, nextWord: Hex) {
                TODO("Not yet implemented")
            }
        },
        ROL(mapOf(ABS to Hex("2E", BYTE_SIZE), ABS_X to Hex("3E", BYTE_SIZE), ACCUMULATOR to Hex("2A", BYTE_SIZE), ZP to Hex("26", BYTE_SIZE), ZP_X to Hex("36", BYTE_SIZE))) {
            override fun execute(arch: Architecture, amode: AModes, nextByte: Hex, nextWord: Hex) {
                TODO("Not yet implemented")
            }
        },
        ROR(mapOf(ABS to Hex("6E", BYTE_SIZE), ABS_X to Hex("7E", BYTE_SIZE), ACCUMULATOR to Hex("6A", BYTE_SIZE), ZP to Hex("66", BYTE_SIZE), ZP_X to Hex("76", BYTE_SIZE))) {
            override fun execute(arch: Architecture, amode: AModes, nextByte: Hex, nextWord: Hex) {
                TODO("Not yet implemented")
            }
        },

        // flag
        CLC(mapOf(IMPLIED to Hex("18", BYTE_SIZE))) {
            override fun execute(arch: Architecture, amode: AModes, nextByte: Hex, nextWord: Hex) {
                TODO("Not yet implemented")
            }
        },
        CLD(mapOf(IMPLIED to Hex("D8", BYTE_SIZE))) {
            override fun execute(arch: Architecture, amode: AModes, nextByte: Hex, nextWord: Hex) {
                TODO("Not yet implemented")
            }
        },
        CLI(mapOf(IMPLIED to Hex("58", BYTE_SIZE))) {
            override fun execute(arch: Architecture, amode: AModes, nextByte: Hex, nextWord: Hex) {
                TODO("Not yet implemented")
            }
        },
        CLV(mapOf(IMPLIED to Hex("B8", BYTE_SIZE))) {
            override fun execute(arch: Architecture, amode: AModes, nextByte: Hex, nextWord: Hex) {
                TODO("Not yet implemented")
            }
        },
        SEC(mapOf(IMPLIED to Hex("38", BYTE_SIZE))) {
            override fun execute(arch: Architecture, amode: AModes, nextByte: Hex, nextWord: Hex) {
                TODO("Not yet implemented")
            }
        },
        SED(mapOf(IMPLIED to Hex("F8", BYTE_SIZE))) {
            override fun execute(arch: Architecture, amode: AModes, nextByte: Hex, nextWord: Hex) {
                TODO("Not yet implemented")
            }
        },
        SEI(mapOf(IMPLIED to Hex("78", BYTE_SIZE))) {
            override fun execute(arch: Architecture, amode: AModes, nextByte: Hex, nextWord: Hex) {
                TODO("Not yet implemented")
            }
        },

        // comparison
        CMP(mapOf(ABS to Hex("CD", BYTE_SIZE), ABS_X to Hex("DD", BYTE_SIZE), ABS_Y to Hex("D9", BYTE_SIZE), IMM to Hex("C9", BYTE_SIZE), ZP to Hex("C5", BYTE_SIZE), ZP_X_IND to Hex("C1", BYTE_SIZE), ZP_X to Hex("D5", BYTE_SIZE), ZPIND_Y to Hex("D1", BYTE_SIZE))) {
            override fun execute(arch: Architecture, amode: AModes, nextByte: Hex, nextWord: Hex) {
                TODO("Not yet implemented")
            }
        },
        CPX(mapOf(ABS to Hex("EC", BYTE_SIZE), IMM to Hex("E0", BYTE_SIZE), ZP to Hex("E4", BYTE_SIZE))) {
            override fun execute(arch: Architecture, amode: AModes, nextByte: Hex, nextWord: Hex) {
                TODO("Not yet implemented")
            }
        },
        CPY(mapOf(ABS to Hex("CC", BYTE_SIZE), IMM to Hex("C0", BYTE_SIZE), ZP to Hex("C4", BYTE_SIZE))) {
            override fun execute(arch: Architecture, amode: AModes, nextByte: Hex, nextWord: Hex) {
                TODO("Not yet implemented")
            }
        },

        // conditional branches
        BCC(mapOf(REL to Hex("90", BYTE_SIZE))) {
            override fun execute(arch: Architecture, amode: AModes, nextByte: Hex, nextWord: Hex) {
                TODO("Not yet implemented")
            }
        },
        BCS(mapOf(REL to Hex("B0", BYTE_SIZE))) {
            override fun execute(arch: Architecture, amode: AModes, nextByte: Hex, nextWord: Hex) {
                TODO("Not yet implemented")
            }
        },
        BEQ(mapOf(REL to Hex("F0", BYTE_SIZE))) {
            override fun execute(arch: Architecture, amode: AModes, nextByte: Hex, nextWord: Hex) {
                TODO("Not yet implemented")
            }
        },
        BMI(mapOf(REL to Hex("30", BYTE_SIZE))) {
            override fun execute(arch: Architecture, amode: AModes, nextByte: Hex, nextWord: Hex) {
                TODO("Not yet implemented")
            }
        },
        BNE(mapOf(REL to Hex("D0", BYTE_SIZE))) {
            override fun execute(arch: Architecture, amode: AModes, nextByte: Hex, nextWord: Hex) {
                TODO("Not yet implemented")
            }
        },
        BPL(mapOf(REL to Hex("10", BYTE_SIZE))) {
            override fun execute(arch: Architecture, amode: AModes, nextByte: Hex, nextWord: Hex) {
                TODO("Not yet implemented")
            }
        },
        BVC(mapOf(REL to Hex("50", BYTE_SIZE))) {
            override fun execute(arch: Architecture, amode: AModes, nextByte: Hex, nextWord: Hex) {
                TODO("Not yet implemented")
            }
        },
        BVS(mapOf(REL to Hex("70", BYTE_SIZE))) {
            override fun execute(arch: Architecture, amode: AModes, nextByte: Hex, nextWord: Hex) {
                TODO("Not yet implemented")
            }
        },

        // jumps, subroutines
        JMP(mapOf(ABS to Hex("4C", BYTE_SIZE), IND to Hex("6C", BYTE_SIZE))) {
            override fun execute(arch: Architecture, amode: AModes, nextByte: Hex, nextWord: Hex) {
                TODO("Not yet implemented")
            }
        },
        JSR(mapOf(ABS to Hex("20", BYTE_SIZE))) {
            override fun execute(arch: Architecture, amode: AModes, nextByte: Hex, nextWord: Hex) {
                TODO("Not yet implemented")
            }
        },
        RTS(mapOf(IMPLIED to Hex("60", BYTE_SIZE))) {
            override fun execute(arch: Architecture, amode: AModes, nextByte: Hex, nextWord: Hex) {
                TODO("Not yet implemented")
            }
        },

        // interrupts
        BRK(mapOf(IMPLIED to Hex("00", BYTE_SIZE))) {
            override fun execute(arch: Architecture, amode: AModes, nextByte: Hex, nextWord: Hex) {
                TODO("Not yet implemented")
            }
        },
        RTI(mapOf(IMPLIED to Hex("40", BYTE_SIZE))) {
            override fun execute(arch: Architecture, amode: AModes, nextByte: Hex, nextWord: Hex) {
                TODO("Not yet implemented")
            }
        },

        // other
        BIT(mapOf(ABS to Hex("2C", BYTE_SIZE), IMM to Hex("89", BYTE_SIZE), ZP to Hex("24", BYTE_SIZE))) {
            override fun execute(arch: Architecture, amode: AModes, nextByte: Hex, nextWord: Hex) {
                TODO("Not yet implemented")
            }
        },
        NOP(mapOf(IMPLIED to Hex("EA", BYTE_SIZE))) {
            override fun execute(arch: Architecture, amode: AModes, nextByte: Hex, nextWord: Hex) {
                TODO("Not yet implemented")
            }
        };

        abstract fun execute(arch: Architecture, amode: AModes, nextByte: Hex, nextWord: Hex)

        fun getOperand(arch: Architecture, amode: AModes, nextByte: Hex, nextWord: Hex): Hex? {
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
                    arch.getMemory().load(nextByte.getUResized(WORD_SIZE)).toHex()
                }

                ZP_X -> {
                    val addr = (nextByte + x.get()).toHex().getUResized(WORD_SIZE)
                    arch.getMemory().load(addr.toHex()).toHex()
                }

                ZP_Y -> {
                    val addr = (nextByte + y.get()).toHex().getUResized(WORD_SIZE)
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
                    val addr = (nextByte + x.get()).toHex().getUResized(WORD_SIZE)
                    val loadedAddr = arch.getMemory().load(addr.toHex()).toHex()
                    arch.getMemory().load(loadedAddr).toHex()
                }

                ZPIND_Y -> {
                    val loadedAddr = arch.getMemory().load(nextWord)
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

        fun setFlags(arch: Architecture, result16: Bin, n: Boolean = false, z: Boolean = false, c: Boolean = false,  v: Boolean = false,  seti: Boolean? = null, setd: Boolean? = null, setb: Boolean? = null) {
            val sr = arch.getRegByName("SR") ?: return

            var nflag = sr.get().toBin().getBit(0) ?: return
            var vflag = sr.get().toBin().getBit(1) ?: return
            var bflag = sr.get().toBin().getBit(3) ?: return
            var dflag = sr.get().toBin().getBit(4) ?: return
            var iflag = sr.get().toBin().getBit(5) ?: return
            var zflag = sr.get().toBin().getBit(6) ?: return
            var cflag = sr.get().toBin().getBit(7) ?: return

            if (n) {
                nflag = result16.getBit(8) ?: return
            }
            if (z) {
                zflag = if (result16 == Bin("0", WORD_SIZE)) {
                    Bin("1", Bit1())
                } else {
                    Bin("0", Bit1())
                }
            }
            if (c) {
                cflag = result16.getBit(7) ?: return
            }
            if (seti != null) {
                iflag = if(seti) Bin("1", Bit1()) else Bin("0", Bit1())
            }
            if (setd != null) {
                dflag = if(setd) Bin("1", Bit1()) else Bin("0", Bit1())
            }
            if(setb != null){
                bflag = if(setb) Bin("1", Bit1()) else Bin("0", Bit1())
            }
            if (v) {
                vflag = if (Bin(result16.getRawBinaryStr().substring(0, 8), BYTE_SIZE) == Bin("0", BYTE_SIZE)) {
                    Bin("0", Bit1())
                } else {
                    Bin("1", Bit1())
                }
            }

            sr.set(Bin("${nflag.getRawBinaryStr()}${vflag.getRawBinaryStr()}0${bflag.getRawBinaryStr()}${dflag.getRawBinaryStr()}${iflag.getRawBinaryStr()}${zflag.getRawBinaryStr()}${cflag.getRawBinaryStr()}", BYTE_SIZE))
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