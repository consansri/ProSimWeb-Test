package cengine.lang.cown.psi

import cengine.editor.annotation.AnnotationProvider
import cengine.editor.annotation.Annotation
import cengine.psi.core.PsiFile

class CownAnnotator : AnnotationProvider {
    override var cachedNotations: MutableMap<PsiFile, List<Annotation>> = mutableMapOf(    )

    override fun updateAnnotations(psiFile: PsiFile) {

    }
}