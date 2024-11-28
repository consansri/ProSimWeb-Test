package cengine.util.integer

import Settings
import cengine.util.integer.binary.BinaryTools
import emulator.kit.nativeError

/**
 * Provides the binary representation of [Value].
 */
class Bin(binString: String, size: Size) : Value(size) {
    override val valid: Boolean
    override val rawInput: String

    constructor(size: Size) : this(Settings.PRESTRING_BINARY + "0", size)

    constructor(binString: String) : this(binString, Size.Original(binString.trim().removePrefix(Settings.PRESTRING_BINARY).length))

    init {
        val result = check(binString, size)
        rawInput = result.correctedRawInput
        valid = result.valid
    }

    override fun check(string: String, size: Size): CheckResult {
        val formattedInput = string.trim().removePrefix(Settings.PRESTRING_BINARY).padStart(size.bitWidth, '0')
        val message: String
        if (formattedInput.all { it == '0' || it == '1' }) {
            // bin string without prestring
            return if (formattedInput.length <= size.bitWidth) {
                CheckResult(true, formattedInput)
            } else {
                val trimmedString = formattedInput.substring(formattedInput.length - size.bitWidth)
                message = "Bin.check(): $string is to long! Casted to TrimmedString(${trimmedString}) This value is layouted to hold up values with a bit width <= ${size.bitWidth}!"
                CheckResult(false, trimmedString, message)
            }
        } else {
            val zeroString = "0".repeat(size.bitWidth)
            message = "Bin.check(): $string does not match the binary Pattern (${Settings.PRESTRING_BINARY + "X".repeat(size.bitWidth)} where X is element of [0,1]), returning $zeroString instead!"
            nativeError(message)
            return CheckResult(false, zeroString, message)
        }
    }

    override fun check(size: Size): CheckResult = check(rawInput, size)

    fun getUResized(size: Size): Bin {
        val paddedBinString = if (size.bitWidth < size.bitWidth) {
            rawInput.substring(rawInput.length - size.bitWidth)
        } else {
            rawInput.padStart(size.bitWidth, '0')
        }
        return Bin(paddedBinString, size)
    }

    fun getResized(size: Size): Bin {
        val paddedBinString = if (size.bitWidth < this.size.bitWidth) {
            rawInput.substring(rawInput.length - size.bitWidth)
        } else {
            rawInput.padStart(size.bitWidth, if (rawInput.first() == '1') '1' else '0')
        }
        return Bin(paddedBinString, size)
    }

    fun splitToByteArray(): Array<Bin> {
        val paddedString = if (rawInput.length % 8 == 0) rawInput else rawInput.padStart(rawInput.length + (8 - rawInput.length % 8), '0')
        return paddedString.chunked(8).map { Bin(it, Size.Bit8) }.toTypedArray()
    }

    /**
     * Returns null if Matches
     */
    override fun checkSizeUnsigned(other: Size): Boolean {
        return when {
            this.size.bitWidth == other.bitWidth -> {
                true
            }

            this.size.bitWidth > other.bitWidth -> {
                val exceeding = rawInput.substring(0, this.size.bitWidth - other.bitWidth)
                exceeding.indexOf('1') == -1
            }

            this.size.bitWidth < other.bitWidth -> {
                rawInput.first() != '1'
            }

            else -> {
                true
            }
        }
    }

    override fun checkSizeSigned(other: Size): Boolean {
        return this.toDec().checkSizeSigned(other)
    }

    override fun toHex(): Hex = getHex()
    override fun toOct(): Oct = getOct()
    override fun toBin(): Bin = this
    override fun toDec(): Dec = getDec()
    override fun toUDec(): UDec = getUDec()
    override fun toASCII(): String = getASCII()
    override fun toByte(): Byte = toUByte().toByte()
    override fun toShort(): Short = toUShort().toShort()
    override fun toInt(): Int = toUInt().toInt()
    override fun toLong(): Long = toULong().toLong()
    override fun toUByte(): UByte = rawInput.takeLast(8).toUByte(2)
    override fun toUShort(): UShort = rawInput.takeLast(16).toUShort(2)
    override fun toUInt(): UInt = rawInput.takeLast(32).toUInt(2)
    override fun toULong(): ULong = rawInput.takeLast(64).toULong(2)
    override fun getBiggest(): Value = Bin("1".repeat(size.bitWidth), size)

