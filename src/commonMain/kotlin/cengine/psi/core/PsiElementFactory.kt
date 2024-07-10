package cengine.psi.core

/**
 * Factory for creating PSI elements
 */
interface PsiElementFactory {
    fun createFile(name: String, content: String): PsiFile
    // Add more factory methods for other PSI elements
}