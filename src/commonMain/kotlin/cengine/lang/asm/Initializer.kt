package cengine.lang.asm

import cengine.util.newint.BigInt
import cengine.util.newint.IntNumber
import emulator.kit.memory.Memory

interface Initializer {

    val id: String
    fun initialize(memory: Memory<*,*>)
    fun contents(): Map<BigInt, Pair<List<IntNumber<*>>, List<Disassembler.Label>>>

}