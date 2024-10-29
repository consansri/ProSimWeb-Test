package cengine.lang.asm

import emulator.kit.memory.Memory

interface Initializer {

    val id: String
    fun initialize(memory: Memory)

}