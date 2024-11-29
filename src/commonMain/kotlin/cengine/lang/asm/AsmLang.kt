package cengine.lang.asm

import cengine.editor.annotation.AnnotationProvider
import cengine.editor.completion.CompletionProvider
import cengine.editor.formatting.Formatter
import cengine.editor.highlighting.HighlightProvider
import cengine.lang.LanguageService
import cengine.lang.Runner
import cengine.lang.asm.ast.TargetSpec
import cengine.lang.asm.features.AsmAnnotator
import cengine.lang.asm.features.AsmCompleter
import cengine.lang.asm.features.AsmFormatter
import cengine.lang.asm.features.AsmHighlighter
import cengine.psi.core.PsiService
import cengine.psi.impl.PsiServiceImpl

class AsmLang(spec: TargetSpec<*>) : LanguageService() {
    companion object {
        const val OUTPUT_DIR = ".asm"
    }

    var spec: TargetSpec<*> = spec
        set(value) {
            field = value
            psiParser = AsmPsiParser(value, this)
            psiService = PsiServiceImpl(psiParser)
            completionProvider = AsmCompleter(value)
            highlightProvider = AsmHighlighter(value)
        }

    override var runConfig: Runner<AsmLang> = AsmRunner(this)
    override val name: String = "Assembly"
    override val fileSuffix: String = ".s"
    override var psiParser: AsmPsiParser = AsmPsiParser(spec, this)
    override var psiService: PsiService = PsiServiceImpl(psiParser)
    override var completionProvider: CompletionProvider = AsmCompleter(spec)
    override val annotationProvider: AnnotationProvider = AsmAnnotator()
    override var highlightProvider: HighlightProvider = AsmHighlighter(spec)
    override val formatter: Formatter = AsmFormatter()
}