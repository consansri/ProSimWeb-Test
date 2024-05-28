package emulator.archs

import emulator.archs.riscv64.RV64
import emulator.archs.riscv64.RV64BinMapper
import emulator.kit.optional.BasicArchImpl
import emulator.archs.riscv64.RV64Syntax.InstrType.*

class ArchRV64 : BasicArchImpl(RV64.config, RV64.asmConfig) {    override fun executeNext(): ExecutionResult {
        val currentPc = regContainer.pc.get().toHex()
        val instrBin = memory.load(currentPc,RV64.WORD_WIDTH.getByteCount())
        val result = RV64BinMapper.getInstrFromBinary(instrBin) ?: return ExecutionResult(valid = false, typeIsReturnFromSubroutine =  false, typeIsBranchToSubroutine = false)
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
}