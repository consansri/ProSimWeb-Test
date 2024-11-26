package cengine.util.integer


/**
 * Rotate left
 */
fun UInt.rol(bits: Int): UInt{
    val shift = bits % 32 // Ensure the shift is within 0-31
    return (this shl shift) or (this shr (32 - shift))
}

/**
 * Rotate right
 */
fun UInt.ror(bits: Int): UInt{
    val shift = bits % 32 // Ensure the shift is within 0-31
    return (this shr shift) or (this shl (32 - shift))
}

fun IntRange.overlaps(other: IntRange): Boolean {
    return this.first <= other.last && other.first <= this.last
}

fun Long.multiplyWithHighLow(other: Long): Pair<Long, Long>{
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

fun ULong.multiplyWithHighLow(other: ULong): Pair<ULong, ULong>{
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

fun Long.multiplyWithHighLow(other: ULong): Pair<Long, Long>{
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

