package me.c3.ui.styled

import me.c3.ui.manager.ScaleManager
import me.c3.ui.styled.editor.CEditorAnalyzer
import me.c3.ui.manager.ThemeManager
import javax.swing.BorderFactory
import javax.swing.JComponent
import javax.swing.border.AbstractBorder
import javax.swing.plaf.PanelUI

class CRawPanelUI( private val border: AbstractBorder? = null) : PanelUI() {

    override fun installUI(c: JComponent?) {
        super.installUI(c)

        val panel = c as? CEditorAnalyzer ?: return

        ThemeManager.addThemeChangeListener {
            setDefaults(panel)
        }

        ScaleManager.addScaleChangeEvent {
            setDefaults(panel)
        }

        setDefaults(panel)
    }

    private fun setDefaults(panel: CEditorAnalyzer) {
        panel.isOpaque = false
        panel.background = ThemeManager.curr.globalLaF.bgPrimary

        panel.border = BorderFactory.createEmptyBorder()
        panel.insets.set(0, 0, 0, 0)
        if (border != null) {
            panel.border = border
        }

        panel.revalidate()
        panel.repaint()
    }

}