package cengine.util.integer

import Settings
import cengine.util.integer.binary.BinaryTools
import emulator.kit.nativeError

/**
 * Provides the binary representation of [Value].
 */
class Bin(binString: String, size: Size) : Value(size) {
    override val input: String
    override val valid: Boolean

    companion object {
        val regex = Regex("[0-1]+")
    }

    constructor(size: Size) : this(Settings.PRESTRING_BINARY + "0", size)

    constructor(binString: String) : this(binString, Size.Original(binString.trim().removePrefix(Settings.PRESTRING_BINARY).length))

    init {
        val result = check(binString, size)
        input = result.corrected
        valid = result.valid
    }

    override fun check(string: String, size: Size): CheckResult {
        val formattedInput = string.trim().removePrefix(Settings.PRESTRING_BINARY).padStart(size.bitWidth, '0')
        val message: String
        if (regex.matches(formattedInput)) {
            // bin string without prestring
            return if (formattedInput.length <= size.bitWidth) {
                CheckResult(true, formattedInput)
            } else {
                val trimmedString = formattedInput.substring(formattedInput.length - size.bitWidth)
                message = "Bin.check(): $string is to long! Casted to TrimmedString(${trimmedString}) This value is layouted to hold up values with a bit width <= ${size.bitWidth}!"
                CheckResult(false, trimmedString, message)
            }
        } else {
            val zeroString = Settings.PRESTRING_BINARY + "0".repeat(size.bitWidth)
            message = "Bin.check(): $string does not match the binary Pattern (${Settings.PRESTRING_BINARY + "X".repeat(size.bitWidth)} where X is element of [0,1]), returning $zeroString instead!"
            nativeError(message)
            return CheckResult(false, formattedInput, message)
        }
    }

    override fun check(size: Size): CheckResult = check(toRawString(), size)

    fun getUResized(size: Size): Bin {
        val paddedBinString = if (size.bitWidth < size.bitWidth) {
            toRawString().substring(toRawString().length - size.bitWidth)
        } else {
            toRawString().padStart(size.bitWidth, '0')
        }
        return Bin(paddedBinString, size)
    }

    fun getResized(size: Size): Bin {
        val paddedBinString = if (size.bitWidth < this.size.bitWidth) {
            toRawString().substring(toRawString().length - size.bitWidth)
        } else {
            toRawString().padStart(size.bitWidth, if (toRawString().first() == '1') '1' else '0')
        }
        return Bin(paddedBinString, size)
    }

    fun splitToByteArray(): Array<Bin> {
        val paddedString = if (toRawString().length % 8 == 0) toRawString() else toRawString().padStart(toRawString().length + (8 - toRawString().length % 8), '0')
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
                val exceeding = toRawString().substring(0, this.size.bitWidth - other.bitWidth)
                exceeding.indexOf('1') == -1
            }

