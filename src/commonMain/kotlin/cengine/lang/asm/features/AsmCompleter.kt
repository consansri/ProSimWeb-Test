package cengine.lang.asm.features

import cengine.editor.completion.Completion
import cengine.editor.completion.CompletionProvider
import cengine.editor.completion.CompletionProvider.Companion.asCompletions
import cengine.editor.text.TextModel
import cengine.lang.asm.psi.AsmFile
import cengine.lang.asm.psi.stmnt.AsmLabel
import cengine.psi.core.PsiElement
import cengine.psi.core.PsiElementVisitor
import cengine.psi.core.PsiFile
import emulator.kit.nativeLog

class AsmCompleter : CompletionProvider {

    val labels: MutableMap<PsiFile, List<String>> = mutableMapOf()

    override fun getCompletions(textModel: TextModel, offset: Int, prefix: String, psiFile: PsiFile?): List<Completion> {
        return labels[psiFile]?.asCompletions(prefix) ?: listOf()
    }

    override fun buildCompletionSet(file: PsiFile) {
        if (file !is AsmFile) return

        val builder = CompletionSetBuilder()
        file.accept(builder)
        labels.clear() // cleares all label completions currently
        labels.remove(file)
        labels[file] = builder.labels
        nativeLog("CompletionSet ${file.name} = ${labels[file]}")
    }

    private class CompletionSetBuilder : PsiElementVisitor {
        val labels = mutableListOf<String>()

        override fun visitFile(file: PsiFile) {

        }

        override fun visitElement(element: PsiElement) {
            when (element) {
                is AsmLabel -> {
                    labels.add(element.name.removeSuffix(":"))
                }
            }
        }
    }

}