package cengine.ast.node

import cengine.ast.ASTVisitor
import cengine.editor.annotation.Notation
import cengine.editor.widgets.Widget
import cengine.psi.core.PsiElement
import cengine.psi.core.PsiElementVisitor

sealed class Node : PsiElement {
    override val pathName: String = this::class.simpleName.toString()
    override var parent: PsiElement? = null
    override val notations: MutableList<Notation> = mutableListOf()
    override val children: MutableList<Node> = mutableListOf()
    override val additionalInfo: String = ""
    override val inlayWidgets: List<Widget>
        get() = emptyList()

    override val interlineWidgets: List<Widget>
        get() = emptyList()

    override fun accept(visitor: PsiElementVisitor) {
        visitor as? ASTVisitor<*> ?: return
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