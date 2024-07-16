package cengine.lang.cown.psi

import cengine.editor.widgets.Widget
import cengine.editor.widgets.WidgetProvider
import cengine.psi.core.PsiFile
import cengine.psi.core.TextPosition

class CownWidgets: WidgetProvider {
    override var cachedWidgets: List<Widget> = listOf(
        Widget("interline", "interline widget", Widget.Type.INTERLINE, TextPosition(0, 1,0)),
        Widget("inlay", "inlay widget", Widget.Type.INLAY, TextPosition(3, 1,3))
    )
        set(value) {
            field = value
            cachedInterLineWidgets = value.filter { it.type == Widget.Type.INTERLINE }
            cachedInlayWidgets = value.filter { it.type == Widget.Type.INLAY }
            cachedPostLineWidget = value.filter { it.type == Widget.Type.POSTLINE }
        }
    override var cachedPostLineWidget: List<Widget> = listOf()
    override var cachedInterLineWidgets: List<Widget> = listOf(Widget("interline", "interline widget", Widget.Type.INTERLINE, TextPosition(0, 1,0)))
    override var cachedInlayWidgets: List<Widget> = listOf(Widget("inlay", "inlay widget", Widget.Type.INLAY, TextPosition(3, 1,3)))
    override var cachedPreLineWidgets: List<Widget>
        get() = TODO("Not yet implemented")
        set(value) {}

    override fun getWidgets(psiFile: PsiFile): List<Widget> {
        TODO("Not yet implemented")
    }
}