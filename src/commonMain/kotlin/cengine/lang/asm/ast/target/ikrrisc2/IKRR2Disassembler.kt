package cengine.lang.asm.ast.target.ikrrisc2

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import cengine.lang.asm.Disassembler
import cengine.lang.asm.ast.target.ikrrisc2.IKRR2Disassembler.InstrType.*
import cengine.util.integer.Hex
import cengine.util.integer.Size
import cengine.util.integer.Value.Companion.toValue

object IKRR2Disassembler : Disassembler {
    override val decoded: MutableState<List<Disassembler.DecodedSegment>> = mutableStateOf(emptyList())

    override fun disassemble(startAddr: Hex, buffer: List<Hex>): List<Disassembler.Decoded> {
        var currIndex = 0
        var currInstr: IKRR2InstrProvider
        val decoded = mutableListOf<Disassembler.Decoded>()

        while (currIndex < buffer.size) {
            currInstr = try {
                IKRR2InstrProvider(buffer[currIndex].toULong().toUInt())
            } catch (e: IndexOutOfBoundsException) {
                break
            }

            decoded.add(currInstr.decode(startAddr, currIndex.toULong()))

            currIndex += 4
        }

        return decoded
    }

    data class IKRR2InstrProvider(val binary: UInt) : Disassembler.InstrProvider {
        val binaryAsHex = binary.toValue()

        val opcodeRType = binary.shr(10) and 0b111111U
        val opcodeIType = binary.shr(26)
        val ra = binary and 0b11111U
        val rb = binary.shr(16) and 0b11111U
        val rc = binary.shr(21) and 0b11111U
        val raReg get() = IKRR2BaseRegs.entries[ra.toInt()].displayName
        val rbReg get() = IKRR2BaseRegs.entries[rb.toInt()].displayName
        val rcReg get() = IKRR2BaseRegs.entries[rc.toInt()].displayName
        val imm16 = binary and 0xFFFFU
        val disp16 get() = imm16
        val disp18 = binary and 0b111111111111111111U
        val disp26 = binary and 0b11111111111111111111111111U
        val bType = binary.shr(18) and 0b111U

        val type: InstrType? = when (opcodeIType) {
            0x3FU -> {
                // R Type
                when (opcodeRType) {
                    0x00U -> ADD
                    0x20U -> ADDX
                    0x02U -> SUB
                    0x22U -> SUBX
                    0x04U -> AND
                    0x06U -> OR
                    0x07U -> XOR
                    0x08U -> CMPU
                    0x09U -> CMPS
                    0x10U -> LDR
                    0x14U -> STR
                    0x28U -> LSL
                    0x29U -> LSR
                    0x2AU -> ASL
                    0x2BU -> ASR
                    0x2CU -> when {
                        ra == 0x01U -> ROL
                        ra == 0x10U -> SWAPH
                        else -> null
                    }

                    0x2DU -> ROR
                    0x30U -> EXTB
                    0x31U -> EXTH
                    0x32U -> SWAPB
                    0x33U -> NOT
                    0x3CU -> JMP
                    0x3DU -> JSR
                    else -> null
                }
            }
            // I Type
            0x00U -> ADDI
            0x01U -> ADDLI
            0x02U -> ADDHI
            0x04U -> AND0I
            0x05U -> AND1I
            0x06U -> ORI
            0x07U -> XORI
            0x08U -> CMPUI
            0x09U -> CMPSI
            0x10U -> LDD
            0x14U -> STD
            0x3CU -> BRA
            0x3DU -> BSR
            0x3EU -> {
                when (bType) {
                    0x0U -> BEQ
                    0x1U -> BNE
                    0x2U -> BLT
                    0x3U -> BGT
                    0x4U -> BLE
                    0x5U -> BGE
                    else -> null
                }
            }

            else -> null
        }

        override fun decode(segmentAddr: Hex, offset: ULong): Disassembler.Decoded {
            val hexPrefix = IKRR2Spec.prefices.hex
            val wordAlignedOffset = offset shr 2
            return when (type) {
                ADD, ADDX, SUB, SUBX, AND, OR, XOR, CMPU, CMPS -> Disassembler.Decoded(wordAlignedOffset, binaryAsHex, "${type.lc5Name} $rcReg := $rbReg, $raReg")
                ADDI, ADDLI, ADDHI, CMPUI, CMPSI, AND0I, AND1I, ORI, XORI -> Disassembler.Decoded(wordAlignedOffset, binaryAsHex, "${type.lc5Name} $rcReg := $rbReg, #$hexPrefix${imm16.toString(16)}")
                LSL, LSR, ASL, ASR, ROL, ROR, SWAPH, SWAPB, EXTB, EXTH, NOT -> Disassembler.Decoded(wordAlignedOffset, binaryAsHex, "${type.lc5Name} $rcReg := $rbReg")
                LDD -> Disassembler.Decoded(wordAlignedOffset, binaryAsHex, "${type.lc5Name} $rcReg := ($rbReg, $hexPrefix${disp16.toString(16)})")
                LDR -> Disassembler.Decoded(wordAlignedOffset, binaryAsHex, "${type.lc5Name} $rcReg := ($rbReg, $raReg)")
                STD -> Disassembler.Decoded(wordAlignedOffset, binaryAsHex, "${type.lc5Name} ($rbReg, $hexPrefix${disp16.toString(16)}) := $rcReg")
                STR -> Disassembler.Decoded(wordAlignedOffset, binaryAsHex, "${type.lc5Name} ($rbReg, $raReg) := $rcReg")
                BEQ, BNE, BLT, BGT, BLE, BGE -> {
                    val offset18 = disp18.toValue(Size.Bit18).toDec().getResized(segmentAddr.size)
                    val target = (segmentAddr + wordAlignedOffset.toValue(segmentAddr.size) + offset18).toHex()
                    Disassembler.Decoded(wordAlignedOffset, binaryAsHex, "${type.lc5Name} $hexPrefix${disp18.toString(16)}", target)
                }
                BRA, BSR -> {
                    val offset26 = disp26.toValue(Size.Bit26).toDec().getResized(segmentAddr.size)
                    val target = (segmentAddr + wordAlignedOffset.toValue(segmentAddr.size) + offset26).toHex()
                    Disassembler.Decoded(wordAlignedOffset, binaryAsHex, "${type.lc5Name} $hexPrefix${disp26.toString(16)}", target)
                }
                JMP, JSR -> Disassembler.Decoded(wordAlignedOffset, binaryAsHex, "${type.lc5Name} $rbReg")
                null -> Disassembler.Decoded(wordAlignedOffset, binaryAsHex, "[invalid]")
            }
        }
    }

    enum class InstrType {
        ADD,
        ADDI,
        ADDLI,
        ADDHI,
        ADDX,
        SUB,
        SUBX,
        CMPU,
        CMPS,
        CMPUI,
        CMPSI,
        AND,
        AND0I,
        AND1I,
        OR,
        ORI,
        XOR,
        XORI,
        LSL,
        LSR,
        ASL,
        ASR,
        ROL,
        ROR,
        EXTB,
        EXTH,
        SWAPB,
        SWAPH,
        NOT,
        LDD,
        LDR,
        STD,
        STR,
        BEQ,
        BNE,
        BLT,
        BGT,
        BLE,
        BGE,
        BRA,
        BSR,
        JMP,
        JSR;

        val lc5Name: String = name.lowercase().padEnd(5, ' ')
    }

}