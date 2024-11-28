package cengine.util.integer

import Settings
import cengine.util.integer.Size.Companion.nearestDecSize
import cengine.util.integer.decimal.DecimalTools
import emulator.kit.nativeError

/**
 * Provides the decimal representation of [Value].
 */
class Dec(decString: String, size: Size) : Value(size) {
    override val valid: Boolean
    override val rawInput: String
    private val negative: Boolean

    init {
        val result = check(decString, size)
        valid = result.valid
        this.negative = DecimalTools.isNegative(result.correctedRawInput)
        rawInput = result.correctedRawInput
    }

    constructor(decString: String) : this(decString, nearestDecSize(decString.trim().removePrefix(Settings.PRESTRING_DECIMAL)))

    fun isNegative(): Boolean = negative

    fun getResized(size: Size): Dec = Dec(rawInput, size)

    override fun check(string: String, size: Size): CheckResult {
        val formatted = string.trim().removePrefix(Settings.PRESTRING_DECIMAL)
        val message: String
        if (!formatted.replace("-", "").all { it.isDigit() }) {
            val zeroString = "0"
            message = "Dec.check(): $formatted does not match the dec Pattern (${Settings.PRESTRING_DECIMAL + "(-)" + "X".repeat(size.bitWidth)} where X is element of [0-9]), returning $zeroString instead!"
            nativeError(message)
            return CheckResult(false, zeroString, message)
        } else {
            return if (DecimalTools.isGreaterThan(formatted, Bounds(size).max)) {
                message = "Dec.check(): $formatted must be smaller equal ${Bounds(size).max} -> setting ${Bounds(size).max}"
                CheckResult(false, Bounds(size).max, message)
            } else if (DecimalTools.isGreaterThan(Bounds(size).min, formatted)) {
                message = "Dec.check(): $formatted must be bigger equal ${Bounds(size).min} -> setting ${Bounds(size).min}"
                CheckResult(false, Bounds(size).min, message)
            } else {
                CheckResult(true, formatted)
            }
        }
    }

    override fun check(size: Size): CheckResult = check(rawInput, size)

    override fun checkSizeSigned(other: Size): Boolean = getResized(other).valid
    override fun checkSizeUnsigned(other: Size): Boolean = toBin().checkSizeUnsigned(other)
    override fun toBin(): Bin = getBinary()
    override fun toHex(): Hex = getBinary().getHex()
    override fun toOct(): Oct = getBinary().getOct()
    override fun toDec(): Dec = this
    override fun toUDec(): UDec = getBinary().getUDec()
    override fun toASCII(): String = getASCII()
    override fun toByte(): Byte = rawInput.toByte()
    override fun toShort(): Short = rawInput.toShort()
    override fun toInt(): Int = rawInput.toInt()
    override fun toLong(): Long = rawInput.toLong()
    override fun toUByte(): UByte = toByte().toUByte()
    override fun toUShort(): UShort = toShort().toUShort()
    override fun toUInt(): UInt = toInt().toUInt()
    override fun toULong(): ULong = toLong().toULong()

    fun toIntOrNull(): Int? = rawInput.toIntOrNull()
    fun toDoubleOrNull(): Double? = rawInput.toDoubleOrNull()
    override fun getBiggest(): Value = Dec(Bounds(size).max, size)
    override fun plus(operand: Value): Dec {
        val biggerSize = if (size.bitWidth > operand.size.bitWidth) size else operand.size
        val result = if (biggerSize.bitWidth <= 64) {
            (toLong() + operand.toLong()).toString()
        } else {
            DecimalTools.add(rawInput, operand.toDec().rawInput)
        }

        return Dec(result, biggerSize)
    }

    override fun minus(operand: Value): Dec {
        val biggerSize = if (size.bitWidth > operand.size.bitWidth) size else operand.size
        val result = if (biggerSize.bitWidth <= 64) {
            (toLong() - operand.toLong()).toString()
        } else {
            DecimalTools.sub(rawInput, operand.toDec().rawInput)
        }

        return Dec(result, biggerSize)
    }

    override fun times(operand: Value): Dec {
        val biggerSize = if (size.bitWidth > operand.size.bitWidth) size else operand.size
        val result = if (biggerSize.bitWidth <= 64) {
            (toLong() * operand.toLong()).toString()
        } else {
            DecimalTools.multiply(rawInput, operand.toDec().rawInput)
        }

        return Dec(result, biggerSize)
    }

    override fun div(operand: Value): Dec {
        val biggerSize = if (size.bitWidth > operand.size.bitWidth) size else operand.size
        val result = if (biggerSize.bitWidth <= 64) {
            (toLong() / operand.toLong()).toString()
        } else {
            DecimalTools.divide(rawInput, operand.toDec().rawInput).result
        }

        return Dec(result, biggerSize)
    }

    override fun rem(operand: Value): Dec {
        val biggerSize = if (size.bitWidth > operand.size.bitWidth) size else operand.size
        val result = if (biggerSize.bitWidth <= 64) {
            (toLong() % operand.toLong()).toString()
        } else {
            DecimalTools.checkEmpty(DecimalTools.divide(rawInput, operand.toDec().rawInput).rest)
        }

        return Dec(result, biggerSize)
    }

    override fun unaryMinus(): Value = Dec(DecimalTools.negotiate(rawInput), size)
    override fun inc(): Value = Dec(DecimalTools.add(rawInput, "1"), size)
    override fun dec(): Value = Dec(DecimalTools.sub(rawInput, "1"), size)
    override fun compareTo(other: Value): Int {
        if (size.bitWidth <= 64 && other.size.bitWidth <= 64) return toLong().compareTo(other.toLong())
        return if (DecimalTools.isEqual(rawInput, other.toDec().rawInput)) {
            0
        } else if (DecimalTools.isGreaterThan(rawInput, other.toDec().rawInput)) {
            1
        } else {
            -1
        }
    }

    override fun equals(other: Any?): Boolean {
        if (other is Value) {
            return DecimalTools.isEqual(rawInput, other.toDec().rawInput)
        }
        return false
    }

    override fun and(other: Value): Dec = toBin().and(other.toBin()).toDec()

    override fun or(other: Value): Dec = toBin().or(other.toBin()).toDec()

    override fun xor(other: Value): Dec = toBin().xor(other.toBin()).toDec()

    override fun toString(): String = "${Settings.PRESTRING_DECIMAL}$rawInput"
}