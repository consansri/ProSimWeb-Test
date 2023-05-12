import extendable.Architecture
import extendable.archs.ArchCISC
import extendable.archs.ArchMinimalprozessor
import extendable.archs.ArchRISCII
import extendable.archs.ArchRISCV

class AppData {

    var selID = -1

    var testBoolean: Boolean = false

    val archCISC: ArchCISC = ArchCISC()
    val archRISCII: ArchRISCII = ArchRISCII()
    val archRISCV: ArchRISCV = ArchRISCV()
    val archMinimalprozessor: ArchMinimalprozessor = ArchMinimalprozessor()

    private val archList = listOf<Architecture>(archMinimalprozessor, archCISC, archRISCII, archRISCV)

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