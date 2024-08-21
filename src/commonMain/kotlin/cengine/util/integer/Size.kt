package cengine.util.integer

import kotlin.math.roundToInt

/**
 * This class defines the [Size] of each [Value] or [Variable], custom needed sizes for specific architectures can be added.
 * <CAN BE EXTENDED>
 */
sealed class Size(val name: String, val bitWidth: Int) {

    val hexChars = bitWidth / 4 + if (bitWidth % 4 == 0) 0 else 1
    val octChars = bitWidth / 3 + if (bitWidth % 3 == 0) 0 else 1

    override fun equals(other: Any?): Boolean {
        when (other) {
            is Size -> {
                return this.bitWidth == other.bitWidth
            }
        }
        return super.equals(other)
    }

    override fun toString(): String {
        return "$bitWidth Bits"
    }

    fun getByteCount(): Int {
        return (bitWidth.toFloat() / 8.0f).roundToInt()
    }

    override fun hashCode(): Int {
        return bitWidth.hashCode()
    }

    class Original(bitWidth: Int) : Size("original", bitWidth)
    object Bit1 : Size("1 Bit", 1)
    object Bit2 : Size("2 Bit", 2)
    object Bit3 : Size("3 Bit", 3)
    object Bit4 : Size("4 Bit", 4)
    object Bit5 : Size("5 Bit", 5)
    object Bit6 : Size("6 Bit", 6)
    object Bit7 : Size("7 Bit", 7)
    object Bit8 : Size("8 Bit", 8)
    object Bit9 : Size("9 Bit", 9)
    object Bit12 : Size("12 Bit", 12)
    object Bit16 : Size("16 Bit", 16)
    object Bit18 : Size("18 Bit", 18)
    object Bit20 : Size("20 Bit", 20)
    object Bit24 : Size("24 Bit", 24)
    object Bit26 : Size("26 Bit", 26)
    object Bit28 : Size("28 Bit", 28)
    object Bit32 : Size("32 Bit", 32)
    object Bit40 : Size("40 Bit", 40)
    object Bit44 : Size("44 Bit", 44)
    object Bit48 : Size("48 Bit", 48)
    object Bit52 : Size("52 Bit", 52)
    object Bit56 : Size("56 Bit", 56)
    object Bit60 : Size("60 Bit", 60)
    object Bit64 : Size("64 Bit", 64)
    object Bit128 : Size("128 Bit", 128)
}