package emulator.archs.ikrrisc2

import emulator.archs.ArchIKRRisc2
import emulator.core.Size.Bit32
import emulator.core.Size.Bit5
import emulator.core.Value.Bin
import emulator.core.Variable
import emulator.kit.common.Docs
import emulator.kit.common.RegContainer
import emulator.kit.common.RegContainer.Register
import emulator.kit.common.RegContainer.RegisterFile
import emulator.kit.config.AsmConfig
import emulator.kit.config.Config
import emulator.kit.memory.*
import emulator.kit.optional.SetupSetting

data object IKRRisc2 {

    val WORD_WIDTH = Bit32
    val REG_SIZE = Bit5
    val WORD_WIDTH_ONE = Bin("1", WORD_WIDTH)
    val WORD_WIDTH_NEGATIVE_ONE = Bin("1".repeat(WORD_WIDTH.bitWidth), WORD_WIDTH).toBin()
    val WORD_WIDTH_ZERO = Bin("0", WORD_WIDTH)
    val R31_ADDR = Bin("11111", REG_SIZE)
    const val REG_INIT = "0"

    const val standardRegFileName = "main"
    val standardRegFile = RegisterFile(
        standardRegFileName, arrayOf(
            Register(Bin("00000", REG_SIZE), listOf("r0"), listOf(), Variable(REG_INIT, WORD_WIDTH), "hardwired zero", hardwire = true),
            Register(Bin("00001", REG_SIZE), listOf("r1"), listOf(), Variable(REG_INIT, WORD_WIDTH), ""),
            Register(Bin("00010", REG_SIZE), listOf("r2"), listOf(), Variable(REG_INIT, WORD_WIDTH), ""),
            Register(Bin("00011", REG_SIZE), listOf("r3"), listOf(), Variable(REG_INIT, WORD_WIDTH), ""),
            Register(Bin("00100", REG_SIZE), listOf("r4"), listOf(), Variable(REG_INIT, WORD_WIDTH), ""),
            Register(Bin("00101", REG_SIZE), listOf("r5"), listOf(), Variable(REG_INIT, WORD_WIDTH), ""),
            Register(Bin("00110", REG_SIZE), listOf("r6"), listOf(), Variable(REG_INIT, WORD_WIDTH), ""),
            Register(Bin("00111", REG_SIZE), listOf("r7"), listOf(), Variable(REG_INIT, WORD_WIDTH), ""),
            Register(Bin("01000", REG_SIZE), listOf("r8"), listOf(), Variable(REG_INIT, WORD_WIDTH), ""),
            Register(Bin("01001", REG_SIZE), listOf("r9"), listOf(), Variable(REG_INIT, WORD_WIDTH), ""),
            Register(Bin("01010", REG_SIZE), listOf("r10"), listOf(), Variable(REG_INIT, WORD_WIDTH), ""),
            Register(Bin("01011", REG_SIZE), listOf("r11"), listOf(), Variable(REG_INIT, WORD_WIDTH), ""),
            Register(Bin("01100", REG_SIZE), listOf("r12"), listOf(), Variable(REG_INIT, WORD_WIDTH), ""),
            Register(Bin("01101", REG_SIZE), listOf("r13"), listOf(), Variable(REG_INIT, WORD_WIDTH), ""),
            Register(Bin("01110", REG_SIZE), listOf("r14"), listOf(), Variable(REG_INIT, WORD_WIDTH), ""),
            Register(Bin("01111", REG_SIZE), listOf("r15"), listOf(), Variable(REG_INIT, WORD_WIDTH), ""),
            Register(Bin("10000", REG_SIZE), listOf("r16"), listOf(), Variable(REG_INIT, WORD_WIDTH), ""),
            Register(Bin("10001", REG_SIZE), listOf("r17"), listOf(), Variable(REG_INIT, WORD_WIDTH), ""),
            Register(Bin("10010", REG_SIZE), listOf("r18"), listOf(), Variable(REG_INIT, WORD_WIDTH), ""),
            Register(Bin("10011", REG_SIZE), listOf("r19"), listOf(), Variable(REG_INIT, WORD_WIDTH), ""),
            Register(Bin("10100", REG_SIZE), listOf("r20"), listOf(), Variable(REG_INIT, WORD_WIDTH), ""),
            Register(Bin("10101", REG_SIZE), listOf("r21"), listOf(), Variable(REG_INIT, WORD_WIDTH), ""),
            Register(Bin("10110", REG_SIZE), listOf("r22"), listOf(), Variable(REG_INIT, WORD_WIDTH), ""),
            Register(Bin("10111", REG_SIZE), listOf("r23"), listOf(), Variable(REG_INIT, WORD_WIDTH), ""),
            Register(Bin("11000", REG_SIZE), listOf("r24"), listOf(), Variable(REG_INIT, WORD_WIDTH), ""),
            Register(Bin("11001", REG_SIZE), listOf("r25"), listOf(), Variable(REG_INIT, WORD_WIDTH), ""),
            Register(Bin("11010", REG_SIZE), listOf("r26"), listOf(), Variable(REG_INIT, WORD_WIDTH), ""),
            Register(Bin("11011", REG_SIZE), listOf("r27"), listOf(), Variable(REG_INIT, WORD_WIDTH), ""),
            Register(Bin("11100", REG_SIZE), listOf("r28"), listOf(), Variable(REG_INIT, WORD_WIDTH), ""),
            Register(Bin("11101", REG_SIZE), listOf("r29"), listOf(), Variable(REG_INIT, WORD_WIDTH), ""),
            Register(Bin("11110", REG_SIZE), listOf("r30"), listOf(), Variable(REG_INIT, WORD_WIDTH), ""),
            Register(R31_ADDR, listOf("r31"), listOf(), Variable(REG_INIT, WORD_WIDTH), "return address"),
        )
    )

