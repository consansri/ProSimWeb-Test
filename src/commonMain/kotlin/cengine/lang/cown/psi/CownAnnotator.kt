package cengine.lang.cown.psi

import cengine.editor.annotation.Annotation
import cengine.editor.annotation.AnnotationProvider
import cengine.editor.annotation.Severity
import cengine.psi.core.PsiFile

class CownAnnotator : AnnotationProvider {
    override var cachedAnnotations: List<Annotation> = listOf(
        Annotation(10..20, "This is a warning!", Severity.WARNING),
        Annotation(33..40, "This is an error!", Severity.ERROR)
    )

    override fun getAnnotations(psiFile: PsiFile): List<Annotation> {
        return listOf()
    }
}