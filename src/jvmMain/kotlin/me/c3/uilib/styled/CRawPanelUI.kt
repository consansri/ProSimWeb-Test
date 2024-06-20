package me.c3.uilib.styled

import me.c3.uilib.UIManager
import me.c3.uilib.styled.editor.CEditorAnalyzer
import java.lang.ref.WeakReference
import javax.swing.BorderFactory
import javax.swing.JComponent
import javax.swing.border.AbstractBorder
import javax.swing.plaf.PanelUI

class CRawPanelUI( private val border: AbstractBorder? = null) : PanelUI() {

    override fun installUI(c: JComponent?) {
        super.installUI(c)

        val panel = c as? CEditorAnalyzer ?: return

        UIManager.theme.addEvent(WeakReference(panel)) { _ ->
            setDefaults(panel)
        }

        UIManager.scale.addEvent(WeakReference(panel)) { _ ->
            setDefaults(panel)
        }

        setDefaults(panel)
    }

    private fun setDefaults(panel: CEditorAnalyzer) {
        panel.isOpaque = false
        panel.background = UIManager.theme.get().globalLaF.bgPrimary

        panel.border = BorderFactory.createEmptyBorder()
        panel.insets.set(0, 0, 0, 0)
        if (border != null) {
            panel.border = border
        }

        panel.revalidate()
        panel.repaint()
    }

}