package cengine.lang.cown

import cengine.editor.annotation.AnnotationProvider
import cengine.editor.completion.CompletionProvider
import cengine.editor.formatting.Formatter
import cengine.editor.highlighting.HighlightProvider
import cengine.lang.LanguageService
import cengine.lang.Runner
import cengine.lang.cown.psi.CownAnnotator
import cengine.lang.cown.psi.CownCompleter
import cengine.lang.cown.psi.CownPsiFile
import cengine.lang.cown.psi.CownPsiParser
import cengine.psi.core.PsiParser
import cengine.psi.core.PsiService
import cengine.psi.impl.PsiServiceImpl

object CownLang: LanguageService() {
    override val name: String = "cown"
    override val fileSuffix: String = ".cown"
    override val psiParser: PsiParser<CownPsiFile> = CownPsiParser(this)
    override val psiService: PsiService = PsiServiceImpl(psiParser)
    override val runConfig: Runner<CownLang> = CownRunner
    override val completionProvider: CompletionProvider = CownCompleter()
    override val annotationProvider: AnnotationProvider = CownAnnotator()
    override val highlightProvider: HighlightProvider? = null
    override val formatter: Formatter? = null
}