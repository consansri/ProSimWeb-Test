package emulator.archs.t6502

import emulator.kit.Architecture
import emulator.kit.assembly.Compiler
import emulator.kit.types.Variable.Value.*
import emulator.archs.t6502.T6502Syntax.AModes.*
import emulator.archs.t6502.T6502.BYTE_SIZE
import emulator.archs.t6502.T6502.WORD_SIZE
import emulator.kit.assembly.Syntax.TokenSeq.Component.InSpecific.*
import emulator.kit.assembly.Syntax.TokenSeq.Component.Specific
import emulator.kit.assembly.Syntax.TokenSeq.Component.SpecConst
import emulator.kit.assembly.Syntax.TokenSeq.Component.WordOrSpecConst
import emulator.kit.assembly.standards.StandardSyntax
import emulator.kit.types.Variable
import emulator.kit.types.Variable.Size.*

/**
 * T6502 Syntax
 *
 */
class T6502Syntax : StandardSyntax(T6502.MEM_ADDR_SIZE, commentStartSymbol = ';', instrParamsCanContainWordsBesideLabels = true) {
    override fun MutableList<Compiler.Token>.checkInstr(elements: MutableList<TreeNode.ElementNode>, errors: MutableList<Error>, warnings: MutableList<Warning>, currentLabel: ELabel?): Boolean {
        for (amode in AModes.entries) {
            val amodeResult = amode.tokenSequence.matchStart(*this.toTypedArray())

            if (amodeResult.matches) {

                val type = InstrType.entries.firstOrNull { it.name.uppercase() == amodeResult.sequenceMap[0].token.content.uppercase() } ?: return false

                if (!type.opCode.keys.contains(amode)) continue

                val eInstr = T6502Instr(type, amode, parentLabel = currentLabel, nameToken = amodeResult.sequenceMap[0].token, params = amodeResult.sequenceMap.map { it.token }.drop(1))
                elements.add(eInstr)
                eInstr.tokens.forEach {
                    this.remove(it)
                }
                return true
            }
        }
        return false
    }


    /**
     * Addressing Modes
     * IMPORTANT: The order of the enums determines the order in which the modes will be checked!
     */
    enum class AModes(val tokenSequence: TokenSeq, val byteAmount: Int, val exampleString: String, val description: String) {

        ZP_X(TokenSeq(Word, Space, SpecConst(BYTE_SIZE), Specific(","), Specific("X"), NewLine, ignoreSpaces = true), 2, exampleString = "$00, X", description = "zeropage, X-indexed"), // Zero Page Indexed with X: zp,x
        ZP_Y(TokenSeq(Word, Space, SpecConst(BYTE_SIZE), Specific(","), Specific("Y"), NewLine, ignoreSpaces = true), 2, exampleString = "$00, Y", description = "zeropage, Y-indexed"), // Zero Page Indexed with Y: zp,y

        ABS_X(TokenSeq(Word, Space, WordOrSpecConst(WORD_SIZE), Specific(","), Specific("X"), NewLine, ignoreSpaces = true), 3, exampleString = "$0000, X", description = "absolute, X-indexed"), // Absolute Indexed with X: a,x
        ABS_Y(TokenSeq(Word, Space, WordOrSpecConst(WORD_SIZE), Specific(","), Specific("Y"), NewLine, ignoreSpaces = true), 3, exampleString = "$0000, Y", description = "absolute, Y-indexed"), // Absolute Indexed with Y: a,y
        ZP_X_IND(TokenSeq(Word, Space, Specific("("), SpecConst(BYTE_SIZE), Specific(","), Specific("X"), Specific(")"), NewLine, ignoreSpaces = true), 2, exampleString = "($00, X)", description = "X-indexed, indirect"), // Zero Page Indexed Indirect: (zp,x)

        ZPIND_Y(TokenSeq(Word, Space, Specific("("), SpecConst(BYTE_SIZE), Specific(")"), Specific(","), Specific("Y"), NewLine, ignoreSpaces = true), 2, exampleString = "($00), Y", description = "indirect, Y-indexed"), // Zero Page Indirect Indexed with Y: (zp),y

        IND(TokenSeq(Word, Space, Specific("("), WordOrSpecConst(WORD_SIZE), Specific(")"), NewLine, ignoreSpaces = true), 3, exampleString = "($0000)", description = "indirect"), // Absolute Indirect: (a)

        ACCUMULATOR(TokenSeq(Word, Space, Specific("A"), NewLine, ignoreSpaces = true), 1, exampleString = "A", description = "Accumulator"), // Accumulator: A
        IMM(TokenSeq(Word, Space, Specific("#"), SpecConst(BYTE_SIZE), NewLine, ignoreSpaces = true), 2, exampleString = "#$00", description = "immediate"), // Immediate: #
        REL(TokenSeq(Word, Space, SpecConst(BYTE_SIZE), NewLine, ignoreSpaces = true), 2, exampleString = "$00", description = "relative"), // Relative: r
        ZP(TokenSeq(Word, Space, SpecConst(BYTE_SIZE), NewLine, ignoreSpaces = true), 2, exampleString = "$00", description = "zeropage"), // Zero Page: zp
        ABS(TokenSeq(Word, Space, WordOrSpecConst(WORD_SIZE), NewLine, ignoreSpaces = true), 3, exampleString = "$0000", description = "absolute"), // Absolute: a

