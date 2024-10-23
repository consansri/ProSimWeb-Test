package emulator.kit.common

import emulator.kit.memory.Memory

interface Initializer {

    val id: String
    fun initialize(memory: Memory)

}