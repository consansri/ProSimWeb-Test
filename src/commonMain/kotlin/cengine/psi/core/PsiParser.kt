package cengine.psi.core

import cengine.psi.lexer.core.Token
import cengine.vfs.VirtualFile

/**
 * Language-specific parser interface
 */
interface PsiParser<T: PsiFile> {
    fun parse(file: VirtualFile): T

    class NodeException(element: PsiElement, message: String) : Exception("${element.range}: $message")
    class TokenException(element: Token, message: String) : Exception("${element.range}: $message")
}