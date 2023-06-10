package extendable.cisc

import extendable.Architecture
import extendable.archs.mini.Mini

class ArchMini : Architecture {

    constructor() : super(Mini.config) {

    }

    override fun exeContinuous() {
        super.exeContinuous()
        val flag = getFlagsConditions()?.findFlag("Carry")
        if (flag != null) {
            getFlagsConditions()?.setFlag(flag, !flag.getValue())
            console.log("Flag ${flag?.name} ${flag?.getValue()}")
        }




    }

}