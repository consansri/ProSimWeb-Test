package cengine.util.newint

import com.ionspin.kotlin.bignum.integer.BigInteger
import kotlin.experimental.and
import kotlin.experimental.inv
import kotlin.experimental.or
import kotlin.experimental.xor

class Int16(override val value: Short) : IntNumber<Int16> {

    constructor(value: Int) : this(value.toShort())
    constructor(value: Long) : this(value.toShort())

    companion object {
        fun Short.toInt16() = Int16(this)

        val ZERO = Int16(0)
        val ONE = Int16(1)

        fun String.parseInt16(radix: Int): Int16 = Int16(toShort(radix))

        fun fromUInt8(byte1: UInt8, byte0: UInt8): Int16 = (byte1.toInt16() shl 8) or byte0.toInt16()
    }

    override val bitWidth: Int
        get() = 16

    override val byteCount: Int
        get() = 2

    override fun plus(other: Int16): Int16 = Int16(value + other.value)
    override fun minus(other: Int16): Int16 = Int16(value - other.value)
    override fun times(other: Int16): Int16 = Int16(value * other.value)
    override fun div(other: Int16): Int16 = Int16(value / other.value)
    override fun rem(other: Int16): Int16 = Int16(value % other.value)

    override fun unaryMinus(): Int16 = Int16(-value)
    override fun inc(): Int16 = Int16(value.inc())
    override fun dec(): Int16 = Int16(value.dec())

    override fun inv(): Int16 = Int16(value.inv())
    override fun and(other: Int16): Int16 = Int16(value.toInt() and other.value.toInt())
    override fun or(other: Int16): Int16 = Int16(value.toInt() or other.value.toInt())
    override fun xor(other: Int16): Int16 = Int16(value.toInt() xor other.value.toInt())

    override fun shl(bits: Int16): Int16 = Int16(value.toInt() shl bits.value.toInt())
    override fun shr(bits: Int16): Int16 = Int16(value.toInt() shr bits.value.toInt())


    override fun plus(other: Int): Int16 = Int16(value + other)
    override fun plus(other: Long): Int16 = Int16(value + other)

    override fun minus(other: Int): Int16 = Int16(value - other)
    override fun minus(other: Long): Int16 = Int16(value - other)

    override fun times(other: Int): Int16 = Int16(value * other)
    override fun times(other: Long): Int16 = Int16(value * other)

    override fun div(other: Int): Int16 = Int16(value / other)
    override fun div(other: Long): Int16 = Int16(value / other)

    override fun rem(other: Int): Int16 = Int16(value % other)
    override fun rem(other: Long): Int16 = Int16(value % other)

    override fun and(other: Int): Int16 = Int16(value and other.toShort())
    override fun and(other: Long): Int16 = Int16(value and other.toShort())

    override fun or(other: Int): Int16 = Int16(value or other.toShort())
    override fun or(other: Long): Int16 = Int16(value or other.toShort())

    override fun xor(other: Int): Int16 = Int16(value xor other.toShort())
    override fun xor(other: Long): Int16 = Int16(value xor other.toShort())

    override fun shl(bits: Int): Int16 = Int16(value.toInt() shl bits)
    override fun shr(bits: Int): Int16 = Int16(value.toInt() shr bits)

    override fun compareTo(other: Int16): Int = value.compareTo(other.value)
    override fun compareTo(other: Long): Int = value.compareTo(other)
    override fun compareTo(other: Int): Int = value.compareTo(other)
    override fun equals(other: Any?): Boolean = if (other is Int16) value == other.value else false

    override fun toInt8(): Int8 = Int8(value.toByte())
    override fun toInt16(): Int16 = this
    override fun toInt32(): Int32 = Int32(value.toInt())
    override fun toInt64(): Int64 = Int64(value.toLong())
    override fun toInt128(): Int128 = Int128(BigInteger.fromShort(value))
    override fun toBigInt(): BigInt = BigInt(BigInteger.fromShort(value))
    override fun toUInt8(): UInt8 = UInt8(value.toUByte())
    override fun toUInt16(): UInt16 = UInt16(value.toUShort())
    override fun toUInt32(): UInt32 = UInt32(value.toUInt())
    override fun toUInt64(): UInt64 = UInt64(value.toULong())

    override fun toString(radix: Int): String = value.toString(radix)

    override fun hashCode(): Int {
        return value.hashCode()
    }

    override fun int8s() = (this shr bitWidth / 2).toInt8().int8s() + this.toInt8().int8s()

}