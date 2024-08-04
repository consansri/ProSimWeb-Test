package cengine.lang.asm.features

import cengine.editor.annotation.AnnotationProvider
import cengine.editor.annotation.Notation
import cengine.psi.core.PsiElement
import cengine.psi.core.PsiElementVisitor
import cengine.psi.core.PsiFile

class AsmAnnotator : AnnotationProvider {
    override val cachedNotations: MutableMap<PsiFile, List<Notation>> = mutableMapOf()

    override fun updateAnnotations(psiFile: PsiFile) {
        val collector = AnnotationCollector()
        psiFile.accept(collector)
        cachedNotations.remove(psiFile)
        cachedNotations[psiFile] = collector.notations
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