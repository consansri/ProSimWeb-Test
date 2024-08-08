package cengine.lang.asm

import cengine.editor.annotation.AnnotationProvider
import cengine.editor.annotation.Notation
import cengine.editor.completion.CompletionProvider
import cengine.editor.folding.CodeFoldingProvider
import cengine.editor.formatting.Formatter
import cengine.editor.highlighting.HighlightProvider
import cengine.editor.widgets.WidgetProvider
import cengine.lang.LanguageService
import cengine.lang.asm.ast.AsmSpec
import cengine.lang.asm.features.*
import cengine.lang.asm.target.risc2.IKRR2Spec
import cengine.lang.asm.target.riscv.rv32.RV32Spec
import cengine.lang.asm.target.riscv.rv64.RV64Spec
import cengine.psi.core.PsiService
import cengine.psi.impl.PsiServiceImpl
import cengine.vfs.VirtualFile

class AsmLang(spec: AsmSpec) : LanguageService {
    companion object {
        val specs = setOf(RV64Spec, RV32Spec, IKRR2Spec)
    }

    var spec: AsmSpec = specs.first()
        set(value) {
            field = value
            psiParser = AsmPsiParser(value, this)
            psiService = PsiServiceImpl(psiParser)
            completionProvider = AsmCompleter(value)
            highlightProvider = AsmHighlighter(value)
            annotations.clear()
        }

    override val name: String = "Assembly"
    override val fileSuffix: String = ".s"
    override var psiParser: AsmPsiParser = AsmPsiParser(spec, this)
    override var psiService: PsiService = PsiServiceImpl(psiParser)
    override val codeFoldingProvider: CodeFoldingProvider = AsmFolder()
    override val widgetProvider: WidgetProvider = AsmWidgets()
    override var completionProvider: CompletionProvider = AsmCompleter(spec)
    override val annotationProvider: AnnotationProvider = AsmAnnotator()
    override var highlightProvider: HighlightProvider = AsmHighlighter(spec)
    override val formatter: Formatter = AsmFormatter()
    override val annotations: MutableMap<VirtualFile, Set<Notation>> = mutableMapOf()
}