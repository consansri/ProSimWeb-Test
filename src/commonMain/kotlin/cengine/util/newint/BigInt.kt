package cengine.util.newint

import cengine.util.newint.Int8.Companion.toInt8
import com.ionspin.kotlin.bignum.integer.BigInteger
import com.ionspin.kotlin.bignum.integer.toBigInteger

data class BigInt(val value: BigInteger) : IntNumber<BigInt> {

    override val bitWidth: Int
        get() = value.bitLength()

    override val byteCount: Int
        get() = value.toByteArray().size


    companion object {
        fun String.parseBigInt(radix: Int): BigInt = BigInt(BigInteger.parseString(this, radix))

        val ZERO = BigInt(BigInteger.ZERO)
        val ONE = BigInt(BigInteger.ONE)

        fun Int.toBigInt(): BigInt = BigInt(this.toBigInteger())
        fun Long.toBigInt(): BigInt = BigInt(this.toBigInteger())
        fun UInt.toBigInt(): BigInt = BigInt(this.toBigInteger())
        fun ULong.toBigInt(): BigInt = BigInt(this.toBigInteger())
    }

    override fun plus(other: BigInt): BigInt = BigInt(value + other.value)
    override fun minus(other: BigInt): BigInt = BigInt(value - other.value)
    override fun times(other: BigInt): BigInt = BigInt(value * other.value)
    override fun div(other: BigInt): BigInt = BigInt(value / other.value)
    override fun rem(other: BigInt): BigInt = BigInt(value % other.value)

    override fun unaryMinus(): BigInt = BigInt(value.negate())
    override fun inc(): BigInt = BigInt(value.inc())
    override fun dec(): BigInt = BigInt(value.dec())

    override fun inv(): BigInt = BigInt(value.not())

    override fun plus(other: Int): BigInt = BigInt(value + other)
    override fun minus(other: Int): BigInt = BigInt(value - other)
    override fun and(other: Int): BigInt = BigInt(value and other.toBigInteger())
    override fun or(other: Int): BigInt = BigInt(value or other.toBigInteger())
    override fun xor(other: Int): BigInt = BigInt(value xor other.toBigInteger())
    override fun shl(other: Int): BigInt = BigInt(value shl other)
    override fun shr(other: Int): BigInt = BigInt(value shr other)

    override fun and(other: BigInt): BigInt = BigInt(value and other.value)
    override fun or(other: BigInt): BigInt = BigInt(value or other.value)
    override fun xor(other: BigInt): BigInt = BigInt(value xor other.value)

    override fun shl(other: BigInt): BigInt = BigInt(value shl other.value.intValue())
    override fun shr(other: BigInt): BigInt = BigInt(value shr other.value.intValue())

    override fun compareTo(other: BigInt): Int = value.compareTo(other.value)
    override fun equals(other: Any?): Boolean = if (other is BigInt) value == other.value else false

    override fun toInt8(): Int8 = Int8(value.byteValue())
    override fun toInt16(): Int16 = Int16(value.shortValue())
    override fun toInt32(): Int32 = Int32(value.intValue())
    override fun toInt64(): Int64 = Int64(value.longValue())
    override fun toInt128(): Int128 = Int128(value)
    override fun toBigInt(): BigInt = this

    override fun toUInt8(): UInt8 = UInt8(value.ubyteValue())
    override fun toUInt16(): UInt16 = UInt16(value.ushortValue())
    override fun toUInt32(): UInt32 = UInt32(value.uintValue())
    override fun toUInt64(): UInt64 = UInt64(value.ulongValue())

    override fun toString(radix: Int): String = value.toString(radix)

    override fun hashCode(): Int = value.hashCode()

    override fun int8s() = value.toByteArray().map { it.toInt8() }

}