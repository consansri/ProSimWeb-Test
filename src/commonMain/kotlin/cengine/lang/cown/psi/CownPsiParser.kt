package cengine.lang.cown.psi

import cengine.editor.text.TextModel
import cengine.psi.core.PsiFile
import cengine.psi.core.PsiParser
import cengine.vfs.VirtualFile

class CownPsiParser: PsiParser {
    override fun parseFile(file: VirtualFile, textModel: TextModel?): PsiFile {
        return CownPsiFile(file)
    }
}