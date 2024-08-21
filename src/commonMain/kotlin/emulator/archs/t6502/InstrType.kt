package emulator.archs.t6502

import cengine.util.integer.Bin
import cengine.util.integer.Hex
import cengine.util.integer.Size.Bit1
import cengine.util.integer.Size.Bit9
import emulator.archs.ArchT6502
import emulator.archs.t6502.AModes.*
import emulator.kit.assembler.InstrTypeInterface
import emulator.kit.memory.Memory

enum class InstrType(val opCode: Map<AModes, Hex>, val description: String) : InstrTypeInterface {
    // load, store, interregister transfer
    LDA(
        mapOf(
            ABS to Hex("AD", T6502.BYTE_SIZE),
            ABS_X to Hex("BD", T6502.BYTE_SIZE),
            ABS_Y to Hex("B9", T6502.BYTE_SIZE),
            IMM to Hex("A9", T6502.BYTE_SIZE),
            ZP to Hex("A5", T6502.BYTE_SIZE),
            ZP_X_IND to Hex("A1", T6502.BYTE_SIZE),
            ZP_X to Hex("B5", T6502.BYTE_SIZE),
            ZPIND_Y to Hex("B1", T6502.BYTE_SIZE)
        ), "load accumulator"
    ),
    LDX(mapOf(ABS to Hex("AE", T6502.BYTE_SIZE), ABS_Y to Hex("BE", T6502.BYTE_SIZE), IMM to Hex("A2", T6502.BYTE_SIZE), ZP to Hex("A6", T6502.BYTE_SIZE), ZP_Y to Hex("B6", T6502.BYTE_SIZE)), "load X"),
    LDY(mapOf(ABS to Hex("AC", T6502.BYTE_SIZE), ABS_X to Hex("BC", T6502.BYTE_SIZE), IMM to Hex("A0", T6502.BYTE_SIZE), ZP to Hex("A4", T6502.BYTE_SIZE), ZP_X to Hex("B4", T6502.BYTE_SIZE)), "load Y"),
    STA(
        mapOf(
            ABS to Hex("8D", T6502.BYTE_SIZE), ABS_X to Hex("9D", T6502.BYTE_SIZE), ABS_Y to Hex("99", T6502.BYTE_SIZE), ZP to Hex("85", T6502.BYTE_SIZE), ZP_X_IND to Hex(
                "81",
                T6502.BYTE_SIZE
            ), ZP_X to Hex("95", T6502.BYTE_SIZE), ZPIND_Y to Hex("91", T6502.BYTE_SIZE)
        ), "store accumulator"
    ),
    STX(mapOf(ABS to Hex("8E", T6502.BYTE_SIZE), ZP to Hex("86", T6502.BYTE_SIZE), ZP_Y to Hex("96", T6502.BYTE_SIZE)), "store X"),
    STY(mapOf(ABS to Hex("8C", T6502.BYTE_SIZE), ZP to Hex("84", T6502.BYTE_SIZE), ZP_X to Hex("94", T6502.BYTE_SIZE)), "store Y"),
    TAX(mapOf(IMPLIED to Hex("AA", T6502.BYTE_SIZE)), "transfer accumulator to X"),
    TAY(mapOf(IMPLIED to Hex("A8", T6502.BYTE_SIZE)), "transfer accumulator to Y"),
    TSX(mapOf(IMPLIED to Hex("BA", T6502.BYTE_SIZE)), "transfer stack pointer to X"),
    TXA(mapOf(IMPLIED to Hex("8A", T6502.BYTE_SIZE)), "transfer X to accumulator"),
    TXS(mapOf(IMPLIED to Hex("9A", T6502.BYTE_SIZE)), "transfer X to stack pointer"),
    TYA(mapOf(IMPLIED to Hex("98", T6502.BYTE_SIZE)), "transfer Y to accumulator"),

    // stack
    PHA(mapOf(IMPLIED to Hex("48", T6502.BYTE_SIZE)), "push accumulator"),
    PHP(mapOf(IMPLIED to Hex("08", T6502.BYTE_SIZE)), "push processor status (SR)"),
    PLA(mapOf(IMPLIED to Hex("68", T6502.BYTE_SIZE)), "pull accumulator"),
    PLP(mapOf(IMPLIED to Hex("28", T6502.BYTE_SIZE)), "pull processor status (SR)"),

    // decrements, increments
    DEC(mapOf(ABS to Hex("CE", T6502.BYTE_SIZE), ABS_X to Hex("DE", T6502.BYTE_SIZE), ZP to Hex("C6", T6502.BYTE_SIZE), ZP_X to Hex("D6", T6502.BYTE_SIZE)), "decrement"),
    DEX(mapOf(IMPLIED to Hex("CA", T6502.BYTE_SIZE)), "decrement X"),
    DEY(mapOf(IMPLIED to Hex("88", T6502.BYTE_SIZE)), "decrement Y"),
    INC(mapOf(ABS to Hex("EE", T6502.BYTE_SIZE), ABS_X to Hex("FE", T6502.BYTE_SIZE), ZP to Hex("E6", T6502.BYTE_SIZE), ZP_X to Hex("F6", T6502.BYTE_SIZE)), "increment"),
    INX(mapOf(IMPLIED to Hex("E8", T6502.BYTE_SIZE)), "increment X"),
    INY(mapOf(IMPLIED to Hex("C8", T6502.BYTE_SIZE)), "increment Y"),

