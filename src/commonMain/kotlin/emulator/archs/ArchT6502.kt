package emulator.archs

import emulator.archs.t6502.AModes
import emulator.archs.t6502.InstrType
import emulator.archs.t6502.T6502
import emulator.kit.common.memory.Memory
import emulator.kit.optional.BasicArchImpl

/**
 * MOS Technology 6502 Architecture
 */
class ArchT6502 : BasicArchImpl(T6502.config, T6502.asmConfig) {
    override fun executeNext(tracker: Memory.AccessTracker): ExecutionResult {
        val currentPC = regContainer.pc.get().toHex()
        val threeBytes = memory.loadArray(currentPC, 3).map { it.toBin() }.toTypedArray()

        var paramType: AModes? = null
        val instrType = InstrType.entries.firstOrNull { type ->
            paramType = type.opCode.entries.firstOrNull {
                threeBytes.first().toHex().getRawHexStr().uppercase() == it.value.getRawHexStr().uppercase()
            }?.key
            paramType != null
        } ?: return ExecutionResult(false, false, false)
        val actualParamType = paramType ?: return ExecutionResult(false, false, false)

        instrType.execute(this, actualParamType, threeBytes)
        return ExecutionResult(valid = true, typeIsReturnFromSubroutine = instrType == InstrType.RTS, typeIsBranchToSubroutine = instrType == InstrType.JSR)
    }
}