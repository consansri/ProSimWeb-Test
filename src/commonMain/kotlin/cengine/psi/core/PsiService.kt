package cengine.psi.core

import cengine.editor.annotation.Annotation
import cengine.lang.asm.CodeStyle
import cengine.psi.feature.Highlightable
import cengine.util.integer.overlaps
import cengine.vfs.VirtualFile

/**
 * Service for managing PSI-related operations
 */
interface PsiService {
    fun createFile(file: VirtualFile): PsiFile
    fun findElementAt(file: PsiFile, offset: Int): PsiElement?

    /**
     * Searches for references to [element]
     */
    fun findReferences(element: PsiElement): List<PsiReference>

    fun collectHighlights(file: PsiFile, inRange: IntRange): List<Pair<IntRange, CodeStyle>> {
        class HighlightRangeCollector : PsiElementVisitor {
            val highlights = mutableListOf<Pair<IntRange, CodeStyle>>()
            override fun visitFile(file: PsiFile) {
                if (!file.range.overlaps(inRange)) return
                file.children.forEach {
                    it.accept(this)
                }
            }

            override fun visitElement(element: PsiElement) {
                if (!element.range.overlaps(inRange)) return
                if (element is Highlightable) {
                    element.style?.let {
                        highlights.add(element.range to it)
                    }
                }

                element.children.forEach {
                    it.accept(this)
                }
            }
        }

        val collector = HighlightRangeCollector()
        file.accept(collector)
        return collector.highlights
    }

    fun collectNotations(file: PsiFile, inRange: IntRange? = null): Set<Annotation> {
        val all = mutableListOf<Annotation>()
        val collector = NotationRangeCollector(inRange){ element, annotations ->
            all.addAll(annotations)
        }
        file.accept(collector)
        return all.toSet()
    }

    fun annotationsMapped(file: PsiFile, inRange: IntRange? = null): Map<PsiElement, List<Annotation>> {
        val map = mutableMapOf<PsiElement, List<Annotation>>()
        val collector = NotationRangeCollector(inRange){ element, annotations ->
            map[element] = annotations
        }
        file.accept(collector)
        return map
    }

    fun path(of: PsiElement): List<PsiElement> {

        val path = mutableListOf(of)

        var currElement = of

        while (true) {
            val parent = currElement.parent

            if (parent != null) {
                path.add(0, parent)
                currElement = parent
            } else {
                break
            }
        }

        return path
    }

    private class NotationRangeCollector(val inRange: IntRange? = null, val found: (PsiElement, List<Annotation>) -> Unit) : PsiElementVisitor {
        override fun visitFile(file: PsiFile) {
            if (inRange != null && !file.range.overlaps(inRange)) return

            if (file.annotations.isNotEmpty()) {
                found(file, file.annotations)
            }

            file.children.forEach {
                it.accept(this)
            }
        }

        override fun visitElement(element: PsiElement) {
            if (inRange != null && !element.range.overlaps(inRange)) return

            if (element.annotations.isNotEmpty()) {
                found(element, element.annotations)
            }

            element.children.forEach {
                it.accept(this)
            }
        }
    }
}