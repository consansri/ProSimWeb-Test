package emulator.kit.common

import emulator.kit.types.Variable

/**
 * The [RegContainer] is making all [RegisterFile]s besides the [PC] accessible.
 * It contains searching functions to find a register by name or alias ([getReg]).
 */
class RegContainer(private val registerFileList: List<RegisterFile>, val pcSize: Variable.Size) {

    val pc = PC(Variable("0", pcSize), Variable.Value.Bin("0"))

    fun clear() {
        for (registerFile in registerFileList) {
            for (reg in registerFile.registers) {
                reg.variable.clear()
            }
        }
    }

    fun getReg(name: String): Register? {
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

    fun getReg(address: Variable.Value): Register? {
        for (registerFile in registerFileList) {
            for (reg in registerFile.registers) {
                if (reg.address == address) {
                    return reg
                }
            }
        }
        return null
    }

    fun getAllRegs(): List<Register> {
        val allRegs = mutableListOf<Register>()
        registerFileList.forEach { allRegs.addAll(it.registers) }
        return allRegs
    }

    fun getRegFileList(): List<RegisterFile> {
        return registerFileList
    }

    /**
     * A [Register] is identified by [address], [names] or [aliases] it contains a [variable] to hold the current state.
     * You can [hardwire] it to disallow changeability.
     * To identify registers more easily a [description] is needed in the constructor.
     */
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

    data class PC(val variable: Variable, val initial: Variable.Value) {
        val name = "program counter"
        val shortName = "pc"

        fun get(): Variable.Value = variable.get()

        fun set(value: Variable.Value){
            variable.set(value)
        }

        fun reset() {
            variable.set(initial)
        }

    }

    data class RegisterFile(val name: String, val registers: Array<Register>)


}