import extendable.Architecture
import extendable.cisc.ArchCISC
import extendable.cisc.ArchMini
import extendable.cisc.ArchRISCII
import extendable.cisc.ArchRISCV

class AppLogic {

    var selID = -1

    var testBoolean: Boolean = false

    val archCISC: ArchCISC = ArchCISC()
    val archRISCII: ArchRISCII = ArchRISCII()
    val archRISCV: ArchRISCV = ArchRISCV()
    val archMini: ArchMini = ArchMini()

    private val archList = listOf<Architecture>(archMini, archCISC, archRISCII, archRISCV)

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