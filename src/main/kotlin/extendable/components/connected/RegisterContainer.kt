package extendable.components.connected

import extendable.components.types.MutVal

class RegisterContainer(private val registerFileList: List<RegisterFile>, val pcSize: MutVal.Size) {

    val pc = PC(MutVal("0", pcSize), MutVal.Value.Binary("0"))

    fun clear() {
        for (registerFile in registerFileList) {
            for (reg in registerFile.registers) {
                reg.mutVal.clear()
            }
        }
    }

    fun getRegistersFromLabel(label: RegLabel): RegisterFile? {
        for (registerFile in registerFileList) {
            if (registerFile.label == label) {
                return registerFile
            }
        }
        return null
    }

    fun getRegister(name: String): Register? {
        for (registerFile in registerFileList) {
            for (reg in registerFile.registers) {
                if (reg.names.contains(name)) {
                    return reg
                }
            }
        }
        return null
    }

    fun getRegister(address: MutVal.Value): Register? {
        for (registerFile in registerFileList) {
            for (reg in registerFile.registers) {
                if (reg.address == address) {
                    return reg
                }
            }
        }
        return null
    }

    fun getRegisterFileList(): List<RegisterFile> {
        return registerFileList
    }

    data class Register(val address: MutVal.Value, val names: List<String>, val mutVal: MutVal, val description: String, val hardwire: Boolean = false) {
        fun get(): MutVal.Value {
            return mutVal.get()
        }

        fun set(value: MutVal.Value) {
            if (!hardwire) {
                mutVal.set(value)
            }
        }
    }

    data class PC(val value: MutVal, val initial: MutVal.Value) {
        val name = "program counter"
        val shortName = "pc"

        fun reset() {
            value.set(initial)
        }

    }

    data class RegisterFile(val label: RegLabel, val name: String, val registers: Array<Register>)

    enum class RegLabel {
        MAIN,
        SYSTEM,
        CUSTOM
    }


}