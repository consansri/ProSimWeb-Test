package emulator

import emulator.archs.*
import emulator.kit.Architecture
import emulator.kit.configs.Config

/**
 *  This enum-class contains every specific Architecture
 *  <NEEDS TO BE EXTENDED>
 */
enum class Link(private val arch: Architecture) {
    RV32I(ArchRV32()),
    RV64I(ArchRV64()),
    T6502(ArchT6502()),
    IKRMINI(ArchIKRMini());

    fun load(): Architecture{
        arch.resetMicroArch()
        return arch
    }

    fun descr(): Config.Description{
        return arch.description
    }

    override fun toString(): String {
        return descr().fullName
    }

}