package extendable.archs.riscv

import extendable.ArchConst
import extendable.components.types.ByteValue

object RISCVAsm {

    object Regex {
        val LABEL = Regex("""(.*?):""")
        val COMMENT = Regex("""#\s*(.*$)""")
        val DIRECTIVE = Regex("""\.(.*?)\s+""")
        val MACRO = Regex("""@(\w+)""")
        val INCLUDE = Regex("""#include\s+(.*)""")

        val P_IMM_BIN = Regex("""\s*(${ArchConst.PRESTRING_BINARY}[01]+)\s*""")
        val P_IMM_HEX = Regex("""\s*(${ArchConst.PRESTRING_HEX}[0-9a-f]+)\s*""", RegexOption.IGNORE_CASE)
        val P_IMM_DEC = Regex("""\s*(${ArchConst.PRESTRING_DECIMAL}(-)?[0-9]+)\s*""")
        val P_REG = Regex("""\s*([a-z][a-z0-9]*)\s*""", RegexOption.IGNORE_CASE)
        val P_LABEL = Regex("""\s*([a-z][a-z0-9]*)""")
        val P_SPLIT = Regex("""\s*(,)\s*""")

        fun getRegex(name: String): kotlin.text.Regex {
            return kotlin.text.Regex("""(^|\s+)($name)(\s+|$)""")
        }

        fun getICRegex(name: String): kotlin.text.Regex {
            return kotlin.text.Regex("""(^|\s+)($name)(\s+|$)""", RegexOption.IGNORE_CASE)
        }

        fun getImm(string: String): ImmResult? {
            val binResult = P_IMM_BIN.find(string)
            if (binResult != null) {
                return ImmResult(binResult, ByteValue.Type.Binary(binResult.groupValues[1], ByteValue.Size.Int()))
            }
            val hexResult = P_IMM_HEX.find(string)
            if (hexResult != null) {
                return ImmResult(hexResult, ByteValue.Type.Hex(hexResult.groupValues[1], ByteValue.Size.Int()))
            }
            val decResult = P_IMM_DEC.find(string)
            if (decResult != null) {
                return ImmResult(decResult, ByteValue.Type.Dec(decResult.groupValues[1], ByteValue.Size.Int()))
            }
            return null
        }

        data class ImmResult(val matchResult: MatchResult, val value: ByteValue.Type)

    }


}