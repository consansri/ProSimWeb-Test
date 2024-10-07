package cengine.lang.cown

import cengine.editor.annotation.Annotation
import cengine.editor.annotation.AnnotationProvider
import cengine.editor.completion.CompletionProvider
import cengine.editor.formatting.Formatter
import cengine.editor.highlighting.HighlightProvider
import cengine.lang.LanguageService
import cengine.lang.RunConfiguration
import cengine.lang.cown.psi.CownAnnotator
import cengine.lang.cown.psi.CownCompleter
import cengine.lang.cown.psi.CownPsiFile
import cengine.lang.cown.psi.CownPsiParser
import cengine.psi.core.PsiParser
import cengine.psi.core.PsiService
import cengine.psi.impl.PsiServiceImpl
import cengine.vfs.VirtualFile

class CownLang: LanguageService {
    override val name: String = "cown"
    override val fileSuffix: String = ".cown"
    override val psiParser: PsiParser<CownPsiFile> = CownPsiParser(this)
    override val psiService: PsiService = PsiServiceImpl(psiParser)
    override val runConfigurations: Set<RunConfiguration<LanguageService>> = setOf()
    override val completionProvider: CompletionProvider = CownCompleter()
    override val annotationProvider: AnnotationProvider = CownAnnotator()
    override val highlightProvider: HighlightProvider? = null
    override val formatter: Formatter? = null
    override val annotations: MutableMap<VirtualFile, Set<Annotation>> = mutableMapOf()
}