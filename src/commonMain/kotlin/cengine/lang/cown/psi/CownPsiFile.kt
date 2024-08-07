package cengine.lang.cown.psi

import cengine.editor.annotation.Notation
import cengine.editor.text.TextModel
import cengine.editor.widgets.Widget
import cengine.lang.LanguageService
import cengine.lang.cown.CownLang
import cengine.psi.core.PsiElement
import cengine.psi.impl.PsiFileImpl
import cengine.vfs.VirtualFile

class CownPsiFile(file: VirtualFile): PsiFileImpl(file) {
    override val children: MutableList<PsiElement> = mutableListOf()

    override val additionalInfo: String = ""
    override val interlineWidgets: List<Widget>
        get() = emptyList()
    override val inlayWidgets: List<Widget>
        get() = emptyList()

    override val notations: List<Notation> = listOf()
    override var range: IntRange = 0..<file.getContent().size

    override val lang: LanguageService = CownLang
    override var textModel: TextModel? = null

    override fun update() {
        val file = lang.psiParser.parseFile(file, textModel)
        children.clear()
        children.addAll(file.children)
    }
}