package cengine.lang.asm.psi.stmnt

import cengine.lang.asm.psi.dir.AsmDir
import cengine.psi.core.PsiElementVisitor
import cengine.psi.core.TextRange

class AsmDirStmnt(val label: AsmLabel?, val directive: AsmDir, override val textRange: TextRange) : AsmStatement() {
    override val children: List<AsmDir> = listOf(directive)

    override fun accept(visitor: PsiElementVisitor) {
        visitor.visitElement(this)
        children.forEach {
            it.accept(visitor)
        }
    }
}