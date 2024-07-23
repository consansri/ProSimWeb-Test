package prosim.uilib.styled.table

import prosim.uilib.styled.CLabel
import prosim.uilib.styled.params.FontType
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.JComponent
import javax.swing.SwingConstants
import javax.swing.SwingUtilities
import javax.swing.plaf.ComponentUI

class CVirtualTableUI : ComponentUI() {

    override fun installUI(c: JComponent?) {
        c as? CVirtualTable ?: return super.installUI(c)

        c.isOpaque = false
    }

    fun createHeaderRenderer(table: CVirtualTable, rowID: Int, colID: Int, text: String): CHeaderRenderer {
        return CHeaderRenderer(table, text, table.headerFontType, rowID, colID)
    }

    fun createCellRenderer(table: CVirtualTable, rowID: Int, colID: Int): CCellRenderer {
        return CCellRenderer(table, table.contentfontType, rowID, colID)
    }

    class CHeaderRenderer(val table: CVirtualTable, text: String, fontType: FontType, val rowID: Int, val colID: Int) : CLabel(text, fontType) {
        init {
            horizontalAlignment = SwingConstants.CENTER
            verticalAlignment = SwingConstants.CENTER
            addMouseListener(object : MouseAdapter() {
                override fun mouseClicked(e: MouseEvent) {
                    if (SwingUtilities.isLeftMouseButton(e)) {
                        table.onHeaderClick(this@CHeaderRenderer, rowID, colID)
                    }
                }
            })
        }
    }

    class CCellRenderer(val table: CVirtualTable, fontType: FontType, val rowID: Int, val colID: Int) : CCell(fontType) {

        init {
            addMouseListener(object : MouseAdapter() {
                override fun mouseClicked(e: MouseEvent) {
                    if (SwingUtilities.isLeftMouseButton(e)) {
                        table.onCellClick(this@CCellRenderer, rowID + table.vScrollOffset, colID + table.hScrollOffset)
                    }
                }
            })
        }

        override fun textToDraw(): String {
            val realRowID = rowID + table.vScrollOffset
            val realColID = colID + table.hScrollOffset
            customFG = table.customCellFGColor(realRowID, realColID)
            customBG = table.customCellBGColor(realRowID, realColID)
            return table.getCellContent(realRowID, realColID)
        }
    }
}