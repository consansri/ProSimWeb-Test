package cengine.lang.asm

import cengine.editor.annotation.AnnotationProvider
import cengine.editor.annotation.Severity
import cengine.editor.completion.CompletionProvider
import cengine.editor.folding.CodeFoldingProvider
import cengine.editor.highlighting.Highlight
import cengine.editor.highlighting.HighlightProvider
import cengine.editor.widgets.WidgetProvider
import cengine.lang.LanguageService
import cengine.lang.asm.features.AsmCompleter
import emulator.kit.assembler.Assembler

class AsmLang(val assembler: Assembler): LanguageService {
    override val name: String = "Assembly"
    override val fileSuffix: String = ".s"
    override val psiParser: AsmPsiParser = AsmPsiParser(assembler, this)
    override val codeFoldingProvider: CodeFoldingProvider? = null
    override val widgetProvider: WidgetProvider? = null
    override val completionProvider: CompletionProvider = AsmCompleter()
    override val annotationProvider: AnnotationProvider? = null
    override val highlightProvider: HighlightProvider? = null

    override fun hlToColor(type: Highlight.Type): Int {
        TODO("Not yet implemented")
    }

    override fun severityToColor(type: Severity): Int? {
        TODO("Not yet implemented")
    }
}