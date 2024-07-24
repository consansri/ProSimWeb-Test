package emulator

import emulator.archs.*
import emulator.kit.Architecture
import emulator.kit.config.Config
import emulator.kit.nativeLog
import kotlin.reflect.KClass

/**
 *  This enum-class contains every specific Architecture
 *  <NEEDS TO BE EXTENDED>
 */
enum class Link(private val arch: () -> Architecture) {
    RV32I({ ArchRV32() }),
    RV64I({ ArchRV64() }),
    T6502({ ArchT6502() }),
    IKRMINI({ ArchIKRMini() }),
    IKRRISC2({ ArchIKRRisc2() });

    fun load(): Architecture {
        nativeLog("KIT: Loading $name ...")
        val loaded = arch()
        loaded.resetMicroArch()
        return loaded
    }

    fun descr(): Config.Description {
        return arch().description
    }

    fun classType(): KClass<*> = arch::class

    override fun toString(): String {
        return descr().fullName
    }
}