package cengine.psi.impl

import cengine.psi.core.*

class PsiServiceImpl(
    private val parser: PsiParser,
    private val elementFactory: PsiElementFactory
) : PsiService {
    override fun createFile(name: String, content: String): PsiFile {
        return parser.parseFile(content, name)
    }

    override fun findElementAt(file: PsiFile, offset: Int): PsiElement? {
        class Finder(private val targetOffset: Int) : PsiElementVisitor() {
            var result: PsiElement? = null

            override fun visitElement(visitedElement: PsiElement) {
                if (visitedElement.textRange.startOffset <= targetOffset && targetOffset < visitedElement.textRange.endOffset) {
                    result = visitedElement
                    visitedElement.children.forEach { it.accept(this) }
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

        class ReferenceFinder : PsiElementVisitor() {
            override fun visitElement(visitedElement: PsiElement) {
                if (visitedElement is PsiReference && visitedElement.isReferenceTo(element)) {
                    references.add(visitedElement)
                }
                visitedElement.children.forEach { it.accept(this) }
            }
        }

        val root = generateSequence(element) { it.parent }.last()
        root.accept(ReferenceFinder())
        return references
    }
}