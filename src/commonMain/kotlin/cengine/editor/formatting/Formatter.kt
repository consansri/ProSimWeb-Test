package cengine.editor.formatting

import cengine.psi.core.PsiFile

interface Formatter {
    fun formatted(psiFile: PsiFile): String?
}