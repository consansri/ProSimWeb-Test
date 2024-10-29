package cengine.lang.asm.ast.target.riscv

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import cengine.lang.asm.Disassembler
import cengine.lang.asm.Disassembler.Decoded
import cengine.util.ByteBuffer
import cengine.util.integer.Hex
import cengine.util.integer.Size
import cengine.util.integer.toValue

object RVDisassembler : Disassembler {
    override val decoded: MutableState<List<Disassembler.DecodedSegment>> = mutableStateOf(emptyList())

    override fun disassemble(byteBuffer: ByteBuffer, startAddr: Hex): List<Decoded> {
        var currIndex = 0
        var currInstr: RVInstrInfoProvider
        val decoded = mutableListOf<Decoded>()

        while (currIndex < byteBuffer.size) {
            currInstr = RVInstrInfoProvider(byteBuffer.getUInt(currIndex))

            decoded.add(currInstr.decode(startAddr, currIndex.toULong()))

            currIndex += 4
        }

        return decoded
    }

    data class RVInstrInfoProvider(val binary: UInt) {
        val binaryAsHex: Hex = binary.toValue()
        val opcode = binary and 0b1111111U
        val funct3 = (binary shr 12) and 0b111U
        val funct7 = binary.shr(25) and 0b1111111U
        val rd = (binary shr 7) and 0b11111U
        val rs1 = (binary shr 15) and 0b11111U
        val rs2 = (binary shr 20) and 0b11111U
        val imm12iType = (binary shr 20) and 0b111111111111U
        val imm12sType = ((binary shr 25) shl 5) or rd
        val imm12bType = ((binary shr 31) shl 12) or (((binary shr 7) and 0b1U) shl 11) or (((binary shr 25) and 0b111111U) shl 5) or ((binary shr 1) and 0b1111U)
        val imm20uType = binary shr 12
        val imm20jType = ((binary shr 31) shl 20) or (((binary shr 12) and 0b11111111U) shl 12) or (((binary shr 20) and 0b1U) shl 11) or (((binary shr 21) and 0b1111111111U) shl 1)
        val shamt = (imm12iType and 0b111111U)

