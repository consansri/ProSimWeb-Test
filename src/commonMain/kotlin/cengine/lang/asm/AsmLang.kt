package cengine.lang.asm

import cengine.editor.annotation.AnnotationProvider
import cengine.editor.annotation.Severity
import cengine.editor.completion.CompletionProvider
import cengine.editor.folding.CodeFoldingProvider
import cengine.editor.formatting.Formatter
import cengine.editor.highlighting.HighlightProvider
import cengine.editor.widgets.WidgetProvider
import cengine.lang.LanguageService
import cengine.lang.asm.ast.AsmSpec
import cengine.lang.asm.features.*
import cengine.psi.core.PsiService
import cengine.psi.impl.PsiServiceImpl

class AsmLang(val spec: AsmSpec): LanguageService {
    override val name: String = "Assembly"
    override val fileSuffix: String = ".s"
    override val psiParser: AsmPsiParser = AsmPsiParser(spec, this)
    override val psiService: PsiService = PsiServiceImpl(psiParser)
    override val codeFoldingProvider: CodeFoldingProvider = AsmFolder()
    override val widgetProvider: WidgetProvider? = null
    override val completionProvider: CompletionProvider = AsmCompleter(spec)
    override val annotationProvider: AnnotationProvider = AsmAnnotator()
    override val highlightProvider: HighlightProvider = AsmHighlighter(spec)
    override val formatter: Formatter = AsmFormatter()

    override fun severityToColor(type: Severity): Int? {
        return when(type){
            Severity.INFO -> null
            Severity.WARNING -> CodeStyle.YELLOW.lightHexColor
            Severity.ERROR -> CodeStyle.RED.lightHexColor
        }
    }
}