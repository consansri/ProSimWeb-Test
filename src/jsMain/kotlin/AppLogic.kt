import extendable.Architecture
import extendable.cisc.ArchCISC
import extendable.cisc.ArchMini
import extendable.cisc.ArchRISCII
import extendable.cisc.ArchRISCV

class AppLogic() {

    var selID = -1

    val archCISC: ArchCISC
    val archRISCII: ArchRISCII
    var archRISCV: ArchRISCV
    val archMini: ArchMini

    private val archList: List<Architecture>

    init {
        archRISCV = ArchRISCV()
        archCISC = ArchCISC()
        archRISCII = ArchRISCII()
        archMini = ArchMini()
        archList = listOf<Architecture>(archRISCV, archMini, archCISC, archRISCII)
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