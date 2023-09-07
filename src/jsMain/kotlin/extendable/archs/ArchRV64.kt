package extendable.cisc

import extendable.Architecture
import extendable.archs.riscv64.RV64

class ArchRV64 : Architecture {

    constructor() : super(RV64.config, RV64.asmConfig) {

    }

}