package me.c3.ui.components.transcript

import javax.swing.table.DefaultTableModel

class TSTableModel : DefaultTableModel() {
    override fun isCellEditable(row: Int, column: Int): Boolean {
        return false
    }

}