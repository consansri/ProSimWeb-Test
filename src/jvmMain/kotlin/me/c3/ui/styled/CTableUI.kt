package me.c3.ui.styled

import emulator.kit.common.memory.MainMemory
import me.c3.ui.States
import me.c3.ui.styled.borders.DirectionalBorder
import me.c3.ui.styled.params.FontType
import java.awt.Color
import java.awt.Component
import java.awt.event.MouseEvent
import javax.swing.*
import javax.swing.plaf.basic.BasicTableUI
import javax.swing.table.DefaultTableCellRenderer
import javax.swing.table.TableCellEditor

class CTableUI(private val primary: Boolean) : BasicTableUI() {

    var highlightColor: Color? = null
    var highlightRow: Int? = null
    var highlightColumn: Int? = null

    override fun installUI(c: JComponent?) {
        super.installUI(c)

        val table = c as? CTable ?: return

        States.theme.addEvent { _ ->
            setDefaults(table)
        }

        States.scale.addEvent { _ ->
            setDefaults(table)
        }

        setDefaults(table)
    }

    inner class CCellRenderer(val primary: Boolean) : DefaultTableCellRenderer() {
        override fun getTableCellRendererComponent(table: JTable?, value: Any?, isSelected: Boolean, hasFocus: Boolean, row: Int, column: Int): Component {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column)

            val fg = States.theme.get().textLaF.base
            val bg = if (primary) States.theme.get().globalLaF.bgPrimary else States.theme.get().globalLaF.bgSecondary

            val cTable = table as? CTable
            cTable?.let { tab ->
                horizontalAlignment = when (tab.columnAlignments.getOrNull(column)) {
                    SwingConstants.CENTER -> SwingConstants.CENTER
                    SwingConstants.LEFT -> SwingConstants.LEFT
                    SwingConstants.RIGHT -> SwingConstants.RIGHT
                    else -> SwingConstants.CENTER
                }
            }

            foreground = if (highlightColor != null && (highlightRow != null || highlightColumn != null)) {
                if ((highlightRow == null || highlightRow == row) && (highlightColumn == null || highlightColumn == column)) {
                    highlightColor
                } else {
                    when (value) {
                        is MainMemory.MemInstance -> States.theme.get().dataLaF.getMemInstanceColor(value.mark)
                        else -> fg
                    }
                }
            } else {
                when (value) {
                    is MainMemory.MemInstance -> States.theme.get().dataLaF.getMemInstanceColor(value.mark)
                    else -> fg
                }
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
                CLabel(value.toString(), FontType.BASIC).apply {
                    border = BorderFactory.createEmptyBorder()
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
        table.background = States.theme.get().globalLaF.bgSecondary
        table.autoResizeMode = JTable.AUTO_RESIZE_ALL_COLUMNS
        table.setDefaultRenderer(Any::class.java, CCellRenderer(primary))
        table.setDefaultEditor(Any::class.java, CCellEditor())
        table.tableHeader.border = DirectionalBorder(south = true)
        table.isOpaque = true
        table.setShowGrid(false)
        table.showVerticalLines = false
        table.showHorizontalLines = false
        table.gridColor = table.background
        table.rowHeight = table.getFontMetrics(States.theme.get().codeLaF.getFont().deriveFont(States.scale.get().fontScale.dataSize)).height + 2 * States.scale.get().borderScale.insets

        val header = table.tableHeader
        header.resizingAllowed = false
        header.background = States.theme.get().globalLaF.bgPrimary
        header.foreground = States.theme.get().textLaF.baseSecondary
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

}