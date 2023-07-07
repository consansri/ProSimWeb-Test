package extendable.components.types

import extendable.ArchConst
import tools.DebugTools

class ByteValue {

    val initialBinary: String
    val size: Size
    private var value: Type

    constructor(initialBinary: String, size: Size) {
        this.initialBinary = initialBinary
        this.size = size
        value = Type.Binary(initialBinary, size)
    }

    constructor(type: Type) {
        this.value = type
        this.size = type.size
        this.initialBinary = type.toBin().getBinaryStr()
    }


    /* GETTER SETTER */
    fun get(): Type {
        return value
    }

    fun getBounds(): Bounds {
        return Bounds(size)
    }

    fun set(value: Type): ByteValue {
        this.value = value
        return this
    }

    fun setHex(hexString: String): ByteValue {
        value = Type.Hex(hexString, size)
        return this
    }

    fun setDec(decString: String): ByteValue {
        value = Type.Dec(decString, size)
        return this
    }

    fun setUDec(udecString: String): ByteValue {
        value = Type.UDec(udecString, size)
        return this
    }

    fun setBin(binString: String): ByteValue {
        value = Type.Binary(binString, size)
        return this
    }

    fun clear() {
        value = Type.Binary(initialBinary, size)
    }

    /* operator */
    operator fun plus(operand: ByteValue): ByteValue {
        return ByteValue(value + operand.get())
    }

    operator fun minus(operand: ByteValue): ByteValue {
        return ByteValue(value - operand.get())
    }

    operator fun times(operand: ByteValue): ByteValue {
        return ByteValue(value * operand.get())
    }

    operator fun div(operand: ByteValue): ByteValue {
        return ByteValue(value / operand.get())
    }

    operator fun rem(operand: ByteValue): ByteValue {
        return ByteValue(value % operand.get())
    }

    operator fun unaryMinus(): ByteValue {
        return ByteValue(-value)
    }

    override fun equals(other: Any?): Boolean {
        when (other) {
            is ByteValue -> {
                return (this.size == other.size && this.value == other.value)
            }

            is Type -> {
                return (this.value == other)
            }
        }


        return super.equals(other)
    }

    operator fun inc(): ByteValue {
        this.value = this.value++
        return ByteValue(++value)
    }

    operator fun dec(): ByteValue {
        this.value = this.value--
        return ByteValue(--value)
    }

    sealed class Type(val size: Size) {
        abstract fun check(string: String, size: Size, warnings: Boolean): CheckResult
        abstract fun toBin(): Binary
        abstract fun toHex(): Hex
        abstract fun toDec(): Dec
        abstract fun toUDec(): UDec
        abstract fun toASCII(): String
        abstract fun toDouble(): Double
        abstract fun getBiggest(): Type
        abstract operator fun plus(operand: Type): Type
        abstract operator fun minus(operand: Type): Type
        abstract operator fun times(operand: Type): Type
        abstract operator fun div(operand: Type): Type
        abstract operator fun rem(operand: Type): Type
        abstract operator fun unaryMinus(): Type
        abstract operator fun inc(): Type
        abstract operator fun dec(): Type
        override fun equals(other: Any?): Boolean {
            when (other) {
                is Type -> {
                    return BinaryTools.isEqual(this.toBin().getRawBinaryStr(), other.toBin().getRawBinaryStr())
                }
            }
            return super.equals(other)
        }

        class Binary(binString: String, size: Size) : Type(size) {
            private val binString: String

            constructor(size: Size) : this(ArchConst.PRESTRING_BINARY + "0", size)

            constructor(binString: String) : this(binString, Tools.getNearestSize(binString.trim().removePrefix(ArchConst.PRESTRING_BINARY).length))

            init {
                this.binString = check(binString, size, DebugTools.ARCH_showBVCheckWarnings).corrected
            }

