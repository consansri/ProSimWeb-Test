package extendable.archs.riscii

import extendable.components.assembly.Compiler
import extendable.components.assembly.Grammar
import extendable.components.connected.FileHandler

class RISCIIGrammar: Grammar() {

    override val applyStandardHLForRest: Boolean = true

    override fun clear() {

    }

    override fun check(compiler: Compiler, tokenLines: List<List<Compiler.Token>>, others: List<FileHandler.File>): GrammarTree {
        return GrammarTree()
    }

}