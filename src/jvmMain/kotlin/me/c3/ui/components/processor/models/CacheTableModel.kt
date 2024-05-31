package me.c3.ui.components.processor.models

import javax.swing.table.DefaultTableModel

class CacheTableModel : DefaultTableModel() {

    override fun isCellEditable(row: Int, column: Int): Boolean {
        return false
    }

}