    // arithmetic operations
    /**
     *
     */
    ADC(
        mapOf(
            ABS to Hex("6D", T6502.BYTE_SIZE), ABS_X to Hex("7D", T6502.BYTE_SIZE), ABS_Y to Hex("79", T6502.BYTE_SIZE), IMM to Hex("69", T6502.BYTE_SIZE), ZP to Hex(
                "65",
                T6502.BYTE_SIZE
            ), ZP_X_IND to Hex("61", T6502.BYTE_SIZE), ZP_X to Hex("75", T6502.BYTE_SIZE), ZPIND_Y to Hex("71", T6502.BYTE_SIZE)
        ), "add with carry"
    ),

    /**
     *
     */
    SBC(
        mapOf(
            ABS to Hex("ED", T6502.BYTE_SIZE), ABS_X to Hex("FD", T6502.BYTE_SIZE), ABS_Y to Hex("F9", T6502.BYTE_SIZE), IMM to Hex("E9", T6502.BYTE_SIZE), ZP to Hex(
                "E5",
                T6502.BYTE_SIZE
            ), ZP_X_IND to Hex("E1", T6502.BYTE_SIZE), ZP_X to Hex("F5", T6502.BYTE_SIZE), ZPIND_Y to Hex("F1", T6502.BYTE_SIZE)
        ), "subtract with carry"
    ),

    // logical operations
    AND(
        mapOf(
            ABS to Hex("2D", T6502.BYTE_SIZE), ABS_X to Hex("3D", T6502.BYTE_SIZE), ABS_Y to Hex("39", T6502.BYTE_SIZE), IMM to Hex("29", T6502.BYTE_SIZE), ZP to Hex(
                "25",
                T6502.BYTE_SIZE
            ), ZP_X_IND to Hex("21", T6502.BYTE_SIZE), ZP_X to Hex("35", T6502.BYTE_SIZE), ZPIND_Y to Hex("31", T6502.BYTE_SIZE)
        ), "and (with accumulator)"
    ),
    EOR(
        mapOf(
            ABS to Hex("0D", T6502.BYTE_SIZE), ABS_X to Hex("1D", T6502.BYTE_SIZE), ABS_Y to Hex("19", T6502.BYTE_SIZE), IMM to Hex("09", T6502.BYTE_SIZE), ZP to Hex(
                "05",
                T6502.BYTE_SIZE
            ), ZP_X_IND to Hex("01", T6502.BYTE_SIZE), ZP_X to Hex("15", T6502.BYTE_SIZE), ZPIND_Y to Hex("11", T6502.BYTE_SIZE)
        ), "exclusive or (with accumulator)"
    ),
    ORA(
        mapOf(
            ABS to Hex("4D", T6502.BYTE_SIZE), ABS_X to Hex("5D", T6502.BYTE_SIZE), ABS_Y to Hex("59", T6502.BYTE_SIZE), IMM to Hex("49", T6502.BYTE_SIZE), ZP to Hex(
                "45",
                T6502.BYTE_SIZE
            ), ZP_X_IND to Hex("41", T6502.BYTE_SIZE), ZP_X to Hex("55", T6502.BYTE_SIZE), ZPIND_Y to Hex("51", T6502.BYTE_SIZE)
        ), "or (with accumulator)"
    ),

    // shift & rotate
    ASL(mapOf(ABS to Hex("0E", T6502.BYTE_SIZE), ABS_X to Hex("1E", T6502.BYTE_SIZE), ACCUMULATOR to Hex("0A", T6502.BYTE_SIZE), ZP to Hex("06", T6502.BYTE_SIZE), ZP_X to Hex("16", T6502.BYTE_SIZE)), "arithmetic shift left"),
    LSR(mapOf(ABS to Hex("4E", T6502.BYTE_SIZE), ABS_X to Hex("5E", T6502.BYTE_SIZE), ACCUMULATOR to Hex("4A", T6502.BYTE_SIZE), ZP to Hex("46", T6502.BYTE_SIZE), ZP_X to Hex("56", T6502.BYTE_SIZE)), "logical shift right"),
    ROL(mapOf(ABS to Hex("2E", T6502.BYTE_SIZE), ABS_X to Hex("3E", T6502.BYTE_SIZE), ACCUMULATOR to Hex("2A", T6502.BYTE_SIZE), ZP to Hex("26", T6502.BYTE_SIZE), ZP_X to Hex("36", T6502.BYTE_SIZE)), "rotate left"),
    ROR(mapOf(ABS to Hex("6E", T6502.BYTE_SIZE), ABS_X to Hex("7E", T6502.BYTE_SIZE), ACCUMULATOR to Hex("6A", T6502.BYTE_SIZE), ZP to Hex("66", T6502.BYTE_SIZE), ZP_X to Hex("76", T6502.BYTE_SIZE)), "rotate right"),

