package prosim.ui.components.processor.models

import javax.swing.table.DefaultTableModel

class MemTableModel : DefaultTableModel() {

    override fun isCellEditable(row: Int, column: Int): Boolean {
        return column != 0 && column != this.columnCount - 1
    }

}