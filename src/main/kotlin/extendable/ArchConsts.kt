package extendable

object ArchConsts {
    /*
     *    !! use this Object only in Development Phase to test instances even if other Instances are not integrated yet !!
     */
    val TRANSCRIPT_HEADERS = arrayOf("Address", "Line", "Code", "Labels", "Instruction")


    /*
        NOT OVERRIDABLE!
     */

    // REGISTER
    val REGISTER_HEADERS = arrayOf("Address", "Name", "Data", "Description")
    val REGISTER_NOVALUE = -1

    // STATES
    const val STATE_UNCHECKED = "unchecked"
    const val STATE_HASERRORS = "hasErrors"
    const val STATE_BUILDABLE = "buildable"
    const val STATE_EXECUTION = "execution"

    // EXECUTION START LINE
    const val LINE_NOLINE = -1



}