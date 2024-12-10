package cengine.util.newint

import com.ionspin.kotlin.bignum.integer.BigInteger
import kotlin.experimental.and
import kotlin.experimental.inv
import kotlin.experimental.or
import kotlin.experimental.xor

class Int8(override val value: Byte) : IntNumber<Int8> {

    constructor(value: Int) : this(value.toByte())
    constructor(value: Long) : this(value.toByte())

    companion object {
        fun Byte.toInt8() = Int8(this)

        val ZERO = Int8(0)
        val ONE = Int8(1)

        fun String.parseInt8(radix: Int): Int8 = Int8(toByte(radix))
    }

    override val bitWidth: Int
        get() = 8

    override val byteCount: Int
        get() = 1


    override fun plus(other: Int8): Int8 = Int8(value + other.value)
    override fun minus(other: Int8): Int8 = Int8(value - other.value)
    override fun times(other: Int8): Int8 = Int8(value * other.value)
    override fun div(other: Int8): Int8 = Int8(value / other.value)
    override fun rem(other: Int8): Int8 = Int8(value % other.value)
    override fun times(other: Int): Int8 = Int8(value * other)
    override fun times(other: Long): Int8 = Int8(value * other)

    override fun div(other: Int): Int8 = Int8(value / other)
    override fun div(other: Long): Int8 = Int8(value / other)

    override fun rem(other: Int): Int8 = Int8(value % other)
    override fun rem(other: Long): Int8 = Int8(value % other)

    override fun unaryMinus(): Int8 = Int8(-value)
    override fun inc(): Int8 = Int8(value.inc())
    override fun dec(): Int8 = Int8(value.dec())
    override fun compareTo(other: Long): Int = value.compareTo(other)
    override fun compareTo(other: Int): Int = value.compareTo(other)

    override fun inv(): Int8 = Int8(value.inv())

    override fun plus(other: Int): Int8 = Int8(value + other)
    override fun plus(other: Long): Int8 = Int8(value + other)

    override fun minus(other: Int): Int8 = Int8(value - other)
    override fun minus(other: Long): Int8 = Int8(value - other)

    override fun and(other: Int): Int8 = Int8(value and other.toByte())
    override fun and(other: Long): Int8 = Int8(value and other.toByte())

    override fun or(other: Int): Int8 = Int8(value or other.toByte())
    override fun or(other: Long): Int8 = Int8(value or other.toByte())

    override fun xor(other: Int): Int8 = Int8(value xor other.toByte())
    override fun xor(other: Long): Int8 = Int8(value xor other.toByte())

    override fun shl(bits: Int): Int8 = Int8(value.toInt() shl bits)
    override fun shr(bits: Int): Int8 = Int8(value.toInt() shr bits)

    override fun and(other: Int8): Int8 = Int8(value.toInt() and other.value.toInt())
    override fun or(other: Int8): Int8 = Int8(value.toInt() or other.value.toInt())
    override fun xor(other: Int8): Int8 = Int8(value.toInt() xor other.value.toInt())

    override fun shl(bits: Int8): Int8 = Int8(value.toInt() shl bits.value.toInt())
    override fun shr(bits: Int8): Int8 = Int8(value.toInt() shr bits.value.toInt())

    override fun compareTo(other: Int8): Int = value.compareTo(other.value)
    override fun equals(other: Any?): Boolean = if (other is Int8) value == other.value else false

    override fun toInt8(): Int8 = this
    override fun toInt16(): Int16 = Int16(value.toShort())
    override fun toInt32(): Int32 = Int32(value.toInt())
    override fun toInt64(): Int64 = Int64(value.toLong())
    override fun toInt128(): Int128 = Int128(BigInteger.fromByte(value))
    override fun toBigInt(): BigInt = BigInt(BigInteger.fromByte(value))
    override fun toUInt8(): UInt8 = UInt8(value.toUByte())
    override fun toUInt16(): UInt16 = UInt16(value.toUShort())
    override fun toUInt32(): UInt32 = UInt32(value.toUInt())
    override fun toUInt64(): UInt64 = UInt64(value.toULong())

    override fun toString(radix: Int): String = value.toString(radix)
    override fun int8s() = listOf(this)

    override fun hashCode(): Int {
        return value.hashCode()
    }
}