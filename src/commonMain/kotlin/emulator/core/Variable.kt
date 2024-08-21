package emulator.core

import cengine.util.integer.Bounds
import cengine.util.integer.Size
import cengine.util.integer.Value

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
        this.initialBinary = value.toBin().toString()
    }

    constructor(size: Size) {
        this.value = Value.Bin("0", size)
        this.size = size
        this.initialBinary = value.toBin().toString()
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
    operator fun plusAssign(value: Value) {
        set(get() + value)
    }

    operator fun minusAssign(value: Value) {
        set(get() - value)
    }

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


}
