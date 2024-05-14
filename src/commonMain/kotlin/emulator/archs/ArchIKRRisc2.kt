package emulator.archs

import emulator.archs.ikrrisc2.IKRRisc2
import emulator.kit.optional.BasicArchImpl

class ArchIKRRisc2 : BasicArchImpl(IKRRisc2.config, IKRRisc2.asmConfig) {
    override fun executeNext(): ExecutionResult {
        TODO("Not yet implemented")
    }
}