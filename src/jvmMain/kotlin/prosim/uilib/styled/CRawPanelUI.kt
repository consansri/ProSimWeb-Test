package prosim.uilib.styled

import prosim.uilib.styled.editor.CEditorAnalyzer
import javax.swing.BorderFactory
import javax.swing.JComponent
import javax.swing.border.AbstractBorder
import javax.swing.plaf.PanelUI

class CRawPanelUI(private val border: AbstractBorder? = null) : PanelUI() {

    override fun installUI(c: JComponent?) {
        super.installUI(c)

        val panel = c as? CEditorAnalyzer ?: return

        setDefaults(panel)
    }

    private fun setDefaults(panel: CEditorAnalyzer) {
        panel.isOpaque = false
        panel.border = BorderFactory.createEmptyBorder()
        panel.insets.set(0, 0, 0, 0)

        if (border != null) {
            panel.border = border
        }

        panel.revalidate()
        panel.repaint()
    }




}