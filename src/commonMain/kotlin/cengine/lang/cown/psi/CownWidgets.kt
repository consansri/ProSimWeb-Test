package cengine.lang.cown.psi

import cengine.editor.widgets.Widget
import cengine.editor.widgets.WidgetProvider
import cengine.psi.core.PsiFile

class CownWidgets: WidgetProvider {
    override var cachedWidgets: List<Widget> = listOf(
        Widget("interline", "interline widget", Widget.Type.INTERLINE, 0),
        Widget("inlay", "inlay widget", Widget.Type.INLAY, 3)
    )
        set(value) {
            field = value
            cachedInterLineWidgets = value.filter { it.type == Widget.Type.INTERLINE }
            cachedInlayWidgets = value.filter { it.type == Widget.Type.INLAY }
            cachedPostLineWidget = value.filter { it.type == Widget.Type.POSTLINE }
        }
    override var cachedPostLineWidget: List<Widget> = listOf()
    override var cachedInterLineWidgets: List<Widget> = listOf(Widget("interline", "interline widget", Widget.Type.INTERLINE, 0))
    override var cachedInlayWidgets: List<Widget> = listOf(Widget("inlay", "inlay widget", Widget.Type.INLAY, 3))
    override var cachedPreLineWidgets: List<Widget> = listOf()

    override fun getWidgets(psiFile: PsiFile): List<Widget> {
        return listOf()
    }
}