    // flag
    CLC(mapOf(IMPLIED to Hex("18", T6502.BYTE_SIZE)), "clear carry"),
    CLD(mapOf(IMPLIED to Hex("D8", T6502.BYTE_SIZE)), "clear decimal"),
    CLI(mapOf(IMPLIED to Hex("58", T6502.BYTE_SIZE)), "clear interrupt disable"),
    CLV(mapOf(IMPLIED to Hex("B8", T6502.BYTE_SIZE)), "clear overflow"),
    SEC(mapOf(IMPLIED to Hex("38", T6502.BYTE_SIZE)), "set carry"),
    SED(mapOf(IMPLIED to Hex("F8", T6502.BYTE_SIZE)), "set decimal"),
    SEI(mapOf(IMPLIED to Hex("78", T6502.BYTE_SIZE)), "set interrupt disable"),

    // comparison
    CMP(
        mapOf(
            ABS to Hex("CD", T6502.BYTE_SIZE), ABS_X to Hex("DD", T6502.BYTE_SIZE), ABS_Y to Hex("D9", T6502.BYTE_SIZE), IMM to Hex("C9", T6502.BYTE_SIZE), ZP to Hex(
                "C5",
                T6502.BYTE_SIZE
            ), ZP_X_IND to Hex("C1", T6502.BYTE_SIZE), ZP_X to Hex("D5", T6502.BYTE_SIZE), ZPIND_Y to Hex("D1", T6502.BYTE_SIZE)
        ), "compare (with accumulator)"
    ),
    CPX(mapOf(ABS to Hex("EC", T6502.BYTE_SIZE), IMM to Hex("E0", T6502.BYTE_SIZE), ZP to Hex("E4", T6502.BYTE_SIZE)), "compare with X"),
    CPY(mapOf(ABS to Hex("CC", T6502.BYTE_SIZE), IMM to Hex("C0", T6502.BYTE_SIZE), ZP to Hex("C4", T6502.BYTE_SIZE)), "compare with Y"),

    // conditional branches
    BCC(mapOf(REL to Hex("90", T6502.BYTE_SIZE)), "branch on carry clear"),
    BCS(mapOf(REL to Hex("B0", T6502.BYTE_SIZE)), "branch on carry set"),
    BEQ(mapOf(REL to Hex("F0", T6502.BYTE_SIZE)), "branch on equal (zero set)"),
    BMI(mapOf(REL to Hex("30", T6502.BYTE_SIZE)), "branch on minus (negative set)"),
    BNE(mapOf(REL to Hex("D0", T6502.BYTE_SIZE)), "branch on not equal (zero clear)"),
    BPL(mapOf(REL to Hex("10", T6502.BYTE_SIZE)), "branch on plus (negative clear)"),
    BVC(mapOf(REL to Hex("50", T6502.BYTE_SIZE)), "branch on overflow clear"),
    BVS(mapOf(REL to Hex("70", T6502.BYTE_SIZE)), "branch on overflow set"),

    // jumps, subroutines
    JMP(mapOf(ABS to Hex("4C", T6502.BYTE_SIZE), IND to Hex("6C", T6502.BYTE_SIZE)), "jump"),
    JSR(mapOf(ABS to Hex("20", T6502.BYTE_SIZE)), "jump subroutine"),
    RTS(mapOf(IMPLIED to Hex("60", T6502.BYTE_SIZE)), "return from subroutine"),

    // interrupts
    BRK(mapOf(IMPLIED to Hex("00", T6502.BYTE_SIZE)), "break / interrupt"),
    RTI(mapOf(IMPLIED to Hex("40", T6502.BYTE_SIZE)), "return from interrupt"),

    // other
    BIT(mapOf(ABS to Hex("2C", T6502.BYTE_SIZE), IMM to Hex("89", T6502.BYTE_SIZE), ZP to Hex("24", T6502.BYTE_SIZE)), "bit test"),
    NOP(mapOf(IMPLIED to Hex("EA", T6502.BYTE_SIZE)), "no operation");

    override fun getDetectionName(): String = this.name

