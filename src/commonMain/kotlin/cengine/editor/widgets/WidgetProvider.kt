package cengine.editor.widgets

import cengine.psi.core.PsiFile

interface WidgetProvider {
    var cachedWidgets: List<Widget>

    var cachedPostLineWidget: List<Widget>
    var cachedInterLineWidgets: List<Widget>
    var cachedInlayWidgets: List<Widget>
    var cachedPreLineWidgets: List<Widget>
    fun getWidgets(psiFile: PsiFile): List<Widget>
}