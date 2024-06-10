package emulator.kit.types

import Settings
import debug.DebugTools
import emulator.kit.nativeError
import emulator.kit.nativeInfo
import emulator.kit.nativeWarn
import emulator.kit.types.Variable.Value
import emulator.kit.types.Variable.Value.*
import kotlin.math.roundToInt


/**
 * [Variable] is the mutable version of [Value] which can contain several types all based on [String].
 * Each Variable has a fixed [size] which can't be changed. When a new value will be [set] it will automatically be resized to the former [size].
 * Operator Functions such as comparing functions are overwritten.
 */
class Variable {
    private val initialBinary: String
    val size: Size
    var value: Value

    constructor(initialBinary: String, size: Size) {
        this.initialBinary = initialBinary
        this.size = size
        value = Value.Bin(initialBinary, size)
    }

    constructor(value: Value) {
        this.value = value
        this.size = value.size
        this.initialBinary = value.toBin().getBinaryStr()
    }

    constructor(size: Size) {
        this.value = Value.Bin("0", size)
        this.size = size
        this.initialBinary = value.toBin().getBinaryStr()
    }

    /* GETTER SETTER */
    fun get(): Value = value

    fun get(type: Value.Types): Value = when (type) {
        Value.Types.Bin -> value.toBin()
        Value.Types.Hex -> value.toHex()
        Value.Types.Dec -> value.toDec()
        Value.Types.UDec -> value.toUDec()
    }


    fun getBounds(): Bounds = Bounds(size)

    fun set(value: Value): Variable {
        this.value = value.toBin().getResized(size)
        return this
    }

    fun setHex(hexString: String): Variable {
        value = Value.Hex(hexString, size)
        return this
    }

    fun setDec(decString: String): Variable {
        value = Value.Dec(decString, size)
        return this
    }

    fun setUDec(udecString: String): Variable {
        value = Value.UDec(udecString, size)
        return this
    }

    fun setBin(binString: String): Variable {
        value = Value.Bin(binString, size)
        return this
    }

    fun clear() {
        value = Value.Bin(initialBinary, size)
    }

    /* operator */
    operator fun plus(operand: Variable): Variable = Variable(value + operand.get())
    operator fun minus(operand: Variable): Variable = Variable(value - operand.get())
    operator fun times(operand: Variable): Variable = Variable(value * operand.get())
    operator fun div(operand: Variable): Variable = Variable(value / operand.get())
    operator fun rem(operand: Variable): Variable = Variable(value % operand.get())
    operator fun unaryMinus(): Variable = Variable(-value)
    override fun equals(other: Any?): Boolean {
        when (other) {
            is Variable -> {
                return (this.size == other.size && this.value == other.value)
            }

            is Value -> {
                return (this.value == other)
            }
        }
        return super.equals(other)
    }

    override fun toString(): String {
        return this.value.toString()
    }

    operator fun inc(): Variable {
        this.value = this.value++
        return Variable(++value)
    }

    operator fun dec(): Variable {
        this.value = this.value--
        return Variable(--value)
    }

    override fun hashCode(): Int {
        return value.hashCode()
    }

    /**
     * A [Value] can have the following types: [Bin], [Hex], [Dec] or [UDec].
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
    sealed class Value(val input: String, val size: Size) {

        lateinit var checkResult: CheckResult

        /**
         * This function implements the [String] format checks, of each specific type.
         * This function can write errors and warnings such as applied changes to the Console if any input isn't valid.
         */
        protected abstract fun check(string: String, size: Size): CheckResult
        abstract fun check(size: Size): CheckResult
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
        override fun equals(other: Any?): Boolean {
            when (other) {
                is Value -> {
                    return BinaryTools.isEqual(this.toBin().getRawBinStr(), other.toBin().getRawBinStr())
                }
            }
            return super.equals(other)
        }

        abstract fun toRawString(): String
        fun toRawZeroTrimmedString(): String = StringTools.removeLeadingZeros(toRawString())
        abstract override fun toString(): String
        fun isSigned(): Boolean {
            return when (this) {
                is Dec -> true
                is Bin -> false
                is Hex -> false
                is UDec -> false
                is Oct -> false
            }
        }

        override fun hashCode(): Int {
            var result = input.hashCode()
            result = 31 * result + size.hashCode()
            return result
        }

        /**
         * Provides the binary representation of [Value].
         */
        class Bin(binString: String, size: Size) : Value(binString, size) {
            private val binString: String
            val regex = Regex("[0-1]+")

            constructor(size: Size) : this(Settings.PRESTRING_BINARY + "0", size)

            constructor(binString: String) : this(binString, Size.Original(binString.trim().removePrefix(Settings.PRESTRING_BINARY).length))

