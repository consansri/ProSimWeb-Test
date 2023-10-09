package emulator.kit.types

import emulator.kit.Settings
import debug.DebugTools
import react.createContext
import kotlin.math.roundToInt


/**
 * [Variable] is the mutable version of [Value] which can contain several types all based on [String].
 * Each Variable has a fixed [size] which can't be changed. When a new value will be [set] it will automatically be resized to the former [size].
 * Operator Functions such as comparing functions are overwritten.
 */
class Variable {

    val initialBinary: String
    val size: Size
    private var value: Value

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
     *
     */
    sealed class Value(val input: String, val size: Size) {

        lateinit var checkResult: CheckResult

        /**
         * This function implements the [String] format checks, of each specific type.
         * This function can write errors and warnings such as applied changes to the console if any input isn't valid.
         */
        abstract fun check(string: String, size: Size): CheckResult
        abstract fun toBin(): Bin
        abstract fun toHex(): Hex
        abstract fun toDec(): Dec
        abstract fun toUDec(): UDec
        abstract fun toASCII(): String
        abstract fun toDouble(): Double
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
                    return BinaryTools.isEqual(this.toBin().getRawBinaryStr(), other.toBin().getRawBinaryStr())
                }
            }
            return super.equals(other)
        }
        abstract override fun toString(): String


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
                        message = "Bin.check(): ${string} is to long! Casted to TrimmedString(${trimmedString}) This value is layouted to hold up values with a bit width <= ${size.bitWidth}!"
                        console.warn(message)
                        CheckResult(false, trimmedString, message)
                    }
                } else {
                    val zeroString = Settings.PRESTRING_BINARY + "0".repeat(size.bitWidth)
                    message = "Bin.check(): ${string} does not match the binary Pattern (${Settings.PRESTRING_BINARY + "X".repeat(size.bitWidth)} where X is element of [0,1]), returning ${zeroString} instead!"
                    console.error(message)
                    return CheckResult(false, formattedInput, message)
                }
            }

            fun getRawBinaryStr(): String {
                val binStr = binString.removePrefix(Settings.PRESTRING_BINARY)
                return binStr
            }

            fun getBinaryStr(): String {
                return binString
            }

            fun getUResized(size: Size): Bin {
                return Bin(getRawBinaryStr(), size)
            }

            fun getResized(size: Size): Bin {
                val paddedBinString = getRawBinaryStr().padStart(size.bitWidth, if (getRawBinaryStr().first() == '1') '1' else '0')
                return Bin(paddedBinString, size)
            }

            override fun toHex(): Hex {
                return Conversion.getHex(this)
            }

            override fun toBin(): Bin {
                return this
            }

            override fun toDec(): Dec {
                return Conversion.getDec(this)
            }

            override fun toUDec(): UDec {
                return Conversion.getUDec(this)
            }

            override fun toASCII(): String {
                return Conversion.getASCII(this)
            }

            override fun toDouble(): Double {
                return this.toDec().toDouble()
            }

            override fun getBiggest(): Value {
                return Bin("1".repeat(size.bitWidth), size)
            }

            override fun plus(operand: Value): Value {
                val result = BinaryTools.add(this.getRawBinaryStr(), operand.toBin().getRawBinaryStr())
                val biggerSize = if (this.size.bitWidth > operand.size.bitWidth) this.size else operand.size
                return Bin(result, biggerSize)
            }

            override fun minus(operand: Value): Value {
                val result = BinaryTools.sub(this.getRawBinaryStr(), operand.toBin().getRawBinaryStr())
                val biggerSize = if (this.size.bitWidth > operand.size.bitWidth) this.size else operand.size
                return Bin(result, biggerSize)
            }

            override fun times(operand: Value): Value {
                val result = BinaryTools.multiply(this.getRawBinaryStr(), operand.toBin().getRawBinaryStr())
                val biggerSize = if (this.size.bitWidth > operand.size.bitWidth) this.size else operand.size
                return Bin(result, biggerSize)
            }

            override fun div(operand: Value): Value {
                val divResult = BinaryTools.divide(this.getRawBinaryStr(), operand.toBin().getRawBinaryStr())
                val biggerSize = if (this.size.bitWidth > operand.size.bitWidth) this.size else operand.size
                return Bin(divResult.result, biggerSize)
            }

            override fun rem(operand: Value): Value {
                val divResult = BinaryTools.divide(this.getRawBinaryStr(), operand.toBin().getRawBinaryStr())
                val biggerSize = if (this.size.bitWidth > operand.size.bitWidth) this.size else operand.size
                return Bin(BinaryTools.checkEmpty(divResult.rest), biggerSize)
            }

            override fun unaryMinus(): Value {
                return Bin(BinaryTools.negotiate(this.getRawBinaryStr()), size)
            }

            override fun inc(): Value {
                return Bin(BinaryTools.add(this.getRawBinaryStr(), "1"), size)
            }

            override fun dec(): Value {
                return Bin(BinaryTools.sub(this.getRawBinaryStr(), "1"), size)
            }

            override fun compareTo(other: Value): Int {
                if (BinaryTools.isEqual(getRawBinaryStr(), other.toBin().getRawBinaryStr())) {
                    return 0
                } else if (BinaryTools.isGreaterThan(getRawBinaryStr(), other.toBin().getRawBinaryStr())) {
                    return 1
                } else {
                    return -1
                }
            }

            override fun toString(): String {
                return this.binString
            }

            infix fun shl(bitCount: Int): Bin {
                val shiftedBinary = getRawBinaryStr().substring(bitCount).padEnd(size.bitWidth, '0')
                return Bin(shiftedBinary, size)
            }

            infix fun ushl(bitCount: Int): Bin {
                val shiftedBinary = getRawBinaryStr().substring(bitCount).padEnd(size.bitWidth, '0')
                return Bin(shiftedBinary, size)
            }

            infix fun shr(bitCount: Int): Bin {
                val shiftedBinary = getRawBinaryStr().substring(0, size.bitWidth - bitCount).padStart(size.bitWidth, if (getRawBinaryStr().first() == '1') '1' else '0')
                return Bin(shiftedBinary, size)
            }

            infix fun ushr(bitCount: Int): Bin {
                val shiftedBinary = getRawBinaryStr().substring(0, size.bitWidth - bitCount).padStart(size.bitWidth, '0')
                return Bin(shiftedBinary, size)
            }

            infix fun xor(bin2: Bin): Bin {
                val biggestSize = if (size.bitWidth >= bin2.size.bitWidth) size else bin2.size
                return Bin(BinaryTools.xor(getRawBinaryStr(), bin2.getRawBinaryStr()), biggestSize)
            }

            infix fun or(bin2: Bin): Bin {
                val biggestSize = if (size.bitWidth >= bin2.size.bitWidth) size else bin2.size
                return Bin(BinaryTools.or(getRawBinaryStr(), bin2.getRawBinaryStr()), biggestSize)
            }

            infix fun and(bin2: Bin): Bin {
                val biggestSize = if (size.bitWidth >= bin2.size.bitWidth) size else bin2.size
                return Bin(BinaryTools.and(getRawBinaryStr(), bin2.getRawBinaryStr()), biggestSize)
            }

            operator fun inv(): Bin {
                return Bin(BinaryTools.inv(getRawBinaryStr()), size)
            }


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

            fun getRawHexStr(): String {
                return hexString.removePrefix(Settings.PRESTRING_HEX)
            }

            fun getHexStr(): String {
                return hexString
            }

            fun getUResized(size: Size): Hex {
                return Hex(getRawHexStr(), size)
            }

            override fun check(string: String, size: Size): CheckResult {
                var formatted = string.trim().removePrefix(Settings.PRESTRING_HEX).padStart(size.bitWidth / 4, '0').uppercase()
                val message: String
                if (regex.matches(formatted)) {
                    if (formatted.length <= size.bitWidth / 4) {
                        formatted = formatted.padStart(size.bitWidth / 4, '0')
                        return CheckResult(true, Settings.PRESTRING_HEX + formatted)
                    } else {
                        val trimmedString = formatted.substring(formatted.length - size.bitWidth / 4)
                        message = "Hex.check(): ${string} is to long! Casted to TrimmedString(${trimmedString}) This value is layouted to hold up values with width <= ${size.bitWidth / 4}!"
                        console.warn(message)
                        return CheckResult(false, Settings.PRESTRING_HEX + trimmedString, message)
                    }
                } else {
                    val zeroString = Settings.PRESTRING_HEX + "0".repeat(size.bitWidth / 4)
                    message = "Hex.check(): ${string} does not match the hex Pattern (${Settings.PRESTRING_HEX + "X".repeat(size.bitWidth / 4)} where X is element of [0-9,A-F]), returning ${zeroString} instead!"
                    console.error(message)
                    return CheckResult(false, zeroString, message)
                }
            }

            override fun toBin(): Bin {
                return Conversion.getBinary(this)
            }

            override fun toDec(): Dec {
                return Conversion.getDec(this.toBin())
            }

            override fun toUDec(): UDec {
                return Conversion.getUDec(this.toBin())
            }

            override fun toASCII(): String {
                return Conversion.getASCII(this)
            }

            override fun toDouble(): Double {
                return this.toDec().toDouble()
            }

            override fun getBiggest(): Value {
                return Hex("F".repeat(size.bitWidth / 4), size)
            }

            override fun plus(operand: Value): Value {
                val result = BinaryTools.add(this.toBin().getRawBinaryStr(), operand.toBin().getRawBinaryStr())
                val biggerSize = if (this.size.bitWidth > operand.size.bitWidth) this.size else operand.size
                return Bin(result, biggerSize).toHex()
            }

            override fun minus(operand: Value): Value {
                val result = BinaryTools.sub(this.toBin().getRawBinaryStr(), operand.toBin().getRawBinaryStr())
                val biggerSize = if (this.size.bitWidth > operand.size.bitWidth) this.size else operand.size
                return Bin(result, biggerSize).toHex()
            }

            override fun times(operand: Value): Value {
                val result = BinaryTools.multiply(this.toBin().getRawBinaryStr(), operand.toBin().getRawBinaryStr())
                val biggerSize = if (this.size.bitWidth > operand.size.bitWidth) this.size else operand.size
                return Bin(result, biggerSize).toHex()
            }

            override fun div(operand: Value): Value {
                val divResult = BinaryTools.divide(this.toBin().getRawBinaryStr(), operand.toBin().getRawBinaryStr())
                val biggerSize = if (this.size.bitWidth > operand.size.bitWidth) this.size else operand.size
                return Bin(divResult.result, biggerSize).toHex()
            }

            override fun rem(operand: Value): Value {
                val divResult = BinaryTools.divide(this.toBin().getRawBinaryStr(), operand.toBin().getRawBinaryStr())
                val biggerSize = if (this.size.bitWidth > operand.size.bitWidth) this.size else operand.size
                return Bin(BinaryTools.checkEmpty(divResult.rest), biggerSize).toHex()
            }

            override fun unaryMinus(): Value {
                return Bin(BinaryTools.negotiate(this.toBin().getRawBinaryStr()), size).toHex()
            }

            override fun inc(): Value {
                return Bin(BinaryTools.add(this.toBin().getRawBinaryStr(), "1"), size).toHex()
            }

            override fun dec(): Value {
                return Bin(BinaryTools.sub(this.toBin().getRawBinaryStr(), "1"), size).toHex()
            }

            override fun compareTo(other: Value): Int {
                if (BinaryTools.isEqual(this.toBin().getRawBinaryStr(), other.toBin().getRawBinaryStr())) {
                    return 0
                } else if (BinaryTools.isGreaterThan(this.toBin().getRawBinaryStr(), other.toBin().getRawBinaryStr())) {
                    return 1
                } else {
                    return -1
                }
            }

            override fun toString(): String {
                return this.hexString
            }

            override fun toHex(): Hex {
                return this
            }
        }

        /**
         * Provides the decimal representation of [Value].
         */
        class Dec(decString: String, size: Size) : Value(decString, size) {
            private val decString: String
            private val negative: Boolean
            val posRegex = Regex("[0-9]+")

            init {
                this.checkResult = check(input, size)
                this.decString = checkResult.corrected
                this.negative = DecTools.isNegative(checkResult.corrected)
            }

            constructor(decString: String) : this(decString, Tools.getNearestDecSize(decString.trim().removePrefix(Settings.PRESTRING_DECIMAL)))

            fun isNegative(): Boolean {
                return negative
            }

            fun getRawDecStr(): String {
                return decString.removePrefix(Settings.PRESTRING_DECIMAL)
            }

            fun getDecStr(): String {
                return decString
            }

            fun getResized(size: Size): Dec {
                return Dec(getRawDecStr(), size)
            }

            override fun check(string: String, size: Size): CheckResult {
                val formatted = string.trim().removePrefix(Settings.PRESTRING_DECIMAL)
                val message: String
                if (!posRegex.matches(formatted.replace("-", ""))) {
                    val zeroString = "0"
                    message = "Dec.check(): ${formatted} does not match the dec Pattern (${Settings.PRESTRING_DECIMAL + "(-)" + "X".repeat(size.bitWidth)} where X is element of [0-9]), returning ${zeroString} instead!"
                    console.error(message)
                    return CheckResult(false, Settings.PRESTRING_DECIMAL + zeroString, message)
                } else {
                    if (DecTools.isGreaterThan(formatted, Bounds(size).max)) {
                        message = "Dec.check(): ${formatted} must be smaller equal ${Bounds(size).max} -> setting ${Bounds(size).max}"
                        console.warn(message)
                        return CheckResult(false, Settings.PRESTRING_DECIMAL + Bounds(size).max, message)
                    } else if (DecTools.isGreaterThan(Bounds(size).min, formatted)) {
                        message = "Dec.check(): ${formatted} must be bigger equal ${Bounds(size).min} -> setting ${Bounds(size).min}"
                        console.warn(message)
                        return CheckResult(false, Settings.PRESTRING_DECIMAL + Bounds(size).min, message)
                    } else {
                        return CheckResult(true, Settings.PRESTRING_DECIMAL + formatted)
                    }
                }
            }

            override fun toBin(): Bin {
                return Conversion.getBinary(this)
            }

            override fun toHex(): Hex {
                return Conversion.getBinary(this).toHex()
            }

            override fun toDec(): Dec {
                return this
            }

            override fun toUDec(): UDec {
                return Conversion.getUDec(this.toBin())
            }

            override fun toASCII(): String {
                return Conversion.getBinary(this).toASCII()
            }

            override fun toDouble(): Double {
                try {
                    return getRawDecStr().toDouble()
                } catch (e: NumberFormatException) {
                    console.warn("Value.toDouble(): NumberFormatException (dec: ${getRawDecStr()}) -> returning 0")
                    return 0.0
                }
            }

            override fun getBiggest(): Value {
                return Dec(Bounds(size).max, size)
            }

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

            override fun unaryMinus(): Value {
                return Dec(DecTools.negotiate(this.getRawDecStr()), size)
            }

            override fun inc(): Value {
                return Dec(DecTools.add(this.getRawDecStr(), "1"), size)
            }

            override fun dec(): Value {
                return Dec(DecTools.sub(this.getRawDecStr(), "1"), size)
            }

            override fun compareTo(other: Value): Int {
                if (DecTools.isEqual(this.getRawDecStr(), other.toDec().getRawDecStr())) {
                    return 0
                } else if (DecTools.isGreaterThan(this.getRawDecStr(), other.toDec().getRawDecStr())) {
                    return 1
                } else {
                    return -1
                }
            }

            override fun toString(): String {
                return this.decString
            }

        }

        /**
         * Provides the unsigned decimal representation of [Value].
         */
        class UDec(udecString: String, size: Size) : Value(udecString, size) {
            private val udecString: String
            val posRegex = Regex("[0-9]+")

            init {
                this.checkResult = check(input, size)
                this.udecString = checkResult.corrected
            }

            constructor(udecString: String) : this(udecString, Tools.getNearestUDecSize(udecString.trim().removePrefix(Settings.PRESTRING_UDECIMAL))) {
                if (DebugTools.KIT_showValCheckWarnings) {
                    console.log("UDec(): Calculated Size from $udecString as hex ${this.toHex().getRawHexStr()} -> ${size.bitWidth}")
                }
            }

            fun getUDecStr(): String {
                return udecString
            }

            fun getRawUDecStr(): String {
                return udecString.removePrefix(Settings.PRESTRING_UDECIMAL)
            }

            fun getUResized(size: Size): UDec {
                return UDec(getRawUDecStr(), size)
            }

            override fun check(string: String, size: Size): CheckResult {
                val formatted = string.trim().removePrefix(Settings.PRESTRING_UDECIMAL)
                val message: String
                if (!posRegex.matches(formatted)) {
                    val zeroString = "0"
                    message = "UDec.check(): ${formatted} does not match the udec Pattern (${Settings.PRESTRING_UDECIMAL + "X".repeat(size.bitWidth)} where X is element of [0-9]), returning ${zeroString} instead!"
                    console.error(message)
                    return CheckResult(false, Settings.PRESTRING_UDECIMAL + zeroString, message)
                } else {
                    if (DecTools.isGreaterThan(formatted, Bounds(size).umax)) {
                        message = "UDec.check(): ${formatted} must be smaller equal ${Bounds(size).umax} -> setting ${Bounds(size).umax}"
                        console.warn(message)
                        return CheckResult(false, Settings.PRESTRING_UDECIMAL + Bounds(size).umax, message)
                    } else if (DecTools.isGreaterThan(Bounds(size).umin, formatted)) {
                        message = "UDec.check(): ${formatted} must be bigger equal ${Bounds(size).umin} -> setting ${Bounds(size).umin}"
                        console.warn(message)
                        return CheckResult(false, Settings.PRESTRING_UDECIMAL + Bounds(size).umin, message)
                    } else {
                        return CheckResult(true, Settings.PRESTRING_UDECIMAL + formatted)
                    }
                }
            }

            override fun toBin(): Bin {
                return Conversion.getBinary(this)
            }

            override fun toHex(): Hex {
                return Conversion.getBinary(this).toHex()
            }

            override fun toDec(): Dec {
                return Conversion.getBinary(this).toDec()
            }

            override fun toUDec(): UDec {
                return this
            }

            override fun toASCII(): String {
                return Conversion.getASCII(this)
            }

            override fun toDouble(): Double {
                try {
                    return getRawUDecStr().toDouble()
                } catch (e: NumberFormatException) {
                    console.warn("Value.toDouble(): NumberFormatException (udec: ${getRawUDecStr()}) -> returning 0")
                    return 0.0
                }
            }

            override fun getBiggest(): Value {
                return UDec(Bounds(size).umax, size)
            }

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
                console.warn("Value.UDec: Executing unaryMinus on unsigned decimal! -> no affect!")
                return UDec(this.getRawUDecStr(), size)
            }

            override fun inc(): Value {
                return Dec(DecTools.add(this.getRawUDecStr(), "1"), size)
            }

            override fun dec(): Value {
                return Dec(DecTools.sub(this.getRawUDecStr(), "1"), size)
            }

            override fun compareTo(other: Value): Int {
                if (DecTools.isEqual(this.getRawUDecStr(), other.toUDec().getRawUDecStr())) {
                    return 0
                } else if (DecTools.isGreaterThan(this.getRawUDecStr(), other.toUDec().getRawUDecStr())) {
                    return 1
                } else {
                    return -1
                }
            }

            override fun toString(): String {
                return this.udecString
            }
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
                removedPrefString = string.trim().removePrefix(Settings.PRESTRING_UDECIMAL)
                if (removedPrefString.length < string.trim().length - 1) {
                    return UDec("u0", Size.Bit8())
                }
                return Dec("0", Size.Bit8())
            }

            fun getHex(bin: Bin): Hex {
                val stringBuilder = StringBuilder()

                val binStr = bin.getRawBinaryStr()

                for (i in binStr.indices step 4) {
                    val substring = binStr.substring(i, i + 4)
                    val int = substring.toInt(2)
                    stringBuilder.append(int.toString(16).uppercase())
                }

                if (DebugTools.KIT_showValTypeConversionInfo) {
                    console.info("Conversion: ${bin.getBinaryStr()} to ${stringBuilder}")
                }

                return Hex(stringBuilder.toString(), bin.size)
            }

            fun getBinary(hex: Hex): Bin {
                val stringBuilder = StringBuilder()

                val hexStr = hex.getRawHexStr()

                for (i in hexStr.indices) {
                    val hexDigit = hexStr[i].digitToInt(16)
                    stringBuilder.append(hexDigit.toString(2).padStart(4, '0'))
                }
                if (DebugTools.KIT_showValTypeConversionInfo) {
                    console.info("Conversion: ${hex.getHexStr()} to ${stringBuilder}")
                }
                return Bin(stringBuilder.toString(), hex.size)
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
                        binaryStr = binaryStr + "1"
                        decString = DecTools.sub(decString, weight)
                    } else {
                        binaryStr = binaryStr + "0"
                    }
                }

                if (binaryStr == "") {
                    console.warn("Conversion.getBinary(dec: Dec) : error in calculation ${dec.getRawDecStr()} to ${binaryStr}")
                }

                if (dec.isNegative()) {
                    binaryStr = BinaryTools.inv(binaryStr)
                }

                if (DebugTools.KIT_showValTypeConversionInfo) {
                    console.info("Conversion: ${dec.getDecStr()} to ${binaryStr}")
                }

                return Bin(binaryStr, dec.size)
            }

            fun getBinary(udec: UDec): Bin {

                var udecString = udec.getRawUDecStr()

                var binaryStr = ""

                for (i in udec.size.bitWidth - 1 downTo 0) {
                    val weight = DecTools.binaryWeights[i].weight
                    if (DecTools.isGreaterEqualThan(udecString, weight)) {
                        binaryStr = binaryStr + "1"
                        udecString = DecTools.sub(udecString, weight)
                    } else {
                        binaryStr = binaryStr + "0"
                    }
                }

                if (binaryStr == "") {
                    console.warn("Conversion.getBinary(udec: UDec) : error in calculation ${udec.getRawUDecStr()} to ${binaryStr}")
                }

                if (DebugTools.KIT_showValTypeConversionInfo) {
                    console.info("Conversion: ${udec.getUDecStr()} to ${binaryStr}")
                }

                return Bin(binaryStr, udec.size)
            }

            fun getDec(bin: Bin): Dec {
                var binString = bin.getRawBinaryStr()
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

                for (weight in binString.length - 1 downTo 0) {
                    val index = binString.length - 1 - weight
                    val summand = DecTools.binaryWeights[weight].weight
                    if (binString[index] == '1') {
                        decString = DecTools.add(decString, summand)
                    }
                }

                if (negative) {
                    decString = DecTools.negotiate(decString)
                }

                if (DebugTools.KIT_showValTypeConversionInfo) {
                    console.info("Conversion: ${bin.getBinaryStr()} to ${decString}")
                }

                return Dec(decString, bin.size)
            }

            fun getUDec(bin: Bin): UDec {
                val binString = bin.getRawBinaryStr()

                var udecString = "0"
                if (binString.length > 0) {
                    val absBin: String = binString

                    for (i in absBin.indices) {
                        val index = absBin.length - 1 - i
                        val add = DecTools.pow("2", i.toString(10))
                        if (absBin[index] == '1') {
                            udecString = DecTools.add(udecString, add)
                        }
                    }
                }
                if (DebugTools.KIT_showValTypeConversionInfo) {
                    console.info("Conversion: ${bin.getBinaryStr()} to ${udecString}")
                }

                return UDec(udecString, bin.size)
            }

            fun getASCII(value: Value): String {
                val stringBuilder = StringBuilder()

                val hexString = when (value) {
                    is Hex -> value.getRawHexStr()
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
                    console.info("Conversion: ${value.toHex().getHexStr()} to ${stringBuilder}")
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

        override fun equals(other: Any?): Boolean {
            when (other) {
                is Size -> {
                    return this.bitWidth == other.bitWidth
                }
            }
            return super.equals(other)
        }

        fun getByteCount(): Int {
            return (bitWidth.toFloat() / 8.0f).roundToInt()
        }

        class Original(bitWidth: Int) : Size("original", bitWidth)
        class Bit1 : Size("1 Bit", 1)
        class Bit3 : Size("3 Bit", 3)
        class Bit5 : Size("5 Bit", 5)
        class Bit7 : Size("7 Bit", 7)
        class Bit8 : Size("8 Bit", 8)
        class Bit12 : Size("12 Bit", 12)
        class Bit16 : Size("16 Bit", 16)
        class Bit20 : Size("20 Bit", 20)
        class Bit32 : Size("32 Bit", 32)
        class Bit64 : Size("64 Bit", 64)
        class Bit128 : Size("128 Bit", 128)
    }

    /**
     * Bounds are used to check if a specific [Value] is in it's [Bounds]. Sizes won't need to be defined.
     * throws an [console].[error] if the [Bounds] of a specific [Size] isn't defined.
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

                is Size.Bit5 -> {
                    this.min = "-16"
                    this.max = "15"
                    this.umin = "0"
                    this.umax = "31"
                }

                is Size.Bit7 -> {
                    this.min = "-64"
                    this.max = "63"
                    this.umin = "0"
                    this.umax = "127"
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

                is Size.Original -> {
                    console.error("Variable.Bounds: Can't get bounds from original Size Type! Use getNearestSize() or getNearestDecSize() first!")

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
                    console.warn("Bounds.getNearestSize(): $bitWidth is greater than possible maximum Size of 128bit -> returning Size.Bit128()")
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
                    console.warn("Bounds.getNearestDecSize(): $decString is not in Bounds of Size.Bit128() [max: ${Bounds(Size.Bit128()).max}, min: ${Bounds(Size.Bit128()).min}] -> returning Size.Bit128()")
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
                    console.warn("Bounds.getNearestDecSize(): $udecString is not in Bounds of Size.Bit128() [max: ${Bounds(Size.Bit128()).umax}, min: ${Bounds(Size.Bit128()).umin}] -> returning Size.Bit128()")
                    return Size.Bit128()
                }
            }
        }

        fun asciiToHex(asciiString: String): String {
            val hexBuilder = StringBuilder()

            for (char in asciiString) {
                val hexValue = char.code.toString(16)
                hexBuilder.append(hexValue)
            }

            return hexBuilder.toString()
        }

    }

    data class CheckResult(val valid: Boolean, val corrected: String, val message: String = "")

}
