package cengine.psi.core

import cengine.editor.text.TextModel
import cengine.psi.lexer.core.Token
import cengine.vfs.VirtualFile

/**
 * Language-specific parser interface
 */
interface PsiParser {
    fun parseFile(file: VirtualFile, textModel: TextModel?): PsiFile

    class NodeException(element: PsiElement, message: String): Exception("${element.range}: $message")
    class TokenException(element: Token, message: String): Exception("${element.range}: $message")
}