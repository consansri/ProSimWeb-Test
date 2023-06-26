package extendable

import extendable.components.connected.Instruction

object ArchConst {
    /*
     *    !! use this Object only in Development Phase to test instances even if other Instances are not integrated yet !!
     */
    val TRANSCRIPT_HEADERS = arrayOf("Address", "Line", "Code", "Labels", "Instruction")


    /*
        NOT OVERRIDABLE!
     */

    // REGISTER
    val REGISTER_HEADERS = arrayOf(RegHeaders.ADDRESS, RegHeaders.NAME, RegHeaders.VALUE, RegHeaders.DESCRIPTION)
    val REGISTER_VALUETYPES = arrayOf(RegTypes.BIN, RegTypes.HEX, RegTypes.UDEC, RegTypes.DEC)
    val REGISTER_LABEL_PC = "pc"
    val REGISTER_LABEL_MAIN = "main"

    // ADDRESS
    const val ADDRESS_NOVALUE = -1L

    // EXTENSION, OPLABE and REGEX TYPES
    const val EXTYPE_LABEL = "[label]"
    const val EXTYPE_REGISTER = "[reg]"
    const val EXTYPE_IMMEDIATE = "[imm]"
    const val EXTYPE_ADDRESS = "[addr]"
    const val EXTYPE_SHIFT = "[shift]"

    // TYPE IDENTIFICATION
    const val PRESTRING_COMMENT = "#"
    const val PRESTRING_HEX = "0x"
    const val PRESTRING_BINARY = "0b"
    const val PRESTRING_DECIMAL = ""
    const val POSTSTRING_JLABEL = ":"

    // REGEX SPLITTER
    val LINEBREAKS = listOf("\n", "\r", "\r\n")

    // STATES
    const val STATE_UNCHECKED = "unchecked"
    const val STATE_HASERRORS = "hasErrors"
    const val STATE_BUILDABLE = "buildable"
    const val STATE_EXECUTION = "execution"

    enum class RegHeaders {
        ADDRESS,
        NAME,
        VALUE,
        DESCRIPTION
    }

    enum class RegTypes {
        HEX,
        BIN,
        DEC,
        UDEC
    }

}