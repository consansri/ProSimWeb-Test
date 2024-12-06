package cengine.util.newint

import com.ionspin.kotlin.bignum.integer.BigInteger

class Int64(private val value: Long) : IntNumber {

    override fun plus(other: IntNumber): Int64 = if (other is Int64) Int64(value + other.value) else throw IllegalArgumentException("$other must be an Int64 integer!")
    override fun minus(other: IntNumber): Int64 = if (other is Int64) Int64(value - other.value) else throw IllegalArgumentException("$other must be an Int64 integer!")
    override fun times(other: IntNumber): Int64 = if (other is Int64) Int64(value * other.value) else throw IllegalArgumentException("$other must be an Int64 integer!")
    override fun div(other: IntNumber): Int64 = if (other is Int64) Int64(value / other.value) else throw IllegalArgumentException("$other must be an Int64 integer!")
    override fun rem(other: IntNumber): Int64 = if (other is Int64) Int64(value % other.value) else throw IllegalArgumentException("$other must be an Int64 integer!")

    override fun unaryMinus(): Int64 = Int64(-value)
    override fun inc(): Int64 = Int64(value.inc())
    override fun dec(): Int64 = Int64(value.dec())

    override fun inv(): IntNumber = Int64(value.inv())
    override fun and(other: IntNumber): Int64 = if (other is Int64) Int64(value and other.value) else throw IllegalArgumentException("$other must be an Int64 integer!")
    override fun or(other: IntNumber): Int64 = if (other is Int64) Int64(value or other.value) else throw IllegalArgumentException("$other must be an Int64 integer!")
    override fun xor(other: IntNumber): Int64 = if (other is Int64) Int64(value xor other.value) else throw IllegalArgumentException("$other must be an Int64 integer!")

    override fun shl(other: IntNumber): Int64 = if (other is Int64) Int64(value shl other.value.toInt()) else throw IllegalArgumentException("$other must be an Int64 integer!")
    override fun shr(other: IntNumber): Int64 = if (other is Int64) Int64(value shr other.value.toInt()) else throw IllegalArgumentException("$other must be an Int64 integer!")

    override fun compareTo(other: IntNumber): Int = if (other is Int64) value.compareTo(other.value) else throw IllegalArgumentException("$other must be an Int64 integer!")
    override fun equals(other: Any?): Boolean = if (other is Int64) value == other.value else false

    override fun toInt32(): Int32 = Int32(value.toInt())
    override fun toInt64(): Int64 = this
    override fun toInt128(): Int128 = Int128(BigInteger.fromLong(value))
    override fun toBigInt(): BigInt = BigInt(BigInteger.fromLong(value))
    override fun toUInt32(): UInt32 = UInt32(value.toUInt())
    override fun toUInt64(): UInt64 = UInt64(value.toULong())

    override fun toString(radix: Int): String = value.toString(radix)

    override fun hashCode(): Int = value.hashCode()

}