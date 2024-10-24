package emulator.archs.ikrmini

import cengine.util.integer.Size
import emulator.archs.ArchIKRMini
import emulator.archs.ikrmini.IKRMini.WORDSIZE
import emulator.kit.assembler.InstrTypeInterface
import emulator.kit.assembler.syntax.Rule
import emulator.kit.assembler.syntax.Component.*
import emulator.kit.assembler.gas.GASNodeType
import emulator.kit.memory.Memory
import cengine.util.integer.Bin
import cengine.util.integer.Hex

class IKRMiniSyntax {
    enum class ParamType(val tokenSeq: Rule?, val wordAmount: Int, val exampleString: String) {
        INDIRECT(Rule { Seq(Specific("("), Specific("("), SpecNode(GASNodeType.INT_EXPR), Specific(")"), Specific(")")) }, 2, "(([16 Bit]))"),
        INDIRECT_WITH_OFFSET(Rule { Seq(Specific("("), SpecNode(GASNodeType.INT_EXPR), Specific(","), Specific("("), SpecNode(GASNodeType.INT_EXPR), Specific(")"), Specific(")")) }, 3, "([16 Bit],([16 Bit]))"),
        DIRECT(Rule { Seq(Specific("("), SpecNode(GASNodeType.INT_EXPR), Specific(")")) }, 2, "([16 Bit])"),
        IMMEDIATE(Rule { Seq(Specific("#"), SpecNode(GASNodeType.INT_EXPR)) }, 2, "#[16 Bit]"),
        DESTINATION(Rule { Seq(SpecNode(GASNodeType.INT_EXPR)) }, 2, "[label]"),
        IMPLIED(null, 1, ""),
    }

    enum class InstrType(val paramMap: Map<ParamType, Hex>, val descr: String) : InstrTypeInterface {
        // Data Transport
        LOAD(mapOf(ParamType.IMMEDIATE to Hex("010C", WORDSIZE), ParamType.DIRECT to Hex("020C", WORDSIZE), ParamType.INDIRECT to Hex("030C", WORDSIZE), ParamType.INDIRECT_WITH_OFFSET to Hex("040C", WORDSIZE)), "load AC"),
        LOADI(mapOf(ParamType.IMPLIED to Hex("200C", WORDSIZE)), "load indirect"),
        STORE(mapOf(ParamType.DIRECT to Hex("3200", WORDSIZE), ParamType.INDIRECT to Hex("3300", WORDSIZE), ParamType.INDIRECT_WITH_OFFSET to Hex("3400", WORDSIZE)), "store AC at address"),

        // Data Manipulation
        AND(mapOf(ParamType.IMMEDIATE to Hex("018A", WORDSIZE), ParamType.DIRECT to Hex("028A", WORDSIZE), ParamType.INDIRECT to Hex("038A", WORDSIZE), ParamType.INDIRECT_WITH_OFFSET to Hex("048A", WORDSIZE)), "and (logic)"),
        OR(mapOf(ParamType.IMMEDIATE to Hex("0188", WORDSIZE), ParamType.DIRECT to Hex("0288", WORDSIZE), ParamType.INDIRECT to Hex("0388", WORDSIZE), ParamType.INDIRECT_WITH_OFFSET to Hex("0488", WORDSIZE)), "or (logic)"),
        XOR(mapOf(ParamType.IMMEDIATE to Hex("0189", WORDSIZE), ParamType.DIRECT to Hex("0289", WORDSIZE), ParamType.INDIRECT to Hex("0389", WORDSIZE), ParamType.INDIRECT_WITH_OFFSET to Hex("0489", WORDSIZE)), "xor (logic)"),
        ADD(mapOf(ParamType.IMMEDIATE to Hex("018D", WORDSIZE), ParamType.DIRECT to Hex("028D", WORDSIZE), ParamType.INDIRECT to Hex("038D", WORDSIZE), ParamType.INDIRECT_WITH_OFFSET to Hex("048D", WORDSIZE)), "add"),
        ADDC(mapOf(ParamType.IMMEDIATE to Hex("01AD", WORDSIZE), ParamType.DIRECT to Hex("02AD", WORDSIZE), ParamType.INDIRECT to Hex("03AD", WORDSIZE), ParamType.INDIRECT_WITH_OFFSET to Hex("04AD", WORDSIZE)), "add with carry"),
        SUB(mapOf(ParamType.IMMEDIATE to Hex("018E", WORDSIZE), ParamType.DIRECT to Hex("028E", WORDSIZE), ParamType.INDIRECT to Hex("038E", WORDSIZE), ParamType.INDIRECT_WITH_OFFSET to Hex("048E", WORDSIZE)), "sub"),
        SUBC(mapOf(ParamType.IMMEDIATE to Hex("01AE", WORDSIZE), ParamType.DIRECT to Hex("02AE", WORDSIZE), ParamType.INDIRECT to Hex("03AE", WORDSIZE), ParamType.INDIRECT_WITH_OFFSET to Hex("04AE", WORDSIZE)), "sub with carry"),

