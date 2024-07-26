package cengine.lang.asm.psi.dir

import cengine.lang.asm.psi.stmnt.AsmDirStmnt
import cengine.psi.core.PsiElement
import cengine.psi.core.PsiElementVisitor
import cengine.psi.core.TextRange

data class AsmDir(override var parent: AsmDirStmnt?, override val children: List<AsmSymbol>, override val textRange: TextRange): PsiElement {
    override fun accept(visitor: PsiElementVisitor) {
        visitor.visitElement(this)
        children.forEach {
            it.accept(visitor)
        }
    }
}