package emulator.archs

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import cengine.util.Endianness
import cengine.util.newint.UInt16
import cengine.util.newint.UInt8
import emulator.archs.t6502.T6502
import emulator.kit.MicroSetup
import emulator.kit.memory.MainMemory
import emulator.kit.memory.Memory
import emulator.kit.optional.BasicArchImpl

/**
 * MOS Technology 6502 Architecture
 */
class ArchT6502 : BasicArchImpl<UInt16, UInt8>(T6502.config) {

    override val pcState: MutableState<UInt16> = mutableStateOf(UInt16.ZERO)

    private var pc by pcState

    override val memory: MainMemory<UInt16, UInt8> = MainMemory(Endianness.LITTLE, UInt16, UInt8)

    var instrMemory: Memory<UInt16, UInt8> = memory
        set(value) {
            field = value
            resetMicroArch()
        }
    var dataMemory: Memory<UInt16, UInt8> = memory
        set(value) {
            field = value
            resetMicroArch()
        }

    override fun executeNext(tracker: Memory.AccessTracker): ExecutionResult {
        val threeBytes = instrMemory.loadArray(pc, 3, tracker)

        TODO()

    }

    override fun setupMicroArch() {
        MicroSetup.append(memory)
        if (instrMemory != memory) MicroSetup.append(instrMemory)
        if (dataMemory != memory) MicroSetup.append(dataMemory)
    }

    override fun resetPC() {
        pc = UInt16.ZERO
    }
}