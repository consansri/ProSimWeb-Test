package cengine.lang.asm.ast.target.riscv

import cengine.lang.asm.Disassembler
import cengine.lang.asm.ast.target.riscv.RVDisassembler.InstrType.*
import cengine.util.integer.Hex
import cengine.util.integer.Size
import cengine.util.integer.Value.Companion.toValue
import cengine.util.integer.signExtend

object RVDisassembler : Disassembler() {
    override fun disassemble(startAddr: Hex, buffer: List<Hex>): List<Decoded> {
        var currIndex = 0
        var currInstr: RVInstrInfoProvider
        val decoded = mutableListOf<Decoded>()
        val words = buffer.chunked(4) { bytes -> bytes.reversed().joinToString("") { it.rawInput }.toUInt(16) }

        while ((currIndex / 4) < words.size) {
            currInstr = try {
                RVInstrInfoProvider(words[currIndex / 4])
            } catch (e: IndexOutOfBoundsException) {
                break
            }

            val instr = currInstr.decode(startAddr, currIndex.toULong())
            decoded.add(instr)

            // nativeLog("Instr: ${currInstr.binary.toString(16).padStart(32, '0')} -> ${currInstr.binaryAsHex}: ${instr.disassembled}")

            currIndex += 4
        }

        return decoded
    }

