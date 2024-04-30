package emulator.kit.configs

import emulator.kit.assembler.Assembly
import emulator.kit.assembler.gas.DefinedAssembly
import emulator.kit.optional.ArchSetting
import emulator.kit.optional.Feature


/**
 * [AsmConfig] is the configuration Class which holds specific implementations of the [Syntax] and [Assembly] process for each specific Architecture.
 */
data class AsmConfig(
    val definedAssembly: DefinedAssembly,
    val features: List<Feature> = listOf(),
    val settings: List<ArchSetting> = listOf()
)