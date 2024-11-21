package cengine.lang.mif

import cengine.editor.annotation.AnnotationProvider
import cengine.editor.completion.CompletionProvider
import cengine.editor.formatting.Formatter
import cengine.editor.highlighting.HighlightProvider
import cengine.lang.LanguageService
import cengine.lang.Runner
import cengine.lang.mif.features.MifHighlighter
import cengine.psi.core.PsiService
import cengine.psi.impl.PsiServiceImpl

object MifLang : LanguageService() {
    override val name: String = "MIF"
    override val fileSuffix: String = ".mif"
    override val psiParser: MifParser = MifParser
    override val psiService: PsiService = PsiServiceImpl(psiParser)
    override val runConfig: Runner<MifLang> = MifRunner
    override val completionProvider: CompletionProvider? = null
    override val annotationProvider: AnnotationProvider? = null
    override val highlightProvider: HighlightProvider = MifHighlighter()
    override val formatter: Formatter? = null
}