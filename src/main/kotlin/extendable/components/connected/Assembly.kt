package extendable.components.connected

class Assembly {


    sealed class Token(val lineLoc: LineLoc, val content: String, val valid: Boolean) {

        class Comment(lineLoc: LineLoc, content: String) : Token(lineLoc, content, true) {

        }

        class Inst(lineLoc: LineLoc, content: String, valid: Boolean, val params: List<Param>) : Token(lineLoc, content, valid) {

        }

        class Directive(lineLoc: LineLoc, content: String) : Token(lineLoc, content, true) {

        }

        class Label(lineLoc: LineLoc, content: String) : Token(lineLoc, content, true) {

        }

        class Var(lineLoc: LineLoc, content: String) : Token(lineLoc, content, true) {

        }

        class DataArea(lineLoc: LineLoc, content: String) : Token(lineLoc, content, true) {

        }

        class Macro(lineLoc: LineLoc, content: String) : Token(lineLoc, content, true) {

        }
    }

    data class LineLoc(val lineID: Int, val startIndex: Int, val endIndex: Int)

    sealed class Param {

        class Reg(val reg: RegisterContainer.Register) : Param()

        class Label() : Param()

        class Shift() : Param()

        class Address() : Param()

    }

    object Regex {
        val LABEL = Regex("""(.*?):""")
        val COMMENT = Regex("#.*$")
        val DIRECTIVE = Regex("""\.(.*?)\s""")
        val MACRO = Regex("""@(\w+)""")
        val INCLUDE = Regex("""#include\s+(.*)""")
    }


}