package emulator.kit

import Constants
import StyleAttr.Main.Editor.HL
import emulator.kit.assembly.Compiler


/**
 * Settings for the kit.
 */
object Settings {
    // GLOBAL
    /**
     * [PROSIMNAME] defines the name of the app.
     */
    const val PROSIMNAME = Constants.NAME

    // FILEHANDLER
    /**
     * [UNDO_STATE_COUNT] defines the amount of undo states which can be held until the first state will be removed.
     */
    const val UNDO_STATE_COUNT = 32

    /**
     * [REDO_STATE_COUNT] defines the amount of redo states which can be held until the first state will be removed.
     */
    const val REDO_STATE_COUNT = 32

    /**
     * [UNDO_DELAY_MILLIS] define the time of no edit which is needed to define a new undo state.
     */
    const val UNDO_DELAY_MILLIS = 1000

    // COMPILER
    /**
     * [COMPILER_TOKEN_PSEUDOID] defines the pseudo id of compiler tokens which have no real text representation.
     * They are generated through the [Compiler.pseudoTokenize] function which is used to analyze code which is for example generated through a macro.
     */
    const val COMPILER_TOKEN_PSEUDOID = -100

    // TYPE IDENTIFICATION
    /**
     * [PRESTRING_HEX] defines the prefix of hexadecimal values.
     */
    const val PRESTRING_HEX = "0x"

    /**
     * [PRESTRING_BINARY] defines the prefix of binary values.
     */
    const val PRESTRING_BINARY = "0b"

    /**
     * [PRESTRING_DECIMAL] defines the prefix of decimal values.
     */
    const val PRESTRING_DECIMAL = ""

    /**
     * [PRESTRING_UDECIMAL] defines the prefix of unsigned decimal values.
     */
    const val PRESTRING_UDECIMAL = "u"

    // REGEX SPLITTER
    /**
     * [LINEBREAKS] provides all possible occuring linebreak characters.
     */
    val LINEBREAKS = listOf("\n", "\r", "\r\n")

    /**
     * [COMPILER_HLCOLL] defines common highlighting colors for each [Compiler.Token].
     * Those will be overwritten by architecture specific Highlighting.
     */
    val COMPILER_HLCOLL = Compiler.HLFlagCollection(
        alphaNum = HL.violet.getFlag(),
        word = HL.magenta.getFlag(),
        constHex = HL.blue.getFlag(),
        constBin = HL.blue.getFlag(),
        constDec = HL.blue.getFlag(),
        constUDec = HL.blue.getFlag(),
        constAscii = HL.green.getFlag(),
        constString = HL.green.getFlag(),
        register = HL.orange.getFlag(),
        symbol = HL.cyan.getFlag(),
        instruction = HL.blue.getFlag(),
        comment = HL.base05.getFlag(),
        //whitespace = HL.whitespace.getFlag(),
        error = HL.red.getFlag()
    )

}