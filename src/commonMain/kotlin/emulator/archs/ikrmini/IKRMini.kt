package emulator.archs.ikrmini

import cengine.lang.asm.ast.target.ikrmini.IKRMiniDisassembler
import cengine.util.integer.Size.*
import cengine.util.integer.Variable
import emulator.archs.ArchIKRMini
import emulator.kit.common.Docs
import emulator.kit.common.Docs.DocComponent.Chapter
import emulator.kit.common.Docs.DocComponent.Text
import emulator.kit.common.RegContainer
import emulator.kit.config.Config
import emulator.kit.memory.*
import emulator.kit.optional.SetupSetting

data object IKRMini {

    val BYTESIZE = Bit8
    val WORDSIZE = Bit16
    val MEM_ADDRESS_WIDTH = WORDSIZE

    val descr = Config.Description(
        "IKR Mini",
        "IKR Minimalprozessor",
        Docs(
            usingProSimAS = true,
            Docs.DocFile.DefinedFile(
                "IKR Mini Implemented",
                Chapter(
                    "Memory",
                    Text("address-width: $MEM_ADDRESS_WIDTH"),
                    Text("value-width: $BYTESIZE")
                )
            ),
        )
    )

    val settings = listOf(
        SetupSetting.Enumeration("Instruction Cache", Cache.Setting.entries, Cache.Setting.NONE) { arch, setting ->
            if (arch is ArchIKRMini) {
                arch.instrMemory = when (setting.get()) {
                    Cache.Setting.NONE -> arch.memory
                    Cache.Setting.DirectedMapped -> DMCache(arch.memory, arch.console, CacheSize.KiloByte_32, "Instruction")
                    Cache.Setting.FullAssociativeRandom -> FACache(arch.memory, arch.console, CacheSize.KiloByte_32, Cache.Model.ReplaceAlgo.RANDOM, "Instruction")
                    Cache.Setting.FullAssociativeLRU -> FACache(arch.memory, arch.console, CacheSize.KiloByte_32, Cache.Model.ReplaceAlgo.LRU, "Instruction")
                    Cache.Setting.FullAssociativeFIFO -> FACache(arch.memory, arch.console, CacheSize.KiloByte_32, Cache.Model.ReplaceAlgo.FIFO, "Instruction")
                    Cache.Setting.SetAssociativeRandom -> SACache(arch.memory, arch.console,4, CacheSize.KiloByte_32, Cache.Model.ReplaceAlgo.RANDOM, "Instruction")
                    Cache.Setting.SetAssociativeLRU -> SACache(arch.memory, arch.console, 4, CacheSize.KiloByte_32, Cache.Model.ReplaceAlgo.LRU, "Instruction")
                    Cache.Setting.SetAssociativeFIFO -> SACache(arch.memory, arch.console, 4, CacheSize.KiloByte_32, Cache.Model.ReplaceAlgo.FIFO, "Instruction")
                }
            }
        },
        SetupSetting.Enumeration("Data Cache", Cache.Setting.entries, Cache.Setting.NONE) { arch, setting ->
            if (arch is ArchIKRMini) {
                arch.dataMemory = when (setting.get()) {
                    Cache.Setting.NONE -> arch.memory
                    Cache.Setting.DirectedMapped -> DMCache(arch.memory, arch.console, CacheSize.KiloByte_32, "Data")
                    Cache.Setting.FullAssociativeRandom -> FACache(arch.memory, arch.console, CacheSize.KiloByte_32, Cache.Model.ReplaceAlgo.RANDOM, "Data")
                    Cache.Setting.FullAssociativeLRU -> FACache(arch.memory, arch.console, CacheSize.KiloByte_32, Cache.Model.ReplaceAlgo.LRU, "Data")
                    Cache.Setting.FullAssociativeFIFO -> FACache(arch.memory, arch.console, CacheSize.KiloByte_32, Cache.Model.ReplaceAlgo.FIFO, "Data")
                    Cache.Setting.SetAssociativeRandom -> SACache(arch.memory, arch.console,4, CacheSize.KiloByte_32, Cache.Model.ReplaceAlgo.RANDOM, "Data")
                    Cache.Setting.SetAssociativeLRU -> SACache(arch.memory, arch.console, 4, CacheSize.KiloByte_32, Cache.Model.ReplaceAlgo.LRU, "Data")
                    Cache.Setting.SetAssociativeFIFO -> SACache(arch.memory, arch.console, 4, CacheSize.KiloByte_32, Cache.Model.ReplaceAlgo.FIFO, "Data")
                }
            }
        }
    )

    val config = Config(
        descr,
        fileEnding = "s",
        RegContainer(
            listOf(
                RegContainer.RegisterFile(
                    "common", arrayOf(
                        RegContainer.Register(0U, listOf("AC"), listOf(), Variable("0", WORDSIZE), description = "Accumulator"),
                        RegContainer.Register(1U, listOf("NZVC"), listOf(), Variable("0", Bit4), description = "NZVC ALU flags", containsFlags = true)
                    )
                )
            ),
            WORDSIZE,
            "common"
        ),
        MainMemory(WORDSIZE, WORDSIZE, Memory.Endianess.BigEndian),
        IKRMiniDisassembler,
        settings
    )
}