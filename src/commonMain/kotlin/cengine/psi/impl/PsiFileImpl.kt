package cengine.psi.impl

import cengine.psi.core.PsiElement
import cengine.psi.core.PsiElementVisitor
import cengine.psi.core.PsiFile
import cengine.psi.core.TextRange

/**
 * Basic implementation of PsiFile
 */
abstract class PsiFileImpl(
    override val name: String,
    override val text: String
) : PsiElementImpl(), PsiFile {
    override val parent: PsiElement? = null
    override val textRange: TextRange = TextRange(0, text.length)

    override fun accept(visitor: PsiElementVisitor) {
        visitor.visitFile(this)
    }
}