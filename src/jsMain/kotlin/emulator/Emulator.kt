package emulator

import emulator.kit.Architecture
import emulator.cisc.ArchRV64
import emulator.cisc.ArchRV32

/**
 * This Class Contains the whole Architecture States and is the main connection to the visual view components.
 */
class Emulator {

    /**
     *  The ID which points on the selected Architecture in the archList.
     */
    var selID = -1

    /**
     *  Contains one object of each specific Architecture.
     */
    private val archList: List<Link> = Link.entries.toList()

    fun getArchList(): List<Architecture> {
        return archList.map { it.architecture }
    }

    fun getArch(): Architecture {
        return if (selID in 0..archList.size) {
            archList[selID].architecture
        } else {
            archList[0].architecture
        }
    }
}