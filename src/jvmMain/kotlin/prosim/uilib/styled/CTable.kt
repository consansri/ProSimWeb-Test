package prosim.uilib.styled

import prosim.uilib.UIStates
import prosim.uilib.styled.params.FontType
import java.awt.Color
import java.awt.Font
import javax.swing.JTable
import javax.swing.table.AbstractTableModel
import javax.swing.table.JTableHeader

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

    override fun getFont(): Font {
        return FontType.DATA.getFont()
    }

    override fun getGridColor(): Color {
        return background
    }

    override fun getBackground(): Color {
        return UIStates.theme.get().COLOR_BG_1
    }

    override fun getRowHeight(): Int {
        return getFontMetrics(font).height + 2 * UIStates.scale.get().SIZE_INSET_MEDIUM
    }

    override fun getTableHeader(): JTableHeader {
        return super.getTableHeader()
    }

    class CTableHeader(): JTableHeader(){

        override fun getForeground(): Color {
            return UIStates.theme.get().COLOR_FG_1
        }

        override fun getBackground(): Color {
            return UIStates.theme.get().COLOR_BG_0
        }

    }

}