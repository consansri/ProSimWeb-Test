package cengine.lang.cown.psi

import cengine.editor.annotation.Notation
import cengine.editor.annotation.AnnotationProvider
import cengine.editor.annotation.Severity
import cengine.psi.core.PsiFile

class CownAnnotator : AnnotationProvider {
    override var cachedNotations: List<Notation> = listOf(
        Notation(10..20, "This is a warning!", Severity.WARNING),
        Notation(33..40, "This is an error!", Severity.ERROR)
    )

    override fun getAnnotations(psiFile: PsiFile): List<Notation> {
        return listOf()
    }
}