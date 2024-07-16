package cengine.lang.cown.psi

import cengine.editor.completion.Completion
import cengine.editor.completion.CompletionProvider
import cengine.editor.text.TextModel
import cengine.lang.cown.CownLexer
import cengine.psi.core.PsiFile

class CownCompleter : CompletionProvider {



    override fun getCompletions(textModel: TextModel, offset: Int, prefix: String, psiFile: PsiFile?): List<Completion> {
        return CownLexer.keywords
            .filter { it.startsWith(prefix, ignoreCase = true) }
            .map { keyword ->
                Completion(
                    keyword,
                    keyword.removePrefix(prefix),
                )
            }
    }
}