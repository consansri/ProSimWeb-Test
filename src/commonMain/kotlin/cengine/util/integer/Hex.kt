package cengine.util.integer

import Settings
import cengine.util.integer.binary.BinaryTools
import emulator.kit.nativeError

/**
 * Provides the hexadecimal representation of [Value].
 */
class Hex(hexString: String, size: Size) : Value(size) {

    override val valid: Boolean
    override val rawInput: String

    init {
        val result = check(hexString, size)
        valid = result.valid
        rawInput = result.correctedRawInput
    }

    constructor(hexString: String) : this(hexString, Size.Original(hexString.trim().removePrefix(Settings.PRESTRING_HEX).length * 4))

    fun getUResized(size: Size): Hex = Hex(rawInput, size)

    fun splitToByteArray(): Array<Hex> {
        val paddedString = if (this.rawInput.length % 2 == 0) this.rawInput else this.rawInput.padStart(this.rawInput.length + 1, '0')
        return paddedString.chunked(2).map { Hex(it, Size.Bit8) }.toTypedArray()
    }

    fun splitToArray(size: Size): Array<Hex> {
        val sizeHexChars = size.hexChars
        val paddingSize = this.rawInput.length % sizeHexChars
        val paddedString = "0".repeat(paddingSize) + this.rawInput
        return paddedString.chunked(sizeHexChars).map { Hex(it, size) }.toTypedArray()
    }

    override fun check(string: String, size: Size): CheckResult {
        var formatted = string.trim().removePrefix(Settings.PRESTRING_HEX).padStart(size.hexChars, '0')
        val message: String
        if (formatted.all { it.isDigit() || it == 'A' || it == 'B' || it == 'C' || it == 'D' || it == 'E' || it == 'F' || it == 'a' || it == 'b' || it == 'c' || it == 'd' || it == 'e' || it == 'f' }) {
            return if (formatted.length <= size.hexChars) {
                formatted = formatted.padStart(size.hexChars, '0')
                CheckResult(true, formatted)
            } else {
                val trimmedString = formatted.substring(formatted.length - size.hexChars)
                message = "Hex.check(): $string is to long! Casted to TrimmedString(${trimmedString}) This value is layouted to hold up values with width <= ${size.hexChars}!"
                CheckResult(false, trimmedString, message)
            }
        } else {
            val zeroString = "0".repeat(size.hexChars)
            message = "Hex.check(): $string does not match the hex Pattern (${Settings.PRESTRING_HEX + "X".repeat(size.hexChars)} where X is element of [0-9,A-F]), returning $zeroString instead!"
            nativeError(message)
            return CheckResult(false, zeroString, message)
        }
    }

    override fun check(size: Size): CheckResult = check(rawInput, size)
    override fun checkSizeSigned(other: Size): Boolean = toDec().checkSizeSigned(other)
    override fun checkSizeUnsigned(other: Size): Boolean = toBin().checkSizeUnsigned(other)
    override fun toBin(): Bin = getBinary()
    override fun toOct(): Oct = getBinary().getOct()
    override fun toDec(): Dec = getBinary().getDec()
    override fun toUDec(): UDec = getBinary().getUDec()
    override fun toASCII(): String = getASCII()
    override fun toByte(): Byte = toUByte().toByte()
    override fun toShort(): Short = toUShort().toShort()
    override fun toInt(): Int = toUInt().toInt()
    override fun toLong(): Long = toULong().toLong()
    override fun toUByte(): UByte = rawInput.takeLast(2).toUByte(16)
    override fun toUShort(): UShort = rawInput.takeLast(4).toUShort(16)
    override fun toUInt(): UInt = rawInput.takeLast(8).toUInt(16)
    override fun toULong(): ULong = rawInput.toULong(16)
    override fun getBiggest(): Value = Bin("1".repeat(size.bitWidth), size)

    override fun plus(operand: Value): Hex {
        val biggerSize = if (this.size.bitWidth > operand.size.bitWidth) this.size else operand.size

        return if (this.size.bitWidth <= 64 && operand.size.bitWidth <= 64) {
            val result = (toULong() + operand.toULong())
            Hex(result.toString(16), biggerSize)
        } else {
            val result = BinaryTools.add(this.toBin().rawInput, operand.toBin().rawInput)
            Bin(result, biggerSize).toHex()
        }
    }

    override fun minus(operand: Value): Hex {
        val biggerSize = if (this.size.bitWidth > operand.size.bitWidth) this.size else operand.size
        return if (this.size.bitWidth <= 64 && operand.size.bitWidth <= 64) {
            val result = (toULong() - operand.toULong())
            Hex(result.toString(16), biggerSize)
        } else {
            val result = BinaryTools.sub(this.toBin().rawInput, operand.toBin().rawInput)
            Bin(result, biggerSize).toHex()
        }
    }

    override fun times(operand: Value): Hex {
        val biggerSize = if (this.size.bitWidth > operand.size.bitWidth) this.size else operand.size
        return if (this.size.bitWidth <= 64 && operand.size.bitWidth <= 64) {
            val result = (toULong() * operand.toULong())
            Hex(result.toString(16), biggerSize)
        } else {
            val result = BinaryTools.multiply(this.toBin().rawInput, operand.toBin().rawInput)
            Bin(result, biggerSize).toHex()
        }
    }

    override fun div(operand: Value): Hex {
        val biggerSize = if (this.size.bitWidth > operand.size.bitWidth) this.size else operand.size
        return if (this.size.bitWidth <= 64 && operand.size.bitWidth <= 64) {
            val result = (toULong() / operand.toULong())
            Hex(result.toString(16), biggerSize)
        } else {
            val divResult = BinaryTools.divide(this.toBin().rawInput, operand.toBin().rawInput)
            Bin(divResult.result, biggerSize).toHex()
        }
    }

    override fun rem(operand: Value): Hex {
        val biggerSize = if (this.size.bitWidth > operand.size.bitWidth) this.size else operand.size
        return if (this.size.bitWidth <= 64 && operand.size.bitWidth <= 64) {
            val result = (toULong() % operand.toULong())
            Hex(result.toString(16), biggerSize)
        } else {
            val divResult = BinaryTools.divide(this.toBin().rawInput, operand.toBin().rawInput)
            Bin(BinaryTools.checkEmpty(divResult.remainder), biggerSize).toHex()
        }
    }

    override fun unaryMinus(): Value = Bin(BinaryTools.negotiate(this.toBin().rawInput), size)
    override fun inc(): Value = Bin(BinaryTools.add(this.toBin().rawInput, "1"), size)
    override fun dec(): Value = Bin(BinaryTools.sub(this.toBin().rawInput, "1"), size)
    override fun compareTo(other: Value): Int {
        if (this.size.bitWidth <= 64 && other.size.bitWidth <= 64) {
            return toULong().compareTo(other.toULong())
        }
        return if (BinaryTools.isEqual(this.toBin().rawInput, other.toBin().rawInput)) {
            0
        } else if (BinaryTools.isGreaterThan(this.toBin().rawInput, other.toBin().rawInput)) {
            1
        } else {
            -1
        }
    }

    override fun toString(): String = "${Settings.PRESTRING_HEX}$rawInput"
    override fun toHex(): Hex = this

    override fun equals(other: Any?): Boolean {
        if (other is Value) {
            return BinaryTools.isEqual(toBin().rawInput, other.toBin().rawInput)
        }
        return false
    }
}