package cengine.util.newint

import com.ionspin.kotlin.bignum.integer.BigInteger

class UInt8(private val value: UByte) : IntNumber<UInt8> {

    constructor(value: UInt) : this(value.toUByte())

    companion object {
        fun UByte.toUInt8() = UInt8(this)

        val ZERO = UInt8(0U)
        val ONE = UInt8(1U)

        fun String.parseUInt8(radix: Int): UInt8 = UInt8(toUByte(radix))
    }

    override val bitWidth: Int
        get() = 8

    override val byteCount: Int
        get() = 1

    override fun plus(other: UInt8): UInt8 = UInt8(value + other.value)
    override fun minus(other: UInt8): UInt8 = UInt8(value - other.value)
    override fun times(other: UInt8): UInt8 = UInt8(value * other.value)
    override fun div(other: UInt8): UInt8 = UInt8(value / other.value)
    override fun rem(other: UInt8): UInt8 = UInt8(value % other.value)

    override fun unaryMinus(): UInt8 = throw Exception("Can't negotiate unsigned value!")
    override fun inc(): UInt8 = UInt8(value.inc())
    override fun dec(): UInt8 = UInt8(value.dec())

    override fun inv(): UInt8 = UInt8(value.inv())
    override fun and(other: UInt8): UInt8 = UInt8(value.toUInt() and other.value.toUInt())
    override fun or(other: UInt8): UInt8 = UInt8(value.toUInt() or other.value.toUInt())
    override fun xor(other: UInt8): UInt8 = UInt8(value.toUInt() xor other.value.toUInt())

    override fun shl(other: UInt8): UInt8 = UInt8(value.toUInt() shl other.value.toInt())
    override fun shr(other: UInt8): UInt8 = UInt8(value.toUInt() shr other.value.toInt())


    override fun plus(other: Int): UInt8 = UInt8(value + other.toUInt())
    override fun minus(other: Int): UInt8 = UInt8(value - other.toUInt())
    override fun times(other: Int): UInt8 = UInt8(value * other.toUInt())
    override fun div(other: Int): UInt8 = UInt8(value / other.toUInt())
    override fun rem(other: Int): UInt8 = UInt8(value % other.toUInt())

    override fun and(other: Int): UInt8 = UInt8(value and other.toUByte())
    override fun or(other: Int): UInt8 = UInt8(value or other.toUByte())
    override fun xor(other: Int): UInt8 = UInt8(value xor other.toUByte())
    override fun shl(other: Int): UInt8 = UInt8(value.toUInt() shl other)
    override fun shr(other: Int): UInt8 = UInt8(value.toUInt() shr other)


    override fun compareTo(other: UInt8): Int = value.compareTo(other.value)
    override fun equals(other: Any?): Boolean = if (other is UInt8) value == other.value else false

    override fun toInt8(): Int8 = Int8(value.toByte())
    override fun toInt16(): Int16 = Int16(value.toShort())
    override fun toInt32(): Int32 = Int32(value.toInt())
    override fun toInt64(): Int64 = Int64(value.toLong())
    override fun toInt128(): Int128 = Int128(BigInteger.fromUByte(value))
    override fun toBigInt(): BigInt = BigInt(BigInteger.fromUByte(value))
    override fun toUInt8(): UInt8 = this
    override fun toUInt16(): UInt16 = UInt16(value.toUShort())
    override fun toUInt32(): UInt32 = UInt32(value.toUInt())
    override fun toUInt64(): UInt64 = UInt64(value.toULong())

    override fun toString(radix: Int): String = value.toString(radix)

    override fun hashCode(): Int {
        return value.hashCode()
    }

    override fun int8s() = listOf(this.toInt8())

}