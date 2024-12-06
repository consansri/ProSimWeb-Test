package cengine.util.newint

import com.ionspin.kotlin.bignum.integer.BigInteger

class UInt64(val value: ULong) : IntNumber<UInt64> {

    companion object {
        fun ULong.toUInt64() = UInt64(this)

        fun String.parseUInt64(radix: Int): UInt64 = UInt64(toULong(radix))
    }

    override val bitWidth: Int
        get() = 64

    override fun plus(other: UInt64): UInt64 = UInt64(value + other.value)
    override fun minus(other: UInt64): UInt64 = UInt64(value - other.value)
    override fun times(other: UInt64): UInt64 = UInt64(value * other.value)
    override fun div(other: UInt64): UInt64 = UInt64(value / other.value)
    override fun rem(other: UInt64): UInt64 = UInt64(value % other.value)

    override fun unaryMinus(): UInt64 = throw Exception("Can't negotiate unsigned value!")
    override fun inc(): UInt64 = UInt64(value.inc())
    override fun dec(): UInt64 = UInt64(value.dec())

    override fun inv(): UInt64 = UInt64(value.inv())
    override fun and(other: UInt64): UInt64 = UInt64(value and other.value)
    override fun or(other: UInt64): UInt64 = UInt64(value or other.value)
    override fun xor(other: UInt64): UInt64 = UInt64(value xor other.value)

    override fun shl(other: UInt64): UInt64 = UInt64(value shl other.value.toInt())
    override fun shr(other: UInt64): UInt64 = UInt64(value shr other.value.toInt())


    override fun plus(other: Int): UInt64 = UInt64(value + other.toUInt())
    override fun minus(other: Int): UInt64 = UInt64(value - other.toUInt())
    override fun and(other: Int): UInt64 = UInt64(value and other.toULong())
    override fun or(other: Int): UInt64 = UInt64(value or other.toULong())
    override fun xor(other: Int): UInt64 = UInt64(value xor other.toULong())
    override fun shl(other: Int): UInt64 = UInt64(value shl other)
    override fun shr(other: Int): UInt64 = UInt64(value shr other)


    override fun compareTo(other: UInt64): Int = value.compareTo(other.value)
    override fun equals(other: Any?): Boolean = if (other is UInt64) value == other.value else false

    override fun toInt8(): Int8 = Int8(value.toByte())
    override fun toInt16(): Int16 = Int16(value.toShort())
    override fun toInt32(): Int32 = Int32(value.toInt())
    override fun toInt64(): Int64 = Int64(value.toLong())
    override fun toInt128(): Int128 = Int128(BigInteger.fromULong(value))
    override fun toBigInt(): BigInt = BigInt(BigInteger.fromULong(value))
    override fun toUInt8(): UInt8 = UInt8(value.toUByte())
    override fun toUInt16(): UInt16 = UInt16(value.toUShort())
    override fun toUInt32(): UInt32 = UInt32(value.toUInt())
    override fun toUInt64(): UInt64 = this

    override fun toString(radix: Int): String = value.toString(radix)

    override fun hashCode(): Int = value.hashCode()

    override fun int8s(): Array<Int8> = (this shr bitWidth / 2).toUInt32().int8s() + this.toUInt32().int8s()

}