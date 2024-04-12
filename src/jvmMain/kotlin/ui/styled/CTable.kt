package me.c3.ui.styled

import me.c3.ui.UIManager
import me.c3.ui.spacing.ScaleManager
import me.c3.ui.theme.ThemeManager
import me.c3.ui.theme.core.ui.UIAdapter
import javax.swing.JTable
import javax.swing.SwingConstants
import javax.swing.SwingUtilities
import javax.swing.table.AbstractTableModel
import javax.swing.table.DefaultTableCellRenderer
import javax.swing.table.JTableHeader

open class CTable(themeManager: ThemeManager, scaleManager: ScaleManager, tableModel: AbstractTableModel, private val primary: Boolean, vararg val columnAlignments: Int) : JTable(tableModel) {

    init {
        this.setUI(CTableUI(themeManager, scaleManager, primary))
    }

    fun fitColumnWidths(padding: Int) {
        for (columnIndex in 0 until model.columnCount) {
            val headerRenderer = tableHeader.defaultRenderer
            val headerComponent = headerRenderer.getTableCellRendererComponent(this, model.getColumnName(columnIndex), false, false, -1, columnIndex)
            var maxWith = headerComponent.preferredSize.width

            for (rowIndex in 0 until model.rowCount) {
                val cellRenderer = getCellRenderer(rowIndex, columnIndex)
                val cellComponent = cellRenderer.getTableCellRendererComponent(this, model.getValueAt(rowIndex, columnIndex), false, false, rowIndex, columnIndex)
                maxWith = maxOf(maxWith, cellComponent.preferredSize.width)
            }
            val column = columnModel.getColumn(columnIndex)
            column.preferredWidth = maxWith + padding
        }
    }
}