package cengine.util.newint

import com.ionspin.kotlin.bignum.integer.BigInteger

class Int32(val value: Int) : IntNumber<Int32> {

    companion object {
        fun Int.toInt32() = Int32(this)

        val ZERO = Int32(0)
        val ONE = Int32(1)

        fun String.parseInt32(radix: Int): Int32 = Int32(toInt(radix))
    }

    override val bitWidth: Int
        get() = 32

    override val byteCount: Int
        get() = 4

    override fun plus(other: Int32): Int32 = Int32(value + other.value)
    override fun minus(other: Int32): Int32 = Int32(value - other.value)
    override fun times(other: Int32): Int32 = Int32(value * other.value)
    override fun div(other: Int32): Int32 = Int32(value / other.value)
    override fun rem(other: Int32): Int32 = Int32(value % other.value)

    override fun unaryMinus(): Int32 = Int32(-value)
    override fun inc(): Int32 = Int32(value.inc())
    override fun dec(): Int32 = Int32(value.dec())

    override fun inv(): Int32 = Int32(value.inv())
    override fun and(other: Int32): Int32 = Int32(value and other.value)
    override fun or(other: Int32): Int32 = Int32(value or other.value)
    override fun xor(other: Int32): Int32 = Int32(value xor other.value)
    override fun shl(other: Int32): Int32 = Int32(value shl other.value)
    override fun shr(other: Int32): Int32 = Int32(value shr other.value)


    override fun plus(other: Int): Int32 = Int32(value + other)
    override fun minus(other: Int): Int32 = Int32(value - other)
    override fun times(other: Int): Int32 = Int32(value * other)
    override fun div(other: Int): Int32 = Int32(value / other)
    override fun rem(other: Int): Int32 = Int32(value % other)

    override fun and(other: Int): Int32 = Int32(value and other)
    override fun or(other: Int): Int32 = Int32(value or other)
    override fun xor(other: Int): Int32 = Int32(value xor other)
    override fun shl(other: Int): Int32 = Int32(value shl other)
    override fun shr(other: Int): Int32 = Int32(value shr other)


    override fun compareTo(other: Int32): Int = value.compareTo(other.value)
    override fun equals(other: Any?): Boolean = if (other is Int32) value == other.value else false

    override fun toInt8(): Int8 = Int8(value.toByte())
    override fun toInt16(): Int16 = Int16(value.toShort())
    override fun toInt32(): Int32 = this
    override fun toInt64(): Int64 = Int64(value.toLong())
    override fun toInt128(): Int128 = Int128(BigInteger.fromInt(value))
    override fun toBigInt(): BigInt = BigInt(BigInteger.fromInt(value))
    override fun toUInt8(): UInt8 = UInt8(value.toUByte())
    override fun toUInt16(): UInt16 = UInt16(value.toUShort())
    override fun toUInt32(): UInt32 = UInt32(value.toUInt())
    override fun toUInt64(): UInt64 = UInt64(value.toULong())

    override fun toString(radix: Int): String = value.toString(radix)

    override fun hashCode(): Int {
        return value.hashCode()
    }

    override fun int8s() = (this shr bitWidth / 2).toInt16().int8s() + this.toInt16().int8s()


}