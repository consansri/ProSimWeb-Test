package cengine.ast

import cengine.ast.node.*

interface ASTVisitor<T> {
    fun visitProgram(node: Program): T
    fun visitFunctionDefinition(node: FunctionDefinition): T
    fun visitParameter(node: Parameter): T
    fun visitBlockStatement(node: BlockStatement): T
    fun visitVariableDeclaration(node: VariableDeclaration): T
    fun visitBinaryExpression(node: BinaryExpression): T
    fun visitLiteral(node: Literal): T
    fun visitIdentifier(node: Identifier): T
    fun visitFunctionCall(node: FunctionCall): T
    fun visitTypeNode(node: TypeNode): T
}