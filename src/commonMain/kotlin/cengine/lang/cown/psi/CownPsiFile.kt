package cengine.lang.cown.psi

import cengine.editor.annotation.Notation
import cengine.lang.LanguageService
import cengine.lang.cown.CownLang
import cengine.psi.core.TextRange
import cengine.psi.impl.PsiFileImpl
import cengine.vfs.VirtualFile

class CownPsiFile(file: VirtualFile): PsiFileImpl(file) {
    override val notations: List<Notation> = listOf()
    override var textRange: TextRange = TextRange(0, file.getContent().size)
    override val lang: LanguageService = CownLang

    override fun update() {
        
    }

}