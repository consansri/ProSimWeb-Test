package emulator.archs.ikrmini

import emulator.archs.ArchIKRMini
import emulator.kit.common.Docs
import emulator.kit.common.Docs.DocComponent.*
import emulator.kit.common.RegContainer
import emulator.kit.common.memory.*
import emulator.kit.configs.AsmConfig
import emulator.kit.configs.Config
import emulator.kit.optional.SetupSetting
import emulator.kit.types.Variable
import emulator.kit.types.Variable.Size.*
import emulator.kit.types.Variable.Value.Hex

data object IKRMini {

    val BYTESIZE = Bit8()
    val WORDSIZE = Bit16()
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
                    Text("address-width: ${MEM_ADDRESS_WIDTH}"),
                    Text("value-width: ${BYTESIZE}")
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
        SetupSetting.Enumeration("Cache", Cache.Setting.entries, Cache.Setting.NONE) { arch, setting ->
            if (arch is ArchIKRMini) {
                arch.cachedMemory = when (setting.get()) {
                    Cache.Setting.NONE -> arch.memory
                    Cache.Setting.DirectedMapped -> DMCache(arch.memory, arch.console, 4, 4)
                    Cache.Setting.FullAssociativeRandom -> FACache(arch.memory, arch.console, 4, 16, Cache.Model.ReplaceAlgo.RANDOM)
                    Cache.Setting.FullAssociativeLRU -> FACache(arch.memory, arch.console, 4, 16, Cache.Model.ReplaceAlgo.LRU)
                    Cache.Setting.FullAssociativeFIFO -> FACache(arch.memory, arch.console, 4, 16, Cache.Model.ReplaceAlgo.FIFO)
                    Cache.Setting.SetAssociativeRandom -> SACache(arch.memory, arch.console, 3, 4, 4, Cache.Model.ReplaceAlgo.RANDOM)
                    Cache.Setting.SetAssociativeLRU -> SACache(arch.memory, arch.console, 3, 4, 4, Cache.Model.ReplaceAlgo.LRU)
                    Cache.Setting.SetAssociativeFIFO -> SACache(arch.memory, arch.console, 3, 4, 4, Cache.Model.ReplaceAlgo.FIFO)
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
                        RegContainer.Register(Hex("0", Bit1()), listOf("AC"), listOf(), Variable("0", WORDSIZE), description = "Accumulator"),
                        RegContainer.Register(Hex("1", Bit1()), listOf("NZVC"), listOf(), Variable("0", Bit4()), description = "NZVC ALU flags", containsFlags = true)
                    )
                )
            ),
            WORDSIZE,
            "common"
        ),
        MainMemory(WORDSIZE, BYTESIZE, Memory.Endianess.BigEndian),
        settings
    )

    val asmConfig = AsmConfig(IKRMiniAssembler())


}