package emulator.kit.common

import emulator.kit.Settings

class ArchState {

    private val states = listOf(Settings.STATE_UNCHECKED, Settings.STATE_HASERRORS, Settings.STATE_EXECUTABLE)
    private var stateID = states.indexOf(Settings.STATE_UNCHECKED)

    fun getState(): String {
        return states[stateID]
    }

    fun edit() {
        stateID = states.indexOf(Settings.STATE_UNCHECKED)
    }

    fun check(success: Boolean) {
        if (states[stateID] == Settings.STATE_UNCHECKED) {
            if (success) {
                stateID = states.indexOf(Settings.STATE_EXECUTABLE)
            } else {
                stateID = states.indexOf(Settings.STATE_HASERRORS)
            }
        }
    }


}