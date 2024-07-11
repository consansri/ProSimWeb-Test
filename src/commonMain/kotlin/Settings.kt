

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
    const val UNDO_DELAY_MILLIS = 500L

    /**
     * [EDITOR_MAX_ANALYSIS_MILLIS] defines the time which will be waited after the last edit before the syntax will be checked.
     */
    const val EDITOR_MAX_ANALYSIS_MILLIS = 2000

    // TYPE IDENTIFICATION
    /**
     * [PRESTRING_HEX] defines the prefix of hexadecimal values.
     */
    const val PRESTRING_HEX = "0x"

    /**
     * [PRESTRING_OCT] defines the prefix of octal values.
     */
    const val PRESTRING_OCT = "0"

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

    /**
     * [LOCAL_SYMBOL_PREFIX]
     */
    const val LOCAL_SYMBOL_PREFIX: String = "L"

}