    data class RVInstrInfoProvider(val binary: UInt) : Disassembler.InstrProvider {
        val binaryAsHex: Hex = binary.toValue()
        val opcode = binary and 0b1111111U
        val funct3 = (binary shr 12) and 0b111U
        val funct7 = binary.shr(25) and 0b1111111U
        val rd = (binary shr 7) and 0b11111U
        val rs1 = (binary shr 15) and 0b11111U
        val rs2 = (binary shr 20) and 0b11111U
        val imm12iType = (binary shr 20) and 0b111111111111U
        val iTypeImm get() = imm12iType.toLong().signExtend(12)
        val imm12sType = ((binary shr 25) shl 5) or rd
        val sTypeImm get() = imm12sType.toLong().signExtend(12)
        val imm12bType = ((binary shr 31) shl 12) or (((binary shr 7) and 0b1U) shl 11) or (((binary shr 25) and 0b111111U) shl 5) or (((binary shr 8) and 0b1111U) shl 1)
        val bTypeOffset get() = imm12bType.toLong().signExtend(13)
        val imm20uType = binary shr 12
        val imm20jType = ((binary shr 31) shl 20) or (((binary shr 12) and 0b11111111U) shl 12) or (((binary shr 20) and 0b1U) shl 11) or (((binary shr 21) and 0b1111111111U) shl 1)
        val jTypeOffset get() = imm20jType.toLong().signExtend(21)
        val shamt = (imm12iType and 0b111111U)
        val pred = binary shr 24 and 0b1111U
        val succ = binary shr 20 and 0b1111U
        val type: InstrType? = when (opcode) {
            RVConst.OPC_LUI -> LUI
            RVConst.OPC_AUIPC -> AUIPC
            RVConst.OPC_JAL -> JAL
            RVConst.OPC_JALR -> JALR
            RVConst.OPC_OS -> {
                when (funct3) {
                    RVConst.FUNCT3_CSR_RW -> CSRRW
                    RVConst.FUNCT3_CSR_RS -> CSRRS
                    RVConst.FUNCT3_CSR_RC -> CSRRC
                    RVConst.FUNCT3_CSR_RWI -> CSRRWI
                    RVConst.FUNCT3_CSR_RSI -> CSRRSI
                    RVConst.FUNCT3_CSR_RCI -> CSRRCI
                    RVConst.FUNCT3_E -> when (imm12iType) {
                        1U -> EBREAK
                        else -> ECALL
                    }

                    else -> null
                }
            }

            RVConst.OPC_CBRA -> {
                when (funct3) {
                    RVConst.FUNCT3_CBRA_BEQ -> BEQ
                    RVConst.FUNCT3_CBRA_BNE -> BNE
                    RVConst.FUNCT3_CBRA_BLT -> BLT
                    RVConst.FUNCT3_CBRA_BGE -> BGE
                    RVConst.FUNCT3_CBRA_BLTU -> BLTU
                    RVConst.FUNCT3_CBRA_BGEU -> BGEU
                    else -> null
                }
            }

            RVConst.OPC_LOAD -> {
                when (funct3) {
                    RVConst.FUNCT3_LOAD_B -> LB
                    RVConst.FUNCT3_LOAD_H -> LH
                    RVConst.FUNCT3_LOAD_W -> LW
                    RVConst.FUNCT3_LOAD_D -> LD
                    RVConst.FUNCT3_LOAD_BU -> LBU
                    RVConst.FUNCT3_LOAD_HU -> LHU
                    RVConst.FUNCT3_LOAD_WU -> LWU
                    else -> null
                }
            }

            RVConst.OPC_STORE -> {
                when (funct3) {
                    RVConst.FUNCT3_STORE_B -> SB
                    RVConst.FUNCT3_STORE_H -> SH
                    RVConst.FUNCT3_STORE_W -> SW
                    RVConst.FUNCT3_STORE_D -> SD
                    else -> null
                }
            }

            RVConst.OPC_ARITH -> {
                when (funct7) {
                    RVConst.FUNCT7_SHIFT_ARITH_OR_SUB -> {
                        when (funct3) {
                            RVConst.FUNCT3_SHIFT_RIGHT -> SRA
                            RVConst.FUNCT3_OPERATION -> SUB
                            else -> null
                        }
                    }

                    RVConst.FUNCT7_M -> {
                        when (funct3) {
                            RVConst.FUNCT3_M_MUL -> MUL
                            RVConst.FUNCT3_M_MULH -> MULH
                            RVConst.FUNCT3_M_MULHSU -> MULHSU
                            RVConst.FUNCT3_M_MULHU -> MULHU
                            RVConst.FUNCT3_M_DIV -> DIV
                            RVConst.FUNCT3_M_DIVU -> DIVU
                            RVConst.FUNCT3_M_REM -> REM
                            RVConst.FUNCT3_M_REMU -> REMU
                            else -> null
                        }
                    }

                    else -> {
                        when (funct3) {
                            RVConst.FUNCT3_SHIFT_LEFT -> SLL
                            RVConst.FUNCT3_SHIFT_RIGHT -> SRL
                            RVConst.FUNCT3_SLT -> SLT
                            RVConst.FUNCT3_SLTU -> SLTU
                            RVConst.FUNCT3_OR -> OR
                            RVConst.FUNCT3_AND -> AND
                            RVConst.FUNCT3_XOR -> XOR
                            RVConst.FUNCT3_OPERATION -> ADD
                            else -> null
                        }
                    }
                }
            }

            RVConst.OPC_ARITH_WORD -> {
                when (funct7) {
                    RVConst.FUNCT7_SHIFT_ARITH_OR_SUB -> {
                        when (funct3) {
                            RVConst.FUNCT3_SHIFT_RIGHT -> SRAW
                            RVConst.FUNCT3_OPERATION -> SUBW
                            else -> null
                        }
                    }

                    RVConst.FUNCT7_M -> {
                        when (funct3) {
                            RVConst.FUNCT3_M_MUL -> MULW
                            RVConst.FUNCT3_M_DIV -> DIVW
                            RVConst.FUNCT3_M_DIVU -> DIVUW
                            RVConst.FUNCT3_M_REM -> REMW
                            RVConst.FUNCT3_M_REMU -> REMUW
                            else -> null
                        }
                    }

                    else -> {
                        when (funct3) {
                            RVConst.FUNCT3_SHIFT_LEFT -> SLLW
                            RVConst.FUNCT3_SHIFT_RIGHT -> SRLW
                            RVConst.FUNCT3_OPERATION -> ADDW
                            else -> null
                        }
                    }
                }
            }

            RVConst.OPC_ARITH_IMM -> {
                when (funct3) {
                    RVConst.FUNCT3_OPERATION -> ADDI
                    RVConst.FUNCT3_SLT -> SLTI
                    RVConst.FUNCT3_SLTU -> SLTIU
                    RVConst.FUNCT3_XOR -> XORI
                    RVConst.FUNCT3_OR -> ORI
                    RVConst.FUNCT3_AND -> ANDI
                    RVConst.FUNCT3_SHIFT_LEFT -> SLLI
                    RVConst.FUNCT3_SHIFT_RIGHT -> when (funct7) {
                        RVConst.FUNCT7_SHIFT_ARITH_OR_SUB -> SRAI
                        else -> SRLI
                    }

                    else -> null
                }
            }

            RVConst.OPC_ARITH_IMM_WORD -> {
                when (funct3) {
                    RVConst.FUNCT3_OPERATION -> ADDIW
                    RVConst.FUNCT3_SHIFT_LEFT -> SLLIW
                    RVConst.FUNCT3_SHIFT_RIGHT -> when (funct7) {
                        RVConst.FUNCT7_SHIFT_ARITH_OR_SUB -> SRAIW
                        else -> SRLIW
                    }

                    else -> null
                }
            }

            RVConst.OPC_FENCE -> {
                when (funct3) {
                    0U -> FENCE
                    RVConst.FUNCT3_FENCE_I -> FENCEI
                    else -> null
                }
            }

            else -> null
        }