            init {
                this.checkResult = check(input, size)
                this.binString = checkResult.corrected
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

            override fun check(size: Size): CheckResult = check(getRawBinStr(), size)
            fun getRawBinStr(): String = binString.removePrefix(Settings.PRESTRING_BINARY)
            fun getBinaryStr(): String = binString

            fun getUResized(size: Size): Bin {
                val paddedBinString = if (size.bitWidth < this.size.bitWidth) {
                    getRawBinStr().substring(getRawBinStr().length - size.bitWidth)
                } else {
                    getRawBinStr().padStart(size.bitWidth, '0')
                }
                return Bin(paddedBinString, size)
            }

            fun getResized(size: Size): Bin {
                val paddedBinString = if (size.bitWidth < this.size.bitWidth) {
                    getRawBinStr().substring(getRawBinStr().length - size.bitWidth)
                } else {
                    getRawBinStr().padStart(size.bitWidth, if (getRawBinStr().first() == '1') '1' else '0')
                }
                return Bin(paddedBinString, size)
            }

            fun splitToByteArray(): Array<Bin> {
                val paddedString = if (this.getRawBinStr().length % 8 == 0) this.getRawBinStr() else this.getRawBinStr().padStart(this.getRawBinStr().length + (8 - this.getRawBinStr().length % 8), '0')
                return paddedString.chunked(8).map { Bin(it, Size.Bit8()) }.toTypedArray()
            }

            /**
             * Returns null if Matches
             */
            fun checkSizeUnsigned(size: Size): NoMatch? {
                return when {
                    this.size.bitWidth == size.bitWidth -> {
                        null
                    }

                    this.size.bitWidth > size.bitWidth -> {
                        val exceeding = this.getRawBinStr().substring(0, this.size.bitWidth - size.bitWidth)
                        if (exceeding.indexOf('1') == -1) null else NoMatch(this.size, size)
                    }

                    this.size.bitWidth < size.bitWidth -> {
                        if (this.getRawBinStr().first() == '1') {
                            return NoMatch(this.size, size, true)
                        }
                        return null
                    }

                    else -> {
                        null
                    }
                }
            }

            fun checkSizeSigned(size: Size): NoMatch? {
                return when {
                    this.size.bitWidth == size.bitWidth -> {
                        null
                    }

                    this.size.bitWidth > size.bitWidth -> {
                        val exceeding = this.getRawBinStr().substring(0, this.size.bitWidth - size.bitWidth)
                        return if (exceeding.first() == '0') {
                            if (exceeding.indexOf('1') == -1) null else NoMatch(this.size, size)
                        } else {
                            if (exceeding.indexOf('0') == -1) null else NoMatch(this.size, size)
                        }
                    }

                    this.size.bitWidth < size.bitWidth -> {
                        if (this.getRawBinStr().first() == '1') {
                            return NoMatch(this.size, size, true)
                        }
                        return null
                    }

                    else -> {
                        null
                    }
                }
            }

            override fun toHex(): Hex = Conversion.getHex(this)
            override fun toOct(): Oct = Conversion.getOct(this)
            override fun toBin(): Bin = this
            override fun toDec(): Dec = Conversion.getDec(this)
            override fun toUDec(): UDec = Conversion.getUDec(this)
            override fun toASCII(): String = Conversion.getASCII(this)
            override fun getBiggest(): Value = Bin("1".repeat(size.bitWidth), size)

            override fun plus(operand: Value): Value {
                val result = BinaryTools.add(this.getRawBinStr(), operand.toBin().getRawBinStr())
                val biggerSize = if (this.size.bitWidth > operand.size.bitWidth) this.size else operand.size
                return Bin(result, biggerSize)
            }

            fun detailedPlus(operand: Value): AddResult {
                val result = BinaryTools.addWithCarry(this.getRawBinStr(), operand.toBin().getRawBinStr())
                val biggerSize = if (this.size.bitWidth > operand.size.bitWidth) this.size else operand.size
                return AddResult(Bin(result.result, biggerSize), result.carry == '1')
            }

            override fun minus(operand: Value): Value {
                val result = BinaryTools.sub(this.getRawBinStr(), operand.toBin().getRawBinStr())
                val biggerSize = if (this.size.bitWidth > operand.size.bitWidth) this.size else operand.size
                return Bin(result, biggerSize)
            }

            fun detailedMinus(operand: Value): SubResult {
                val result = BinaryTools.subWithBorrow(this.getRawBinStr(), operand.toBin().getRawBinStr())
                val biggerSize = if (this.size.bitWidth > operand.size.bitWidth) this.size else operand.size
                return SubResult(Bin(result.result, biggerSize), result.borrow == '1')
            }

            override fun times(operand: Value): Value {
                val result = BinaryTools.multiply(this.getRawBinStr(), operand.toBin().getRawBinStr())
                //val biggerSize = if (this.size.bitWidth > operand.size.bitWidth) this.size else operand.size
                return Bin(result)
            }

            /**
             * Flexible Multiplication Operation
             */
            fun flexTimesSigned(factor: Bin, resizeToLargestParamSize: Boolean = true, factorIsUnsigned: Boolean = false): Bin {
                val result = if (factorIsUnsigned) BinaryTools.multiplyMixed(this.getRawBinStr(), factor.getRawBinStr()) else BinaryTools.multiplySigned(this.getRawBinStr(), factor.getRawBinStr())
                val biggerSize = if (this.size.bitWidth > factor.size.bitWidth) this.size else factor.size

                return if (resizeToLargestParamSize) Bin(result).getResized(biggerSize) else Bin(result)
            }

            override fun div(operand: Value): Value {
                val divResult = BinaryTools.divide(this.getRawBinStr(), operand.toBin().getRawBinStr())
                val biggerSize = if (this.size.bitWidth > operand.size.bitWidth) this.size else operand.size
                return Bin(divResult.result).getUResized(biggerSize)
            }

            fun flexDivSigned(divisor: Bin, resizeToLargestParamSize: Boolean = true, dividendIsUnsigned: Boolean = false): Bin {
                val divResult = if (dividendIsUnsigned) BinaryTools.divideMixed(this.getRawBinStr(), divisor.getRawBinStr()) else BinaryTools.divideSigned(this.getRawBinStr(), divisor.getRawBinStr())
                val biggerSize = if (this.size.bitWidth > divisor.size.bitWidth) this.size else divisor.size
                return if (resizeToLargestParamSize) Bin(divResult.result).getResized(biggerSize) else Bin(divResult.result)
            }

            override fun rem(operand: Value): Value {
                val divResult = BinaryTools.divide(this.getRawBinStr(), operand.toBin().getRawBinStr())
                val biggerSize = if (this.size.bitWidth > operand.size.bitWidth) this.size else operand.size
                return Bin(BinaryTools.checkEmpty(divResult.remainder), biggerSize)
            }

            fun flexRemSigned(divisor: Bin, resizeToLargestParamSize: Boolean = true): Bin {
                val divResult = BinaryTools.divideSigned(this.getRawBinStr(), divisor.getRawBinStr())
                val biggerSize = if (this.size.bitWidth > divisor.size.bitWidth) this.size else divisor.size
                return if (resizeToLargestParamSize) Bin(BinaryTools.checkEmpty(divResult.remainder)).getResized(biggerSize) else Bin(divResult.remainder)
            }

            override fun unaryMinus(): Value = Bin(BinaryTools.negotiate(this.getRawBinStr()), size)

            override fun inc(): Value = Bin(BinaryTools.add(this.getRawBinStr(), "1"), size)

            override fun dec(): Value = Bin(BinaryTools.sub(this.getRawBinStr(), "1"), size)

            override fun compareTo(other: Value): Int {
                return if (BinaryTools.isEqual(getRawBinStr(), other.toBin().getRawBinStr())) {
                    0
                } else if (BinaryTools.isGreaterThan(getRawBinStr(), other.toBin().getRawBinStr())) {
                    1
                } else {
                    -1
                }
            }

            override fun toRawString(): String = getRawBinStr()
            override fun toString(): String = binString

            infix fun shl(bitCount: Int): Bin {
                val shiftedBinary = getRawBinStr().substring(bitCount).padEnd(size.bitWidth, '0')
                return Bin(shiftedBinary, size)
            }

            infix fun ushl(bitCount: Int): Bin {
                val shiftedBinary = getRawBinStr().substring(bitCount).padEnd(size.bitWidth, '0')
                return Bin(shiftedBinary, size)
            }

            infix fun shr(bitCount: Int): Bin {
                val shiftedBinary = getRawBinStr().padStart(size.bitWidth * 2, getRawBinStr().first()).substring(0, size.bitWidth * 2 - bitCount).padStart(size.bitWidth * 2, getRawBinStr().first())
                return Bin(shiftedBinary.substring(shiftedBinary.length - size.bitWidth), size)
            }

            infix fun ushr(bitCount: Int): Bin {
                val shiftedBinary = getRawBinStr().padStart(size.bitWidth * 2, '0').substring(0, size.bitWidth * 2 - bitCount).padStart(size.bitWidth * 2, '0')
                return Bin(shiftedBinary.substring(shiftedBinary.length - size.bitWidth), size)
            }

            fun rotateLeft(bitCount: Int): Bin {
                val normalizedShift = bitCount % getRawBinStr().length
                val doubled = getRawBinStr() + getRawBinStr()
                val rotatedBinStr = doubled.substring(normalizedShift, normalizedShift + getRawBinStr().length)
                return Bin(rotatedBinStr, size)
            }

            fun rotateRight(bitCount: Int): Bin {
                val normalizedShift = bitCount % getRawBinStr().length
                val doubled = getRawBinStr() + getRawBinStr()
                val rotatedBinStr = doubled.substring(getRawBinStr().length - normalizedShift, 2 * getRawBinStr().length - normalizedShift)
                return Bin(rotatedBinStr, size)
            }

            infix fun xor(bin2: Bin): Bin {
                val biggestSize = if (size.bitWidth >= bin2.size.bitWidth) size else bin2.size
                return Bin(BinaryTools.xor(getRawBinStr(), bin2.getRawBinStr()), biggestSize)
            }

            infix fun or(bin2: Bin): Bin {
                val biggestSize = if (size.bitWidth >= bin2.size.bitWidth) size else bin2.size
                return Bin(BinaryTools.or(getRawBinStr(), bin2.getRawBinStr()), biggestSize)
            }

            infix fun and(bin2: Bin): Bin {
                val biggestSize = if (size.bitWidth >= bin2.size.bitWidth) size else bin2.size
                return Bin(BinaryTools.and(getRawBinStr(), bin2.getRawBinStr()), biggestSize)
            }

            operator fun inv(): Bin {
                return Bin(BinaryTools.inv(getRawBinStr()), size)
            }

            fun getBit(index: Int): Bin? {
                val bit = getRawBinStr().getOrNull(index) ?: return null
                return Bin(bit.toString(), Size.Bit1())
            }

            data class NoMatch(val size: Size, val expectedSize: Size, val needsSignExtension: Boolean = false)

        }

