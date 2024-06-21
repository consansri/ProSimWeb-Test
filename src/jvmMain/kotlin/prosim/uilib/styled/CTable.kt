package prosim.uilib.styled

import java.awt.Color
import javax.swing.JTable
import javax.swing.table.AbstractTableModel

open class CTable( tableModel: AbstractTableModel, private val primary: Boolean, vararg val columnAlignments: Int, private val shouldDrawComponents: Boolean = false) : JTable(tableModel) {

    val clickableHeaderIds = mutableListOf<Int>()

    init {
        this.setUI(CTableUI( primary))
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

    fun setClickableHeaders(vararg ids: Int) {
        clickableHeaderIds.clear()
        clickableHeaderIds.addAll(ids.toList())
    }

    /**
     * set color to null to remove any cell highlighting
     */
    fun addCellHighlighting(color: Color,rowID: Int? = null, colID: Int? = null) {
        val tableUI = (ui as? CTableUI) ?: return
        tableUI.cellHighlighting.add(CTableUI.CellHL(color, rowID, colID))
        repaint()
    }

    fun resetCellHighlighting() {
        val tableUI = (ui as? CTableUI) ?: return
        tableUI.cellHighlighting.clear()
    }

}