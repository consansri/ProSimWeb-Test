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

    val nameRegex: Regex
    val paramRegexList: Map<String, Regex>

    init {

        val tempParamRegs = mutableMapOf<String, Regex>()

        for (format in exFormats) {
            when (format) {
                ArchConst.EXTYPE_REGISTER -> tempParamRegs.put(ArchConst.EXTYPE_REGISTER, Regex("""\s*(?<reg>[a-zA-Z][a-zA-Z0-9]*)\s*""", RegexOption.IGNORE_CASE))
                ArchConst.EXTYPE_IMMEDIATE -> tempParamRegs.put(ArchConst.EXTYPE_IMMEDIATE, Regex("""\s*(?<imm>((?<hex>0x[0-9a-fA-F]+)|(?<bin>0b[0-1]+)|(?<dec>(-)?[0-9]+)))\s*""", RegexOption.IGNORE_CASE))
                ArchConst.EXTYPE_LABEL -> tempParamRegs.put(ArchConst.EXTYPE_LABEL, Regex("""\s*(?<lbl>[a-zA-Z0-9]+)\s*""", RegexOption.IGNORE_CASE))
                ArchConst.EXTYPE_SHIFT -> tempParamRegs.put(ArchConst.EXTYPE_SHIFT, Regex("""\s*(?<shift>(-)?[0-9]+)\s*"""))
                else -> tempParamRegs.put("", Regex("""\s*"""))
            }
        }
        paramRegexList = tempParamRegs


        //regex = insTemplate.replace("[params]", paramRegexTemplate).toRegex(RegexOption.IGNORE_CASE)
        nameRegex = Regex("""(^|\s+)($name)(\s+|$)""", RegexOption.IGNORE_CASE)

        console.log(paramRegexList)
    }

    fun check(line: String, registerContainer: RegisterContainer, mem: Memory): Boolean {
        val matchResult = nameRegex.find(line)

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

    sealed class Ext {
        data class Reg(val name: String) : Ext()
        data class Imm(val imm: Number) : Ext()
    }

    data class InsCheckResult(val success: Boolean)

    enum class EXT{
        REG,
        IMM,
        ADDRESS,



    }

}