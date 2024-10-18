package cengine.lang.obj

import cengine.lang.LanguageService
import cengine.psi.core.PsiFile
import cengine.psi.core.PsiParser
import cengine.vfs.VirtualFile

class ObjPsiParser(val lang: LanguageService): PsiParser<PsiFile> {
    override fun parse(file: VirtualFile): PsiFile {
        return ObjPsiFile(file, lang)
    }
}