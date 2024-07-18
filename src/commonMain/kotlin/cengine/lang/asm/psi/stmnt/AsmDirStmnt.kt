package cengine.lang.asm.psi.stmnt

import cengine.lang.asm.psi.AsmSection
import cengine.lang.asm.psi.dir.AsmDir
import cengine.psi.core.PsiElementVisitor
import cengine.psi.core.TextRange

sealed class AsmDirStmnt(override val parent: AsmSection?, val label: AsmLabel?, val directive: AsmDir, override val textRange: TextRange) : AsmStatement() {
    override val children: List<AsmDir> = listOf(directive)

    override fun accept(visitor: PsiElementVisitor) {
        TODO("Not yet implemented")
    }
}