package me.c3.ui.components.processor.models

import emulator.kit.common.RegContainer
import javax.swing.table.AbstractTableModel
import javax.swing.table.DefaultTableModel

class RegTableModel : DefaultTableModel() {
    override fun isCellEditable(row: Int, column: Int): Boolean {
        return column == 1
    }
}