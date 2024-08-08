package cengine.lang.asm.features

import cengine.editor.widgets.Widget
import cengine.editor.widgets.WidgetProvider
import cengine.lang.asm.ast.gas.GASNode
import cengine.lang.asm.lexer.AsmTokenType
import cengine.lang.asm.psi.AsmFile
import cengine.psi.core.PsiElement
import cengine.psi.core.PsiElementVisitor
import cengine.psi.core.PsiFile
import emulator.kit.nativeLog

class AsmWidgets : WidgetProvider {
    override fun updateWidgets(psiFile: PsiFile) {
        psiFile.accept(WidgetBuilder())
    }

    private inner class WidgetBuilder : PsiElementVisitor {
        init {
            nativeLog("Building Widgets!")
        }

        override fun visitFile(file: PsiFile) {
            if (file !is AsmFile) return
            file.children.forEach {
                it.accept(this)
            }
        }

        override fun visitElement(element: PsiElement) {
            if (element !is GASNode) return
            element.interlineWidgets.clear()
            element.inlayWidgets.clear()
            when (element) {
                is GASNode.Instruction -> {
                    if (element.type.isPseudo) {
                        element.interlineWidgets.add(Widget("pseudo", "${element.type.bytesNeeded ?: "?"} bytes", Widget.Type.INTERLINE, { element.range.first }))
                    }
                }

                is GASNode.NumericExpr.Prefix -> {
                    if ((element.operand !is GASNode.NumericExpr.Operand.Number) || (element.operand.number.type != AsmTokenType.INT_DEC)) {
                        element.inlayWidgets.add(Widget("result", "= ${element.evaluate(false)}", Widget.Type.INLAY, { element.range.last }))
                    }
                    return
                }

                is GASNode.NumericExpr.Classic -> {
                    element.inlayWidgets.add(Widget("result", "= ${element.evaluate(false)}", Widget.Type.INLAY, { element.range.last }))
                    return
                }

                else -> {}
            }

            element.children.forEach {
                it.accept(this)
            }
        }

    }

}