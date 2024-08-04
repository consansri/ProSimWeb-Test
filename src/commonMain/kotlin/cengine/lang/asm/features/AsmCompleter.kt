package cengine.lang.asm.features

import cengine.editor.annotation.Notation
import cengine.editor.completion.Completion
import cengine.editor.completion.CompletionItemKind
import cengine.editor.completion.CompletionProvider
import cengine.editor.completion.CompletionProvider.Companion.asCompletions
import cengine.editor.text.TextModel
import cengine.lang.asm.ast.AsmSpec
import cengine.lang.asm.ast.gas.GASDirType
import cengine.lang.asm.ast.gas.GASNode
import cengine.lang.asm.lexer.AsmTokenType
import cengine.lang.asm.psi.AsmFile
import cengine.psi.core.PsiElement
import cengine.psi.core.PsiElementVisitor
import cengine.psi.core.PsiFile

class AsmCompleter(asmSpec: AsmSpec) : CompletionProvider {

    val labels: MutableMap<PsiFile, Set<String>> = mutableMapOf()
    val macros: MutableMap<PsiFile, Set<String>> = mutableMapOf()
    val directives: Set<String> = (GASDirType.entries + asmSpec.additionalDirectives()).map { "." + it.getDetectionString().lowercase() }.filter { it.isNotEmpty() }.toSet()
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
        labels.clear()
        macros.clear()
        labels.remove(file)
        macros.remove(file)
        labels[file] = builder.labels
        macros[file] = builder.macros
    }

    private class CompletionSetBuilder : PsiElementVisitor {
        val labels = mutableSetOf<String>()
        val macros = mutableSetOf<String>()

        override fun visitFile(file: PsiFile) {

        }

        override fun visitElement(element: PsiElement) {
            when (element) {
                is GASNode.Label -> {
                    if (labels.contains(element.identifier)) {
                        element.notations.add(Notation.error(element, "Label is already defined!"))
                    } else {
                        labels.add(element.identifier)
                    }
                }

                is GASNode.Directive -> {
                    when (element.type) {
                        GASDirType.MACRO -> {
                            val identifier = element.allTokens.firstOrNull { it.type == AsmTokenType.SYMBOL }
                            if (identifier != null) {
                                if (macros.contains(identifier.value)) {
                                    element.notations.add(Notation.error(element, "Macro is already defined!"))
                                } else {
                                    macros.add(identifier.value)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

}