        LSL(mapOf(ParamType.IMPLIED to Hex("00A0", WORDSIZE), ParamType.DIRECT to Hex("0220", WORDSIZE), ParamType.INDIRECT to Hex("0320", WORDSIZE), ParamType.INDIRECT_WITH_OFFSET to Hex("0420", WORDSIZE)), "logic shift left"),
        LSR(mapOf(ParamType.IMPLIED to Hex("00A1", WORDSIZE), ParamType.DIRECT to Hex("0221", WORDSIZE), ParamType.INDIRECT to Hex("0321", WORDSIZE), ParamType.INDIRECT_WITH_OFFSET to Hex("0421", WORDSIZE)), "logic shift right"),
        ROL(mapOf(ParamType.IMPLIED to Hex("00A2", WORDSIZE), ParamType.DIRECT to Hex("0222", WORDSIZE), ParamType.INDIRECT to Hex("0322", WORDSIZE), ParamType.INDIRECT_WITH_OFFSET to Hex("0422", WORDSIZE)), "rotate left"),
        ROR(mapOf(ParamType.IMPLIED to Hex("00A3", WORDSIZE), ParamType.DIRECT to Hex("0223", WORDSIZE), ParamType.INDIRECT to Hex("0323", WORDSIZE), ParamType.INDIRECT_WITH_OFFSET to Hex("0423", WORDSIZE)), "rotate right"),
        ASL(mapOf(ParamType.IMPLIED to Hex("00A4", WORDSIZE), ParamType.DIRECT to Hex("0224", WORDSIZE), ParamType.INDIRECT to Hex("0324", WORDSIZE), ParamType.INDIRECT_WITH_OFFSET to Hex("0424", WORDSIZE)), "arithmetic shift left"),
        ASR(mapOf(ParamType.IMPLIED to Hex("00A5", WORDSIZE), ParamType.DIRECT to Hex("0225", WORDSIZE), ParamType.INDIRECT to Hex("0325", WORDSIZE), ParamType.INDIRECT_WITH_OFFSET to Hex("0425", WORDSIZE)), "arithmetic shift right"),

        RCL(mapOf(ParamType.IMPLIED to Hex("00A6", WORDSIZE), ParamType.IMMEDIATE to Hex("0126", WORDSIZE), ParamType.DIRECT to Hex("0226", WORDSIZE), ParamType.INDIRECT to Hex("0326", WORDSIZE), ParamType.INDIRECT_WITH_OFFSET to Hex("0426", WORDSIZE)), "rotate left with carry"),
        RCR(mapOf(ParamType.IMPLIED to Hex("00A7", WORDSIZE), ParamType.IMMEDIATE to Hex("0127", WORDSIZE), ParamType.DIRECT to Hex("0227", WORDSIZE), ParamType.INDIRECT to Hex("0327", WORDSIZE), ParamType.INDIRECT_WITH_OFFSET to Hex("0427", WORDSIZE)), "rotate right with carry"),
        NOT(mapOf(ParamType.IMPLIED to Hex("008B", WORDSIZE), ParamType.DIRECT to Hex("020B", WORDSIZE), ParamType.INDIRECT to Hex("030B", WORDSIZE), ParamType.INDIRECT_WITH_OFFSET to Hex("040B", WORDSIZE)), "invert (logic not)"),

