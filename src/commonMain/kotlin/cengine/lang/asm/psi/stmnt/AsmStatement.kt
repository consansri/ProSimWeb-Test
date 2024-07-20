package cengine.lang.asm.psi.stmnt

import cengine.lang.asm.psi.AsmFile
import cengine.psi.core.PsiElement

sealed class AsmStatement: PsiElement{
    override var parent: AsmFile? = null
}