    override fun plus(operand: Value): Bin {
        return if (size.bitWidth <= 64 && operand.size.bitWidth <= 64) {
            val biggerSize = if (this.size.bitWidth > operand.size.bitWidth) this.size else operand.size
            val result = toULong() + operand.toULong()
            Bin(result.toString(2), biggerSize)
        } else {
            val result = BinaryTools.add(this.rawInput, operand.toBin().rawInput)
            val biggerSize = if (this.size.bitWidth > operand.size.bitWidth) this.size else operand.size
            Bin(result, biggerSize)
        }
    }

    fun detailedPlus(operand: Value): AddResult {

        val result = BinaryTools.addWithCarry(this.rawInput, operand.toBin().rawInput)
        val biggerSize = if (this.size.bitWidth > operand.size.bitWidth) this.size else operand.size
        return AddResult(Bin(result.result, biggerSize), result.carry == '1')
    }

    override fun minus(operand: Value): Bin {
        return if (size.bitWidth <= 64 && operand.size.bitWidth <= 64) {
            val biggerSize = if (this.size.bitWidth > operand.size.bitWidth) this.size else operand.size
            val result = toULong() - operand.toULong()
            Bin(result.toString(2), biggerSize)
        } else {
            val result = BinaryTools.sub(this.rawInput, operand.toBin().rawInput)
            val biggerSize = if (this.size.bitWidth > operand.size.bitWidth) this.size else operand.size
            Bin(result, biggerSize)
        }
    }

    fun detailedMinus(operand: Value): SubResult {
        val result = BinaryTools.subWithBorrow(this.rawInput, operand.toBin().rawInput)
        val biggerSize = if (this.size.bitWidth > operand.size.bitWidth) this.size else operand.size
        return SubResult(Bin(result.result, biggerSize), result.borrow == '1')
    }

    override fun times(operand: Value): Bin {
        return if (size.bitWidth <= 64 && operand.size.bitWidth <= 64) {
            val biggerSize = if (this.size.bitWidth > operand.size.bitWidth) this.size else operand.size
            val result = toULong() * operand.toULong()
            Bin(result.toString(2), biggerSize)
        } else {
            val result = BinaryTools.multiply(this.rawInput, operand.toBin().rawInput)
            val biggerSize = if (this.size.bitWidth > operand.size.bitWidth) this.size else operand.size
            Bin(result, biggerSize)
        }
    }

    /**
     * Flexible Multiplication Operation
     */
    fun flexTimesSigned(factor: Bin, resizeToLargestParamSize: Boolean = true, factorIsUnsigned: Boolean = false): Bin {
        val result = if (factorIsUnsigned) BinaryTools.multiplyMixed(rawInput, factor.rawInput) else BinaryTools.multiplySigned(this.rawInput, factor.rawInput)
        val biggerSize = if (this.size.bitWidth > factor.size.bitWidth) size else factor.size

        return if (resizeToLargestParamSize) Bin(result).getResized(biggerSize) else Bin(result)
    }

    override fun div(operand: Value): Bin {
        return if (size.bitWidth <= 64 && operand.size.bitWidth <= 64) {
            val biggerSize = if (this.size.bitWidth > operand.size.bitWidth) this.size else operand.size
            val result = toULong() / operand.toULong()
            Bin(result.toString(2), biggerSize)
        } else {
            val divResult = BinaryTools.divide(rawInput, operand.toBin().rawInput)
            val biggerSize = if (this.size.bitWidth > operand.size.bitWidth) size else operand.size
            Bin(divResult.result).getUResized(biggerSize)
        }
    }

    fun flexDivSigned(divisor: Bin, resizeToLargestParamSize: Boolean = true, dividendIsUnsigned: Boolean = false): Bin {
        val divResult = if (dividendIsUnsigned) BinaryTools.divideMixed(rawInput, divisor.rawInput) else BinaryTools.divideSigned(rawInput, divisor.rawInput)
        val biggerSize = if (this.size.bitWidth > divisor.size.bitWidth) size else divisor.size
        return if (resizeToLargestParamSize) Bin(divResult.result).getResized(biggerSize) else Bin(divResult.result)
    }

    override fun rem(operand: Value): Bin {
        return if (size.bitWidth <= 64 && operand.size.bitWidth <= 64) {
            val biggerSize = if (this.size.bitWidth > operand.size.bitWidth) this.size else operand.size
            val result = toULong() % operand.toULong()
            Bin(result.toString(2), biggerSize)
        } else {
            val divResult = BinaryTools.divide(rawInput, operand.toBin().rawInput)
            val biggerSize = if (this.size.bitWidth > operand.size.bitWidth) this.size else operand.size
            Bin(BinaryTools.checkEmpty(divResult.remainder), biggerSize)
        }
    }

