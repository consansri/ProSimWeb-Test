package cengine.lang.asm.features

import cengine.editor.formatting.Formatter
import cengine.lang.asm.psi.AsmFile
import cengine.psi.core.PsiFile

class AsmFormatter : Formatter {
    override fun formatted(psiFile: PsiFile): String? {
        if (psiFile !is AsmFile) return null
        psiFile.update()
        return psiFile.getFormattedString(4)
    }
}