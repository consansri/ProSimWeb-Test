package emulator.archs.t6502

import emulator.kit.assembly.Compiler
import emulator.kit.common.*
import emulator.kit.configs.AsmConfig
import emulator.kit.configs.Config
import emulator.kit.types.Variable
import emulator.kit.types.Variable.Value.*
import emulator.kit.types.Variable.Size.*


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

    val commonRegFile = RegContainer.RegisterFile(
        name = "common",
        unsortedRegisters = arrayOf(
            RegContainer.Register(Hex("00", WORD_SIZE), listOf("AC"), listOf(), Variable("00000000", BYTE_SIZE), description = "accumulator" ),
            RegContainer.Register(Hex("01", WORD_SIZE), listOf("X"), listOf(), Variable("00000000", BYTE_SIZE), description = "X register" ),
            RegContainer.Register(Hex("02", WORD_SIZE), listOf("Y"), listOf(), Variable("00000000", BYTE_SIZE), description = "Y register" ),
            RegContainer.Register(Hex("03", WORD_SIZE), listOf("SR"), listOf(), Variable("00000000", BYTE_SIZE), description = "status register [NV-BDIZC]" ),
            RegContainer.Register(Hex("04", WORD_SIZE), listOf("SP"), listOf(), Variable("11111111", BYTE_SIZE), description = "stack pointer" )
        )
    )

    val description = Config.Description(
        "T6502",
        "MOS Technology 6502",
        Docs()
    )

    val config = Config(
        description,
        FileHandler("s"),
        RegContainer(listOf(commonRegFile), WORD_SIZE, "common"),
        Memory(MEM_ADDR_SIZE, initBin = "0".repeat(BYTE_SIZE.bitWidth), BYTE_SIZE, Memory.Endianess.LittleEndian),
        Transcript()
    )

    val asmConfig = AsmConfig(
        syntax = T6502Syntax(),
        assembly = T6502Assembly(),
        compilerDetectRegistersByNames = false,
        numberSystemPrefixes = Compiler.ConstantPrefixes("$", "%", "", "u")
    )


}