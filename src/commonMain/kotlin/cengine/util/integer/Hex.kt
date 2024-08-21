package cengine.util.integer

import Settings
import cengine.util.integer.binary.BinaryTools
import emulator.kit.nativeError

/**
 * Provides the hexadecimal representation of [Value].
 */
class Hex(hexString: String, size: Size) : Value(size) {

    override val input: String
    override val valid: Boolean

    companion object {
        val regex = Regex("[0-9A-Fa-f]+")
    }

    init {
        val result = check(hexString, size)
        this.valid = result.valid
        this.input = result.corrected
    }

    constructor(hexString: String) : this(hexString, Size.Original(hexString.trim().removePrefix(Settings.PRESTRING_HEX).length * 4))

    fun getUResized(size: Size): Hex = Hex(toRawString(), size)

    fun splitToByteArray(): Array<Hex> {
        val paddedString = if (this.toRawString().length % 2 == 0) this.toRawString() else this.toRawString().padStart(this.toRawString().length + 1, '0')
        return paddedString.chunked(2).map { Hex(it, Size.Bit8) }.toTypedArray()
    }

    fun splitToArray(size: Size): Array<Hex> {
        val sizeHexChars = size.hexChars
        val paddingSize = this.toRawString().length % sizeHexChars
        val paddedString = "0".repeat(paddingSize) + this.toRawString()
        return paddedString.chunked(sizeHexChars).map { Hex(it, size) }.toTypedArray()
    }

    override fun check(string: String, size: Size): CheckResult {
        var formatted = string.trim().removePrefix(Settings.PRESTRING_HEX).padStart(size.hexChars, '0').uppercase()
        val message: String
        if (regex.matches(formatted)) {
            return if (formatted.length <= size.hexChars) {
                formatted = formatted.padStart(size.hexChars, '0')
                CheckResult(true, Settings.PRESTRING_HEX + formatted)
            } else {
                val trimmedString = formatted.substring(formatted.length - size.hexChars)
                message = "Hex.check(): $string is to long! Casted to TrimmedString(${trimmedString}) This value is layouted to hold up values with width <= ${size.hexChars}!"
                CheckResult(false, Settings.PRESTRING_HEX + trimmedString, message)
            }
        } else {
            val zeroString = Settings.PRESTRING_HEX + "0".repeat(size.hexChars)
            message = "Hex.check(): $string does not match the hex Pattern (${Settings.PRESTRING_HEX + "X".repeat(size.hexChars)} where X is element of [0-9,A-F]), returning $zeroString instead!"
            nativeError(message)
            return CheckResult(false, zeroString, message)
        }
    }

    override fun check(size: Size): CheckResult = check(toRawString(), size)
    override fun checkSizeSigned(other: Size): Boolean = toDec().checkSizeSigned(other)
    override fun checkSizeUnsigned(other: Size): Boolean = toBin().checkSizeUnsigned(other)
    override fun toBin(): Bin = getBinary()
    override fun toOct(): Oct =  getBinary().getOct()
    override fun toDec(): Dec = getBinary().getDec()
    override fun toUDec(): UDec = getBinary().getUDec()
    override fun toASCII(): String = getASCII()
    override fun getBiggest(): Value = Bin("1".repeat(size.bitWidth), size)

    override fun plus(operand: Value): Value {
        val result = BinaryTools.add(this.toBin().toRawString(), operand.toBin().toRawString())
        val biggerSize = if (this.size.bitWidth > operand.size.bitWidth) this.size else operand.size
        return Bin(result, biggerSize)
    }

    override fun minus(operand: Value): Value {
        val result = BinaryTools.sub(this.toBin().toRawString(), operand.toBin().toRawString())
        val biggerSize = if (this.size.bitWidth > operand.size.bitWidth) this.size else operand.size
        return Bin(result, biggerSize)
    }

    override fun times(operand: Value): Value {
        val result = BinaryTools.multiply(this.toBin().toRawString(), operand.toBin().toRawString())
        val biggerSize = if (this.size.bitWidth > operand.size.bitWidth) this.size else operand.size
        return Bin(result, biggerSize)
    }

    override fun div(operand: Value): Value {
        val divResult = BinaryTools.divide(this.toBin().toRawString(), operand.toBin().toRawString())
        val biggerSize = if (this.size.bitWidth > operand.size.bitWidth) this.size else operand.size
        return Bin(divResult.result, biggerSize)
    }

    override fun rem(operand: Value): Value {
        val divResult = BinaryTools.divide(this.toBin().toRawString(), operand.toBin().toRawString())
        val biggerSize = if (this.size.bitWidth > operand.size.bitWidth) this.size else operand.size
        return Bin(BinaryTools.checkEmpty(divResult.remainder), biggerSize)
    }

    override fun unaryMinus(): Value = Bin(BinaryTools.negotiate(this.toBin().toRawString()), size)
    override fun inc(): Value = Bin(BinaryTools.add(this.toBin().toRawString(), "1"), size)
    override fun dec(): Value = Bin(BinaryTools.sub(this.toBin().toRawString(), "1"), size)
    override fun compareTo(other: Value): Int = if (BinaryTools.isEqual(this.toBin().toRawString(), other.toBin().toRawString())) {
        0
    } else if (BinaryTools.isGreaterThan(this.toBin().toRawString(), other.toBin().toRawString())) {
        1
    } else {
        -1
    }

    override fun toRawString(): String = input.removePrefix(Settings.PRESTRING_HEX).lowercase()
    override fun toString(): String = input.lowercase()
    override fun toHex(): Hex = this

    override fun equals(other: Any?): Boolean {
        if (other is Value) {
            return BinaryTools.isEqual(toBin().toRawString(), other.toBin().toRawString())
        }
        return false
    }
}