    fun execute(arch: ArchT6502, amode: AModes, threeBytes: Array<Bin>, tracker: Memory.AccessTracker) {
        val smallVal = threeBytes.drop(1).first().toHex()
        val bigVal = Hex(threeBytes.drop(1).joinToString("") { it.toHex().toRawString() }, T6502.WORD_SIZE)

        val flags = this.getFlags(arch)
        val pc = arch.regContainer.pc
        val ac = arch.getRegByName("AC")
        val x = arch.getRegByName("X")
        val y = arch.getRegByName("Y")
        val sr = arch.getRegByName("SR")
        val sp = arch.getRegByName("SP")

        if (ac == null || x == null || y == null || flags == null || sp == null || sr == null) {
            arch.console.error("Register missing!")
            return
        }
        val operand = this.getOperand(arch, amode, smallVal, bigVal)
        val address = this.getAddress(arch, amode, smallVal, bigVal, x.get().toHex(), y.get().toHex())

        // EXECUTION TYPE SPECIFIC
        when (this) {
            LDA -> {
                if (operand == null) {
                    arch.console.error("Couldn't load operand for ${this.name} ${amode.name}!")
                    return
                }
                ac.set(operand)
                setFlags(arch, n = ac.get().toBin().getBit(0), checkZero = ac.get().toBin())
            }

            LDX -> {
                if (operand == null) {
                    arch.console.error("Couldn't load operand for ${this.name} ${amode.name}!")
                    return
                }
                x.set(operand)
                setFlags(arch, n = x.get().toBin().getBit(0), checkZero = x.get().toBin())
            }

            LDY -> {
                if (operand == null) {
                    arch.console.error("Couldn't load operand for ${this.name} ${amode.name}!")
                    return
                }
                y.set(operand)
                setFlags(arch, n = y.get().toBin().getBit(0), checkZero = y.get().toBin())
            }

            STA -> address?.let { arch.dataMemory.store(it, ac.get(), tracker = tracker) } ?: {
                arch.console.error("Couldn't load address for ${this.name} ${amode.name}!")
            }

            STX -> address?.let { arch.dataMemory.store(it, x.get(), tracker = tracker) } ?: {
                arch.console.error("Couldn't load address for ${this.name} ${amode.name}!")
            }

            STY -> address?.let { arch.dataMemory.store(it, y.get(), tracker = tracker) } ?: {
                arch.console.error("Couldn't load address for ${this.name} ${amode.name}!")
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
                arch.dataMemory.store(sp.get().toBin().getUResized(T6502.WORD_SIZE).toHex(), ac.get(), tracker = tracker)
                sp.set(sp.get().toBin() - Bin("1", T6502.BYTE_SIZE))
            }

            PHP -> {
                arch.dataMemory.store(sp.get().toBin().getUResized(T6502.WORD_SIZE).toHex(), sr.get(), tracker = tracker)
                sp.set(sp.get().toBin() - Bin("1", T6502.BYTE_SIZE))
            }

            PLA -> {
                sp.set(sp.get().toBin() + Bin("1", T6502.BYTE_SIZE))
                val loaded = arch.dataMemory.load(sp.get().toHex().getUResized(T6502.WORD_SIZE), tracker = tracker).toBin()
                ac.set(loaded)
                setFlags(arch, n = loaded.getBit(0), checkZero = loaded)
            }

            PLP -> {
                sp.set(sp.get().toBin() + Bin("1", T6502.BYTE_SIZE))
                val loaded = arch.dataMemory.load(sp.get().toHex().getUResized(T6502.WORD_SIZE), tracker = tracker).toBin()
                setFlags(arch, n = loaded.getBit(0), v = loaded.getBit(1), d = loaded.getBit(4), i = loaded.getBit(5), z = loaded.getBit(6), c = loaded.getBit(7))
            }

            DEC -> {
                address?.let {
                    val dec = arch.dataMemory.load(it, tracker = tracker) - Bin("1", T6502.BYTE_SIZE)
                    arch.dataMemory.store(it, dec, tracker = tracker)
                    setFlags(arch, n = dec.toBin().getBit(0), checkZero = dec.toBin())
                } ?: {
                    arch.console.error("Couldn't load address for ${this.name} ${amode.name}!")
                }
            }

            DEX -> {
                val dec = x.get() - Bin("1", T6502.BYTE_SIZE)
                x.set(dec)
                setFlags(arch, n = dec.toBin().getBit(0), checkZero = dec.toBin())
            }

            DEY -> {
                val dec = y.get() - Bin("1", T6502.BYTE_SIZE)
                y.set(dec)
                setFlags(arch, n = dec.toBin().getBit(0), checkZero = dec.toBin())
            }

            INC -> {
                address?.let {
                    val inc = arch.dataMemory.load(it, tracker = tracker) + Bin("1", T6502.BYTE_SIZE)
                    arch.dataMemory.store(it, inc, tracker = tracker)
                    setFlags(arch, n = inc.toBin().getBit(0), checkZero = inc.toBin())
                } ?: {
                    arch.console.error("Couldn't load address for ${this.name} ${amode.name}!")
                }
            }

            INX -> {
                val inc = x.get() + Bin("1", T6502.BYTE_SIZE)
                x.set(inc)
                setFlags(arch, n = inc.toBin().getBit(0), checkZero = inc.toBin())
            }

            INY -> {
                val inc = y.get() + Bin("1", T6502.BYTE_SIZE)
                y.set(inc)
                setFlags(arch, n = inc.toBin().getBit(0), checkZero = inc.toBin())
            }

            ADC -> {
                if (operand == null) {
                    arch.console.error("Couldn't load operand for ${this.name} ${amode.name}!")
                    return
                }

                // Binary Add
                val binSumBit9 = ac.get().toBin().getUResized(Bit9) + operand + flags.c
                val sum = Bin(binSumBit9.toBin().toRawString().substring(1), T6502.BYTE_SIZE)
                val acBit0 = ac.get().toBin().getBit(0) ?: Bin("0", Bit1)
                val operandBit0 = operand.toBin().getBit(0)
                val sumBit0 = sum.getBit(0) ?: Bin("0", Bit1)
                val v = if (acBit0 == operandBit0) sumBit0 xor acBit0 else Bin("0", Bit1)
                val c = binSumBit9.toBin().getBit(0)
                setFlags(arch, v = v, c = c)

                ac.set(sum)
                setFlags(arch, checkZero = sum, n = sum.getBit(0))
            }

            SBC -> {
                if (operand == null) {
                    arch.console.error("Couldn't load operand for ${this.name} ${amode.name}!")
                    return
                }

                // Binary SBC
                val opInv = operand.toBin().inv()

                val binSumBit9 = ac.get().toBin() + opInv + flags.c
                val subBit8 = Bin(binSumBit9.toBin().toRawString().substring(1), T6502.BYTE_SIZE)
                val acBit0 = ac.get().toBin().getBit(0) ?: Bin("0", Bit1)
                val opInvBit0 = opInv.toBin().getBit(0)
                val subBit0 = subBit8.getBit(0) ?: Bin("0", Bit1)
                val v = if (acBit0 == opInvBit0) subBit0 xor acBit0 else Bin("0", Bit1)
                val c = binSumBit9.toBin().getBit(0)
                setFlags(arch, v = v, c = c)

                ac.set(subBit8)
                setFlags(arch, checkZero = subBit8, n = subBit8.getBit(0))
            }

            AND -> {
                if (operand == null) {
                    arch.console.error("Couldn't load operand for ${this.name} ${amode.name}!")
                    return
                }

                val and = ac.get().toBin() and operand.toBin()
                ac.set(and)
                setFlags(arch, n = and.getBit(0), checkZero = and)
            }

            EOR -> {
                if (operand == null) {
                    arch.console.error("Couldn't load operand for ${this.name} ${amode.name}!")
                    return
                }

                val xor = ac.get().toBin() xor operand.toBin()
                ac.set(xor)
                setFlags(arch, n = xor.getBit(0), checkZero = xor)
            }

            ORA -> {
                if (operand == null) {
                    arch.console.error("Couldn't load operand for ${this.name} ${amode.name}!")
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
                            val memBin = arch.dataMemory.load(it, tracker = tracker).toBin()
                            val shiftRes = memBin shl 1
                            arch.dataMemory.store(it, shiftRes, tracker = tracker)
                            setFlags(arch, c = memBin.getBit(0), checkZero = shiftRes, n = shiftRes.getBit(0))
                        } ?: arch.console.error("Couldn't load address for ${this.name} ${amode.name}!")
                    }
                }
            }

