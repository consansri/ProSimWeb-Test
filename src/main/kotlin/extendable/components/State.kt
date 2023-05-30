package extendable.components

import extendable.ArchConsts

class State {

    private val states = listOf(ArchConsts.STATE_UNCHECKED, ArchConsts.STATE_HASERRORS, ArchConsts.STATE_BUILDABLE, ArchConsts.STATE_EXECUTION)
    private var stateID = states.indexOf(ArchConsts.STATE_UNCHECKED)

    fun getState(): String {
        return states[stateID]
    }

    fun build() {
        if (states[stateID] == ArchConsts.STATE_BUILDABLE) {
            stateID = states.indexOf(ArchConsts.STATE_EXECUTION)
        }
    }

    fun edit() {
        stateID = states.indexOf(ArchConsts.STATE_UNCHECKED)
    }

    fun check(success: Boolean) {
        if (states[stateID] == ArchConsts.STATE_UNCHECKED) {
            if (success) {
                stateID = states.indexOf(ArchConsts.STATE_BUILDABLE)
            } else {
                stateID = states.indexOf(ArchConsts.STATE_HASERRORS)
            }
        }
    }


}