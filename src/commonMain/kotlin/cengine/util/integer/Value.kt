package cengine.util.integer

import Settings
import cengine.util.integer.Value.*
import debug.DebugTools
import emulator.core.StringTools
import emulator.kit.nativeError
import emulator.kit.nativeInfo
import emulator.kit.nativeWarn

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
sealed class Value(val size: Size) {

    abstract val input: String
    abstract val valid: Boolean

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
    abstract fun toRawString(): String
    fun toRawZeroTrimmedString(): String = StringTools.removeLeadingZeros(toRawString())
    abstract override fun toString(): String

    override fun hashCode(): Int {
        var result = input.hashCode()
        result = 31 * result + size.hashCode()
        return result
    }

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

        override fun toHex(): Hex = Conversion.getHex(this)
        override fun toOct(): Oct = Conversion.getOct(this)
        override fun toBin(): Bin = this
        override fun toDec(): Dec = Conversion.getDec(this)
        override fun toUDec(): UDec = Conversion.getUDec(this)
        override fun toASCII(): String = Conversion.getASCII(this)
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

    /**
     * Provides the octal representation of [Value]
     */
    class Oct(octString: String, size: Size) : Value(size) {
        override val input: String
        override val valid: Boolean

        companion object {
            val regex = Regex("[0-7]+")
        }

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
            if (regex.matches(formatted)) {
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
        override fun toBin(): Bin = Conversion.getBinary(this)
        override fun toHex(): Hex = Conversion.getBinary(this).toHex()
        override fun toOct(): Oct = this
        override fun toDec(): Dec = Conversion.getBinary(this).toDec()
        override fun toUDec(): UDec = Conversion.getBinary(this).toUDec()
        override fun toASCII(): String = Conversion.getASCII(this)
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

        override fun toRawString(): String = input.removePrefix(Settings.PRESTRING_OCT)

        override fun toString(): String = input

        override fun equals(other: Any?): Boolean {
            if (other is Value) {
                return BinaryTools.isEqual(toBin().toRawString(), other.toBin().toRawString())
            }
            return false
        }
    }

    /**
     * Provides the unsigned decimal representation of [Value].
     */
    class UDec(udecString: String, size: Size) : Value(size) {
        override val input: String
        override val valid: Boolean

        companion object {
            private val posRegex = Regex("[0-9]+")
        }

        init {
            val result = check(udecString, size)
            input = result.corrected
            valid = result.valid
        }

        constructor(udecString: String) : this(udecString, Tools.getNearestUDecSize(udecString.trim().removePrefix(Settings.PRESTRING_UDECIMAL))) {
            if (DebugTools.KIT_showValCheckWarnings) {
                println("UDec(): Calculated Size from $udecString as hex ${this.toHex().toRawString()} -> ${size.bitWidth}")
            }
        }

        fun getUResized(size: Size): UDec = UDec(toRawString(), size)

        override fun check(string: String, size: Size): CheckResult {
            val formatted = string.trim().removePrefix(Settings.PRESTRING_UDECIMAL)
            val message: String
            if (!posRegex.matches(formatted)) {
                val zeroString = "0"
                message = "UDec.check(): $formatted does not match the udec Pattern (${Settings.PRESTRING_UDECIMAL + "X".repeat(size.bitWidth)} where X is element of [0-9]), returning $zeroString instead!"
                nativeError(message)
                return CheckResult(false, Settings.PRESTRING_UDECIMAL + zeroString, message)
            } else {
                return if (DecTools.isGreaterThan(formatted, Bounds(size).umax)) {
                    message = "UDec.check(): $formatted must be smaller equal ${Bounds(size).umax} -> setting ${Bounds(size).umax}"
                    CheckResult(false, Settings.PRESTRING_UDECIMAL + Bounds(size).umax, message)
                } else if (DecTools.isGreaterThan(Bounds(size).umin, formatted)) {
                    message = "UDec.check(): $formatted must be bigger equal ${Bounds(size).umin} -> setting ${Bounds(size).umin}"
                    CheckResult(false, Settings.PRESTRING_UDECIMAL + Bounds(size).umin, message)
                } else {
                    CheckResult(true, Settings.PRESTRING_UDECIMAL + formatted)
                }
            }
        }

