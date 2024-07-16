package cengine.editor.completion

import cengine.editor.text.TextModel
import cengine.psi.core.PsiFile

interface CompletionProvider {
    fun getCompletions(textModel: TextModel, offset: Int, prefix: String, psiFile: PsiFile? = null): List<Completion>

}
