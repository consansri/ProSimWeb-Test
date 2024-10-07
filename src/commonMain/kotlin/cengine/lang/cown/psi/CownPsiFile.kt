package cengine.lang.cown.psi

import cengine.editor.annotation.Annotation
import cengine.lang.cown.CownLang
import cengine.psi.core.PsiElement
import cengine.psi.impl.PsiFileImpl
import cengine.vfs.VirtualFile

class CownPsiFile(file: VirtualFile, override val lang: CownLang): PsiFileImpl(file) {
    override val children: MutableList<PsiElement> = mutableListOf()

    override val additionalInfo: String = ""

    override val annotations: List<Annotation> = listOf()
    override var range: IntRange = 0..<file.getContent().size

    override fun update() {
        val file = lang.psiParser.parse(file)
        children.clear()
        children.addAll(file.children)
    }
}