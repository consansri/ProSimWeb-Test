package me.c3.ui.styled

import emulator.kit.common.Memory
import emulator.kit.nativeLog
import me.c3.ui.components.styled.CLabel
import me.c3.ui.components.styled.CTextButton
import me.c3.ui.spacing.ScaleManager
import me.c3.ui.styled.borders.DirectionalBorder
import me.c3.ui.styled.params.FontType
import me.c3.ui.theme.ThemeManager
import java.awt.*
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.*
import javax.swing.plaf.basic.BasicTableUI
import javax.swing.table.DefaultTableCellRenderer
import javax.swing.table.TableCellEditor

class CTableUI(private val themeManager: ThemeManager, private val scaleManager: ScaleManager, private val primary: Boolean) : BasicTableUI() {

    var highlightColor: Color? = null
    var highlightRow: Int? = null
    var highlightColumn: Int? = null

    override fun installUI(c: JComponent?) {
        super.installUI(c)

        val table = c as? CTable ?: return

        themeManager.addThemeChangeListener {
            setDefaults(table)
        }

        scaleManager.addScaleChangeEvent {
            setDefaults(table)
        }

        setDefaults(table)
    }

    inner class CCellRenderer(val primary: Boolean) : DefaultTableCellRenderer() {
        override fun getTableCellRendererComponent(table: JTable?, value: Any?, isSelected: Boolean, hasFocus: Boolean, row: Int, column: Int): Component {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column)

            val fg = themeManager.curr.textLaF.base
            val bg = if (primary) themeManager.curr.globalLaF.bgPrimary else themeManager.curr.globalLaF.bgSecondary

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
                    if (value is Memory.MemInstance) themeManager.curr.dataLaF.getMemInstanceColor(value.mark) else fg
                }
            } else {
                if (value is Memory.MemInstance) themeManager.curr.dataLaF.getMemInstanceColor(value.mark) else fg
            }

            border = BorderFactory.createEmptyBorder()
            background = bg
            font = FontType.DATA.getFont(themeManager, scaleManager)
            border = DirectionalBorder(themeManager, scaleManager)
            border = scaleManager.curr.borderScale.getInsetBorder()

            return value as? JComponent ?: this
        }
    }

    inner class CHeaderRenderer(val primary: Boolean) : DefaultTableCellRenderer() {
        override fun getTableCellRendererComponent(table: JTable?, value: Any?, isSelected: Boolean, hasFocus: Boolean, row: Int, column: Int): Component {
            val component = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column)

            val cTable = table as? CTable ?: return component

            return if (column in cTable.clickableHeaderIds) {
                CTextButton(themeManager, scaleManager, "[$value]", FontType.CODE)
            } else {
                CLabel(themeManager, scaleManager, value.toString(), FontType.BASIC).apply {
                    horizontalAlignment = SwingConstants.CENTER
                }
            }
        }
    }

    inner class CCellEditor : DefaultCellEditor(CTextField(themeManager, scaleManager, FontType.CODE)), TableCellEditor {
        init {
            (editorComponent as? CTextField)?.horizontalAlignment = JTextField.CENTER
        }

        override fun getTableCellEditorComponent(table: JTable?, value: Any?, isSelected: Boolean, row: Int, column: Int): Component {
            super.getTableCellEditorComponent(table, value, isSelected, row, column)
            return editorComponent
        }
    }

    fun setDefaults(table: CTable) {
        table.background = themeManager.curr.globalLaF.bgSecondary
        table.autoResizeMode = JTable.AUTO_RESIZE_ALL_COLUMNS
        table.setDefaultRenderer(Any::class.java, CCellRenderer(primary))
        table.setDefaultEditor(Any::class.java, CCellEditor())
        table.tableHeader.border = DirectionalBorder(themeManager, scaleManager, south = true)
        table.isOpaque = true
        table.setShowGrid(false)
        table.showVerticalLines = false
        table.showHorizontalLines = false
        table.gridColor = table.background
        table.rowHeight = table.getFontMetrics(themeManager.curr.codeLaF.getFont().deriveFont(scaleManager.curr.fontScale.dataSize)).height + 2 * scaleManager.curr.borderScale.insets

        val header = table.tableHeader
        header.resizingAllowed = false
        header.background = themeManager.curr.globalLaF.bgPrimary
        header.foreground = themeManager.curr.textLaF.baseSecondary
        header.font = FontType.DATA.getFont(themeManager, scaleManager)
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