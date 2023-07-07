package extendable.archs.cisc

import extendable.components.assembly.Compiler
import extendable.components.assembly.Grammar

class CISCGrammar: Grammar() {

    override val applyStandardHLForRest: Boolean = true

    override fun clear() {

    }

    override fun check(tokenLines: List<List<Compiler.Token>>): GrammarTree {
        return GrammarTree()
    }
}