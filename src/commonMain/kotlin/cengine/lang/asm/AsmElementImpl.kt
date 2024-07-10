package cengine.lang.asm

import cengine.psi.core.PsiElement
import cengine.psi.core.TextRange
import cengine.psi.impl.PsiElementImpl

abstract class AsmElementImpl(
    override val textRange: TextRange,
    val lineNumber: Int
): PsiElementImpl() {
    override var parent: PsiElement? = null
}