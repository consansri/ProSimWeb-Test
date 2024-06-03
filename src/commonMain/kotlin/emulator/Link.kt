package emulator

import emulator.archs.ArchIKRMini
import emulator.archs.ArchRV32
import emulator.archs.ArchRV64
import emulator.archs.ArchT6502
import emulator.kit.Architecture
import emulator.kit.configs.Config
import emulator.kit.nativeLog
import kotlin.reflect.KClass

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
        nativeLog("Loading Arch")
        arch.resetMicroArch()
        return arch
    }

    fun descr(): Config.Description{
        return arch.description
    }

    fun classType(): KClass<*> = arch::class

    override fun toString(): String {
        return descr().fullName
    }

}