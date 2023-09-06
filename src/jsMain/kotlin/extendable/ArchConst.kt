package extendable

import StyleConst.Main.Editor.HL
import extendable.components.assembly.Compiler

object ArchConst {
    /*
     *    !! use this Object only in Development Phase to test instances even if other Instances are not integrated yet !!
     */

    /*
        NOT OVERRIDABLE!
     */

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

        val COMPILER_COLL = Compiler.HLFlagCollection(
            alphaNum = alphaNum,
            word = word,
            const_hex = hex,
            const_bin = bin,
            const_dec = dec,
            const_udec = udec,
            const_ascii = ascii,
            const_string = string,
            register = register,
            symbol = symbol,
            instruction = instruction,
            comment = comment,
            //whitespace = whiteSpace
        )
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

    // TRANSCRIPT PARAM SPLIT SYMBOL
    val TRANSCRIPT_PARAMSPLIT = ",\t"

    // ADDRESS
    const val hex = 0b10111001

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

    enum class TranscriptHeaders{
        addr,
        label,
        instr,
        params
    }

    enum class RegTypes {
        HEX,
        BIN,
        DEC,
        UDEC
    }

}