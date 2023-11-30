package emulator.kit.optional

class Feature(val id: Int, val name: String, private var activated: Boolean, val static: Boolean, val invisible: Boolean = false, val descr: String = "") {

    fun activate() {
        if (!static) activated = true
    }

    fun deactivate() {
        if (!static) activated = false
    }

    fun switch(){
        if(!static) activated = !activated
    }

    fun isActive(): Boolean = activated

}