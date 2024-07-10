package cengine.psi.core

/**
 * Language-specific parser interface
 */
interface PsiParser {
    fun parseFile(content: String, fileName: String): PsiFile
}