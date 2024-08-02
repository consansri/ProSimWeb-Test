package cengine.editor.annotation

import cengine.psi.core.PsiFile

interface AnnotationProvider {

    var cachedNotations: List<Notation>

    fun getAnnotations(psiFile: PsiFile): List<Notation>

}