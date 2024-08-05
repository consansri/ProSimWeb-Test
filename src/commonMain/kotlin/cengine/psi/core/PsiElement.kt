package cengine.psi.core

import cengine.editor.annotation.Notation

/**
 * Base Element for all PSI elements
 */
interface PsiElement : Locatable {

    val pathName: String
    val parent: PsiElement?
    val children: List<PsiElement>
    val notations: List<Notation>

    val additionalInfo: String

    override var textRange: TextRange

    fun print(prefix: String): String = "$prefix${this::class.simpleName}: $additionalInfo\n" + ArrayList(children).joinToString("\n") { it.print(prefix + "\t") }

    suspend fun inserted(index: Int, value: String) {
        textRange = textRange.expand(value.length)
        children.forEach {
            when {
                index < it.textRange.startOffset.index -> {}
                index > it.textRange.endOffset.index -> {
                    it.moveTextRange(value.length)
                }

                index in it.textRange -> {
                    // affected
                    it.inserted(index, value)
                }
            }
        }
    }

    suspend fun deleted(start: Int, end: Int) {
        textRange = textRange.shrink(end - start)
        children.forEach {
            when {
                end < it.textRange.startOffset.index -> {}
                start > it.textRange.endOffset.index -> {
                    it.moveTextRange(start - end)
                }

                else -> {
                    // affected
                    it.deleted(start.coerceIn(textRange.toIntRange()), end.coerceIn(textRange.toIntRange()))
                }
            }
        }
    }

    fun moveTextRange(offset: Int) {
        textRange = textRange.move(offset)
    }

    fun accept(visitor: PsiElementVisitor)
}