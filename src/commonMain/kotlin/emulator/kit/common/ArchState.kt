package emulator.kit.common

/**
 * This class solves as a state manager for the input, in the code editor. A distinction is made between the 3 different [State]: [State.UNCHECKED], [State.HASERRORS] and [State.EXECUTABLE].
 */
class ArchState {

    var state = State.UNCHECKED

    fun getState(): State {
        return state
    }

    fun edit() {
        state = State.UNCHECKED
    }

    fun check(success: Boolean) {
        if (state == State.UNCHECKED) {
            state = if (success) {
                State.EXECUTABLE
            } else {
                State.HASERRORS
            }
        }
    }

    enum class State {
        UNCHECKED,
        HASERRORS,
        EXECUTABLE,
        EXECUTION
    }


}