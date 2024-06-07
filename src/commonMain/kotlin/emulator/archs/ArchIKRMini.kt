package emulator.archs

import emulator.archs.ikrmini.IKRMini
import emulator.archs.ikrmini.IKRMiniSyntax
import emulator.kit.MicroSetup
import emulator.kit.common.memory.Memory
import emulator.kit.optional.BasicArchImpl
import emulator.kit.types.Variable.Value.Hex

class ArchIKRMini : BasicArchImpl(IKRMini.config, IKRMini.asmConfig) {

    var cachedMemory: Memory = memory
        set(value) {
            field = value
            resetMicroArch()
        }

    override fun executeNext(tracker: Memory.AccessTracker): ExecutionResult {
        val pc = regContainer.pc.get().toHex()
        val opCode = memory.load(pc, 2).toHex()

        var paramType: IKRMiniSyntax.ParamType? = null
        val instrType = IKRMiniSyntax.InstrType.entries.firstOrNull { type ->
            paramType = type.paramMap.entries.toList().firstOrNull { it.value.getRawHexStr().uppercase() == opCode.getRawHexStr().uppercase() }?.key
            paramType != null
        }

        val currParamType = paramType
        if (currParamType == null) return ExecutionResult(false, typeIsBranchToSubroutine = false, typeIsReturnFromSubroutine = false)

        val extensions = (1..<currParamType.wordAmount).map {
            memory.load((pc + Hex((it * 2).toString(16), IKRMini.MEM_ADDRESS_WIDTH)).toHex(), 2).toHex()
        }

        if (instrType != null) {
            console.log("> executing: ${instrType.name} -> ${currParamType.name}")
            instrType.execute(this, currParamType, extensions, tracker)
            return ExecutionResult(valid = true, typeIsReturnFromSubroutine = instrType == IKRMiniSyntax.InstrType.JMP || instrType == IKRMiniSyntax.InstrType.BRA, typeIsBranchToSubroutine = instrType == IKRMiniSyntax.InstrType.BSR)
        }
        return ExecutionResult(valid = false, typeIsReturnFromSubroutine = false, typeIsBranchToSubroutine = false)
    }

    override fun setupMicroArch() {
        MicroSetup.append(memory)
        if (cachedMemory != memory) MicroSetup.append(cachedMemory)
    }
}