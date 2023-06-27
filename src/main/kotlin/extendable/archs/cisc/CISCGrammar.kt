package extendable.archs.cisc

import extendable.components.assembly.Assembly
import extendable.components.assembly.Grammar

class CISCGrammar: Grammar() {
    override fun clear() {

    }

    override fun check(tokenLines: List<List<Assembly.Token>>): GrammarTree {
        return GrammarTree()
    }
}