            LSR -> {
                when (amode) {
                    ACCUMULATOR -> {
                        val shifted = ac.get().toBin().ushr(1)
                        setFlags(arch, n = Bin("0", Bit1), checkZero = shifted, c = ac.get().toBin().getBit(7))
                        ac.set(shifted)
                    }

                    else -> {
                        address?.let {
                            val loaded = arch.dataMemory.load(it, tracker = tracker).toBin()
                            val shifted = loaded ushr 1
                            setFlags(arch, n = Bin("0", Bit1), checkZero = shifted, c = loaded.getBit(7))
                            arch.dataMemory.store(it, shifted, tracker = tracker)
                        } ?: arch.console.error("Couldn't load address for ${this.name} ${amode.name}!")
                    }
                }
            }

            ROL -> {
                when (amode) {
                    ACCUMULATOR -> {
                        val acBin = ac.get().toBin()
                        val rotated = acBin.ushl(1) and flags.c.getUResized(T6502.BYTE_SIZE)
                        setFlags(arch, n = rotated.getBit(0), checkZero = rotated, c = acBin.getBit(0))
                        ac.set(rotated)
                    }

                    else -> {
                        address?.let {
                            val loaded = arch.dataMemory.load(it, tracker = tracker).toBin()
                            val rotated = loaded.ushl(1) and flags.c.getUResized(T6502.BYTE_SIZE)
                            setFlags(arch, n = rotated.getBit(0), checkZero = rotated, c = loaded.getBit(0))
                            arch.dataMemory.store(it, rotated, tracker = tracker)
                        } ?: arch.console.error("Couldn't load address for ${this.name} ${amode.name}!")
                    }
                }
            }

