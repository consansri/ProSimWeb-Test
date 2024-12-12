package cengine.util.integer


/**
 * Sign extends an Integer of [bitWidth] to a 64-Bit Integer.
 */
@Deprecated("Use IntNumber instead, cause it's way faster then Value!")
fun Long.signExtend(bitWidth: Int): Long {
    require(bitWidth in 1..32) { "bitWidth must be between 1 and 32" }

    // Mask the input value to the specified bit width
    val mask = (1L shl bitWidth) - 1L // Creates a mask with `bitWidth` bits set
    val maskedValue = this and mask

    // Check the sign bit (highest bit in the specified bit width)
    val signBit = 1L shl (bitWidth - 1)
    val result = if (maskedValue and signBit != 0L) {
        // If the sign bit is set, extend with 1s
        (maskedValue or (-1L shl bitWidth))
    } else {
        // If the sign bit is not set, return as-is
        maskedValue
    }

    return result
}

/**
 * Sign extends an Integer of [bitWidth] to a 32-Bit Integer.
 */
@Deprecated("Use IntNumber instead, cause it's way faster then Value!")
fun Int.signExtend(bitWidth: Int): Int {
    require(bitWidth in 1..32) { "bitWidth must be between 1 and 32" }

    // Mask the input value to the specified bit width
    val mask = (1 shl bitWidth) - 1 // Creates a mask with `bitWidth` bits set
    val maskedValue = this and mask

    // Check the sign bit (highest bit in the specified bit width)
    val signBit = 1 shl (bitWidth - 1)
    return if (maskedValue and signBit != 0) {
        // If the sign bit is set, extend with 1s
        (maskedValue or (-1 shl bitWidth))
    } else {
        // If the sign bit is not set, return as-is
        maskedValue
    }
}

/**
 * Rotate left
 */
@Deprecated("Use IntNumber instead, cause it's way faster then Value!")
infix fun UShort.rol(bits: Int): UShort {
    val shift = bits % 16
    return (this.toUInt() shl shift).toUShort() or (this.toUInt() shr (16 - shift)).toUShort()
}

/**
 * Rotate right
 */
@Deprecated("Use IntNumber instead, cause it's way faster then Value!")
infix fun UShort.ror(bits: Int): UShort {
    val shift = bits % 32 // Ensure the shift is within 0-31
    return (this.toUInt() shr shift).toUShort() or (this.toUInt() shl (32 - shift)).toUShort()
}



@Deprecated("Use IntNumber instead, cause it's way faster then Value!")
fun Long.multiplyWithHighLow(other: Long): Pair<Long, Long> {
    // Split the longs into high and low 32-bit parts
    val aLow = this and 0xFFFFFFFFL
    val aHigh = (this shr 32)
    val bLow = other and 0xFFFFFFFFL
    val bHigh = (other shr 32)

    // Multiply parts
    val lowLow = aLow * bLow
    val lowHigh = aLow * bHigh
    val highLow = aHigh * bLow
    val highHigh = aHigh * bHigh

    // Combine results
    val mid = lowHigh + (lowLow shr 32) + highLow
    val high = highHigh + (mid shr 32)
    val low = (mid shl 32) or (lowLow and 0xFFFFFFFFL)

    return high to low
}

@Deprecated("Use IntNumber instead, cause it's way faster then Value!")
fun ULong.multiplyWithHighLow(other: ULong): Pair<ULong, ULong> {
    // Split the longs into high and low 32-bit parts
    val aLow = this and 0xFFFFFFFFUL
    val aHigh = (this shr 32)
    val bLow = other and 0xFFFFFFFFUL
    val bHigh = (other shr 32)

    // Multiply parts
    val lowLow = aLow * bLow
    val lowHigh = aLow * bHigh
    val highLow = aHigh * bLow
    val highHigh = aHigh * bHigh

    // Combine results
    val mid = lowHigh + (lowLow shr 32) + highLow
    val high = highHigh + (mid shr 32)
    val low = (mid shl 32) or (lowLow and 0xFFFFFFFFUL)

    return high to low
}

@Deprecated("Use IntNumber instead, cause it's way faster then Value!")
fun Long.multiplyWithHighLow(other: ULong): Pair<Long, Long> {
    // Split the longs into high and low 32-bit parts
    val aLow = this and 0xFFFFFFFFL
    val aHigh = (this shr 32)
    val bLow = other and 0xFFFFFFFFUL
    val bHigh = (other shr 32)

    // Multiply parts
    val lowLow = aLow * bLow.toLong()
    val lowHigh = aLow * bHigh.toLong()
    val highLow = aHigh * bLow.toLong()
    val highHigh = aHigh * bHigh.toLong()

    // Combine results
    val mid = lowHigh + (lowLow shr 32) + highLow
    val high = highHigh + (mid shr 32)
    val low = (mid shl 32) or (lowLow and 0xFFFFFFFFL)

    return high to low
}

