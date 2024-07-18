package cengine.ast.node

import cengine.ast.ASTVisitor
import cengine.psi.core.TextRange

sealed class ASTNode {
    abstract val children: List<ASTNode>
    abstract val range: TextRange

    fun accept(visitor: ASTVisitor<*>) {
        when (this) {
            is BlockStatement -> visitor.visitBlockStatement(this)
            is BinaryExpression -> visitor.visitBinaryExpression(this)
            is FunctionCall -> visitor.visitFunctionCall(this)
            is Identifier -> visitor.visitIdentifier(this)
            is Literal -> visitor.visitLiteral(this)
            is FunctionDefinition -> visitor.visitFunctionDefinition(this)
            is Parameter -> visitor.visitParameter(this)
            is Program -> visitor.visitProgram(this)
            is TypeNode -> visitor.visitTypeNode(this)
            is VariableDeclaration -> visitor.visitVariableDeclaration(this)
        }
    }
}