package extendable.archs.mini

import extendable.components.assembly.Compiler
import extendable.components.assembly.Grammar

class MiniGrammar : Grammar() {

    override val applyStandardHLForRest: Boolean = true

    override fun clear() {
    }

    override fun check(compiler: Compiler, tokenLines: List<List<Compiler.Token>>, others: List<Compiler.OtherFile>): GrammarTree {
        return GrammarTree()
    }
}