        NEG(mapOf(ParamType.DIRECT to Hex("024E", WORDSIZE), ParamType.INDIRECT to Hex("034E", WORDSIZE), ParamType.INDIRECT_WITH_OFFSET to Hex("044E", WORDSIZE)), "negotiate"),

        CLR(mapOf(ParamType.IMPLIED to Hex("004C", WORDSIZE)), "clear"),

        INC(mapOf(ParamType.IMPLIED to Hex("009C", WORDSIZE), ParamType.DIRECT to Hex("021C", WORDSIZE), ParamType.INDIRECT to Hex("031C", WORDSIZE), ParamType.INDIRECT_WITH_OFFSET to Hex("041C", WORDSIZE)), "increment (+1)"),
        DEC(mapOf(ParamType.IMPLIED to Hex("009F", WORDSIZE), ParamType.DIRECT to Hex("021F", WORDSIZE), ParamType.INDIRECT to Hex("031F", WORDSIZE), ParamType.INDIRECT_WITH_OFFSET to Hex("041F", WORDSIZE)), "decrement (-1)"),

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

        override fun getDetectionName(): String = this.name

        fun execute(arch: ArchIKRMini, paramtype: ParamType, ext: List<Hex>, tracker: Memory.AccessTracker) {
            val pc = arch.regContainer.pc
            val ac = arch.getRegByName("AC")

            if (ac == null) {
                arch.console.error("Couldn't find AC!")
                return
            }

            val operand: Hex = when (paramtype) {
                ParamType.INDIRECT -> arch.dataMemory.load(arch.dataMemory.load(ext[0], 2).toHex(), 2, tracker = tracker).toHex()
                ParamType.DIRECT -> arch.dataMemory.load(ext[0], 2, tracker = tracker).toHex()
                ParamType.IMMEDIATE -> ext[0]
                ParamType.DESTINATION -> (pc.get() + ext[0]).toHex()
                ParamType.IMPLIED -> ac.get().toHex()
                ParamType.INDIRECT_WITH_OFFSET -> {
                    val offset = ext[0]
                    val address = ext[1]
                    arch.dataMemory.load((arch.dataMemory.load(address, 2, tracker = tracker) + offset).toHex(), 2).toHex()
                }
            }

            val address: Hex = when (paramtype) {
                ParamType.INDIRECT -> arch.dataMemory.load(ext[0], 2, tracker = tracker).toHex()
                ParamType.DIRECT -> ext[0]
                ParamType.IMMEDIATE -> ext[0]
                ParamType.DESTINATION -> (pc.get().toHex() + ext[0]).toHex()
                ParamType.IMPLIED -> ac.get().toHex()
                ParamType.INDIRECT_WITH_OFFSET -> {
                    val offset = ext[0]
                    val address = ext[1]
                    (arch.dataMemory.load(address, 2, tracker = tracker) + offset).toHex()
                }
            }

            val flags = getFlags(arch)
            val acFirstBit = ac.get().toBin().getBit(0)?.toRawString() ?: "0"
            val opFirstBit = operand.toBin().getBit(0)?.toRawString() ?: "0"

            when (this) {
                LOAD -> ac.set(operand)
                LOADI -> ac.set(arch.dataMemory.load(operand, 2, tracker = tracker))
                STORE -> arch.dataMemory.store(address, ac.get(), Memory.InstanceType.DATA, tracker = tracker)
                AND -> {
                    val result = ac.get().toBin() and operand.toBin()
                    val n = result.getBit(0)?.toRawString() == "1"
                    val z = result.toRawString() == "0".repeat(WORDSIZE.bitWidth)
                    val v = false // TODO Check if should reset or not
                    val c = false // TODO Check if should reset or not
                    ac.set(result)
                    setFlags(arch, n, z, v, c)
                }

                OR -> {
                    val result = ac.get().toBin() or operand.toBin()
                    val n = result.getBit(0)?.toRawString() == "1"
                    val z = result.toRawString() == "0".repeat(WORDSIZE.bitWidth)
                    val v = false // TODO Check if should reset or not
                    val c = false // TODO Check if should reset or not
                    ac.set(result)
                    setFlags(arch, n, z, v, c)
                }

                XOR -> {
                    val result = ac.get().toBin() xor operand.toBin()
                    val n = result.getBit(0)?.toRawString() == "1"
                    val z = result.toRawString() == "0".repeat(WORDSIZE.bitWidth)
                    val v = false // TODO Check if should reset or not
                    val c = false // TODO Check if should reset or not
                    ac.set(result)
                    setFlags(arch, n, z, v, c)
                }

                ADD -> {
                    val firstRes = ac.get().toBin().detailedPlus(operand)
                    val result = firstRes.result
                    val resFirstBit = result.getBit(0)?.toRawString() ?: "0"
                    val n = result.getBit(0)?.toRawString() == "1"
                    val z = result.toRawString() == "0".repeat(WORDSIZE.bitWidth)
                    val v = acFirstBit == opFirstBit && acFirstBit != resFirstBit
                    val c = firstRes.carry
                    ac.set(result)
                    setFlags(arch, n, z, v, c)
                }

                ADDC -> {
                    val firstRes = ac.get().toBin().detailedPlus(operand)
                    val secondRes = firstRes.result.detailedPlus(if (flags.c) Bin("1", WORDSIZE) else Bin("0", WORDSIZE))
                    val result = secondRes.result
                    val resFirstBit = result.getBit(0)?.toRawString() ?: "0"
                    val n = result.getBit(0)?.toRawString() == "1"
                    val z = result.toRawString() == "0".repeat(WORDSIZE.bitWidth)
                    val v = acFirstBit == opFirstBit && acFirstBit != resFirstBit
                    val c = firstRes.carry xor secondRes.carry
                    ac.set(result)
                    setFlags(arch, n, z, v, c)
                }

                SUB -> {
                    val firstRes = ac.get().toBin().detailedMinus(operand)
                    val result = firstRes.result

                    val resFirstBit = result.getBit(0)?.toRawString() ?: "0"
                    val n = resFirstBit == "1"
                    val z = result.toRawString() == "0".repeat(WORDSIZE.bitWidth)
                    val v = acFirstBit != opFirstBit && opFirstBit == resFirstBit
                    val c = firstRes.borrow
                    ac.set(result)
                    setFlags(arch, n, z, v, c)
                }

                SUBC -> {
                    val result = (ac.get().toBin() - operand.toBin() - if (flags.c) Bin("1", WORDSIZE) else Bin("0", WORDSIZE)).toBin()
                    val resFirstBit = result.getBit(0)?.toRawString() ?: "0"
                    val n = resFirstBit == "1"
                    val z = result.toRawString() == "0".repeat(WORDSIZE.bitWidth)
                    val v = acFirstBit != opFirstBit && opFirstBit == resFirstBit
                    val c = acFirstBit == "0" && opFirstBit == "1"
                    ac.set(result)
                    setFlags(arch, n, z, v, c)
                }

                LSL -> {
                    val result = operand.toBin() ushl 1
                    val resFirstBit = result.getBit(0)?.toRawString() ?: "0"
                    val n = resFirstBit == "1"
                    val z = result.toRawString() == "0".repeat(WORDSIZE.bitWidth)
                    val v = false // TODO Check if should reset or not
                    val c = operand.toBin().getBit(0)?.toRawString() == "1"
                    setFlags(arch, n, z, v, c)
                    ac.set(result)
                }

                LSR -> {
                    val result = operand.toBin() ushr 1
                    val resFirstBit = result.getBit(0)?.toRawString() ?: "0"
                    val n = resFirstBit == "1"
                    val z = result.toRawString() == "0".repeat(WORDSIZE.bitWidth)
                    val v = false // TODO Check if should reset or not
                    val c = operand.toBin().getBit(15)?.toRawString() == "1"
                    setFlags(arch, n, z, v, c)
                    ac.set(result)
                }

                ROL -> {
                    val result = operand.toBin().rotateLeft(1)
                    val resFirstBit = result.getBit(0)?.toRawString() ?: "0"
                    val n = resFirstBit == "1"
                    val z = result.toRawString() == "0".repeat(WORDSIZE.bitWidth)
                    val v = false // TODO Check if should reset or not
                    val c = false // TODO Check if should reset or not
                    setFlags(arch, n, z, v, c)
                    ac.set(result)
                }

                ROR -> {
                    val result = operand.toBin().rotateRight(1)
                    val resFirstBit = result.getBit(0)?.toRawString() ?: "0"
                    val n = resFirstBit == "1"
                    val z = result.toRawString() == "0".repeat(WORDSIZE.bitWidth)
                    val v = false // TODO Check if should reset or not
                    val c = false // TODO Check if should reset or not
                    setFlags(arch, n, z, v, c)
                    ac.set(result)
                }

                ASL -> {
                    val result = operand.toBin() shl 1
                    val resFirstBit = result.getBit(0)?.toRawString() ?: "0"
                    val n = resFirstBit == "1"
                    val z = result.toRawString() == "0".repeat(WORDSIZE.bitWidth)
                    val v = false // TODO Check if should reset or not
                    val c = operand.toBin().getBit(0)?.toRawString() == "1"
                    setFlags(arch, n, z, v, c)
                    ac.set(result)
                }

                ASR -> {
                    val result = operand.toBin() shr 1
                    val resFirstBit = result.getBit(0)?.toRawString() ?: "0"
                    val n = resFirstBit == "1"
                    val z = result.toRawString() == "0".repeat(WORDSIZE.bitWidth)
                    val v = false // TODO Check if should reset or not
                    val c = operand.toBin().getBit(15)?.toRawString() == "1"
                    setFlags(arch, n, z, v, c)
                    ac.set(result)
                }

                RCL -> {
                    val result = Bin(operand.toBin().toRawString().substring(1) + if (flags.c) "1" else "0", WORDSIZE)
                    val resFirstBit = result.getBit(0)?.toRawString() ?: "0"
                    val n = resFirstBit == "1"
                    val z = result.toRawString() == "0".repeat(WORDSIZE.bitWidth)
                    val v = false // TODO Check if should reset or not
                    val c = false // TODO Check if should reset or not
                    setFlags(arch, n, z, v, c)
                    ac.set(result)
                }

                RCR -> {
                    val result = Bin((if (flags.c) "1" else "0") + operand.toBin().toRawString().substring(0, WORDSIZE.bitWidth - 1), WORDSIZE)
                    val resFirstBit = result.getBit(0)?.toRawString() ?: "0"
                    val n = resFirstBit == "1"
                    val z = result.toRawString() == "0".repeat(WORDSIZE.bitWidth)
                    val v = false // TODO Check if should reset or not
                    val c = false // TODO Check if should reset or not
                    setFlags(arch, n, z, v, c)
                    ac.set(result)
                }

                NOT -> {
                    val result = operand.toBin().inv()
                    val resFirstBit = result.getBit(0)?.toRawString() ?: "0"
                    val n = resFirstBit == "1"
                    val z = result.toRawString() == "0".repeat(WORDSIZE.bitWidth)
                    val v = false // TODO Check if should reset or not
                    val c = false // TODO Check if should reset or not
                    setFlags(arch, n, z, v, c)
                    ac.set(result)
                }

                NEG -> {
                    val result = (-operand).toBin()
                    val resFirstBit = result.getBit(0)?.toRawString() ?: "0"
                    val n = resFirstBit == "1"
                    val z = result.toRawString() == "0".repeat(WORDSIZE.bitWidth)
                    val v = false // TODO Check if should reset or not
                    val c = false // TODO Check if should reset or not
                    setFlags(arch, n, z, v, c)
                    ac.set(result)
                }

                CLR -> {
                    // TODO Was wird gecleared? AC? die Flags?
                    val result = Bin("0", WORDSIZE)
                    val n = false
                    val z = true
                    val v = false // TODO Check if should reset or not
                    val c = false // TODO Check if should reset or not
                    setFlags(arch, n, z, v, c)
                    ac.set(result)
                }

                INC -> {
                    val one = Bin("1", WORDSIZE)
                    val detailedResult = operand.toBin().detailedPlus(one)
                    val result = detailedResult.result
                    val resFirstBit = result.getBit(0)?.toRawString() ?: "0"
                    val n = result.getBit(0)?.toRawString() == "1"
                    val z = result.toRawString() == "0".repeat(WORDSIZE.bitWidth)
                    val v = opFirstBit == "0" && opFirstBit != resFirstBit
                    val c = detailedResult.carry
                    ac.set(result)
                    setFlags(arch, n, z, v, c)
                }

                DEC -> {
                    val one = Bin("1", WORDSIZE)
                    val detailedResult = operand.toBin().detailedMinus(one)
                    val result = detailedResult.result
                    val resFirstBit = result.getBit(0)?.toRawString() ?: "0"
                    val n = result.getBit(0)?.toRawString() == "1"
                    val z = result.toRawString() == "0".repeat(WORDSIZE.bitWidth)
                    val v = opFirstBit == "1" && opFirstBit != resFirstBit
                    val c = detailedResult.borrow
                    ac.set(result)
                    setFlags(arch, n, z, v, c)
                }

                BSR -> {
                    ac.set(pc.get() + Hex("4", WORDSIZE))
                    pc.set(address)
                    return
                }

                JMP -> {
                    pc.set(address)
                    return
                }

                BRA -> {
                    pc.set(address)
                    return
                }

                BHI -> {
                    if (!(flags.c || flags.z)) {
                        pc.set(address)
                        return
                    }
                }

                BLS -> {
                    if (flags.c || flags.z) {
                        pc.set(address)
                        return
                    }
                }

                BCC -> {
                    if (!flags.c) {
                        pc.set(address)
                        return
                    }
                }

                BCS -> {
                    if (flags.c) {
                        pc.set(address)
                        return
                    }
                }

                BNE -> {
                    if (!flags.z) {
                        pc.set(address)
                        return
                    }
                }

                BEQ -> {
                    if (flags.z) {
                        pc.set(address)
                        return
                    }
                }

                BVC -> {
                    if (!flags.v) {
                        pc.set(address)
                        return
                    }
                }

                BVS -> {
                    if (flags.v) {
                        pc.set(address)
                        return
                    }
                }

                BPL -> {
                    if (!flags.n) {
                        pc.set(address)
                        return
                    }
                }

                BMI -> {
                    if (flags.n) {
                        pc.set(address)
                        return
                    }
                }

                BGE -> {
                    if (!((flags.n && !flags.v) || (!flags.n && flags.v))) {
                        pc.set(address)
                        return
                    }
                }

                BLT -> {
                    if ((flags.n && !flags.v) || (!flags.n && flags.v)) {
                        pc.set(address)
                        return
                    }
                }

                BGT -> {
                    if (!((flags.n && !flags.v) || (!flags.n && flags.v) || flags.z)) {
                        pc.set(address)
                        return
                    }
                }

                BLE -> {
                    if ((flags.n && !flags.v) || (!flags.n && flags.v) || flags.z) {
                        pc.set(address)
                        return
                    }
                }
            }

            // increase pc (return early to skip)
            pc.set(pc.get() + Hex((paramtype.wordAmount * WORDSIZE.getByteCount()).toString(16), WORDSIZE))
        }

