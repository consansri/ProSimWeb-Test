package extendable.components

import extendable.ArchConst

class ArchState {

    private val states = listOf(ArchConst.STATE_UNCHECKED, ArchConst.STATE_HASERRORS, ArchConst.STATE_EXECUTABLE)
    private var stateID = states.indexOf(ArchConst.STATE_UNCHECKED)

    fun getState(): String {
        return states[stateID]
    }

    fun edit() {
        stateID = states.indexOf(ArchConst.STATE_UNCHECKED)
    }

    fun check(success: Boolean) {
        if (states[stateID] == ArchConst.STATE_UNCHECKED) {
            if (success) {
                stateID = states.indexOf(ArchConst.STATE_EXECUTABLE)
            } else {
                stateID = states.indexOf(ArchConst.STATE_HASERRORS)
            }
        }
    }


}