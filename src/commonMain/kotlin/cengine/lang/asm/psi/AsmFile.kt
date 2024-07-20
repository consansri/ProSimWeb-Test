package cengine.lang.asm.psi

import cengine.lang.asm.AsmLang
import cengine.lang.asm.psi.stmnt.AsmStatement
import cengine.psi.core.PsiElement
import cengine.psi.core.PsiElementVisitor
import cengine.psi.core.PsiFile
import cengine.psi.core.TextRange

class AsmFile(override val name: String, override var text: String, override val lang: AsmLang, override val children: MutableList<AsmStatement> = mutableListOf()) : PsiFile {
    override val parent: PsiElement? = null

    override val textRange: TextRange
        get() = TextRange(0, text.length)

    override fun accept(visitor: PsiElementVisitor) {
        visitor.visitFile(this)
        ArrayList(children).forEach {
            it.accept(visitor)
        }
    }

    override fun updateFrom(content: String) {
        text = content
        children.clear()
        // Reparse the file and update children
        parse()
    }

    private fun parse() {
        children.addAll(lang.psiParser.parseFile(text, name).children)
    }


}