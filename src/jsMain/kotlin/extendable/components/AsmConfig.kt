package extendable.components

import extendable.components.assembly.Assembly
import extendable.components.assembly.Grammar

data class AsmConfig(val grammar: Grammar, val assembly: Assembly)