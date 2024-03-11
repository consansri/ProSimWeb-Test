package emulator.archs

import emulator.archs.ikrrisc2.IKRRisc2
import emulator.kit.assembly.standards.StandardArch

class ArchIKRRisc2 : StandardArch(IKRRisc2.config, IKRRisc2.asmConfig) {
    override fun executeNext(): ExecutionResult {
        TODO("Not yet implemented")
    }
}