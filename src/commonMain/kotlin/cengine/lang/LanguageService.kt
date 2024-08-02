package cengine.lang

import cengine.editor.annotation.AnnotationProvider
import cengine.editor.annotation.Severity
import cengine.editor.completion.CompletionProvider
import cengine.editor.folding.CodeFoldingProvider
import cengine.editor.highlighting.HighlightProvider
import cengine.editor.text.Informational
import cengine.editor.widgets.WidgetProvider
import cengine.psi.core.PsiFile
import cengine.psi.core.PsiParser

interface LanguageService {

    val name: String
    val fileSuffix: String

    val psiParser: PsiParser

    val codeFoldingProvider: CodeFoldingProvider?
    val widgetProvider: WidgetProvider?
    val completionProvider: CompletionProvider?
    val annotationProvider: AnnotationProvider?
    val highlightProvider: HighlightProvider?

    fun severityToColor(type: Severity): Int?

    fun updateAnalytics(file: PsiFile, informational: Informational?) {

        if (informational != null) {
            codeFoldingProvider?.getFoldingRegions(file, informational)
        }
        widgetProvider?.getWidgets(file)
        completionProvider?.buildCompletionSet(file)
        annotationProvider?.getAnnotations(file)
        highlightProvider?.getHighlights(file)
    }

}