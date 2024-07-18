package cengine.lang.asm.psi

import cengine.lang.LanguageService
import cengine.psi.core.PsiElement
import cengine.psi.core.PsiElementVisitor
import cengine.psi.core.PsiFile
import cengine.psi.core.TextRange

class AsmFile(override val name: String, override var text: String, override val lang: LanguageService?, override val children: MutableList<PsiElement> = mutableListOf()) : PsiFile {
    override val parent: PsiElement? = null

    override val textRange: TextRange
        get() = TextRange(0,text.length)

    override fun accept(visitor: PsiElementVisitor) {

    }

    override fun updateFrom(content: String) {
        text = content
        children.clear()
        // Reparse the file and update children
        parse()
    }

    private fun parse(){
        TODO()
    }


}