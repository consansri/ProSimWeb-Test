package cengine.editor.completion

import cengine.psi.core.PsiFile

interface CompletionProvider {
    fun getCompletions(psiFile: PsiFile, offset: Int): List<CompletionItem>
}
