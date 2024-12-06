package cengine.util.newint

import com.ionspin.kotlin.bignum.integer.BigInteger
import com.ionspin.kotlin.bignum.integer.Sign

class Int128(value: BigInteger) : IntNumber {

    private val value: BigInteger = value.truncateTo128Bits()

    companion object {
        private val MASK_128 = BigInteger.fromByteArray(ByteArray(16) { 0xFF.toByte() }, Sign.POSITIVE)  // 2^128 - 1

        /** Enforces 128-bit range by truncating the value. */
        private fun BigInteger.truncateTo128Bits(): BigInteger = and(MASK_128)
    }

    override fun plus(other: IntNumber): Int128 = if (other is Int128) Int128(value + other.value) else throw IllegalArgumentException("$other must be a Int128 integer!")
    override fun minus(other: IntNumber): Int128 = if (other is Int128) Int128(value - other.value) else throw IllegalArgumentException("$other must be a Int128 integer!")
    override fun times(other: IntNumber): Int128 = if (other is Int128) Int128(value * other.value) else throw IllegalArgumentException("$other must be a Int128 integer!")
    override fun div(other: IntNumber): Int128 = if (other is Int128) Int128(value / other.value) else throw IllegalArgumentException("$other must be a Int128 integer!")
    override fun rem(other: IntNumber): Int128 = if (other is Int128) Int128(value % other.value) else throw IllegalArgumentException("$other must be a Int128 integer!")

    override fun unaryMinus(): Int128 = Int128(value.negate())
    override fun inc(): Int128 = Int128(value.inc())
    override fun dec(): Int128 = Int128(value.dec())

    override fun inv(): IntNumber = Int128(value.not())
    override fun and(other: IntNumber): Int128 = if (other is Int128) Int128(value and other.value) else throw IllegalArgumentException("$other must be a Int128 integer!")
    override fun or(other: IntNumber): Int128 = if (other is Int128) Int128(value or other.value) else throw IllegalArgumentException("$other must be a Int128 integer!")
    override fun xor(other: IntNumber): Int128 = if (other is Int128) Int128(value xor other.value) else throw IllegalArgumentException("$other must be a Int128 integer!")

    override fun shl(other: IntNumber): Int128 = if (other is Int128) Int128(value shl other.value.intValue()) else throw IllegalArgumentException("$other must be a Int128 integer!")
    override fun shr(other: IntNumber): Int128 = if (other is Int128) Int128(value shr other.value.intValue()) else throw IllegalArgumentException("$other must be a Int128 integer!")

    override fun compareTo(other: IntNumber): Int = if (other is Int128) value.compareTo(other.value) else throw IllegalArgumentException("$other must be a Int128 integer!")
    override fun equals(other: Any?): Boolean = if (other is Int128) value == other.value else false

    override fun toInt32(): Int32 = Int32(value.intValue())
    override fun toInt64(): Int64 = Int64(value.longValue())
    override fun toInt128(): Int128 = this
    override fun toBigInt(): BigInt = BigInt(value)
    override fun toUInt32(): UInt32 = UInt32(value.uintValue())
    override fun toUInt64(): UInt64 = UInt64(value.ulongValue())

    override fun toString(radix: Int): String = value.toString(radix)

    override fun hashCode(): Int = value.hashCode()

}