        /**
         * Provides the hexadecimal representation of [Value].
         */
        class Hex(hexString: String, size: Size) : Value(hexString, size) {
            private val hexString: String
            val regex = Regex("[0-9A-Fa-f]+")

            init {
                this.checkResult = check(input, size)
                this.hexString = checkResult.corrected
            }

            constructor(hexString: String) : this(hexString, Size.Original(hexString.trim().removePrefix(Settings.PRESTRING_HEX).length * 4))

            fun getRawHexStr(): String = hexString.removePrefix(Settings.PRESTRING_HEX).lowercase()
            fun getHexStr(): String = hexString.lowercase()
            fun getUResized(size: Size): Hex = Hex(getRawHexStr(), size)

            fun splitToByteArray(): Array<Hex> {
                val paddedString = if (this.getRawHexStr().length % 2 == 0) this.getRawHexStr() else this.getRawHexStr().padStart(this.getRawHexStr().length + 1, '0')
                return paddedString.chunked(2).map { Hex(it, Size.Bit8()) }.toTypedArray()
            }

            fun splitToArray(size: Size): Array<Hex> {
                val sizeHexChars = size.hexChars
                val paddingSize = this.getRawHexStr().length % sizeHexChars
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

            override fun check(size: Size): CheckResult = check(getRawHexStr(), size)
            override fun toBin(): Bin = Conversion.getBinary(this)
            override fun toOct(): Oct = Conversion.getOct(this.toBin())
            override fun toDec(): Dec = Conversion.getDec(this.toBin())
            override fun toUDec(): UDec = Conversion.getUDec(this.toBin())
            override fun toASCII(): String = Conversion.getASCII(this)
            override fun getBiggest(): Value = Bin("1".repeat(size.bitWidth), size)

            override fun plus(operand: Value): Value {
                val result = BinaryTools.add(this.toBin().getRawBinStr(), operand.toBin().getRawBinStr())
                val biggerSize = if (this.size.bitWidth > operand.size.bitWidth) this.size else operand.size
                return Bin(result, biggerSize)
            }

            override fun minus(operand: Value): Value {
                val result = BinaryTools.sub(this.toBin().getRawBinStr(), operand.toBin().getRawBinStr())
                val biggerSize = if (this.size.bitWidth > operand.size.bitWidth) this.size else operand.size
                return Bin(result, biggerSize)
            }

            override fun times(operand: Value): Value {
                val result = BinaryTools.multiply(this.toBin().getRawBinStr(), operand.toBin().getRawBinStr())
                val biggerSize = if (this.size.bitWidth > operand.size.bitWidth) this.size else operand.size
                return Bin(result, biggerSize)
            }

            override fun div(operand: Value): Value {
                val divResult = BinaryTools.divide(this.toBin().getRawBinStr(), operand.toBin().getRawBinStr())
                val biggerSize = if (this.size.bitWidth > operand.size.bitWidth) this.size else operand.size
                return Bin(divResult.result, biggerSize)
            }

            override fun rem(operand: Value): Value {
                val divResult = BinaryTools.divide(this.toBin().getRawBinStr(), operand.toBin().getRawBinStr())
                val biggerSize = if (this.size.bitWidth > operand.size.bitWidth) this.size else operand.size
                return Bin(BinaryTools.checkEmpty(divResult.remainder), biggerSize)
            }

            override fun unaryMinus(): Value = Bin(BinaryTools.negotiate(this.toBin().getRawBinStr()), size)
            override fun inc(): Value = Bin(BinaryTools.add(this.toBin().getRawBinStr(), "1"), size)
            override fun dec(): Value = Bin(BinaryTools.sub(this.toBin().getRawBinStr(), "1"), size)
            override fun compareTo(other: Value): Int = if (BinaryTools.isEqual(this.toBin().getRawBinStr(), other.toBin().getRawBinStr())) {
                0
            } else if (BinaryTools.isGreaterThan(this.toBin().getRawBinStr(), other.toBin().getRawBinStr())) {
                1
            } else {
                -1
            }

            override fun toRawString(): String = this.getRawHexStr()
            override fun toString(): String = this.hexString.lowercase()
            override fun toHex(): Hex = this

        }

