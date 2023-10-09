package emulator.cisc

import emulator.kit.Architecture
import emulator.archs.riscv64.RV64
import emulator.kit.types.Variable
import kotlin.time.measureTime

class ArchRV64 : Architecture {

    constructor() : super(RV64.config, RV64.asmConfig) {

    }


}