    val settings = listOf(
        SetupSetting.Enumeration("Instruction Cache", Cache.Setting.entries, Cache.Setting.NONE) { arch, setting ->
            if (arch is ArchIKRRisc2) {
                arch.instrMemory = when (setting.get()) {
                    Cache.Setting.NONE -> arch.memory
                    Cache.Setting.DirectedMapped -> DMCache(arch.memory, arch.console, CacheSize.KiloByte_32, "Instruction")
                    Cache.Setting.FullAssociativeRandom -> FACache(arch.memory, arch.console, CacheSize.KiloByte_32, Cache.Model.ReplaceAlgo.RANDOM, "Instruction")
                    Cache.Setting.FullAssociativeLRU -> FACache(arch.memory, arch.console, CacheSize.KiloByte_32, Cache.Model.ReplaceAlgo.LRU, "Instruction")
                    Cache.Setting.FullAssociativeFIFO -> FACache(arch.memory, arch.console, CacheSize.KiloByte_32, Cache.Model.ReplaceAlgo.FIFO, "Instruction")
                    Cache.Setting.SetAssociativeRandom -> SACache(arch.memory, arch.console, 4, CacheSize.KiloByte_32, Cache.Model.ReplaceAlgo.RANDOM, "Instruction")
                    Cache.Setting.SetAssociativeLRU -> SACache(arch.memory, arch.console, 4, CacheSize.KiloByte_32, Cache.Model.ReplaceAlgo.LRU, "Instruction")
                    Cache.Setting.SetAssociativeFIFO -> SACache(arch.memory, arch.console, 4, CacheSize.KiloByte_32, Cache.Model.ReplaceAlgo.FIFO, "Instruction")
                }
            }
        },
        SetupSetting.Enumeration("Data Cache", Cache.Setting.entries, Cache.Setting.NONE) { arch, setting ->
            if (arch is ArchIKRRisc2) {
                arch.dataMemory = when (setting.get()) {
                    Cache.Setting.NONE -> arch.memory
                    Cache.Setting.DirectedMapped -> DMCache(arch.memory, arch.console, CacheSize.KiloByte_32, "Data")
                    Cache.Setting.FullAssociativeRandom -> FACache(arch.memory, arch.console, CacheSize.KiloByte_32, Cache.Model.ReplaceAlgo.RANDOM, "Data")
                    Cache.Setting.FullAssociativeLRU -> FACache(arch.memory, arch.console, CacheSize.KiloByte_32, Cache.Model.ReplaceAlgo.LRU, "Data")
                    Cache.Setting.FullAssociativeFIFO -> FACache(arch.memory, arch.console, CacheSize.KiloByte_32, Cache.Model.ReplaceAlgo.FIFO, "Data")
                    Cache.Setting.SetAssociativeRandom -> SACache(arch.memory, arch.console, 4, CacheSize.KiloByte_32, Cache.Model.ReplaceAlgo.RANDOM, "Data")
                    Cache.Setting.SetAssociativeLRU -> SACache(arch.memory, arch.console, 4, CacheSize.KiloByte_32, Cache.Model.ReplaceAlgo.LRU, "Data")
                    Cache.Setting.SetAssociativeFIFO -> SACache(arch.memory, arch.console, 4, CacheSize.KiloByte_32, Cache.Model.ReplaceAlgo.FIFO, "Data")
                }
            }
        }
    )

    val implementationDoc = Docs.DocFile.DefinedFile(
        "IKR RISC-II Implemented",
        Docs.DocComponent.Chapter(
            "Instructions",
            Docs.DocComponent.Table(
                listOf("instruction", "params", "opcode"),
                contentRows = InstrType.entries.map { instr ->
                    listOf(Docs.DocComponent.Text(instr.id), Docs.DocComponent.Text(instr.paramType.exampleString),
                        Docs.DocComponent.Table(
                            header = instr.opCode.maskLabels.map { it.name },
                            contentRows = arrayOf(instr.opCode.opMaskList.map { Docs.DocComponent.Text(it) })))
                }.toTypedArray()
            )
        ),
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
        MainMemory(WORD_WIDTH, WORD_WIDTH, endianess = Memory.Endianess.BigEndian, entrysInRow = 4),
        settings
    )

    val asmConfig = AsmConfig(IKRRisc2Assembler)
}