        /**
         * Provides the octal representation of [Value]
         */
        class Oct(octString: String, size: Size) : Value(octString, size) {
            private val octString: String
            val regex = Regex("[0-7]+")

            init {
                this.checkResult = check(input, size)
                this.octString = checkResult.corrected
            }

            fun getRawOctStr(): String = octString.removePrefix(Settings.PRESTRING_OCT)
            fun getOctString(): String = octString
            fun getUResized(size: Size): Oct = Oct(getRawOctStr(), size)

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

            override fun check(size: Size): CheckResult = check(getRawOctStr(), size)

            override fun toBin(): Bin = Conversion.getBinary(this)

            override fun toHex(): Hex = Conversion.getBinary(this).toHex()
            override fun toOct(): Oct = this

            override fun toDec(): Dec = Conversion.getBinary(this).toDec()

            override fun toUDec(): UDec = Conversion.getBinary(this).toUDec()

            override fun toASCII(): String = Conversion.getASCII(this)

            override fun getBiggest(): Value = Bin("1".repeat(size.bitWidth), size)

            override fun plus(operand: Value): Value {
                val result = BinaryTools.add(this.toBin().getRawBinStr(), operand.toBin().getRawBinStr())
                val biggerSize = if (this.size.bitWidth > operand.size.bitWidth) this.size else operand.size
                return Bin(result, biggerSize)
            }

            override fun minus(operand: Value): Value {
                val result = BinaryTools.sub(this.toBin().getRawBinStr(), operand.toBin().getRawBinStr())
                val biggerSize = if (this.size.bitWidth > operand.size.bitWidth) this.size else operand.size
                return Bin(result, biggerSize)
            }

            override fun times(operand: Value): Value {
                val result = BinaryTools.multiply(this.toBin().getRawBinStr(), operand.toBin().getRawBinStr())
                val biggerSize = if (this.size.bitWidth > operand.size.bitWidth) this.size else operand.size
                return Bin(result, biggerSize)
            }

            override fun div(operand: Value): Value {
                val divResult = BinaryTools.divide(this.toBin().getRawBinStr(), operand.toBin().getRawBinStr())
                val biggerSize = if (this.size.bitWidth > operand.size.bitWidth) this.size else operand.size
                return Bin(divResult.result, biggerSize)
            }

