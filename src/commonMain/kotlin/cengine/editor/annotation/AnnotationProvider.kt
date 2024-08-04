package cengine.editor.annotation

import cengine.psi.core.PsiFile

interface AnnotationProvider {

    val cachedNotations: MutableMap<PsiFile, List<Notation>>

    fun updateAnnotations(psiFile: PsiFile)

}