        IMPLIED(TokenSeq(Word, NewLine, ignoreSpaces = true), 1, exampleString = "", description = "implied"); // Implied: i

        fun getString(threeByte: Array<Bin>): String {
            val smallVal = threeByte.get(1).toHex().getRawHexStr()
            val bigVal = threeByte.drop(1).joinToString("") { it.toHex().getRawHexStr() }
            return when (this) {
                ZP_X -> "$${smallVal}, X"
                ZP_Y -> "$${smallVal}, Y"
                ABS_X -> "$${bigVal}, X"
                ABS_Y -> "$${bigVal}, Y"
                ZP_X_IND -> "($${smallVal}, X)"
                ZPIND_Y -> "($${smallVal}), Y"
                IND -> "(${bigVal})"
                ACCUMULATOR -> "A"
                IMM -> "#$${smallVal}"
                REL -> "$${smallVal}"
                ZP -> "$${smallVal}"
                ABS -> "$${bigVal}"
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

        fun execute(arch: Architecture, amode: AModes, threeBytes: Array<Bin>) {

            val smallVal = threeBytes.drop(1).first().toHex()
            val bigVal = Hex(threeBytes.drop(1).joinToString("") { it.toHex().getRawHexStr() }, WORD_SIZE)

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
            val operand = this.getOperand(arch, amode, smallVal, bigVal)
            val address = this.getAddress(arch, amode, smallVal, bigVal, x.get().toHex(), y.get().toHex())

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
                    val binSumBit9 = ac.get().toBin().getUResized(Bit9()) + operand + flags.c
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
                        val nextPC = pc.get() + Hex(amode.byteAmount.toString(16), WORD_SIZE)
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
                        val nextPC = pc.get() + Hex(amode.byteAmount.toString(16), WORD_SIZE)
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
                        val nextPC = pc.get() + Hex(amode.byteAmount.toString(16), WORD_SIZE)
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
                        val nextPC = pc.get() + Hex(amode.byteAmount.toString(16), WORD_SIZE)
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
                        val nextPC = pc.get() + Hex(amode.byteAmount.toString(16), WORD_SIZE)
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
                        val nextPC = pc.get() + Hex(amode.byteAmount.toString(16), WORD_SIZE)
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
                        val nextPC = pc.get() + Hex(amode.byteAmount.toString(16), WORD_SIZE)
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
                        val nextPC = pc.get() + Hex(amode.byteAmount.toString(16), WORD_SIZE)
                        arch.getRegContainer().pc.set(nextPC)
                    }
                }

                JMP -> {
                    when (amode) {
                        ABS -> {
                            pc.set(bigVal)
                        }

                        IND -> {
                            pc.set(Bin(pc.get().toBin().getRawBinStr().substring(0, 8) + arch.getMemory().load(bigVal).getRawBinStr(), WORD_SIZE))
                        }

                        else -> {}
                    }
                }

                JSR -> {
                    val nextPC = pc.get() + Hex(amode.byteAmount.toString(16), WORD_SIZE)
                    arch.getMemory().store(sp.get().toBin().getUResized(WORD_SIZE) - Bin("1", WORD_SIZE), nextPC)
                    sp.set(sp.get().toBin() - Bin("10", BYTE_SIZE))
                    pc.set(bigVal)
                }

                RTS -> {
                    val loadedPC = arch.getMemory().load((sp.get().toBin().getUResized(WORD_SIZE) + Bin("1", WORD_SIZE)).toHex(), 2)
                    sp.set(sp.get().toBin() + Bin("10", BYTE_SIZE))
                    pc.set(loadedPC)
                }

                BRK -> {
                    // Store Return Address
                    val nextPC = pc.get() + Hex(amode.byteAmount.toString(16), WORD_SIZE)
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
                    val nextPC = pc.get() + Hex(amode.byteAmount.toString(16), WORD_SIZE)
                    arch.getRegContainer().pc.set(nextPC)
                }

                else -> {
                    // Nothing to do for branches and jumps
                }
            }

        }

