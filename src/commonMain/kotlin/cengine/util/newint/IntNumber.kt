package cengine.util.newint

/**
 * Provides Integer Calculation Bases for different Sizes.
 *
 */
sealed interface IntNumber: Comparable<IntNumber> {

    // Arithmetic Operations
    operator fun plus(other: IntNumber): IntNumber
    operator fun minus(other: IntNumber): IntNumber
    operator fun times(other: IntNumber): IntNumber
    operator fun div(other: IntNumber): IntNumber
    operator fun rem(other: IntNumber): IntNumber

    operator fun unaryMinus(): IntNumber
    operator fun inc(): IntNumber
    operator fun dec(): IntNumber

    // Binary Operations
    operator fun inv(): IntNumber
    infix fun and(other: IntNumber): IntNumber
    infix fun or(other: IntNumber): IntNumber
    infix fun xor(other: IntNumber): IntNumber
    infix fun shl(other: IntNumber): IntNumber
    infix fun shr(other: IntNumber): IntNumber

    // Comparison
    override fun compareTo(other: IntNumber): Int
    override fun equals(other: Any?): Boolean
    override fun hashCode(): Int

    // Conversion
    fun toInt32(): Int32
    fun toInt64(): Int64
    fun toInt128(): Int128
    fun toBigInt(): BigInt

    fun toUInt32(): UInt32
    fun toUInt64(): UInt64

    // Transformation
    fun toString(radix: Int = 10): String

}