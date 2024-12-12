package emulator.archs.ikrrisc2

import cengine.lang.asm.ast.target.ikrrisc2.IKRR2Disassembler
import cengine.util.integer.Size.Bit32
import cengine.util.integer.Size.Bit5
import cengine.util.integer.Variable
import emulator.archs.ArchIKRRisc2
import emulator.kit.common.Docs
import emulator.kit.common.RegContainer
import emulator.kit.common.RegContainer.Register
import emulator.kit.common.RegContainer.RegisterFile
import emulator.kit.config.Config
import emulator.kit.memory.*
import emulator.kit.optional.SetupSetting
import emulator.kit.register.FieldProvider

data object IKRRisc2 {

    val WORD_WIDTH = Bit32
    val REG_SIZE = Bit5
    const val REG_INIT = "0"

    const val standardRegFileName = "main"

    object BaseNameProvider : FieldProvider {
        override val name: String = "NAME"

        override fun get(id: Int): String = when (id) {
            in 0..31 -> "r$id"
            else -> ""
        }
    }

    object BaseProvider : FieldProvider {
        override val name: String = "DESCR"
        override fun get(id: Int): String = when (id) {
            1 -> "hardwired zero"
            31 -> "return address"
            else -> ""
        }
    }

    val standardRegFile = RegisterFile(
        standardRegFileName, arrayOf(
            Register(0U, listOf("r0"), listOf(), Variable(REG_INIT, WORD_WIDTH), "hardwired zero", hardwire = true),
            Register(1U, listOf("r1"), listOf(), Variable(REG_INIT, WORD_WIDTH), ""),
            Register(2U, listOf("r2"), listOf(), Variable(REG_INIT, WORD_WIDTH), ""),
            Register(3U, listOf("r3"), listOf(), Variable(REG_INIT, WORD_WIDTH), ""),
            Register(4U, listOf("r4"), listOf(), Variable(REG_INIT, WORD_WIDTH), ""),
            Register(5U, listOf("r5"), listOf(), Variable(REG_INIT, WORD_WIDTH), ""),
            Register(6U, listOf("r6"), listOf(), Variable(REG_INIT, WORD_WIDTH), ""),
            Register(7U, listOf("r7"), listOf(), Variable(REG_INIT, WORD_WIDTH), ""),
            Register(8U, listOf("r8"), listOf(), Variable(REG_INIT, WORD_WIDTH), ""),
            Register(9U, listOf("r9"), listOf(), Variable(REG_INIT, WORD_WIDTH), ""),
            Register(10U, listOf("r10"), listOf(), Variable(REG_INIT, WORD_WIDTH), ""),
            Register(11U, listOf("r11"), listOf(), Variable(REG_INIT, WORD_WIDTH), ""),
            Register(12U, listOf("r12"), listOf(), Variable(REG_INIT, WORD_WIDTH), ""),
            Register(13U, listOf("r13"), listOf(), Variable(REG_INIT, WORD_WIDTH), ""),
            Register(14U, listOf("r14"), listOf(), Variable(REG_INIT, WORD_WIDTH), ""),
            Register(15U, listOf("r15"), listOf(), Variable(REG_INIT, WORD_WIDTH), ""),
            Register(16U, listOf("r16"), listOf(), Variable(REG_INIT, WORD_WIDTH), ""),
            Register(17U, listOf("r17"), listOf(), Variable(REG_INIT, WORD_WIDTH), ""),
            Register(18U, listOf("r18"), listOf(), Variable(REG_INIT, WORD_WIDTH), ""),
            Register(19U, listOf("r19"), listOf(), Variable(REG_INIT, WORD_WIDTH), ""),
            Register(20U, listOf("r20"), listOf(), Variable(REG_INIT, WORD_WIDTH), ""),
            Register(21U, listOf("r21"), listOf(), Variable(REG_INIT, WORD_WIDTH), ""),
            Register(22U, listOf("r22"), listOf(), Variable(REG_INIT, WORD_WIDTH), ""),
            Register(23U, listOf("r23"), listOf(), Variable(REG_INIT, WORD_WIDTH), ""),
            Register(24U, listOf("r24"), listOf(), Variable(REG_INIT, WORD_WIDTH), ""),
            Register(25U, listOf("r25"), listOf(), Variable(REG_INIT, WORD_WIDTH), ""),
            Register(26U, listOf("r26"), listOf(), Variable(REG_INIT, WORD_WIDTH), ""),
            Register(27U, listOf("r27"), listOf(), Variable(REG_INIT, WORD_WIDTH), ""),
            Register(28U, listOf("r28"), listOf(), Variable(REG_INIT, WORD_WIDTH), ""),
            Register(29U, listOf("r29"), listOf(), Variable(REG_INIT, WORD_WIDTH), ""),
            Register(30U, listOf("r30"), listOf(), Variable(REG_INIT, WORD_WIDTH), ""),
            Register(31U, listOf("r31"), listOf(), Variable(REG_INIT, WORD_WIDTH), "return address"),
        )
    )

