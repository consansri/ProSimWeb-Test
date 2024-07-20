package cengine.editor.completion

import cengine.editor.text.TextModel
import cengine.psi.core.PsiFile

interface CompletionProvider {
    fun getCompletions(textModel: TextModel, offset: Int, prefix: String, psiFile: PsiFile? = null): List<Completion>

    fun buildCompletionSet(file: PsiFile)


    companion object {
        fun List<String>.asCompletions(prefix: String): List<Completion> {
            return this
                .filter { it.startsWith(prefix) && prefix.length != it.length }
                .map { keyword ->
                    Completion(
                        keyword,
                        keyword.substring(prefix.length),
                    )
                }
        }
    }


}
