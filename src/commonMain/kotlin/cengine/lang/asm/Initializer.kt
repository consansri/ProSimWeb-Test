package cengine.lang.asm

import cengine.util.integer.BigInt
import cengine.util.integer.IntNumber
import emulator.kit.memory.Memory

interface Initializer {

    val id: String
    fun initialize(memory: Memory<*,*>)
    fun contents(): Map<BigInt, Pair<List<IntNumber<*>>, List<Disassembler.Label>>>

}