        private fun getOperand(arch: Architecture, amode: AModes, smallVal: Hex, bigVal: Hex): Hex? {
            val pc = arch.getRegContainer().pc
            val ac = arch.getRegByName("AC")
            val x = arch.getRegByName("X")
            val y = arch.getRegByName("Y")
            val sr = arch.getRegByName("SR")
            val sp = arch.getRegByName("SP")

            if (ac == null || x == null || y == null || sr == null || sp == null) return null

            return when (amode) {
                IMM -> {
                    smallVal
                }

                ZP -> {
                    arch.getMemory().load(Hex("00${smallVal.getRawHexStr()}", WORD_SIZE)).toHex()
                }

                ZP_X -> {
                    val addr = Hex("00${(smallVal + x.get()).toHex().getRawHexStr()}", WORD_SIZE)
                    arch.getMemory().load(addr.toHex()).toHex()
                }

                ZP_Y -> {
                    val addr = Hex("00${(smallVal + y.get()).toHex().getRawHexStr()}", WORD_SIZE)
                    arch.getMemory().load(addr.toHex()).toHex()
                }

                ABS -> {
                    arch.getMemory().load(bigVal).toHex()
                }

                ABS_X -> {
                    val addr = bigVal + x.get().toHex()
                    arch.getMemory().load(addr.toHex()).toHex()
                }

                ABS_Y -> {
                    val addr = bigVal + y.get().toHex()
                    arch.getMemory().load(addr.toHex()).toHex()
                }

                IND -> {
                    val loadedAddr = arch.getMemory().load(bigVal).toHex()
                    arch.getMemory().load(loadedAddr).toHex()
                }

                ZP_X_IND -> {
                    val addr = Hex("00${(smallVal + x.get()).toHex().getRawHexStr()}", WORD_SIZE)
                    val loadedAddr = arch.getMemory().load(addr.toHex()).toHex()
                    arch.getMemory().load(loadedAddr).toHex()
                }

                ZPIND_Y -> {
                    val loadedAddr = arch.getMemory().load(Hex("00${smallVal.getRawHexStr()}", WORD_SIZE))
                    val incAddr = loadedAddr + y.get()
                    arch.getMemory().load(incAddr.toHex()).toHex()
                }

                ACCUMULATOR -> {
                    ac.get().toHex()
                }

                REL -> {
                    (pc.get() + smallVal.toBin().getResized(WORD_SIZE)).toHex()
                }

                else -> null
            }
        }

        private fun getAddress(arch: Architecture, amode: AModes, smallVal: Hex, bigVal: Hex, x: Hex, y: Hex): Hex? {
            return when (amode) {
                ABS -> bigVal
                ABS_X -> (bigVal + x.toBin().getResized(WORD_SIZE)).toHex()
                ABS_Y -> (bigVal + y.toBin().getResized(WORD_SIZE)).toHex()
                ZP -> Hex("00${smallVal.getRawHexStr()}", WORD_SIZE)
                ZP_X -> Hex("00${(smallVal + x).toHex().getRawHexStr()}", WORD_SIZE)
                ZP_Y -> Hex("00${(smallVal + y).toHex().getRawHexStr()}", WORD_SIZE)
                ZP_X_IND -> arch.getMemory().load(Hex("00${(smallVal + x).toHex().getRawHexStr()}", WORD_SIZE), 2).toHex()
                ZPIND_Y -> (arch.getMemory().load(Hex("00${smallVal.getRawHexStr()}", WORD_SIZE), 2) + y).toHex()
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

    class T6502Instr(
        val type: InstrType,
        val addressingMode: AModes,
        parentLabel: ELabel?,
        nameToken: Compiler.Token,
        params: List<Compiler.Token>

    ) : EInstr(
        nameToken,
        params,
        parentLabel
    ) {
        fun getOpBin(arch: Architecture): Array<Hex> {
            val opCode = type.opCode[addressingMode]
            val addr = address
            val lblAddr = linkedLabels.firstOrNull()?.address
            if (opCode == null) {
                arch.getConsole().error("Couldn't resolve opcode for the following combination: ${type.name} and ${addressingMode.name}")
                return emptyArray()
            }

            val codeWithExt: Array<Hex> = when (addressingMode) {
                ZP_X, ZP_Y, ZPIND_Y, ZP_X_IND, ZP, IMM -> {
                    val const = this.constants.firstOrNull()
                    if (const == null) {
                        arch.getConsole().error("Missing imm for the following combination: ${type.name} and ${addressingMode.name}")
                        return emptyArray()
                    }
                    arrayOf(opCode, const.getValue(BYTE_SIZE).toHex())
                }

                ABS_X, ABS_Y, ABS, IND -> {
                    if (lblAddr != null) {
                        arrayOf(opCode, *lblAddr.toHex().splitToByteArray())
                    } else {
                        val const = this.constants.firstOrNull()
                        if (const == null) {
                            arch.getConsole().error("Missing imm for the following combination: ${type.name} and ${addressingMode.name}")
                            return emptyArray()
                        }
                        arrayOf(opCode, *const.getValue(WORD_SIZE).toHex().splitToByteArray())
                    }
                }

                ACCUMULATOR, IMPLIED -> arrayOf(opCode)

                REL -> {
                    if (lblAddr != null && addr != null) {
                        arrayOf(opCode, (addr.toHex() - lblAddr).toHex().getUResized(BYTE_SIZE))
                    } else {
                        val const = this.constants.firstOrNull()
                        if (const == null) {
                            arch.getConsole().error("Missing imm for the following combination: ${type.name} and ${addressingMode.name}")
                            return emptyArray()
                        }
                        arrayOf(opCode, const.getValue(BYTE_SIZE).toHex())
                    }
                }
            }
            return codeWithExt
        }
    }

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