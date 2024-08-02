package cengine.lang.asm.ast

import cengine.lang.asm.ast.gas.GASNode
import cengine.lang.asm.lexer.AsmLexer
import cengine.lang.asm.parser.Rule

interface DirTypeInterface {

    /**
     * Leave blank if it isn't starting with the directive itself!
     */
    fun getDetectionString(): String
    val isSection: Boolean
    val rule: Rule?
    fun buildDirectiveContent(lexer: AsmLexer, asmSpec: AsmSpec): GASNode.Directive?

}