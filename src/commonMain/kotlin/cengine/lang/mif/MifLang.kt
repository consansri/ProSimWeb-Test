package cengine.lang.mif

import cengine.editor.annotation.Annotation
import cengine.editor.annotation.AnnotationProvider
import cengine.editor.completion.CompletionProvider
import cengine.editor.formatting.Formatter
import cengine.editor.highlighting.HighlightProvider
import cengine.lang.LanguageService
import cengine.lang.RunConfiguration
import cengine.lang.mif.features.MifHighlighter
import cengine.psi.core.PsiService
import cengine.psi.impl.PsiServiceImpl
import cengine.vfs.VirtualFile

object MifLang : LanguageService {
    override val name: String = "MIF"
    override val fileSuffix: String = ".mif"
    override val psiParser: MifParser = MifParser
    override val psiService: PsiService = PsiServiceImpl(psiParser)
    override val runConfigurations: Set<RunConfiguration<LanguageService>> = setOf()
    override val completionProvider: CompletionProvider? = null
    override val annotationProvider: AnnotationProvider? = null
    override val highlightProvider: HighlightProvider = MifHighlighter()
    override val formatter: Formatter? = null
    override val annotations: MutableMap<VirtualFile, Set<Annotation>> = mutableMapOf()
}