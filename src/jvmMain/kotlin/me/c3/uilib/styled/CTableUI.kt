package me.c3.uilib.styled

import emulator.kit.memory.MainMemory
import me.c3.uilib.UIStates
import me.c3.uilib.styled.borders.DirectionalBorder
import me.c3.uilib.styled.params.FontType
import java.awt.Color
import java.awt.Component
import java.awt.event.MouseEvent
import java.lang.ref.WeakReference
import javax.swing.*
import javax.swing.plaf.basic.BasicTableUI
import javax.swing.table.DefaultTableCellRenderer
import javax.swing.table.TableCellEditor

class CTableUI(private val primary: Boolean) : BasicTableUI() {

    val cellHighlighting = mutableListOf<CellHL>()

    override fun installUI(c: JComponent?) {
        super.installUI(c)

        val table = c as? CTable ?: return

        UIStates.theme.addEvent(WeakReference(table)) { _ ->
            setDefaults(table)
        }

        UIStates.scale.addEvent(WeakReference(table)) { _ ->
            setDefaults(table)
        }

        setDefaults(table)
    }

    inner class CCellRenderer(val primary: Boolean) : DefaultTableCellRenderer() {
        override fun getTableCellRendererComponent(table: JTable?, value: Any?, isSelected: Boolean, hasFocus: Boolean, row: Int, column: Int): Component {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column)

            val fg = UIStates.theme.get().textLaF.base
            val bg = if (primary) UIStates.theme.get().globalLaF.bgPrimary else UIStates.theme.get().globalLaF.bgSecondary

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
                is MainMemory.MemInstance -> UIStates.theme.get().dataLaF.getMemInstanceColor(value.mark)
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
                CTextButton("[$value]", FontType.CODE)
            } else {
                CLabel(value.toString(), FontType.CODE).apply {
                    val inset = UIStates.scale.get().borderScale.insets
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
        table.background = UIStates.theme.get().globalLaF.bgSecondary
        table.autoResizeMode = JTable.AUTO_RESIZE_ALL_COLUMNS
        table.setDefaultRenderer(Any::class.java, CCellRenderer(primary))
        table.setDefaultEditor(Any::class.java, CCellEditor())
        table.tableHeader.border = DirectionalBorder(south = true)
        table.isOpaque = true
        table.setShowGrid(false)
        table.showVerticalLines = false
        table.showHorizontalLines = false
        table.gridColor = table.background
        table.rowHeight = table.getFontMetrics(UIStates.theme.get().codeLaF.getFont().deriveFont(UIStates.scale.get().fontScale.dataSize)).height + 2 * UIStates.scale.get().borderScale.insets

        val header = table.tableHeader
        header.resizingAllowed = false
        header.background = UIStates.theme.get().globalLaF.bgPrimary
        header.foreground = UIStates.theme.get().textLaF.baseSecondary
        header.font = FontType.DATA.getFont()
        header.defaultRenderer = CHeaderRenderer(!primary)
        //header.resizingAllowed = false
        header.isOpaque = true
        header.reorderingAllowed = false
        header.updateTableInRealTime = true
    }

    private fun forwardMouseEvent(e: MouseEvent?) {
        e?.let { event ->
            val point = event.point
            val column = table.tableHeader.columnAtPoint(point)
            val headerRenderer = table.tableHeader.defaultRenderer
            val component = headerRenderer.getTableCellRendererComponent(table, table.columnModel.getColumn(column).headerValue, false, false, -1, column)
            val translatedEvent = SwingUtilities.convertMouseEvent(table.tableHeader, event, component)

            component.dispatchEvent(translatedEvent)
        }
    }

    data class CellHL(val color: Color, val rowID: Int?, val colID: Int?)

}