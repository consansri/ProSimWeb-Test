package emulator.kit.optional

import emulator.kit.Architecture
import kotlin.enums.EnumEntries

sealed class SetupSetting<T>(val name: String, val init: T, private val valueToString: (T) -> String, private val parseValueFromString: (String) -> T, val onChange: (Architecture, SetupSetting<T>) -> Unit) {

    val trimmedName = name.replace(spaceRegex, "")
    private var value: T = init

    fun set(arch: Architecture, new: T) {
        value = new
        onChange(arch, this)
    }

    fun get(): T = value

    fun valueToString(): String {
        return valueToString(value)
    }

    fun loadFromString(arch: Architecture, string: String) {
        set(arch, parseValueFromString(string))
    }

    class Bool(name: String, init: Boolean, onChange: (Architecture, SetupSetting<Boolean>) -> Unit) : SetupSetting<Boolean>(name, init, { it.toString() }, { it.toBoolean() }, onChange)
    class Enumeration<T : Enum<T>>(name: String, val enumValues: EnumEntries<T>, init: T, onChange: (Architecture, SetupSetting<T>) -> Unit) : SetupSetting<T>(name, init, { it.name }, { value -> enumValues.firstOrNull { it.name == value } ?: init }, onChange)

    class Any<T>(name: String, init: T, valueToString: (T) -> String, parseValueFromString: (String) -> T, onChange: (Architecture, SetupSetting<T>) -> Unit) : SetupSetting<T>(name, init, valueToString, parseValueFromString, onChange)

    companion object{
        val spaceRegex = Regex("""\s""")
    }

}