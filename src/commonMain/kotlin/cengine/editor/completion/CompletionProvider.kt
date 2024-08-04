package cengine.editor.completion

import cengine.editor.text.TextModel
import cengine.psi.core.PsiFile

interface CompletionProvider {
    fun fetchCompletions(textModel: TextModel, offset: Int, prefix: String, psiFile: PsiFile? = null): List<Completion>

    fun buildCompletionSet(file: PsiFile)

    companion object {
        fun Collection<String>.asCompletions(prefix: String, ignoreCase: Boolean, kind: CompletionItemKind?): List<Completion> {
            return this
                .filter { it.startsWith(prefix, ignoreCase) && prefix.length != it.length }
                .map { keyword ->
                    Completion(
                        keyword,
                        keyword.substring(prefix.length),
                        kind
                    )
                }
        }
    }


}
