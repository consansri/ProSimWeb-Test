package extendable.archs.riscii

import extendable.components.assembly.Assembly
import extendable.components.assembly.Grammar

class RISCIIGrammar: Grammar() {

    override val applyStandardHLForRest: Boolean = true

    override fun clear() {

    }

    override fun check(tokenLines: List<List<Assembly.Token>>): GrammarTree {
        return GrammarTree()
    }

}