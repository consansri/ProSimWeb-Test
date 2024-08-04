package cengine.lang.asm.features

import cengine.editor.completion.Completion
import cengine.editor.completion.CompletionItemKind
import cengine.editor.completion.CompletionProvider
import cengine.editor.completion.CompletionProvider.Companion.asCompletions
import cengine.editor.text.TextModel
import cengine.lang.asm.ast.AsmSpec
import cengine.lang.asm.ast.gas.GASDirType
import cengine.lang.asm.ast.gas.GASNode
import cengine.lang.asm.psi.AsmFile
import cengine.psi.core.PsiElement
import cengine.psi.core.PsiElementVisitor
import cengine.psi.core.PsiFile
import emulator.kit.nativeLog

class AsmCompleter(asmSpec: AsmSpec) : CompletionProvider {

    val labels: MutableMap<PsiFile, List<String>> = mutableMapOf()
    val directives: Set<String> = (GASDirType.entries + asmSpec.additionalDirectives()).map { "."+ it.getDetectionString().lowercase() }.filter { it.isNotEmpty() }.toSet()
    val instructions: Set<String> = asmSpec.allInstrTypes().map { it.getDetectionName() }.toSet()

    override fun fetchCompletions(textModel: TextModel, offset: Int, prefix: String, psiFile: PsiFile?): List<Completion> {
        val directives = directives.asCompletions(prefix, true, CompletionItemKind.KEYWORD)
        val instructions = instructions.asCompletions(prefix, true, CompletionItemKind.KEYWORD)
        val labels = labels[psiFile]?.asCompletions(prefix, false, CompletionItemKind.VARIABLE) ?: listOf()
        return labels + instructions + directives
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
                is GASNode.Label -> {
                    labels.add(element.identifier)
                }
            }
        }
    }

}