package cengine.lang.asm.features

import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.snapshots.SnapshotStateMap
import cengine.editor.annotation.Annotation
import cengine.editor.annotation.AnnotationProvider
import cengine.psi.core.PsiElement
import cengine.psi.core.PsiElementVisitor
import cengine.psi.core.PsiFile

class AsmAnnotator : AnnotationProvider {
    override val cachedNotations: SnapshotStateMap<PsiFile, List<Annotation>> = mutableStateMapOf()

    override fun updateAnnotations(psiFile: PsiFile) {
        val collector = AnnotationCollector()
        psiFile.accept(collector)
        cachedNotations.remove(psiFile)
        cachedNotations[psiFile] = collector.annotations
    }

    inner class AnnotationCollector : PsiElementVisitor {
        val annotations = mutableListOf<Annotation>()
        override fun visitFile(file: PsiFile) {

        }

        override fun visitElement(element: PsiElement) {
            annotations.addAll(element.annotations)
        }
    }
}