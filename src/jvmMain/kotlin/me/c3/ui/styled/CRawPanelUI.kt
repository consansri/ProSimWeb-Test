package me.c3.ui.styled

import me.c3.ui.States
import me.c3.ui.styled.editor.CEditorAnalyzer
import javax.swing.BorderFactory
import javax.swing.JComponent
import javax.swing.border.AbstractBorder
import javax.swing.plaf.PanelUI

class CRawPanelUI( private val border: AbstractBorder? = null) : PanelUI() {

    override fun installUI(c: JComponent?) {
        super.installUI(c)

        val panel = c as? CEditorAnalyzer ?: return

        States.theme.addEvent { _ ->
            setDefaults(panel)
        }

        States.scale.addEvent { _ ->
            setDefaults(panel)
        }

        setDefaults(panel)
    }

    private fun setDefaults(panel: CEditorAnalyzer) {
        panel.isOpaque = false
        panel.background = States.theme.get().globalLaF.bgPrimary

        panel.border = BorderFactory.createEmptyBorder()
        panel.insets.set(0, 0, 0, 0)
        if (border != null) {
            panel.border = border
        }

        panel.revalidate()
        panel.repaint()
    }

}