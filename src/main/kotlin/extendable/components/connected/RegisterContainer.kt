package extendable.components.connected

import extendable.components.assembly.Compiler
import extendable.components.types.ByteValue

class RegisterContainer(private val registerFileList: List<RegisterFile>, val pcSize: ByteValue.Size) {

    val pc = PC(ByteValue("0", pcSize), ByteValue.Type.Binary("0"))

    fun clear() {
        for (registerFile in registerFileList) {
            for (reg in registerFile.registers) {
                reg.byteValue.clear()
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

    fun getRegister(address: ByteValue.Type): Register? {
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

    data class Register(val address: ByteValue.Type, val names: List<String>, val byteValue: ByteValue, val description: String, val hardwire: Boolean = false) {
        fun get(): ByteValue.Type {
            return byteValue.get()
        }

        fun set(value: ByteValue.Type) {
            if (!hardwire) {
                byteValue.set(value)
            }
        }
    }

    data class PC(val value: ByteValue, val initial: ByteValue.Type) {
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