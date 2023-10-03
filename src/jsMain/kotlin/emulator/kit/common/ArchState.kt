package emulator.kit.common

import emulator.kit.Settings

/**
 * This class solves as a state manager for the input, in the code editor. A distinction is made between the 3 different states: [Settings.STATE_UNCHECKED], [Settings.STATE_HASERRORS] and [Settings.STATE_EXECUTABLE].
 */
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