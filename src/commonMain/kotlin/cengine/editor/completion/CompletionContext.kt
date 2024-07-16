package cengine.editor.completion

import cengine.psi.core.PsiFile
import cengine.vfs.VirtualFile

data class CompletionContext(
    val file: VirtualFile,
    val offset: Int,
    val prefix: Int,
    val psiFile: PsiFile?
)