            ROR -> {
                when (amode) {
                    ACCUMULATOR -> {
                        val acBin = ac.get().toBin()
                        val rotated = acBin.ushr(1) and Bin(flags.c.toRawString() + "0000000", T6502.BYTE_SIZE)
                        setFlags(arch, n = rotated.getBit(0), checkZero = rotated, c = acBin.getBit(7))
                        ac.set(rotated)
                    }

                    else -> {
                        address?.let {
                            val loaded = arch.dataMemory.load(it, tracker = tracker).toBin()
                            val rotated = loaded.ushr(1) and Bin(flags.c.toRawString() + "0000000", T6502.BYTE_SIZE)
                            setFlags(arch, n = rotated.getBit(0), checkZero = rotated, c = loaded.getBit(7))
                            arch.dataMemory.store(it, rotated, tracker = tracker)
                        } ?: arch.console.error("Couldn't load address for ${this.name} ${amode.name}!")
                    }
                }
            }

            CLC -> setFlags(arch, c = Bin("0", Bit1))
            CLD -> setFlags(arch, d = Bin("0", Bit1))
            CLI -> setFlags(arch, i = Bin("0", Bit1))
            CLV -> setFlags(arch, v = Bin("0", Bit1))

            SEC -> setFlags(arch, c = Bin("1", Bit1))
            SED -> setFlags(arch, d = Bin("1", Bit1))
            SEI -> setFlags(arch, i = Bin("1", Bit1))

            CMP -> {
                if (operand == null) {
                    arch.console.error("Couldn't load operand for ${this.name} ${amode.name}!")
                    return
                }

                val cmpResult = (ac.get().toBin() - operand).toBin()
                val c = if (operand.toDec() <= ac.get().toDec()) Bin("1", Bit1) else Bin("0", Bit1)
                setFlags(arch, checkZero = cmpResult, n = cmpResult.getBit(0), c = c)
            }

            CPX -> {
                if (operand == null) {
                    arch.console.error("Couldn't load operand for ${this.name} ${amode.name}!")
                    return
                }

                val cmpResult = (x.get().toBin() - operand).toBin()
                val c = if (operand.toDec() <= x.get().toDec()) Bin("1", Bit1) else Bin("0", Bit1)
                setFlags(arch, checkZero = cmpResult, n = cmpResult.getBit(0), c = c)
            }

            CPY -> {
                if (operand == null) {
                    arch.console.error("Couldn't load operand for ${this.name} ${amode.name}!")
                    return
                }

                val cmpResult = (y.get().toBin() - operand).toBin()
                val c = if (operand.toDec() <= y.get().toDec()) Bin("1", Bit1) else Bin("0", Bit1)
                setFlags(arch, checkZero = cmpResult, n = cmpResult.getBit(0), c = c)
            }

            BCC -> {
                if (operand == null) {
                    arch.console.error("Couldn't load operand for ${this.name} ${amode.name}!")
                    return
                }
                if (flags.c == Bin("0", Bit1)) {
                    // Carry Clear
                    pc.set(operand)
                } else {
                    // Carry not clear
                    val nextPC = pc.get() + Hex(amode.byteAmount.toString(16), T6502.WORD_SIZE)
                    arch.regContainer.pc.set(nextPC)
                }
            }

            BCS -> {
                if (operand == null) {
                    arch.console.error("Couldn't load operand for ${this.name} ${amode.name}!")
                    return
                }
                if (flags.c == Bin("1", Bit1)) {
                    // Carry Set
                    pc.set(operand)
                } else {
                    // Carry not set
                    val nextPC = pc.get() + Hex(amode.byteAmount.toString(16), T6502.WORD_SIZE)
                    arch.regContainer.pc.set(nextPC)
                }
            }

            BEQ -> {
                if (operand == null) {
                    arch.console.error("Couldn't load operand for ${this.name} ${amode.name}!")
                    return
                }
                if (flags.z == Bin("1", Bit1)) {
                    // Zero
                    pc.set(operand)
                } else {
                    // not zero
                    val nextPC = pc.get() + Hex(amode.byteAmount.toString(16), T6502.WORD_SIZE)
                    arch.regContainer.pc.set(nextPC)
                }
            }

            BMI -> {
                if (operand == null) {
                    arch.console.error("Couldn't load operand for ${this.name} ${amode.name}!")
                    return
                }
                if (flags.n == Bin("1", Bit1)) {
                    // negative
                    pc.set(operand)
                } else {
                    // not negative
                    val nextPC = pc.get() + Hex(amode.byteAmount.toString(16), T6502.WORD_SIZE)
                    arch.regContainer.pc.set(nextPC)
                }
            }

            BNE -> {
                if (operand == null) {
                    arch.console.error("Couldn't load operand for ${this.name} ${amode.name}!")
                    return
                }
                if (flags.z == Bin("0", Bit1)) {
                    // Not Zero
                    pc.set(operand)
                } else {
                    // zero
                    val nextPC = pc.get() + Hex(amode.byteAmount.toString(16), T6502.WORD_SIZE)
                    arch.regContainer.pc.set(nextPC)
                }
            }

