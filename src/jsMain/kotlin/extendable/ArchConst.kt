package extendable

import StyleAttr.Main.Editor.HL
import extendable.components.assembly.Compiler

object ArchConst {
    // Architecture Assembly Constructor
    object StandardHL {
        val comment = HL.base05.getFlag()
        val register = HL.orange.getFlag()
        val word = HL.magenta.getFlag()
        val alphaNum = HL.violet.getFlag()
        val instruction = HL.blue.getFlag()
        val symbol = HL.cyan.getFlag()
        val bin = HL.blue.getFlag()
        val hex = HL.blue.getFlag()
        val dec = HL.blue.getFlag()
        val udec =HL.blue.getFlag()
        val ascii = HL.green.getFlag()
        val string = HL.green.getFlag()

        val error = HL.red.getFlag()
        val whiteSpace = HL.whitespace.getFlag()


    }

    // GLOBAL
    const val PROSIMNAME = "ProSimWeb"

    // FILEHANDLER
    const val UNDO_STATE_COUNT = 32
    const val REDO_STATE_COUNT = 32
    const val UNDO_DELAY_MILLIS = 1000L

    // COMPILER
    const val COMPILER_TOKEN_PSEUDOID = -100

    // REGISTER
    val REGISTER_VALUETYPES = arrayOf(RegTypes.BIN, RegTypes.HEX, RegTypes.UDEC, RegTypes.DEC)

    // TYPE IDENTIFICATION
    const val PRESTRING_HEX = "0x"
    const val PRESTRING_BINARY = "0b"
    const val PRESTRING_DECIMAL = ""
    const val PRESTRING_UDECIMAL = "u"

    // REGEX SPLITTER
    val LINEBREAKS = listOf("\n", "\r", "\r\n")

    // STATES
    const val STATE_UNCHECKED = "unchecked"
    const val STATE_HASERRORS = "hasErrors"
    const val STATE_EXECUTABLE = "buildable"
    const val STATE_EXECUTION = "execution"

    val COMPILER_REGEX = Compiler.RegexCollection(
        Regex("""^\s+"""),
        Regex("""^[^0-9A-Za-z]"""),
        Regex("""^(-)?${PRESTRING_BINARY}[01]+"""),
        Regex("""^(-)?${PRESTRING_HEX}[0-9a-f]+""", RegexOption.IGNORE_CASE),
        Regex("""^(-)?${PRESTRING_DECIMAL}[0-9]+"""),
        Regex("""^${PRESTRING_UDECIMAL}[0-9]+"""),
        Regex("""^'.'"""),
        Regex("""^".+""""),
        Regex("""^[a-z][a-z0-9]*""", RegexOption.IGNORE_CASE),
        Regex("""^[a-z]+""", RegexOption.IGNORE_CASE)
    )

    val COMPILER_HLCOLL = Compiler.HLFlagCollection(
        alphaNum = StandardHL.alphaNum,
        word = StandardHL.word,
        const_hex = StandardHL.hex,
        const_bin = StandardHL.bin,
        const_dec = StandardHL.dec,
        const_udec = StandardHL.udec,
        const_ascii = StandardHL.ascii,
        const_string = StandardHL.string,
        register = StandardHL.register,
        symbol = StandardHL.symbol,
        instruction = StandardHL.instruction,
        comment = StandardHL.comment,
        //whitespace = whiteSpace
    )

    enum class RegTypes {
        HEX,
        BIN,
        DEC,
        UDEC
    }

}