            override fun rem(operand: Value): Value {
                val divResult = BinaryTools.divide(this.toBin().getRawBinStr(), operand.toBin().getRawBinStr())
                val biggerSize = if (this.size.bitWidth > operand.size.bitWidth) this.size else operand.size
                return Bin(BinaryTools.checkEmpty(divResult.remainder), biggerSize)
            }

            override fun unaryMinus(): Value = Bin(BinaryTools.negotiate(this.toBin().getRawBinStr()), size)

            override fun inc(): Value = Bin(BinaryTools.add(this.toBin().getRawBinStr(), "1"), size)

            override fun dec(): Value = Bin(BinaryTools.sub(this.toBin().getRawBinStr(), "1"), size)

            override fun compareTo(other: Value): Int = if (BinaryTools.isEqual(this.toBin().getRawBinStr(), other.toBin().getRawBinStr())) {
                0
            } else if (BinaryTools.isGreaterThan(this.toBin().getRawBinStr(), other.toBin().getRawBinStr())) {
                1
            } else {
                -1
            }

            override fun toRawString(): String = getRawOctStr()

            override fun toString(): String = getOctString()
        }

        /**
         * Provides the decimal representation of [Value].
         */
        class Dec(decString: String, size: Size) : Value(decString, size) {
            private val decString: String
            private val negative: Boolean
            private val posRegex = Regex("[0-9]+")

            init {
                this.checkResult = check(input, size)
                this.decString = checkResult.corrected
                this.negative = DecTools.isNegative(checkResult.corrected)
            }

            constructor(decString: String) : this(decString, Tools.getNearestDecSize(decString.trim().removePrefix(Settings.PRESTRING_DECIMAL)))

            fun isNegative(): Boolean = negative
            fun getRawDecStr(): String = decString.removePrefix(Settings.PRESTRING_DECIMAL)
            fun getDecStr(): String = decString
            fun getResized(size: Size): Dec = Dec(getRawDecStr(), size)

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

            override fun check(size: Size): CheckResult = check(getRawDecStr(), size)
            override fun toBin(): Bin = Conversion.getBinary(this)
            override fun toHex(): Hex = Conversion.getBinary(this).toHex()
            override fun toOct(): Oct = Conversion.getBinary(this).toOct()
            override fun toDec(): Dec = this
            override fun toUDec(): UDec = Conversion.getUDec(this.toBin())
            override fun toASCII(): String = Conversion.getBinary(this).toASCII()
            fun toIntOrNull(): Int? = getRawDecStr().toIntOrNull()
            fun toDoubleOrNull(): Double? = getRawDecStr().toDoubleOrNull()
            override fun getBiggest(): Value = Dec(Bounds(size).max, size)
            override fun plus(operand: Value): Value {
                val result = DecTools.add(this.getRawDecStr(), operand.toDec().getRawDecStr())
                val biggerSize = if (this.size.bitWidth > operand.size.bitWidth) this.size else operand.size
                return Dec(result, biggerSize)
            }

            override fun minus(operand: Value): Value {
                val result = DecTools.sub(this.getRawDecStr(), operand.toDec().getRawDecStr())
                val biggerSize = if (this.size.bitWidth > operand.size.bitWidth) this.size else operand.size
                return Dec(result, biggerSize)
            }

            override fun times(operand: Value): Value {
                val result = DecTools.multiply(this.getRawDecStr(), operand.toDec().getRawDecStr())
                val biggerSize = if (this.size.bitWidth > operand.size.bitWidth) this.size else operand.size
                return Dec(result, biggerSize)
            }

            override fun div(operand: Value): Value {
                val divResult = DecTools.divide(this.getRawDecStr(), operand.toDec().getRawDecStr())
                val biggerSize = if (this.size.bitWidth > operand.size.bitWidth) this.size else operand.size
                return Dec(divResult.result, biggerSize)
            }

            override fun rem(operand: Value): Value {
                val divResult = DecTools.divide(this.getRawDecStr(), operand.toDec().getRawDecStr())
                val biggerSize = if (this.size.bitWidth > operand.size.bitWidth) this.size else operand.size
                return Dec(DecTools.checkEmpty(divResult.rest), biggerSize)
            }

            override fun unaryMinus(): Value = Dec(DecTools.negotiate(this.getRawDecStr()), size)
            override fun inc(): Value = Dec(DecTools.add(this.getRawDecStr(), "1"), size)
            override fun dec(): Value = Dec(DecTools.sub(this.getRawDecStr(), "1"), size)
            override fun compareTo(other: Value): Int = if (DecTools.isEqual(this.getRawDecStr(), other.toDec().getRawDecStr())) {
                0
            } else if (DecTools.isGreaterThan(this.getRawDecStr(), other.toDec().getRawDecStr())) {
                1
            } else {
                -1
            }

            override fun toRawString(): String = this.getRawDecStr()
            override fun toString(): String = this.decString
        }

        /**
         * Provides the unsigned decimal representation of [Value].
         */
        class UDec(udecString: String, size: Size) : Value(udecString, size) {
            private val udecString: String
            private val posRegex = Regex("[0-9]+")

            init {
                this.checkResult = check(input, size)
                this.udecString = checkResult.corrected
            }

            constructor(udecString: String) : this(udecString, Tools.getNearestUDecSize(udecString.trim().removePrefix(Settings.PRESTRING_UDECIMAL))) {
                if (DebugTools.KIT_showValCheckWarnings) {
                    println("UDec(): Calculated Size from $udecString as hex ${this.toHex().getRawHexStr()} -> ${size.bitWidth}")
                }
            }

