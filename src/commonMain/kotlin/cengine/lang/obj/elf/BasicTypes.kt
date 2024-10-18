package cengine.lang.obj.elf

import cengine.util.Endianness

typealias Elf32_Addr = UInt
typealias Elf64_Addr = ULong

typealias Elf32_Off = UInt
typealias Elf64_Off = ULong

typealias Elf_Byte = UByte

typealias Elf_Half = UShort

typealias Elf_Sword = Int

typealias Elf_Word = UInt

typealias Elf_Sxword = Long

typealias Elf_Xword = ULong

// Helper functions to ensure 4-byte alignment
fun alignTo4Bytes(value: Int): Int = (value + 3) and (-4)
fun alignTo4Bytes(value: Long): Long = (value + 3L) and (-4L)

// Helper functions to load multiple bytes into other types
fun ByteArray.loadUByte(index: Int): UByte = this[index].toUByte()

fun ByteArray.loadByte(index: Int): Byte = this[index]

fun ByteArray.loadUShort(endianness: Endianness, index: Int): UShort{
    val bytes = this.copyOfRange(index, index + UShort.SIZE_BYTES)
    val result = if (endianness == Endianness.BIG) {
        bytes.joinToString("") { it.toUByte().toString(16).padStart(2,'0') }.toUShort(16)
    } else {
        bytes.reversed().joinToString("") { it.toUByte().toString(16).padStart(2,'0') }.toUShort(16)
    }
    return result
}

fun ByteArray.loadUShort(e_ident: E_IDENT, index: Int): UShort = loadUShort(e_ident.endianness, index)

fun ByteArray.loadShort(e_ident: E_IDENT, index: Int): Short = loadUShort(e_ident, index).toShort()

fun ByteArray.loadUInt(endianness: Endianness, index: Int): UInt {
    val bytes = this.copyOfRange(index, index + UInt.SIZE_BYTES)
    val result = if (endianness == Endianness.BIG) {
        bytes.joinToString("") { it.toUByte().toString(16).padStart(2,'0') }.toUInt(16)
    } else {
        bytes.reversed().joinToString("") { it.toUByte().toString(16).padStart(2,'0') }.toUInt(16)
    }
    return result
}

fun ByteArray.loadUInt(e_ident: E_IDENT, index: Int): UInt = loadUInt(e_ident.endianness, index)

fun ByteArray.loadInt(e_ident: E_IDENT, index: Int): Int = this.loadUInt(e_ident, index).toInt()

fun ByteArray.loadULong(endianness: Endianness, index: Int): ULong {
    val bytes = this.copyOfRange(index, index + ULong.SIZE_BYTES)
    val result = if (endianness == Endianness.BIG) {
        bytes.joinToString("") { it.toUByte().toString(16).padStart(2,'0') }.toULong(16)
    } else {
        bytes.reversed().joinToString("") { it.toUByte().toString(16).padStart(2,'0') }.toULong(16)
    }
    return result
}

fun ByteArray.loadULong(e_ident: E_IDENT, index: Int): ULong = loadULong(e_ident.endianness, index)

fun ByteArray.loadLong(e_ident: E_IDENT, index: Int): Long = loadULong(e_ident, index).toLong()


// Exceptions
object NotInELFFormatException : Exception("File is not in ELF Format!")
