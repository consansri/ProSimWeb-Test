package extendable.archs.mini

import extendable.components.assembly.Assembly
import extendable.components.assembly.Grammar

class MiniGrammar: Grammar() {
    override fun clear() {
    }

    override fun check(tokenLines: List<List<Assembly.Token>>): GrammarTree {
        return GrammarTree()
    }
}