package cengine.lang.asm.ast.target.ikrrisc2

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import cengine.lang.asm.Disassembler
import cengine.util.integer.Hex
import cengine.util.integer.Size
import cengine.util.integer.toValue
import emulator.kit.nativeLog

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

        nativeLog("disassemble: $startAddr, $buffer -> $decoded")

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

        override fun decode(segmentAddr: Hex, offset: ULong): Disassembler.Decoded {
            val hexPrefix = IKRR2Spec.prefices.hex
            val wordAlignedOffset = offset shr 2
            return when (opcodeIType) {
                0x3FU -> {
                    // R Type
                    when (opcodeRType) {
                        0x00U -> {
                            // ADD
                            Disassembler.Decoded(wordAlignedOffset, binaryAsHex, "add $rcReg := $rbReg, $raReg")
                        }

                        0x20U -> {
                            // ADDX
                            Disassembler.Decoded(wordAlignedOffset, binaryAsHex, "addx $rcReg := $rbReg, $raReg")
                        }

                        0x02U -> {
                            // SUB
                            Disassembler.Decoded(wordAlignedOffset, binaryAsHex, "sub $rcReg := $rbReg, $raReg")
                        }

                        0x22U -> {
                            // SUBX
                            Disassembler.Decoded(wordAlignedOffset, binaryAsHex, "subx $rcReg := $rbReg, $raReg")
                        }

                        0x04U -> {
                            // AND
                            Disassembler.Decoded(wordAlignedOffset, binaryAsHex, "and $rcReg := $rbReg, $raReg")
                        }

                        0x06U -> {
                            // OR
                            Disassembler.Decoded(wordAlignedOffset, binaryAsHex, "or $rcReg := $rbReg, $raReg")
                        }

                        0x07U -> {
                            // XOR
                            Disassembler.Decoded(wordAlignedOffset, binaryAsHex, "xor $rcReg := $rbReg, $raReg")
                        }

                        0x08U -> {
                            // CMPU
                            Disassembler.Decoded(wordAlignedOffset, binaryAsHex, "cmpu $rcReg := $rbReg, $raReg")
                        }

                        0x09U -> {
                            // CMPS
                            Disassembler.Decoded(wordAlignedOffset, binaryAsHex, "cmps $rcReg := $rbReg, $raReg")
                        }

                        0x10U -> {
                            // LDR
                            Disassembler.Decoded(wordAlignedOffset, binaryAsHex, "ldr $rcReg := ($rbReg, $raReg)")
                        }

                        0x14U -> {
                            // STR
                            Disassembler.Decoded(wordAlignedOffset, binaryAsHex, "str ($rbReg, $raReg) := $rcReg")
                        }

                        0x28U -> {
                            // LSL
                            Disassembler.Decoded(wordAlignedOffset, binaryAsHex, "lsl $rcReg := $rbReg")
                        }

                        0x29U -> {
                            // LSR
                            Disassembler.Decoded(wordAlignedOffset, binaryAsHex, "lsr $rcReg := $rbReg")
                        }

                        0x2AU -> {
                            // ASL
                            Disassembler.Decoded(wordAlignedOffset, binaryAsHex, "asl $rcReg := $rbReg")
                        }

                        0x2BU -> {
                            // ASR
                            Disassembler.Decoded(wordAlignedOffset, binaryAsHex, "asr $rcReg := $rbReg")
                        }

                        0x2CU -> when {
                            ra == 0x01U -> {
                                // ROL
                                Disassembler.Decoded(wordAlignedOffset, binaryAsHex, "rol $rcReg := $rbReg")
                            }

                            ra == 0x10U -> {
                                // SWAPH
                                Disassembler.Decoded(wordAlignedOffset, binaryAsHex, "swaph $rcReg := $rbReg")
                            }

                            else -> Disassembler.Decoded(wordAlignedOffset, binaryAsHex, "[invalid]")
                        }


                        0x2DU -> {
                            // ROR
                            Disassembler.Decoded(wordAlignedOffset, binaryAsHex, "ror $rcReg := $rbReg")
                        }

                        0x30U -> {
                            // EXTB
                            Disassembler.Decoded(wordAlignedOffset, binaryAsHex, "extb $rcReg := $rbReg")
                        }

                        0x31U -> {
                            // EXTH
                            Disassembler.Decoded(wordAlignedOffset, binaryAsHex, "exth $rcReg := $rbReg")
                        }

                        0x32U -> {
                            // SWAPB
                            Disassembler.Decoded(wordAlignedOffset, binaryAsHex, "swapb $rcReg := $rbReg")
                        }

                        0x33U -> {
                            // NOT
                            Disassembler.Decoded(wordAlignedOffset, binaryAsHex, "not $rcReg := $rbReg")
                        }

                        0x3CU -> {
                            // JMP
                            Disassembler.Decoded(wordAlignedOffset, binaryAsHex, "jmp $rbReg")
                        }

                        0x3DU -> {
                            // JSR
                            Disassembler.Decoded(wordAlignedOffset, binaryAsHex, "jsr $rbReg")
                        }

                        else -> Disassembler.Decoded(wordAlignedOffset, binaryAsHex, "[invalid]")
                    }
                }
                // I Type
                0x00U -> {
                    // ADDI
                    Disassembler.Decoded(wordAlignedOffset, binaryAsHex, "addi $rcReg := $rbReg, #$hexPrefix${imm16.toString(16)}")
                }

                0x01U -> {
                    // ADDLI
                    Disassembler.Decoded(wordAlignedOffset, binaryAsHex, "addli $rcReg := $rbReg, #$hexPrefix${imm16.toString(16)}")
                }

                0x02U -> {
                    // ADDHI
                    Disassembler.Decoded(wordAlignedOffset, binaryAsHex, "addhi $rcReg := $rbReg, #$hexPrefix${imm16.toString(16)}")
                }

                0x04U -> {
                    // AND0I
                    Disassembler.Decoded(wordAlignedOffset, binaryAsHex, "and0i $rcReg := $rbReg, #$hexPrefix${imm16.toString(16)}")
                }

                0x05U -> {
                    // AND1I
                    Disassembler.Decoded(wordAlignedOffset, binaryAsHex, "and1i $rcReg := $rbReg, #$hexPrefix${imm16.toString(16)}")
                }

                0x06U -> {
                    // ORI
                    Disassembler.Decoded(wordAlignedOffset, binaryAsHex, "ori $rcReg := $rbReg, #$hexPrefix${imm16.toString(16)}")
                }

                0x07U -> {
                    // XORI
                    Disassembler.Decoded(wordAlignedOffset, binaryAsHex, "xori $rcReg := $rbReg, #$hexPrefix${imm16.toString(16)}")
                }

                0x08U -> {
                    // CMPUI
                    Disassembler.Decoded(wordAlignedOffset, binaryAsHex, "cmpui $rcReg := $rbReg, #$hexPrefix${imm16.toString(16)}")
                }

                0x09U -> {
                    // CMPSI
                    Disassembler.Decoded(wordAlignedOffset, binaryAsHex, "cmpsi $rcReg := $rbReg, #$hexPrefix${imm16.toString(16)}")
                }

                0x10U -> {
                    // LDD
                    Disassembler.Decoded(wordAlignedOffset, binaryAsHex, "ldd $rcReg := ($rbReg, $hexPrefix${disp16.toString(16)})")
                }

                0x14U -> {
                    // STD
                    Disassembler.Decoded(wordAlignedOffset, binaryAsHex, "std ($rbReg, $hexPrefix${disp16.toString(16)}) := $rcReg")
                }

                0x3CU -> {
                    val offset26 = disp26.toValue(Size.Bit26).toDec().getResized(segmentAddr.size)
                    val target = (segmentAddr + wordAlignedOffset.toValue(segmentAddr.size) + offset26).toHex()
                    // BRA
                    Disassembler.Decoded(wordAlignedOffset, binaryAsHex, "bra $hexPrefix${disp26.toString(16)}", target)
                }

                0x3DU -> {
                    val offset26 = disp26.toValue(Size.Bit26).toDec().getResized(segmentAddr.size)
                    val target = (segmentAddr + wordAlignedOffset.toValue(segmentAddr.size) + offset26).toHex()
                    // BSR
                    Disassembler.Decoded(wordAlignedOffset, binaryAsHex, "bsr $hexPrefix${disp26.toString(16)}", target)
                }

                0x3EU -> {
                    val offset18 = disp18.toValue(Size.Bit18).toDec().getResized(segmentAddr.size)
                    val target = (segmentAddr + wordAlignedOffset.toValue(segmentAddr.size) + offset18).toHex()
                    when (bType) {
                        0x0U -> {
                            // BEQ
                            Disassembler.Decoded(wordAlignedOffset, binaryAsHex, "beq $hexPrefix${disp18.toString(16)}", target)
                        }

                        0x1U -> {
                            // BNE
                            Disassembler.Decoded(wordAlignedOffset, binaryAsHex, "bne $hexPrefix${disp18.toString(16)}", target)
                        }

                        0x2U -> {
                            // BLT
                            Disassembler.Decoded(wordAlignedOffset, binaryAsHex, "blt $hexPrefix${disp18.toString(16)}", target)
                        }

                        0x3U -> {
                            // BGT
                            Disassembler.Decoded(wordAlignedOffset, binaryAsHex, "bgt $hexPrefix${disp18.toString(16)}", target)
                        }

                        0x4U -> {
                            // BLE
                            Disassembler.Decoded(wordAlignedOffset, binaryAsHex, "ble $hexPrefix${disp18.toString(16)}", target)
                        }

                        0x5U -> {
                            // BGE
                            Disassembler.Decoded(wordAlignedOffset, binaryAsHex, "bge $hexPrefix${disp18.toString(16)}", target)
                        }

                        else -> Disassembler.Decoded(wordAlignedOffset, binaryAsHex, "[invalid]")
                    }
                }

                else -> Disassembler.Decoded(wordAlignedOffset, binaryAsHex, "[invalid]")

            }
        }
    }

}