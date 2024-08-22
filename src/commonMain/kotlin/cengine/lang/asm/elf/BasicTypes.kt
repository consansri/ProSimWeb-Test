package cengine.lang.asm.elf

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

fun ByteArray.loadUShort(e_ident: E_IDENT, index: Int): UShort {
    val bigEndian = e_ident.ei_data == E_IDENT.ELFDATA2MSB

    val bytes = this.copyOfRange(index, index + UShort.SIZE_BYTES)
    val result = if (bigEndian) {
        bytes.joinToString("") { it.toUByte().toString(16) }.toUShort(16)
    } else {
        bytes.reversed().joinToString("") { it.toUByte().toString(16) }.toUShort(16)
    }
    return result
}

fun ByteArray.loadShort(e_ident: E_IDENT, index: Int): Short = this.loadUShort(e_ident, index).toShort()

fun ByteArray.loadUInt(e_ident: E_IDENT, index: Int): UInt {
    val bigEndian = e_ident.ei_data == E_IDENT.ELFDATA2MSB

    val bytes = this.copyOfRange(index, index + UInt.SIZE_BYTES)
    val result = if (bigEndian) {
        bytes.joinToString("") { it.toUByte().toString(16) }.toUInt(16)
    } else {
        bytes.reversed().joinToString("") { it.toUByte().toString(16) }.toUInt(16)
    }
    return result
}

fun ByteArray.loadInt(e_ident: E_IDENT, index: Int): Int = this.loadUInt(e_ident, index).toInt()

fun ByteArray.loadULong(e_ident: E_IDENT, index: Int): ULong {
    val bigEndian = e_ident.ei_data == E_IDENT.ELFDATA2MSB

    val bytes = this.copyOfRange(index, index + ULong.SIZE_BYTES)
    val result = if (bigEndian) {
        bytes.joinToString("") { it.toUByte().toString(16) }.toULong(16)
    } else {
        bytes.reversed().joinToString("") { it.toUByte().toString(16) }.toULong(16)
    }
    return result
}

fun ByteArray.loadLong(e_ident: E_IDENT, index: Int): Long = loadULong(e_ident, index).toLong()


// Exceptions
object NotInELFFormatException : Exception("File is not in ELF Format!")