        fun getFlags(arch: ArchIKRMini): Flags {
            val flags = arch.getRegByName("NZVC")
            if (flags == null) {
                arch.console.error("Flag register not found!")
                return Flags(false, false, false, false)
            }

            val n = flags.get().toBin().getBit(0)?.toRawString() == "1"
            val z = flags.get().toBin().getBit(1)?.toRawString() == "1"
            val v = flags.get().toBin().getBit(2)?.toRawString() == "1"
            val c = flags.get().toBin().getBit(3)?.toRawString() == "1"
            return Flags(n, z, v, c)
        }

        fun setFlags(arch: ArchIKRMini, n: Boolean? = null, z: Boolean? = null, v: Boolean? = null, c: Boolean? = null) {
            val flags = arch.getRegByName("NZVC")
            if (flags == null) {
                arch.console.error("Flag register not found!")
                return
            }

            val oldFlags = getFlags(arch)
            flags.set(Bin("${if (n ?: oldFlags.n) "1" else "0"}${if (z ?: oldFlags.z) "1" else "0"}${if (v ?: oldFlags.v) "1" else "0"}${if (c ?: oldFlags.c) "1" else "0"}", Size.Bit4))
        }

    }

    data class Flags(val n: Boolean, val z: Boolean, val v: Boolean, val c: Boolean)

}