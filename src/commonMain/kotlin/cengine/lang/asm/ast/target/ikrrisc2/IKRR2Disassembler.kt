package cengine.lang.asm.ast.target.ikrrisc2

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import cengine.lang.asm.Disassembler
import cengine.util.ByteBuffer
import cengine.util.integer.Hex
import cengine.util.integer.Size
import cengine.util.integer.toValue

object IKRR2Disassembler : Disassembler {
    override val decoded: MutableState<List<Disassembler.DecodedSegment>> = mutableStateOf(emptyList())

    override fun disassemble(byteBuffer: ByteBuffer, startAddr: Hex): List<Disassembler.Decoded> {
        var currIndex = 0
        var currInstr: IKRR2InstrProvider
        val decoded = mutableListOf<Disassembler.Decoded>()

        while (currIndex < byteBuffer.size) {
            currInstr = try {
                IKRR2InstrProvider(byteBuffer.getUInt(currIndex))
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

        override fun decode(segmentAddr: Hex, offset: ULong): Disassembler.Decoded {
            return when (opcodeIType) {
                0x3FU -> {
                    // R Type
                    when (opcodeRType) {
                        0x00U -> {
                            // ADD
                            Disassembler.Decoded(offset, binaryAsHex, "add $rcReg := $rbReg, $raReg")
                        }

                        0x20U -> {
                            // ADDX
                            Disassembler.Decoded(offset, binaryAsHex, "addx $rcReg := $rbReg, $raReg")
                        }

                        0x02U -> {
                            // SUB
                            Disassembler.Decoded(offset, binaryAsHex, "sub $rcReg := $rbReg, $raReg")
                        }

                        0x22U -> {
                            // SUBX
                            Disassembler.Decoded(offset, binaryAsHex, "subx $rcReg := $rbReg, $raReg")
                        }

                        0x04U -> {
                            // AND
                            Disassembler.Decoded(offset, binaryAsHex, "and $rcReg := $rbReg, $raReg")
                        }

                        0x06U -> {
                            // OR
                            Disassembler.Decoded(offset, binaryAsHex, "or $rcReg := $rbReg, $raReg")
                        }

                        0x07U -> {
                            // XOR
                            Disassembler.Decoded(offset, binaryAsHex, "xor $rcReg := $rbReg, $raReg")
                        }

                        0x08U -> {
                            // CMPU
                            Disassembler.Decoded(offset, binaryAsHex, "cmpu $rcReg := $rbReg, $raReg")
                        }

                        0x09U -> {
                            // CMPS
                            Disassembler.Decoded(offset, binaryAsHex, "cmps $rcReg := $rbReg, $raReg")
                        }

                        0x10U -> {
                            // LDR
                            Disassembler.Decoded(offset, binaryAsHex, "ldr $rcReg := ($rbReg, $raReg)")
                        }

                        0x14U -> {
                            // STR
                            Disassembler.Decoded(offset, binaryAsHex, "str ($rbReg, $raReg) := $rcReg")
                        }

                        0x28U -> {
                            // LSL
                            Disassembler.Decoded(offset, binaryAsHex, "lsl $rcReg := $rbReg")
                        }

                        0x29U -> {
                            // LSR
                            Disassembler.Decoded(offset, binaryAsHex, "lsr $rcReg := $rbReg")
                        }

                        0x2AU -> {
                            // ASL
                            Disassembler.Decoded(offset, binaryAsHex, "asl $rcReg := $rbReg")
                        }

                        0x2BU -> {
                            // ASR
                            Disassembler.Decoded(offset, binaryAsHex, "asr $rcReg := $rbReg")
                        }

                        0x2CU -> when {
                            ra == 0x01U -> {
                                // ROL
                                Disassembler.Decoded(offset, binaryAsHex, "rol $rcReg := $rbReg")
                            }

                            ra == 0x10U -> {
                                // SWAPH
                                Disassembler.Decoded(offset, binaryAsHex, "swaph $rcReg := $rbReg")
                            }

                            else -> Disassembler.Decoded(offset, binaryAsHex, "[invalid]")
                        }


                        0x2DU -> {
                            // ROR
                            Disassembler.Decoded(offset, binaryAsHex, "ror $rcReg := $rbReg")
                        }

                        0x30U -> {
                            // EXTB
                            Disassembler.Decoded(offset, binaryAsHex, "extb $rcReg := $rbReg")
                        }

                        0x31U -> {
                            // EXTH
                            Disassembler.Decoded(offset, binaryAsHex, "exth $rcReg := $rbReg")
                        }

                        0x32U -> {
                            // SWAPB
                            Disassembler.Decoded(offset, binaryAsHex, "swapb $rcReg := $rbReg")
                        }

                        0x33U -> {
                            // NOT
                            Disassembler.Decoded(offset, binaryAsHex, "not $rcReg := $rbReg")
                        }

                        0x3CU -> {
                            // JMP
                            Disassembler.Decoded(offset, binaryAsHex, "jmp $rbReg")
                        }

                        0x3DU -> {
                            // JSR
                            Disassembler.Decoded(offset, binaryAsHex, "jsr $rbReg")
                        }

                        else -> Disassembler.Decoded(offset, binaryAsHex, "[invalid]")
                    }
                }
                // I Type
                0x00U -> {
                    // ADDI
                    Disassembler.Decoded(offset, binaryAsHex, "addi $rcReg := $rbReg, #${imm16.toString(16)}")
                }

                0x01U -> {
                    // ADDLI
                    Disassembler.Decoded(offset, binaryAsHex, "addli $rcReg := $rbReg, #${imm16.toString(16)}")
                }

                0x02U -> {
                    // ADDHI
                    Disassembler.Decoded(offset, binaryAsHex, "addhi $rcReg := $rbReg, #${imm16.toString(16)}")
                }

                0x04U -> {
                    // AND0I
                    Disassembler.Decoded(offset, binaryAsHex, "and0i $rcReg := $rbReg, #${imm16.toString(16)}")
                }

                0x05U -> {
                    // AND1I
                    Disassembler.Decoded(offset, binaryAsHex, "and1i $rcReg := $rbReg, #${imm16.toString(16)}")
                }

                0x06U -> {
                    // ORI
                    Disassembler.Decoded(offset, binaryAsHex, "ori $rcReg := $rbReg, #${imm16.toString(16)}")
                }

                0x07U -> {
                    // XORI
                    Disassembler.Decoded(offset, binaryAsHex, "xori $rcReg := $rbReg, #${imm16.toString(16)}")
                }

                0x08U -> {
                    // CMPUI
                    Disassembler.Decoded(offset, binaryAsHex, "cmpui $rcReg := $rbReg, #${imm16.toString(16)}")
                }

                0x09U -> {
                    // CMPSI
                    Disassembler.Decoded(offset, binaryAsHex, "cmpsi $rcReg := $rbReg, #${imm16.toString(16)}")
                }

                0x10U -> {
                    // LDD
                    Disassembler.Decoded(offset, binaryAsHex, "ldd $rcReg := ($rbReg, ${disp16.toString(16)})")
                }

                0x14U -> {
                    // STD
                    Disassembler.Decoded(offset, binaryAsHex, "std ($rbReg, ${disp16.toString(16)}) := $rcReg")
                }

                0x3CU -> {
                    val offset26 = disp18.toValue(Size.Bit26).toDec()
                    val target = (segmentAddr + offset.toValue(segmentAddr.size) + offset26).toHex()
                    // BRA
                    Disassembler.Decoded(offset, binaryAsHex, "bra ${disp26.toString(16)}", target)
                }

                0x3DU -> {
                    val offset26 = disp18.toValue(Size.Bit26).toDec()
                    val target = (segmentAddr + offset.toValue(segmentAddr.size) + offset26).toHex()
                    // BSR
                    Disassembler.Decoded(offset, binaryAsHex, "bsr ${disp26.toString(16)}", target)
                }

                0x3EU -> {
                    val offset18 = disp18.toValue(Size.Bit18).toDec()
                    val target = (segmentAddr + offset.toValue(segmentAddr.size) + offset18).toHex()
                    when (bType) {
                        0x0U -> {
                            // BEQ
                            Disassembler.Decoded(offset, binaryAsHex, "beq ${disp18.toString(16)}", target)
                        }

                        0x1U -> {
                            // BNE
                            Disassembler.Decoded(offset, binaryAsHex, "bne ${disp18.toString(16)}", target)
                        }

                        0x2U -> {
                            // BLT
                            Disassembler.Decoded(offset, binaryAsHex, "blt ${disp18.toString(16)}", target)
                        }

                        0x3U -> {
                            // BGT
                            Disassembler.Decoded(offset, binaryAsHex, "bgt ${disp18.toString(16)}", target)
                        }

                        0x4U -> {
                            // BLE
                            Disassembler.Decoded(offset, binaryAsHex, "ble ${disp18.toString(16)}", target)
                        }

                        0x5U -> {
                            // BGE
                            Disassembler.Decoded(offset, binaryAsHex, "bge ${disp18.toString(16)}", target)
                        }

                        else -> Disassembler.Decoded(offset, binaryAsHex, "[invalid]")
                    }
                }

                else -> Disassembler.Decoded(offset, binaryAsHex, "[invalid]")

            }
        }
    }

}