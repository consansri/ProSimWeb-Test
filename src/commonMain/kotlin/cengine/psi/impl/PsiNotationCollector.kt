package cengine.psi.impl

import cengine.editor.annotation.Notation
import cengine.psi.core.PsiElement
import cengine.psi.core.PsiElementVisitor
import cengine.psi.core.PsiFile

class PsiNotationCollector(): PsiElementVisitor {
    val notations = mutableListOf<Notation>()
    override fun visitFile(file: PsiFile) {
        notations.addAll(file.notations)
        file.children.forEach {
            it.accept(this)
        }
    }

    override fun visitElement(element: PsiElement) {
        notations.addAll(element.notations)
        element.children.forEach {
            it.accept(this)
        }
    }
}