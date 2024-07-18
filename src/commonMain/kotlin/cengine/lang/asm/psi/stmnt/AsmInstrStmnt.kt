package cengine.lang.asm.psi.stmnt

import cengine.lang.asm.psi.AsmSection
import cengine.lang.asm.psi.instr.AsmInstr
import cengine.psi.core.PsiElement
import cengine.psi.core.PsiElementVisitor
import cengine.psi.core.TextRange

data class AsmInstrStmnt(override val parent: AsmSection, val label: AsmLabel?, val instr: AsmInstr, override val textRange: TextRange): AsmStatement(){

    override val children: List<PsiElement> = listOfNotNull(label, instr)

    override fun accept(visitor: PsiElementVisitor) {
        TODO("Not yet implemented")
    }
}
