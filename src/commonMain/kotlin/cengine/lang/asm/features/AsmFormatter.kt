package cengine.lang.asm.features

import cengine.editor.formatting.Formatter
import cengine.lang.asm.psi.AsmFile
import cengine.psi.core.PsiFile

class AsmFormatter : Formatter {
    override fun reformat(psiFile: PsiFile) {
        if (psiFile !is AsmFile) return
        psiFile.update()
        psiFile.reformat()
    }
}