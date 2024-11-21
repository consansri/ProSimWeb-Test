package cengine.lang.asm

import cengine.util.integer.Hex
import emulator.kit.memory.Memory

interface Initializer {

    val id: String
    fun initialize(memory: Memory)
    fun contents(): Map<Hex, Pair<List<Hex>, List<Disassembler.Label>>>

}