    fun flexRemSigned(divisor: Bin, resizeToLargestParamSize: Boolean = true): Bin {
        val divResult = BinaryTools.divideSigned(rawInput, divisor.rawInput)
        val biggerSize = if (this.size.bitWidth > divisor.size.bitWidth) this.size else divisor.size
        return if (resizeToLargestParamSize) Bin(BinaryTools.checkEmpty(divResult.remainder)).getResized(biggerSize) else Bin(divResult.remainder)
    }

    override fun unaryMinus(): Value = Bin(BinaryTools.negotiate(this.rawInput), size)

    override fun inc(): Value = Bin(BinaryTools.add(rawInput, "1"), size)

    override fun dec(): Value = Bin(BinaryTools.sub(rawInput, "1"), size)

    override fun compareTo(other: Value): Int {
        return if (size.bitWidth <= 64 && other.size.bitWidth <= 64) {
            toULong().compareTo(other.toULong())
        } else {
            if (BinaryTools.isEqual(rawInput, other.toBin().rawInput)) {
                0
            } else if (BinaryTools.isGreaterThan(rawInput, other.toBin().rawInput)) {
                1
            } else {
                -1
            }
        }
    }

    override fun equals(other: Any?): Boolean {
        if (other is Value) {
            return BinaryTools.isEqual(rawInput, other.toBin().rawInput)
        }
        return false
    }

    override fun and(other: Value): Bin {
        val biggestSize = if (size.bitWidth >= other.size.bitWidth) size else other.size
        return Bin(BinaryTools.and(rawInput, other.toBin().rawInput), biggestSize)
    }

    override fun or(other: Value): Bin {
        val biggestSize = if (size.bitWidth >= other.size.bitWidth) size else other.size
        return Bin(BinaryTools.or(rawInput, other.toBin().rawInput), biggestSize)
    }

    override fun xor(other: Value): Bin {
        val biggestSize = if (size.bitWidth >= other.size.bitWidth) size else other.size
        return Bin(BinaryTools.xor(rawInput, other.toBin().rawInput), biggestSize)
    }

    infix fun shl(bitCount: Int): Bin {
        if (bitCount > size.bitWidth) return Bin("0", size)
        val shiftedBinary = rawInput.substring(bitCount).padEnd(size.bitWidth, '0')
        return Bin(shiftedBinary, size)
    }

    infix fun ushl(bitCount: Int): Bin {
        if (bitCount > size.bitWidth) return Bin("0", size)
        val shiftedBinary = rawInput.substring(bitCount).padEnd(size.bitWidth, '0')
        return Bin(shiftedBinary, size)
    }

    infix fun shr(bitCount: Int): Bin {
        val shiftedBinary = rawInput.padStart(size.bitWidth * 2, rawInput.first()).substring(0, size.bitWidth * 2 - bitCount).padStart(size.bitWidth * 2, rawInput.first())
        return Bin(shiftedBinary.substring(shiftedBinary.length - size.bitWidth), size)
    }

    infix fun ushr(bitCount: Int): Bin {
        val shiftedBinary = rawInput.padStart(size.bitWidth * 2, '0').substring(0, size.bitWidth * 2 - bitCount).padStart(size.bitWidth * 2, '0')
        return Bin(shiftedBinary.substring(shiftedBinary.length - size.bitWidth), size)
    }

    fun rotateLeft(bitCount: Int): Bin {
        val normalizedShift = bitCount % rawInput.length
        val doubled = rawInput + rawInput
        val rotatedBinStr = doubled.substring(normalizedShift, normalizedShift + rawInput.length)
        return Bin(rotatedBinStr, size)
    }

    fun rotateRight(bitCount: Int): Bin {
        val normalizedShift = bitCount % rawInput.length
        val doubled = rawInput + rawInput
        val rotatedBinStr = doubled.substring(rawInput.length - normalizedShift, 2 * rawInput.length - normalizedShift)
        return Bin(rotatedBinStr, size)
    }

    operator fun inv(): Bin {
        return Bin(BinaryTools.inv(rawInput), size)
    }

    fun getBit(index: Int): Bin? {
        val bit = rawInput.getOrNull(index) ?: return null
        return Bin(bit.toString(), Size.Bit1)
    }

    override fun toString(): String = "${Settings.PRESTRING_BINARY}$rawInput"
}