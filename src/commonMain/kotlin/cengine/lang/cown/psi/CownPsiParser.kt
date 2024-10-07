package cengine.lang.cown.psi

import cengine.lang.cown.CownLang
import cengine.psi.core.PsiParser
import cengine.vfs.VirtualFile

class CownPsiParser(val lang: CownLang): PsiParser<CownPsiFile> {
    override fun parse(file: VirtualFile): CownPsiFile {
        return CownPsiFile(file, lang)
    }
}