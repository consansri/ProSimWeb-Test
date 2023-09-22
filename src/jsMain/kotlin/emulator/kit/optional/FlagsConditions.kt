package emulator.kit.optional

class FlagsConditions(val flags: List<Flag>, val conditions: List<Condition>) {

    init {
        refreshFlags()
    }

    fun setFlag(flag: Flag, value: Boolean) {
        val changed = flag.setValue(value)
        if (changed) {
            refreshFlags()
        }
    }

    fun findFlag(name: String): Flag? {
        for (flag in flags) {
            if (flag.name == name) {
                return flag
            }
        }
        console.warn("FlagsConditions.findCondition: Couldn't find Condition (name: '$name')")
        return null
    }

    fun findCondition(name: String): Condition? {
        for (cond in conditions) {
            if (cond.name == name) {
                return cond
            }
        }
        console.warn("FlagsConditions.findFlag: Couldn't find Flag (name: '$name')")
        return null
    }

    private fun refreshFlags() {
        for (flag in conditions) {
            flag.check(this)
        }
    }

    class Flag(val name: String, val description: String) {
        private var value: Boolean = false
        fun setValue(value: Boolean): Boolean {
            /*
            * !! NOT USE THIS !!
            * (Manually Flag Refresh Necessary)
            * Use FlagsConditions.setFlag(condition: Condition, value: Boolean) instead            *
            */

            var changed = false
            if (value != this.value) {
                this.value = value
                changed = true
            }
            return changed
        }

        fun getValue(): Boolean {
            return value
        }
    }

    class Condition(val name: String, val description: String, val calc: (FlagsConditions) -> Boolean) {
        private var value: Boolean = false
        fun check(flagsConditions: FlagsConditions): Boolean {
            return calc(flagsConditions)
        }

        fun getValue(): Boolean {
            return value
        }
    }

}