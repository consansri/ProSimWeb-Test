package extendable.components.connected

import extendable.ArchConst
import extendable.components.types.ByteValue
import extendable.components.types.OpCode

class Instruction(
    val name: String,
    val exFormats: List<EXT>,
    val opCode: OpCode,
    val pseudoCode: String,
    val description: String,
    val paramSplit: String,
    val logic: (execute: Boolean, opCodeBinary: String?, extensionWords: List<Ext>?, mem: Memory, registerContainer: RegisterContainer, flagsConditions: FlagsConditions?) -> ReturnType
) {

    val nameRegex: Regex
    val splitRegex: Regex
    val paramRegexList: Map<EXT, Regex>

    init {
        val tempParamRegs = mutableMapOf<EXT, Regex>()

        for (format in exFormats) {
            when (format) {
                EXT.REG -> tempParamRegs.put(EXT.REG, Regex("""\s*(?<reg>[a-zA-Z][a-zA-Z0-9]*)\s*""", RegexOption.IGNORE_CASE))
                EXT.IMM -> tempParamRegs.put(EXT.IMM, Regex("""\s*(?<imm>((?<hex>0x[0-9a-fA-F]+)|(?<bin>0b[0-1]+)|(?<dec>(-)?[0-9]+)))\s*""", RegexOption.IGNORE_CASE))
                EXT.LABEL -> tempParamRegs.put(EXT.LABEL, Regex("""\s*(?<lbl>[a-zA-Z0-9]+)\s*""", RegexOption.IGNORE_CASE))
                EXT.SHIFT -> tempParamRegs.put(EXT.SHIFT, Regex("""\s*(?<shift>(-)?[0-9]+)\s*"""))
                EXT.ADDRESS -> tempParamRegs.put(EXT.ADDRESS, Regex("""\s*(?<shift>(-)?[0-9]+)\s*"""))
            }
        }
        paramRegexList = tempParamRegs
        splitRegex = Regex("""$paramSplit""")

        //regex = insTemplate.replace("[params]", paramRegexTemplate).toRegex(RegexOption.IGNORE_CASE)
        nameRegex = Regex("""(^|\s+)($name)(\s+|$)""", RegexOption.IGNORE_CASE)
    }

    fun example(): String {
        val exampleParams = exFormats.joinToString(separator = paramSplit)
        val string = "$name $exampleParams"
        return string
    }

    fun execute(execute: Boolean,opCodeBinary: String?, extensionWords: List<Ext>?, mem: Memory, registerContainer: RegisterContainer, flagsConditions: FlagsConditions?): ReturnType {
        return logic(execute, opCodeBinary, extensionWords, mem, registerContainer, flagsConditions)
    }

    sealed class Ext {
        data class Reg(val name: String) : Ext()
        data class Imm(val imm: ByteValue.Type) : Ext()
    }

    sealed class ReturnType {
        data class BinaryRep(val binaryList: List<ByteValue.Type>) : ReturnType()
        data class ExecutionSuccess(val success: Boolean) : ReturnType()

    }

    data class InsCheckResult(val success: Boolean)

    enum class EXT {
        REG,
        IMM,
        LABEL,
        ADDRESS,
        SHIFT,
    }

}