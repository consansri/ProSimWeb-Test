package cengine.lang

import cengine.editor.annotation.AnnotationProvider
import cengine.editor.annotation.Severity
import cengine.editor.completion.CompletionProvider
import cengine.editor.folding.CodeFoldingProvider
import cengine.editor.highlighting.Highlight
import cengine.editor.highlighting.HighlightProvider
import cengine.editor.widgets.WidgetProvider
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

    fun hlToColor(type: Highlight.Type): Int
    fun severityToColor(type: Severity): Int?

}