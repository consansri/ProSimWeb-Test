package cengine.util.newint

/**
 * Provides Integer Calculation Bases for different Sizes.
 *
 */
sealed class IntNumber : Comparable<IntNumber> {

    // Arithmetic Operations
    abstract operator fun plus(other: IntNumber): IntNumber
    abstract operator fun minus(other: IntNumber): IntNumber
    abstract operator fun times(other: IntNumber): IntNumber
    abstract operator fun div(other: IntNumber): IntNumber
    abstract operator fun rem(other: IntNumber): IntNumber

    abstract operator fun unaryMinus(): IntNumber
    abstract operator fun inc(): IntNumber
    abstract operator fun dec(): IntNumber

    // Binary Operations
    abstract operator fun inv(): IntNumber
    abstract infix fun and(other: IntNumber): IntNumber
    abstract infix fun or(other: IntNumber): IntNumber
    abstract infix fun xor(other: IntNumber): IntNumber
    abstract infix fun shl(other: IntNumber): IntNumber
    abstract infix fun shr(other: IntNumber): IntNumber

    // Comparison
    abstract override fun compareTo(other: IntNumber): Int
    abstract override fun equals(other: Any?): Boolean
    abstract override fun hashCode(): Int

    // Conversion
    abstract fun toInt32(): Int32
    abstract fun toInt64(): Int64
    abstract fun toInt128(): Int128
    abstract fun toBigInt(): BigInt

    abstract fun toUInt32(): UInt32
    abstract fun toUInt64(): UInt64

    // Transformation
    abstract fun toString(radix: Int = 10): String

}