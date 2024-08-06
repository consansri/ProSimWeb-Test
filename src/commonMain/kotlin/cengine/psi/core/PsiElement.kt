package cengine.psi.core

import cengine.editor.annotation.Notation

/**
 * Base Element for all PSI elements
 */
interface PsiElement : Interval {

    val pathName: String
    val parent: PsiElement?
    val children: List<PsiElement>
    val notations: List<Notation>

    val additionalInfo: String

    override var range: IntRange

    fun print(prefix: String): String = "$prefix${this::class.simpleName}: $additionalInfo\n" + ArrayList(children).joinToString("\n") { it.print(prefix + "\t") }

    fun inserted(index: Int, value: String) {
        range = IntRange(range.first, range.last + value.length)
        val affectedChildren = children.filter { index <= it.range.last }
        affectedChildren.forEach { child ->
            when {
                index <= child.range.first -> {
                    child.move(value.length)
                }

                index in child.range -> {
                    child.inserted(index, value)
                }
            }
        }
    }

    fun deleted(start: Int, end: Int) {
        val length = end - start
        range = IntRange(range.first, range.last - length)
        val affectedChildren = children
        affectedChildren.forEach { child ->
            when {
                end <= child.range.first -> {
                    child.move(-length)
                }

                start >= child.range.last -> {
                    // No change needed
                }

                else -> {
                    val childStart = start.coerceAtLeast(child.range.first)
                    val childEnd = end.coerceAtMost(child.range.last)
                    child.deleted(childStart, childEnd)
                }
            }
        }
    }

    fun move(offset: Int) {
        range = IntRange(range.first + offset, range.last + offset)
        children.forEach {
            it.move(offset)
        }
    }

    fun accept(visitor: PsiElementVisitor)
}