        override fun decode(segmentAddr: Hex, offset: ULong): Decoded {
            return when (type) {
                LUI -> Decoded(offset, binaryAsHex, "lui    ${rdName()}, 0x${imm20uType.toString(16)}")
                AUIPC -> Decoded(offset, binaryAsHex, "auipc  ${rdName()}, 0x${imm20uType.toString(16)}")
                JAL -> {
                    val target = (segmentAddr.toLong() + offset.toLong() + jTypeOffset).toULong().toValue(segmentAddr.size)
                    Decoded(offset, binaryAsHex, "jal    ${rdName()}, $jTypeOffset", target)
                }

                JALR -> Decoded(offset, binaryAsHex, "jalr   ${rdName()}, ${rs1Name()}, ${imm12iType.toLong().signExtend(12)}")
                ECALL -> Decoded(offset, binaryAsHex, "ecall")
                EBREAK -> Decoded(offset, binaryAsHex, "ebreak")
                BEQ, BNE, BLT, BGE, BLTU, BGEU -> {
                    val target = (segmentAddr.toLong() + offset.toLong() + bTypeOffset).toULong().toValue(segmentAddr.size)
                    Decoded(offset, binaryAsHex, "${type.lc6char} ${rs1Name()}, ${rs2Name()}, $jTypeOffset", target)
                }

                LB, LH, LW, LD, LBU, LHU, LWU -> {
                    Decoded(offset, binaryAsHex, "${type.lc6char} ${rdName()}, $iTypeImm(${rs1Name()})")
                }

                SB, SH, SW, SD -> {
                    Decoded(offset, binaryAsHex, "${type.lc6char} ${rs2Name()}, $sTypeImm(${rs1Name()})")
                }

                ADDI, ADDIW, SLTI, SLTIU, XORI, ORI, ANDI -> {
                    val imm12 = imm12iType.toValue(Size.Bit12).toDec()
                    Decoded(offset, binaryAsHex, "${type.lc6char} ${rdName()}, ${rs1Name()}, $imm12")
                }

                SLLI, SLLIW, SRLI, SRLIW, SRAI, SRAIW -> Decoded(offset, binaryAsHex, "${type.lc6char} ${rdName()}, ${rs1Name()}, $shamt")
                ADD, ADDW, SUB, SUBW, SLL, SLLW,
                SLT, SLTU, XOR, SRL, SRLW, SRA,
                SRAW, OR, AND, MUL, MULH, MULHSU,
                MULHU, DIV, DIVU, REM, REMU, MULW,
                DIVW, DIVUW, REMW, REMUW,
                -> Decoded(offset, binaryAsHex, "${type.lc6char} ${rdName()}, ${rs1Name()}, ${rs2Name()}")

                FENCE -> Decoded(offset, binaryAsHex, "${type.lc6char} $pred,$succ")
                FENCEI -> Decoded(offset, binaryAsHex, type.lc6char)
                CSRRW, CSRRS, CSRRC -> Decoded(offset, binaryAsHex, "${type.lc6char} ${rdName()}, ${csrName()}, ${rs1Name()}")
                CSRRWI, CSRRSI, CSRRCI -> Decoded(offset, binaryAsHex, "${type.lc6char} ${rdName()}, ${csrName()}, 0b${rs1.toString(2)}")
                null -> Decoded(offset, binaryAsHex, "[invalid]")
            }
        }

        fun csrName(): String = RVCsr.regs.firstOrNull { it.numericalValue == imm12iType }?.displayName ?: "0x${imm12iType.toString(16)}"
        fun rdName(): String = RVBaseRegs.entries.getOrNull(rd.toInt())?.displayName ?: "[invalid reg]"
        fun rs1Name(): String = RVBaseRegs.entries.getOrNull(rs1.toInt())?.displayName ?: "[invalid reg]"
        fun rs2Name(): String = RVBaseRegs.entries.getOrNull(rs2.toInt())?.displayName ?: "[invalid reg]"
    }

    enum class InstrType(val special: String? = null) {
        LUI,
        AUIPC,
        JAL,
        JALR,
        ECALL,
        EBREAK,
        BEQ,
        BNE,
        BLT,
        BGE,
        BLTU,
        BGEU,
        LB,
        LH,
        LW,
        LD,
        LBU,
        LHU,
        LWU,
        SB,
        SH,
        SW,
        SD,
        ADDI,
        ADDIW,
        SLTI,
        SLTIU,
        XORI,
        ORI,
        ANDI,
        SLLI,
        SLLIW,
        SRLI,
        SRLIW,
        SRAI,
        SRAIW,
        ADD,
        ADDW,
        SUB,
        SUBW,
        SLL,
        SLLW,
        SLT,
        SLTU,
        XOR,
        SRL,
        SRLW,
        SRA,
        SRAW,
        OR,
        AND,
        FENCE,
        FENCEI("fence.i"),
        CSRRW,
        CSRRS,
        CSRRC,
        CSRRWI,
        CSRRSI,
        CSRRCI,
        MUL,
        MULH,
        MULHSU,
        MULHU,
        DIV,
        DIVU,
        REM,
        REMU,
        MULW,
        DIVW,
        DIVUW,
        REMW,
        REMUW;

        val lc6char: String = (special ?: name).lowercase().padEnd(6, ' ')
    }

}