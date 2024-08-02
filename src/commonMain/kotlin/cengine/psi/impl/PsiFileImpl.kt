package cengine.psi.impl

import cengine.psi.core.PsiElement
import cengine.psi.core.PsiElementVisitor
import cengine.psi.core.PsiFile
import cengine.vfs.VirtualFile

/**
 * Basic implementation of PsiFile
 */
abstract class PsiFileImpl(
    override val file: VirtualFile
) : PsiElementImpl(), PsiFile {
    override val parent: PsiElement? = null

    override fun accept(visitor: PsiElementVisitor) {
        visitor.visitFile(this)
    }
}