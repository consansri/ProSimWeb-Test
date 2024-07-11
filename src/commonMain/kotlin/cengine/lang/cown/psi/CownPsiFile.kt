package cengine.lang.cown.psi

import cengine.lang.LanguageService
import cengine.lang.cown.CownLang
import cengine.psi.impl.PsiFileImpl

class CownPsiFile(name: String, content: String): PsiFileImpl(name, content) {
    override val lang: LanguageService = CownLang

    override fun updateFrom(content: String) {

    }
}