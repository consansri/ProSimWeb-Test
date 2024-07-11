package cengine.lang.cown.psi

import cengine.psi.core.PsiFile
import cengine.psi.core.PsiParser

class CownPsiParser: PsiParser {
    override fun parseFile(content: String, name: String): PsiFile {
        return CownPsiFile(name, content)
    }
}