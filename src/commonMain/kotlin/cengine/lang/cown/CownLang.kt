package cengine.lang.cown

import cengine.editor.annotation.AnnotationProvider
import cengine.editor.annotation.Severity
import cengine.editor.completion.CompletionProvider
import cengine.editor.folding.CodeFoldingProvider
import cengine.editor.highlighting.Highlight
import cengine.editor.highlighting.Highlight.Type.*
import cengine.editor.highlighting.HighlightProvider
import cengine.editor.widgets.WidgetProvider
import cengine.lang.LanguageService
import cengine.lang.cown.psi.CownHighlighter
import cengine.lang.cown.psi.CownPsiParser
import cengine.lang.cown.psi.CownWidgets
import cengine.psi.core.PsiParser
import emulator.kit.assembler.CodeStyle

object CownLang: LanguageService {
    override val name: String = "cown"
    override val fileSuffix: String = ".cown"
    override val psiParser: PsiParser = CownPsiParser()
    override val codeFoldingProvider: CodeFoldingProvider? = null
    override val widgetProvider: WidgetProvider = CownWidgets()
    override val completionProvider: CompletionProvider? = null
    override val annotationProvider: AnnotationProvider? = null
    override val highlightProvider: HighlightProvider = CownHighlighter()

    override fun hlToColor(type: Highlight.Type): Int {
        return when(type){
            KEYWORD -> CodeStyle.BLUE.lightHexColor
            FUNCTION -> CodeStyle.CYAN.lightHexColor
            VARIABLE -> CodeStyle.MAGENTA.lightHexColor
            STRING -> CodeStyle.GREEN.lightHexColor
            COMMENT -> CodeStyle.BASE3.lightHexColor
        }
    }

    override fun severityToColor(type: Severity): Int {
        return when(type){
            Severity.INFO -> CodeStyle.BASE0.lightHexColor
            Severity.WARNING -> CodeStyle.YELLOW.lightHexColor
            Severity.ERROR -> CodeStyle.RED.lightHexColor
        }
    }
}