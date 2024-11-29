package cengine.editor.annotation

import androidx.compose.runtime.snapshots.SnapshotStateMap
import cengine.psi.core.PsiFile

interface AnnotationProvider {

    val cachedNotations: SnapshotStateMap<PsiFile, List<Annotation>>

    fun updateAnnotations(psiFile: PsiFile)

}