package me.c3.ui.styled

import me.c3.ui.UIManager
import me.c3.ui.theme.core.ui.UIAdapter
import javax.swing.JComponent
import javax.swing.SwingUtilities
import javax.swing.plaf.basic.BasicTableUI
import javax.swing.table.DefaultTableCellRenderer

class CTableUI(private val uiManager: UIManager): BasicTableUI() {

    override fun installUI(c: JComponent?) {
        super.installUI(c)

        val table = c as? CTable ?: return
    }

    inner class CCellRenderer : DefaultTableCellRenderer(){

    }


}