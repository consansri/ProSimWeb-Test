package extendable.components.types

import extendable.ArchConst
import tools.DebugTools
import kotlin.math.abs
import kotlin.math.absoluteValue
import kotlin.math.pow

class ByteValue {

    val initialBinary: String
    val size: Size
    private var value: Type

    constructor(initialBinary: String, byteCount: Int) {
        this.initialBinary = initialBinary
        when (byteCount) {
            1 -> {
                size = Size.Byte()
            }

            2 -> {
                size = Size.Short()
            }

            in 3..4 -> {
                size = Size.Int()
            }

            in 5..8 -> {
                size = Size.Long()
            }

            else -> {
                size = Size.Long()
                console.warn("Binary: $byteCount is no valid ByteCount (Maximum 8 Bytes) (Size is set to 8 Bytes)")
            }
        }
        value = Type.Binary(initialBinary, size)
    }

    constructor(type: Type) {
        this.value = type
        this.size = type.size
        this.initialBinary = type.toBinary().getBinaryStr()
    }


    /* GETTER SETTER */
    fun get(): Type {
        return value
    }

    fun getBounds(): Bounds {
        return Bounds(size)
    }

    fun setHex(hexString: String) {
        value = Type.Hex(hexString, size)
    }

    fun setDec(decString: String) {
        value = Type.Dec(decString, size)
    }

    fun setUDec(udecString: String) {
        value = Type.UDec(udecString, size)
    }

    fun setBin(binString: String) {
        value = Type.Binary(binString, size)
    }

    fun clear() {
        value = Type.Binary(initialBinary, size)
    }

    enum class Interpretation {
        BINARY,
        HEX,
        SIGNEDDEC,
        UNSIGNEDDEC,
        ASCII
    }

    sealed class Type(val size: Size) {

        abstract fun check(string: String, size: Size): CheckResult
        abstract fun toBinary(): Binary
        abstract fun toHex(): Hex
        abstract fun toDec(): Dec
        abstract fun toUDec(): UDec
        abstract fun toASCII(): String

        class Binary(binString: String, size: Size) : Type(size) {
            private val binString: String

            constructor(size: Size) : this(ArchConst.PRESTRING_BINARY + "0", size)

            init {
                this.binString = check(binString, size).corrected
            }

            override fun check(string: String, size: Size): CheckResult {
                val regexWithPreString = Regex("0b[0-1]+")
                val regex = Regex("[0-1]+")

                val formattedInput = ArchConst.PRESTRING_BINARY + string.trim().removePrefix(ArchConst.PRESTRING_BINARY).padStart(size.bitWidth, '0')

                if (regexWithPreString.matches(formattedInput)) {
                    // bin string with prestring
                    return if (formattedInput.length <= size.bitWidth + 2) {
                        if (string.length < size.bitWidth) {
                            CheckResult(true, ArchConst.PRESTRING_BINARY + formattedInput.removePrefix(ArchConst.PRESTRING_BINARY).padStart(size.bitWidth, '0'))
                        } else {
                            CheckResult(true, formattedInput)
                        }
                    } else {
                        val trimmedString = formattedInput.removePrefix(ArchConst.PRESTRING_BINARY).substring(formattedInput.length - 2 - size.bitWidth)
                        console.warn("ByteValue.Type.Binary.check(): ${formattedInput} is to long! Casted to TrimmedString(${trimmedString}) This value is layouted to hold up values with a bit width <= ${size.bitWidth}!")
                        CheckResult(false, trimmedString)
                    }
                } else if (regex.matches(formattedInput)) {
                    // bin string without prestring
                    return if (string.length <= size.bitWidth) {
                        if (string.length < size.bitWidth) {
                            CheckResult(true, formattedInput.padStart(size.bitWidth, '0'))
                        } else {
                            CheckResult(true, formattedInput)
                        }
                    } else {
                        val trimmedString = formattedInput.substring(formattedInput.length - size.bitWidth)
                        console.warn("ByteValue.Type.Binary.check(): ${formattedInput} is to long! Casted to TrimmedString(${trimmedString}) This value is layouted to hold up values with a bit width <= ${size.bitWidth}!")
                        CheckResult(false, trimmedString)
                    }
                } else {
                    val zeroString = ArchConst.PRESTRING_BINARY + "0".repeat(size.bitWidth)
                    console.warn("ByteValue.Type.Binary.check(): ${formattedInput} does not match the binary Pattern (${ArchConst.PRESTRING_BINARY + "X".repeat(size.bitWidth)} where X is element of [0,1]), returning ${zeroString} instead!")
                    return CheckResult(false, formattedInput)
                }
            }

