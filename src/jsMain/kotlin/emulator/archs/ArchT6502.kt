package emulator.archs

import emulator.archs.ikrmini.IKRMiniSyntax
import emulator.archs.t6502.T6502
import emulator.archs.t6502.T6502Syntax
import emulator.kit.Architecture
import emulator.kit.assembly.standards.StandardArch
import emulator.kit.types.Variable
import emulator.kit.types.Variable.Value.*
import kotlin.time.measureTime
import kotlin.time.measureTimedValue

/**
 * MOS Technology 6502 Architecture
 */
class ArchT6502 : StandardArch(T6502.config, T6502.asmConfig) {
    override fun executeNext(): ExecutionResult {
        val currentPC = getRegContainer().pc.get().toHex()
        val threeBytes = getMemory().loadArray(currentPC, 3)

        var paramType: T6502Syntax.AModes? = null
        val instrType = T6502Syntax.InstrType.entries.firstOrNull { type ->
            paramType = type.opCode.entries.firstOrNull {
                threeBytes.first().toHex().getRawHexStr().uppercase() == it.value.getRawHexStr().uppercase()
            }?.key
            paramType != null
        } ?: return ExecutionResult(false, false, false)
        val actualParamType = paramType ?: return ExecutionResult(false, false, false)

        instrType.execute(this, actualParamType, threeBytes)
        return ExecutionResult(valid = true, typeIsReturnFromSubroutine = instrType == T6502Syntax.InstrType.RTS, typeIsBranchToSubroutine = instrType == T6502Syntax.InstrType.JSR)
    }
}