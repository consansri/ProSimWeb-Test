package cengine.lang.asm.elf

interface BinaryProvider {

    fun print(): String
    fun build(): ByteArray

}