        override fun check(size: Size): CheckResult = check(toRawString(), size)
        override fun checkSizeSigned(other: Size): Boolean = toDec().checkSizeSigned(other)
        override fun checkSizeUnsigned(other: Size): Boolean = toBin().checkSizeUnsigned(other)
        override fun toBin(): Bin = Conversion.getBinary(this)
        override fun toHex(): Hex = Conversion.getBinary(this).toHex()
        override fun toOct(): Oct = Conversion.getBinary(this).toOct()
        override fun toDec(): Dec = Conversion.getBinary(this).toDec()
        override fun toUDec(): UDec = this
        override fun toASCII(): String = Conversion.getASCII(this)
        fun toIntOrNull(): Int? = toRawString().toIntOrNull()
        fun toDoubleOrNull(): Double? = toRawString().toDoubleOrNull()
        override fun getBiggest(): Value = UDec(Bounds(size).umax, size)

        override fun plus(operand: Value): Value {
            val result = DecTools.add(this.toRawString(), operand.toUDec().toRawString())
            val biggerSize = if (this.size.bitWidth > operand.size.bitWidth) this.size else operand.size
            return UDec(result, biggerSize)
        }

        override fun minus(operand: Value): Value {
            val result = DecTools.sub(this.toRawString(), operand.toUDec().toRawString())
            val biggerSize = if (this.size.bitWidth > operand.size.bitWidth) this.size else operand.size
            return UDec(result, biggerSize)
        }

        override fun times(operand: Value): Value {
            val result = DecTools.multiply(this.toRawString(), operand.toUDec().toRawString())
            val biggerSize = if (this.size.bitWidth > operand.size.bitWidth) this.size else operand.size
            return UDec(result, biggerSize)
        }

        override fun div(operand: Value): Value {
            val divResult = DecTools.divide(this.toRawString(), operand.toUDec().toRawString())
            val biggerSize = if (this.size.bitWidth > operand.size.bitWidth) this.size else operand.size
            return UDec(divResult.result, biggerSize)
        }

        override fun rem(operand: Value): Value {
            val divResult = DecTools.divide(this.toRawString(), operand.toUDec().toRawString())
            val biggerSize = if (this.size.bitWidth > operand.size.bitWidth) this.size else operand.size
            return UDec(DecTools.checkEmpty(divResult.rest), biggerSize)
        }

        override fun unaryMinus(): Value {
            return -this.toDec()
        }

        override fun inc(): Value = Dec(DecTools.add(this.toRawString(), "1"), size)
        override fun dec(): Value = Dec(DecTools.sub(this.toRawString(), "1"), size)

        override fun compareTo(other: Value): Int = if (DecTools.isEqual(this.toRawString(), other.toUDec().toRawString())) {
            0
        } else if (DecTools.isGreaterThan(this.toRawString(), other.toUDec().toRawString())) {
            1
        } else {
            -1
        }

        override fun toRawString(): String = input.removePrefix(Settings.PRESTRING_UDECIMAL)
        override fun toString(): String = input

        override fun equals(other: Any?): Boolean {
            if (other is Value) {
                return BinaryTools.isEqual(toBin().toRawString(), other.toBin().toRawString())
            }
            return false
        }
    }

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
        override fun toBin(): Bin = Conversion.getBinary(this)
        override fun toOct(): Oct = Conversion.getOct(this.toBin())
        override fun toDec(): Dec = Conversion.getDec(this.toBin())
        override fun toUDec(): UDec = Conversion.getUDec(this.toBin())
        override fun toASCII(): String = Conversion.getASCII(this)
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


    /**
     * Provides the decimal representation of [Value].
     */
    class Dec(decString: String, size: Size) : Value(size) {
        override val input: String
        override val valid: Boolean

        private val negative: Boolean

        companion object {
            private val posRegex = Regex("[0-9]+")
        }

        init {
            val result = check(decString, size)
            input = result.corrected
            valid = result.valid
            this.negative = DecTools.isNegative(result.corrected)
        }

        constructor(decString: String) : this(decString, Tools.getNearestDecSize(decString.trim().removePrefix(Settings.PRESTRING_DECIMAL)))

        fun isNegative(): Boolean = negative

        fun getResized(size: Size): Dec = Dec(toRawString(), size)

