package extendable.components.connected

import extendable.components.types.Address
import extendable.components.types.ByteValue

class RegisterContainer(private val registerFileList: List<RegisterFile>) {

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
                if (reg.name == name) {
                    return reg
                }
            }
        }
        return null
    }

    fun getRegister(address: Address): Register? {
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

    data class Register(val address: Address, val name: String, val byteValue: ByteValue, val description: String)

    data class RegisterFile(val label: RegLabel, val name: String, val registers: Array<Register>)

    enum class RegLabel {
        PC,
        MAIN,
        SYSTEM,
        CUSTOM
    }

}