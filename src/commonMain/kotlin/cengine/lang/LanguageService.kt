package cengine.lang

import cengine.editor.annotation.AnnotationProvider
import cengine.editor.completion.CompletionProvider
import cengine.editor.formatting.Formatter
import cengine.editor.highlighting.HighlightProvider
import cengine.psi.core.PsiFile
import cengine.psi.core.PsiParser
import cengine.psi.core.PsiService

abstract class LanguageService {

    abstract val name: String
    abstract val fileSuffix: String

    abstract val psiParser: PsiParser<*>
    abstract val psiService: PsiService

    abstract val runConfig: Runner<*>
    abstract val completionProvider: CompletionProvider?
    abstract val annotationProvider: AnnotationProvider?
    abstract val highlightProvider: HighlightProvider?
    abstract val formatter: Formatter?

    fun updateAnalytics(file: PsiFile) {
        completionProvider?.buildCompletionSet(file)
        annotationProvider?.updateAnnotations(file)
    }
}