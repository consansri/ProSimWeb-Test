package cengine.psi.core

import cengine.psi.lexer.core.Token
import cengine.vfs.VirtualFile

/**
 * Language-specific parser interface
 */
interface PsiParser {
    fun parseFile(file: VirtualFile): PsiFile

    class NodeException(element: PsiElement, message: String): Exception("${element.textRange}: $message")
    class TokenException(element: Token, message: String): Exception("${element.textRange}: $message")
}