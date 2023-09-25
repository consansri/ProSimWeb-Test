package emulator.kit.common

import emulator.kit.types.Variable

class RegisterContainer(private val registerFileList: List<RegisterFile>, val pcSize: Variable.Size) {

    val pc = PC(Variable("0", pcSize), Variable.Value.Bin("0"))

    fun clear() {
        for (registerFile in registerFileList) {
            for (reg in registerFile.registers) {
                reg.variable.clear()
            }
        }
    }

    fun getRegister(name: String): Register? {
        for (registerFile in registerFileList) {
            for (reg in registerFile.registers) {
                if (reg.names.contains(name)) {
                    return reg
                }
                if (reg.aliases.contains(name)) {
                    return reg
                }
            }
        }
        return null
    }

    fun getRegister(address: Variable.Value): Register? {
        for (registerFile in registerFileList) {
            for (reg in registerFile.registers) {
                if (reg.address == address) {
                    return reg
                }
            }
        }
        return null
    }

    fun getAllRegisters(): List<Register> {
        val allRegs = mutableListOf<Register>()
        registerFileList.forEach { allRegs.addAll(it.registers) }
        return allRegs
    }

    fun getRegisterFileList(): List<RegisterFile> {
        return registerFileList
    }

    data class Register(val address: Variable.Value, val names: List<String>, val aliases: List<String>, val variable: Variable, val description: String, val hardwire: Boolean = false) {

        private val regexList: List<Regex>

        init {
            val mutableRegexList: MutableList<Regex> = mutableListOf()
            names.forEach { mutableRegexList.add(Regex("""\b(${Regex.escape(it)})\b""")) }
            aliases.forEach { mutableRegexList.add(Regex("""\b(${Regex.escape(it)})\b""")) }
            regexList = mutableRegexList
        }

        fun get(): Variable.Value {
            return variable.get()
        }

        fun set(value: Variable.Value) {
            if (!hardwire) {
                variable.set(value)
            }
        }

        fun getRegexList(): List<Regex> {
            return regexList
        }
    }

    data class PC(val value: Variable, val initial: Variable.Value) {
        val name = "program counter"
        val shortName = "pc"

        fun reset() {
            value.set(initial)
        }

    }

    data class RegisterFile(val name: String, val registers: Array<Register>)


}