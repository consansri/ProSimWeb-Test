package cengine.util.newint

import com.ionspin.kotlin.bignum.integer.BigInteger

class UInt32(override val value: UInt) : IntNumber<UInt32> {

    constructor(value: ULong) : this(value.toUInt())

    companion object {
        fun UInt.toUInt32(): UInt32 = UInt32(this)

        val ZERO = UInt32(0U)
        val ONE = UInt32(1U)

        fun String.parseUInt32(radix: Int): UInt32 = UInt32(toUInt(radix))

        fun fromUInt16(byte1: UInt16, byte0: UInt16): UInt32 = (byte1.toUInt32() shl 16) or byte0.toUInt32()
    }

    override val bitWidth: Int
        get() = 32

    override val byteCount: Int
        get() = 4

    override fun plus(other: UInt32): UInt32 = UInt32(value + other.value)
    override fun minus(other: UInt32): UInt32 = UInt32(value - other.value)
    override fun times(other: UInt32): UInt32 = UInt32(value * other.value)
    override fun div(other: UInt32): UInt32 = UInt32(value / other.value)
    override fun rem(other: UInt32): UInt32 = UInt32(value % other.value)

    override fun unaryMinus(): UInt32 = throw Exception("Can't negotiate unsigned value!")
    override fun inc(): UInt32 = UInt32(value.inc())
    override fun dec(): UInt32 = UInt32(value.dec())

    override fun inv(): UInt32 = UInt32(value.inv())
    override fun and(other: UInt32): UInt32 = UInt32(value and other.value)
    override fun or(other: UInt32): UInt32 = UInt32(value or other.value)
    override fun xor(other: UInt32): UInt32 = UInt32(value xor other.value)

    override fun shl(bits: UInt32): UInt32 = UInt32(value shl bits.value.toInt())
    override fun shr(bits: UInt32): UInt32 = UInt32(value shr bits.value.toInt())


    override fun plus(other: Int): UInt32 = UInt32(value + other.toUInt())
    override fun plus(other: Long): UInt32 = UInt32(value + other.toULong())

    override fun minus(other: Int): UInt32 = UInt32(value - other.toUInt())
    override fun minus(other: Long): UInt32 = UInt32(value - other.toULong())

    override fun times(other: Int): UInt32 = UInt32(value * other.toUInt())
    override fun times(other: Long): UInt32 = UInt32(value * other.toULong())

    override fun div(other: Int): UInt32 = UInt32(value / other.toUInt())
    override fun div(other: Long): UInt32 = UInt32(value / other.toULong())

    override fun rem(other: Int): UInt32 = UInt32(value % other.toUInt())
    override fun rem(other: Long): UInt32 = UInt32(value % other.toULong())

    override fun and(other: Int): UInt32 = UInt32(value and other.toUInt())
    override fun and(other: Long): UInt32 = UInt32(value and other.toUInt())

    override fun or(other: Int): UInt32 = UInt32(value or other.toUInt())
    override fun or(other: Long): UInt32 = UInt32(value or other.toUInt())

    override fun xor(other: Int): UInt32 = UInt32(value xor other.toUInt())
    override fun xor(other: Long): UInt32 = UInt32(value xor other.toUInt())

    override fun shl(bits: Int): UInt32 = UInt32(value shl bits)
    override fun shr(bits: Int): UInt32 = UInt32(value shr bits)


    override fun compareTo(other: UInt32): Int = value.compareTo(other.value)
    override fun compareTo(other: Long): Int = value.compareTo(other.toULong())
    override fun compareTo(other: Int): Int = value.compareTo(other.toUInt())
    override fun equals(other: Any?): Boolean = if (other is UInt32) value == other.value else false

    override fun toInt8(): Int8 = Int8(value.toByte())
    override fun toInt16(): Int16 = Int16(value.toShort())
    override fun toInt32(): Int32 = Int32(value.toInt())
    override fun toInt64(): Int64 = Int64(value.toLong())
    override fun toInt128(): Int128 = Int128(BigInteger.fromUInt(value))
    override fun toBigInt(): BigInt = BigInt(BigInteger.fromUInt(value))
    override fun toUInt8(): UInt8 = UInt8(value.toUByte())
    override fun toUInt16(): UInt16 = UInt16(value.toUShort())
    override fun toUInt32(): UInt32 = this
    override fun toUInt64(): UInt64 = UInt64(value.toULong())

    override fun toString(radix: Int): String = value.toString(radix)

    override fun hashCode(): Int = value.hashCode()

    override fun int8s() = (this shr bitWidth / 2).toUInt16().int8s() + this.toUInt16().int8s()

}