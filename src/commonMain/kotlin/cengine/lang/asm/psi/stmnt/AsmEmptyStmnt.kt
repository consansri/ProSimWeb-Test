package cengine.lang.asm.psi.stmnt

import cengine.psi.core.PsiElement
import cengine.psi.core.PsiElementVisitor
import cengine.psi.core.TextRange

data class AsmEmptyStmnt( val label: AsmLabel?, override val textRange: TextRange) : AsmStatement() {
    override val children: List<PsiElement> = listOfNotNull(label)

    override fun accept(visitor: PsiElementVisitor) {
        visitor.visitElement(this)
        children.forEach {
            it.accept(visitor)
        }
    }
}