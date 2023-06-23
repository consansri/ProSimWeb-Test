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

    // Assembler
    /* REGULAR EXPRESSIONS */
    val extMap = mapOf<Instruction.EXT, Regex>(
        Instruction.EXT.REG to Regex("""\s*(?<reg>[a-zA-Z][a-zA-Z0-9]*)\s*""", RegexOption.IGNORE_CASE),
        Instruction.EXT.IMM to Regex("""\s*(?<imm>((?<hex>0x[0-9a-fA-F]+)|(?<bin>0b[0-1]+)|(?<dec>(-)?[0-9]+)))\s*""", RegexOption.IGNORE_CASE),
        Instruction.EXT.LABEL to Regex("""\s*(?<lbl>[a-zA-Z0-9]+)\s*""", RegexOption.IGNORE_CASE),
        Instruction.EXT.SHIFT to Regex("""\s*(?<shift>(-)?[0-9]+)\s*"""),
        Instruction.EXT.ADDRESS to Regex("""\s*(?<shift>(-)?[0-9]+)\s*""")
    )


}