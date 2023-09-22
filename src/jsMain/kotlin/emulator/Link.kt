package emulator

import emulator.archs.riscv32.RV32
import emulator.cisc.ArchRV32
import emulator.cisc.ArchRV64
import emulator.kit.Architecture

/**
 *  This class contains every specific Architecture
 *  <NEEDS TO BE EXTENDED>
 */
enum class Link(val architecture: Architecture) {
    RV32I(ArchRV32()),
    RV64I(ArchRV64())
}