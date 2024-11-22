package cengine.util.integer

import cengine.util.integer.Size.Companion.nearestDecSize

/**
 * Quality of use extensions for [Value].
 */


/**
 * [Dec] from signed dec string.
 */
fun String.asDec(size: Size): Dec = Dec(this, size)
fun String.asDec(): Dec = Dec(this, nearestDecSize(this))
fun String.asHex(size: Size): Hex = Hex(this, size)
fun String.asHex(): Hex = Hex(this)
fun String.asOct(size: Size): Oct = Oct(this, size)
fun String.asOct(): Oct = Oct(this)
fun String.asBin(size: Size): Bin = Bin(this, size)
fun String.asBin(): Bin = Bin(this)

fun Byte.toValue(size: Size = Size.Bit8): Dec = Dec(this.toString(), size)
fun Short.toValue(size: Size = Size.Bit16): Dec = Dec(this.toString(), size)
fun Int.toValue(size: Size = Size.Bit32): Dec = Dec(this.toString(), size)
fun Long.toValue(size: Size = Size.Bit64): Dec = Dec(this.toString(), size)
fun UByte.toValue(size: Size = Size.Bit8): Hex = Hex(this.toString(16), size)
fun UShort.toValue(size: Size = Size.Bit16): Hex = Hex(this.toString(16), size)
fun UInt.toValue(size: Size = Size.Bit32): Hex = Hex(this.toString(16), size)
fun ULong.toValue(size: Size = Size.Bit64): Hex = Hex(this.toString(16), size)

fun Dec.toLong(): Long? = this.rawInput.toLongOrNull()
fun UDec.toULong(): ULong? = this.rawInput.toULongOrNull()
fun Bin.toULong(): ULong? = this.rawInput.toULongOrNull(2)
fun Oct.toULong(): ULong? = this.rawInput.toULongOrNull(8)
fun Hex.toULong(): ULong? = this.rawInput.toULongOrNull(16)

fun Dec.toInt(): Int? = this.rawInput.toIntOrNull()
fun UDec.toUInt(): UInt? = this.rawInput.toUIntOrNull()
fun Bin.toUInt(): UInt? = this.rawInput.toUIntOrNull(2)
fun Oct.toUInt(): UInt? = this.rawInput.toUIntOrNull(8)
fun Hex.toUInt(): UInt? = this.rawInput.toUIntOrNull(16)

fun Dec.toShort(): Short? = this.rawInput.toShortOrNull()
fun UDec.toUShort(): UShort? = this.rawInput.toUShortOrNull()
fun Bin.toUShort(): UShort? = this.rawInput.toUShortOrNull(2)
fun Oct.toUShort(): UShort? = this.rawInput.toUShortOrNull(8)
fun Hex.toUShort(): UShort? = this.rawInput.toUShortOrNull(16)

fun Dec.toByte(): Byte? = this.rawInput.toByteOrNull()
fun UDec.toUByte(): UByte? = this.rawInput.toUByteOrNull()
fun Bin.toUByte(): UByte? = this.rawInput.toUByteOrNull(2)
fun Oct.toUByte(): UByte? = this.rawInput.toUByteOrNull(8)
fun Hex.toUByte(): UByte? = this.rawInput.toUByteOrNull(16)

fun IntRange.overlaps(other: IntRange): Boolean {
    return this.first <= other.last && other.first <= this.last
}

