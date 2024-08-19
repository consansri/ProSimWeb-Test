package cengine.lang.asm

import cengine.editor.annotation.AnnotationProvider
import cengine.editor.annotation.Notation
import cengine.editor.completion.CompletionProvider
import cengine.editor.folding.CodeFoldingProvider
import cengine.editor.formatting.Formatter
import cengine.editor.highlighting.HighlightProvider
import cengine.editor.widgets.WidgetProvider
import cengine.lang.LanguageService
import cengine.lang.asm.ast.TargetSpec
import cengine.lang.asm.features.*
import cengine.psi.core.PsiService
import cengine.psi.impl.PsiServiceImpl
import cengine.vfs.VirtualFile

class AsmLang(spec: TargetSpec) : LanguageService {

    var spec: TargetSpec = spec
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