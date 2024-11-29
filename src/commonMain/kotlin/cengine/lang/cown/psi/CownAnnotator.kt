package cengine.lang.cown.psi

import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.snapshots.SnapshotStateMap
import cengine.editor.annotation.Annotation
import cengine.editor.annotation.AnnotationProvider
import cengine.psi.core.PsiFile

class CownAnnotator : AnnotationProvider {
    override var cachedNotations: SnapshotStateMap<PsiFile, List<Annotation>> = mutableStateMapOf()

    override fun updateAnnotations(psiFile: PsiFile) {

    }
}