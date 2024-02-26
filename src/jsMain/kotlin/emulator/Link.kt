package emulator

import emulator.archs.*
import emulator.kit.Architecture

/**
 *  This enum-class contains every specific Architecture
 *  <NEEDS TO BE EXTENDED>
 */
enum class Link(val architecture: emulator.kit.Architecture) {
    RV32I(emulator.archs.ArchRV32()),
    RV64I(emulator.archs.ArchRV64()),
    T6502(ArchT6502()),
    IKRMINI(emulator.archs.ArchIKRMini());
}