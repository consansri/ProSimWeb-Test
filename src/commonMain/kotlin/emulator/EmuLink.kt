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
enum class EmuLink(private val arch: () -> Architecture<*,*>, val classType: KClass<*>) {
    RV32I({ ArchRV32() }, ArchRV32::class),
    RV64I({ ArchRV64() }, ArchRV64::class),
    T6502({ ArchT6502() }, ArchT6502::class),
    IKRMINI({ ArchIKRMini() }, ArchIKRMini::class),
    IKRRISC2({ ArchIKRRisc2() }, ArchIKRRisc2::class);

    fun load(): Architecture<*,*> {
        nativeLog("KIT: Loading $name ...")
        val loaded = arch()
        loaded.resetMicroArch()
        return loaded
    }

    fun descr(): Config.Description {
        return arch().description
    }

    override fun toString(): String {
        return descr().fullName
    }
}