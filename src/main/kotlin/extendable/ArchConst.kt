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

    // INSTRUCTION
    const val INSTYPE_INS = "[ins]"
    const val INSTYPE_REGSRC = "[rs]"
    const val INSTYPE_REGDEST = "[rd]"
    const val INSTYPE_ADDRESS = "[address]"
    const val INSTYPE_OFFSETABS = "[absoff]"
    const val INSTYPE_OFFSETREL = "[reloff]"
    const val INSTYPE_IMM = "[const]"
    const val INSTYPE_FLAG = "[flag]"

    // STATES
    const val STATE_UNCHECKED = "unchecked"
    const val STATE_HASERRORS = "hasErrors"
    const val STATE_BUILDABLE = "buildable"
    const val STATE_EXECUTION = "execution"



}