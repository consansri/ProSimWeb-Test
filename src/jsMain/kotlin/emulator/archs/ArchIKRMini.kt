package emulator.archs

import emulator.archs.ikrmini.IKRMini
import emulator.archs.ikrmini.IKRMiniSyntax
import emulator.kit.Architecture
import emulator.kit.assembly.standards.StandardArch
import emulator.kit.types.Variable
import emulator.kit.types.Variable.Value.*
import web.cssom.Inset
import kotlin.time.measureTime

class ArchIKRMini : StandardArch(IKRMini.config, IKRMini.asmConfig) {

    override fun executeNext(): ExecutionResult {
        val nextFourBytes = getMemory().loadArray(getRegContainer().pc.get().toHex(), 4)
        val opCode = nextFourBytes.take(2).joinToString("") { it.toHex().getRawHexStr() }
        val possibleExt = Hex(nextFourBytes.drop(2).joinToString("") { it.toHex().getRawHexStr() }, IKRMini.WORDSIZE)

        var paramType: IKRMiniSyntax.ParamType? = null
        val instrType = IKRMiniSyntax.InstrType.entries.firstOrNull {
            paramType = it.paramMap.entries.toList().firstOrNull { it.value.getRawHexStr().uppercase() == opCode.uppercase() }?.key
            paramType != null
        }
        val actualParamType = paramType
        console.log("$opCode with $possibleExt got $actualParamType")
        if (instrType != null && actualParamType != null) {
            this.getConsole().log("> executing: ${instrType.name} -> ${actualParamType.name}")
            instrType.execute(this, actualParamType, possibleExt)
            return ExecutionResult(valid = true, typeIsReturnFromSubroutine = instrType == IKRMiniSyntax.InstrType.JMP || instrType == IKRMiniSyntax.InstrType.BRA, typeIsBranchToSubroutine = instrType == IKRMiniSyntax.InstrType.BSR)
        }
        return ExecutionResult(valid = false, typeIsReturnFromSubroutine = false, typeIsBranchToSubroutine = false)
    }
}