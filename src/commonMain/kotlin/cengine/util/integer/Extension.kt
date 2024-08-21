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
fun UShort.toValue(size: Size = Size.Bit16): UDec = UDec(this.toString(), size)
fun UInt.toValue(size: Size = Size.Bit32): UDec = UDec(this.toString(), size)
fun ULong.toValue(size: Size = Size.Bit64): UDec = UDec(this.toString(), size)

fun Dec.toLong(): Long? = this.toRawString().toLongOrNull()
fun UDec.toULong(): ULong? = this.toRawString().toULongOrNull()
fun Bin.toULong(): ULong? = this.toRawString().toULongOrNull(2)
fun Oct.toULong(): ULong?= this.toRawString().toULongOrNull(8)
fun Hex.toULong(): ULong?= this.toRawString().toULongOrNull(16)

fun Dec.toInt(): Int? = this.toRawString().toIntOrNull()
fun UDec.toUInt(): UInt?= this.toRawString().toUIntOrNull()
fun Bin.toUInt(): UInt?= this.toRawString().toUIntOrNull(2)
fun Oct.toUInt(): UInt?= this.toRawString().toUIntOrNull(8)
fun Hex.toUInt(): UInt?= this.toRawString().toUIntOrNull(16)

fun Dec.toShort(): Short? = this.toRawString().toShortOrNull()
fun UDec.toUShort(): UShort?= this.toRawString().toUShortOrNull()
fun Bin.toUShort(): UShort?= this.toRawString().toUShortOrNull(2)
fun Oct.toUShort(): UShort?= this.toRawString().toUShortOrNull(8)
fun Hex.toUShort(): UShort?= this.toRawString().toUShortOrNull(16)

fun Dec.toByte(): Byte? = this.toRawString().toByteOrNull()
fun UDec.toUByte(): UByte?= this.toRawString().toUByteOrNull()
fun Bin.toUByte(): UByte?= this.toRawString().toUByteOrNull(2)
fun Oct.toUByte(): UByte?= this.toRawString().toUByteOrNull(8)
fun Hex.toUByte(): UByte?= this.toRawString().toUByteOrNull(16)