            this.size.bitWidth < other.bitWidth -> {
                toRawString().first() != '1'
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
    override fun getBiggest(): Value = Bin("1".repeat(size.bitWidth), size)

    override fun plus(operand: Value): Value {
        val result = BinaryTools.add(this.toRawString(), operand.toBin().toRawString())
        val biggerSize = if (this.size.bitWidth > operand.size.bitWidth) this.size else operand.size
        return Bin(result, biggerSize)
    }

    fun detailedPlus(operand: Value): AddResult {
        val result = BinaryTools.addWithCarry(this.toRawString(), operand.toBin().toRawString())
        val biggerSize = if (this.size.bitWidth > operand.size.bitWidth) this.size else operand.size
        return AddResult(Bin(result.result, biggerSize), result.carry == '1')
    }

    override fun minus(operand: Value): Value {
        val result = BinaryTools.sub(this.toRawString(), operand.toBin().toRawString())
        val biggerSize = if (this.size.bitWidth > operand.size.bitWidth) this.size else operand.size
        return Bin(result, biggerSize)
    }

    fun detailedMinus(operand: Value): SubResult {
        val result = BinaryTools.subWithBorrow(this.toRawString(), operand.toBin().toRawString())
        val biggerSize = if (this.size.bitWidth > operand.size.bitWidth) this.size else operand.size
        return SubResult(Bin(result.result, biggerSize), result.borrow == '1')
    }

    override fun times(operand: Value): Value {
        val result = BinaryTools.multiply(this.toRawString(), operand.toBin().toRawString())
        //val biggerSize = if (this.size.bitWidth > operand.size.bitWidth) this.size else operand.size
        return Bin(result)
    }

    /**
     * Flexible Multiplication Operation
     */
    fun flexTimesSigned(factor: Bin, resizeToLargestParamSize: Boolean = true, factorIsUnsigned: Boolean = false): Bin {
        val result = if (factorIsUnsigned) BinaryTools.multiplyMixed(toRawString(), factor.toRawString()) else BinaryTools.multiplySigned(this.toRawString(), factor.toRawString())
        val biggerSize = if (this.size.bitWidth > factor.size.bitWidth) size else factor.size

        return if (resizeToLargestParamSize) Bin(result).getResized(biggerSize) else Bin(result)
    }

    override fun div(operand: Value): Value {
        val divResult = BinaryTools.divide(toRawString(), operand.toBin().toRawString())
        val biggerSize = if (this.size.bitWidth > operand.size.bitWidth) size else operand.size
        return Bin(divResult.result).getUResized(biggerSize)
    }

    fun flexDivSigned(divisor: Bin, resizeToLargestParamSize: Boolean = true, dividendIsUnsigned: Boolean = false): Bin {
        val divResult = if (dividendIsUnsigned) BinaryTools.divideMixed(toRawString(), divisor.toRawString()) else BinaryTools.divideSigned(toRawString(), divisor.toRawString())
        val biggerSize = if (this.size.bitWidth > divisor.size.bitWidth) size else divisor.size
        return if (resizeToLargestParamSize) Bin(divResult.result).getResized(biggerSize) else Bin(divResult.result)
    }

    override fun rem(operand: Value): Value {
        val divResult = BinaryTools.divide(toRawString(), operand.toBin().toRawString())
        val biggerSize = if (this.size.bitWidth > operand.size.bitWidth) this.size else operand.size
        return Bin(BinaryTools.checkEmpty(divResult.remainder), biggerSize)
    }

    fun flexRemSigned(divisor: Bin, resizeToLargestParamSize: Boolean = true): Bin {
        val divResult = BinaryTools.divideSigned(toRawString(), divisor.toRawString())
        val biggerSize = if (this.size.bitWidth > divisor.size.bitWidth) this.size else divisor.size
        return if (resizeToLargestParamSize) Bin(BinaryTools.checkEmpty(divResult.remainder)).getResized(biggerSize) else Bin(divResult.remainder)
    }

    override fun unaryMinus(): Value = Bin(BinaryTools.negotiate(this.toRawString()), size)

    override fun inc(): Value = Bin(BinaryTools.add(toRawString(), "1"), size)

    override fun dec(): Value = Bin(BinaryTools.sub(toRawString(), "1"), size)

    override fun compareTo(other: Value): Int {
        return if (BinaryTools.isEqual(toRawString(), other.toBin().toRawString())) {
            0
        } else if (BinaryTools.isGreaterThan(toRawString(), other.toBin().toRawString())) {
            1
        } else {
            -1
        }
    }

    override fun equals(other: Any?): Boolean {
        if (other is Value) {
            return BinaryTools.isEqual(toRawString(), other.toBin().toRawString())
        }
        return false
    }

    override fun toRawString(): String = input.removePrefix(Settings.PRESTRING_BINARY)
    override fun toString(): String = input

    infix fun shl(bitCount: Int): Bin {
        if (bitCount > size.bitWidth) return Bin("0", size)
        val shiftedBinary = toRawString().substring(bitCount).padEnd(size.bitWidth, '0')
        return Bin(shiftedBinary, size)
    }

    infix fun ushl(bitCount: Int): Bin {
        if (bitCount > size.bitWidth) return Bin("0", size)
        val shiftedBinary = toRawString().substring(bitCount).padEnd(size.bitWidth, '0')
        return Bin(shiftedBinary, size)
    }

    infix fun shr(bitCount: Int): Bin {
        val shiftedBinary = toRawString().padStart(size.bitWidth * 2, toRawString().first()).substring(0, size.bitWidth * 2 - bitCount).padStart(size.bitWidth * 2, toRawString().first())
        return Bin(shiftedBinary.substring(shiftedBinary.length - size.bitWidth), size)
    }

    infix fun ushr(bitCount: Int): Bin {
        val shiftedBinary = toRawString().padStart(size.bitWidth * 2, '0').substring(0, size.bitWidth * 2 - bitCount).padStart(size.bitWidth * 2, '0')
        return Bin(shiftedBinary.substring(shiftedBinary.length - size.bitWidth), size)
    }

    fun rotateLeft(bitCount: Int): Bin {
        val normalizedShift = bitCount % toRawString().length
        val doubled = toRawString() + toRawString()
        val rotatedBinStr = doubled.substring(normalizedShift, normalizedShift + toRawString().length)
        return Bin(rotatedBinStr, size)
    }

    fun rotateRight(bitCount: Int): Bin {
        val normalizedShift = bitCount % toRawString().length
        val doubled = toRawString() + toRawString()
        val rotatedBinStr = doubled.substring(toRawString().length - normalizedShift, 2 * toRawString().length - normalizedShift)
        return Bin(rotatedBinStr, size)
    }

    infix fun xor(bin2: Bin): Bin {
        val biggestSize = if (size.bitWidth >= bin2.size.bitWidth) size else bin2.size
        return Bin(BinaryTools.xor(toRawString(), bin2.toRawString()), biggestSize)
    }

    infix fun or(bin2: Bin): Bin {
        val biggestSize = if (size.bitWidth >= bin2.size.bitWidth) size else bin2.size
        return Bin(BinaryTools.or(toRawString(), bin2.toRawString()), biggestSize)
    }

    infix fun and(bin2: Bin): Bin {
        val biggestSize = if (size.bitWidth >= bin2.size.bitWidth) size else bin2.size
        return Bin(BinaryTools.and(toRawString(), bin2.toRawString()), biggestSize)
    }

    operator fun inv(): Bin {
        return Bin(BinaryTools.inv(toRawString()), size)
    }

    fun getBit(index: Int): Bin? {
        val bit = toRawString().getOrNull(index) ?: return null
        return Bin(bit.toString(), Size.Bit1)
    }
}