package extendable

object ArchConst {
    /*
     *    !! use this Object only in Development Phase to test instances even if other Instances are not integrated yet !!
     */
    val TRANSCRIPT_HEADERS = arrayOf("Address", "Line", "Code", "Labels", "Instruction")


    /*
        NOT OVERRIDABLE!
     */

    // REGISTER
    val REGISTER_HEADERS = arrayOf("Address", "Name", "Data", "Description")
    const val REGISTER_NOVALUE = -1

    // EXTENSION TYPES
    const val EXTYPE_REGISTER = "[reg]"
    const val EXTYPE_IMMEDIATE = "[imm]"
    const val EXTYPE_ADDRESS = "[addr]"
    const val EXTYPE_JUMPADDRESS = "[jaddr]"
    const val EXTYPE_CSR = "[csr]" // CONTROL AND STATUS REGISTER
    const val EXTYPE_SHIFT = "[shift]"

    // STATES
    const val STATE_UNCHECKED = "unchecked"
    const val STATE_HASERRORS = "hasErrors"
    const val STATE_BUILDABLE = "buildable"
    const val STATE_EXECUTION = "execution"



}