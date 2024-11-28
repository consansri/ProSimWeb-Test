package emulator.archs

import cengine.lang.asm.ast.target.ikrmini.IKRMiniDisassembler
import cengine.lang.asm.ast.target.ikrmini.IKRMiniDisassembler.InstrType.*
import cengine.util.integer.Hex
import cengine.util.integer.Size
import cengine.util.integer.Value.Companion.toValue
import cengine.util.integer.rol
import cengine.util.integer.ror
import emulator.archs.ikrmini.IKRMini
import emulator.kit.MicroSetup
import emulator.kit.common.RegContainer
import emulator.kit.memory.Memory
import emulator.kit.optional.BasicArchImpl

class ArchIKRMini : BasicArchImpl(IKRMini.config) {
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

    private val alu = ALU()
    private val ac: RegContainer.Register = getRegByName("AC") ?: throw Exception("AC is missing!")
    private val nzvc: RegContainer.Register = getRegByName("NZVC") ?: throw Exception("NZVC is missing!")
    private val pc get() = regContainer.pc

    override fun executeNext(tracker: Memory.AccessTracker): ExecutionResult {
        val currPc = pc.get().toHex()
        val opCode = instrMemory.load(currPc, tracker = tracker).toHex().toUShort()
        val second = instrMemory.load(currPc + 1U.toValue(Size.Bit16), tracker = tracker).toHex().toUShort()
        val third = instrMemory.load(currPc + 2U.toValue(Size.Bit16), tracker = tracker).toHex().toUShort()

        val decoded = IKRMiniDisassembler.IKRMiniInstrProvider(opCode, second, third)
        when (decoded.type) {
            LOAD_IMM -> {
                ac.set(second.toValue())
                decoded.type.incPC()
            }

            LOAD_DIR -> {
                ac.set(dataMemory.load(second.toValue()))
                decoded.type.incPC()
            }

            LOAD_IND -> {
                ac.set(dataMemory.load(dataMemory.load(second.toValue())))
                decoded.type.incPC()
            }

            LOAD_IND_OFF -> {
                ac.set(dataMemory.load(dataMemory.load(second.toValue()) + third.toValue()))
                decoded.type.incPC()
            }

            LOADI -> {
                ac.set(dataMemory.load(ac.get().toHex()))
                decoded.type.incPC()
            }

            STORE_DIR -> {
                dataMemory.store(second.toValue(), ac.get())
                decoded.type.incPC()
            }

            STORE_IND -> {
                dataMemory.store(dataMemory.load(second.toValue()), ac.get())
                decoded.type.incPC()
            }

            STORE_IND_OFF -> {
                dataMemory.store(dataMemory.load(second.toValue()) + third.toValue(), ac.get())
                decoded.type.incPC()
            }

            AND_IMM -> {
                ac.set(
                    alu.and(ac.get().toUShort(), second).toValue()
                )
                decoded.type.incPC()
            }

            AND_DIR -> {
                ac.set(
                    alu.and(
                        ac.get().toUShort(),
                        dataMemory.load(second.toValue()).toUShort()
                    ).toValue()
                )
                decoded.type.incPC()
            }

            AND_IND -> {
                ac.set(
                    alu.and(
                        ac.get().toUShort(),
                        dataMemory.load(dataMemory.load(second.toValue())).toUShort()
                    ).toValue()
                )
                decoded.type.incPC()
            }

            AND_IND_OFF -> {
                ac.set(
                    alu.and(
                        ac.get().toUShort(),
                        (dataMemory.load(dataMemory.load(second.toValue())).toUShort() + third).toUShort()
                    ).toValue()
                )
                decoded.type.incPC()
            }

            OR_IMM -> {
                ac.set(
                    alu.or(ac.get().toUShort(), second).toValue()
                )
                decoded.type.incPC()
            }

            OR_DIR -> {
                ac.set(
                    alu.or(
                        ac.get().toUShort(),
                        dataMemory.load(second.toValue()).toUShort()
                    ).toValue()
                )
                decoded.type.incPC()
            }

            OR_IND -> {
                ac.set(
                    alu.or(
                        ac.get().toUShort(),
                        dataMemory.load(dataMemory.load(second.toValue())).toUShort()
                    ).toValue()
                )
                decoded.type.incPC()
            }

            OR_IND_OFF -> {
                ac.set(
                    alu.or(
                        ac.get().toUShort(),
                        (dataMemory.load(dataMemory.load(second.toValue())).toUShort() + third).toUShort()
                    ).toValue()
                )
                decoded.type.incPC()
            }

            XOR_IMM -> {
                ac.set(
                    alu.xor(ac.get().toUShort(), second).toValue()
                )
                decoded.type.incPC()
            }

            XOR_DIR -> {
                ac.set(
                    alu.xor(
                        ac.get().toUShort(),
                        dataMemory.load(second.toValue()).toUShort()
                    ).toValue()
                )
                decoded.type.incPC()
            }

            XOR_IND -> {
                ac.set(
                    alu.xor(
                        ac.get().toUShort(),
                        dataMemory.load(dataMemory.load(second.toValue())).toUShort()
                    ).toValue()
                )
                decoded.type.incPC()
            }

            XOR_IND_OFF -> {
                ac.set(
                    alu.xor(
                        ac.get().toUShort(),
                        (dataMemory.load(dataMemory.load(second.toValue())).toUShort() + third).toUShort()
                    ).toValue()
                )
                decoded.type.incPC()
            }

            ADD_IMM -> {
                ac.set(
                    alu.add(ac.get().toUShort(), second).toValue()
                )
                decoded.type.incPC()
            }

            ADD_DIR -> {
                ac.set(
                    alu.add(
                        ac.get().toUShort(),
                        dataMemory.load(second.toValue()).toUShort()
                    ).toValue()
                )
                decoded.type.incPC()
            }

            ADD_IND -> {
                ac.set(
                    alu.add(
                        ac.get().toUShort(),
                        dataMemory.load(dataMemory.load(second.toValue())).toUShort()
                    ).toValue()
                )
                decoded.type.incPC()
            }

            ADD_IND_OFF -> {
                ac.set(
                    alu.add(
                        ac.get().toUShort(),
                        (dataMemory.load(dataMemory.load(second.toValue())).toUShort() + third).toUShort()
                    ).toValue()
                )
                decoded.type.incPC()
            }

            ADDC_IMM -> {
                ac.set(
                    alu.addc(ac.get().toUShort(), second).toValue()
                )
                decoded.type.incPC()
            }

            ADDC_DIR -> {
                ac.set(
                    alu.addc(
                        ac.get().toUShort(),
                        dataMemory.load(second.toValue()).toUShort()
                    ).toValue()
                )
                decoded.type.incPC()
            }

            ADDC_IND -> {
                ac.set(
                    alu.addc(
                        ac.get().toUShort(),
                        dataMemory.load(dataMemory.load(second.toValue())).toUShort()
                    ).toValue()
                )
                decoded.type.incPC()
            }

            ADDC_IND_OFF -> {
                ac.set(
                    alu.addc(
                        ac.get().toUShort(),
                        (dataMemory.load(dataMemory.load(second.toValue())).toUShort() + third).toUShort()
                    ).toValue()
                )
                decoded.type.incPC()
            }

            SUB_IMM -> {
                ac.set(
                    alu.sub(ac.get().toUShort(), second).toValue()
                )
                decoded.type.incPC()
            }

            SUB_DIR -> {
                ac.set(
                    alu.sub(
                        ac.get().toUShort(),
                        dataMemory.load(second.toValue()).toUShort()
                    ).toValue()
                )
                decoded.type.incPC()
            }

            SUB_IND -> {
                ac.set(
                    alu.sub(
                        ac.get().toUShort(),
                        dataMemory.load(dataMemory.load(second.toValue())).toUShort()
                    ).toValue()
                )
                decoded.type.incPC()
            }

            SUB_IND_OFF -> {
                ac.set(
                    alu.sub(
                        ac.get().toUShort(),
                        (dataMemory.load(dataMemory.load(second.toValue())).toUShort() + third).toUShort()
                    ).toValue()
                )
                decoded.type.incPC()
            }

            SUBC_IMM -> {
                ac.set(
                    alu.subc(ac.get().toUShort(), second).toValue()
                )
                decoded.type.incPC()
            }

            SUBC_DIR -> {
                ac.set(
                    alu.subc(
                        ac.get().toUShort(),
                        dataMemory.load(second.toValue()).toUShort()
                    ).toValue()
                )
                decoded.type.incPC()
            }

            SUBC_IND -> {
                ac.set(
                    alu.subc(
                        ac.get().toUShort(),
                        dataMemory.load(dataMemory.load(second.toValue())).toUShort()
                    ).toValue()
                )
                decoded.type.incPC()
            }

            SUBC_IND_OFF -> {
                ac.set(
                    alu.subc(
                        ac.get().toUShort(),
                        (dataMemory.load(dataMemory.load(second.toValue())).toUShort() + third).toUShort()
                    ).toValue()
                )
                decoded.type.incPC()
            }

            LSL -> {
                ac.set(
                    alu.lsl(ac.get().toUShort()).toValue()
                )
                decoded.type.incPC()
            }

            LSL_DIR -> {
                ac.set(
                    alu.lsl(
                        dataMemory.load(second.toValue()).toUShort()
                    ).toValue()
                )
                decoded.type.incPC()
            }

            LSL_IND -> {
                ac.set(
                    alu.lsl(
                        dataMemory.load(dataMemory.load(second.toValue())).toUShort()
                    ).toValue()
                )
                decoded.type.incPC()
            }

            LSL_IND_OFF -> {
                ac.set(
                    alu.lsl(
                        (dataMemory.load(dataMemory.load(second.toValue())).toUShort() + third).toUShort()
                    ).toValue()
                )
                decoded.type.incPC()
            }

            LSR -> {
                ac.set(
                    alu.lsr(ac.get().toUShort()).toValue()
                )
                decoded.type.incPC()
            }

            LSR_DIR -> {
                ac.set(
                    alu.lsr(
                        dataMemory.load(second.toValue()).toUShort()
                    ).toValue()
                )
                decoded.type.incPC()
            }

            LSR_IND -> {
                ac.set(
                    alu.lsr(
                        dataMemory.load(dataMemory.load(second.toValue())).toUShort()
                    ).toValue()
                )
                decoded.type.incPC()
            }

            LSR_IND_OFF -> {
                ac.set(
                    alu.lsr(
                        (dataMemory.load(dataMemory.load(second.toValue())).toUShort() + third).toUShort()
                    ).toValue()
                )
                decoded.type.incPC()
            }

            ROL -> {
                ac.set(
                    alu.rol(ac.get().toUShort()).toValue()
                )
                decoded.type.incPC()
            }

            ROL_DIR -> {
                ac.set(
                    alu.rol(
                        dataMemory.load(second.toValue()).toUShort()
                    ).toValue()
                )
                decoded.type.incPC()
            }

            ROL_IND -> {
                ac.set(
                    alu.rol(
                        dataMemory.load(dataMemory.load(second.toValue())).toUShort()
                    ).toValue()
                )
                decoded.type.incPC()
            }

            ROL_IND_OFF -> {
                ac.set(
                    alu.rol(
                        (dataMemory.load(dataMemory.load(second.toValue())).toUShort() + third).toUShort()
                    ).toValue()
                )
                decoded.type.incPC()
            }

            ROR -> {
                ac.set(
                    alu.ror(ac.get().toUShort()).toValue()
                )
                decoded.type.incPC()
            }

            ROR_DIR -> {
                ac.set(
                    alu.ror(
                        dataMemory.load(second.toValue()).toUShort()
                    ).toValue()
                )
                decoded.type.incPC()
            }

            ROR_IND -> {
                ac.set(
                    alu.ror(
                        dataMemory.load(dataMemory.load(second.toValue())).toUShort()
                    ).toValue()
                )
                decoded.type.incPC()
            }

            ROR_IND_OFF -> {
                ac.set(
                    alu.ror(
                        (dataMemory.load(dataMemory.load(second.toValue())).toUShort() + third).toUShort()
                    ).toValue()
                )
                decoded.type.incPC()
            }

            ASL -> {
                ac.set(
                    alu.asl(ac.get().toUShort()).toValue()
                )
                decoded.type.incPC()
            }

            ASL_DIR -> {
                ac.set(
                    alu.asl(
                        dataMemory.load(second.toValue()).toUShort()
                    ).toValue()
                )
                decoded.type.incPC()
            }

            ASL_IND -> {
                ac.set(
                    alu.asl(
                        dataMemory.load(dataMemory.load(second.toValue())).toUShort()
                    ).toValue()
                )
                decoded.type.incPC()
            }

            ASL_IND_OFF -> {
                ac.set(
                    alu.asl(
                        (dataMemory.load(dataMemory.load(second.toValue())).toUShort() + third).toUShort()
                    ).toValue()
                )
                decoded.type.incPC()
            }

            ASR -> {
                ac.set(
                    alu.asr(ac.get().toUShort()).toValue()
                )
                decoded.type.incPC()
            }

            ASR_DIR -> {
                ac.set(
                    alu.asr(
                        dataMemory.load(second.toValue()).toUShort()
                    ).toValue()
                )
                decoded.type.incPC()
            }

            ASR_IND -> {
                ac.set(
                    alu.asr(
                        dataMemory.load(dataMemory.load(second.toValue())).toUShort()
                    ).toValue()
                )
                decoded.type.incPC()
            }

            ASR_IND_OFF -> {
                ac.set(
                    alu.asr(
                        (dataMemory.load(dataMemory.load(second.toValue())).toUShort() + third).toUShort()
                    ).toValue()
                )
                decoded.type.incPC()
            }

            RCL -> {
                ac.set(
                    alu.rcl(ac.get().toUShort()).toValue()
                )
                decoded.type.incPC()
            }

            RCL_IMM -> {
                ac.set(
                    alu.rcl(
                        second
                    ).toValue()
                )
                decoded.type.incPC()
            }

            RCL_DIR -> {
                ac.set(
                    alu.rcl(
                        dataMemory.load(second.toValue()).toUShort()
                    ).toValue()
                )
                decoded.type.incPC()
            }

            RCL_IND -> {
                ac.set(
                    alu.rcl(
                        dataMemory.load(dataMemory.load(second.toValue())).toUShort()
                    ).toValue()
                )
                decoded.type.incPC()
            }

            RCL_IND_OFF -> {
                ac.set(
                    alu.rcl(
                        (dataMemory.load(dataMemory.load(second.toValue())).toUShort() + third).toUShort()
                    ).toValue()
                )
                decoded.type.incPC()
            }

            RCR -> {
                ac.set(
                    alu.rcr(ac.get().toUShort()).toValue()
                )
                decoded.type.incPC()
            }

            RCR_IMM -> {
                ac.set(
                    alu.rcr(
                        second
                    ).toValue()
                )
                decoded.type.incPC()
            }

            RCR_DIR -> {
                ac.set(
                    alu.rcr(
                        dataMemory.load(second.toValue()).toUShort()
                    ).toValue()
                )
                decoded.type.incPC()
            }

            RCR_IND -> {
                ac.set(
                    alu.rcr(
                        dataMemory.load(dataMemory.load(second.toValue())).toUShort()
                    ).toValue()
                )
                decoded.type.incPC()
            }

            RCR_IND_OFF -> {
                ac.set(
                    alu.rcr(
                        (dataMemory.load(dataMemory.load(second.toValue())).toUShort() + third).toUShort()
                    ).toValue()
                )
                decoded.type.incPC()
            }

            NOT -> {
                ac.set(
                    alu.not(ac.get().toUShort()).toValue()
                )
                decoded.type.incPC()
            }

            NOT_DIR -> {
                ac.set(
                    alu.not(
                        dataMemory.load(second.toValue()).toUShort()
                    ).toValue()
                )
                decoded.type.incPC()
            }

            NOT_IND -> {
                ac.set(
                    alu.not(
                        dataMemory.load(dataMemory.load(second.toValue())).toUShort()
                    ).toValue()
                )
                decoded.type.incPC()
            }

            NOT_IND_OFF -> {
                ac.set(
                    alu.not(
                        (dataMemory.load(dataMemory.load(second.toValue())).toUShort() + third).toUShort()
                    ).toValue()
                )
                decoded.type.incPC()
            }

            NEG_DIR -> {
                ac.set(
                    alu.neg(
                        dataMemory.load(second.toValue()).toUShort()
                    ).toValue()
                )
                decoded.type.incPC()
            }

            NEG_IND -> {
                ac.set(
                    alu.neg(
                        dataMemory.load(dataMemory.load(second.toValue())).toUShort()
                    ).toValue()
                )
                decoded.type.incPC()
            }

            NEG_IND_OFF -> {
                ac.set(
                    alu.neg(
                        (dataMemory.load(dataMemory.load(second.toValue())).toUShort() + third).toUShort()
                    ).toValue()
                )
                decoded.type.incPC()
            }

            CLR -> {
                ac.set(0U.toValue(Size.Bit16))
                nzvc.set(0b0100U.toValue(Size.Bit4))
                decoded.type.incPC()
            }

            INC -> {
                ac.set(
                    alu.add(ac.get().toUShort(), 1U).toValue()
                )
                decoded.type.incPC()
            }

            INC_DIR -> {
                ac.set(
                    alu.add(
                        dataMemory.load(second.toValue()).toUShort(),
                        1U
                    ).toValue()
                )
                decoded.type.incPC()
            }

            INC_IND -> {
                ac.set(
                    alu.add(
                        dataMemory.load(dataMemory.load(second.toValue())).toUShort(),
                        1U
                    ).toValue()
                )
                decoded.type.incPC()
            }

            INC_IND_OFF -> {
                ac.set(
                    alu.add(
                        (dataMemory.load(dataMemory.load(second.toValue())).toUShort() + third).toUShort(),
                        1U
                    ).toValue()
                )
                decoded.type.incPC()
            }

            DEC -> {
                ac.set(
                    alu.sub(ac.get().toUShort(), 1U).toValue()
                )
                decoded.type.incPC()
            }

            DEC_DIR -> {
                ac.set(
                    alu.sub(
                        dataMemory.load(second.toValue()).toUShort(),
                        1U
                    ).toValue()
                )
                decoded.type.incPC()
            }

            DEC_IND -> {
                ac.set(
                    alu.sub(
                        dataMemory.load(dataMemory.load(second.toValue())).toUShort(),
                        1U
                    ).toValue()
                )
                decoded.type.incPC()
            }

            DEC_IND_OFF -> {
                ac.set(
                    alu.sub(
                        (dataMemory.load(dataMemory.load(second.toValue())).toUShort() + third).toUShort(),
                        1U
                    ).toValue()
                )
                decoded.type.incPC()
            }

            BSR -> {
                ac.set(currPc + 2U.toValue(Size.Bit16))
                pc.set(second.toValue(Size.Bit16))
            }

            JMP -> {
                pc.set(ac.get())
            }

            BRA -> {
                pc.set(second.toValue(Size.Bit16))
            }

            BHI -> {
                if(Condition.HI.test(nzvc.get().toUInt())){
                    pc.set(second.toValue())
                }else{
                    decoded.type.incPC()
                }
            }

            BLS -> {
                if(Condition.LS.test(nzvc.get().toUInt())){
                    pc.set(second.toValue())
                }else{
                    decoded.type.incPC()
                }
            }
            BCC -> {
                if(Condition.CC.test(nzvc.get().toUInt())){
                    pc.set(second.toValue())
                }else{
                    decoded.type.incPC()
                }
            }
            BCS -> {
                if(Condition.CS.test(nzvc.get().toUInt())){
                    pc.set(second.toValue())
                }else{
                    decoded.type.incPC()
                }
            }
            BNE -> {
                if(Condition.NE.test(nzvc.get().toUInt())){
                    pc.set(second.toValue())
                }else{
                    decoded.type.incPC()
                }
            }
            BEQ -> {
                if(Condition.EQ.test(nzvc.get().toUInt())){
                    pc.set(second.toValue())
                }else{
                    decoded.type.incPC()
                }
            }
            BVC -> {
                if(Condition.VC.test(nzvc.get().toUInt())){
                    pc.set(second.toValue())
                }else{
                    decoded.type.incPC()
                }
            }
            BVS -> {
                if(Condition.VS.test(nzvc.get().toUInt())){
                    pc.set(second.toValue())
                }else{
                    decoded.type.incPC()
                }
            }
            BPL -> {
                if(Condition.PL.test(nzvc.get().toUInt())){
                    pc.set(second.toValue())
                }else{
                    decoded.type.incPC()
                }
            }
            BMI ->{
                if(Condition.MI.test(nzvc.get().toUInt())){
                    pc.set(second.toValue())
                }else{
                    decoded.type.incPC()
                }
            }
            BGE -> {
                if(Condition.GE.test(nzvc.get().toUInt())){
                    pc.set(second.toValue())
                }else{
                    decoded.type.incPC()
                }
            }
            BLT -> {
                if(Condition.LT.test(nzvc.get().toUInt())){
                    pc.set(second.toValue())
                }else{
                    decoded.type.incPC()
                }
            }
            BGT -> {
                if(Condition.GT.test(nzvc.get().toUInt())){
                    pc.set(second.toValue())
                }else{
                    decoded.type.incPC()
                }
            }
            BLE -> {
                if(Condition.LE.test(nzvc.get().toUInt())){
                    pc.set(second.toValue())
                }else{
                    decoded.type.incPC()
                }
            }
            null -> ExecutionResult(false)
        }

        val isReturnFromSubroutine = when (decoded.type) {
            JMP -> true
            else -> false
        }

        val isBranchToSubroutine = when (decoded.type) {
            BSR -> true
            else -> false
        }

        return ExecutionResult(true, typeIsReturnFromSubroutine = isReturnFromSubroutine, typeIsBranchToSubroutine = isBranchToSubroutine)
    }