    val settings = listOf(
        SetupSetting.Enumeration("Instruction Cache", Cache.Setting.entries, Cache.Setting.NONE) { arch, setting ->
            if (arch is ArchIKRRisc2) {
                arch.instrMemory = when (setting.get()) {
                    Cache.Setting.NONE -> arch.memory
                    Cache.Setting.DirectedMapped -> DMCache(arch.memory, CacheSize.KiloByte_32,  "Instruction")
                    Cache.Setting.FullAssociativeRandom -> FACache(arch.memory, CacheSize.KiloByte_32, Cache.ReplaceAlgo.RANDOM,  "Instruction")
                    Cache.Setting.FullAssociativeLRU -> FACache(arch.memory, CacheSize.KiloByte_32, Cache.ReplaceAlgo.LRU,  "Instruction")
                    Cache.Setting.FullAssociativeFIFO -> FACache(arch.memory, CacheSize.KiloByte_32, Cache.ReplaceAlgo.FIFO,  "Instruction")
                    Cache.Setting.SetAssociativeRandom -> SACache(arch.memory, 4, CacheSize.KiloByte_32, Cache.ReplaceAlgo.RANDOM,  "Instruction")
                    Cache.Setting.SetAssociativeLRU -> SACache(arch.memory, 4, CacheSize.KiloByte_32, Cache.ReplaceAlgo.LRU,  "Instruction")
                    Cache.Setting.SetAssociativeFIFO -> SACache(arch.memory, 4, CacheSize.KiloByte_32, Cache.ReplaceAlgo.FIFO,  "Instruction")
                }
            }
        },
        SetupSetting.Enumeration("Data Cache", Cache.Setting.entries, Cache.Setting.NONE) { arch, setting ->
            if (arch is ArchIKRRisc2) {
                arch.dataMemory = when (setting.get()) {
                    Cache.Setting.NONE -> arch.memory
                    Cache.Setting.DirectedMapped -> DMCache(arch.memory, CacheSize.KiloByte_32,  "Data")
                    Cache.Setting.FullAssociativeRandom -> FACache(arch.memory, CacheSize.KiloByte_32, Cache.ReplaceAlgo.RANDOM,  "Data")
                    Cache.Setting.FullAssociativeLRU -> FACache(arch.memory, CacheSize.KiloByte_32, Cache.ReplaceAlgo.LRU,  "Data")
                    Cache.Setting.FullAssociativeFIFO -> FACache(arch.memory, CacheSize.KiloByte_32, Cache.ReplaceAlgo.FIFO,  "Data")
                    Cache.Setting.SetAssociativeRandom -> SACache(arch.memory, 4, CacheSize.KiloByte_32, Cache.ReplaceAlgo.RANDOM,  "Data")
                    Cache.Setting.SetAssociativeLRU -> SACache(arch.memory, 4, CacheSize.KiloByte_32, Cache.ReplaceAlgo.LRU,  "Data")
                    Cache.Setting.SetAssociativeFIFO -> SACache(arch.memory, 4, CacheSize.KiloByte_32, Cache.ReplaceAlgo.FIFO,  "Data")
                }
            }
        }
    )

    val implementationDoc = Docs.DocFile.DefinedFile(
        "IKR RISC-II Implemented",
        Docs.DocComponent.Chapter(
            "Registers",
            Docs.DocComponent.Text("address-width: $REG_SIZE"),
            Docs.DocComponent.Text("value-width: $WORD_WIDTH")
        ),
        Docs.DocComponent.Chapter(
            "Memory",
            Docs.DocComponent.Text("address-width: $WORD_WIDTH"),
            Docs.DocComponent.Text("value-width: $WORD_WIDTH")
        )
    )

    val config = Config(
        Config.Description("IKR RISC-II", "IKR RISC-II", Docs(true, implementationDoc)),
        fileEnding = "s",
        RegContainer(
            listOf(standardRegFile),
            pcSize = WORD_WIDTH,
            standardRegFileName
        ),
        IKRR2Disassembler,
        settings
    )
}