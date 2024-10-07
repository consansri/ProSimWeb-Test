package cengine.psi.impl

import cengine.editor.annotation.Annotation
import cengine.psi.core.PsiElement
import cengine.psi.core.PsiElementVisitor
import cengine.psi.core.PsiFile

class PsiNotationCollector(): PsiElementVisitor {
    val annotations = mutableListOf<Annotation>()
    override fun visitFile(file: PsiFile) {
        annotations.addAll(file.annotations)
        file.children.forEach {
            it.accept(this)
        }
    }

    override fun visitElement(element: PsiElement) {
        annotations.addAll(element.annotations)
        element.children.forEach {
            it.accept(this)
        }
    }
}