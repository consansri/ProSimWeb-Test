package emulator

import emulator.archs.*
import emulator.kit.Architecture
import kotlin.reflect.KClass
import kotlin.reflect.cast
import kotlin.reflect.safeCast

/**
 *  This enum-class contains every specific Architecture
 *  <NEEDS TO BE EXTENDED>
 */
enum class Link(val arch: Architecture) {
    RV32I(ArchRV32()),
    RV64I(ArchRV64()),
    T6502(ArchT6502()),
    IKRMINI(ArchIKRMini());
}