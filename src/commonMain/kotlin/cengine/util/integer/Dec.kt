package cengine.util.integer

import Settings
import cengine.util.integer.Size.Companion.nearestDecSize
import cengine.util.integer.decimal.DecimalTools
import emulator.kit.nativeError
import emulator.kit.nativeWarn

/**
 * Provides the decimal representation of [Value].
 */
class Dec(decString: String, size: Size) : Value(size) {
    override val input: String
    override val valid: Boolean

    private val negative: Boolean

    init {
        val result = check(decString, size)
        input = result.corrected
        valid = result.valid
        this.negative = DecimalTools.isNegative(result.corrected)
    }

    constructor(decString: String) : this(decString, nearestDecSize(decString.trim().removePrefix(Settings.PRESTRING_DECIMAL)))

    fun isNegative(): Boolean = negative

    fun getResized(size: Size): Dec = Dec(toRawString(), size)

    override fun check(string: String, size: Size): CheckResult {
        val formatted = string.trim().removePrefix(Settings.PRESTRING_DECIMAL)
        val message: String
        if (!formatted.replace("-", "").all { it.isDigit() }) {
            val zeroString = "0"
            message = "Dec.check(): $formatted does not match the dec Pattern (${Settings.PRESTRING_DECIMAL + "(-)" + "X".repeat(size.bitWidth)} where X is element of [0-9]), returning $zeroString instead!"
            nativeError(message)
            return CheckResult(false, Settings.PRESTRING_DECIMAL + zeroString, message)
        } else {
            return if (DecimalTools.isGreaterThan(formatted, Bounds(size).max)) {
                message = "Dec.check(): $formatted must be smaller equal ${Bounds(size).max} -> setting ${Bounds(size).max}"
                CheckResult(false, Settings.PRESTRING_DECIMAL + Bounds(size).max, message)
            } else if (DecimalTools.isGreaterThan(Bounds(size).min, formatted)) {
                message = "Dec.check(): $formatted must be bigger equal ${Bounds(size).min} -> setting ${Bounds(size).min}"
                nativeWarn(message)
                CheckResult(false, Settings.PRESTRING_DECIMAL + Bounds(size).min, message)
            } else {
                CheckResult(true, Settings.PRESTRING_DECIMAL + formatted)
            }
        }
    }

    override fun check(size: Size): CheckResult = check(toRawString(), size)

    override fun checkSizeSigned(other: Size): Boolean = getResized(other).valid
    override fun checkSizeUnsigned(other: Size): Boolean = toBin().checkSizeUnsigned(other)
    override fun toBin(): Bin = getBinary()
    override fun toHex(): Hex = getBinary().getHex()
    override fun toOct(): Oct = getBinary().getOct()
    override fun toDec(): Dec = this
    override fun toUDec(): UDec = getBinary().getUDec()
    override fun toASCII(): String = getASCII()
    override fun toULong(): ULong = toLong().toULong()
    override fun toLong(): Long = toRawString().toLong()
    fun toIntOrNull(): Int? = toRawString().toIntOrNull()
    fun toDoubleOrNull(): Double? = toRawString().toDoubleOrNull()
    override fun getBiggest(): Value = Dec(Bounds(size).max, size)
    override fun plus(operand: Value): Dec {
        val biggerSize = if (size.bitWidth > operand.size.bitWidth) size else operand.size
        val result = if (biggerSize.bitWidth <= 64) {
            (toLong() + operand.toLong()).toString()
        } else {
            DecimalTools.add(toRawString(), operand.toDec().toRawString())
        }

        return Dec(result, biggerSize)
    }

    override fun minus(operand: Value): Dec {
        val biggerSize = if (size.bitWidth > operand.size.bitWidth) size else operand.size
        val result = if (biggerSize.bitWidth <= 64) {
            (toLong() - operand.toLong()).toString()
        } else {
            DecimalTools.sub(toRawString(), operand.toDec().toRawString())
        }

        return Dec(result, biggerSize)
    }

    override fun times(operand: Value): Dec {
        val biggerSize = if (size.bitWidth > operand.size.bitWidth) size else operand.size
        val result = if (biggerSize.bitWidth <= 64) {
            (toLong() * operand.toLong()).toString()
        } else {
            DecimalTools.multiply(toRawString(), operand.toDec().toRawString())
        }

        return Dec(result, biggerSize)
    }

    override fun div(operand: Value): Dec {
        val biggerSize = if (size.bitWidth > operand.size.bitWidth) size else operand.size
        val result = if (biggerSize.bitWidth <= 64) {
            (toLong() / operand.toLong()).toString()
        } else {
            DecimalTools.divide(toRawString(), operand.toDec().toRawString()).result
        }

        return Dec(result, biggerSize)
    }

    override fun rem(operand: Value): Dec {
        val biggerSize = if (size.bitWidth > operand.size.bitWidth) size else operand.size
        val result = if (biggerSize.bitWidth <= 64) {
            (toLong() % operand.toLong()).toString()
        } else {
            DecimalTools.checkEmpty(DecimalTools.divide(toRawString(), operand.toDec().toRawString()).rest)
        }

        return Dec(result, biggerSize)
    }

    override fun unaryMinus(): Value = Dec(DecimalTools.negotiate(toRawString()), size)
    override fun inc(): Value = Dec(DecimalTools.add(toRawString(), "1"), size)
    override fun dec(): Value = Dec(DecimalTools.sub(toRawString(), "1"), size)
    override fun compareTo(other: Value): Int {
        if (size.bitWidth <= 64 && other.size.bitWidth <= 64) return toLong().compareTo(other.toLong())
        return if (DecimalTools.isEqual(toRawString(), other.toDec().toRawString())) {
            0
        } else if (DecimalTools.isGreaterThan(toRawString(), other.toDec().toRawString())) {
            1
        } else {
            -1
        }
    }

    override fun equals(other: Any?): Boolean {
        if (other is Value) {
            return DecimalTools.isEqual(toRawString(), other.toDec().toRawString())
        }
        return false
    }

    override fun toRawString(): String = input.removePrefix(Settings.PRESTRING_DECIMAL)
    override fun toString(): String = input
}