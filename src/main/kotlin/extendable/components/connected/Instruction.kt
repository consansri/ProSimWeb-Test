package extendable.components.connected

import extendable.ArchConst
import extendable.components.types.OpCode

class Instruction(
    val name: String,
    val exFormats: List<String>,
    val opCode: OpCode,
    val pseudoCode: String,
    val description: String,
    val paramSplit: String,
    val logic: (opCodeBinary: String?, extensionWords: List<Ext>?, mem: Memory, registerContainer: RegisterContainer, flagsConditions: FlagsConditions?) -> Boolean
) {

    val regex: Regex

    init {

        val paramRegexTemplate = exFormats.joinToString(paramSplit) {
            when (it) {
                ArchConst.EXTYPE_REGISTER -> "\\s*[a-fA-F0-9]+\\s*"
                ArchConst.EXTYPE_IMMEDIATE -> "\\s*((0x[0-9a-fA-F]+)|((-)?[0-9]+)|(0b[0-1]+))\\s*"
                ArchConst.EXTYPE_LABEL -> "\\s*[a-fA-F0-9]+\\s*"
                ArchConst.EXTYPE_SHIFT -> "\\s*(-)?[0-9]+\\s*"
                else -> "\\s*"
            }
        }
        val insTemplate = "\\s+$name\\s+[params]\\s+"
        val combinedTemplate = insTemplate.replace("[params]", paramRegexTemplate)

        this.regex = Regex(combinedTemplate)

    }

    fun check(line: String, registerContainer: RegisterContainer, mem: Memory): Boolean {
        val matchResult = regex.find(line)

        if (matchResult != null) {
            for (paramID in exFormats.indices) {
                val param1 = matchResult.groupValues[1]

            }
        }

        return true
    }

    fun example(): String {
        val exampleParams = exFormats.joinToString(separator = paramSplit)
        val string = "$name $exampleParams"
        return string
    }

    fun execute(opCodeBinary: String?, extensionWords: List<Ext>?, mem: Memory, registerContainer: RegisterContainer, flagsConditions: FlagsConditions?): Boolean {
        return logic(opCodeBinary, extensionWords, mem, registerContainer, flagsConditions)
    }

    sealed class Ext{
        data class Reg(val name: String) : Ext()
        data class Imm(val imm: Number) : Ext()
    }

}