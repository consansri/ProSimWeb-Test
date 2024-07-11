package cengine.psi.core

import cengine.lang.LanguageService

/**
 * Represents a file in the PSI structure
 */
interface PsiFile: PsiElement {
    val lang: LanguageService?
    val name: String
    val text: String

    fun updateFrom(content: String)
}