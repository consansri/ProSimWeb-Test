package me.c3.ui.styled.table

import javax.swing.JComponent
import javax.swing.plaf.ComponentUI

class CVirtualTableUI: ComponentUI() {

    override fun installUI(c: JComponent?) {
        super.installUI(c)

        val table = c as? CVirtualTable ?: return




    }

    fun setDefaults(table: CVirtualTable){
        table.isOpaque = false

    }

}