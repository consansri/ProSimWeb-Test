package cengine.util.newint

import com.ionspin.kotlin.bignum.integer.BigInteger

class Int32(private val value: Int) : IntNumber {
    override fun plus(other: IntNumber): Int32 = if (other is Int32) Int32(value + other.value) else throw IllegalArgumentException("$other must be an Int32 integer!")
    override fun minus(other: IntNumber): Int32 = if (other is Int32) Int32(value - other.value) else throw IllegalArgumentException("$other must be an Int32 integer!")
    override fun times(other: IntNumber): Int32 = if (other is Int32) Int32(value * other.value) else throw IllegalArgumentException("$other must be an Int32 integer!")
    override fun div(other: IntNumber): Int32 = if (other is Int32) Int32(value / other.value) else throw IllegalArgumentException("$other must be an Int32 integer!")
    override fun rem(other: IntNumber): Int32 = if (other is Int32) Int32(value % other.value) else throw IllegalArgumentException("$other must be an Int32 integer!")

    override fun unaryMinus(): Int32 = Int32(-value)
    override fun inc(): Int32 = Int32(value.inc())
    override fun dec(): Int32 = Int32(value.dec())

    override fun inv(): Int32 = Int32(value.inv())
    override fun and(other: IntNumber): Int32 = if(other is Int32) Int32(value and other.value) else throw IllegalArgumentException("$other must be an Int32 integer!")
    override fun or(other: IntNumber): Int32 = if(other is Int32) Int32(value or other.value) else throw IllegalArgumentException("$other must be an Int32 integer!")
    override fun xor(other: IntNumber): Int32 = if (other is Int32) Int32(value xor other.value) else throw IllegalArgumentException("$other must be an Int32 integer!")

    override fun shl(other: IntNumber): Int32 = if(other is Int32) Int32(value shl other.value) else throw IllegalArgumentException("$other must be an Int32 integer!")
    override fun shr(other: IntNumber): Int32 = if(other is Int32) Int32(value shr other.value) else throw IllegalArgumentException("$other must be an Int32 integer!")

    override fun compareTo(other: IntNumber): Int = if(other is Int32) value.compareTo(other.value) else throw IllegalArgumentException("$other must be an Int32 integer!")
    override fun equals(other: Any?): Boolean = if(other is Int32) value == other.value else false

    override fun toInt32(): Int32 = this
    override fun toInt64(): Int64 = Int64(value.toLong())
    override fun toInt128(): Int128 = Int128(BigInteger.fromInt(value))
    override fun toBigInt(): BigInt = BigInt(BigInteger.fromInt(value))
    override fun toUInt32(): UInt32 = UInt32(value.toUInt())
    override fun toUInt64(): UInt64 = UInt64(value.toULong())

    override fun toString(radix: Int): String = value.toString(radix)

    override fun hashCode(): Int {
        return value.hashCode()
    }
}