            BPL -> {
                if (operand == null) {
                    arch.console.error("Couldn't load operand for ${this.name} ${amode.name}!")
                    return
                }
                if (flags.n == Bin("0", Bit1)) {
                    // positive
                    pc.set(operand)
                } else {
                    // not positive
                    val nextPC = pc.get() + Hex(amode.byteAmount.toString(16), T6502.WORD_SIZE)
                    arch.regContainer.pc.set(nextPC)
                }
            }

            BVC -> {
                if (operand == null) {
                    arch.console.error("Couldn't load operand for ${this.name} ${amode.name}!")
                    return
                }
                if (flags.v == Bin("0", Bit1)) {
                    // overflow clear
                    pc.set(operand)
                } else {
                    // overflow set
                    val nextPC = pc.get() + Hex(amode.byteAmount.toString(16), T6502.WORD_SIZE)
                    arch.regContainer.pc.set(nextPC)
                }
            }

            BVS -> {
                if (operand == null) {
                    arch.console.error("Couldn't load operand for ${this.name} ${amode.name}!")
                    return
                }
                if (flags.v == Bin("1", Bit1)) {
                    // overflow set
                    pc.set(operand)
                } else {
                    // overflow clear
                    val nextPC = pc.get() + Hex(amode.byteAmount.toString(16), T6502.WORD_SIZE)
                    arch.regContainer.pc.set(nextPC)
                }
            }

            JMP -> {
                when (amode) {
                    ABS -> {
                        pc.set(bigVal)
                    }

                    IND -> {
                        pc.set(Bin(pc.get().toBin().toRawString().substring(0, 8) + arch.dataMemory.load(bigVal, tracker = tracker).toBin().toRawString(), T6502.WORD_SIZE))
                    }

                    else -> {}
                }
            }

            JSR -> {
                val nextPC = pc.get() + Hex(amode.byteAmount.toString(16), T6502.WORD_SIZE)
                arch.dataMemory.store((sp.get().toBin().getUResized(T6502.WORD_SIZE) - Bin("1", T6502.WORD_SIZE)).toHex(), nextPC, tracker = tracker)
                sp.set(sp.get().toBin() - Bin("10", T6502.BYTE_SIZE))
                pc.set(bigVal)
            }

            RTS -> {
                val loadedPC = arch.dataMemory.load((sp.get().toBin().getUResized(T6502.WORD_SIZE) + Bin("1", T6502.WORD_SIZE)).toHex(), 2, tracker = tracker)
                sp.set(sp.get().toBin() + Bin("10", T6502.BYTE_SIZE))
                pc.set(loadedPC)
            }

            BRK -> {
                // Store Return Address
                val nextPC = pc.get() + Hex(amode.byteAmount.toString(16), T6502.WORD_SIZE)
                arch.dataMemory.store((sp.get().toBin().getUResized(T6502.WORD_SIZE) - Bin("1", T6502.WORD_SIZE)).toHex(), nextPC, tracker = tracker)
                sp.set(sp.get().toBin() - Bin("10", T6502.BYTE_SIZE))

                // Store Status Register
                arch.dataMemory.store((sp.get().toBin().getUResized(T6502.WORD_SIZE)).toHex(), sr.get(), tracker = tracker)
                sp.set(sp.get().toBin() - Bin("1", T6502.BYTE_SIZE))

                // Load Interrupt Vendor
                val lsb = arch.dataMemory.load(Hex("FFFE", T6502.WORD_SIZE), tracker = tracker).toHex()
                val msb = arch.dataMemory.load(Hex("FFFF", T6502.WORD_SIZE), tracker = tracker).toHex()

                // Jump to Interrupt Handler Address
                pc.set(Hex(msb.toRawString() + lsb.toRawString(), T6502.WORD_SIZE))
            }

            RTI -> {
                // Load Status Register
                sp.set(sp.get().toBin() + Bin("1", T6502.BYTE_SIZE))
                val loadedSR = arch.dataMemory.load(sp.get().toHex().getUResized(T6502.WORD_SIZE), tracker = tracker).toBin()
                setFlags(arch, n = loadedSR.getBit(0), v = loadedSR.getBit(1), d = loadedSR.getBit(4), i = loadedSR.getBit(5), z = loadedSR.getBit(6), c = loadedSR.getBit(7))

                // Load Return Address
                sp.set(sp.get().toBin() + Bin("10", T6502.BYTE_SIZE))
                val retAddr = arch.dataMemory.load((sp.get().toHex().getUResized(T6502.WORD_SIZE) - Hex("1", T6502.WORD_SIZE)).toHex(), 2, tracker = tracker)

                // Jump To Return Address
                pc.set(retAddr)
            }

