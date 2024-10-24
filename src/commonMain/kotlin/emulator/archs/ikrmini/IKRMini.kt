package emulator.archs.ikrmini

import emulator.archs.ArchIKRMini
import emulator.kit.common.Docs
import emulator.kit.common.Docs.DocComponent.*
import emulator.kit.common.RegContainer
import emulator.kit.memory.*
import emulator.kit.config.AsmConfig
import emulator.kit.config.Config
import emulator.kit.optional.SetupSetting
import cengine.util.integer.Size.*
import cengine.util.integer.Hex
import cengine.util.integer.Variable

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
                ),
                Chapter(
                    "Instructions",
                    Table(
                        listOf("instruction", "param", "opcode", "description"),
                        *IKRMiniSyntax.InstrType.entries.map { instr ->
                            listOf(
                                Text(instr.name),
                                Text(instr.paramMap.entries.map { it.key }.joinToString("\n") { it.exampleString }),
                                Text(instr.paramMap.entries.joinToString("\n") { "${it.value} <- ${it.key.name}" }),
                                Text(instr.descr)
                            )
                        }.toTypedArray()
                    )
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
                        RegContainer.Register(Hex("0", Bit1), listOf("AC"), listOf(), Variable("0", WORDSIZE), description = "Accumulator"),
                        RegContainer.Register(Hex("1", Bit1), listOf("NZVC"), listOf(), Variable("0", Bit4), description = "NZVC ALU flags", containsFlags = true)
                    )
                )
            ),
            WORDSIZE,
            "common"
        ),
        MainMemory(WORDSIZE, BYTESIZE, Memory.Endianess.BigEndian),
        settings
    )

    val asmConfig = AsmConfig(IKRMiniAssembler)


}