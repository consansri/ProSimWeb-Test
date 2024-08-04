package cengine.lang.cown

import cengine.editor.annotation.AnnotationProvider
import cengine.editor.annotation.Severity
import cengine.editor.completion.CompletionProvider
import cengine.editor.folding.CodeFoldingProvider
import cengine.editor.formatting.Formatter
import cengine.editor.highlighting.HighlightProvider
import cengine.editor.widgets.WidgetProvider
import cengine.lang.LanguageService
import cengine.lang.cown.psi.*
import cengine.psi.core.PsiParser
import cengine.psi.core.PsiService
import cengine.psi.impl.PsiServiceImpl
import emulator.kit.assembler.CodeStyle

object CownLang: LanguageService {
    override val name: String = "cown"
    override val fileSuffix: String = ".cown"
    override val psiParser: PsiParser = CownPsiParser()
    override val psiService: PsiService = PsiServiceImpl(psiParser)
    override val codeFoldingProvider: CodeFoldingProvider = CownFolder()
    override val widgetProvider: WidgetProvider = CownWidgets()
    override val completionProvider: CompletionProvider = CownCompleter()
    override val annotationProvider: AnnotationProvider = CownAnnotator()
    override val highlightProvider: HighlightProvider = CownHighlighter()
    override val formatter: Formatter? = null

    override fun severityToColor(type: Severity): Int {
        return when(type){
            Severity.INFO -> CodeStyle.BASE0.lightHexColor
            Severity.WARNING -> CodeStyle.YELLOW.lightHexColor
            Severity.ERROR -> CodeStyle.RED.lightHexColor
        }
    }


}