            fun getUDecStr(): String = udecString
            fun getRawUDecStr(): String = udecString.removePrefix(Settings.PRESTRING_UDECIMAL)
            fun getUResized(size: Size): UDec = UDec(getRawUDecStr(), size)

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

            override fun check(size: Size): CheckResult = check(getRawUDecStr(), size)
            override fun toBin(): Bin = Conversion.getBinary(this)
            override fun toHex(): Hex = Conversion.getBinary(this).toHex()
            override fun toOct(): Oct = Conversion.getBinary(this).toOct()
            override fun toDec(): Dec = Conversion.getBinary(this).toDec()
            override fun toUDec(): UDec = this
            override fun toASCII(): String = Conversion.getASCII(this)
            fun toIntOrNull(): Int? = getRawUDecStr().toIntOrNull()
            fun toDoubleOrNull(): Double? = getRawUDecStr().toDoubleOrNull()
            override fun getBiggest(): Value = UDec(Bounds(size).umax, size)

            override fun plus(operand: Value): Value {
                val result = DecTools.add(this.getRawUDecStr(), operand.toUDec().getRawUDecStr())
                val biggerSize = if (this.size.bitWidth > operand.size.bitWidth) this.size else operand.size
                return UDec(result, biggerSize)
            }

            override fun minus(operand: Value): Value {
                val result = DecTools.sub(this.getRawUDecStr(), operand.toUDec().getRawUDecStr())
                val biggerSize = if (this.size.bitWidth > operand.size.bitWidth) this.size else operand.size
                return UDec(result, biggerSize)
            }

            override fun times(operand: Value): Value {
                val result = DecTools.multiply(this.getRawUDecStr(), operand.toUDec().getRawUDecStr())
                val biggerSize = if (this.size.bitWidth > operand.size.bitWidth) this.size else operand.size
                return UDec(result, biggerSize)
            }

            override fun div(operand: Value): Value {
                val divResult = DecTools.divide(this.getRawUDecStr(), operand.toUDec().getRawUDecStr())
                val biggerSize = if (this.size.bitWidth > operand.size.bitWidth) this.size else operand.size
                return UDec(divResult.result, biggerSize)
            }

            override fun rem(operand: Value): Value {
                val divResult = DecTools.divide(this.getRawUDecStr(), operand.toUDec().getRawUDecStr())
                val biggerSize = if (this.size.bitWidth > operand.size.bitWidth) this.size else operand.size
                return UDec(DecTools.checkEmpty(divResult.rest), biggerSize)
            }

            override fun unaryMinus(): Value {
                return -this.toDec()
            }

            override fun inc(): Value = Dec(DecTools.add(this.getRawUDecStr(), "1"), size)
            override fun dec(): Value = Dec(DecTools.sub(this.getRawUDecStr(), "1"), size)

            override fun compareTo(other: Value): Int = if (DecTools.isEqual(this.getRawUDecStr(), other.toUDec().getRawUDecStr())) {
                0
            } else if (DecTools.isGreaterThan(this.getRawUDecStr(), other.toUDec().getRawUDecStr())) {
                1
            } else {
                -1
            }

            override fun toRawString(): String = this.getRawUDecStr()
            override fun toString(): String = this.udecString
        }

        /**
         * Contains all implementations of type conversions, to switch between all Types of [Value].
         */
        object Conversion {
            fun getType(string: String): Value {
                var removedPrefString = string.trim().removePrefix(Settings.PRESTRING_BINARY)
                if (removedPrefString.length < string.trim().length - 1) {
                    return Bin("0", Size.Bit8())
                }
                removedPrefString = string.trim().removePrefix(Settings.PRESTRING_HEX)
                if (removedPrefString.length < string.trim().length - 1) {
                    return Hex("0", Size.Bit8())
                }
                removedPrefString = string.trim().removePrefix(Settings.PRESTRING_OCT)
                if (removedPrefString.length < string.trim().length - 1) {
                    return Oct("0", Size.Bit8())
                }
                removedPrefString = string.trim().removePrefix(Settings.PRESTRING_UDECIMAL)
                if (removedPrefString.length < string.trim().length - 1) {
                    return UDec("u0", Size.Bit8())
                }
                return Dec("0", Size.Bit8())
            }

            fun getHex(bin: Bin): Hex {
                var hexStr = ""

                var binStr = bin.getRawBinStr()
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
                    nativeInfo("Conversion: ${bin.getBinaryStr()} to $hexStr")
                }

                return Hex(hexStr, bin.size)
            }

            fun getOct(bin: Bin): Oct {
                var octStr = ""

                var binStr = bin.getRawBinStr()
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
                    nativeInfo("Conversion: ${bin.getBinaryStr()} to $octStr")
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

                val hexStr = hex.getRawHexStr().uppercase()

                for (i in hexStr.indices) {
                    binStr += BinaryTools.hexToBinDigit[hexStr[i]]
                }
                if (DebugTools.KIT_showValTypeConversionInfo) {
                    nativeInfo("Conversion: ${hex.getHexStr()} to $binStr")
                }
                return Bin(binStr, hex.size)
            }

            fun getBinary(dec: Dec): Bin {

                var decString = dec.getRawDecStr()

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
                    nativeWarn("Conversion.getBinary(dec: Dec) : error in calculation ${dec.getRawDecStr()} to $binaryStr")
                }