        override fun check(string: String, size: Size): CheckResult {
            val formatted = string.trim().removePrefix(Settings.PRESTRING_DECIMAL)
            val message: String
            if (!posRegex.matches(formatted.replace("-", ""))) {
                val zeroString = "0"
                message = "Dec.check(): $formatted does not match the dec Pattern (${Settings.PRESTRING_DECIMAL + "(-)" + "X".repeat(size.bitWidth)} where X is element of [0-9]), returning $zeroString instead!"
                nativeError(message)
                return CheckResult(false, Settings.PRESTRING_DECIMAL + zeroString, message)
            } else {
                return if (DecTools.isGreaterThan(formatted, Bounds(size).max)) {
                    message = "Dec.check(): $formatted must be smaller equal ${Bounds(size).max} -> setting ${Bounds(size).max}"
                    CheckResult(false, Settings.PRESTRING_DECIMAL + Bounds(size).max, message)
                } else if (DecTools.isGreaterThan(Bounds(size).min, formatted)) {
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
        override fun toBin(): Bin = Conversion.getBinary(this)
        override fun toHex(): Hex = Conversion.getBinary(this).toHex()
        override fun toOct(): Oct = Conversion.getBinary(this).toOct()
        override fun toDec(): Dec = this
        override fun toUDec(): UDec = Conversion.getUDec(toBin())
        override fun toASCII(): String = Conversion.getBinary(this).toASCII()
        fun toIntOrNull(): Int? = toRawString().toIntOrNull()
        fun toDoubleOrNull(): Double? = toRawString().toDoubleOrNull()
        override fun getBiggest(): Value = Dec(Bounds(size).max, size)
        override fun plus(operand: Value): Value {
            val result = DecTools.add(toRawString(), operand.toDec().toRawString())
            val biggerSize = if (size.bitWidth > operand.size.bitWidth) size else operand.size
            return Dec(result, biggerSize)
        }

        override fun minus(operand: Value): Value {
            val result = DecTools.sub(toRawString(), operand.toDec().toRawString())
            val biggerSize = if (size.bitWidth > operand.size.bitWidth) size else operand.size
            return Dec(result, biggerSize)
        }

        override fun times(operand: Value): Value {
            val result = DecTools.multiply(toRawString(), operand.toDec().toRawString())
            val biggerSize = if (size.bitWidth > operand.size.bitWidth) size else operand.size
            return Dec(result, biggerSize)
        }

        override fun div(operand: Value): Value {
            val divResult = DecTools.divide(toRawString(), operand.toDec().toRawString())
            val biggerSize = if (size.bitWidth > operand.size.bitWidth) size else operand.size
            return Dec(divResult.result, biggerSize)
        }

        override fun rem(operand: Value): Value {
            val divResult = DecTools.divide(toRawString(), operand.toDec().toRawString())
            val biggerSize = if (size.bitWidth > operand.size.bitWidth) size else operand.size
            return Dec(DecTools.checkEmpty(divResult.rest), biggerSize)
        }

        override fun unaryMinus(): Value = Dec(DecTools.negotiate(toRawString()), size)
        override fun inc(): Value = Dec(DecTools.add(toRawString(), "1"), size)
        override fun dec(): Value = Dec(DecTools.sub(toRawString(), "1"), size)
        override fun compareTo(other: Value): Int = if (DecTools.isEqual(toRawString(), other.toDec().toRawString())) {
            0
        } else if (DecTools.isGreaterThan(toRawString(), other.toDec().toRawString())) {
            1
        } else {
            -1
        }

        override fun equals(other: Any?): Boolean {
            if (other is Value) {
                return DecTools.isEqual(toRawString(), other.toDec().toRawString())
            }
            return false
        }

        override fun toRawString(): String = input.removePrefix(Settings.PRESTRING_DECIMAL)
        override fun toString(): String = input
        override fun hashCode(): Int {
            var result = super.hashCode()
            result = 31 * result + input.hashCode()
            return result
        }
    }


    /**
     * Contains all implementations of type conversions, to switch between all Types of [Value].
     */
    object Conversion {
        fun getType(string: String): Value {
            var removedPrefString = string.trim().removePrefix(Settings.PRESTRING_BINARY)
            if (removedPrefString.length < string.trim().length - 1) {
                return Bin("0", Size.Bit8)
            }
            removedPrefString = string.trim().removePrefix(Settings.PRESTRING_HEX)
            if (removedPrefString.length < string.trim().length - 1) {
                return Hex("0", Size.Bit8)
            }
            removedPrefString = string.trim().removePrefix(Settings.PRESTRING_OCT)
            if (removedPrefString.length < string.trim().length - 1) {
                return Oct("0", Size.Bit8)
            }
            removedPrefString = string.trim().removePrefix(Settings.PRESTRING_UDECIMAL)
            if (removedPrefString.length < string.trim().length - 1) {
                return UDec("u0", Size.Bit8)
            }
            return Dec("0", Size.Bit8)
        }

        fun getHex(bin: Bin): Hex {
            var hexStr = ""

            var binStr = bin.toRawString()
            binStr = if (binStr.length % 4 != 0) {
                "0".repeat(4 - (binStr.length % 4)) + binStr
            } else {
                binStr
            }

            for (i in binStr.indices step 4) {
                val substring = binStr.substring(i, i + 4)
                hexStr += BinaryTools.binToHexDigit[substring] ?: break
            }

            if (DebugTools.KIT_showValTypeConversionInfo) {
                nativeInfo("Conversion: ${bin.toString()} to $hexStr")
            }

            return Hex(hexStr, bin.size)
        }

        fun getOct(bin: Bin): Oct {
            var octStr = ""

            var binStr = bin.toRawString()
            binStr = if (binStr.length % 3 != 0) {
                "0".repeat(3 - (binStr.length % 3)) + binStr
            } else {
                binStr
            }

            for (i in binStr.indices step 3) {
                val substring = binStr.substring(i, i + 3)
                octStr += BinaryTools.binToOctDigit[substring] ?: break
            }

            if (DebugTools.KIT_showValTypeConversionInfo) {
                nativeInfo("Conversion: ${bin.toString()} to $octStr")
            }

            return Oct(octStr, bin.size)
        }

        fun getBinary(oct: Oct): Bin {
            var binStr = ""

            val hexStr = oct.toRawString()

            for (i in hexStr.indices) {
                binStr += BinaryTools.octToBinDigit[hexStr[i]]
            }
            if (DebugTools.KIT_showValTypeConversionInfo) {
                nativeInfo("Conversion: ${oct} to $binStr")
            }
            return Bin(binStr, oct.size)
        }

        fun getBinary(hex: Hex): Bin {
            var binStr = ""

            val hexStr = hex.toRawString().uppercase()

            for (i in hexStr.indices) {
                binStr += BinaryTools.hexToBinDigit[hexStr[i]]
            }
            if (DebugTools.KIT_showValTypeConversionInfo) {
                nativeInfo("Conversion: ${hex.toString()} to $binStr")
            }
            return Bin(binStr, hex.size)
        }

        fun getBinary(dec: Dec): Bin {

            var decString = dec.toRawString()

            if (dec.isNegative()) {
                decString = DecTools.negotiate(decString)
                decString = DecTools.sub(decString, "1")
            }

            var binaryStr = ""

            for (i in dec.size.bitWidth - 1 downTo 0) {
                val weight = DecTools.binaryWeights[i].weight
                if (DecTools.isGreaterEqualThan(decString, weight)) {
                    binaryStr += "1"
                    decString = DecTools.sub(decString, weight)
                } else {
                    binaryStr += "0"
                }
            }

            if (binaryStr == "") {
                nativeWarn("Conversion.getBinary(dec: Dec) : error in calculation ${dec.toRawString()} to $binaryStr")
            }

            if (dec.isNegative()) {
                binaryStr = BinaryTools.inv(binaryStr)
            }

            if (DebugTools.KIT_showValTypeConversionInfo) {
                nativeInfo("Conversion: ${dec.toString()} to $binaryStr")
            }

            return Bin(binaryStr, dec.size)
        }

        fun getBinary(udec: UDec): Bin {

            var udecString = udec.toRawString()

            var binaryStr = ""

            for (i in udec.size.bitWidth - 1 downTo 0) {
                val weight = DecTools.binaryWeights[i].weight
                if (DecTools.isGreaterEqualThan(udecString, weight)) {
                    binaryStr += "1"
                    udecString = DecTools.sub(udecString, weight)
                } else {
                    binaryStr += "0"
                }
            }

            if (binaryStr == "") {
                nativeWarn("Conversion.getBinary(udec: UDec) : error in calculation ${udec.toRawString()} to $binaryStr")
            }

            if (DebugTools.KIT_showValTypeConversionInfo) {
                nativeInfo("Conversion: ${udec.toString()} to $binaryStr")
            }

            return Bin(binaryStr, udec.size)
        }

        fun getDec(bin: Bin): Dec {
            var binString = bin.toRawString()
            var decString = "0"
            val negative: Boolean

            // check negative/pos binary value
            if (bin.size.bitWidth == binString.length) {
                if (binString[0] == '1') {
                    negative = true
                    binString = BinaryTools.negotiate(binString)
                } else {
                    negative = false
                }
            } else {
                binString = binString.padStart(bin.size.bitWidth, '0')
                negative = false
            }

            for (index in binString.indices) {
                val binWeight = binString.length - 1 - index
                val decWeight = DecTools.binaryWeights[binWeight].weight
                if (binString[index] == '1') {
                    decString = DecTools.add(decString, decWeight)
                }
            }

            if (negative) {
                decString = DecTools.negotiate(decString)
            }

            if (DebugTools.KIT_showValTypeConversionInfo) {
                nativeInfo("Conversion: ${bin.toString()} to $decString")
            }

            return Dec(decString, bin.size)
        }

        fun getUDec(bin: Bin): UDec {
            val binString = bin.toRawString()

            var udecString = "0"
            if (binString.isNotEmpty()) {
                val absBin: String = binString

                for (index in absBin.indices) {
                    val binWeight = absBin.length - 1 - index
                    val decWeight = DecTools.binaryWeights[binWeight].weight
                    // val add = DecTools.pow("2", i.toString(10))
                    if (absBin[index] == '1') {
                        udecString = DecTools.add(udecString, decWeight)
                    }
                }
            }
            if (DebugTools.KIT_showValTypeConversionInfo) {
                nativeInfo("Conversion: ${bin.toString()} to $udecString")
            }

            return UDec(udecString, bin.size)
        }

        fun getASCII(value: Value): String {
            val stringBuilder = StringBuilder()

            val hexString = when (value) {
                is Hex -> value.toRawString()
                is Oct -> value.toHex().toRawString()
                is Bin -> value.toHex().toRawString()
                is Dec -> value.toHex().toRawString()
                is UDec -> value.toHex().toRawString()
            }

            val trimmedHex = hexString.trim().removePrefix(Settings.PRESTRING_HEX)

            for (i in trimmedHex.indices step 2) {
                val hex = trimmedHex.substring(i, i + 2)
                val decimal = hex.toIntOrNull(16)

                if ((decimal != null) && (decimal in (32..126))) {
                    stringBuilder.append(decimal.toChar())
                } else {
                    stringBuilder.append("·")
                }
            }

            if (DebugTools.KIT_showValTypeConversionInfo) {
                nativeInfo("Conversion: ${value.toHex().toString()} to $stringBuilder")
            }

            return stringBuilder.toString()
        }

    }

    /**
     * This will only be used by the visual components to get all representation types fast and allow a switch between them.
     */
    enum class Types(val visibleName: String) {
        Bin("BIN"),
        Hex("HEX"),
        Dec("DEC"),
        UDec("UDEC")
    }

    /**
     * Contains all additional needed tools for working with [Value].
     */
    object Tools {
        fun String.asDec(size: Size): Dec = Dec(this, size)
        fun String.asDec(): Dec = Dec(this, getNearestDecSize(this))
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
        fun UShort.toValue(size: Size = Size.Bit16): UDec = UDec(this.toString(), size)
        fun UInt.toValue(size: Size = Size.Bit32): UDec = UDec(this.toString(), size)
        fun ULong.toValue(size: Size = Size.Bit64): UDec = UDec(this.toString(), size)


        fun Array<Bin>.mergeToChunks(currSize: Size, chunkSize: Size): Array<Bin> {
            val source = this.toMutableList()
            val amount = chunkSize.bitWidth / currSize.bitWidth
            val padding = this.size % amount

            repeat(padding) {
                source.add(0, Bin("0", currSize))
            }

            return source.chunked(amount).map { values ->
                Bin(values.joinToString("") { it.toRawString() }, chunkSize)
            }.toTypedArray()
        }

        fun Array<Hex>.mergeToChunks(currSize: Size, chunkSize: Size): Array<Hex> {
            val source = this.toMutableList()
            val amount = chunkSize.hexChars / currSize.hexChars
            val padding = this.size % amount

            repeat(padding) {
                source.add(0, Hex("0", currSize))
            }

            return source.chunked(amount).map { values ->
                Hex(values.joinToString("") { it.toRawString() }, chunkSize)
            }.toTypedArray()
        }

        fun getNearestSize(bitWidth: Int): Size {
            when {
                bitWidth <= 8 -> {
                    return Size.Bit8
                }

                bitWidth <= 16 -> {
                    return Size.Bit16
                }

                bitWidth <= 32 -> {
                    return Size.Bit32
                }

                bitWidth <= 64 -> {
                    return Size.Bit64
                }

                bitWidth <= 128 -> {
                    return Size.Bit128
                }

                else -> {
                    nativeWarn("Bounds.getNearestSize(): $bitWidth is greater than possible maximum Size of 128bit -> returning Size.Bit128")
                    return Size.Bit128
                }
            }
        }

        fun getNearestDecSize(decString: String): Size {
            val string = decString.trim().removePrefix(Settings.PRESTRING_DECIMAL)
            when {
                DecTools.isGreaterEqualThan(Bounds(Size.Bit8).max, string) && DecTools.isGreaterEqualThan(string, Bounds(Size.Bit8).min) -> {
                    return Size.Bit8
                }

                DecTools.isGreaterEqualThan(Bounds(Size.Bit16).max, string) && DecTools.isGreaterEqualThan(string, Bounds(Size.Bit16).min) -> {
                    return Size.Bit16
                }

                DecTools.isGreaterEqualThan(Bounds(Size.Bit32).max, string) && DecTools.isGreaterEqualThan(string, Bounds(Size.Bit32).min) -> {
                    return Size.Bit32
                }

                DecTools.isGreaterEqualThan(Bounds(Size.Bit64).max, string) && DecTools.isGreaterEqualThan(string, Bounds(Size.Bit64).min) -> {
                    return Size.Bit64
                }

                DecTools.isGreaterEqualThan(Bounds(Size.Bit128).max, string) && DecTools.isGreaterEqualThan(string, Bounds(Size.Bit128).min) -> {
                    return Size.Bit128
                }

                else -> {
                    nativeWarn("Bounds.getNearestDecSize(): $decString is not in Bounds of Size.Bit128 [max: ${Bounds(Size.Bit128).max}, min: ${Bounds(Size.Bit128).min}] -> returning Size.Bit128")
                    return Size.Bit128
                }
            }
        }

        fun getNearestUDecSize(udecString: String): Size {
            val string = udecString.trim().removePrefix(Settings.PRESTRING_UDECIMAL)
            when {
                DecTools.isGreaterEqualThan(Bounds(Size.Bit8).umax, string) -> {
                    return Size.Bit8
                }

                DecTools.isGreaterEqualThan(Bounds(Size.Bit16).umax, string) -> {
                    return Size.Bit16
                }

                DecTools.isGreaterEqualThan(Bounds(Size.Bit32).umax, string) -> {
                    return Size.Bit32
                }

                DecTools.isGreaterEqualThan(Bounds(Size.Bit64).umax, string) -> {
                    return Size.Bit64
                }

                DecTools.isGreaterEqualThan(Bounds(Size.Bit128).umax, string) -> {
                    return Size.Bit128
                }

                else -> {
                    nativeWarn("Bounds.getNearestDecSize(): $udecString is not in Bounds of Size.Bit128 [max: ${Bounds(Size.Bit128).umax}, min: ${Bounds(Size.Bit128).umin}] -> returning Size.Bit128")
                    return Size.Bit128
                }
            }
        }

        fun asciiToHex(asciiString: String): String {
            return asciiString.map {
                it.code.toString(16).padStart(2, '0')
            }.joinToString("") { it }
        }

    }

    data class CheckResult(val valid: Boolean, val corrected: String, val message: String = "")
    data class AddResult(val result: Bin, val carry: Boolean)
    data class SubResult(val result: Bin, val borrow: Boolean)

}