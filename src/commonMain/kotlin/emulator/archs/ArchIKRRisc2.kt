package emulator.archs

import cengine.lang.asm.ast.target.ikrrisc2.IKRR2Disassembler.IKRR2InstrProvider
import cengine.lang.asm.ast.target.ikrrisc2.IKRR2Disassembler.InstrType.*
import cengine.lang.asm.ast.target.riscv.RVConst.signExtend
import cengine.util.integer.Value.Companion.toValue
import cengine.util.integer.rol
import cengine.util.integer.ror
import emulator.archs.ikrrisc2.IKRRisc2
import emulator.kit.MicroSetup
import emulator.kit.memory.Memory
import emulator.kit.optional.BasicArchImpl

class ArchIKRRisc2 : BasicArchImpl(IKRRisc2.config) {
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
        val loaded = instrMemory.load(regContainer.pc.get().toHex(), tracker = tracker)
        val pc = regContainer.pc
        val decoded = IKRR2InstrProvider(loaded.toUInt())

        when (decoded.type) {
            ADD -> {
                val ra = getRegByAddr(decoded.ra) ?: return ExecutionResult(false)
                val rb = getRegByAddr(decoded.rb) ?: return ExecutionResult(false)
                val rc = getRegByAddr(decoded.rc) ?: return ExecutionResult(false)

                rc.set(ra.get() + rb.get())
                pc.inc(1U)
            }

            ADDI -> {
                val rb = getRegByAddr(decoded.rb) ?: return ExecutionResult(false)
                val rc = getRegByAddr(decoded.rc) ?: return ExecutionResult(false)
                val imm16 = decoded.imm16.toInt().signExtend(16)

                rc.set(rb.get() + imm16.toValue())
                pc.inc(1U)
            }

            ADDLI -> {
                val rb = getRegByAddr(decoded.rb) ?: return ExecutionResult(false)
                val rc = getRegByAddr(decoded.rc) ?: return ExecutionResult(false)
                val imm16 = decoded.imm16.toInt()

                rc.set(rb.get() + imm16.toValue())
                pc.inc(1U)
            }

            ADDHI -> {
                val rb = getRegByAddr(decoded.rb) ?: return ExecutionResult(false)
                val rc = getRegByAddr(decoded.rc) ?: return ExecutionResult(false)
                val imm16 = decoded.imm16.toInt() shl 16

                rc.set(rb.get() + imm16.toValue())
                pc.inc(1U)
            }

            ADDX -> {
                val ra = getRegByAddr(decoded.ra) ?: return ExecutionResult(false)
                val rb = getRegByAddr(decoded.rb) ?: return ExecutionResult(false)
                val rc = getRegByAddr(decoded.rc) ?: return ExecutionResult(false)

                val carry = (ra.get().toULong() + rb.get().toULong()) shr 32

                rc.set(carry.toUInt().toValue())
                pc.inc(1U)
            }

            SUB -> {
                val ra = getRegByAddr(decoded.ra) ?: return ExecutionResult(false)
                val rb = getRegByAddr(decoded.rb) ?: return ExecutionResult(false)
                val rc = getRegByAddr(decoded.rc) ?: return ExecutionResult(false)

                rc.set(ra.get() - rb.get())
                pc.inc(1U)
            }

            SUBX -> {
                val ra = getRegByAddr(decoded.ra) ?: return ExecutionResult(false)
                val rb = getRegByAddr(decoded.rb) ?: return ExecutionResult(false)
                val rc = getRegByAddr(decoded.rc) ?: return ExecutionResult(false)

                val borrow = if (rb.get().toUInt() < ra.get().toUInt()) 1U else 0U

                rc.set(borrow.toValue())
                pc.inc(1U)
            }

            CMPU -> {
                val ra = getRegByAddr(decoded.ra) ?: return ExecutionResult(false)
                val rb = getRegByAddr(decoded.rb) ?: return ExecutionResult(false)
                val rc = getRegByAddr(decoded.rc) ?: return ExecutionResult(false)

                val comparison = rb.get().toUInt().compareTo(ra.get().toUInt())

                rc.set(comparison.toValue())
                pc.inc(1U)
            }

            CMPS -> {
                val ra = getRegByAddr(decoded.ra) ?: return ExecutionResult(false)
                val rb = getRegByAddr(decoded.rb) ?: return ExecutionResult(false)
                val rc = getRegByAddr(decoded.rc) ?: return ExecutionResult(false)

                val comparison = rb.get().toInt().compareTo(ra.get().toInt())

                rc.set(comparison.toValue())
                pc.inc(1U)
            }

            CMPUI -> {
                val imm16 = decoded.imm16
                val rb = getRegByAddr(decoded.rb) ?: return ExecutionResult(false)
                val rc = getRegByAddr(decoded.rc) ?: return ExecutionResult(false)

                val comparison = rb.get().toUInt().compareTo(imm16)

                rc.set(comparison.toValue())
                pc.inc(1U)
            }

            CMPSI -> {
                val imm16 = decoded.imm16.toInt().signExtend(16)
                val rb = getRegByAddr(decoded.rb) ?: return ExecutionResult(false)
                val rc = getRegByAddr(decoded.rc) ?: return ExecutionResult(false)

                val comparison = rb.get().toInt().compareTo(imm16)

                rc.set(comparison.toValue())
                pc.inc(1U)
            }

            AND -> {
                val ra = getRegByAddr(decoded.ra) ?: return ExecutionResult(false)
                val rb = getRegByAddr(decoded.rb) ?: return ExecutionResult(false)
                val rc = getRegByAddr(decoded.rc) ?: return ExecutionResult(false)

                val result = rb.get().toUInt() and ra.get().toUInt()

                rc.set(result.toValue())
                pc.inc(1U)
            }

            AND0I -> {
                val imm16 = decoded.imm16
                val rb = getRegByAddr(decoded.rb) ?: return ExecutionResult(false)
                val rc = getRegByAddr(decoded.rc) ?: return ExecutionResult(false)

                val result = rb.get().toUInt() and imm16

                rc.set(result.toValue())
                pc.inc(1U)
            }

            AND1I -> {
                val imm16 = decoded.imm16 or (0b1111111111111111U shl 16)
                val rb = getRegByAddr(decoded.rb) ?: return ExecutionResult(false)
                val rc = getRegByAddr(decoded.rc) ?: return ExecutionResult(false)

                val result = rb.get().toUInt() and imm16

                rc.set(result.toValue())
                pc.inc(1U)
            }

            OR -> {
                val ra = getRegByAddr(decoded.ra) ?: return ExecutionResult(false)
                val rb = getRegByAddr(decoded.rb) ?: return ExecutionResult(false)
                val rc = getRegByAddr(decoded.rc) ?: return ExecutionResult(false)

                val result = rb.get().toUInt() or ra.get().toUInt()

                rc.set(result.toValue())
                pc.inc(1U)
            }

            ORI -> {
                val imm16 = decoded.imm16
                val rb = getRegByAddr(decoded.rb) ?: return ExecutionResult(false)
                val rc = getRegByAddr(decoded.rc) ?: return ExecutionResult(false)

                val result = rb.get().toUInt() or imm16

                rc.set(result.toValue())
                pc.inc(1U)
            }

            XOR -> {
                val ra = getRegByAddr(decoded.ra) ?: return ExecutionResult(false)
                val rb = getRegByAddr(decoded.rb) ?: return ExecutionResult(false)
                val rc = getRegByAddr(decoded.rc) ?: return ExecutionResult(false)

                val result = rb.get().toUInt() xor ra.get().toUInt()

                rc.set(result.toValue())
                pc.inc(1U)
            }

            XORI -> {
                val imm16 = decoded.imm16
                val rb = getRegByAddr(decoded.rb) ?: return ExecutionResult(false)
                val rc = getRegByAddr(decoded.rc) ?: return ExecutionResult(false)

                val result = rb.get().toUInt() xor imm16

                rc.set(result.toValue())
                pc.inc(1U)
            }

            LSL -> {
                val rb = getRegByAddr(decoded.rb) ?: return ExecutionResult(false)
                val rc = getRegByAddr(decoded.rc) ?: return ExecutionResult(false)

                val result = rb.get().toUInt() shl 1

                rc.set(result.toValue())
                pc.inc(1U)
            }

            LSR -> {
                val rb = getRegByAddr(decoded.rb) ?: return ExecutionResult(false)
                val rc = getRegByAddr(decoded.rc) ?: return ExecutionResult(false)

                val result = rb.get().toUInt() shr 1

                rc.set(result.toValue())
                pc.inc(1U)
            }

            ASL -> {
                val rb = getRegByAddr(decoded.rb) ?: return ExecutionResult(false)
                val rc = getRegByAddr(decoded.rc) ?: return ExecutionResult(false)

                val result = rb.get().toInt() shl 1

                rc.set(result.toValue())
                pc.inc(1U)
            }

            ASR -> {
                val rb = getRegByAddr(decoded.rb) ?: return ExecutionResult(false)
                val rc = getRegByAddr(decoded.rc) ?: return ExecutionResult(false)

                val result = rb.get().toInt() shr 1

                rc.set(result.toValue())
                pc.inc(1U)
            }

            ROL -> {
                val rb = getRegByAddr(decoded.rb) ?: return ExecutionResult(false)
                val rc = getRegByAddr(decoded.rc) ?: return ExecutionResult(false)

                val result = rb.get().toUInt().rol(1)

                rc.set(result.toValue())
                pc.inc(1U)
            }

            ROR -> {
                val rb = getRegByAddr(decoded.rb) ?: return ExecutionResult(false)
                val rc = getRegByAddr(decoded.rc) ?: return ExecutionResult(false)

                val result = rb.get().toUInt().ror(1)

                rc.set(result.toValue())
                pc.inc(1U)
            }

            EXTB -> {
                val rb = getRegByAddr(decoded.rb) ?: return ExecutionResult(false)
                val rc = getRegByAddr(decoded.rc) ?: return ExecutionResult(false)

                val result = rb.get().toInt().signExtend(8)

                rc.set(result.toValue())
                pc.inc(1U)
            }

            EXTH -> {
                val rb = getRegByAddr(decoded.rb) ?: return ExecutionResult(false)
                val rc = getRegByAddr(decoded.rc) ?: return ExecutionResult(false)

                val result = rb.get().toInt().signExtend(16)

                rc.set(result.toValue())
                pc.inc(1U)
            }

            SWAPB -> {
                val rb = getRegByAddr(decoded.rb) ?: return ExecutionResult(false)
                val rc = getRegByAddr(decoded.rc) ?: return ExecutionResult(false)

                val origin = rb.get().toUInt()
                val b0 = origin and 0xffU
                val b1 = origin shr 8 and 0xffU
                val b2 = origin shr 16 and 0xffU
                val b3 = origin shr 24 and 0xffU

                val result = b2 shl 8 or b3 shl 8 or b0 shl 8 or b1

                rc.set(result.toValue())
                pc.inc(1U)
            }

            SWAPH -> {
                val rb = getRegByAddr(decoded.rb) ?: return ExecutionResult(false)
                val rc = getRegByAddr(decoded.rc) ?: return ExecutionResult(false)

                val origin = rb.get().toUInt()
                val h0 = origin and 0xffffU
                val h1 = origin shr 16 and 0xffffU

                val result = h0 shl 16 or h1

                rc.set(result.toValue())
                pc.inc(1U)
            }

            NOT -> {
                val rb = getRegByAddr(decoded.rb) ?: return ExecutionResult(false)
                val rc = getRegByAddr(decoded.rc) ?: return ExecutionResult(false)

                val result = rb.get().toUInt().inv()

                rc.set(result.toValue())
                pc.inc(1U)
            }

            LDD -> {
                val rb = getRegByAddr(decoded.rb) ?: return ExecutionResult(false)
                val rc = getRegByAddr(decoded.rc) ?: return ExecutionResult(false)
                val disp = decoded.disp16.toInt().signExtend(16)
                val address = (rb.get().toInt() + disp).toUInt().toValue()

                val fetched = dataMemory.load(address, tracker = tracker)

                rc.set(fetched)
                pc.inc(1U)
            }

            LDR -> {
                val ra = getRegByAddr(decoded.ra) ?: return ExecutionResult(false)
                val rb = getRegByAddr(decoded.rb) ?: return ExecutionResult(false)
                val rc = getRegByAddr(decoded.rc) ?: return ExecutionResult(false)
                val address = (rb.get().toUInt() + ra.get().toUInt()).toValue()

                val fetched = dataMemory.load(address, tracker = tracker)

                rc.set(fetched)
                pc.inc(1U)
            }

            STD -> {
                val rb = getRegByAddr(decoded.rb) ?: return ExecutionResult(false)
                val rc = getRegByAddr(decoded.rc) ?: return ExecutionResult(false)
                val disp = decoded.disp16.toInt().signExtend(16)
                val address = (rb.get().toInt() + disp).toUInt().toValue()

                dataMemory.store(address, rc.get(), tracker = tracker)

                pc.inc(1U)
            }

            STR -> {
                val ra = getRegByAddr(decoded.ra) ?: return ExecutionResult(false)
                val rb = getRegByAddr(decoded.rb) ?: return ExecutionResult(false)
                val rc = getRegByAddr(decoded.rc) ?: return ExecutionResult(false)
                val address = (rb.get().toUInt() + ra.get().toUInt()).toValue()

                dataMemory.store(address, rc.get(), tracker = tracker)

                pc.inc(1U)
            }

            BEQ -> {
                val rc = getRegByAddr(decoded.rc) ?: return ExecutionResult(false)
                val disp = decoded.disp18.toInt().signExtend(18)
                if (rc.get().toInt() == 0) {
                    val address = pc.get().toInt() + disp
                    pc.set(address.toValue())
                } else {
                    pc.inc(1U)
                }
            }

            BNE -> {
                val rc = getRegByAddr(decoded.rc) ?: return ExecutionResult(false)
                val disp = decoded.disp18.toInt().signExtend(18)
                if (rc.get().toInt() != 0) {
                    val address = pc.get().toInt() + disp
                    pc.set(address.toValue())
                } else {
                    pc.inc(1U)
                }
            }

            BLT -> {
                val rc = getRegByAddr(decoded.rc) ?: return ExecutionResult(false)
                val disp = decoded.disp18.toInt().signExtend(18)
                if (rc.get().toInt() < 0) {
                    val address = pc.get().toInt() + disp
                    pc.set(address.toValue())
                } else {
                    pc.inc(1U)
                }
            }

            BGT -> {
                val rc = getRegByAddr(decoded.rc) ?: return ExecutionResult(false)
                val disp = decoded.disp18.toInt().signExtend(18)
                if (rc.get().toInt() > 0) {
                    val address = pc.get().toInt() + disp
                    pc.set(address.toValue())
                } else {
                    pc.inc(1U)
                }
            }

            BLE -> {
                val rc = getRegByAddr(decoded.rc) ?: return ExecutionResult(false)
                val disp = decoded.disp18.toInt().signExtend(18)
                if (rc.get().toInt() <= 0) {
                    val address = pc.get().toInt() + disp
                    pc.set(address.toValue())
                } else {
                    pc.inc(1U)
                }
            }

            BGE -> {
                val rc = getRegByAddr(decoded.rc) ?: return ExecutionResult(false)
                val disp = decoded.disp18.toInt().signExtend(18)
                if (rc.get().toInt() >= 0) {
                    val address = pc.get().toInt() + disp
                    pc.set(address.toValue())
                } else {
                    pc.inc(1U)
                }
            }

            BRA -> {
                val disp = decoded.disp26.toInt().signExtend(26)

                val address = pc.get().toInt() + disp
                pc.set(address.toValue())
            }

            BSR -> {
                val r31 = getRegByAddr(31U) ?: return ExecutionResult(false)
                val disp = decoded.disp26.toInt().signExtend(26)

                r31.set(pc.get() + 1U.toValue()) // Save Return Address

                val address = pc.get().toInt() + disp
                pc.set(address.toValue())
            }

            JMP -> {
                val rb = getRegByAddr(decoded.rb) ?: return ExecutionResult(false)

                pc.set(rb.get())
            }

            JSR -> {
                val rb = getRegByAddr(decoded.rb) ?: return ExecutionResult(false)
                val r31 = getRegByAddr(31U) ?: return ExecutionResult(false)

                r31.set(pc.get() + 1U.toValue())

                pc.set(rb.get())
            }

            null -> {
                console.error("Invalid Instruction!")
                return ExecutionResult(false)
            }
        }

        val isBranchToSubRoutine = when (decoded.type) {
            JSR -> true
            BSR -> true
            else -> false
        }
        
        val isReturnFromSubRoutine = when (decoded.type) {
            JMP -> true
            else -> false
        }
        
        return ExecutionResult(true, isReturnFromSubRoutine, isBranchToSubRoutine)
    }

    override fun setupMicroArch() {
        MicroSetup.append(memory)
        if (instrMemory != memory) MicroSetup.append(instrMemory)
        if (dataMemory != memory) MicroSetup.append(dataMemory)
    }

}