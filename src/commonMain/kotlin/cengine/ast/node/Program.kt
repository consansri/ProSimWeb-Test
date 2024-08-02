package cengine.ast.node

import cengine.psi.core.TextRange

data class Program(override val children: List<Node>, override var textRange: TextRange): Node()
