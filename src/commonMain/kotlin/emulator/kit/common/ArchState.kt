package emulator.kit.common

/**
 * This class solves as a state manager for the input, in the code editor. A distinction is made between the 3 different [State]: [State.UNCHECKED], [State.HASERRORS] and [State.EXECUTABLE].
 */
@Deprecated("Currently is only used by the JS CodeEditor. Will be removed soon.")
class ArchState {

    var currentState = State.UNCHECKED

    fun edit() {
        currentState = State.UNCHECKED
    }

    fun check(success: Boolean) {
        if (currentState == State.UNCHECKED) {
            currentState = if (success) {
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