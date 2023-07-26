package extendable.cisc

import extendable.Architecture
import extendable.archs.riscii.RISCII
import extendable.components.assembly.Compiler

class ArchRISCII : Architecture {

    constructor() : super(RISCII.config, RISCII.asmConfig) {

    }

}