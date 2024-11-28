package emulator.archs


import cengine.lang.asm.ast.target.riscv.RVDisassembler
import cengine.util.integer.Size
import cengine.util.integer.Value.Companion.toValue
import cengine.util.integer.signExtend
import emulator.archs.riscv32.RV32
import emulator.archs.riscv64.RV64
import emulator.kit.MicroSetup
import emulator.kit.memory.Memory
import emulator.kit.optional.BasicArchImpl

class ArchRV32 : BasicArchImpl(RV32.config) {

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
        val instrBin = instrMemory.load(currentPc, RV32.WORD_WIDTH.byteCount, tracker).toBin().toULong().toUInt()

        // DC
        val decoded = RVDisassembler.RVInstrInfoProvider(instrBin)

        // EX
        when (decoded.type) {
            RVDisassembler.InstrType.LUI -> {
                val rd = getRegByAddr(decoded.rd) ?: return ExecutionResult(false)
                rd.set((decoded.imm20uType shl 12).toInt().toValue())
                incPCBy4()
            }

            RVDisassembler.InstrType.AUIPC -> {
                val rd = getRegByAddr(decoded.rd) ?: return ExecutionResult(false)
                val result = currentPc.toInt() + (decoded.imm20uType shl 12).toInt()
                rd.set(result.toValue())
                incPCBy4()
            }

            RVDisassembler.InstrType.JAL -> {
                val rd = getRegByAddr(decoded.rd) ?: return ExecutionResult(false)
                rd.set(currentPc + 4U.toValue())
                val target = currentPc.toInt() + decoded.jTypeOffset.toInt()
                pc.set(target.toValue())
            }

            RVDisassembler.InstrType.JALR -> {
                val rd = getRegByAddr(decoded.rd) ?: return ExecutionResult(false)
                val rs1 = getRegByAddr(decoded.rs1) ?: return ExecutionResult(false)
                rd.set(currentPc + 4U.toValue())
                val target = rs1.get().toUInt() + decoded.imm12iType.toInt().signExtend(12).toUInt() and (-1 shl 1).toUInt()
                pc.set(target.toValue())
            }

            RVDisassembler.InstrType.ECALL -> {

            }

            RVDisassembler.InstrType.EBREAK -> {

            }

            RVDisassembler.InstrType.BEQ -> {
                val rs1 = getRegByAddr(decoded.rs1) ?: return ExecutionResult(false)
                val rs2 = getRegByAddr(decoded.rs2) ?: return ExecutionResult(false)
                val target = (currentPc.toInt() + decoded.bTypeOffset.toInt()).toUInt().toValue()
                if (rs1.get() == rs2.get()) {
                    pc.set(target)
                } else {
                    incPCBy4()
                }
            }

            RVDisassembler.InstrType.BNE -> {
                val rs1 = getRegByAddr(decoded.rs1) ?: return ExecutionResult(false)
                val rs2 = getRegByAddr(decoded.rs2) ?: return ExecutionResult(false)
                val target = (currentPc.toInt() + decoded.bTypeOffset.toInt()).toUInt().toValue()
                if (rs1.get() != rs2.get()) {
                    pc.set(target)
                } else {
                    incPCBy4()
                }
            }

            RVDisassembler.InstrType.BLT -> {
                val rs1 = getRegByAddr(decoded.rs1) ?: return ExecutionResult(false)
                val rs2 = getRegByAddr(decoded.rs2) ?: return ExecutionResult(false)
                val target = (currentPc.toInt() + decoded.bTypeOffset.toInt()).toUInt().toValue()
                if (rs1.get().toInt() < rs2.get().toInt()) {
                    pc.set(target)
                } else {
                    incPCBy4()
                }
            }

            RVDisassembler.InstrType.BGE -> {
                val rs1 = getRegByAddr(decoded.rs1) ?: return ExecutionResult(false)
                val rs2 = getRegByAddr(decoded.rs2) ?: return ExecutionResult(false)
                val target = (currentPc.toInt() + decoded.bTypeOffset.toInt()).toUInt().toValue()
                if (rs1.get().toInt() >= rs2.get().toInt()) {
                    pc.set(target)
                } else {
                    incPCBy4()
                }
            }

            RVDisassembler.InstrType.BLTU -> {
                val rs1 = getRegByAddr(decoded.rs1) ?: return ExecutionResult(false)
                val rs2 = getRegByAddr(decoded.rs2) ?: return ExecutionResult(false)
                val target = (currentPc.toInt() + decoded.bTypeOffset.toInt()).toUInt().toValue()
                if (rs1.get().toUInt() < rs2.get().toUInt()) {
                    pc.set(target)
                } else {
                    incPCBy4()
                }
            }

            RVDisassembler.InstrType.BGEU -> {
                val rs1 = getRegByAddr(decoded.rs1) ?: return ExecutionResult(false)
                val rs2 = getRegByAddr(decoded.rs2) ?: return ExecutionResult(false)
                val target = (currentPc.toInt() + decoded.bTypeOffset.toInt()).toUInt().toValue()
                if (rs1.get().toUInt() >= rs2.get().toUInt()) {
                    pc.set(target)
                } else {
                    incPCBy4()
                }
            }

            RVDisassembler.InstrType.LB -> {
                val rd = getRegByAddr(decoded.rd) ?: return ExecutionResult(false)
                val rs1 = getRegByAddr(decoded.rs1) ?: return ExecutionResult(false)
                val address = (rs1.get().toInt() + decoded.iTypeImm.toInt()).toUInt().toValue()
                val loaded = dataMemory.load(address, tracker = tracker).toInt().signExtend(8)
                rd.set(loaded.toValue())
                incPCBy4()
            }

            RVDisassembler.InstrType.LH -> {
                val rd = getRegByAddr(decoded.rd) ?: return ExecutionResult(false)
                val rs1 = getRegByAddr(decoded.rs1) ?: return ExecutionResult(false)
                val address = (rs1.get().toInt() + decoded.iTypeImm.toInt()).toUInt().toValue()
                val loaded = dataMemory.load(address, amount = 2, tracker = tracker).toInt().signExtend(16)
                rd.set(loaded.toValue())
                incPCBy4()
            }

            RVDisassembler.InstrType.LW -> {
                val rd = getRegByAddr(decoded.rd) ?: return ExecutionResult(false)
                val rs1 = getRegByAddr(decoded.rs1) ?: return ExecutionResult(false)
                val address = (rs1.get().toInt() + decoded.iTypeImm.toInt()).toUInt().toValue()
                val loaded = dataMemory.load(address, amount = 4, tracker = tracker).toInt()
                rd.set(loaded.toValue())
                incPCBy4()
            }

            RVDisassembler.InstrType.LBU -> {
                val rd = getRegByAddr(decoded.rd) ?: return ExecutionResult(false)
                val rs1 = getRegByAddr(decoded.rs1) ?: return ExecutionResult(false)
                val address = (rs1.get().toInt() + decoded.iTypeImm.toInt()).toUInt().toValue()
                val loaded = dataMemory.load(address, tracker = tracker).toUInt()
                rd.set(loaded.toValue())
                incPCBy4()
            }

            RVDisassembler.InstrType.LHU -> {
                val rd = getRegByAddr(decoded.rd) ?: return ExecutionResult(false)
                val rs1 = getRegByAddr(decoded.rs1) ?: return ExecutionResult(false)
                val address = (rs1.get().toInt() + decoded.iTypeImm.toInt()).toUInt().toValue()
                val loaded = dataMemory.load(address, amount = 2, tracker = tracker).toUInt()
                rd.set(loaded.toValue())
                incPCBy4()
            }

            RVDisassembler.InstrType.SB -> {
                val rs1 = getRegByAddr(decoded.rs1) ?: return ExecutionResult(false)
                val rs2 = getRegByAddr(decoded.rs2) ?: return ExecutionResult(false)
                val address = (rs1.get().toInt() + decoded.sTypeImm.toInt()).toUInt().toValue()
                val value = rs2.get().toUInt().toUByte().toValue()
                dataMemory.store(address, value, tracker = tracker)
                incPCBy4()
            }

            RVDisassembler.InstrType.SH -> {
                val rs1 = getRegByAddr(decoded.rs1) ?: return ExecutionResult(false)
                val rs2 = getRegByAddr(decoded.rs2) ?: return ExecutionResult(false)
                val address = (rs1.get().toInt() + decoded.sTypeImm.toInt()).toUInt().toValue()
                val value = rs2.get().toUInt().toUShort().toValue()
                dataMemory.store(address, value, tracker = tracker)
                incPCBy4()
            }

            RVDisassembler.InstrType.SW -> {
                val rs1 = getRegByAddr(decoded.rs1) ?: return ExecutionResult(false)
                val rs2 = getRegByAddr(decoded.rs2) ?: return ExecutionResult(false)
                val address = (rs1.get().toInt() + decoded.sTypeImm.toInt()).toUInt().toValue()
                val value = rs2.get().toUInt().toValue()
                dataMemory.store(address, value, tracker = tracker)
                incPCBy4()
            }

            RVDisassembler.InstrType.ADDI -> {
                val rd = getRegByAddr(decoded.rd) ?: return ExecutionResult(false)
                val rs1 = getRegByAddr(decoded.rs1) ?: return ExecutionResult(false)
                val result = rs1.get() + decoded.iTypeImm.toUInt().toValue()
                rd.set(result)
                incPCBy4()
            }

            RVDisassembler.InstrType.SLTI -> {
                val rd = getRegByAddr(decoded.rd) ?: return ExecutionResult(false)
                val rs1 = getRegByAddr(decoded.rs1) ?: return ExecutionResult(false)
                if (rs1.get().toInt() < decoded.iTypeImm.toInt()) {
                    rd.set(1.toValue(Size.Bit32))
                } else {
                    rd.set(0.toValue(Size.Bit32))
                }
                incPCBy4()
            }

            RVDisassembler.InstrType.SLTIU -> {
                val rd = getRegByAddr(decoded.rd) ?: return ExecutionResult(false)
                val rs1 = getRegByAddr(decoded.rs1) ?: return ExecutionResult(false)
                if (rs1.get().toUInt() < decoded.imm12iType) {
                    rd.set(1.toValue(Size.Bit32))
                } else {
                    rd.set(0.toValue(Size.Bit32))
                }
                incPCBy4()
            }

            RVDisassembler.InstrType.XORI -> {
                val rd = getRegByAddr(decoded.rd) ?: return ExecutionResult(false)
                val rs1 = getRegByAddr(decoded.rs1) ?: return ExecutionResult(false)
                val result = rs1.get().toUInt() xor decoded.iTypeImm.toUInt()
                rd.set(result.toValue())
                incPCBy4()
            }

            RVDisassembler.InstrType.ORI -> {
                val rd = getRegByAddr(decoded.rd) ?: return ExecutionResult(false)
                val rs1 = getRegByAddr(decoded.rs1) ?: return ExecutionResult(false)
                val result = rs1.get().toUInt() or decoded.iTypeImm.toUInt()
                rd.set(result.toValue())
                incPCBy4()
            }

            RVDisassembler.InstrType.ANDI -> {
                val rd = getRegByAddr(decoded.rd) ?: return ExecutionResult(false)
                val rs1 = getRegByAddr(decoded.rs1) ?: return ExecutionResult(false)
                val result = rs1.get().toUInt() and decoded.iTypeImm.toUInt()
                rd.set(result.toValue())
                incPCBy4()
            }

            RVDisassembler.InstrType.SLLI -> {
                val rd = getRegByAddr(decoded.rd) ?: return ExecutionResult(false)
                val rs1 = getRegByAddr(decoded.rs1) ?: return ExecutionResult(false)
                val result = rs1.get().toUInt() shl decoded.shamt.toInt()
                rd.set(result.toValue())
                incPCBy4()
            }

            RVDisassembler.InstrType.SRLI -> {
                val rd = getRegByAddr(decoded.rd) ?: return ExecutionResult(false)
                val rs1 = getRegByAddr(decoded.rs1) ?: return ExecutionResult(false)
                val result = rs1.get().toUInt() shr decoded.shamt.toInt()
                rd.set(result.toValue())
                incPCBy4()
            }

            RVDisassembler.InstrType.SRAI -> {
                val rd = getRegByAddr(decoded.rd) ?: return ExecutionResult(false)
                val rs1 = getRegByAddr(decoded.rs1) ?: return ExecutionResult(false)
                val result = rs1.get().toInt() shr decoded.shamt.toInt()
                rd.set(result.toValue())
                incPCBy4()
            }

            RVDisassembler.InstrType.ADD -> {
                val rd = getRegByAddr(decoded.rd) ?: return ExecutionResult(false)
                val rs1 = getRegByAddr(decoded.rs1) ?: return ExecutionResult(false)
                val rs2 = getRegByAddr(decoded.rs2) ?: return ExecutionResult(false)
                val result = rs1.get() + rs2.get()
                rd.set(result)
                incPCBy4()
            }

            RVDisassembler.InstrType.SUB -> {
                val rd = getRegByAddr(decoded.rd) ?: return ExecutionResult(false)
                val rs1 = getRegByAddr(decoded.rs1) ?: return ExecutionResult(false)
                val rs2 = getRegByAddr(decoded.rs2) ?: return ExecutionResult(false)
                val result = rs1.get() - rs2.get()
                rd.set(result)
                incPCBy4()
            }

            RVDisassembler.InstrType.SLL -> {
                val rd = getRegByAddr(decoded.rd) ?: return ExecutionResult(false)
                val rs1 = getRegByAddr(decoded.rs1) ?: return ExecutionResult(false)
                val rs2 = getRegByAddr(decoded.rs2) ?: return ExecutionResult(false)
                val result = (rs1.get().toUInt() shl rs2.get().toUInt().toInt())
                rd.set(result.toValue())
                incPCBy4()
            }

            RVDisassembler.InstrType.SLT -> {
                val rd = getRegByAddr(decoded.rd) ?: return ExecutionResult(false)
                val rs1 = getRegByAddr(decoded.rs1) ?: return ExecutionResult(false)
                val rs2 = getRegByAddr(decoded.rs2) ?: return ExecutionResult(false)
                if (rs1.get().toInt() < rs2.get().toInt()) {
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
                if (rs1.get().toUInt() < rs2.get().toUInt()) {
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
                val result = (rs1.get().toUInt() xor rs2.get().toUInt())
                rd.set(result.toValue())
                incPCBy4()
            }

            RVDisassembler.InstrType.SRL -> {
                val rd = getRegByAddr(decoded.rd) ?: return ExecutionResult(false)
                val rs1 = getRegByAddr(decoded.rs1) ?: return ExecutionResult(false)
                val rs2 = getRegByAddr(decoded.rs2) ?: return ExecutionResult(false)
                val result = (rs1.get().toUInt() shr rs2.get().toUInt().toInt())
                rd.set(result.toValue())
                incPCBy4()
            }

            RVDisassembler.InstrType.SRA -> {
                val rd = getRegByAddr(decoded.rd) ?: return ExecutionResult(false)
                val rs1 = getRegByAddr(decoded.rs1) ?: return ExecutionResult(false)
                val rs2 = getRegByAddr(decoded.rs2) ?: return ExecutionResult(false)
                val result = (rs1.get().toInt() shr rs2.get().toUInt().toInt())
                rd.set(result.toValue())
                incPCBy4()
            }

            RVDisassembler.InstrType.OR -> {
                val rd = getRegByAddr(decoded.rd) ?: return ExecutionResult(false)
                val rs1 = getRegByAddr(decoded.rs1) ?: return ExecutionResult(false)
                val rs2 = getRegByAddr(decoded.rs2) ?: return ExecutionResult(false)
                val result = (rs1.get().toUInt() or rs2.get().toUInt())
                rd.set(result.toValue())
                incPCBy4()
            }

            RVDisassembler.InstrType.AND -> {
                val rd = getRegByAddr(decoded.rd) ?: return ExecutionResult(false)
                val rs1 = getRegByAddr(decoded.rs1) ?: return ExecutionResult(false)
                val rs2 = getRegByAddr(decoded.rs2) ?: return ExecutionResult(false)
                val result = (rs1.get().toUInt() and rs2.get().toUInt())
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

                val t = csr.get().toInt()
                rd.set(t.toValue())
                csr.set(rs1.get())

                incPCBy4()
            }

            RVDisassembler.InstrType.CSRRS -> {
                val rd = getRegByAddr(decoded.rd) ?: return ExecutionResult(false)
                val rs1 = getRegByAddr(decoded.rs1) ?: return ExecutionResult(false)
                val csr = getRegByAddr(decoded.imm12iType, RV64.CSR_REGFILE_NAME) ?: return ExecutionResult(false)

                val t = csr.get().toInt()
                rd.set(t.toValue())
                csr.set(rs1.get().toBin() or csr.get().toBin())

                incPCBy4()
            }

            RVDisassembler.InstrType.CSRRC -> {
                val rd = getRegByAddr(decoded.rd) ?: return ExecutionResult(false)
                val rs1 = getRegByAddr(decoded.rs1) ?: return ExecutionResult(false)
                val csr = getRegByAddr(decoded.imm12iType, RV64.CSR_REGFILE_NAME) ?: return ExecutionResult(false)

                val t = csr.get().toInt()
                rd.set(t.toValue())
                csr.set(rs1.get().toBin() and csr.get().toBin())

                incPCBy4()
            }

            RVDisassembler.InstrType.CSRRWI -> {
                val rd = getRegByAddr(decoded.rd) ?: return ExecutionResult(false)
                val csr = getRegByAddr(decoded.imm12iType, RV64.CSR_REGFILE_NAME) ?: return ExecutionResult(false)

                val t = csr.get().toInt()
                rd.set(t.toValue())
                csr.set(decoded.rs1.toValue())

                incPCBy4()
            }

            RVDisassembler.InstrType.CSRRSI -> {
                val rd = getRegByAddr(decoded.rd) ?: return ExecutionResult(false)
                val csr = getRegByAddr(decoded.imm12iType, RV64.CSR_REGFILE_NAME) ?: return ExecutionResult(false)

                val t = csr.get().toInt()
                rd.set(t.toValue())
                csr.set((t.toUInt() or decoded.rs1).toValue())

                incPCBy4()
            }

            RVDisassembler.InstrType.CSRRCI -> {
                val rd = getRegByAddr(decoded.rd) ?: return ExecutionResult(false)
                val csr = getRegByAddr(decoded.imm12iType, RV64.CSR_REGFILE_NAME) ?: return ExecutionResult(false)

                val t = csr.get().toInt()
                rd.set(t.toValue())
                csr.set((t.toUInt() and decoded.rs1.inv()).toValue())

                incPCBy4()
            }
            RVDisassembler.InstrType.MUL -> {
                val rd = getRegByAddr(decoded.rd) ?: return ExecutionResult(false)
                val rs1 = getRegByAddr(decoded.rs1) ?: return ExecutionResult(false)
                val rs2 = getRegByAddr(decoded.rs2) ?: return ExecutionResult(false)
                val result = (rs1.get().toInt() * rs2.get().toInt())
                rd.set(result.toValue())
                incPCBy4()
            }

            RVDisassembler.InstrType.MULH -> {
                val rd = getRegByAddr(decoded.rd) ?: return ExecutionResult(false)
                val rs1 = getRegByAddr(decoded.rs1) ?: return ExecutionResult(false)
                val rs2 = getRegByAddr(decoded.rs2) ?: return ExecutionResult(false)

                val a = rs1.get().toLong()
                val b = rs2.get().toLong()
                val result = (a * b).shr(32).toInt()

                rd.set(result.toValue())
                incPCBy4()
            }

            RVDisassembler.InstrType.MULHSU -> {
                val rd = getRegByAddr(decoded.rd) ?: return ExecutionResult(false)
                val rs1 = getRegByAddr(decoded.rs1) ?: return ExecutionResult(false)
                val rs2 = getRegByAddr(decoded.rs2) ?: return ExecutionResult(false)

                val a = rs1.get().toInt().toLong()
                val b = rs2.get().toUInt().toLong()
                val result = (a * b).shr(32).toInt()

                rd.set(result.toValue())
                incPCBy4()
            }

            RVDisassembler.InstrType.MULHU -> {
                val rd = getRegByAddr(decoded.rd) ?: return ExecutionResult(false)
                val rs1 = getRegByAddr(decoded.rs1) ?: return ExecutionResult(false)
                val rs2 = getRegByAddr(decoded.rs2) ?: return ExecutionResult(false)

                val a = rs1.get().toUInt().toULong()
                val b = rs2.get().toUInt().toULong()
                val result = (a * b).shr(32).toUInt()

                rd.set(result.toValue())
                incPCBy4()
            }

            RVDisassembler.InstrType.DIV -> {
                val rd = getRegByAddr(decoded.rd) ?: return ExecutionResult(false)
                val rs1 = getRegByAddr(decoded.rs1) ?: return ExecutionResult(false)
                val rs2 = getRegByAddr(decoded.rs2) ?: return ExecutionResult(false)

                val a = rs1.get().toInt()
                val b = rs2.get().toInt()
                val result = a / b

                rd.set(result.toValue())
                incPCBy4()
            }

            RVDisassembler.InstrType.DIVU -> {
                val rd = getRegByAddr(decoded.rd) ?: return ExecutionResult(false)
                val rs1 = getRegByAddr(decoded.rs1) ?: return ExecutionResult(false)
                val rs2 = getRegByAddr(decoded.rs2) ?: return ExecutionResult(false)

                val a = rs1.get().toUInt()
                val b = rs2.get().toUInt()
                val result = a / b

                rd.set(result.toValue())
                incPCBy4()
            }

            RVDisassembler.InstrType.REM -> {
                val rd = getRegByAddr(decoded.rd) ?: return ExecutionResult(false)
                val rs1 = getRegByAddr(decoded.rs1) ?: return ExecutionResult(false)
                val rs2 = getRegByAddr(decoded.rs2) ?: return ExecutionResult(false)

                val a = rs1.get().toInt()
                val b = rs2.get().toInt()
                val result = a % b

                rd.set(result.toValue())
                incPCBy4()
            }

            RVDisassembler.InstrType.REMU -> {
                val rd = getRegByAddr(decoded.rd) ?: return ExecutionResult(false)
                val rs1 = getRegByAddr(decoded.rs1) ?: return ExecutionResult(false)
                val rs2 = getRegByAddr(decoded.rs2) ?: return ExecutionResult(false)

                val a = rs1.get().toUInt()
                val b = rs2.get().toUInt()
                val result = a % b

                rd.set(result.toValue())
                incPCBy4()
            }

            null -> {
                console.error("Invalid Instruction!")
                return ExecutionResult(false)
            }

            else -> {
                console.error("${decoded.type} is not a ${description.name} instruction!")
                return ExecutionResult(false)
            }
        }

        val isReturnFromSubroutine = when (decoded.type) {
            RVDisassembler.InstrType.JALR -> true
            else -> false
        }
        val isBranchToSubroutine = when (decoded.type) {
            RVDisassembler.InstrType.JAL -> true
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