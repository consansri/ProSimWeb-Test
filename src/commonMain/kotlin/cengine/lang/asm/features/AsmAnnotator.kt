package cengine.lang.asm.features

import cengine.editor.annotation.AnnotationProvider
import cengine.editor.annotation.Notation
import cengine.psi.core.PsiElement
import cengine.psi.core.PsiElementVisitor
import cengine.psi.core.PsiFile

class AsmAnnotator : AnnotationProvider {
    override var cachedNotations: List<Notation> = listOf()

    override fun getAnnotations(psiFile: PsiFile): List<Notation> {
        val collector = AnnotationCollector()
        psiFile.accept(collector)
        cachedNotations = collector.notations
        return collector.notations
    }

    inner class AnnotationCollector : PsiElementVisitor {
        val notations = mutableListOf<Notation>()
        override fun visitFile(file: PsiFile) {

        }

        override fun visitElement(element: PsiElement) {
            notations.addAll(element.notations)
        }

    }
}