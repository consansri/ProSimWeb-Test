package me.c3.uilib.styled.table

import me.c3.uilib.UIManager
import me.c3.uilib.styled.CLabel
import me.c3.uilib.styled.params.FontType
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.lang.ref.WeakReference
import javax.swing.JComponent
import javax.swing.SwingConstants
import javax.swing.SwingUtilities
import javax.swing.plaf.ComponentUI

class CVirtualTableUI : ComponentUI() {

    override fun installUI(c: JComponent?) {
        super.installUI(c)

        val table = c as? CVirtualTable ?: return

        UIManager.theme.addEvent(WeakReference(table)) { _ ->
            setDefaults(table)
        }

        UIManager.scale.addEvent(WeakReference(table)) { _ ->
            setDefaults(table)
        }

        setDefaults(table)
    }

    fun setDefaults(table: CVirtualTable) {
        table.isOpaque = false

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