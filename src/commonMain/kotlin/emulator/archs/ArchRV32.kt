package emulator.archs

import emulator.archs.riscv32.RV32
import emulator.archs.riscv32.RV32BinMapper
import emulator.archs.riscv32.RV32Syntax
import emulator.kit.optional.BasicArchImpl

class ArchRV32 : BasicArchImpl(RV32.config, RV32.asmConfig) {
    override fun executeNext(): ExecutionResult {
        val currentPc = getRegContainer().pc.get().toHex()
        val instrBin = getMemory().load(currentPc, RV32.WORD_WIDTH.getByteCount())
        val result = RV32BinMapper.getInstrFromBinary(instrBin) ?: return ExecutionResult(valid = false, typeIsReturnFromSubroutine = false, typeIsBranchToSubroutine = false)
        result.type.execute(arch = this, result.binMap)
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
}