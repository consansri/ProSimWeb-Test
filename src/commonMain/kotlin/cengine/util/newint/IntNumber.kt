package cengine.util.newint

/**
 * Provides Integer Calculation Bases for different Sizes.
 *
 */
sealed interface IntNumber<T : Any> : Comparable<T> {

    companion object {
        fun bitMask(bitWidth: Int): Int {
            require(bitWidth <= 32) { "Can't create Int Bit Mask with more than 32 bits ($bitWidth)!" }

            if (bitWidth == 0) return 0

            return 1 shl bitWidth - 1
        }

        fun Collection<IntNumber<*>>.toArray(): Array<IntNumber<*>> = this.toTypedArray()
    }

    // Arithmetic Operations

    val bitWidth: Int

    /**
     * @param other will be converted to same type as first operand.
     */
    operator fun plus(other: T): IntNumber<T>
    operator fun minus(other: T): IntNumber<T>
    operator fun times(other: T): IntNumber<T>
    operator fun div(other: T): IntNumber<T>
    operator fun rem(other: T): IntNumber<T>

    operator fun unaryMinus(): IntNumber<T>
    operator fun inc(): IntNumber<T>
    operator fun dec(): IntNumber<T>

    // Binary Operations
    operator fun inv(): IntNumber<T>
    infix fun and(other: T): IntNumber<T>
    infix fun or(other: T): IntNumber<T>
    infix fun xor(other: T): IntNumber<T>
    infix fun shl(other: T): IntNumber<T>
    infix fun shr(other: T): IntNumber<T>

    // Operations with other Int
    operator fun plus(other: Int): IntNumber<T>
    operator fun minus(other: Int): IntNumber<T>
    infix fun and(other: Int): IntNumber<T>
    infix fun or(other: Int): IntNumber<T>
    infix fun xor(other: Int): IntNumber<T>
    infix fun shl(other: Int): IntNumber<T>
    infix fun shr(other: Int): IntNumber<T>

    // Comparison
    override fun compareTo(other: T): Int
    override fun equals(other: Any?): Boolean
    override fun hashCode(): Int

    // Conversion
    fun toInt8(): Int8
    fun toInt16(): Int16
    fun toInt32(): Int32
    fun toInt64(): Int64
    fun toInt128(): Int128
    fun toBigInt(): BigInt

    fun toUInt8(): UInt8
    fun toUInt16(): UInt16
    fun toUInt32(): UInt32
    fun toUInt64(): UInt64

    fun int8s(): List<Int8>
    fun int16s(): List<Int16> = int8s().toList().chunked(2) { bytes ->
        bytes.fold(Int16(0)) { acc, byte ->
            (acc shl 8) or (byte.toInt16() and 0xFF)
        }
    }

    fun int32s(): List<Int32> = int8s().toList().chunked(4) { bytes ->
        bytes.fold(Int32(0)) { acc, byte ->
            (acc shl 8) or (byte.toInt32() and 0xFF)
        }
    }

    fun int64s(): List<Int64> = int8s().toList().chunked(8) { bytes ->
        bytes.fold(Int64(0)) { acc, byte ->
            (acc shl 8) or (byte.toInt64() and 0xFF)
        }
    }

    fun uInt8s(): List<UInt8> = int8s().map { it.toUInt8() }
    fun uInt16s(): List<UInt16> = int8s().toList().chunked(2) { bytes ->
        bytes.fold(UInt16(0U)) { acc, byte ->
            (acc shl 8) or (byte.toUInt16() and 0xFF)
        }
    }

    fun uInt32s(): List<UInt32> = int8s().toList().chunked(4) { bytes ->
        bytes.fold(UInt32(0U)) { acc, byte ->
            (acc shl 8) or (byte.toUInt32() and 0xFF)
        }
    }

    fun uInt64s(): List<UInt64> = int8s().toList().chunked(8) { bytes ->
        bytes.fold(UInt64(0U)) { acc, byte ->
            (acc shl 8) or (byte.toUInt64() and 0xFF)
        }
    }

    // Transformation
    fun toString(radix: Int = 10): String


}