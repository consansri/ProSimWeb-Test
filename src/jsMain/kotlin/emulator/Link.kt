package emulator

import emulator.archs.ArchRV32
import emulator.archs.ArchRV64
import emulator.kit.Architecture

/**
 *  This enum-class contains every specific Architecture
 *  <NEEDS TO BE EXTENDED>
 */
enum class Link(val architecture: Architecture) {
    RV32I(ArchRV32()),
    RV64I(ArchRV64());
}