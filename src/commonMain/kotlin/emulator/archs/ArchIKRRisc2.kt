package emulator.archs

import emulator.archs.ikrrisc2.IKRRisc2
import emulator.archs.ikrrisc2.IKRRisc2BinMapper
import emulator.kit.MicroSetup
import emulator.kit.common.memory.Memory
import emulator.kit.optional.BasicArchImpl

class ArchIKRRisc2 : BasicArchImpl(IKRRisc2.config, IKRRisc2.asmConfig) {
    var dataMemory: Memory = memory
        set(value) {
            field = value
            resetMicroArch()
        }

    override fun executeNext(tracker: Memory.AccessTracker): ExecutionResult {
        val loaded = memory.load(regContainer.pc.get().toHex())
        val decodeResult = IKRRisc2BinMapper.decodeBinary(this, loaded.toBin())
        if (decodeResult != null) {
            decodeResult.type.execute(this, regContainer.pc, decodeResult, tracker)
            return ExecutionResult(valid = true, typeIsReturnFromSubroutine = decodeResult.type.isReturnFromSubRoutine(), typeIsBranchToSubroutine = decodeResult.type.isBranchToSubRoutine())
        }
        console.error("Invalid instruction binary $loaded at address ${regContainer.pc.get().toHex()}!")
        return ExecutionResult(valid = false, typeIsReturnFromSubroutine = false, typeIsBranchToSubroutine = false)
    }

    override fun setupMicroArch() {
        MicroSetup.append(memory)
        if (dataMemory != memory) MicroSetup.append(dataMemory)
    }

}