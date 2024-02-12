package emulator.archs

import emulator.archs.riscv64.RV64
import emulator.archs.riscv64.RV64BinMapper
import emulator.archs.riscv64.RV64Syntax
import emulator.kit.assembly.standards.StandardArch
import emulator.kit.types.Variable
import emulator.archs.riscv64.RV64Syntax.InstrType.*

class ArchRV64 : StandardArch(RV64.config, RV64.asmConfig) {
    val binMapper = RV64BinMapper()
    override fun executeNext(): ExecutionResult {
        val currentPc = getRegContainer().pc.get().toHex()
        val instrBin = Variable.Value.Bin(getMemory().loadArray(currentPc, RV64.WORD_WIDTH.getByteCount()).joinToString("") { it.getRawBinStr() }, RV64.WORD_WIDTH)
        val result = binMapper.getInstrFromBinary(instrBin)
        if (result == null) return ExecutionResult(false, false, false)
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