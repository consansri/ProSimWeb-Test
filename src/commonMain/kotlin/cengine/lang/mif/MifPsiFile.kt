package cengine.lang.mif

import cengine.editor.annotation.Annotation
import cengine.psi.core.PsiElement
import cengine.psi.core.PsiElementVisitor
import cengine.psi.core.PsiFile
import cengine.vfs.VirtualFile

class MifPsiFile(
    override val file: VirtualFile, override val lang: MifLang, override val children: List<PsiElement>, override val annotations: List<Annotation>
) : PsiFile {
    override fun update() {

    }

    override var parent: PsiElement? = null
    override val additionalInfo: String = "MifFile"
    override var range: IntRange = (children.minOf { it.range.first })..(children.maxOf { it.range.last })

    override fun accept(visitor: PsiElementVisitor) {
        visitor.visitFile(this)
    }
}