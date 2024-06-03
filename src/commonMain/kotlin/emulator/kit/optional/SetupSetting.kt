package emulator.kit.optional

import emulator.kit.Architecture
import emulator.kit.nativeLog

sealed class SetupSetting<T>(val name: String, val init: T, private val parseValueFromString: (String) -> T, val onChange: (Architecture, SetupSetting<T>) -> Unit) {

    private var value: T = init

    fun set(arch: Architecture, new: T) {
        value = new
        onChange(arch, this)
    }

    fun get(): T = value

    fun valueToString(): String = value.toString()

    fun loadFromString(arch: Architecture, string: String) {
        set(arch, parseValueFromString(string))
    }

    class Bool(name: String, init: Boolean, onChange: (Architecture, SetupSetting<Boolean>) -> Unit) : SetupSetting<Boolean>(name, init, { it.toBoolean() }, onChange)

    class Any<T>(name: String, init: T, parseValueFromString: (String) -> T, onChange: (Architecture, SetupSetting<T>) -> Unit): SetupSetting<T>(name, init, parseValueFromString, onChange)

}