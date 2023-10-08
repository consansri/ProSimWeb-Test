package emulator.kit.configs

import emulator.kit.assembly.Assembly
import emulator.kit.assembly.Syntax


/**
 * [AsmConfig] is the configuration Class which holds specific implementations of the [Syntax] and [Assembly] process for each specific Architecture.
 */
data class AsmConfig(val syntax: Syntax, val assembly: Assembly)