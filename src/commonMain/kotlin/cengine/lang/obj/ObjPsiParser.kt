package cengine.lang.obj

import cengine.psi.core.PsiFile
import cengine.psi.core.PsiParser
import cengine.vfs.VirtualFile

object ObjPsiParser: PsiParser<PsiFile> {
    override fun parse(file: VirtualFile): PsiFile {
        return ObjPsiFile(file)
    }
}