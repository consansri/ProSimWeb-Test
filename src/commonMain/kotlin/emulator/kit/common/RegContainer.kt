package emulator.kit.common

import emulator.kit.optional.Feature
import emulator.kit.types.Variable

/**
 * The [RegContainer] is making all [RegisterFile]s besides the [PC] accessible.
 * It contains searching functions to find a register by name or alias ([getReg]).
 */
class RegContainer(private val registerFileList: List<RegisterFile>, val pcSize: Variable.Size, private val standardRegFileName: String) {

    val pc = PC(Variable("0", pcSize), Variable.Value.Bin("0", pcSize))

    fun clear() {
        for (registerFile in registerFileList) {
            registerFile.clearAll()
        }
        pc.set(Variable.Value.Bin("0", pcSize))
    }

    fun getReg(name: String, features: List<Feature>, regFile: String? = null): Register? {
        for (registerFile in registerFileList) {
            if ((regFile == null && registerFile.name == standardRegFileName) xor (registerFile.name == regFile)) {
                for (reg in registerFile.getRegisters(features)) {
                    if (reg.names.contains(name)) {
                        return reg
                    }
                    if (reg.aliases.contains(name)) {
                        return reg
                    }
                }
            }
        }
        return null
    }

    fun getReg(address: Variable.Value, features: List<Feature>, regFile: String? = null): Register? {
        for (registerFile in registerFileList) {
            if ((regFile == null && registerFile.name == standardRegFileName) xor (registerFile.name == regFile)) {
                for (reg in registerFile.getRegisters(features)) {
                    if (reg.address.toHex().getRawHexStr() == address.toHex().getRawHexStr()) {
                        return reg
                    }
                }
            }
        }
        return null
    }

    fun getAllRegs(features: List<Feature>): List<Register> {
        val allRegs = mutableListOf<Register>()
        registerFileList.forEach { allRegs.addAll(it.getRegisters(features)) }
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
    open class Register(
        val address: Variable.Value,
        val names: List<String>,
        val aliases: List<String>,
        val variable: Variable,
        val description: String,
        val callingConvention: CallingConvention = CallingConvention.UNSPECIFIED,
        val needsFeatureID: List<Int>? = null,
        val privilegeID: String? = null,
        val hardwire: Boolean = false,
        val containsFlags: Boolean = false
    ) {

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

        open fun set(value: Variable.Value) {
            if (!hardwire) {
                variable.set(value)
            }
        }

        fun isNeeded(features: List<Feature>): Boolean {
            if (needsFeatureID == null) return true
            for (feature in features) {
                if (feature.isActive() && needsFeatureID.contains(feature.id)) {
                    return true
                }
            }
            return false
        }

        fun getRegexList(): List<Regex> {
            return regexList
        }
    }

    data class PC(val variable: Variable, val initial: Variable.Value) {
        val name = "program counter"
        val shortName = "pc"

        fun get(): Variable.Value = variable.get()

        fun set(value: Variable.Value) {
            variable.set(value)
        }

        fun reset() {
            variable.set(initial)
        }
    }

    data class RegisterFile(
        val name: String, val unsortedRegisters: Array<Register>, val hasPrivileges: Boolean = false
    ) {
        private val registers: Array<Register> = unsortedRegisters.sortedBy { if(it.aliases.isNotEmpty()) null else it.address.input }.toTypedArray()

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other == null || this::class != other::class) return false

            other as RegisterFile

            return name == other.name
        }

        fun getRegisters(features: List<Feature>): List<Register> {
            return registers.filter { it.isNeeded(features) }
        }

        fun clearAll() {
            registers.forEach { it.variable.clear() }
        }

        override fun hashCode(): Int {
            return name.hashCode()
        }

    }

    enum class CallingConvention(val displayName: String) {
        UNSPECIFIED("-"),
        CALLER("CALLER"),
        CALLEE("CALLEE")
    }


}