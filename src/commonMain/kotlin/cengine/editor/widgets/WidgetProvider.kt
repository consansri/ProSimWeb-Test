package cengine.editor.widgets

import cengine.psi.core.PsiFile

interface WidgetProvider {
    fun updateWidgets(psiFile: PsiFile)
}