                if (dec.isNegative()) {
                    binaryStr = BinaryTools.inv(binaryStr)
                }

                if (DebugTools.KIT_showValTypeConversionInfo) {
                    nativeInfo("Conversion: ${dec.getDecStr()} to $binaryStr")
                }

                return Bin(binaryStr, dec.size)
            }

            fun getBinary(udec: UDec): Bin {

                var udecString = udec.getRawUDecStr()

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
                    nativeWarn("Conversion.getBinary(udec: UDec) : error in calculation ${udec.getRawUDecStr()} to $binaryStr")
                }

                if (DebugTools.KIT_showValTypeConversionInfo) {
                    nativeInfo("Conversion: ${udec.getUDecStr()} to $binaryStr")
                }

                return Bin(binaryStr, udec.size)
            }

            fun getDec(bin: Bin): Dec {
                var binString = bin.getRawBinStr()
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
                    nativeInfo("Conversion: ${bin.getBinaryStr()} to $decString")
                }

                return Dec(decString, bin.size)
            }

            fun getUDec(bin: Bin): UDec {
                val binString = bin.getRawBinStr()

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
                    nativeInfo("Conversion: ${bin.getBinaryStr()} to $udecString")
                }

                return UDec(udecString, bin.size)
            }

            fun getASCII(value: Value): String {
                val stringBuilder = StringBuilder()

                val hexString = when (value) {
                    is Hex -> value.getRawHexStr()
                    is Oct -> value.toHex().getRawHexStr()
                    is Bin -> value.toHex().getRawHexStr()
                    is Dec -> value.toHex().getRawHexStr()
                    is UDec -> value.toHex().getRawHexStr()
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
                    nativeInfo("Conversion: ${value.toHex().getHexStr()} to $stringBuilder")
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
    }

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
        class Bit1 : Size("1 Bit", 1)
        class Bit3 : Size("3 Bit", 3)
        class Bit4 : Size("4 Bit", 4)
        class Bit5 : Size("5 Bit", 5)
        class Bit6 : Size("6 Bit", 6)
        class Bit7 : Size("7 Bit", 7)
        class Bit8 : Size("8 Bit", 8)
        class Bit9 : Size("9 Bit", 9)
        class Bit12 : Size("12 Bit", 12)
        class Bit16 : Size("16 Bit", 16)
        class Bit20 : Size("20 Bit", 20)
        class Bit24 : Size("24 Bit", 24)
        class Bit28 : Size("28 Bit", 28)
        class Bit32 : Size("32 Bit", 32)
        class Bit40 : Size("40 Bit", 40)
        class Bit44 : Size("44 Bit", 44)
        class Bit48 : Size("48 Bit", 48)
        class Bit52 : Size("52 Bit", 52)
        class Bit56 : Size("56 Bit", 56)
        class Bit60 : Size("60 Bit", 60)
        class Bit64 : Size("64 Bit", 64)
        class Bit128 : Size("128 Bit", 128)
    }

    /**
     * Bounds are used to check if a specific [Value] is in it's [Bounds]. Sizes won't need to be defined.
     */
    class Bounds {

        val min: String
        val umin: String
        val max: String
        val umax: String

        constructor(size: Size) {
            when (size) {
                is Size.Bit1 -> {
                    this.min = "0"
                    this.max = "1"
                    this.umin = "0"
                    this.umax = "1"
                }

                is Size.Bit8 -> {
                    this.min = "-128"
                    this.max = "127"
                    this.umin = "0"
                    this.umax = "255"
                }

                is Size.Bit16 -> {
                    this.min = "-32768"
                    this.max = "32767"
                    this.umin = "0"
                    this.umax = "65535"
                }

                is Size.Bit32 -> {
                    this.min = "-2147483648"
                    this.max = "2147483647"
                    this.umin = "0"
                    this.umax = "4294967295"
                }

                is Size.Bit64 -> {
                    this.min = "-9223372036854775807"
                    this.max = "9223372036854775807"
                    this.umin = "0"
                    this.umax = "18446744073709551615"
                }

                is Size.Bit128 -> {
                    this.min = "-170141183460469231731687303715884105728"
                    this.max = "170141183460469231731687303715884105727"
                    this.umin = "0"
                    this.umax = "340282366920938463463374607431768211455"
                }

                is Size.Bit3 -> {
                    this.min = "-4"
                    this.max = "3"
                    this.umin = "0"
                    this.umax = "7"
                }

                is Size.Bit4 -> {
                    this.min = "-8"
                    this.max = "7"
                    this.umin = "0"
                    this.umax = "15"
                }

                is Size.Bit5 -> {
                    this.min = "-16"
                    this.max = "15"
                    this.umin = "0"
                    this.umax = "31"
                }

                is Size.Bit6 -> {
                    this.min = "-32"
                    this.max = "31"
                    this.umin = "0"
                    this.umax = "63"
                }

                is Size.Bit7 -> {
                    this.min = "-64"
                    this.max = "63"
                    this.umin = "0"
                    this.umax = "127"
                }

                is Size.Bit9 -> {
                    this.min = "-256"
                    this.max = "255"
                    this.umin = "0"
                    this.umax = "511"
                }

                is Size.Bit12 -> {
                    this.min = "-2048"
                    this.max = "2047"
                    this.umin = "0"
                    this.umax = "4095"
                }

                is Size.Bit20 -> {
                    this.min = "-524288"
                    this.max = "524287"
                    this.umin = "0"
                    this.umax = "1048575"
                }

                is Size.Bit24 -> {
                    this.min = "-8388608"
                    this.max = "8388607"
                    this.umin = "0"
                    this.umax = "16777215"
                }

                is Size.Bit28 -> {
                    this.min = "-124217728"
                    this.max = "124217727"
                    this.umin = "0"
                    this.umax = "268435456"
                }

                is Size.Bit40 -> {
                    this.min = "-549755813888"
                    this.max = "549755813887"
                    this.umin = "0"
                    this.umax = "1099511627776"
                }

                is Size.Bit44 -> {
                    this.min = "-17592186044416"
                    this.max = "17592186044415"
                    this.umin = "0"
                    this.umax = "17592186044415"
                }

                is Size.Bit48 -> {
                    this.min = "-140737488355328"
                    this.max = "140737488355327"
                    this.umin = "0"
                    this.umax = "281474976710655"
                }

                is Size.Bit52 -> {
                    this.min = "-2251799813685248"
                    this.max = "2251799813685247"
                    this.umin = "0"
                    this.umax = "4503599627370496"
                }

                is Size.Bit56 -> {
                    this.min = "-36028797018963968"
                    this.max = "36028797018963967"
                    this.umin = "0"
                    this.umax = "72057594037927935"
                }

                is Size.Bit60 -> {
                    this.min = "-576460752303423488"
                    this.max = "576460752303423487"
                    this.umin = "0"
                    this.umax = "1152921504606846975"
                }

                is Size.Original -> {
                    nativeError("Variable.Bounds: Can't get bounds from original Size Type! Use getNearestSize() or getNearestDecSize() first!")

                    this.min = "not identified"
                    this.max = "not identified"
                    this.umin = "0"
                    this.umax = "not identified"
                }


            }
        }

        constructor(min: String, max: String) {
            this.min = min
            this.max = max
            this.umin = "0"
            this.umax = DecTools.abs(DecTools.sub(min, max))
        }
    }

    /**
     * Contains all additional needed tools for working with [Value].
     */
    object Tools {

        fun Int.toValue(size: Size): Dec = Dec(this.toString(), size)

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

        fun Array<Hex>.mergeToChunks(currSize: Size, chunkSize: Size): Array<Hex>{
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
                    return Size.Bit8()
                }

                bitWidth <= 16 -> {
                    return Size.Bit16()
                }

                bitWidth <= 32 -> {
                    return Size.Bit32()
                }

                bitWidth <= 64 -> {
                    return Size.Bit64()
                }

                bitWidth <= 128 -> {
                    return Size.Bit128()
                }

                else -> {
                    nativeWarn("Bounds.getNearestSize(): $bitWidth is greater than possible maximum Size of 128bit -> returning Size.Bit128()")
                    return Size.Bit128()
                }
            }
        }

        fun getNearestDecSize(decString: String): Size {
            val string = decString.trim().removePrefix(Settings.PRESTRING_DECIMAL)
            when {
                DecTools.isGreaterEqualThan(Bounds(Size.Bit8()).max, string) && DecTools.isGreaterEqualThan(string, Bounds(Size.Bit8()).min) -> {
                    return Size.Bit8()
                }

                DecTools.isGreaterEqualThan(Bounds(Size.Bit16()).max, string) && DecTools.isGreaterEqualThan(string, Bounds(Size.Bit16()).min) -> {
                    return Size.Bit16()
                }

                DecTools.isGreaterEqualThan(Bounds(Size.Bit32()).max, string) && DecTools.isGreaterEqualThan(string, Bounds(Size.Bit32()).min) -> {
                    return Size.Bit32()
                }

                DecTools.isGreaterEqualThan(Bounds(Size.Bit64()).max, string) && DecTools.isGreaterEqualThan(string, Bounds(Size.Bit64()).min) -> {
                    return Size.Bit64()
                }

                DecTools.isGreaterEqualThan(Bounds(Size.Bit128()).max, string) && DecTools.isGreaterEqualThan(string, Bounds(Size.Bit128()).min) -> {
                    return Size.Bit128()
                }

                else -> {
                    nativeWarn("Bounds.getNearestDecSize(): $decString is not in Bounds of Size.Bit128() [max: ${Bounds(Size.Bit128()).max}, min: ${Bounds(Size.Bit128()).min}] -> returning Size.Bit128()")
                    return Size.Bit128()
                }
            }
        }

        fun getNearestUDecSize(udecString: String): Size {
            val string = udecString.trim().removePrefix(Settings.PRESTRING_UDECIMAL)
            when {
                DecTools.isGreaterEqualThan(Bounds(Size.Bit8()).umax, string) -> {
                    return Size.Bit8()
                }

                DecTools.isGreaterEqualThan(Bounds(Size.Bit16()).umax, string) -> {
                    return Size.Bit16()
                }

                DecTools.isGreaterEqualThan(Bounds(Size.Bit32()).umax, string) -> {
                    return Size.Bit32()
                }

                DecTools.isGreaterEqualThan(Bounds(Size.Bit64()).umax, string) -> {
                    return Size.Bit64()
                }

                DecTools.isGreaterEqualThan(Bounds(Size.Bit128()).umax, string) -> {
                    return Size.Bit128()
                }

                else -> {
                    nativeWarn("Bounds.getNearestDecSize(): $udecString is not in Bounds of Size.Bit128() [max: ${Bounds(Size.Bit128()).umax}, min: ${Bounds(Size.Bit128()).umin}] -> returning Size.Bit128()")
                    return Size.Bit128()
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
    data class AddResult(val result: Value.Bin, val carry: Boolean)
    data class SubResult(val result: Value.Bin, val borrow: Boolean)

}
