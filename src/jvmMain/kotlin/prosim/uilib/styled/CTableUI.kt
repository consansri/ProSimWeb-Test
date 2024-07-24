package prosim.uilib.styled

import emulator.kit.memory.MainMemory
import prosim.uilib.UIStates
import prosim.uilib.styled.borders.DirectionalBorder
import prosim.uilib.styled.params.FontType
import java.awt.Color
import java.awt.Component
import javax.swing.*
import javax.swing.plaf.basic.BasicTableUI
import javax.swing.table.DefaultTableCellRenderer
import javax.swing.table.TableCellEditor

class CTableUI(private val primary: Boolean) : BasicTableUI() {

    val cellHighlighting = mutableListOf<CellHL>()

    override fun installUI(c: JComponent?) {
        super.installUI(c)

        val table = c as? CTable ?: return

        setDefaults(table)
    }

    inner class CCellRenderer(val primary: Boolean) : DefaultTableCellRenderer() {
        override fun getTableCellRendererComponent(table: JTable?, value: Any?, isSelected: Boolean, hasFocus: Boolean, row: Int, column: Int): Component {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column)

            val fg = UIStates.theme.get().COLOR_FG_0
            val bg = if (primary) UIStates.theme.get().COLOR_BG_0 else UIStates.theme.get().COLOR_BG_1

            val cTable = table as? CTable
            cTable?.let { tab ->
                horizontalAlignment = when (tab.columnAlignments.getOrNull(column)) {
                    SwingConstants.CENTER -> SwingConstants.CENTER
                    SwingConstants.LEFT -> SwingConstants.LEFT
                    SwingConstants.RIGHT -> SwingConstants.RIGHT
                    else -> SwingConstants.CENTER
                }
            }

            val hl = cellHighlighting.firstOrNull { (it.rowID == null || it.rowID == row) && (it.colID == column || it.colID == null) }

            foreground = hl?.color ?: when (value) {
                is MainMemory.MemInstance -> UIStates.theme.get().getColor(value.mark)
                else -> fg
            }

            border = BorderFactory.createEmptyBorder()
            background = bg
            font = FontType.DATA.getFont()

            return value as? JComponent ?: this
        }
    }

    inner class CHeaderRenderer(val primary: Boolean) : DefaultTableCellRenderer() {
        override fun getTableCellRendererComponent(table: JTable?, value: Any?, isSelected: Boolean, hasFocus: Boolean, row: Int, column: Int): Component {
            val component = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column)

            val cTable = table as? CTable ?: return component

            return if (column in cTable.clickableHeaderIds) {
                CLabel("[$value]", FontType.CODE).apply {
                    val inset = UIStates.scale.get().SIZE_INSET_MEDIUM
                    border = BorderFactory.createEmptyBorder(inset, inset, 0, 0)
                    horizontalAlignment = SwingConstants.CENTER
                }

            } else {
                CLabel(value.toString(), FontType.CODE).apply {
                    val inset = UIStates.scale.get().SIZE_INSET_MEDIUM
                    border = BorderFactory.createEmptyBorder(inset, inset, 0, 0)
                    horizontalAlignment = SwingConstants.CENTER
                }
            }
        }
    }

    inner class CCellEditor : DefaultCellEditor(CTextField(FontType.CODE)), TableCellEditor {
        init {
            (editorComponent as? CTextField)?.horizontalAlignment = JTextField.CENTER
        }

        override fun getTableCellEditorComponent(table: JTable?, value: Any?, isSelected: Boolean, row: Int, column: Int): Component {
            super.getTableCellEditorComponent(table, value, isSelected, row, column)
            return editorComponent
        }
    }

    fun setDefaults(table: CTable) {
        table.isOpaque = true
        table.setShowGrid(false)
        table.showVerticalLines = false
        table.showHorizontalLines = false
        table.setDefaultRenderer(Any::class.java, CCellRenderer(primary))
        table.setDefaultEditor(Any::class.java, CCellEditor())
        table.autoResizeMode = JTable.AUTO_RESIZE_ALL_COLUMNS

        val header = CTable.CTableHeader()
        header.border = DirectionalBorder(south = true)
        header.isOpaque = true
        header.reorderingAllowed = false
        header.updateTableInRealTime = true
        header.resizingAllowed = false
        header.font = FontType.DATA.getFont()
        header.defaultRenderer = CHeaderRenderer(!primary)
        table.tableHeader = header
    }

    data class CellHL(val color: Color, val rowID: Int?, val colID: Int?)

}