package extendable.cisc

import extendable.Architecture
import extendable.archs.mini.Mini
import extendable.components.assembly.Compiler

class ArchMini : Architecture {

    constructor() : super(Mini.config, Mini.asmConfig) {

    }

    override fun exeContinuous() {
        super.exeContinuous()
        val flag = getFlagsConditions()?.findFlag("Carry")
        if (flag != null) {
            getFlagsConditions()?.setFlag(flag, !flag.getValue())
            console.log("Flag ${flag.name} ${flag.getValue()}")
        }
    }



}