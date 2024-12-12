package cengine.util.integer

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf

/**
 * [Variable] is the mutable version of [Value] which can contain several types all based on [String].
 * Each Variable has a fixed [size] which can't be changed. When a new value will be [set] it will automatically be resized to the former [size].
 * Operator Functions such as comparing functions are overwritten.
 */
@Deprecated("Use IntNumber instead, cause it's way faster then Value!")
class Variable(private val initialBinary: String, val size: Size, initValue: Value) {

    val state: MutableState<Value> = mutableStateOf(initValue)

    var value: Value
        set(value) {
            state.component2().invoke(value)
        }
        get() = state.component1()

    constructor(initialBinary: String, size: Size) : this(initialBinary, size, Bin(initialBinary, size))

    constructor(value: Value) : this(value.toBin().rawInput, value.size, value)

    constructor(size: Size) : this("0", size, Bin("0", size))

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
        state.value = value.toBin().getResized(size)
        return this
    }

    fun setHex(hexString: String): Variable {
        value = Hex(hexString, size)
        return this
    }

    fun setDec(decString: String): Variable {
        value = Dec(decString, size)
        return this
    }

    fun setUDec(udecString: String): Variable {
        value = UDec(udecString, size)
        return this
    }

    fun setBin(binString: String): Variable {
        value = Bin(binString, size)
        return this
    }

    fun clear() {
        value = Bin(initialBinary, size)
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
