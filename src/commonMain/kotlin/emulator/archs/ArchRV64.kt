package emulator.archs

import cengine.lang.asm.ast.target.riscv.RVConst.signExtend
import cengine.lang.asm.ast.target.riscv.RVDisassembler
import cengine.lang.asm.ast.target.riscv.RVDisassembler.InstrType.JAL
import cengine.lang.asm.ast.target.riscv.RVDisassembler.InstrType.JALR
import cengine.util.integer.Size
import cengine.util.integer.Value.Companion.toValue
import cengine.util.integer.multiplyWithHighLow
import emulator.archs.riscv64.RV64
import emulator.kit.MicroSetup
import emulator.kit.memory.Memory
import emulator.kit.optional.BasicArchImpl

class ArchRV64 : BasicArchImpl(RV64.config) {
    var instrMemory: Memory = memory
        set(value) {
            field = value
            resetMicroArch()
        }

    var dataMemory: Memory = memory
        set(value) {
            field = value
            resetMicroArch()
        }

    override fun executeNext(tracker: Memory.AccessTracker): ExecutionResult {
        // Shortcuts
        val pc = regContainer.pc

        // IF
        val currentPc = pc.get().toHex()
        val instrBin = instrMemory.load(currentPc, RV64.WORD_WIDTH.getByteCount(), tracker).toUInt()

        // DC
        val decoded = RVDisassembler.RVInstrInfoProvider(instrBin)

        // EX
        when (decoded.type) {
            RVDisassembler.InstrType.LUI -> {
                val rd = getRegByAddr(decoded.rd) ?: return ExecutionResult(false)
                rd.set((decoded.imm20uType shl 12).toInt().toLong().toValue())
                incPCBy4()
            }

            RVDisassembler.InstrType.AUIPC -> {
                val rd = getRegByAddr(decoded.rd) ?: return ExecutionResult(false)
                val result = currentPc.toLong() + (decoded.imm20uType shl 12).toInt().toLong()
                rd.set(result.toValue())
                incPCBy4()
            }

            RVDisassembler.InstrType.JAL -> {
                val rd = getRegByAddr(decoded.rd) ?: return ExecutionResult(false)
                rd.set(currentPc + 4U.toValue())
                val target = currentPc.toLong() + decoded.jTypeOffset
                pc.set(target.toValue())
            }

            RVDisassembler.InstrType.JALR -> {
                val rd = getRegByAddr(decoded.rd) ?: return ExecutionResult(false)
                val rs1 = getRegByAddr(decoded.rs1) ?: return ExecutionResult(false)
                rd.set(currentPc + 4U.toValue())
                val target = rs1.get().toULong() + decoded.imm12iType.toLong().signExtend(12).toULong() and (-1L shl 1).toULong()
                pc.set(target.toValue())
            }

            RVDisassembler.InstrType.ECALL -> {

            }

            RVDisassembler.InstrType.EBREAK -> {

            }

            RVDisassembler.InstrType.BEQ -> {
                val rs1 = getRegByAddr(decoded.rs1) ?: return ExecutionResult(false)
                val rs2 = getRegByAddr(decoded.rs2) ?: return ExecutionResult(false)
                val target = (currentPc.toLong() + decoded.bTypeOffset).toULong().toValue()
                if (rs1.get() == rs2.get()) {
                    pc.set(target)
                } else {
                    incPCBy4()
                }
            }

            RVDisassembler.InstrType.BNE -> {
                val rs1 = getRegByAddr(decoded.rs1) ?: return ExecutionResult(false)
                val rs2 = getRegByAddr(decoded.rs2) ?: return ExecutionResult(false)
                val target = (currentPc.toLong() + decoded.bTypeOffset).toULong().toValue()
                if (rs1.get() != rs2.get()) {
                    pc.set(target)
                } else {
                    incPCBy4()
                }
            }

            RVDisassembler.InstrType.BLT -> {
                val rs1 = getRegByAddr(decoded.rs1) ?: return ExecutionResult(false)
                val rs2 = getRegByAddr(decoded.rs2) ?: return ExecutionResult(false)
                val target = (currentPc.toLong() + decoded.bTypeOffset).toULong().toValue()
                if (rs1.get().toLong() < rs2.get().toLong()) {
                    pc.set(target)
                } else {
                    incPCBy4()
                }
            }

            RVDisassembler.InstrType.BGE -> {
                val rs1 = getRegByAddr(decoded.rs1) ?: return ExecutionResult(false)
                val rs2 = getRegByAddr(decoded.rs2) ?: return ExecutionResult(false)
                val target = (currentPc.toLong() + decoded.bTypeOffset).toULong().toValue()
                if (rs1.get().toLong() >= rs2.get().toLong()) {
                    pc.set(target)
                } else {
                    incPCBy4()
                }
            }

            RVDisassembler.InstrType.BLTU -> {
                val rs1 = getRegByAddr(decoded.rs1) ?: return ExecutionResult(false)
                val rs2 = getRegByAddr(decoded.rs2) ?: return ExecutionResult(false)
                val target = (currentPc.toLong() + decoded.bTypeOffset).toULong().toValue()
                if (rs1.get().toULong() < rs2.get().toULong()) {
                    pc.set(target)
                } else {
                    incPCBy4()
                }
            }

            RVDisassembler.InstrType.BGEU -> {
                val rs1 = getRegByAddr(decoded.rs1) ?: return ExecutionResult(false)
                val rs2 = getRegByAddr(decoded.rs2) ?: return ExecutionResult(false)
                val target = (currentPc.toLong() + decoded.bTypeOffset).toULong().toValue()
                if (rs1.get().toULong() >= rs2.get().toULong()) {
                    pc.set(target)
                } else {
                    incPCBy4()
                }
            }

            RVDisassembler.InstrType.LB -> {
                val rd = getRegByAddr(decoded.rd) ?: return ExecutionResult(false)
                val rs1 = getRegByAddr(decoded.rs1) ?: return ExecutionResult(false)
                val address = (rs1.get().toLong() + decoded.iTypeImm).toULong().toValue()
                val loaded = dataMemory.load(address, tracker = tracker).toLong().signExtend(8)
                rd.set(loaded.toValue())
                incPCBy4()
            }

            RVDisassembler.InstrType.LH -> {
                val rd = getRegByAddr(decoded.rd) ?: return ExecutionResult(false)
                val rs1 = getRegByAddr(decoded.rs1) ?: return ExecutionResult(false)
                val address = (rs1.get().toLong() + decoded.iTypeImm).toULong().toValue()
                val loaded = dataMemory.load(address, amount = 2, tracker = tracker).toLong().signExtend(16)
                rd.set(loaded.toValue())
                incPCBy4()
            }

            RVDisassembler.InstrType.LW -> {
                val rd = getRegByAddr(decoded.rd) ?: return ExecutionResult(false)
                val rs1 = getRegByAddr(decoded.rs1) ?: return ExecutionResult(false)
                val address = (rs1.get().toLong() + decoded.iTypeImm).toULong().toValue()
                val loaded = dataMemory.load(address, amount = 4, tracker = tracker).toLong().signExtend(32)
                rd.set(loaded.toValue())
                incPCBy4()
            }

            RVDisassembler.InstrType.LD -> {
                val rd = getRegByAddr(decoded.rd) ?: return ExecutionResult(false)
                val rs1 = getRegByAddr(decoded.rs1) ?: return ExecutionResult(false)
                val address = (rs1.get().toLong() + decoded.iTypeImm).toULong().toValue()
                val loaded = dataMemory.load(address, amount = 8, tracker = tracker)
                rd.set(loaded)
                incPCBy4()
            }

            RVDisassembler.InstrType.LBU -> {
                val rd = getRegByAddr(decoded.rd) ?: return ExecutionResult(false)
                val rs1 = getRegByAddr(decoded.rs1) ?: return ExecutionResult(false)
                val address = (rs1.get().toLong() + decoded.iTypeImm).toULong().toValue()
                val loaded = dataMemory.load(address, tracker = tracker).toULong()
                rd.set(loaded.toValue())
                incPCBy4()
            }

            RVDisassembler.InstrType.LHU -> {
                val rd = getRegByAddr(decoded.rd) ?: return ExecutionResult(false)
                val rs1 = getRegByAddr(decoded.rs1) ?: return ExecutionResult(false)
                val address = (rs1.get().toLong() + decoded.iTypeImm).toULong().toValue()
                val loaded = dataMemory.load(address, amount = 2, tracker = tracker).toULong()
                rd.set(loaded.toValue())
                incPCBy4()
            }

            RVDisassembler.InstrType.LWU -> {
                val rd = getRegByAddr(decoded.rd) ?: return ExecutionResult(false)
                val rs1 = getRegByAddr(decoded.rs1) ?: return ExecutionResult(false)
                val address = (rs1.get().toLong() + decoded.iTypeImm).toULong().toValue()
                val loaded = dataMemory.load(address, amount = 4, tracker = tracker).toULong()
                rd.set(loaded.toValue())
                incPCBy4()
            }

            RVDisassembler.InstrType.SB -> {
                val rs1 = getRegByAddr(decoded.rs1) ?: return ExecutionResult(false)
                val rs2 = getRegByAddr(decoded.rs2) ?: return ExecutionResult(false)
                val address = (rs1.get().toLong() + decoded.sTypeImm).toULong().toValue()
                val value = rs2.get().toULong().toUByte().toValue()
                dataMemory.store(address, value, tracker = tracker)
                incPCBy4()
            }

            RVDisassembler.InstrType.SH -> {
                val rs1 = getRegByAddr(decoded.rs1) ?: return ExecutionResult(false)
                val rs2 = getRegByAddr(decoded.rs2) ?: return ExecutionResult(false)
                val address = (rs1.get().toLong() + decoded.sTypeImm).toULong().toValue()
                val value = rs2.get().toULong().toUShort().toValue()
                dataMemory.store(address, value, tracker = tracker)
                incPCBy4()
            }

            RVDisassembler.InstrType.SW -> {
                val rs1 = getRegByAddr(decoded.rs1) ?: return ExecutionResult(false)
                val rs2 = getRegByAddr(decoded.rs2) ?: return ExecutionResult(false)
                val address = (rs1.get().toLong() + decoded.sTypeImm).toULong().toValue()
                val value = rs2.get().toULong().toUInt().toValue()
                dataMemory.store(address, value, tracker = tracker)
                incPCBy4()
            }

            RVDisassembler.InstrType.SD -> {
                val rs1 = getRegByAddr(decoded.rs1) ?: return ExecutionResult(false)
                val rs2 = getRegByAddr(decoded.rs2) ?: return ExecutionResult(false)
                val address = (rs1.get().toLong() + decoded.sTypeImm).toULong().toValue()
                val value = rs2.get()
                dataMemory.store(address, value, tracker = tracker)
                incPCBy4()
            }

            RVDisassembler.InstrType.ADDI -> {
                val rd = getRegByAddr(decoded.rd) ?: return ExecutionResult(false)
                val rs1 = getRegByAddr(decoded.rs1) ?: return ExecutionResult(false)
                val result = rs1.get() + decoded.iTypeImm.toULong().toValue()
                rd.set(result)
                incPCBy4()
            }

            RVDisassembler.InstrType.ADDIW -> {
                val rd = getRegByAddr(decoded.rd) ?: return ExecutionResult(false)
                val rs1 = getRegByAddr(decoded.rs1) ?: return ExecutionResult(false)
                val result = rs1.get().toUInt() + decoded.iTypeImm.toUInt()
                rd.set(result.toLong().signExtend(32).toValue())
                incPCBy4()
            }

            RVDisassembler.InstrType.SLTI -> {
                val rd = getRegByAddr(decoded.rd) ?: return ExecutionResult(false)
                val rs1 = getRegByAddr(decoded.rs1) ?: return ExecutionResult(false)
                if (rs1.get().toLong() < decoded.iTypeImm) {
                    rd.set(1.toValue(Size.Bit64))
                } else {
                    rd.set(0.toValue(Size.Bit64))
                }
                incPCBy4()
            }

            RVDisassembler.InstrType.SLTIU -> {
                val rd = getRegByAddr(decoded.rd) ?: return ExecutionResult(false)
                val rs1 = getRegByAddr(decoded.rs1) ?: return ExecutionResult(false)
                if (rs1.get().toULong() < decoded.imm12iType.toULong()) {
                    rd.set(1.toValue(Size.Bit64))
                } else {
                    rd.set(0.toValue(Size.Bit64))
                }
                incPCBy4()
            }

            RVDisassembler.InstrType.XORI -> {
                val rd = getRegByAddr(decoded.rd) ?: return ExecutionResult(false)
                val rs1 = getRegByAddr(decoded.rs1) ?: return ExecutionResult(false)
                val result = rs1.get().toULong() xor decoded.iTypeImm.toULong()
                rd.set(result.toValue())
                incPCBy4()
            }

            RVDisassembler.InstrType.ORI -> {
                val rd = getRegByAddr(decoded.rd) ?: return ExecutionResult(false)
                val rs1 = getRegByAddr(decoded.rs1) ?: return ExecutionResult(false)
                val result = rs1.get().toULong() or decoded.iTypeImm.toULong()
                rd.set(result.toValue())
                incPCBy4()
            }

            RVDisassembler.InstrType.ANDI -> {
                val rd = getRegByAddr(decoded.rd) ?: return ExecutionResult(false)
                val rs1 = getRegByAddr(decoded.rs1) ?: return ExecutionResult(false)
                val result = rs1.get().toULong() and decoded.iTypeImm.toULong()
                rd.set(result.toValue())
                incPCBy4()
            }

            RVDisassembler.InstrType.SLLI -> {
                val rd = getRegByAddr(decoded.rd) ?: return ExecutionResult(false)
                val rs1 = getRegByAddr(decoded.rs1) ?: return ExecutionResult(false)
                val result = rs1.get().toULong() shl decoded.shamt.toInt()
                rd.set(result.toValue())
                incPCBy4()
            }

            RVDisassembler.InstrType.SLLIW -> {
                val rd = getRegByAddr(decoded.rd) ?: return ExecutionResult(false)
                val rs1 = getRegByAddr(decoded.rs1) ?: return ExecutionResult(false)
                val result = rs1.get().toULong().toUInt() shl decoded.shamt.toInt()
                rd.set(result.toValue())
                incPCBy4()
            }

            RVDisassembler.InstrType.SRLI -> {
                val rd = getRegByAddr(decoded.rd) ?: return ExecutionResult(false)
                val rs1 = getRegByAddr(decoded.rs1) ?: return ExecutionResult(false)
                val result = rs1.get().toULong() shr decoded.shamt.toInt()
                rd.set(result.toValue())
                incPCBy4()
            }

            RVDisassembler.InstrType.SRLIW -> {
                val rd = getRegByAddr(decoded.rd) ?: return ExecutionResult(false)
                val rs1 = getRegByAddr(decoded.rs1) ?: return ExecutionResult(false)
                val result = rs1.get().toULong().toUInt() shr decoded.shamt.toInt()
                rd.set(result.toValue())
                incPCBy4()
            }

            RVDisassembler.InstrType.SRAI -> {
                val rd = getRegByAddr(decoded.rd) ?: return ExecutionResult(false)
                val rs1 = getRegByAddr(decoded.rs1) ?: return ExecutionResult(false)
                val result = rs1.get().toLong() shr decoded.shamt.toInt()
                rd.set(result.toValue())
                incPCBy4()
            }

            RVDisassembler.InstrType.SRAIW -> {
                val rd = getRegByAddr(decoded.rd) ?: return ExecutionResult(false)
                val rs1 = getRegByAddr(decoded.rs1) ?: return ExecutionResult(false)
                val result = rs1.get().toLong().toInt() shr decoded.shamt.toInt()
                rd.set(result.toValue())
                incPCBy4()
            }

            RVDisassembler.InstrType.ADD -> {
                val rd = getRegByAddr(decoded.rd) ?: return ExecutionResult(false)
                val rs1 = getRegByAddr(decoded.rs1) ?: return ExecutionResult(false)
                val rs2 = getRegByAddr(decoded.rs2) ?: return ExecutionResult(false)
                val result = rs1.get().toULong() + rs2.get().toULong()
                rd.set(result.toValue())
                incPCBy4()
            }

            RVDisassembler.InstrType.ADDW -> {
                val rd = getRegByAddr(decoded.rd) ?: return ExecutionResult(false)
                val rs1 = getRegByAddr(decoded.rs1) ?: return ExecutionResult(false)
                val rs2 = getRegByAddr(decoded.rs2) ?: return ExecutionResult(false)
                val result = (rs1.get().toULong() + rs2.get().toULong()).toInt().toLong()
                rd.set(result.toValue())
                incPCBy4()
            }

            RVDisassembler.InstrType.SUB -> {
                val rd = getRegByAddr(decoded.rd) ?: return ExecutionResult(false)
                val rs1 = getRegByAddr(decoded.rs1) ?: return ExecutionResult(false)
                val rs2 = getRegByAddr(decoded.rs2) ?: return ExecutionResult(false)
                val result = rs1.get().toULong() - rs2.get().toULong()
                rd.set(result.toValue())
                incPCBy4()
            }

            RVDisassembler.InstrType.SUBW -> {
                val rd = getRegByAddr(decoded.rd) ?: return ExecutionResult(false)
                val rs1 = getRegByAddr(decoded.rs1) ?: return ExecutionResult(false)
                val rs2 = getRegByAddr(decoded.rs2) ?: return ExecutionResult(false)
                val result = (rs1.get().toULong() - rs2.get().toULong()).toInt().toLong()
                rd.set(result.toValue())
                incPCBy4()
            }

            RVDisassembler.InstrType.SLL -> {
                val rd = getRegByAddr(decoded.rd) ?: return ExecutionResult(false)
                val rs1 = getRegByAddr(decoded.rs1) ?: return ExecutionResult(false)
                val rs2 = getRegByAddr(decoded.rs2) ?: return ExecutionResult(false)
                val result = (rs1.get().toULong() shl rs2.get().toULong().toInt())
                rd.set(result.toValue())
                incPCBy4()
            }

            RVDisassembler.InstrType.SLLW -> {
                val rd = getRegByAddr(decoded.rd) ?: return ExecutionResult(false)
                val rs1 = getRegByAddr(decoded.rs1) ?: return ExecutionResult(false)
                val rs2 = getRegByAddr(decoded.rs2) ?: return ExecutionResult(false)
                val result = (rs1.get().toULong() shl rs2.get().toULong().toInt()).toInt().toULong()
                rd.set(result.toValue())
                incPCBy4()
            }

            RVDisassembler.InstrType.SLT -> {
                val rd = getRegByAddr(decoded.rd) ?: return ExecutionResult(false)
                val rs1 = getRegByAddr(decoded.rs1) ?: return ExecutionResult(false)
                val rs2 = getRegByAddr(decoded.rs2) ?: return ExecutionResult(false)
                if (rs1.get().toLong() < rs2.get().toLong()) {
                    rd.set(1L.toValue())
                } else {
                    rd.set(0L.toValue())
                }
                incPCBy4()
            }

            RVDisassembler.InstrType.SLTU -> {
                val rd = getRegByAddr(decoded.rd) ?: return ExecutionResult(false)
                val rs1 = getRegByAddr(decoded.rs1) ?: return ExecutionResult(false)
                val rs2 = getRegByAddr(decoded.rs2) ?: return ExecutionResult(false)
                if (rs1.get().toULong() < rs2.get().toULong()) {
                    rd.set(1L.toValue())
                } else {
                    rd.set(0L.toValue())
                }
                incPCBy4()
            }

            RVDisassembler.InstrType.XOR -> {
                val rd = getRegByAddr(decoded.rd) ?: return ExecutionResult(false)
                val rs1 = getRegByAddr(decoded.rs1) ?: return ExecutionResult(false)
                val rs2 = getRegByAddr(decoded.rs2) ?: return ExecutionResult(false)
                val result = (rs1.get().toBin() xor rs2.get().toBin())
                rd.set(result)
                incPCBy4()
            }

            RVDisassembler.InstrType.SRL -> {
                val rd = getRegByAddr(decoded.rd) ?: return ExecutionResult(false)
                val rs1 = getRegByAddr(decoded.rs1) ?: return ExecutionResult(false)
                val rs2 = getRegByAddr(decoded.rs2) ?: return ExecutionResult(false)
                val result = (rs1.get().toULong() shr rs2.get().toULong().toInt())
                rd.set(result.toValue())
                incPCBy4()
            }

            RVDisassembler.InstrType.SRLW -> {
                val rd = getRegByAddr(decoded.rd) ?: return ExecutionResult(false)
                val rs1 = getRegByAddr(decoded.rs1) ?: return ExecutionResult(false)
                val rs2 = getRegByAddr(decoded.rs2) ?: return ExecutionResult(false)
                val result = (rs1.get().toULong().toUInt() shr rs2.get().toULong().toInt())
                rd.set(result.toValue())
                incPCBy4()
            }

            RVDisassembler.InstrType.SRA -> {
                val rd = getRegByAddr(decoded.rd) ?: return ExecutionResult(false)
                val rs1 = getRegByAddr(decoded.rs1) ?: return ExecutionResult(false)
                val rs2 = getRegByAddr(decoded.rs2) ?: return ExecutionResult(false)
                val result = (rs1.get().toLong() shr rs2.get().toULong().toInt())
                rd.set(result.toValue())
                incPCBy4()
            }

            RVDisassembler.InstrType.SRAW -> {
                val rd = getRegByAddr(decoded.rd) ?: return ExecutionResult(false)
                val rs1 = getRegByAddr(decoded.rs1) ?: return ExecutionResult(false)
                val rs2 = getRegByAddr(decoded.rs2) ?: return ExecutionResult(false)
                val result = (rs1.get().toLong().toInt() shr rs2.get().toULong().toInt())
                rd.set(result.toValue())
                incPCBy4()
            }

            RVDisassembler.InstrType.OR -> {
                val rd = getRegByAddr(decoded.rd) ?: return ExecutionResult(false)
                val rs1 = getRegByAddr(decoded.rs1) ?: return ExecutionResult(false)
                val rs2 = getRegByAddr(decoded.rs2) ?: return ExecutionResult(false)
                val result = (rs1.get().toULong() or rs2.get().toULong())
                rd.set(result.toValue())
                incPCBy4()
            }

            RVDisassembler.InstrType.AND -> {
                val rd = getRegByAddr(decoded.rd) ?: return ExecutionResult(false)
                val rs1 = getRegByAddr(decoded.rs1) ?: return ExecutionResult(false)
                val rs2 = getRegByAddr(decoded.rs2) ?: return ExecutionResult(false)
                val result = (rs1.get().toULong() and rs2.get().toULong())
                rd.set(result.toValue())
                incPCBy4()
            }

            RVDisassembler.InstrType.FENCE -> {

            }

            RVDisassembler.InstrType.FENCEI -> {

            }

            RVDisassembler.InstrType.CSRRW -> {
                val rd = getRegByAddr(decoded.rd) ?: return ExecutionResult(false)
                val rs1 = getRegByAddr(decoded.rs1) ?: return ExecutionResult(false)
                val csr = getRegByAddr(decoded.imm12iType, RV64.CSR_REGFILE_NAME) ?: return ExecutionResult(false)

                val t = csr.get().toLong()
                rd.set(t.toValue())
                csr.set(rs1.get())

                incPCBy4()
            }

            RVDisassembler.InstrType.CSRRS -> {
                val rd = getRegByAddr(decoded.rd) ?: return ExecutionResult(false)
                val rs1 = getRegByAddr(decoded.rs1) ?: return ExecutionResult(false)
                val csr = getRegByAddr(decoded.imm12iType, RV64.CSR_REGFILE_NAME) ?: return ExecutionResult(false)

                val t = csr.get().toLong()
                rd.set(t.toValue())
                csr.set(rs1.get().toBin() or csr.get().toBin())

                incPCBy4()
            }

            RVDisassembler.InstrType.CSRRC -> {
                val rd = getRegByAddr(decoded.rd) ?: return ExecutionResult(false)
                val rs1 = getRegByAddr(decoded.rs1) ?: return ExecutionResult(false)
                val csr = getRegByAddr(decoded.imm12iType, RV64.CSR_REGFILE_NAME) ?: return ExecutionResult(false)

                val t = csr.get().toLong()
                rd.set(t.toValue())
                csr.set(rs1.get().toBin() and csr.get().toBin())

                incPCBy4()
            }

            RVDisassembler.InstrType.CSRRWI -> {
                val rd = getRegByAddr(decoded.rd) ?: return ExecutionResult(false)
                val csr = getRegByAddr(decoded.imm12iType, RV64.CSR_REGFILE_NAME) ?: return ExecutionResult(false)

                val t = csr.get().toLong()
                rd.set(t.toValue())
                csr.set(decoded.rs1.toULong().toValue())

                incPCBy4()
            }

            RVDisassembler.InstrType.CSRRSI -> {
                val rd = getRegByAddr(decoded.rd) ?: return ExecutionResult(false)
                val csr = getRegByAddr(decoded.imm12iType, RV64.CSR_REGFILE_NAME) ?: return ExecutionResult(false)

                val t = csr.get().toLong()
                rd.set(t.toValue())
                csr.set((t.toULong() or decoded.rs1.toULong()).toValue())

                incPCBy4()
            }

            RVDisassembler.InstrType.CSRRCI -> {
                val rd = getRegByAddr(decoded.rd) ?: return ExecutionResult(false)
                val csr = getRegByAddr(decoded.imm12iType, RV64.CSR_REGFILE_NAME) ?: return ExecutionResult(false)

                val t = csr.get().toLong()
                rd.set(t.toValue())
                csr.set((t.toULong() and decoded.rs1.toULong().inv()).toValue())

                incPCBy4()
            }
            RVDisassembler.InstrType.MUL -> {
                val rd = getRegByAddr(decoded.rd) ?: return ExecutionResult(false)
                val rs1 = getRegByAddr(decoded.rs1) ?: return ExecutionResult(false)
                val rs2 = getRegByAddr(decoded.rs2) ?: return ExecutionResult(false)
                val result = (rs1.get().toLong() * rs2.get().toLong())
                rd.set(result.toValue())
                incPCBy4()
            }

            RVDisassembler.InstrType.MULH -> {
                val rd = getRegByAddr(decoded.rd) ?: return ExecutionResult(false)
                val rs1 = getRegByAddr(decoded.rs1) ?: return ExecutionResult(false)
                val rs2 = getRegByAddr(decoded.rs2) ?: return ExecutionResult(false)

                val a = rs1.get().toLong()
                val b = rs2.get().toLong()

                val result = a.multiplyWithHighLow(b).first

                rd.set(result.toValue())
                incPCBy4()
            }

            RVDisassembler.InstrType.MULHSU -> {
                val rd = getRegByAddr(decoded.rd) ?: return ExecutionResult(false)
                val rs1 = getRegByAddr(decoded.rs1) ?: return ExecutionResult(false)
                val rs2 = getRegByAddr(decoded.rs2) ?: return ExecutionResult(false)

                val a = rs1.get().toLong()
                val b = rs2.get().toULong()

                val result = a.multiplyWithHighLow(b).first

                rd.set(result.toValue())
                incPCBy4()
            }

            RVDisassembler.InstrType.MULHU -> {
                val rd = getRegByAddr(decoded.rd) ?: return ExecutionResult(false)
                val rs1 = getRegByAddr(decoded.rs1) ?: return ExecutionResult(false)
                val rs2 = getRegByAddr(decoded.rs2) ?: return ExecutionResult(false)

                val a = rs1.get().toULong()
                val b = rs2.get().toULong()

                val result = a.multiplyWithHighLow(b).first

                rd.set(result.toValue())
                incPCBy4()
            }

            RVDisassembler.InstrType.DIV -> {
                val rd = getRegByAddr(decoded.rd) ?: return ExecutionResult(false)
                val rs1 = getRegByAddr(decoded.rs1) ?: return ExecutionResult(false)
                val rs2 = getRegByAddr(decoded.rs2) ?: return ExecutionResult(false)

                val a = rs1.get().toLong()
                val b = rs2.get().toLong()
                val result = a / b

                rd.set(result.toValue())
                incPCBy4()
            }

            RVDisassembler.InstrType.DIVU -> {
                val rd = getRegByAddr(decoded.rd) ?: return ExecutionResult(false)
                val rs1 = getRegByAddr(decoded.rs1) ?: return ExecutionResult(false)
                val rs2 = getRegByAddr(decoded.rs2) ?: return ExecutionResult(false)

                val a = rs1.get().toULong()
                val b = rs2.get().toULong()
                val result = a / b

                rd.set(result.toValue())
                incPCBy4()
            }

            RVDisassembler.InstrType.REM -> {
                val rd = getRegByAddr(decoded.rd) ?: return ExecutionResult(false)
                val rs1 = getRegByAddr(decoded.rs1) ?: return ExecutionResult(false)
                val rs2 = getRegByAddr(decoded.rs2) ?: return ExecutionResult(false)

                val a = rs1.get().toLong()
                val b = rs2.get().toLong()
                val result = a % b

                rd.set(result.toValue())
                incPCBy4()
            }

            RVDisassembler.InstrType.REMU -> {
                val rd = getRegByAddr(decoded.rd) ?: return ExecutionResult(false)
                val rs1 = getRegByAddr(decoded.rs1) ?: return ExecutionResult(false)
                val rs2 = getRegByAddr(decoded.rs2) ?: return ExecutionResult(false)

                val a = rs1.get().toULong()
                val b = rs2.get().toULong()
                val result = a % b

                rd.set(result.toValue())
                incPCBy4()
            }

            RVDisassembler.InstrType.MULW -> {
                val rd = getRegByAddr(decoded.rd) ?: return ExecutionResult(false)
                val rs1 = getRegByAddr(decoded.rs1) ?: return ExecutionResult(false)
                val rs2 = getRegByAddr(decoded.rs2) ?: return ExecutionResult(false)
                val result = (rs1.get().toLong().toInt() * rs2.get().toLong().toInt()).toLong()
                rd.set(result.toValue())
                incPCBy4()
            }

            RVDisassembler.InstrType.DIVW -> {
                val rd = getRegByAddr(decoded.rd) ?: return ExecutionResult(false)
                val rs1 = getRegByAddr(decoded.rs1) ?: return ExecutionResult(false)
                val rs2 = getRegByAddr(decoded.rs2) ?: return ExecutionResult(false)

                val a = rs1.get().toLong().toInt()
                val b = rs2.get().toLong().toInt()
                val result = (a / b).toLong()

                rd.set(result.toValue())
                incPCBy4()
            }

            RVDisassembler.InstrType.DIVUW -> {
                val rd = getRegByAddr(decoded.rd) ?: return ExecutionResult(false)
                val rs1 = getRegByAddr(decoded.rs1) ?: return ExecutionResult(false)
                val rs2 = getRegByAddr(decoded.rs2) ?: return ExecutionResult(false)

                val a = rs1.get().toULong().toUInt()
                val b = rs2.get().toULong().toUInt()
                val result = (a / b).toULong()

                rd.set(result.toValue())
                incPCBy4()
            }

            RVDisassembler.InstrType.REMW -> {
                val rd = getRegByAddr(decoded.rd) ?: return ExecutionResult(false)
                val rs1 = getRegByAddr(decoded.rs1) ?: return ExecutionResult(false)
                val rs2 = getRegByAddr(decoded.rs2) ?: return ExecutionResult(false)

                val a = rs1.get().toLong().toInt()
                val b = rs2.get().toLong().toInt()
                val result = (a % b).toLong()

                rd.set(result.toValue())
                incPCBy4()
            }

            RVDisassembler.InstrType.REMUW -> {
                val rd = getRegByAddr(decoded.rd) ?: return ExecutionResult(false)
                val rs1 = getRegByAddr(decoded.rs1) ?: return ExecutionResult(false)
                val rs2 = getRegByAddr(decoded.rs2) ?: return ExecutionResult(false)

                val a = rs1.get().toULong().toUInt()
                val b = rs2.get().toULong().toUInt()
                val result = (a % b).toULong()

                rd.set(result.toValue())
                incPCBy4()
            }

            null -> {
                console.error("Invalid Instruction!")
                return ExecutionResult(false)
            }
        }

        val isReturnFromSubroutine = when (decoded.type) {
            JALR -> true
            else -> false
        }
        val isBranchToSubroutine = when (decoded.type) {
            JAL -> true
            else -> false
        }

        return ExecutionResult(true, typeIsReturnFromSubroutine = isReturnFromSubroutine, typeIsBranchToSubroutine = isBranchToSubroutine)
    }

    private fun incPCBy4() {
        regContainer.pc.inc(4U)
    }

    override fun setupMicroArch() {
        MicroSetup.append(memory)
        if (instrMemory != memory) MicroSetup.append(instrMemory)
        if (dataMemory != memory) MicroSetup.append(dataMemory)
    }
}