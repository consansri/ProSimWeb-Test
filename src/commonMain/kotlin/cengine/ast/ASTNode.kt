package cengine.ast

import cengine.lexer.core.Token

interface ASTNode {
    val children: List<ASTNode>
    val tokens: List<Token>
}