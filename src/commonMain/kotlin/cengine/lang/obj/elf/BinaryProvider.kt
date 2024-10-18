package cengine.lang.obj.elf

import cengine.util.Endianness

interface BinaryProvider {
    fun build(endianness: Endianness): ByteArray

    fun byteSize(): Int

}