package cengine.lang.cown.psi

import cengine.editor.annotation.Notation
import cengine.editor.text.TextModel
import cengine.lang.LanguageService
import cengine.lang.cown.CownLang
import cengine.psi.core.PsiElement
import cengine.psi.core.TextRange
import cengine.psi.impl.PsiFileImpl
import cengine.vfs.VirtualFile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

class CownPsiFile(file: VirtualFile): PsiFileImpl(file) {
    override val quickeditScope: CoroutineScope = CoroutineScope(Dispatchers.Default)
    override val children: MutableList<PsiElement> = mutableListOf()

    override val additionalInfo: String = ""
    override val notations: List<Notation> = listOf()
    override var textRange: TextRange = TextRange(0, file.getContent().size)

    override val lang: LanguageService = CownLang
    override var textModel: TextModel? = null

    override fun update() {
        val file = lang.psiParser.parseFile(file, textModel)
        children.clear()
        children.addAll(file.children)
    }

}