            fun getRawBinaryStr(): String {
                val binStr = binString.removePrefix(ArchConst.PRESTRING_BINARY)
                return binStr
            }

            fun getBinaryStr(): String {
                return binString
            }

            override fun toHex(): Hex {
                return Conversion.getHex(this)
            }


            override fun toBinary(): Binary {
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


        }

        class Hex(hexString: String, size: Size) : Type(size) {
            private val hexString: String

            init {
                this.hexString = check(hexString, size).corrected
            }

            fun getRawHexStr(): String {
                return hexString.removePrefix(ArchConst.PRESTRING_HEX)
            }

            fun getHexStr(): String {
                return hexString
            }

            override fun check(string: String, size: Size): CheckResult {
                var formatted = string.trim().removePrefix(ArchConst.PRESTRING_HEX).padStart(size.byteCount * 2, '0').uppercase()

                val regex = Regex("[0-9A-Fa-f]+")

                if (regex.matches(formatted)) {
                    if (formatted.length <= size.byteCount * 2) {
                        formatted = formatted.padStart(size.byteCount * 2, '0')
                        return CheckResult(true, ArchConst.PRESTRING_HEX + formatted)
                    } else {
                        val trimmedString = formatted.substring(formatted.length - size.byteCount * 2)
                        console.warn("ByteValue.Type.Hex.check(): ${formatted} is to long! Casted to TrimmedString(${trimmedString}) This value is layouted to hold up values with width <= ${size.byteCount * 2}!")
                        return CheckResult(false, ArchConst.PRESTRING_HEX + trimmedString)
                    }
                } else {
                    val zeroString = ArchConst.PRESTRING_HEX + "0".repeat(size.bitWidth)
                    console.warn("ByteValue.Type.Hex.check(): ${formatted} does not match the hex Pattern (${ArchConst.PRESTRING_HEX + "X".repeat(size.byteCount * 2)} where X is element of [0-9,A-F]), returning ${zeroString} instead!")
                    return CheckResult(false, zeroString)
                }
            }

            override fun toBinary(): Binary {
                return Conversion.getBinary(this)
            }

            override fun toDec(): Dec {
                return Conversion.getDec(this.toBinary())
            }

            override fun toUDec(): UDec {
                return Conversion.getUDec(this.toBinary())
            }

            override fun toASCII(): String {
                return Conversion.getASCII(this)
            }

            override fun toHex(): Hex {
                return this
            }
        }

        class Dec(decString: String, size: Size) : Type(size) {
            private val decString: String
            private val negative: Boolean

            init {
                this.decString = check(decString, size).corrected
                this.negative = DecTools.isNegative(this.decString)
            }

            fun isNegative(): Boolean {
                return negative
            }

            fun getRawDecStr(): String {
                return decString.removePrefix(ArchConst.PRESTRING_DECIMAL)
            }

            fun getDecStr(): String {
                return decString
            }

            override fun check(string: String, size: Size): CheckResult {
                val formatted = string.trim().removePrefix(ArchConst.PRESTRING_DECIMAL)

                val posRegex = Regex("[0-9]+")

                if (!posRegex.matches(formatted.replace("-", ""))) {
                    val zeroString = "0"
                    console.warn("ByteValue.Type.Dec.check(): ${formatted} does not match the dec Pattern (${ArchConst.PRESTRING_DECIMAL + "(-)" + "X".repeat(size.bitWidth)} where X is element of [0-9]), returning ${zeroString} instead!")
                    return CheckResult(false, ArchConst.PRESTRING_DECIMAL + zeroString)
                } else {
                    if (DecTools.isGreaterThan(formatted, Bounds(size).max)) {
                        console.warn("ByteValue.Type.Dec.check(): ${formatted} must be smaller equal ${Bounds(size).max} -> setting ${Bounds(size).max}")
                        return CheckResult(false, ArchConst.PRESTRING_DECIMAL + Bounds(size).max)
                    } else if (DecTools.isGreaterThan(Bounds(size).min, formatted)) {
                        console.warn("ByteValue.Type.Dec.check(): ${formatted} must be bigger equal ${Bounds(size).min} -> setting ${Bounds(size).min}")
                        return CheckResult(false, ArchConst.PRESTRING_DECIMAL + Bounds(size).min)
                    } else {
                        return CheckResult(true, ArchConst.PRESTRING_DECIMAL + formatted)
                    }
                }
            }

