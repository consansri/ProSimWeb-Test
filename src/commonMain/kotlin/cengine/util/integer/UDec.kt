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
    override val input: String
    override val valid: Boolean

    init {
        val result = check(udecString, size)
        input = result.corrected
        valid = result.valid
    }

    constructor(udecString: String) : this(udecString, nearestUDecSize(udecString.trim().removePrefix(Settings.PRESTRING_UDECIMAL))) {
        if (DebugTools.KIT_showValCheckWarnings) {
            println("UDec(): Calculated Size from $udecString as hex ${this.toHex().toRawString()} -> ${size.bitWidth}")
        }
    }

    fun getUResized(size: Size): UDec = UDec(toRawString(), size)

    override fun check(string: String, size: Size): CheckResult {
        val formatted = string.trim().removePrefix(Settings.PRESTRING_UDECIMAL)
        val message: String
        if (!formatted.all { it.isDigit() }) {
            val zeroString = "0"
            message = "UDec.check(): $formatted does not match the udec Pattern (${Settings.PRESTRING_UDECIMAL + "X".repeat(size.bitWidth)} where X is element of [0-9]), returning $zeroString instead!"
            nativeError(message)
            return CheckResult(false, Settings.PRESTRING_UDECIMAL + zeroString, message)
        } else {
            return if (DecimalTools.isGreaterThan(formatted, Bounds(size).umax)) {
                message = "UDec.check(): $formatted must be smaller equal ${Bounds(size).umax} -> setting ${Bounds(size).umax}"
                CheckResult(false, Settings.PRESTRING_UDECIMAL + Bounds(size).umax, message)
            } else if (DecimalTools.isGreaterThan(Bounds(size).umin, formatted)) {
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
    override fun toBin(): Bin = getBinary()
    override fun toHex(): Hex = getBinary().getHex()
    override fun toOct(): Oct = getBinary().getOct()
    override fun toDec(): Dec = getBinary().getDec()
    override fun toUDec(): UDec = this
    override fun toASCII(): String = getASCII()
    override fun toLong(): Long = toULong().toLong()

    override fun toULong(): ULong = toRawString().toULong()

    fun toIntOrNull(): Int? = toRawString().toIntOrNull()
    fun toDoubleOrNull(): Double? = toRawString().toDoubleOrNull()
    override fun getBiggest(): Value = UDec(Bounds(size).umax, size)

    override fun plus(operand: Value): UDec {
        val biggerSize = if (this.size.bitWidth > operand.size.bitWidth) this.size else operand.size

        val result = if(biggerSize.bitWidth <= 64){
            (toULong() + operand.toULong()).toString()
        }else{
            DecimalTools.add(this.toRawString(), operand.toUDec().toRawString())
        }

        return UDec(result, biggerSize)
    }

    override fun minus(operand: Value): UDec {
        val biggerSize = if (this.size.bitWidth > operand.size.bitWidth) this.size else operand.size

        val result = if(biggerSize.bitWidth <= 64){
            (toULong() - operand.toULong()).toString()
        }else{
            DecimalTools.sub(this.toRawString(), operand.toUDec().toRawString())
        }

        return UDec(result, biggerSize)
    }

    override fun times(operand: Value): UDec {
        val biggerSize = if (this.size.bitWidth > operand.size.bitWidth) this.size else operand.size

        val result = if(biggerSize.bitWidth <= 64){
            (toULong() * operand.toULong()).toString()
        }else{
            DecimalTools.multiply(this.toRawString(), operand.toUDec().toRawString())
        }

        return UDec(result, biggerSize)
    }

    override fun div(operand: Value): UDec {
        val biggerSize = if (this.size.bitWidth > operand.size.bitWidth) this.size else operand.size

        val result = if(biggerSize.bitWidth <= 64){
            (toULong() / operand.toULong()).toString()
        }else{
            DecimalTools.divide(this.toRawString(), operand.toUDec().toRawString()).result
        }

        return UDec(result, biggerSize)
    }

    override fun rem(operand: Value): UDec {
        val biggerSize = if (this.size.bitWidth > operand.size.bitWidth) this.size else operand.size

        val result = if(biggerSize.bitWidth <= 64){
            (toULong() % operand.toULong()).toString()
        }else{
            DecimalTools.checkEmpty(DecimalTools.divide(this.toRawString(), operand.toUDec().toRawString()).rest)
        }

        return UDec(result, biggerSize)
    }

    override fun unaryMinus(): Value {
        return -this.toDec()
    }

    override fun inc(): Value = Dec(DecimalTools.add(this.toRawString(), "1"), size)
    override fun dec(): Value = Dec(DecimalTools.sub(this.toRawString(), "1"), size)

    override fun compareTo(other: Value): Int {
        if (size.bitWidth <= 64 && other.size.bitWidth <= 64) return toULong().compareTo(other.toULong())

        return if (DecimalTools.isEqual(this.toRawString(), other.toUDec().toRawString())) {
            0
        } else if (DecimalTools.isGreaterThan(this.toRawString(), other.toUDec().toRawString())) {
            1
        } else {
            -1
        }
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