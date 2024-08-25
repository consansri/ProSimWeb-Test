package cengine.lang.asm.ast

import cengine.lang.asm.ast.impl.ASNode
import cengine.lang.asm.ast.lexer.AsmLexer
import cengine.lang.asm.elf.RelocatableELFBuilder

interface DirTypeInterface {

    /**
     * Leave blank if it isn't starting with the directive itself!
     */
    fun getDetectionString(): String
    val isSection: Boolean
    val rule: Rule?
    val typeName: String
    fun buildDirectiveContent(lexer: AsmLexer, targetSpec: TargetSpec): ASNode.Directive?
    fun build(builder: RelocatableELFBuilder, dir: ASNode.Directive)
}