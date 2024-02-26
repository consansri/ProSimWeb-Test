package emulator.kit.configs

import emulator.kit.assembly.Assembly
import emulator.kit.assembly.Compiler
import emulator.kit.assembly.Syntax
import emulator.kit.optional.ArchSetting
import emulator.kit.optional.Feature


/**
 * [AsmConfig] is the configuration Class which holds specific implementations of the [Syntax] and [Assembly] process for each specific Architecture.
 */
data class AsmConfig(
    val syntax: Syntax,
    val assembly: Assembly,
    val compilerDetectRegistersByNames: Boolean,
    val numberSystemPrefixes: Compiler.ConstantPrefixes = Compiler.ConstantPrefixes(),
    val features: List<Feature> = listOf(),
    val settings: List<ArchSetting> = listOf()
)