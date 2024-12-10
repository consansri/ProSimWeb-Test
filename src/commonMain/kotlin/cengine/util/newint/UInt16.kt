package cengine.util.newint

import com.ionspin.kotlin.bignum.integer.BigInteger

class UInt16(override val value: UShort) : IntNumber<UInt16> {

    constructor(value: UInt) : this(value.toUShort())
    constructor(value: ULong) : this(value.toUShort())

    companion object {
        fun UShort.toUInt16() = UInt16(this)

        val ZERO = UInt16(0U)
        val ONE = UInt16(1U)

        fun String.parseUInt16(radix: Int): UInt16 = UInt16(toUShort(radix))

        fun fromUInt8(byte1: UInt8, byte0: UInt8): UInt16 = (byte1.toUInt16() shl 8) or byte0.toUInt16()
    }

    override val bitWidth: Int
        get() = 16

    override val byteCount: Int
        get() = 2

    override fun plus(other: UInt16): UInt16 = UInt16(value + other.value)
    override fun minus(other: UInt16): UInt16 = UInt16(value - other.value)
    override fun times(other: UInt16): UInt16 = UInt16(value * other.value)
    override fun div(other: UInt16): UInt16 = UInt16(value / other.value)
    override fun rem(other: UInt16): UInt16 = UInt16(value % other.value)

    override fun unaryMinus(): UInt16 = throw Exception("Can't negotiate unsigned value!")
    override fun inc(): UInt16 = UInt16(value.inc())
    override fun dec(): UInt16 = UInt16(value.dec())

    override fun inv(): UInt16 = UInt16(value.inv())
    override fun and(other: UInt16): UInt16 = UInt16(value.toUInt() and other.value.toUInt())
    override fun or(other: UInt16): UInt16 = UInt16(value.toUInt() or other.value.toUInt())
    override fun xor(other: UInt16): UInt16 = UInt16(value.toUInt() xor other.value.toUInt())

    override fun shl(bits: UInt16): UInt16 = UInt16(value.toUInt() shl bits.value.toInt())
    override fun shr(bits: UInt16): UInt16 = UInt16(value.toUInt() shr bits.value.toInt())


    override fun plus(other: Int): UInt16 = UInt16(value + other.toUInt())
    override fun plus(other: Long): UInt16 =UInt16(value + other.toULong())

    override fun minus(other: Int): UInt16 = UInt16(value - other.toUInt())
    override fun minus(other: Long): UInt16 = UInt16(value - other.toULong())

    override fun times(other: Int): UInt16 = UInt16(value * other.toUInt())
    override fun times(other: Long): UInt16 = UInt16(value * other.toULong())

    override fun div(other: Int): UInt16 = UInt16(value / other.toUInt())
    override fun div(other: Long): UInt16 = UInt16(value / other.toULong())

    override fun rem(other: Int): UInt16 = UInt16(value % other.toUInt())
    override fun rem(other: Long): UInt16 = UInt16(value % other.toULong())

    override fun and(other: Int): UInt16 = UInt16(value and other.toUShort())
    override fun and(other: Long): UInt16 = UInt16(value and other.toUShort())

    override fun or(other: Int): UInt16 = UInt16(value or other.toUShort())
    override fun or(other: Long): UInt16 = UInt16(value or other.toUShort())

    override fun xor(other: Int): UInt16 = UInt16(value xor other.toUShort())
    override fun xor(other: Long): UInt16 = UInt16(value xor other.toUShort())

    override fun shl(bits: Int): UInt16 = UInt16(value.toUInt() shl bits)
    override fun shr(bits: Int): UInt16 = UInt16(value.toUInt() shr bits)


    override fun compareTo(other: UInt16): Int = value.compareTo(other.value)
    override fun compareTo(other: Long): Int = value.compareTo(other.toULong())
    override fun compareTo(other: Int): Int = value.compareTo(other.toUInt())
    override fun equals(other: Any?): Boolean = if (other is UInt16) value == other.value else false

    override fun toInt8(): Int8 = Int8(value.toByte())
    override fun toInt16(): Int16 = Int16(value.toShort())
    override fun toInt32(): Int32 = Int32(value.toInt())
    override fun toInt64(): Int64 = Int64(value.toLong())
    override fun toInt128(): Int128 = Int128(BigInteger.fromUShort(value))
    override fun toBigInt(): BigInt = BigInt(BigInteger.fromUShort(value))
    override fun toUInt8(): UInt8 = UInt8(value.toUByte())
    override fun toUInt16(): UInt16 = this
    override fun toUInt32(): UInt32 = UInt32(value.toUInt())
    override fun toUInt64(): UInt64 = UInt64(value.toULong())

    override fun toString(radix: Int): String = value.toString(radix)

    override fun hashCode(): Int {
        return value.hashCode()
    }

    override fun int8s() = (this shr bitWidth / 2).toUInt8().int8s() + this.toUInt8().int8s()
}