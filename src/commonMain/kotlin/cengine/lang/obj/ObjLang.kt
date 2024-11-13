package cengine.lang.obj

import cengine.editor.annotation.Annotation
import cengine.editor.annotation.AnnotationProvider
import cengine.editor.completion.CompletionProvider
import cengine.editor.formatting.Formatter
import cengine.editor.highlighting.HighlightProvider
import cengine.lang.LanguageService
import cengine.lang.RunConfiguration
import cengine.lang.obj.run.Mif
import cengine.psi.core.PsiParser
import cengine.psi.core.PsiService
import cengine.psi.impl.PsiServiceImpl
import cengine.vfs.VirtualFile

object ObjLang: LanguageService {

    const val OUTPUT_DIR = ".obj"

    override val name: String = "ObjLang"
    override val fileSuffix: String = ".o"
    override val psiParser: PsiParser<*> = ObjPsiParser
    override val psiService: PsiService = PsiServiceImpl(psiParser)
    override val runConfigurations: Set<RunConfiguration<LanguageService>> = setOf(Mif)
    override val completionProvider: CompletionProvider?= null
    override val annotationProvider: AnnotationProvider? = null
    override val highlightProvider: HighlightProvider? = null
    override val formatter: Formatter? = null
    override val annotations: MutableMap<VirtualFile, Set<Annotation>> = mutableMapOf()

}