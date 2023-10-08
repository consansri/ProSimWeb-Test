package emulator.kit.common

/**
 * This class solves as a state manager for the input, in the code editor. A distinction is made between the 3 different [State]: [State.UNCHECKED], [State.HASERRORS] and [State.EXECUTABLE].
 */
class ArchState {

    private var state = State.UNCHECKED

    fun getState(): State {
        return state
    }

    fun edit() {
        state = State.UNCHECKED
    }

    fun check(success: Boolean) {
        if (state == State.UNCHECKED) {
            if (success) {
                state = State.EXECUTABLE
            } else {
                state = State.HASERRORS
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