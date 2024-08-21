package emulator.archs.t6502


import emulator.archs.ArchT6502
import emulator.kit.common.Docs
import emulator.kit.common.Docs.DocComponent.*
import emulator.kit.common.RegContainer
import emulator.kit.memory.*
import emulator.kit.config.AsmConfig
import emulator.kit.config.Config
import emulator.kit.optional.SetupSetting
import cengine.util.integer.Size.Bit16
import cengine.util.integer.Size.Bit8
import cengine.util.integer.Hex
import emulator.core.*


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

    enum class TSCompiledRow {
        ADDRESS,
        LABEL,
        INSTRUCTION,
        EXTENSION
    }

    enum class TSDisassembledRow {
        ADDRESS,
        INSTRUCTION,
        EXTENSION
    }

    val commonRegFile = RegContainer.RegisterFile(
        name = "common",
        unsortedRegisters = arrayOf(
            RegContainer.Register(Hex("00", WORD_SIZE), listOf("AC"), listOf(), Variable("00000000", BYTE_SIZE), description = "accumulator"),
            RegContainer.Register(Hex("01", WORD_SIZE), listOf("X"), listOf(), Variable("00000000", BYTE_SIZE), description = "X register"),
            RegContainer.Register(Hex("02", WORD_SIZE), listOf("Y"), listOf(), Variable("00000000", BYTE_SIZE), description = "Y register"),
            RegContainer.Register(Hex("03", WORD_SIZE), listOf("SR"), listOf(), Variable("00100000", BYTE_SIZE), description = "status register [NV-BDIZC]", containsFlags = true),
            RegContainer.Register(Hex("04", WORD_SIZE), listOf("SP"), listOf(), Variable("11111111", BYTE_SIZE), description = "stack pointer")
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
                ),
                Chapter(
                    "Instructions",
                    Table(
                        listOf("instruction", "params", "opcode", "description"),
                        *InstrType.entries.map { instr ->
                            listOf(
                                Text(instr.name),
                                Text(instr.opCode.entries.map { it.key }.joinToString("\n") { it.exampleString }),
                                Text(instr.opCode.entries.joinToString("\n") { "${it.value} <- ${it.key.description}" }),
                                Text(instr.description)
                            )
                        }.toTypedArray()
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
            if (arch is ArchT6502) {
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
        description,
        fileEnding = "s",
        RegContainer(listOf(commonRegFile), WORD_SIZE, "common"),
        MainMemory(MEM_ADDR_SIZE, BYTE_SIZE, Memory.Endianess.LittleEndian),
        settings
    )

    val asmConfig = AsmConfig(
        T6502Assembler
    )


}