            BIT -> {
                if (operand == null) {
                    arch.console.error("Couldn't load operand for ${this.name} ${amode.name}!")
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
                val nextPC = pc.get() + Hex(amode.byteAmount.toString(16), T6502.WORD_SIZE)
                arch.regContainer.pc.set(nextPC)
            }

            else -> {
                // Nothing to do for branches and jumps
            }
        }

    }

    private fun getOperand(arch: emulator.kit.Architecture, amode: AModes, smallVal: Hex, bigVal: Hex): Hex? {
        if (arch !is ArchT6502) return null

        val pc = arch.regContainer.pc
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
                arch.dataMemory.load(Hex("00${smallVal.toRawString()}", T6502.WORD_SIZE)).toHex()
            }

            ZP_X -> {
                val addr = Hex("00${(smallVal + x.get()).toHex().toRawString()}", T6502.WORD_SIZE)
                arch.dataMemory.load(addr.toHex()).toHex()
            }

            ZP_Y -> {
                val addr = Hex("00${(smallVal + y.get()).toHex().toRawString()}", T6502.WORD_SIZE)
                arch.dataMemory.load(addr.toHex()).toHex()
            }

            ABS -> {
                arch.dataMemory.load(bigVal).toHex()
            }

            ABS_X -> {
                val addr = bigVal + x.get().toHex()
                arch.dataMemory.load(addr.toHex()).toHex()
            }

            ABS_Y -> {
                val addr = bigVal + y.get().toHex()
                arch.dataMemory.load(addr.toHex()).toHex()
            }

            IND -> {
                val loadedAddr = arch.dataMemory.load(bigVal).toHex()
                arch.dataMemory.load(loadedAddr).toHex()
            }

            ZP_X_IND -> {
                val addr = Hex("00${(smallVal + x.get()).toHex().toRawString()}", T6502.WORD_SIZE)
                val loadedAddr = arch.dataMemory.load(addr.toHex()).toHex()
                arch.dataMemory.load(loadedAddr).toHex()
            }

            ZPIND_Y -> {
                val loadedAddr = arch.dataMemory.load(Hex("00${smallVal.toRawString()}", T6502.WORD_SIZE))
                val incAddr = loadedAddr + y.get()
                arch.dataMemory.load(incAddr.toHex()).toHex()
            }

            ACCUMULATOR -> {
                ac.get().toHex()
            }

            REL -> {
                (pc.get() + smallVal.toBin().getResized(T6502.WORD_SIZE)).toHex()
            }

            else -> null
        }
    }

    private fun getAddress(arch: ArchT6502, amode: AModes, smallVal: Hex, bigVal: Hex, x: Hex, y: Hex): Hex? {
        return when (amode) {
            ABS -> bigVal
            ABS_X -> (bigVal + x.toBin().getResized(T6502.WORD_SIZE)).toHex()
            ABS_Y -> (bigVal + y.toBin().getResized(T6502.WORD_SIZE)).toHex()
            ZP -> Hex("00${smallVal.toRawString()}", T6502.WORD_SIZE)
            ZP_X -> Hex("00${(smallVal + x).toHex().toRawString()}", T6502.WORD_SIZE)
            ZP_Y -> Hex("00${(smallVal + y).toHex().toRawString()}", T6502.WORD_SIZE)
            ZP_X_IND -> arch.dataMemory.load(Hex("00${(smallVal + x).toHex().toRawString()}", T6502.WORD_SIZE), 2).toHex()
            ZPIND_Y -> (arch.dataMemory.load(Hex("00${smallVal.toRawString()}", T6502.WORD_SIZE), 2) + y).toHex()
            else -> null
        }
    }

    private fun getFlags(arch: ArchT6502): T6502Assembler.Flags? {
        val sr = arch.getRegByName("SR") ?: return null
        val nflag = sr.get().toBin().getBit(0) ?: return null
        val vflag = sr.get().toBin().getBit(1) ?: return null
        val bflag = sr.get().toBin().getBit(3) ?: return null
        val dflag = sr.get().toBin().getBit(4) ?: return null
        val iflag = sr.get().toBin().getBit(5) ?: return null
        val zflag = sr.get().toBin().getBit(6) ?: return null
        val cflag = sr.get().toBin().getBit(7) ?: return null
        return T6502Assembler.Flags(nflag, vflag, zflag, cflag, dflag, bflag, iflag)
    }

    private fun setFlags(arch: ArchT6502, n: Bin? = null, v: Bin? = null, b: Bin? = null, d: Bin? = null, i: Bin? = null, z: Bin? = null, c: Bin? = null, checkZero: Bin? = null, seti: Boolean? = null, setd: Boolean? = null, setb: Boolean? = null) {
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
            zflag = if (checkZero == Bin("0", T6502.BYTE_SIZE)) {
                Bin("1", Bit1)
            } else {
                Bin("0", Bit1)
            }
        }
        if (seti != null) {
            iflag = if (seti) Bin("1", Bit1) else Bin("0", Bit1)
        }
        if (setd != null) {
            dflag = if (setd) Bin("1", Bit1) else Bin("0", Bit1)
        }
        if (setb != null) {
            bflag = if (setb) Bin("1", Bit1) else Bin("0", Bit1)
        }


        sr.set(Bin("${nflag.toRawString()}${vflag.toRawString()}1${bflag.toRawString()}${dflag.toRawString()}${iflag.toRawString()}${zflag.toRawString()}${cflag.toRawString()}", T6502.BYTE_SIZE))
    }
}