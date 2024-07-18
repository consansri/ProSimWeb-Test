package cengine.ast.node

import cengine.psi.core.TextRange

data class BlockStatement(
    override val children: List<Statement>,
    override val range: TextRange
): ASTNode()
