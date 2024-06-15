package emulator.archs

import emulator.archs.riscv32.RV32
import emulator.archs.riscv32.RV32BinMapper
import emulator.archs.riscv32.RV32Syntax
import emulator.kit.MicroSetup
import emulator.kit.common.memory.Memory
import emulator.kit.optional.BasicArchImpl

class ArchRV32 : BasicArchImpl(RV32.config, RV32.asmConfig) {

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
        val currentPc = regContainer.pc.get().toHex()
        val instrBin = instrMemory.load(currentPc, RV32.WORD_WIDTH.getByteCount(), tracker).toBin()
        val result = RV32BinMapper.getInstrFromBinary(instrBin) ?: return ExecutionResult(valid = false, typeIsReturnFromSubroutine = false, typeIsBranchToSubroutine = false)
        result.type.execute(arch = this, result.binMap, tracker)
        val isReturnFromSubroutine = when (result.type) {
            RV32Syntax.InstrType.JALR -> true
            else -> false
        }
        val isBranchToSubroutine = when (result.type) {
            RV32Syntax.InstrType.JAL -> true
            else -> false
        }

        return ExecutionResult(true, typeIsReturnFromSubroutine = isReturnFromSubroutine, typeIsBranchToSubroutine = isBranchToSubroutine)
    }

    override fun setupMicroArch() {
        MicroSetup.append(memory)
        if (instrMemory != memory) MicroSetup.append(instrMemory)
        if (dataMemory != memory) MicroSetup.append(dataMemory)
    }


}