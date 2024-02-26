package emulator.archs

import emulator.archs.riscv64.RV64
import emulator.archs.riscv64.RV64BinMapper
import emulator.kit.assembly.standards.StandardArch
import emulator.archs.riscv64.RV64Syntax.InstrType.*

class ArchRV64 : StandardArch(RV64.config, RV64.asmConfig) {
    private val binMapper = RV64BinMapper()
    override fun executeNext(): ExecutionResult {
        val currentPc = getRegContainer().pc.get().toHex()
        val instrBin = getMemory().load(currentPc,RV64.WORD_WIDTH.getByteCount())
        val result = binMapper.getInstrFromBinary(instrBin) ?: return ExecutionResult(valid = false, typeIsReturnFromSubroutine =  false, typeIsBranchToSubroutine = false)
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