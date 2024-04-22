import emulator.kit.assembly.Compiler


/**
 * Settings for the kit.
 */
object Settings {
    // GLOBAL
    // FILEHANDLER
    /**
     * [UNDO_STATE_MAX] defines the amount of undo states which can be held until the first state will be removed.
     */
    const val UNDO_STATE_MAX = 32

    /**
     * [REDO_STATE_MAX] defines the amount of redo states which can be held until the first state will be removed.
     */
    const val REDO_STATE_MAX = 32

    /**
     * [UNDO_DELAY_MILLIS] define the time of no edit which is needed to define a new undo state.
     */
    const val UNDO_DELAY_MILLIS = 1000L

    /**
     * [EDITOR_MAX_ANALYSIS_MILLIS] defines the time which will be waited after the last edit before the syntax will be checked.
     */
    const val EDITOR_MAX_ANALYSIS_MILLIS = 2000

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

}