            override fun toBinary(): Binary {
                return Conversion.getBinary(this)
            }

            override fun toHex(): Hex {
                return Conversion.getBinary(this).toHex()
            }

            override fun toDec(): Dec {
                return this
            }

            override fun toUDec(): UDec {
                return Conversion.getUDec(this.toBinary())
            }

            override fun toASCII(): String {
                return Conversion.getBinary(this).toASCII()
            }

        }

        class UDec(udecString: String, size: Size) : Type(size) {
            private val udecString: String

            init {
                this.udecString = check(udecString, size).corrected
            }

            fun getUDecStr(): String {
                return udecString
            }

            fun getRawUDecStr(): String {
                return udecString.removePrefix(ArchConst.PRESTRING_DECIMAL)
            }

            override fun check(string: String, size: Size): CheckResult {
                val formatted = string.trim().removePrefix(ArchConst.PRESTRING_DECIMAL)

                val posRegex = Regex("[0-9]+")

                if (!posRegex.matches(formatted)) {
                    val zeroString = "0"
                    console.warn("ByteValue.Type.UDec.check(): ${formatted} does not match the dec Pattern (${ArchConst.PRESTRING_DECIMAL + "X".repeat(size.bitWidth)} where X is element of [0-9]), returning ${zeroString} instead!")
                    return CheckResult(false, ArchConst.PRESTRING_DECIMAL + zeroString)
                } else {
                    if (DecTools.isGreaterThan(formatted, Bounds(size).umax)) {
                        console.warn("ByteValue.Type.UDec.check(): ${formatted} must be smaller equal ${Bounds(size).umax} -> setting ${Bounds(size).umax}")
                        return CheckResult(false, ArchConst.PRESTRING_DECIMAL + Bounds(size).umax)
                    } else if (DecTools.isGreaterThan(Bounds(size).umin, formatted)) {
                        console.warn("ByteValue.Type.UDec.check(): ${formatted} must be bigger equal ${Bounds(size).umin} -> setting ${Bounds(size).umin}")
                        return CheckResult(false, ArchConst.PRESTRING_DECIMAL + Bounds(size).umin)
                    } else {
                        return CheckResult(true, ArchConst.PRESTRING_DECIMAL + formatted)
                    }
                }
            }

            override fun toBinary(): Binary {
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
        }

        object Conversion {
            fun getHex(binary: Binary): Hex {
                val stringBuilder = StringBuilder()

                val binStr = binary.getRawBinaryStr()

                for (i in binStr.indices step 4) {
                    val substring = binStr.substring(i, i + 4)
                    val int = substring.toInt(2)
                    stringBuilder.append(int.toString(16).uppercase())
                }

                if (DebugTools.showTypeConversionInfo) {
                    console.info("Conversion: ${binary.getBinaryStr()} to ${stringBuilder}")
                }

                return Hex(stringBuilder.toString(), binary.size)
            }

            fun getBinary(hex: Hex): Binary {
                val stringBuilder = StringBuilder()

                val hexStr = hex.getRawHexStr()

                for (i in hexStr.indices) {
                    val hexDigit = hexStr[i].digitToInt(16)
                    stringBuilder.append(hexDigit.toString(2).padStart(4, '0'))
                }
                if (DebugTools.showTypeConversionInfo) {
                    console.info("Conversion: ${hex.getHexStr()} to ${stringBuilder}")
                }
                return Binary(stringBuilder.toString(), hex.size)
            }

            fun getBinary(dec: Dec): Binary {

                var decString = dec.getRawDecStr()

                if (dec.isNegative()) {
                    decString = DecTools.negotiate(decString)
                    decString = DecTools.sub(decString, "1")
                }

                var binaryStr = ""

                for (i in dec.size.bitWidth - 1 downTo 0) {
                    val weight = DecTools.pow("2", "$i")
                    if (DecTools.isGreaterEqualThan(decString, weight)) {
                        binaryStr = binaryStr + "1"
                        decString = DecTools.sub(decString, weight)
                    } else {
                        binaryStr = binaryStr + "0"
                    }
                }

                if (binaryStr == "") {
                    console.warn("ByteValue.Type.Conversion.getBinary(dec: Dec) : error in calculation ${dec.getRawDecStr()} to ${binaryStr}")
                }

                if (dec.isNegative()) {
                    binaryStr = BinaryTools.inv(binaryStr)
                }

                if (DebugTools.showTypeConversionInfo) {
                    console.info("Conversion: ${dec.getDecStr()} to ${binaryStr}")
                }

                return Binary(binaryStr, dec.size)
            }

