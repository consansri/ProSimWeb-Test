package cengine.editor.annotation

import cengine.psi.core.PsiFile

interface AnnotationProvider {

    var cachedAnnotations: List<Annotation>

    fun getAnnotations(psiFile: PsiFile): List<Annotation>

}