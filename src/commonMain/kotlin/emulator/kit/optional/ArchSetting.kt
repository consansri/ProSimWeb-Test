package emulator.kit.optional

import emulator.kit.types.Variable

sealed class ArchSetting(val name: String) {

    class ImmSetting(name: String , val value: Variable) : ArchSetting(name)

    class BoolSetting(name: String, val boolean: Boolean): ArchSetting(name)

}