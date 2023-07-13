package extendable.cisc

import extendable.Architecture
import extendable.archs.cisc.CISC
import extendable.components.assembly.Compiler

class ArchCISC : Architecture {

    constructor() : super(CISC.config, CISC.asmConfig) {

    }

}