            fun getBinary(udec: UDec): Binary {

                var udecString = udec.getRawUDecStr()

                var binaryStr = ""

                for (i in udec.size.bitWidth - 1 downTo 0) {
                    val weight = DecTools.pow("2", "$i")
                    if (DecTools.isGreaterEqualThan(udecString, weight)) {
                        binaryStr = binaryStr + "1"
                        udecString = DecTools.pow(udecString, weight)
                    } else {
                        binaryStr = binaryStr + "0"
                    }
                }

                if (binaryStr == "") {
                    console.warn("ByteValue.Type.Conversion.getBinary(udec: UDec) : error in calculation ${udec.getRawUDecStr()} to ${binaryStr}")
                }

                if (DebugTools.showTypeConversionInfo) {
                    console.info("Conversion: ${udec.getUDecStr()} to ${binaryStr}")
                }

                return Binary(binaryStr, udec.size)
            }

            fun getDec(binary: Binary): Dec {
                var binString = binary.getRawBinaryStr()
                var decString = "0"
                val negative: Boolean

                // check negative/pos binary value
                if (binary.size.bitWidth == binString.length) {
                    if (binString[0] == '1') {
                        negative = true
                        binString = BinaryTools.negotiate(binString)
                    } else {
                        negative = false
                    }
                } else {
                    binString = binString.padStart(binary.size.bitWidth, '0')
                    negative = false
                }

                for (weight in binString.length - 1 downTo 0) {
                    val index = binString.length - 1 - weight
                    val summand = DecTools.pow("2", weight.toString())
                    if (binString[index] == '1') {
                        decString = DecTools.add(decString, summand)
                    }
                }

                if (negative) {
                    decString = DecTools.negotiate(decString)
                }

                if (DebugTools.showTypeConversionInfo) {
                    console.info("Conversion: ${binary.getBinaryStr()} to ${decString}")
                }

                return Dec(decString, binary.size)
            }

            fun getUDec(binary: Binary): UDec {
                val binString = binary.getRawBinaryStr()

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
                if (DebugTools.showTypeConversionInfo) {
                    console.info("Conversion: ${binary.getBinaryStr()} to ${udecString}")
                }

                return UDec(udecString, binary.size)
            }

            fun getASCII(type: Type): String {
                val stringBuilder = StringBuilder()

                val hexString = when (type) {
                    is Hex -> type.getRawHexStr()
                    is Binary -> type.toHex().getRawHexStr()
                    is Dec -> type.toHex().getRawHexStr()
                    is UDec -> type.toHex().getRawHexStr()
                }

                val trimmedHex = hexString.trim().removePrefix(ArchConst.PRESTRING_HEX)

                for (i in trimmedHex.indices step 2) {
                    val hex = trimmedHex.substring(i, i + 2)
                    val decimal = hex.toIntOrNull(16)

                    if ((decimal != null) && (decimal in (32..126))) {
                        stringBuilder.append(decimal.toChar())
                    } else {
                        stringBuilder.append("·")
                    }
                }

                if (DebugTools.showTypeConversionInfo) {
                    console.info("Conversion: ${type.toHex().getHexStr()} to ${stringBuilder}")
                }

                return stringBuilder.toString()
            }

        }

    }

    sealed class Size(val bitWidth: kotlin.Int, val byteCount: kotlin.Int) {
        class Byte : Size(8, 1)
        class Short : Size(16, 2)
        class Int : Size(32, 4)
        class Long : Size(64, 8)
    }

    class Bounds {

        val min: String
        val umin: String
        val max: String
        val umax: String

        constructor(size: Size) {
            when (size) {
                is Size.Byte -> {
                    this.min = "-128"
                    this.max = "127"
                    this.umin = "0"
                    this.umax = "255"
                }

                is Size.Short -> {
                    this.min = "-32768"
                    this.max = "32767"
                    this.umin = "0"
                    this.umax = "65535"
                }

                is Size.Int -> {
                    this.min = "-2147483648"
                    this.max = "2147483647"
                    this.umin = "0"
                    this.umax = "4294967295"
                }

                is Size.Long -> {
                    this.min = "-9223372036854775807"
                    this.max = "9223372036854775807"
                    this.umin = "0"
                    this.umax = "18446744073709551615"
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

    data class CheckResult(val valid: Boolean, val corrected: String)

}
