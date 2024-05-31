package emulator.kit.optional

import emulator.kit.Architecture
import emulator.kit.types.Variable

sealed class ArchSetting(val name: String, val onChange: (Architecture, ArchSetting) -> Unit) {

    open fun set(arch: Architecture, new: Any) {
        onChange(arch, this)
    }

    abstract fun get(): Any

    class Number(name: String, defaultValue: Variable.Value, onChange: (Architecture, ArchSetting) -> Unit) : ArchSetting(name, onChange) {
        private val number: Variable = Variable(defaultValue)

        override fun set(arch: Architecture, new: Any) {
            if (new !is Variable.Value) throw Exception("$new is not of type ${Variable.Value::class.simpleName}")
            number.set(new)
            super.set(arch, new)
        }

        override fun get(): Variable.Value = number.get()

    }

    class Bool(name: String, defaultValue: Boolean, onChange: (Architecture, ArchSetting) -> Unit) : ArchSetting(name, onChange) {

        private var boolean: Boolean = defaultValue

        override fun set(arch: Architecture, new: Any) {
            if (new !is Boolean) throw Exception("$new is not of type ${Boolean::class.simpleName}")
            boolean = new
            super.set(arch, new)
        }

        override fun get(): Boolean = boolean
    }

}