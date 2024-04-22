package emulator.archs.t6502


import emulator.kit.assembly.Compiler
import emulator.kit.common.*
import emulator.kit.configs.AsmConfig
import emulator.kit.configs.Config
import emulator.kit.types.Variable
import emulator.kit.types.Variable.Value.*
import emulator.kit.types.Variable.Size.*
import emulator.kit.common.Docs.DocComponent.*


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
object T6502 {

    val WORD_SIZE = Bit16()
    val BYTE_SIZE = Bit8()

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
            usingStandard = true,
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
                        *T6502Syntax.InstrType.entries.map { instr ->
                            listOf(
                                Text(instr.name),
                                Text(instr.opCode.entries.map { it.key }.joinToString("\n") { it.exampleString }),
                                Text(instr.opCode.entries.joinToString("\n") { "${it.value} <- ${it.key.description}" }),
                                Text(instr.description)
                            )
                        }.toTypedArray()
                    )
                )
            ),
            Docs.DocFile.SourceFile(
                "Syntax Examples", "documents/t6502/syntaxexamples.html"
            )
        )
    )

    val config = Config(
        description,
        fileEnding = "s",
        RegContainer(listOf(commonRegFile), WORD_SIZE, "common"),
        Memory(MEM_ADDR_SIZE, initBin = "0".repeat(BYTE_SIZE.bitWidth), BYTE_SIZE, Memory.Endianess.LittleEndian),
        Transcript(TSCompiledRow.entries.map { it.name }, TSDisassembledRow.entries.map { it.name })
    )

    val asmConfig = AsmConfig(
        syntax = T6502Syntax(),
        assembly = T6502Assembly(),
        compilerDetectRegistersByNames = false,
        numberSystemPrefixes = Compiler.ConstantPrefixes("$", "%", "", "u")
    )


}