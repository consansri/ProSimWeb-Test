package cengine.util.newint

import com.ionspin.kotlin.bignum.integer.BigInteger

class UInt16(private val value: UShort) : IntNumber<UInt16> {

    constructor(value: UInt) : this(value.toUShort())

    companion object {
        fun UShort.toUInt16() = UInt16(this)

        fun String.parseUInt16(radix: Int): UInt16 = UInt16(toUShort(radix))
    }

    override val bitWidth: Int
        get() = 16

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

    override fun shl(other: UInt16): UInt16 = UInt16(value.toUInt() shl other.value.toInt())
    override fun shr(other: UInt16): UInt16 = UInt16(value.toUInt() shr other.value.toInt())


    override fun plus(other: Int): UInt16 = UInt16(value + other.toUInt())
    override fun minus(other: Int): UInt16 = UInt16(value - other.toUInt())
    override fun and(other: Int): UInt16 = UInt16(value and other.toUShort())
    override fun or(other: Int): UInt16 = UInt16(value or other.toUShort())
    override fun xor(other: Int): UInt16 = UInt16(value xor other.toUShort())
    override fun shl(other: Int): UInt16 = UInt16(value.toUInt() shl other)
    override fun shr(other: Int): UInt16 = UInt16(value.toUInt() shr other)


    override fun compareTo(other: UInt16): Int = value.compareTo(other.value)
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

    override fun int8s(): Array<Int8> = (this shr bitWidth / 2).toUInt8().int8s() + this.toUInt8().int8s()
}