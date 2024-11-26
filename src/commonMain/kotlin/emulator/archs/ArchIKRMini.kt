package emulator.archs

import emulator.archs.ikrmini.IKRMini
import emulator.kit.MicroSetup
import emulator.kit.memory.Memory
import emulator.kit.optional.BasicArchImpl

class ArchIKRMini : BasicArchImpl(IKRMini.config) {
    var instrMemory: Memory = memory
        set(value) {
            field = value
            resetMicroArch()
        }
    var dataMemory: Memory = memory
        set(value) {
            field = value
            resetMicroArch()
        }

    override fun executeNext(tracker: Memory.AccessTracker): ExecutionResult {
        val pc = regContainer.pc.get().toHex()
        val opCode = instrMemory.load(pc, 2, tracker).toHex()

        TODO()
    }

    override fun setupMicroArch() {
        MicroSetup.append(memory)
        if (instrMemory != memory) MicroSetup.append(instrMemory)
        if (dataMemory != memory) MicroSetup.append(dataMemory)
    }
}