    override fun setupMicroArch() {
        MicroSetup.append(memory)
        if (instrMemory != memory) MicroSetup.append(instrMemory)
        if (dataMemory != memory) MicroSetup.append(dataMemory)
    }

    private fun IKRMiniDisassembler.InstrType.incPC(){
        pc.set(pc.get() + this.length.toUShort().toValue())
    }

    private inner class ALU {

        fun lsl(a: UShort): UShort {
            val resultInt = a.toUInt() shl 1
            val result = resultInt.toUShort()
            nzvc.set(flags(isNegative(result), isZero(result)))
            return result
        }

        fun lsr(a: UShort): UShort {
            val resultInt = a.toUInt() shr 1
            val result = resultInt.toUShort()
            nzvc.set(flags(isNegative(result), isZero(result)))
            return result
        }

        fun rol(a: UShort): UShort {
            val result = a rol 1
            nzvc.set(flags(isNegative(result), isZero(result)))
            return result
        }

        fun ror(a: UShort): UShort {
            val result = a ror 1
            nzvc.set(flags(isNegative(result), isZero(result)))
            return result
        }

        fun asl(a: UShort): UShort {
            val resultInt = a.toShort().toInt() shl 1
            val result = resultInt.toUShort()
            nzvc.set(flags(isNegative(result), isZero(result)))
            return result
        }

        fun asr(a: UShort): UShort {
            val resultInt = a.toShort().toInt() shr 1
            val result = resultInt.toUShort()
            nzvc.set(flags(isNegative(result), isZero(result)))
            return result
        }

        fun rcl(a: UShort): UShort {
            val carry = carry()
            val nextCarry = a.toUInt() shr 15 == 1U
            val result = (a and 0b111111111111111U or (carry shl 15).toUShort()) rol 1
            nzvc.set(flags(isNegative(result), isZero(result), c = nextCarry))
            return result
        }

        fun rcr(a: UShort): UShort {
            val carry = carry()
            val nextCarry = a.toUInt() and 1U == 1U
            val result = (a and 0b1111111111111110U or carry.toUShort()) rol 1
            nzvc.set(flags(isNegative(result), isZero(result), c = nextCarry))
            return result
        }

        fun not(a: UShort): UShort {
            val result = a.inv()
            nzvc.set(flags(isNegative(result), isZero(result)))
            return result
        }

        fun neg(a: UShort): UShort {
            val result = (-a.toShort()).toUShort()
            nzvc.set(flags(isNegative(result), isZero(result)))
            return result
        }

        fun and(a: UShort, b: UShort): UShort {
            val result = a and b
            nzvc.set(flags(isNegative(result), isZero(result)))
            return result
        }

        fun or(a: UShort, b: UShort): UShort {
            val result = a or b
            nzvc.set(flags(isNegative(result), isZero(result)))
            return result
        }

        fun xor(a: UShort, b: UShort): UShort {
            val result = a xor b
            nzvc.set(flags(isNegative(result), isZero(result)))
            return result
        }

        fun add(a: UShort, b: UShort): UShort {
            val result = a + b

            val isOverflow = a.toShort() < 0 == b.toShort() < 0
                    && result.toShort() < 0 != a.toShort() < 0

            nzvc.set(flags(isNegative(result.toUShort()), isZero(result.toUShort()), isOverflow, result shr 16 == 1U))
            return result.toUShort()
        }

        fun sub(a: UShort, b: UShort): UShort {
            val result = a + b

            val isOverflow = a.toShort() < 0 != b.toShort() < 0
                    && result.toShort() < 0 != a.toShort() < 0

            nzvc.set(flags(isNegative(result.toUShort()), isZero(result.toUShort()), isOverflow, a < b))
            return result.toUShort()
        }

        fun addc(a: UShort, b: UShort): UShort {
            val result = a + b + nzvc.get().toUInt() and 1U

            val isOverflow = a.toShort() < 0 == b.toShort() < 0
                    && result.toShort() < 0 != a.toShort() < 0

            nzvc.set(flags(isNegative(result.toUShort()), isZero(result.toUShort()), isOverflow, result shr 16 == 1U))
            return result.toUShort()

        }

        fun subc(a: UShort, b: UShort): UShort {
            val result = a.toUInt() - b.toUInt() + nzvc.get().toUInt() and 1U

            val isOverflow = a.toShort() < 0 != b.toShort() < 0
                    && result.toShort() < 0 != a.toShort() < 0

            nzvc.set(flags(isNegative(result.toUShort()), isZero(result.toUShort()), isOverflow, a < b))
            return result.toUShort()
        }

        private fun carry(): UInt = nzvc.get().toUInt() and 1U

        private fun isZero(result: UShort): Boolean = result.toUInt() == 0U

        private fun isNegative(result: UShort): Boolean = result.toUInt() shr 15 == 1U

        private fun flags(n: Boolean, z: Boolean, v: Boolean = false, c: Boolean = false): Hex {
            var result = 0U
            if (n) {
                result += 1U shl 3
            }

            if (z) {
                result += 1U shl 2
            }

            if (v) {
                result += 1U shl 1
            }

            if (c) {
                result += 1U
            }
            return result.toValue(Size.Bit4)
        }

    }

    private enum class Condition {
        HI,
        LS,
        CC,
        CS,
        NE,
        EQ,
        VC,
        VS,
        PL,
        MI,
        GE,
        LT,
        GT,
        LE;

        fun test(nzvc: UInt): Boolean {
            val n = (nzvc shr 3) and 1U == 1U
            val z = (nzvc shr 2) and 1U == 1U
            val v = (nzvc shr 1) and 1U == 1U
            val c = nzvc and 1U == 1U

            return when(this){
                HI -> !(c || z)
                LS -> c || z
                CC -> !c
                CS -> c
                NE -> !z
                EQ -> z
                VC -> !v
                VS -> v
                PL -> !n
                MI -> n
                GE -> !((n && !v) || (!n && v))
                LT -> (n && !v) || (!n && v)
                GT -> !((n && !v) || (!n && v) || z)
                LE -> (n && !v) || (!n && v) || z
            }
        }

    }

}