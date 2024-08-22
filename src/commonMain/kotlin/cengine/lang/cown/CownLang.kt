package cengine.lang.cown

import cengine.editor.annotation.AnnotationProvider
import cengine.editor.annotation.Notation
import cengine.editor.completion.CompletionProvider
import cengine.editor.folding.CodeFoldingProvider
import cengine.editor.formatting.Formatter
import cengine.editor.highlighting.HighlightProvider
import cengine.editor.widgets.WidgetProvider
import cengine.lang.LanguageService
import cengine.lang.RunConfiguration
import cengine.lang.cown.psi.*
import cengine.psi.core.PsiParser
import cengine.psi.core.PsiService
import cengine.psi.impl.PsiServiceImpl
import cengine.vfs.VirtualFile

class CownLang: LanguageService {
    override val name: String = "cown"
    override val fileSuffix: String = ".cown"
    override val psiParser: PsiParser = CownPsiParser(this)
    override val psiService: PsiService = PsiServiceImpl(psiParser)
    override val runConfigurations: Set<RunConfiguration<LanguageService>> = setOf()
    override val codeFoldingProvider: CodeFoldingProvider = CownFolder()
    override val widgetProvider: WidgetProvider = CownWidgets()
    override val completionProvider: CompletionProvider = CownCompleter()
    override val annotationProvider: AnnotationProvider = CownAnnotator()
    override val highlightProvider: HighlightProvider = CownHighlighter()
    override val formatter: Formatter? = null
    override val annotations: MutableMap<VirtualFile, Set<Notation>> = mutableMapOf()
}