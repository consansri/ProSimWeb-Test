package cengine.lang.asm.elf

import cengine.util.Endianness

interface BinaryProvider {
    fun build(endianness: Endianness): ByteArray

}