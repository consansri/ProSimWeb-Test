package emulator.archs

import emulator.archs.riscv32.RV32
import emulator.archs.riscv32.RV32BinMapper
import emulator.archs.riscv32.RV32Syntax
import emulator.archs.riscv64.RV64
import emulator.kit.assembly.standards.StandardArch
import emulator.kit.types.Variable

class ArchRV32 : StandardArch(RV32.config, RV32.asmConfig) {
    val binMapper = RV32BinMapper()
    override fun executeNext(): ExecutionResult {
        val currentPc = getRegContainer().pc.get().toHex()
        val instrBin = Variable.Value.Bin(getMemory().loadArray(currentPc, RV32.WORD_WIDTH.getByteCount()).joinToString("") { it.getRawBinStr() }, RV64.WORD_WIDTH)
        val result = binMapper.getInstrFromBinary(instrBin)
        if (result == null) return ExecutionResult(false, false, false)
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