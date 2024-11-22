package cengine.util.integer

import Settings
import cengine.util.integer.Size.Companion.nearestUDecSize
import cengine.util.integer.binary.BinaryTools
import cengine.util.integer.decimal.DecimalTools
import debug.DebugTools
import emulator.kit.nativeError

/**
 * Provides the unsigned decimal representation of [Value].
 */
class UDec(udecString: String, size: Size) : Value(size) {
    override val valid: Boolean
    override val rawInput: String
    
    init {
        val result = check(udecString, size)
        valid = result.valid
        rawInput = result.correctedRawInput
    }

    constructor(udecString: String) : this(udecString, nearestUDecSize(udecString.trim().removePrefix(Settings.PRESTRING_UDECIMAL))) {
        if (DebugTools.KIT_showValCheckWarnings) {
            println("UDec(): Calculated Size from $udecString as hex ${this.toHex().rawInput} -> ${size.bitWidth}")
        }
    }

    fun getUResized(size: Size): UDec = UDec(rawInput, size)

    override fun check(string: String, size: Size): CheckResult {
        val formatted = string.trim().removePrefix(Settings.PRESTRING_UDECIMAL)
        val message: String
        if (!formatted.all { it.isDigit() }) {
            val zeroString = "0"
            message = "UDec.check(): $formatted does not match the udec Pattern (${Settings.PRESTRING_UDECIMAL + "X".repeat(size.bitWidth)} where X is element of [0-9]), returning $zeroString instead!"
            nativeError(message)
            return CheckResult(false, zeroString, message)
        } else {
            return if (DecimalTools.isGreaterThan(formatted, Bounds(size).umax)) {
                message = "UDec.check(): $formatted must be smaller equal ${Bounds(size).umax} -> setting ${Bounds(size).umax}"
                CheckResult(false, Bounds(size).umax, message)
            } else if (DecimalTools.isGreaterThan(Bounds(size).umin, formatted)) {
                message = "UDec.check(): $formatted must be bigger equal ${Bounds(size).umin} -> setting ${Bounds(size).umin}"
                CheckResult(false, Bounds(size).umin, message)
            } else {
                CheckResult(true, formatted)
            }
        }
    }

    override fun check(size: Size): CheckResult = check(rawInput, size)
    override fun checkSizeSigned(other: Size): Boolean = toDec().checkSizeSigned(other)
    override fun checkSizeUnsigned(other: Size): Boolean = toBin().checkSizeUnsigned(other)
    override fun toBin(): Bin = getBinary()
    override fun toHex(): Hex = getBinary().getHex()
    override fun toOct(): Oct = getBinary().getOct()
    override fun toDec(): Dec = getBinary().getDec()
    override fun toUDec(): UDec = this
    override fun toASCII(): String = getASCII()
    override fun toLong(): Long = toULong().toLong()

    override fun toULong(): ULong = rawInput.toULong()

    fun toIntOrNull(): Int? = rawInput.toIntOrNull()
    fun toDoubleOrNull(): Double? = rawInput.toDoubleOrNull()
    override fun getBiggest(): Value = UDec(Bounds(size).umax, size)

    override fun plus(operand: Value): UDec {
        val biggerSize = if (this.size.bitWidth > operand.size.bitWidth) this.size else operand.size

        val result = if(biggerSize.bitWidth <= 64){
            (toULong() + operand.toULong()).toString()
        }else{
            DecimalTools.add(this.rawInput, operand.toUDec().rawInput)
        }

        return UDec(result, biggerSize)
    }

    override fun minus(operand: Value): UDec {
        val biggerSize = if (this.size.bitWidth > operand.size.bitWidth) this.size else operand.size

        val result = if(biggerSize.bitWidth <= 64){
            (toULong() - operand.toULong()).toString()
        }else{
            DecimalTools.sub(this.rawInput, operand.toUDec().rawInput)
        }

        return UDec(result, biggerSize)
    }

    override fun times(operand: Value): UDec {
        val biggerSize = if (this.size.bitWidth > operand.size.bitWidth) this.size else operand.size

        val result = if(biggerSize.bitWidth <= 64){
            (toULong() * operand.toULong()).toString()
        }else{
            DecimalTools.multiply(this.rawInput, operand.toUDec().rawInput)
        }

        return UDec(result, biggerSize)
    }

    override fun div(operand: Value): UDec {
        val biggerSize = if (this.size.bitWidth > operand.size.bitWidth) this.size else operand.size

        val result = if(biggerSize.bitWidth <= 64){
            (toULong() / operand.toULong()).toString()
        }else{
            DecimalTools.divide(this.rawInput, operand.toUDec().rawInput).result
        }

        return UDec(result, biggerSize)
    }

    override fun rem(operand: Value): UDec {
        val biggerSize = if (this.size.bitWidth > operand.size.bitWidth) this.size else operand.size

        val result = if(biggerSize.bitWidth <= 64){
            (toULong() % operand.toULong()).toString()
        }else{
            DecimalTools.checkEmpty(DecimalTools.divide(this.rawInput, operand.toUDec().rawInput).rest)
        }

        return UDec(result, biggerSize)
    }

    override fun unaryMinus(): Value {
        return -this.toDec()
    }

    override fun inc(): Value = Dec(DecimalTools.add(this.rawInput, "1"), size)
    override fun dec(): Value = Dec(DecimalTools.sub(this.rawInput, "1"), size)

    override fun compareTo(other: Value): Int {
        if (size.bitWidth <= 64 && other.size.bitWidth <= 64) return toULong().compareTo(other.toULong())

        return if (DecimalTools.isEqual(this.rawInput, other.toUDec().rawInput)) {
            0
        } else if (DecimalTools.isGreaterThan(this.rawInput, other.toUDec().rawInput)) {
            1
        } else {
            -1
        }
    }
    
    override fun toString(): String = "${Settings.PRESTRING_UDECIMAL}$rawInput"

    override fun equals(other: Any?): Boolean {
        if (other is Value) {
            return BinaryTools.isEqual(toBin().rawInput, other.toBin().rawInput)
        }
        return false
    }
}