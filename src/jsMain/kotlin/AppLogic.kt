import extendable.Architecture
import extendable.cisc.ArchRV64
import extendable.cisc.ArchRV32

class AppLogic {

    var selID = -1

    /**
     *  Extend Architecture here to make them accesible in in the app
     */

    private val archRV64: ArchRV64 = ArchRV64()
    private var archRV32: ArchRV32 = ArchRV32()

    private val archList: List<Architecture> = listOf(archRV32, archRV64)

    /**
     *
     */

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