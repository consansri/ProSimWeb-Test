package emulator.archs

import emulator.archs.ikrmini.IKRMini
import emulator.archs.ikrmini.IKRMiniSyntax
import emulator.kit.assembly.standards.StandardArch
import emulator.kit.types.Variable
import emulator.kit.types.Variable.Value.*

class ArchIKRMini : StandardArch(IKRMini.config, IKRMini.asmConfig) {

    override fun executeNext(): ExecutionResult {

        val pc = getRegContainer().pc.get().toHex()
        val opCode = getMemory().load(pc, 2).toHex()

        var paramType: IKRMiniSyntax.ParamType? = null
        val instrType = IKRMiniSyntax.InstrType.entries.firstOrNull { type ->
            paramType = type.paramMap.entries.toList().firstOrNull { it.value.getRawHexStr().uppercase() == opCode.getRawHexStr().uppercase() }?.key
            paramType != null
        }

        val currParamType = paramType
        if (currParamType == null) return ExecutionResult(false, typeIsBranchToSubroutine = false, typeIsReturnFromSubroutine = false)


        val extensions = (1..<currParamType.wordAmount).map {
            getMemory().load((pc + Hex((it * 2).toString(16), IKRMini.MEM_ADDRESS_WIDTH)).toHex(), 2).toHex()
        }

        if (instrType != null) {
            this.getConsole().log("> executing: ${instrType.name} -> ${currParamType.name}")
            instrType.execute(this, currParamType, extensions)
            return ExecutionResult(valid = true, typeIsReturnFromSubroutine = instrType == IKRMiniSyntax.InstrType.JMP || instrType == IKRMiniSyntax.InstrType.BRA, typeIsBranchToSubroutine = instrType == IKRMiniSyntax.InstrType.BSR)
        }
        return ExecutionResult(valid = false, typeIsReturnFromSubroutine = false, typeIsBranchToSubroutine = false)
    }
}