        fun decode(segmentAddr: Hex, offset: ULong): Decoded {
            return when (opcode) {
                RVConst.OPC_LUI -> Decoded(offset, binaryAsHex,  "lui ${rdName()}, 0x${imm20uType.toString(16)}")
                RVConst.OPC_AUIPC -> Decoded(offset, binaryAsHex,  "auipc ${rdName()}, 0x${imm20uType.toString(16)}")
                RVConst.OPC_JAL -> Decoded(offset, binaryAsHex,  "jal ${rdName()}, ${imm20jType.toValue(Size.Bit20).toDec()}", (segmentAddr + offset.toValue(segmentAddr.size) + imm20jType.toValue(Size.Bit20).toDec().getResized(segmentAddr.size)).toHex())
                RVConst.OPC_JALR -> Decoded(offset, binaryAsHex,  "jalr ${rdName()}, ${rs1Name()}, ${imm12iType.toInt()}")
                RVConst.OPC_OS -> {
                    when (funct3) {
                        RVConst.FUNCT3_CSR_RW -> Decoded(offset, binaryAsHex,  "csrrw ${rdName()}, ${csrName()}, ${rs1Name()}")
                        RVConst.FUNCT3_CSR_RS -> Decoded(offset, binaryAsHex,  "csrrs ${rdName()}, ${csrName()}, ${rs1Name()}")
                        RVConst.FUNCT3_CSR_RC -> Decoded(offset, binaryAsHex,  "csrrc ${rdName()}, ${csrName()}, ${rs1Name()}")
                        RVConst.FUNCT3_CSR_RWI -> Decoded(offset, binaryAsHex,  "csrrwi ${rdName()}, ${csrName()}, 0b${rs1.toString(2)}")
                        RVConst.FUNCT3_CSR_RSI -> Decoded(offset, binaryAsHex,  "csrrsi ${rdName()}, ${csrName()}, 0b${rs1.toString(2)}")
                        RVConst.FUNCT3_CSR_RCI -> Decoded(offset, binaryAsHex,  "csrrci ${rdName()}, ${csrName()}, 0b${rs1.toString(2)}")
                        RVConst.FUNCT3_E -> when (imm12iType) {
                            1U -> Decoded(offset, binaryAsHex,  "ebreak")
                            else -> Decoded(offset, binaryAsHex,  "ecall")
                        }

                        else -> Decoded(offset, binaryAsHex,  "[invalid]")
                    }
                }

                RVConst.OPC_CBRA -> {
                    val offset12 = imm12bType.toValue(Size.Bit12).toDec().getResized(segmentAddr.size)
                    val target = (segmentAddr + offset.toValue(segmentAddr.size) + offset12).toHex()
                    when (funct3) {
                        RVConst.FUNCT3_CBRA_BEQ -> Decoded(offset, binaryAsHex,  "beq ${rs1Name()}, ${rs2Name()}, $offset12", target)
                        RVConst.FUNCT3_CBRA_BNE -> Decoded(offset, binaryAsHex,  "bne ${rs1Name()}, ${rs2Name()}, $offset12", target)
                        RVConst.FUNCT3_CBRA_BLT -> Decoded(offset, binaryAsHex,  "blt ${rs1Name()}, ${rs2Name()}, $offset12", target)
                        RVConst.FUNCT3_CBRA_BGE -> Decoded(offset, binaryAsHex,  "bge ${rs1Name()}, ${rs2Name()}, $offset12", target)
                        RVConst.FUNCT3_CBRA_BLTU -> Decoded(offset, binaryAsHex,  "bltu ${rs1Name()}, ${rs2Name()}, $offset12", target)
                        RVConst.FUNCT3_CBRA_BGEU -> Decoded(offset, binaryAsHex,  "bgeu ${rs1Name()}, ${rs2Name()}, $offset12", target)
                        else -> Decoded(offset, binaryAsHex,  "[invalid]")
                    }
                }

                RVConst.OPC_LOAD -> {
                    val offset12 = imm12iType.toValue(Size.Bit12).toDec()
                    when (funct3) {
                        RVConst.FUNCT3_LOAD_B -> Decoded(offset, binaryAsHex,  "lb ${rdName()}, $offset12(${rs1Name()})")
                        RVConst.FUNCT3_LOAD_H -> Decoded(offset, binaryAsHex,  "lh ${rdName()}, $offset12(${rs1Name()})")
                        RVConst.FUNCT3_LOAD_W -> Decoded(offset, binaryAsHex,  "lw ${rdName()}, $offset12(${rs1Name()})")
                        RVConst.FUNCT3_LOAD_D -> Decoded(offset, binaryAsHex,  "ld ${rdName()}, $offset12(${rs1Name()})")
                        RVConst.FUNCT3_LOAD_BU -> Decoded(offset, binaryAsHex,  "lbu ${rdName()}, $offset12(${rs1Name()})")
                        RVConst.FUNCT3_LOAD_HU -> Decoded(offset, binaryAsHex,  "lhu ${rdName()}, $offset12(${rs1Name()})")
                        RVConst.FUNCT3_LOAD_WU -> Decoded(offset, binaryAsHex,  "lwu ${rdName()}, $offset12(${rs1Name()})")
                        else -> Decoded(offset, binaryAsHex,  "[invalid]")
                    }
                }

                RVConst.OPC_STORE -> {
                    val offset12 = imm12sType.toValue(Size.Bit12).toDec()
                    when (funct3) {
                        RVConst.FUNCT3_STORE_B -> Decoded(offset, binaryAsHex,  "lb ${rs2Name()}, $offset12(${rs1Name()})")
                        RVConst.FUNCT3_STORE_H -> Decoded(offset, binaryAsHex,  "lh ${rs2Name()}, $offset12(${rs1Name()})")
                        RVConst.FUNCT3_STORE_W -> Decoded(offset, binaryAsHex,  "lw ${rs2Name()}, $offset12(${rs1Name()})")
                        RVConst.FUNCT3_STORE_D -> Decoded(offset, binaryAsHex,  "ld ${rs2Name()}, $offset12(${rs1Name()})")
                        else -> Decoded(offset, binaryAsHex,  "[invalid]")
                    }
                }

                RVConst.OPC_ARITH -> {
                    when (funct7) {
                        RVConst.FUNCT7_SHIFT_ARITH_OR_SUB -> {
                            when (funct3) {
                                RVConst.FUNCT3_SHIFT_RIGHT -> Decoded(offset, binaryAsHex,  "sra ${rdName()}, ${rs1Name()}, ${rs2Name()}")
                                RVConst.FUNCT3_OPERATION -> Decoded(offset, binaryAsHex,  "sub ${rdName()}, ${rs1Name()}, ${rs2Name()}")
                                else -> Decoded(offset, binaryAsHex,  "[invalid]")
                            }
                        }

                        RVConst.FUNCT7_M -> {
                            when (funct3) {
                                RVConst.FUNCT3_M_MUL -> Decoded(offset, binaryAsHex,  "mul ${rdName()}, ${rs1Name()}, ${rs2Name()}")
                                RVConst.FUNCT3_M_MULH -> Decoded(offset, binaryAsHex,  "mulh ${rdName()}, ${rs1Name()}, ${rs2Name()}")
                                RVConst.FUNCT3_M_MULHSU -> Decoded(offset, binaryAsHex,  "mulhsu ${rdName()}, ${rs1Name()}, ${rs2Name()}")
                                RVConst.FUNCT3_M_MULHU -> Decoded(offset, binaryAsHex,  "mulhu ${rdName()}, ${rs1Name()}, ${rs2Name()}")
                                RVConst.FUNCT3_M_DIV -> Decoded(offset, binaryAsHex,  "div ${rdName()}, ${rs1Name()}, ${rs2Name()}")
                                RVConst.FUNCT3_M_DIVU -> Decoded(offset, binaryAsHex,  "divu ${rdName()}, ${rs1Name()}, ${rs2Name()}")
                                RVConst.FUNCT3_M_REM -> Decoded(offset, binaryAsHex,  "rem ${rdName()}, ${rs1Name()}, ${rs2Name()}")
                                RVConst.FUNCT3_M_REMU -> Decoded(offset, binaryAsHex,  "remu ${rdName()}, ${rs1Name()}, ${rs2Name()}")
                                else -> Decoded(offset, binaryAsHex,  "[invalid]")
                            }
                        }

                        else -> {
                            when (funct3) {
                                RVConst.FUNCT3_SHIFT_LEFT -> Decoded(offset, binaryAsHex,  "sll ${rdName()}, ${rs1Name()}, ${rs2Name()}")
                                RVConst.FUNCT3_SHIFT_RIGHT -> Decoded(offset, binaryAsHex,  "srl ${rdName()}, ${rs1Name()}, ${rs2Name()}")
                                RVConst.FUNCT3_SLT -> Decoded(offset, binaryAsHex,  "slt ${rdName()}, ${rs1Name()}, ${rs2Name()}")
                                RVConst.FUNCT3_SLTU -> Decoded(offset, binaryAsHex,  "sltu ${rdName()}, ${rs1Name()}, ${rs2Name()}")
                                RVConst.FUNCT3_OR -> Decoded(offset, binaryAsHex,  "or ${rdName()}, ${rs1Name()}, ${rs2Name()}")
                                RVConst.FUNCT3_AND -> Decoded(offset, binaryAsHex,  "and ${rdName()}, ${rs1Name()}, ${rs2Name()}")
                                RVConst.FUNCT3_XOR -> Decoded(offset, binaryAsHex,  "xor ${rdName()}, ${rs1Name()}, ${rs2Name()}")
                                RVConst.FUNCT3_OPERATION -> Decoded(offset, binaryAsHex,  "add ${rdName()}, ${rs1Name()}, ${rs2Name()}")
                                else -> Decoded(offset, binaryAsHex,  "[invalid]")
                            }
                        }
                    }
                }

                RVConst.OPC_ARITH_WORD -> {
                    when (funct7) {
                        RVConst.FUNCT7_SHIFT_ARITH_OR_SUB -> {
                            when (funct3) {
                                RVConst.FUNCT3_SHIFT_RIGHT -> Decoded(offset, binaryAsHex,  "sraw ${rdName()}, ${rs1Name()}, ${rs2Name()}")
                                RVConst.FUNCT3_OPERATION -> Decoded(offset, binaryAsHex,  "subw ${rdName()}, ${rs1Name()}, ${rs2Name()}")
                                else -> Decoded(offset, binaryAsHex,  "[invalid]")
                            }
                        }

                        RVConst.FUNCT7_M -> {
                            when (funct3) {
                                RVConst.FUNCT3_M_MUL -> Decoded(offset, binaryAsHex,  "mulw ${rdName()}, ${rs1Name()}, ${rs2Name()}")
                                RVConst.FUNCT3_M_DIV -> Decoded(offset, binaryAsHex,  "divw ${rdName()}, ${rs1Name()}, ${rs2Name()}")
                                RVConst.FUNCT3_M_DIVU -> Decoded(offset, binaryAsHex,  "divuw ${rdName()}, ${rs1Name()}, ${rs2Name()}")
                                RVConst.FUNCT3_M_REM -> Decoded(offset, binaryAsHex,  "remw ${rdName()}, ${rs1Name()}, ${rs2Name()}")
                                RVConst.FUNCT3_M_REMU -> Decoded(offset, binaryAsHex,  "remuw ${rdName()}, ${rs1Name()}, ${rs2Name()}")
                                else -> Decoded(offset, binaryAsHex,  "[invalid]")
                            }
                        }

                        else -> {
                            when (funct3) {
                                RVConst.FUNCT3_SHIFT_LEFT -> Decoded(offset, binaryAsHex,  "sllw ${rdName()}, ${rs1Name()}, ${rs2Name()}")
                                RVConst.FUNCT3_SHIFT_RIGHT -> Decoded(offset, binaryAsHex,  "srlw ${rdName()}, ${rs1Name()}, ${rs2Name()}")
                                RVConst.FUNCT3_OPERATION -> Decoded(offset, binaryAsHex,  "addw ${rdName()}, ${rs1Name()}, ${rs2Name()}")
                                else -> Decoded(offset, binaryAsHex,  "[invalid]")
                            }
                        }
                    }
                }

                RVConst.OPC_ARITH_IMM -> {
                    val imm12 = imm12iType.toValue(Size.Bit12).toDec()

                    when (funct3) {
                        RVConst.FUNCT3_OPERATION -> Decoded(offset, binaryAsHex,  "addi ${rdName()}, ${rs1Name()}, $imm12")
                        RVConst.FUNCT3_SLT -> Decoded(offset, binaryAsHex,  "slti ${rdName()}, ${rs1Name()}, $imm12")
                        RVConst.FUNCT3_SLTU -> Decoded(offset, binaryAsHex,  "sltiu ${rdName()}, ${rs1Name()}, $imm12")
                        RVConst.FUNCT3_XOR -> Decoded(offset, binaryAsHex,  "xori ${rdName()}, ${rs1Name()}, $imm12")
                        RVConst.FUNCT3_OR -> Decoded(offset, binaryAsHex,  "ori ${rdName()}, ${rs1Name()}, $imm12")
                        RVConst.FUNCT3_AND -> Decoded(offset, binaryAsHex,  "andi ${rdName()}, ${rs1Name()}, $imm12")
                        RVConst.FUNCT3_SHIFT_LEFT -> Decoded(offset, binaryAsHex,  "slli ${rdName()}, ${rs1Name()}, $shamt")
                        RVConst.FUNCT3_SHIFT_RIGHT -> when (funct7) {
                            RVConst.FUNCT7_SHIFT_ARITH_OR_SUB -> Decoded(offset, binaryAsHex,  "srai ${rdName()}, ${rs1Name()}, $shamt")
                            else -> Decoded(offset, binaryAsHex,  "srli ${rdName()}, ${rs1Name()}, $shamt")
                        }

                        else -> Decoded(offset, binaryAsHex,  "[invalid]")
                    }
                }

                RVConst.OPC_ARITH_IMM_WORD -> {
                    val imm12 = imm12iType.toValue(Size.Bit12).toDec()

                    when (funct3) {
                        RVConst.FUNCT3_OPERATION -> Decoded(offset, binaryAsHex,  "addiw ${rdName()}, ${rs1Name()}, $imm12")
                        RVConst.FUNCT3_SHIFT_LEFT -> Decoded(offset, binaryAsHex,  "slliw ${rdName()}, ${rs1Name()}, $shamt")
                        RVConst.FUNCT3_SHIFT_RIGHT -> when (funct7) {
                            RVConst.FUNCT7_SHIFT_ARITH_OR_SUB -> Decoded(offset, binaryAsHex,  "sraiw ${rdName()}, ${rs1Name()}, $shamt")
                            else -> Decoded(offset, binaryAsHex,  "srliw ${rdName()}, ${rs1Name()}, $shamt")
                        }

                        else -> Decoded(offset, binaryAsHex,  "[invalid]")
                    }
                }

                else -> Decoded(offset, binaryAsHex,  "[invalid]")
            }
        }

        fun csrName(): String = RVCsr.regs.firstOrNull { it.address == imm12iType }?.displayName ?: "[invalid csr]"
        fun rdName(): String = RVBaseRegs.entries.getOrNull(rd.toInt())?.displayName ?: "[invalid reg]"
        fun rs1Name(): String = RVBaseRegs.entries.getOrNull(rs1.toInt())?.displayName ?: "[invalid reg]"
        fun rs2Name(): String = RVBaseRegs.entries.getOrNull(rs2.toInt())?.displayName ?: "[invalid reg]"
    }

}