import extendable.Architecture
import extendable.cisc.ArchRV64
import extendable.cisc.ArchRV32

class AppLogic() {

    var selID = -1

    val archRV64: ArchRV64
    var archRV32: ArchRV32

    private val archList: List<Architecture>

    init {
        archRV32 = ArchRV32()
        archRV64 = ArchRV64()
        archList = listOf<Architecture>(archRV32, archRV64)
    }

    fun getArchList(): List<Architecture> {
        return archList
    }

    fun getArch(): Architecture {
        if (selID in 0..archList.size) {
            return archList[selID]
        } else {
            return archList[0]
        }
    }

}