package cengine.lang

import cengine.editor.annotation.AnnotationProvider
import cengine.editor.annotation.Notation
import cengine.editor.completion.CompletionProvider
import cengine.editor.folding.CodeFoldingProvider
import cengine.editor.formatting.Formatter
import cengine.editor.highlighting.HighlightProvider
import cengine.editor.text.Informational
import cengine.editor.widgets.WidgetProvider
import cengine.psi.core.PsiFile
import cengine.psi.core.PsiParser
import cengine.psi.core.PsiService
import cengine.vfs.VirtualFile

interface LanguageService {

    val name: String
    val fileSuffix: String

    val psiParser: PsiParser
    val psiService: PsiService

    val runConfigurations: Set<RunConfiguration<LanguageService>>

    val codeFoldingProvider: CodeFoldingProvider?
    val widgetProvider: WidgetProvider?
    val completionProvider: CompletionProvider?
    val annotationProvider: AnnotationProvider?
    val highlightProvider: HighlightProvider?
    val formatter: Formatter?

    val annotations: MutableMap<VirtualFile, Set<Notation>>

    fun updateAnalytics(file: PsiFile, informational: Informational?) {
        if (informational != null) {
            codeFoldingProvider?.updateFoldRegions(file, informational)
        }
        widgetProvider?.updateWidgets(file)
        completionProvider?.buildCompletionSet(file)
        annotationProvider?.updateAnnotations(file)
        annotations.remove(file.file)
        annotations[file.file] = psiService.collectNotations(file)
    }

}