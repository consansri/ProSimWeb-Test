package emulator.archs.t6502


import cengine.util.integer.Size.Bit16
import cengine.util.integer.Size.Bit8
import cengine.util.integer.Variable
import emulator.archs.ArchT6502
import emulator.kit.common.Docs
import emulator.kit.common.Docs.DocComponent.*
import emulator.kit.common.RegContainer
import emulator.kit.config.Config
import emulator.kit.memory.*
import emulator.kit.optional.SetupSetting


/**
 * MOS Technology 6502 Configuration
 *
 * Sources:
 * - https://en.wikibooks.org/wiki/6502_Assembly
 * - https://en.wikipedia.org/wiki/MOS_Technology_6502
 * - https://www.masswerk.at/6502/6502_instruction_set.html
 *
 *
 * Comparison:
 * - https://www.masswerk.at/6502/assembler.html
 */
data object T6502 {

    val WORD_SIZE = Bit16
    val BYTE_SIZE = Bit8

    val MEM_ADDR_SIZE = WORD_SIZE

    val commonRegFile = RegContainer.RegisterFile(
        name = "common",
        unsortedRegisters = arrayOf(
            RegContainer.Register(0U, listOf("AC"), listOf(), Variable("00000000", BYTE_SIZE), description = "accumulator"),
            RegContainer.Register(1U, listOf("X"), listOf(), Variable("00000000", BYTE_SIZE), description = "X register"),
            RegContainer.Register(2U, listOf("Y"), listOf(), Variable("00000000", BYTE_SIZE), description = "Y register"),
            RegContainer.Register(3U, listOf("SR"), listOf(), Variable("00100000", BYTE_SIZE), description = "status register [NV-BDIZC]", containsFlags = true),
            RegContainer.Register(4U, listOf("SP"), listOf(), Variable("11111111", BYTE_SIZE), description = "stack pointer")
        )
    )

    val description = Config.Description(
        "T6502",
        "MOS Technology 6502",
        Docs(
            usingProSimAS = true,
            Docs.DocFile.DefinedFile(
                "6502 Implemented",
                Chapter(
                    "Memory",
                    UnlinkedList(
                        Text("address-width: $MEM_ADDR_SIZE"),
                        Text("value-width: $BYTE_SIZE")
                    )
                )
            )
        )
    )

    val settings = listOf(
        SetupSetting.Enumeration("Instruction Cache", Cache.Setting.entries, Cache.Setting.NONE) { arch, setting ->
            if (arch is ArchT6502) {
                arch.instrMemory = when (setting.get()) {
                    Cache.Setting.NONE -> arch.memory
                    Cache.Setting.DirectedMapped -> DMCache(arch.memory,  CacheSize.KiloByte_32,  "Instruction")
                    Cache.Setting.FullAssociativeRandom -> FACache(arch.memory,  CacheSize.KiloByte_32, Cache.ReplaceAlgo.RANDOM,  "Instruction")
                    Cache.Setting.FullAssociativeLRU -> FACache(arch.memory,  CacheSize.KiloByte_32, Cache.ReplaceAlgo.LRU,  "Instruction")
                    Cache.Setting.FullAssociativeFIFO -> FACache(arch.memory,  CacheSize.KiloByte_32, Cache.ReplaceAlgo.FIFO,  "Instruction")
                    Cache.Setting.SetAssociativeRandom -> SACache(arch.memory, 4, CacheSize.KiloByte_32, Cache.ReplaceAlgo.RANDOM,  "Instruction")
                    Cache.Setting.SetAssociativeLRU -> SACache(arch.memory,  4, CacheSize.KiloByte_32, Cache.ReplaceAlgo.LRU,  "Instruction")
                    Cache.Setting.SetAssociativeFIFO -> SACache(arch.memory,  4, CacheSize.KiloByte_32, Cache.ReplaceAlgo.FIFO,  "Instruction")
                }
            }
        },
        SetupSetting.Enumeration("Data Cache", Cache.Setting.entries, Cache.Setting.NONE) { arch, setting ->
            if (arch is ArchT6502) {
                arch.dataMemory = when (setting.get()) {
                    Cache.Setting.NONE -> arch.memory
                    Cache.Setting.DirectedMapped -> DMCache(arch.memory,  CacheSize.KiloByte_32,  "Data")
                    Cache.Setting.FullAssociativeRandom -> FACache(arch.memory,  CacheSize.KiloByte_32, Cache.ReplaceAlgo.RANDOM,  "Data")
                    Cache.Setting.FullAssociativeLRU -> FACache(arch.memory,  CacheSize.KiloByte_32, Cache.ReplaceAlgo.LRU,  "Data")
                    Cache.Setting.FullAssociativeFIFO -> FACache(arch.memory,  CacheSize.KiloByte_32, Cache.ReplaceAlgo.FIFO,  "Data")
                    Cache.Setting.SetAssociativeRandom -> SACache(arch.memory, 4, CacheSize.KiloByte_32, Cache.ReplaceAlgo.RANDOM,  "Data")
                    Cache.Setting.SetAssociativeLRU -> SACache(arch.memory,  4, CacheSize.KiloByte_32, Cache.ReplaceAlgo.LRU,  "Data")
                    Cache.Setting.SetAssociativeFIFO -> SACache(arch.memory,  4, CacheSize.KiloByte_32, Cache.ReplaceAlgo.FIFO,  "Data")
                }
            }
        }
    )

    val config = Config(
        description,
        fileEnding = "s",
        RegContainer(listOf(commonRegFile), WORD_SIZE, "common"),
        null,
        settings
    )


}