            override fun check(string: String, size: Size, warnings: Boolean): CheckResult {
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
                        if (warnings) {
                            console.warn("ByteValue.Type.Binary.check(): ${formattedInput} is to long! Casted to TrimmedString(${trimmedString}) This value is layouted to hold up values with a bit width <= ${size.bitWidth}!")
                        }
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
                        if (warnings) {
                            console.warn("ByteValue.Type.Binary.check(): ${formattedInput} is to long! Casted to TrimmedString(${trimmedString}) This value is layouted to hold up values with a bit width <= ${size.bitWidth}!")
                        }
                        CheckResult(false, trimmedString)
                    }
                } else {
                    val zeroString = ArchConst.PRESTRING_BINARY + "0".repeat(size.bitWidth)
                    if (warnings) {
                        console.warn("ByteValue.Type.Binary.check(): ${formattedInput} does not match the binary Pattern (${ArchConst.PRESTRING_BINARY + "X".repeat(size.bitWidth)} where X is element of [0,1]), returning ${zeroString} instead!")
                    }
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

            fun getResized(size: Size): Binary {
                return Binary(getRawBinaryStr(), size)
            }

            override fun toHex(): Hex {
                return Conversion.getHex(this)
            }


            override fun toBin(): Binary {
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

            override fun getBiggest(): Type {
                return Binary("1".repeat(size.bitWidth), size)
            }

            override fun plus(operand: Type): Type {
                val result = BinaryTools.add(this.getRawBinaryStr(), operand.toBin().getRawBinaryStr())
                val biggerSize = if (this.size.byteCount > operand.size.byteCount) this.size else operand.size
                return Binary(result, biggerSize)
            }

            override fun minus(operand: Type): Type {
                val result = BinaryTools.sub(this.getRawBinaryStr(), operand.toBin().getRawBinaryStr())
                val biggerSize = if (this.size.byteCount > operand.size.byteCount) this.size else operand.size
                return Binary(result, biggerSize)
            }

            override fun times(operand: Type): Type {
                val result = BinaryTools.multiply(this.getRawBinaryStr(), operand.toBin().getRawBinaryStr())
                val biggerSize = if (this.size.byteCount > operand.size.byteCount) this.size else operand.size
                return Binary(result, biggerSize)
            }

            override fun div(operand: Type): Type {
                val divResult = BinaryTools.divide(this.getRawBinaryStr(), operand.toBin().getRawBinaryStr())
                val biggerSize = if (this.size.byteCount > operand.size.byteCount) this.size else operand.size
                return Binary(divResult.result, biggerSize)
            }

            override fun rem(operand: Type): Type {
                val divResult = BinaryTools.divide(this.getRawBinaryStr(), operand.toBin().getRawBinaryStr())
                val biggerSize = if (this.size.byteCount > operand.size.byteCount) this.size else operand.size
                return Binary(BinaryTools.checkEmpty(divResult.rest), biggerSize)
            }

            override fun unaryMinus(): Type {
                return Binary(BinaryTools.negotiate(this.getRawBinaryStr()), size)
            }

            override fun inc(): Type {
                return Binary(BinaryTools.add(this.getRawBinaryStr(), "1"), size)
            }

            override fun dec(): Type {
                return Binary(BinaryTools.sub(this.getRawBinaryStr(), "1"), size)
            }


        }

        class Hex(hexString: String, size: Size) : Type(size) {
            private val hexString: String

            init {
                this.hexString = check(hexString, size, DebugTools.ARCH_showBVCheckWarnings).corrected
            }

            constructor(hexString: String) : this(hexString, Tools.getNearestSize(hexString.trim().removePrefix(ArchConst.PRESTRING_HEX).length * 4))

            fun getRawHexStr(): String {
                return hexString.removePrefix(ArchConst.PRESTRING_HEX)
            }

            fun getHexStr(): String {
                return hexString
            }

            override fun check(string: String, size: Size, warnings: Boolean): CheckResult {
                var formatted = string.trim().removePrefix(ArchConst.PRESTRING_HEX).padStart(size.byteCount * 2, '0').uppercase()

                val regex = Regex("[0-9A-Fa-f]+")

                if (regex.matches(formatted)) {
                    if (formatted.length <= size.byteCount * 2) {
                        formatted = formatted.padStart(size.byteCount * 2, '0')
                        return CheckResult(true, ArchConst.PRESTRING_HEX + formatted)
                    } else {
                        val trimmedString = formatted.substring(formatted.length - size.byteCount * 2)
                        if (warnings) {
                            console.warn("ByteValue.Type.Hex.check(): ${formatted} is to long! Casted to TrimmedString(${trimmedString}) This value is layouted to hold up values with width <= ${size.byteCount * 2}!")
                        }
                        return CheckResult(false, ArchConst.PRESTRING_HEX + trimmedString)
                    }
                } else {
                    val zeroString = ArchConst.PRESTRING_HEX + "0".repeat(size.bitWidth)
                    if (warnings) {
                        console.warn("ByteValue.Type.Hex.check(): ${formatted} does not match the hex Pattern (${ArchConst.PRESTRING_HEX + "X".repeat(size.byteCount * 2)} where X is element of [0-9,A-F]), returning ${zeroString} instead!")
                    }
                    return CheckResult(false, zeroString)
                }
            }

            override fun toBin(): Binary {
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

            override fun getBiggest(): Type {
                return Hex("F".repeat(size.byteCount * 2), size)
            }

            override fun plus(operand: Type): Type {
                val result = BinaryTools.add(this.toBin().getRawBinaryStr(), operand.toBin().getRawBinaryStr())
                val biggerSize = if (this.size.byteCount > operand.size.byteCount) this.size else operand.size
                return Binary(result, biggerSize).toHex()
            }

            override fun minus(operand: Type): Type {
                val result = BinaryTools.sub(this.toBin().getRawBinaryStr(), operand.toBin().getRawBinaryStr())
                val biggerSize = if (this.size.byteCount > operand.size.byteCount) this.size else operand.size
                return Binary(result, biggerSize).toHex()
            }

            override fun times(operand: Type): Type {
                val result = BinaryTools.multiply(this.toBin().getRawBinaryStr(), operand.toBin().getRawBinaryStr())
                val biggerSize = if (this.size.byteCount > operand.size.byteCount) this.size else operand.size
                return Binary(result, biggerSize).toHex()
            }

            override fun div(operand: Type): Type {
                val divResult = BinaryTools.divide(this.toBin().getRawBinaryStr(), operand.toBin().getRawBinaryStr())
                val biggerSize = if (this.size.byteCount > operand.size.byteCount) this.size else operand.size
                return Binary(divResult.result, biggerSize).toHex()
            }

            override fun rem(operand: Type): Type {
                val divResult = BinaryTools.divide(this.toBin().getRawBinaryStr(), operand.toBin().getRawBinaryStr())
                val biggerSize = if (this.size.byteCount > operand.size.byteCount) this.size else operand.size
                return Binary(BinaryTools.checkEmpty(divResult.rest), biggerSize).toHex()
            }

            override fun unaryMinus(): Type {
                return Binary(BinaryTools.negotiate(this.toBin().getRawBinaryStr()), size).toHex()
            }

            override fun inc(): Type {
                return Binary(BinaryTools.add(this.toBin().getRawBinaryStr(), "1"), size).toHex()
            }

            override fun dec(): Type {
                return Binary(BinaryTools.sub(this.toBin().getRawBinaryStr(), "1"), size).toHex()
            }

            override fun toHex(): Hex {
                return this
            }
        }

        class Dec(decString: String, size: Size) : Type(size) {
            private val decString: String
            private val negative: Boolean

            init {
                this.decString = check(decString, size, DebugTools.ARCH_showBVCheckWarnings).corrected
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

            override fun check(string: String, size: Size, warnings: Boolean): CheckResult {
                val formatted = string.trim().removePrefix(ArchConst.PRESTRING_DECIMAL)

                val posRegex = Regex("[0-9]+")

                if (!posRegex.matches(formatted.replace("-", ""))) {
                    val zeroString = "0"
                    if (warnings) {
                        console.warn("ByteValue.Type.Dec.check(): ${formatted} does not match the dec Pattern (${ArchConst.PRESTRING_DECIMAL + "(-)" + "X".repeat(size.bitWidth)} where X is element of [0-9]), returning ${zeroString} instead!")
                    }
                    return CheckResult(false, ArchConst.PRESTRING_DECIMAL + zeroString)
                } else {
                    if (DecTools.isGreaterThan(formatted, Bounds(size).max)) {
                        if (warnings) {
                            console.warn("ByteValue.Type.Dec.check(): ${formatted} must be smaller equal ${Bounds(size).max} -> setting ${Bounds(size).max}")
                        }
                        return CheckResult(false, ArchConst.PRESTRING_DECIMAL + Bounds(size).max)
                    } else if (DecTools.isGreaterThan(Bounds(size).min, formatted)) {
                        if (warnings) {
                            console.warn("ByteValue.Type.Dec.check(): ${formatted} must be bigger equal ${Bounds(size).min} -> setting ${Bounds(size).min}")
                        }
                        return CheckResult(false, ArchConst.PRESTRING_DECIMAL + Bounds(size).min)
                    } else {
                        return CheckResult(true, ArchConst.PRESTRING_DECIMAL + formatted)
                    }
                }
            }

            override fun toBin(): Binary {
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
                    console.warn("ByteValue.Type.toDouble(): NumberFormatException (dec: ${getRawDecStr()}) -> returning 0")
                    return 0.0
                }
            }

            override fun getBiggest(): Type {
                return Dec(Bounds(size).max, size)
            }

            override fun plus(operand: Type): Type {
                val result = DecTools.add(this.getRawDecStr(), operand.toDec().getRawDecStr())
                val biggerSize = if (this.size.byteCount > operand.size.byteCount) this.size else operand.size
                return Dec(result, biggerSize)
            }

            override fun minus(operand: Type): Type {
                val result = DecTools.sub(this.getRawDecStr(), operand.toDec().getRawDecStr())
                val biggerSize = if (this.size.byteCount > operand.size.byteCount) this.size else operand.size
                return Dec(result, biggerSize)
            }

            override fun times(operand: Type): Type {
                val result = DecTools.multiply(this.getRawDecStr(), operand.toDec().getRawDecStr())
                val biggerSize = if (this.size.byteCount > operand.size.byteCount) this.size else operand.size
                return Dec(result, biggerSize)
            }

            override fun div(operand: Type): Type {
                val divResult = DecTools.divide(this.getRawDecStr(), operand.toDec().getRawDecStr())
                val biggerSize = if (this.size.byteCount > operand.size.byteCount) this.size else operand.size
                return Dec(divResult.result, biggerSize)
            }

            override fun rem(operand: Type): Type {
                val divResult = DecTools.divide(this.getRawDecStr(), operand.toDec().getRawDecStr())
                val biggerSize = if (this.size.byteCount > operand.size.byteCount) this.size else operand.size
                return Dec(DecTools.checkEmpty(divResult.rest), biggerSize)
            }

            override fun unaryMinus(): Type {
                return Dec(DecTools.negotiate(this.getRawDecStr()), size)
            }

            override fun inc(): Type {
                return Dec(DecTools.add(this.getRawDecStr(), "1"), size)
            }

            override fun dec(): Type {
                return Dec(DecTools.sub(this.getRawDecStr(), "1"), size)
            }

        }

        class UDec(udecString: String, size: Size) : Type(size) {
            private val udecString: String

            init {
                this.udecString = check(udecString, size, DebugTools.ARCH_showBVCheckWarnings).corrected
            }

            fun getUDecStr(): String {
                return udecString
            }

            fun getRawUDecStr(): String {
                return udecString.removePrefix(ArchConst.PRESTRING_DECIMAL)
            }

            override fun check(string: String, size: Size, warnings: Boolean): CheckResult {
                val formatted = string.trim().removePrefix(ArchConst.PRESTRING_DECIMAL)

                val posRegex = Regex("[0-9]+")
                val negRegex = Regex("-[0-9]+")

                if (negRegex.matches(formatted)) {
                    val posValue = DecTools.abs(formatted)
                    if(warnings) {
                        console.warn("ByteValue.Type.UDec.check(): ${formatted} is negative! returning absolute Value instead: ${posValue}")
                    }
                    return CheckResult(false, ArchConst.PRESTRING_DECIMAL + posValue)
                } else if (!posRegex.matches(formatted)) {
                    val zeroString = "0"
                    if(warnings) {
                        console.warn("ByteValue.Type.UDec.check(): ${formatted} does not match the dec Pattern (${ArchConst.PRESTRING_DECIMAL + "X".repeat(size.bitWidth)} where X is element of [0-9]), returning ${zeroString} instead!")
                    }
                    return CheckResult(false, ArchConst.PRESTRING_DECIMAL + zeroString)
                } else {
                    if (DecTools.isGreaterThan(formatted, Bounds(size).umax)) {
                        if(warnings) {
                            console.warn("ByteValue.Type.UDec.check(): ${formatted} must be smaller equal ${Bounds(size).umax} -> setting ${Bounds(size).umax}")
                        }
                        return CheckResult(false, ArchConst.PRESTRING_DECIMAL + Bounds(size).umax)
                    } else if (DecTools.isGreaterThan(Bounds(size).umin, formatted)) {
                        if(warnings) {
                            console.warn("ByteValue.Type.UDec.check(): ${formatted} must be bigger equal ${Bounds(size).umin} -> setting ${Bounds(size).umin}")
                        }
                        return CheckResult(false, ArchConst.PRESTRING_DECIMAL + Bounds(size).umin)
                    } else {
                        return CheckResult(true, ArchConst.PRESTRING_DECIMAL + formatted)
                    }
                }
            }

            override fun toBin(): Binary {
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
                    console.warn("ByteValue.Type.toDouble(): NumberFormatException (udec: ${getRawUDecStr()}) -> returning 0")
                    return 0.0
                }
            }

            override fun getBiggest(): Type {
                return UDec(Bounds(size).umax, size)
            }

            override fun plus(operand: Type): Type {
                val result = DecTools.add(this.getRawUDecStr(), operand.toUDec().getRawUDecStr())
                val biggerSize = if (this.size.byteCount > operand.size.byteCount) this.size else operand.size
                return UDec(result, biggerSize)
            }

            override fun minus(operand: Type): Type {
                val result = DecTools.sub(this.getRawUDecStr(), operand.toUDec().getRawUDecStr())
                val biggerSize = if (this.size.byteCount > operand.size.byteCount) this.size else operand.size
                return UDec(result, biggerSize)
            }

            override fun times(operand: Type): Type {
                val result = DecTools.multiply(this.getRawUDecStr(), operand.toUDec().getRawUDecStr())
                val biggerSize = if (this.size.byteCount > operand.size.byteCount) this.size else operand.size
                return UDec(result, biggerSize)
            }

            override fun div(operand: Type): Type {
                val divResult = DecTools.divide(this.getRawUDecStr(), operand.toUDec().getRawUDecStr())
                val biggerSize = if (this.size.byteCount > operand.size.byteCount) this.size else operand.size
                return UDec(divResult.result, biggerSize)
            }

            override fun rem(operand: Type): Type {
                val divResult = DecTools.divide(this.getRawUDecStr(), operand.toUDec().getRawUDecStr())
                val biggerSize = if (this.size.byteCount > operand.size.byteCount) this.size else operand.size
                return UDec(DecTools.checkEmpty(divResult.rest), biggerSize)
            }

            override fun unaryMinus(): Type {
                console.warn("ByteValue.Type.UDec: Executing unaryMinus on unsigned decimal! -> no affect!")
                return UDec(this.getRawUDecStr(), size)
            }

            override fun inc(): Type {
                return Dec(DecTools.add(this.getRawUDecStr(), "1"), size)
            }

            override fun dec(): Type {
                return Dec(DecTools.sub(this.getRawUDecStr(), "1"), size)
            }
        }

        object Conversion {

            fun getType(string: String): Type {
                var removedPrefString = string.trim().removePrefix(ArchConst.PRESTRING_BINARY)
                if (removedPrefString.length < string.trim().length - 1) {
                    return Binary("0", Size.Bit8())
                }
                removedPrefString = string.trim().removePrefix(ArchConst.PRESTRING_HEX)
                if (removedPrefString.length < string.trim().length - 1) {
                    return Hex("0", Size.Bit8())
                }
                return Dec("0", Size.Bit8())
            }

            fun getHex(binary: Binary): Hex {
                val stringBuilder = StringBuilder()

                val binStr = binary.getRawBinaryStr()

                for (i in binStr.indices step 4) {
                    val substring = binStr.substring(i, i + 4)
                    val int = substring.toInt(2)
                    stringBuilder.append(int.toString(16).uppercase())
                }

                if (DebugTools.ARCH_showBVTypeConversionInfo) {
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
                if (DebugTools.ARCH_showBVTypeConversionInfo) {
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

                if (DebugTools.ARCH_showBVTypeConversionInfo) {
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

                if (DebugTools.ARCH_showBVTypeConversionInfo) {
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

                if (DebugTools.ARCH_showBVTypeConversionInfo) {
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
                if (DebugTools.ARCH_showBVTypeConversionInfo) {
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

                if (DebugTools.ARCH_showBVTypeConversionInfo) {
                    console.info("Conversion: ${type.toHex().getHexStr()} to ${stringBuilder}")
                }

                return stringBuilder.toString()
            }

        }

    }

    sealed class Size(val bitWidth: kotlin.Int, val byteCount: kotlin.Int) {

        override fun equals(other: Any?): Boolean {
            when (other) {
                is Size -> {
                    return this.byteCount == other.byteCount
                }
            }
            return super.equals(other)
        }

        class Bit3 : Size(3, 1)
        class Bit5 : Size(5, 1)
        class Bit7 : Size(7, 1)
        class Bit8 : Size(8, 1)
        class Bit12 : Size(12, 2)
        class Bit16 : Size(16, 2)
        class Bit20 : Size(20, 3)
        class Bit32 : Size(32, 4)
        class Bit64 : Size(64, 8)
        class Bit128 : Size(128, 16)

    }

    class Bounds {

        val min: String
        val umin: String
        val max: String
        val umax: String

        constructor(size: Size) {
            when (size) {
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
            }
        }

        constructor(min: String, max: String) {
            this.min = min
            this.max = max
            this.umin = "0"
            this.umax = DecTools.abs(DecTools.sub(min, max))
        }


    }

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
                    console.warn("ByteValue.Bounds.getNearestSize(): $bitWidth is greater than possible maximum Size of 128bit -> returning Size.Bit128()")
                    return Size.Bit128()
                }
            }
        }
    }

    data class CheckResult(val valid: Boolean, val corrected: String)

}
