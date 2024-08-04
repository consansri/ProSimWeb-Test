package cengine.lang.cown.psi

import cengine.editor.completion.Completion
import cengine.editor.completion.CompletionItemKind
import cengine.editor.completion.CompletionProvider
import cengine.editor.completion.CompletionProvider.Companion.asCompletions
import cengine.editor.text.TextModel
import cengine.lang.cown.CownLexer
import cengine.psi.core.PsiFile

class CownCompleter : CompletionProvider {

    override fun fetchCompletions(textModel: TextModel, offset: Int, prefix: String, psiFile: PsiFile?): List<Completion> {
        return CownLexer.keywords.asCompletions(prefix, true, CompletionItemKind.KEYWORD)
    }

    override fun buildCompletionSet(file: PsiFile) {
        // Not yet implemented
    }
}