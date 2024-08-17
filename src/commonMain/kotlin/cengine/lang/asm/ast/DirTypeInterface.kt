package cengine.lang.asm.ast

import cengine.lang.asm.ast.impl.ASNode
import cengine.lang.asm.ast.lexer.AsmLexer
import cengine.lang.asm.elf.ELFBuilder

interface DirTypeInterface {

    /**
     * Leave blank if it isn't starting with the directive itself!
     */
    fun getDetectionString(): String
    val isSection: Boolean
    val rule: Rule?
    val typeName: String
    fun buildDirectiveContent(lexer: AsmLexer, asmSpec: AsmSpec): ASNode.Directive?
    fun execute(builder: ELFBuilder, dir: ASNode.Directive)
    fun checkSemantic(dir: ASNode.Directive)

}