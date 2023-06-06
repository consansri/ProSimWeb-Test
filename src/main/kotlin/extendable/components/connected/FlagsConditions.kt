package extendable.components.connected

class FlagsConditions(val flags: List<Flag>, val conditions: List<Condition>) {



    class Flag(val name: String, val description: String, var value: Boolean) {

    }

    class Condition(val name: String, val description: String, var value: Boolean) {

    }

}