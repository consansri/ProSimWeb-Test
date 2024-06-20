package me.c3.uilib.styled.table

import me.c3.uilib.resource.Icons
import me.c3.uilib.scale.core.Scaling
import me.c3.uilib.styled.CComponentUI
import me.c3.uilib.styled.CLabel
import me.c3.uilib.styled.params.FontType
import me.c3.uilib.theme.core.Theme
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.SwingConstants
import javax.swing.SwingUtilities

class CVirtualTableUI : CComponentUI<CVirtualTable>() {

    override fun setDefaults(c: CVirtualTable, theme: Theme, scaling: Scaling, icons: Icons) {
        c.isOpaque = false
    }

    fun createHeaderRenderer(table: CVirtualTable, rowID: Int, colID: Int, text: String): CHeaderRenderer {
        return CHeaderRenderer(table, text, table.headerFontType, rowID, colID)
    }

    fun createCellRenderer(table: CVirtualTable, rowID: Int, colID: Int): CCellRenderer {
        return CCellRenderer(table,table.contentfontType, rowID, colID)
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