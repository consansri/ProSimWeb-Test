package cengine.ast.node

import cengine.psi.core.TextRange

data class Program(override val children: List<ASTNode>, override val range: TextRange): ASTNode()
