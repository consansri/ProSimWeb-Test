package cengine.util.newint

import com.ionspin.kotlin.bignum.integer.BigInteger
import com.ionspin.kotlin.bignum.integer.Sign
import com.ionspin.kotlin.bignum.integer.toBigInteger

class Int128(value: BigInteger) : IntNumber<Int128> {

    override val value: BigInteger = value.truncateTo128Bits()

    companion object {
        private val MASK_128 = BigInteger.fromByteArray(ByteArray(16) { 0xFF.toByte() }, Sign.POSITIVE)  // 2^128 - 1

        /** Enforces 128-bit range by truncating the value. */
        private fun BigInteger.truncateTo128Bits(): BigInteger = and(MASK_128)

        val ZERO = Int128(BigInteger.ZERO)
        val ONE = Int128(BigInteger.ONE)

        fun String.parseInt128(radix: Int): Int128 = Int128(BigInteger.parseString(this, radix))

        fun fromUInt64(value1: UInt64, value0: UInt64): Int128 = (value1.toInt128() shl 64) or value0.toInt128()
    }

    override val bitWidth: Int
        get() = 128

    override val byteCount: Int
        get() = 16

    override fun plus(other: Int128): Int128 = Int128(value + other.value)
    override fun minus(other: Int128): Int128 = Int128(value - other.value)
    override fun times(other: Int128): Int128 = Int128(value * other.value)
    override fun div(other: Int128): Int128 = Int128(value / other.value)
    override fun rem(other: Int128): Int128 = Int128(value % other.value)

    override fun unaryMinus(): Int128 = Int128(value.negate())
    override fun inc(): Int128 = Int128(value.inc())
    override fun dec(): Int128 = Int128(value.dec())

    override fun inv(): Int128 = Int128(value.not())
    override fun and(other: Int128): Int128 = Int128(value and other.value)
    override fun or(other: Int128): Int128 = Int128(value or other.value)
    override fun xor(other: Int128): Int128 = Int128(value xor other.value)

    override fun shl(bits: Int128): Int128 = Int128(value shl bits.value.intValue())
    override fun shr(bits: Int128): Int128 = Int128(value shr bits.value.intValue())


    override fun plus(other: Int): Int128 = Int128(value + other)
    override fun plus(other: Long): Int128 = Int128(value + other)

    override fun minus(other: Int): Int128 = Int128(value - other)
    override fun minus(other: Long): Int128 = Int128(value - other)

    override fun times(other: Int): Int128 = Int128(value * other)
    override fun times(other: Long): Int128 = Int128(value * other)

    override fun div(other: Int): Int128 = Int128(value / other)
    override fun div(other: Long): Int128 = Int128(value / other)

    override fun rem(other: Int): Int128 = Int128(value % other)
    override fun rem(other: Long): Int128 = Int128(value % other)

    override fun and(other: Int): Int128 = Int128(value and other.toBigInteger())
    override fun and(other: Long): Int128 = Int128(value and other.toBigInteger())

    override fun or(other: Int): Int128 = Int128(value or other.toBigInteger())
    override fun or(other: Long): Int128 = Int128(value or other.toBigInteger())

    override fun xor(other: Int): Int128 = Int128(value xor other.toBigInteger())
    override fun xor(other: Long): Int128 = Int128(value xor other.toBigInteger())

    override fun shl(bits: Int): Int128 = Int128(value shl bits)
    override fun shr(bits: Int): Int128 = Int128(value shr bits)


    override fun compareTo(other: Int128): Int = value.compareTo(other.value)
    override fun compareTo(other: Long): Int = value.compareTo(other)
    override fun compareTo(other: Int): Int = value.compareTo(other)
    override fun equals(other: Any?): Boolean = if (other is Int128) value == other.value else false

    override fun toInt8(): Int8 = Int8(value.byteValue())
    override fun toInt16(): Int16 = Int16(value.shortValue())
    override fun toInt32(): Int32 = Int32(value.intValue())
    override fun toInt64(): Int64 = Int64(value.longValue())
    override fun toInt128(): Int128 = this
    override fun toBigInt(): BigInt = BigInt(value)
    override fun toUInt8(): UInt8 = UInt8(value.ubyteValue())
    override fun toUInt16(): UInt16 = UInt16(value.ushortValue())
    override fun toUInt32(): UInt32 = UInt32(value.uintValue())
    override fun toUInt64(): UInt64 = UInt64(value.ulongValue())

    override fun toString(radix: Int): String = value.toString(radix)
    override fun fitsInSigned(bitWidth: Int): Boolean {
        if (bitWidth >= bitWidth) return true
        val minValue = -(ONE shl (bitWidth - 1)) // -2^(bitWidth-1)
        val maxValue = (ONE shl (bitWidth - 1)) - 1 // 2^(bitWidth-1) - 1
        return value in minValue.value..maxValue.value
    }

    override fun fitsInUnsigned(bitWidth: Int): Boolean {
        if (bitWidth >= bitWidth) return true
        val maxValue = (ONE shl bitWidth) - 1 // 2^bitWidth - 1
        return value in ZERO.value..maxValue.value
    }

    override fun hashCode(): Int = value.hashCode()

    override fun int8s() = (this shr bitWidth / 2).toInt64().int8s() + this.toInt64().int8s()

}