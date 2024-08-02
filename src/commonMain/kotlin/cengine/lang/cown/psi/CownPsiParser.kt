package cengine.lang.cown.psi

import cengine.psi.core.PsiFile
import cengine.psi.core.PsiParser
import cengine.vfs.VirtualFile

class CownPsiParser: PsiParser {
    override fun parseFile(file: VirtualFile): PsiFile {
        return CownPsiFile(file)
    }
}