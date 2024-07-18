package cengine.psi.impl

import cengine.psi.core.*

class PsiServiceImpl(
    private val parser: PsiParser
) : PsiService {
    override fun createFile(name: String, content: String): PsiFile {
        return parser.parseFile(content, name)
    }

    override fun findElementAt(file: PsiFile, offset: Int): PsiElement? {
        class Finder(private val targetOffset: Int) : PsiElementVisitor {
            var result: PsiElement? = null

            override fun visitFile(file: PsiFile) {
                file.children.forEach {
                    visitElement(it)
                }
            }

            override fun visitElement(element: PsiElement) {
                if (element.textRange.startOffset <= targetOffset && targetOffset < element.textRange.endOffset) {
                    result = element
                    element.children.forEach { it.accept(this) }
                }
            }
        }

        val finder = Finder(offset)
        file.accept(finder)
        return finder.result
    }

    override fun findReferences(element: PsiElement): List<PsiReference> {
        // This is a simplistic implementation. In a real-world scenario, you'd need a more sophisticated approach to find references.
        val references = mutableListOf<PsiReference>()

        class ReferenceFinder : PsiElementVisitor {
            override fun visitFile(file: PsiFile) {
                file.children.forEach {
                    visitElement(it)
                }
            }
            override fun visitElement(element: PsiElement) {
                if (element is PsiReference && element.isReferenceTo(element)) {
                    references.add(element)
                }
                element.children.forEach { it.accept(this) }
            }
        }

        val root = generateSequence(element) { it.parent }.last()
        root.accept(ReferenceFinder())
        return references
    }
}