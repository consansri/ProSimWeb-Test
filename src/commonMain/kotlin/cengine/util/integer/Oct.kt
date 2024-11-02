package cengine.util.integer

import Settings
import cengine.util.integer.binary.BinaryTools
import emulator.kit.nativeError

/**
 * Provides the octal representation of [Value]
 */
class Oct(octString: String, size: Size) : Value(size) {
    override val input: String
    override val valid: Boolean

    init {
        val result = check(octString, size)
        input = result.corrected
        valid = result.valid
    }

    fun getUResized(size: Size): Oct = Oct(toRawString(), size)

    constructor(octString: String) : this(octString, Size.Original(octString.trim().removePrefix(Settings.PRESTRING_OCT).length * 3))

    override fun check(string: String, size: Size): CheckResult {
        var formatted = string.trim().removePrefix(Settings.PRESTRING_OCT).padStart(size.octChars, '0').uppercase()
        val message: String
        if (formatted.all { it.isDigit() && it != '9' && it != '8' }) {
            return if (formatted.length <= size.octChars) {
                formatted = formatted.padStart(size.octChars, '0')
                CheckResult(true, Settings.PRESTRING_OCT + formatted)
            } else {
                val trimmedString = formatted.substring(formatted.length - size.octChars)
                message = "Oct.check(): $string is to long! Casted to TrimmedString(${trimmedString}) This value is layouted to hold up values with width <= ${size.octChars}!"
                CheckResult(false, Settings.PRESTRING_OCT + trimmedString, message)
            }
        } else {
            val zeroString = Settings.PRESTRING_OCT + "0".repeat(size.octChars)
            message = "Oct.check(): $string does not match the hex Pattern (${Settings.PRESTRING_OCT + "X".repeat(size.octChars)} where X is element of [0-7]), returning $zeroString instead!"
            nativeError(message)
            return CheckResult(false, zeroString, message)
        }
    }

    override fun check(size: Size): CheckResult = check(toRawString(), size)
    override fun checkSizeSigned(other: Size): Boolean = toDec().checkSizeSigned(other)
    override fun checkSizeUnsigned(other: Size): Boolean = toBin().checkSizeUnsigned(other)
    override fun toBin(): Bin = getBinary()
    override fun toHex(): Hex = getBinary().getHex()
    override fun toOct(): Oct = this
    override fun toDec(): Dec = getBinary().getDec()
    override fun toUDec(): UDec = getBinary().getUDec()
    override fun toASCII(): String = getASCII()
    override fun toLong(): Long = toULong().toLong()
    override fun toULong(): ULong = toRawString().toULong(8)

    override fun getBiggest(): Value = Bin("1".repeat(size.bitWidth), size)

    override fun plus(operand: Value): Oct {
        val biggerSize = if (this.size.bitWidth > operand.size.bitWidth) this.size else operand.size

        return if (biggerSize.bitWidth <= 64) {
            Oct((toULong() + operand.toULong()).toString(8), biggerSize)
        } else {
            (toBin() + operand.toBin()).toOct()
        }
    }

    override fun minus(operand: Value): Oct {
        val biggerSize = if (this.size.bitWidth > operand.size.bitWidth) this.size else operand.size

        return if (biggerSize.bitWidth <= 64) {
            Oct((toULong() - operand.toULong()).toString(8), biggerSize)
        } else {
            (toBin() - operand.toBin()).toOct()
        }
    }

    override fun times(operand: Value): Oct {
        val biggerSize = if (this.size.bitWidth > operand.size.bitWidth) this.size else operand.size

        return if (biggerSize.bitWidth <= 64) {
            Oct((toULong() * operand.toULong()).toString(8), biggerSize)
        } else {
            (toBin() * operand.toBin()).toOct()
        }
    }

    override fun div(operand: Value): Oct {
        val biggerSize = if (this.size.bitWidth > operand.size.bitWidth) this.size else operand.size

        return if (biggerSize.bitWidth <= 64) {
            Oct((toULong() / operand.toULong()).toString(8), biggerSize)
        } else {
            (toBin() / operand.toBin()).toOct()
        }
    }

    override fun rem(operand: Value): Oct {
        val biggerSize = if (this.size.bitWidth > operand.size.bitWidth) this.size else operand.size

        return if (biggerSize.bitWidth <= 64) {
            Oct((toULong() % operand.toULong()).toString(8), biggerSize)
        } else {
            (toBin() % operand.toBin()).toOct()
        }
    }

    override fun unaryMinus(): Value = Bin(BinaryTools.negotiate(this.toBin().toRawString()), size)

    override fun inc(): Value = Bin(BinaryTools.add(this.toBin().toRawString(), "1"), size)

    override fun dec(): Value = Bin(BinaryTools.sub(this.toBin().toRawString(), "1"), size)

    override fun compareTo(other: Value): Int {
        if (size.bitWidth <= 64 && other.size.bitWidth <= 64) return toULong().compareTo(other.toULong())

        return if (BinaryTools.isEqual(this.toBin().toRawString(), other.toBin().toRawString())) {
            0
        } else if (BinaryTools.isGreaterThan(this.toBin().toRawString(), other.toBin().toRawString())) {
            1
        } else {
            -1
        }
    }

    override fun toRawString(): String = input.removePrefix(Settings.PRESTRING_OCT)

    override fun toString(): String = input

    override fun equals(other: Any?): Boolean {
        if (other is Value) {
            return BinaryTools.isEqual(toBin().toRawString(), other.toBin().toRawString())
        }
        return false
    }
}