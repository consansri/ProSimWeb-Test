package cengine.psi.core

/**
 * Represents a file in the PSI structure
 */
interface PsiFile: PsiElement {
    val name: String
    val text: String
}