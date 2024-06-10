package emulator.archs

import emulator.archs.ikrrisc2.IKRRisc2
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
        TODO("Not yet implemented")
    }

    override fun setupMicroArch() {
        MicroSetup.append(memory)
        if (dataMemory != memory) MicroSetup.append(dataMemory)
    }

}