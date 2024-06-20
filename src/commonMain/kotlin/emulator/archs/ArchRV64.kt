package emulator.archs

import emulator.archs.riscv64.RV64
import emulator.archs.riscv64.RV64BinMapper
import emulator.archs.riscv64.RV64Syntax.InstrType.JAL
import emulator.archs.riscv64.RV64Syntax.InstrType.JALR
import emulator.kit.MicroSetup
import emulator.kit.memory.Memory
import emulator.kit.optional.BasicArchImpl

class ArchRV64 : BasicArchImpl(RV64.config, RV64.asmConfig) {
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
        val instrBin = instrMemory.load(currentPc, RV64.WORD_WIDTH.getByteCount(), tracker).toBin()
        val result = RV64BinMapper.getInstrFromBinary(instrBin) ?: return ExecutionResult(valid = false, typeIsReturnFromSubroutine = false, typeIsBranchToSubroutine = false)
        result.type.execute(arch = this, result.binMap, tracker)
        val isReturnFromSubroutine = when (result.type) {
            JALR -> true
            else -> false
        }
        val isBranchToSubroutine = when (result.type) {
            JAL -> true
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