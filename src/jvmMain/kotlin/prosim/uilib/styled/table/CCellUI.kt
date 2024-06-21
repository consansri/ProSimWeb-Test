package prosim.uilib.styled.table

import prosim.uilib.UIStates
import prosim.uilib.resource.Icons
import prosim.uilib.scale.core.Scaling
import prosim.uilib.styled.CComponentUI
import prosim.uilib.styled.params.FontType
import prosim.uilib.theme.core.Theme
import java.awt.Color
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.RenderingHints
import javax.swing.BorderFactory
import javax.swing.JComponent

class CCellUI(private val fontType: FontType) : CComponentUI<CCell>() {
    fun updateTextColors(cell: CCell) {
        val customFG = cell.customFG
        val customBG = cell.customBG
        cell.background = customBG ?: Color(0, 0, 0, 0)
        cell.foreground = customFG ?: UIStates.theme.get().textLaF.base
    }

    override fun setDefaults(c: CCell, theme: Theme, scaling: Scaling, icons: Icons) {
        c.isOpaque = false
        c.border = BorderFactory.createEmptyBorder()
        c.font = fontType.getFont()
        c.fontMetrics = c.getFontMetrics(c.font)
        updateTextColors(c)
    }

    override fun onInstall(c: CCell) {
        // nothing needs to be installed
    }

    override fun paint(g: Graphics?, c: JComponent?) {
        val g2d = g?.create() as? Graphics2D ?: return super.paint(g, c)
        val cell = c as? CCell ?: return super.paint(g, c)

        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON)

        val string = cell.textToDraw()

        val y = cell.insets.top + cell.fontMetrics.ascent
        val x = cell.insets.left

        g2d.font = cell.font
        g2d.color = cell.background
        g2d.fillRect(0, 0, cell.bounds.width, cell.bounds.height)

        val contentBounds = cell.fontMetrics.getStringBounds(string, g2d)
        cell.preferredSize = contentBounds.bounds.size

        val offsetx = (cell.bounds.width - contentBounds.bounds.width) / 2
        val offsetY = (cell.bounds.height - contentBounds.bounds.height) / 2
        g2d.color = cell.foreground

        g2d.drawString(string, x + offsetx, y + offsetY)

        g2d.dispose()
    }


}