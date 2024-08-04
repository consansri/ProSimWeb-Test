package cengine.lang.cown.psi

import cengine.editor.annotation.AnnotationProvider
import cengine.editor.annotation.Notation
import cengine.psi.core.PsiFile

class CownAnnotator : AnnotationProvider {
    override var cachedNotations: MutableMap<PsiFile, List<Notation>> = mutableMapOf(    )

    override fun updateAnnotations(psiFile: PsiFile) {

    }
}