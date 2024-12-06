package cengine.util.newint

import com.ionspin.kotlin.bignum.integer.BigInteger

class UInt64(private val value: ULong): IntNumber {

    override fun plus(other: IntNumber): UInt64 = if (other is UInt64) UInt64(value + other.value) else throw IllegalArgumentException("$other must be an UInt64 integer!")
    override fun minus(other: IntNumber): UInt64 = if (other is UInt64) UInt64(value - other.value) else throw IllegalArgumentException("$other must be an UInt64 integer!")
    override fun times(other: IntNumber): UInt64 = if (other is UInt64) UInt64(value * other.value) else throw IllegalArgumentException("$other must be an UInt64 integer!")
    override fun div(other: IntNumber): UInt64 = if (other is UInt64) UInt64(value / other.value) else throw IllegalArgumentException("$other must be an UInt64 integer!")
    override fun rem(other: IntNumber): UInt64 = if (other is UInt64) UInt64(value % other.value) else throw IllegalArgumentException("$other must be an UInt64 integer!")

    override fun unaryMinus(): UInt64 = throw Exception("Can't negotiate unsigned value!")
    override fun inc(): UInt64 = UInt64(value.inc())
    override fun dec(): UInt64 = UInt64(value.dec())

    override fun inv(): IntNumber = UInt64(value.inv())
    override fun and(other: IntNumber): UInt64 = if (other is UInt64) UInt64(value and other.value) else throw IllegalArgumentException("$other must be an UInt64 integer!")
    override fun or(other: IntNumber): UInt64 = if (other is UInt64) UInt64(value or other.value) else throw IllegalArgumentException("$other must be an UInt64 integer!")
    override fun xor(other: IntNumber): UInt64 = if (other is UInt64) UInt64(value xor other.value) else throw IllegalArgumentException("$other must be an UInt64 integer!")

    override fun shl(other: IntNumber): UInt64 = if (other is UInt64) UInt64(value shl other.value.toInt()) else throw IllegalArgumentException("$other must be an UInt64 integer!")
    override fun shr(other: IntNumber): UInt64 = if (other is UInt64) UInt64(value shr other.value.toInt()) else throw IllegalArgumentException("$other must be an UInt64 integer!")

    override fun compareTo(other: IntNumber): Int = if (other is UInt64) value.compareTo(other.value) else throw IllegalArgumentException("$other must be an UInt64 integer!")
    override fun equals(other: Any?): Boolean = if (other is UInt64) value == other.value else false

    override fun toInt32(): Int32 = Int32(value.toInt())
    override fun toInt64(): Int64 = Int64(value.toLong())
    override fun toInt128(): Int128 = Int128(BigInteger.fromULong(value))
    override fun toBigInt(): BigInt = BigInt(BigInteger.fromULong(value))
    override fun toUInt32(): UInt32 = UInt32(value.toUInt())
    override fun toUInt64(): UInt64 = this

    override fun toString(radix: Int): String = value.toString(radix)

    override fun hashCode(): Int = value.hashCode()

}