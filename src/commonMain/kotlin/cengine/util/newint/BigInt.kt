package cengine.util.newint

import com.ionspin.kotlin.bignum.integer.BigInteger

data class BigInt(private val value: BigInteger): IntNumber {

    override fun plus(other: IntNumber): BigInt = if (other is BigInt) BigInt(value + other.value) else throw IllegalArgumentException("$other must be a BigInt integer!")
    override fun minus(other: IntNumber): BigInt = if (other is BigInt) BigInt(value - other.value) else throw IllegalArgumentException("$other must be a BigInt integer!")
    override fun times(other: IntNumber): BigInt = if (other is BigInt) BigInt(value * other.value) else throw IllegalArgumentException("$other must be a BigInt integer!")
    override fun div(other: IntNumber): BigInt = if (other is BigInt) BigInt(value / other.value) else throw IllegalArgumentException("$other must be a BigInt integer!")
    override fun rem(other: IntNumber): BigInt = if (other is BigInt) BigInt(value % other.value) else throw IllegalArgumentException("$other must be a BigInt integer!")

    override fun unaryMinus(): BigInt = BigInt(value.negate())
    override fun inc(): BigInt = BigInt(value.inc())
    override fun dec(): BigInt = BigInt(value.dec())

    override fun inv(): IntNumber = BigInt(value.not())
    override fun and(other: IntNumber): BigInt = if (other is BigInt) BigInt(value and other.value) else throw IllegalArgumentException("$other must be a BigInt integer!")
    override fun or(other: IntNumber): BigInt = if (other is BigInt) BigInt(value or other.value) else throw IllegalArgumentException("$other must be a BigInt integer!")
    override fun xor(other: IntNumber): BigInt = if (other is BigInt) BigInt(value xor other.value) else throw IllegalArgumentException("$other must be a BigInt integer!")

    override fun shl(other: IntNumber): BigInt = if (other is BigInt) BigInt(value shl other.value.intValue()) else throw IllegalArgumentException("$other must be a BigInt integer!")
    override fun shr(other: IntNumber): BigInt = if (other is BigInt) BigInt(value shr other.value.intValue()) else throw IllegalArgumentException("$other must be a BigInt integer!")

    override fun compareTo(other: IntNumber): Int = if (other is BigInt) value.compareTo(other.value) else throw IllegalArgumentException("$other must be a BigInt integer!")
    override fun equals(other: Any?): Boolean = if (other is BigInt) value == other.value else false

    override fun toInt32(): Int32 = Int32(value.intValue())
    override fun toInt64(): Int64 = Int64(value.longValue())
    override fun toInt128(): Int128 = Int128(value)
    override fun toBigInt(): BigInt = this
    override fun toUInt32(): UInt32 = UInt32(value.uintValue())
    override fun toUInt64(): UInt64 = UInt64(value.ulongValue())

    override fun toString(radix: Int): String = value.toString(radix)

    override fun hashCode(): Int = value.hashCode()
    
}