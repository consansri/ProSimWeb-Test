package cengine.util.integer

import androidx.compose.runtime.Immutable
import cengine.util.string.removeLeadingZeros

/**
 * A [Value] can have the following types: [Bin], [Oct], [UDec], [Hex] or [Dec].
 * These Types contain the representation and behaviour of Binary, Hexadecimal, Decimal and Unsigned Decimal Values.
 * To interact with this Values the following functions can be used:
 *
 * [toBin], [toHex], [toDec], [toUDec], [toASCII], [toDouble]: Converts the value to another type. This conversions base on the [Conversion] object.
 *
 * [getBiggest]: Returns the maximum value which is limited by the current [size].
 *
 * [plus], [minus], [times], [div], [rem], [unaryMinus], [inc], [dec]: Can be used to calculate with this [Value]s.
 *
 * These calculations are always based on the type of the first parameter, which differs between the use of [BinaryTools] or [DecTools].
 *
 * [compareTo], [equals]: Allow to compare values with the standard comparing syntax in kotlin.
 *
 * [toString]: Provides the string representation of the value with its prefixes.
 * [toRawString]: Provides the string representation of the value without its prefixes.
 *
 */
@Immutable
sealed class Value(val size: Size) {

    companion object{
        /**
         * Quality of use extensions for [Value].
         */


        /**
         * [Dec] from signed dec string.
         */
        fun String.asDec(size: Size): Dec = Dec(this, size)
        fun String.asDec(): Dec = Dec(this, Size.nearestDecSize(this))
        fun String.asHex(size: Size): Hex = Hex(this, size)
        fun String.asHex(): Hex = Hex(this)
        fun String.asOct(size: Size): Oct = Oct(this, size)
        fun String.asOct(): Oct = Oct(this)
        fun String.asBin(size: Size): Bin = Bin(this, size)
        fun String.asBin(): Bin = Bin(this)

        fun Byte.toValue(size: Size = Size.Bit8): Dec = Dec(this.toString(), size)
        fun Short.toValue(size: Size = Size.Bit16): Dec = Dec(this.toString(), size)
        fun Int.toValue(size: Size = Size.Bit32): Dec = Dec(this.toString(), size)
        fun Long.toValue(size: Size = Size.Bit64): Dec = Dec(this.toString(), size)
        fun UByte.toValue(size: Size = Size.Bit8): Hex = Hex(this.toString(16), size)
        fun UShort.toValue(size: Size = Size.Bit16): Hex = Hex(this.toString(16), size)
        fun UInt.toValue(size: Size = Size.Bit32): Hex = Hex(this.toString(16), size)
        fun ULong.toValue(size: Size = Size.Bit64): Hex = Hex(this.toString(16), size)
    }

    abstract val valid: Boolean
    abstract val rawInput: String

    /**
     * This function implements the [String] format checks, of each specific type.
     * This function can write errors and warnings such as applied changes to the Console if any input isn't valid.
     */
    protected abstract fun check(string: String, size: Size): CheckResult

    /**
     * [check] is used for recognizing format and [Size] issues on initialization.
     */
    protected abstract fun check(size: Size): CheckResult

    /**
     * []
     *
     * @return true if the SIGNED interpretation of [Value] fits in [other].
     */
    abstract fun checkSizeSigned(other: Size): Boolean

    /**
     * @return true if the UNSIGNED interpretation of [Value] fits in [other].
     */
    abstract fun checkSizeUnsigned(other: Size): Boolean

    /**
     * @return true if the SIGNED OR UNSIGNED interpretation of [Value] fits in [other].
     */
    fun checkSizeSignedOrUnsigned(other: Size): Boolean = checkSizeSigned(other) || checkSizeUnsigned(other)

    /**
     * @return true if the SIGNED AND UNSIGNED interpretation of [Value] fits in [other].
     */
    fun checkSizeSignedAndUnsigned(other: Size): Boolean = checkSizeSigned(other) && checkSizeUnsigned(other)
    abstract fun toBin(): Bin
    abstract fun toHex(): Hex
    abstract fun toOct(): Oct
    abstract fun toDec(): Dec
    abstract fun toUDec(): UDec
    abstract fun toASCII(): String
    abstract fun toByte(): Byte
    abstract fun toShort(): Short
    abstract fun toInt(): Int
    abstract fun toLong(): Long
    abstract fun toUByte(): UByte
    abstract fun toUShort(): UShort
    abstract fun toUInt(): UInt
    abstract fun toULong(): ULong
    abstract fun getBiggest(): Value
    abstract operator fun plus(operand: Value): Value
    abstract operator fun minus(operand: Value): Value
    abstract operator fun times(operand: Value): Value
    abstract operator fun div(operand: Value): Value
    abstract operator fun rem(operand: Value): Value
    abstract operator fun unaryMinus(): Value
    abstract operator fun inc(): Value
    abstract operator fun dec(): Value
    abstract operator fun compareTo(other: Value): Int
    abstract override fun equals(other: Any?): Boolean
    fun toRawZeroTrimmedString(): String = rawInput.removeLeadingZeros()
    abstract override fun toString(): String

    override fun hashCode(): Int {
        var result = rawInput.hashCode()
        result = 31 * result + size.hashCode()
        return result
    }

    /**
     * This will only be used by the visual components to get all representation types fast and allow a switch between them.
     */
    enum class Types(val visibleName: String) {
        Bin("BIN"),
        Hex("HEX"),
        Dec("DEC"),
        UDec("UDEC");

        fun next(): Types {
            val length = Types.entries.size
            val currIndex = Types.entries.indexOf(this)
            val nextIndex = (currIndex + 1) % length
            return Types.entries[nextIndex]
        }
    }

    data class CheckResult(val valid: Boolean, val correctedRawInput: String, val message: String = "")
    data class AddResult(val result: Bin, val carry: Boolean)
    data class SubResult(val result: Bin, val borrow: Boolean)

}