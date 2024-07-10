package cengine.psi.core

/**
 * Service for managing PSI-related operations
 */
interface PsiService {
    fun createFile(name: String, content: String): PsiFile
    fun findElementAt(file: PsiFile, offset: Int): PsiElement?
    fun findReferences(element: PsiElement): List<PsiReference>
}