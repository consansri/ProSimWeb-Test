package cengine.util.newint

/**
 * Provides Integer Calculation Bases for different Sizes.
 *
 */
sealed interface IntNumber<T : IntNumber<T>> : ArithOperationProvider<T, T>, LogicOperationProvider<T, T> {

    companion object {
        fun IntRange.overlaps(other: IntRange): Boolean {
            return this.first <= other.last && other.first <= this.last
        }

        fun bitMask(bitWidth: Int): Int {
            require(bitWidth <= 32) { "Can't create Int Bit Mask with more than 32 bits ($bitWidth)!" }

            if (bitWidth == 0) return 0

            return (1 shl bitWidth) - 1
        }

        fun String.parse(radix: Int, type: IntNumberStatic<*>): IntNumber<*> = type.parse(this, radix)

        fun String.parseAnyUInt(radix: Int, byteCount: Int): IntNumber<*> {
            require(byteCount > 0) { "Illegal byteCount $byteCount to parse ${IntNumber::class} from!" }
            return when (byteCount) {
                1 -> parse(radix, UInt8)
                2 -> parse(radix, UInt16)
                4 -> parse(radix, UInt32)
                8 -> parse(radix, UInt64)
                16 -> parse(radix, UInt128)
                else -> parse(radix, BigInt)
            }
        }

        fun String.parseAnyInt(radix: Int, byteCount: Int): IntNumber<*> {
            require(byteCount > 0) { "Illegal byteCount $byteCount to parse ${IntNumber::class} from!" }
            return when (byteCount) {
                1 -> parse(radix, Int8)
                2 -> parse(radix, Int16)
                4 -> parse(radix, Int32)
                8 -> parse(radix, Int64)
                16 -> parse(radix, Int128)
                else -> parse(radix, BigInt)
            }
        }

        fun <T : IntNumber<T>> Collection<UInt8>.mergeToIntNumbers(
            createFromBytes: (List<UInt8>) -> T,
            chunkSize: Int,
        ): List<T> {
            require(chunkSize > 0) { "Chunk size must be greater than 0." }

            return this.chunked(chunkSize) { chunk ->
                createFromBytes(chunk)
            }
        }

        fun Collection<IntNumber<*>>.toArray(): Array<IntNumber<*>> = this.toTypedArray()
    }

    // Arithmetic Operations
    val value: Any
    override val bitWidth: Int
    val byteCount: Int

    /**
     * @param index 0 ..<[bitWidth]
     */
    fun bit(index: Int): T = (this shr index) and 1

    /**
     * @param other will be converted to same type as first operand.
     */
    operator fun inc(): T
    operator fun dec(): T

    infix fun rol(bits: T): T {
        val shift = bits % bitWidth // Ensure the shift is within bounds
        return (this shl shift) or (this shr (-shift + bitWidth))
    }

    infix fun ror(bits: T): T {
        val shift = bits % bitWidth // Ensure the shift is within bounds
        return (this shr shift) or (this shl (-shift + bitWidth))
    }

    infix fun rol(bits: Int): T {
        val shift = bits % bitWidth // Ensure the shift is within bounds
        return (this shl shift) or (this shr (-shift + bitWidth))
    }

    infix fun ror(bits: Int): T {
        val shift = bits % bitWidth // Ensure the shift is within bounds
        return (this shr shift) or (this shl (-shift + bitWidth))
    }

    /**
     * Extends the sign bit of a given subset of bits to the full bit width of the value.
     *
     * @param subsetBitWidth The number of bits in the subset to sign-extend. Must be in 0..64!
     * @return The sign-extended value.
     */
    fun signExtension(subsetBitWidth: Int, one: T): T {
        require(subsetBitWidth in 1..bitWidth) {
            "subsetBitWidth must be in the range 1 to bitWidth ($bitWidth)."
        }

        // Create a mask for the subsetBitWidth
        val mask = (one shl subsetBitWidth) - one // Mask to extract the relevant subset

        // Extract the subset of bits
        val subset = this and mask

        // Check the sign bit of the subset
        val signBitPosition = subsetBitWidth - 1
        val signBit = subset and (one shl signBitPosition)

        return if (signBit.toInt() == 0) {
            // If the sign bit is 0, the value is positive, so return the subset directly
            subset
        } else {
            // If the sign bit is 1, extend the sign to the full bit width
            val extensionMask = ((one shl (bitWidth - subsetBitWidth)) - one) shl subsetBitWidth
            subset or extensionMask
        }
    }

    // Comparison
    operator fun compareTo(other: T): Int
    operator fun compareTo(other: Long): Int
    operator fun compareTo(other: Int): Int

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
    fun toUInt128(): UInt128

    fun int8s(): List<Int8>
    fun int16s(): List<Int16> = int8s().toList().chunked(2) { bytes ->
        bytes.fold(Int16.ZERO) { acc, byte ->
            (acc shl 8) or (byte.toInt16() and 0xFF)
        }
    }

    fun int32s(): List<Int32> = int8s().toList().chunked(4) { bytes ->
        bytes.fold(Int32.ZERO) { acc, byte ->
            (acc shl 8) or (byte.toInt32() and 0xFF)
        }
    }

    fun int64s(): List<Int64> = int8s().toList().chunked(8) { bytes ->
        bytes.fold(Int64.ZERO) { acc, byte ->
            (acc shl 8) or (byte.toInt64() and 0xFF)
        }
    }

    fun int128s(): List<Int128> = int8s().toList().chunked(16) { bytes ->
        bytes.fold(Int128.ZERO) { acc, byte ->
            (acc shl 8) or (byte.toInt128() and 0xFF)
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
    fun uInt128s(): List<UInt128> = int8s().toList().chunked(16) { bytes ->
        bytes.fold(UInt128.ZERO) { acc, byte ->
            (acc shl 8) or (byte.toUInt128() and 0xFF)
        }
    }

    // Kotlin integer type conversion
    fun toByte(): Byte = toInt8().value
    fun toUByte(): UByte = toUInt8().value
    fun toShort(): Short = toInt16().value
    fun toUShort(): UShort = toUInt16().value
    fun toInt(): Int = toInt32().value
    fun toUInt(): UInt = toUInt32().value
    fun toLong(): Long = toInt64().value
    fun toULong(): ULong = toUInt64().value

    // Transformation
    override fun toString(): String
    fun toString(radix: Int = 10): String
    fun zeroPaddedBin(): String = toString(2).padStart(bitWidth, '0')
    fun zeroPaddedHex(): String = toString(16).padStart(byteCount * 2, '0')

    fun toUnsigned(): IntNumber<*>

    // Checks
    fun fitsInSigned(bitWidth: Int): Boolean
    fun fitsInUnsigned(bitWidth: Int): Boolean
    fun fitsInSignedOrUnsigned(bitWidth: Int): Boolean = fitsInSigned(bitWidth) || fitsInUnsigned(bitWidth)

}