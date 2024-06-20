package emulator.kit.config

import emulator.kit.assembler.AsmHeader
import emulator.kit.optional.Feature


/**
 * [AsmConfig] is the configuration Class which holds specific implementations of [AsmHeader] for each specific Architecture.
 */
data class AsmConfig(
    val asmHeader: AsmHeader,
    val features: List<Feature> = listOf()
)