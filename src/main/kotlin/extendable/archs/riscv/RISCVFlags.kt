package extendable.archs.riscv

import extendable.archs.Flags

object RISCVFlags: Flags() {

    val prefixFlag = getOrangeFlag()
    val instrFlag = getOrangeFlag()
    val addressFlag = getGreenFlag()
    val valueFlag = getCyanFlag()
    val errorFlag = getErrorFlag()

}