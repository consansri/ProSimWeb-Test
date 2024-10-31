package cengine.psi.core

import cengine.editor.annotation.Annotation
import cengine.lang.asm.CodeStyle
import cengine.psi.feature.Highlightable
import cengine.vfs.VirtualFile

/**
 * Service for managing PSI-related operations
 */
interface PsiService {
    fun createFile(file: VirtualFile): PsiFile
    fun findElementAt(file: PsiFile, offset: Int): PsiElement?
    fun findReferences(element: PsiElement): List<PsiReference>

    fun collectHighlights(file: PsiFile): List<Pair<IntRange, CodeStyle>> {
        class HighlightCollector : PsiElementVisitor {
            val highlights = mutableListOf<Pair<IntRange, CodeStyle>>()
            override fun visitFile(file: PsiFile) {
                file.children.forEach {
                    it.accept(this)
                }
            }

            override fun visitElement(element: PsiElement) {
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

        val collector = HighlightCollector()
        file.accept(collector)
        return collector.highlights
    }


    fun collectNotations(file: PsiFile, index: Int? = null): Set<Annotation> {
        class NotationCollector : PsiElementVisitor {
            val annotations = mutableSetOf<Annotation>()
            override fun visitFile(file: PsiFile) {
                if (index != null && index !in file.range) return

                annotations.addAll(file.annotations)

                file.children.forEach {
                    it.accept(this)
                }
            }

            override fun visitElement(element: PsiElement) {
                if (index != null && index !in element.range) return

                annotations.addAll(element.annotations)

                element.children.forEach {
                    it.accept(this)
                }
            }
        }

        val collector = NotationCollector()
        file.accept(collector)
        return collector.annotations
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
}