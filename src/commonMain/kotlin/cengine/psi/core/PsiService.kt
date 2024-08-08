package cengine.psi.core

import cengine.editor.annotation.Notation
import cengine.editor.widgets.Widget
import cengine.vfs.VirtualFile

/**
 * Service for managing PSI-related operations
 */
interface PsiService {
    fun createFile(file: VirtualFile): PsiFile
    fun findElementAt(file: PsiFile, offset: Int): PsiElement?
    fun findReferences(element: PsiElement): List<PsiReference>

    fun collectNotations(file: PsiFile): Set<Notation>{
        class NotationCollector: PsiElementVisitor{
            val notations = mutableSetOf<Notation>()
            override fun visitFile(file: PsiFile) {
                notations.addAll(file.notations)
                file.children.forEach {
                    it.accept(this)
                }
            }

            override fun visitElement(element: PsiElement) {
                notations.addAll(element.notations)
                element.children.forEach {
                    it.accept(this)
                }
            }
        }

        val collector = NotationCollector()
        file.accept(collector)
        return collector.notations
    }

    fun collectInterlineWidgetsInRange(file: PsiFile, range: IntRange): Set<Widget> {
        class InterlineWidgetCollector() : PsiElementVisitor {
            val interlineWidgets = mutableSetOf<Widget>()
            override fun visitFile(file: PsiFile) {
                interlineWidgets.addAll(file.interlineWidgets)
                file.children.filter {
                    when {
                        it.range.first > range.last -> false
                        it.range.last < range.first -> false
                        else -> true
                    }
                }.forEach {
                    it.accept(this)
                }
            }

            override fun visitElement(element: PsiElement) {
                interlineWidgets.addAll(element.interlineWidgets)
                element.children.filter {
                    when {
                        it.range.first > range.last -> false
                        it.range.last < range.first -> false
                        else -> true
                    }
                }.forEach {
                    it.accept(this)
                }
            }
        }

        val builder = InterlineWidgetCollector()
        file.accept(builder)
        return builder.interlineWidgets
    }

    fun collectInlayWidgetsInRange(file: PsiFile, range: IntRange): Set<Widget> {
        class InlayWidgetCollector : PsiElementVisitor {
            val inlayWidgets = mutableSetOf<Widget>()
                get() {
                    return field
                }

            override fun visitFile(file: PsiFile) {
                inlayWidgets.addAll(file.inlayWidgets)
                file.children.filter {
                    when {
                        it.range.first > range.last -> false
                        it.range.last < range.first -> false
                        else -> true
                    }
                }.forEach {
                    it.accept(this)
                }
            }

            override fun visitElement(element: PsiElement) {
                inlayWidgets.addAll(element.inlayWidgets)
                element.children.filter {
                    when {
                        it.range.first > range.last -> false
                        it.range.last < range.first -> false
                        else -> true
                    }
                }.forEach {
                    it.accept(this)
                }
            }
        }

        val builder = InlayWidgetCollector()
        file.accept(builder)
        return builder.inlayWidgets
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