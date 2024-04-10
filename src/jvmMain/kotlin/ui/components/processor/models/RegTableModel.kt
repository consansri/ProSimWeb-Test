package me.c3.ui.components.processor.models

import emulator.kit.common.RegContainer
import javax.swing.table.AbstractTableModel

class RegTableModel(private val data: RegContainer.RegisterFile): AbstractTableModel() {

    init {

    }

    override fun getRowCount(): Int {
        TODO("Not yet implemented")
    }

    override fun getColumnCount(): Int {
        TODO("Not yet implemented")
    }

    override fun getValueAt(rowIndex: Int, columnIndex: Int): Any {
        TODO("Not yet implemented")
    }
}