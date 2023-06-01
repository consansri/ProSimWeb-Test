package extendable.components

import extendable.ArchConst

class ArchState {

    private val states = listOf(ArchConst.STATE_UNCHECKED, ArchConst.STATE_HASERRORS, ArchConst.STATE_BUILDABLE, ArchConst.STATE_EXECUTION)
    private var stateID = states.indexOf(ArchConst.STATE_UNCHECKED)

    fun getState(): String {
        return states[stateID]
    }

    fun build() {
        if (states[stateID] == ArchConst.STATE_BUILDABLE) {
            stateID = states.indexOf(ArchConst.STATE_EXECUTION)
        }
    }

    fun edit() {
        stateID = states.indexOf(ArchConst.STATE_UNCHECKED)
    }

    fun check(success: Boolean) {
        if (states[stateID] == ArchConst.STATE_UNCHECKED) {
            if (success) {
                stateID = states.indexOf(ArchConst.STATE_BUILDABLE)
            } else {
                stateID = states.indexOf(ArchConst.STATE_HASERRORS)
            }
        }
    }


}