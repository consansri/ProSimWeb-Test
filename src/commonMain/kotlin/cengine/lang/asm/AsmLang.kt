package cengine.lang.asm

import cengine.editor.annotation.AnnotationProvider
import cengine.editor.annotation.Severity
import cengine.editor.completion.CompletionProvider
import cengine.editor.folding.CodeFoldingProvider
import cengine.editor.highlighting.Highlight
import cengine.editor.highlighting.HighlightProvider
import cengine.editor.widgets.WidgetProvider
import cengine.lang.LanguageService
import cengine.psi.core.PsiParser

object AsmLang: LanguageService {
    override val name: String = "Assembly"
    override val fileSuffix: String = ".s"
    override val psiParser: PsiParser
        get() = TODO("Not yet implemented")
    override val codeFoldingProvider: CodeFoldingProvider? = null
    override val widgetProvider: WidgetProvider? = null
    override val completionProvider: CompletionProvider? = null
    override val annotationProvider: AnnotationProvider? = null
    override val highlightProvider: HighlightProvider? = null

    override fun hlToColor(type: Highlight.Type): Int {
        TODO("Not yet implemented")
    }

    override fun severityToColor(type: Severity): Int? {
        TODO("Not yet implemented")
    }
}