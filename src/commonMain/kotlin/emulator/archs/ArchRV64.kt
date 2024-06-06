package emulator.archs

import emulator.archs.riscv64.RV64
import emulator.archs.riscv64.RV64BinMapper
import emulator.archs.riscv64.RV64Syntax.InstrType.JAL
import emulator.archs.riscv64.RV64Syntax.InstrType.JALR
import emulator.kit.MicroSetup
import emulator.kit.common.memory.DMCache
import emulator.kit.common.memory.Memory
import emulator.kit.optional.BasicArchImpl

class ArchRV64 : BasicArchImpl(RV64.config, RV64.asmConfig) {

    var dataMemory: Memory = DMCache(memory, console,  4, 4)
        set(value) {
            field = value
            resetMicroArch()
        }

    override fun executeNext(): ExecutionResult {
        val currentPc = regContainer.pc.get().toHex()
        val instrBin = memory.load(currentPc, RV64.WORD_WIDTH.getByteCount()).toBin()
        val result = RV64BinMapper.getInstrFromBinary(instrBin) ?: return ExecutionResult(valid = false, typeIsReturnFromSubroutine = false, typeIsBranchToSubroutine = false)
        result.type.execute(arch = this, result.binMap)
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
        if (dataMemory != memory) MicroSetup.append(dataMemory)
    }
}