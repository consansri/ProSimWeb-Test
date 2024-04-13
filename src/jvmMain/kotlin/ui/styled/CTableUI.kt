package me.c3.ui.styled

import emulator.kit.common.Memory
import me.c3.ui.UIManager
import me.c3.ui.components.styled.CIconButton
import me.c3.ui.components.styled.CLabel
import me.c3.ui.components.styled.CTextButton
import me.c3.ui.spacing.ScaleManager
import me.c3.ui.styled.borders.DirectionalBorder
import me.c3.ui.theme.ThemeManager
import me.c3.ui.theme.core.ui.UIAdapter
import java.awt.Color
import java.awt.Component
import java.awt.Dimension
import javax.swing.BorderFactory
import javax.swing.DefaultCellEditor
import javax.swing.JComponent
import javax.swing.JTable
import javax.swing.JTextField
import javax.swing.SwingConstants
import javax.swing.SwingUtilities
import javax.swing.plaf.basic.BasicTableUI
import javax.swing.table.DefaultTableCellRenderer
import javax.swing.table.TableCellEditor
import javax.swing.table.TableCellRenderer

class CTableUI(private val themeManager: ThemeManager, private val scaleManager: ScaleManager, private val primary: Boolean) : BasicTableUI() {

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

            border = BorderFactory.createEmptyBorder()
            foreground = if (value is Memory.MemInstance) themeManager.curr.dataLaF.getMemInstanceColor(value.mark) else fg
            background = bg
            font = themeManager.curr.codeLaF.getFont().deriveFont(scaleManager.curr.fontScale.dataSize)
            border = DirectionalBorder(themeManager, scaleManager)
            border = scaleManager.curr.borderScale.getInsetBorder()

            return value as? Component ?: this
        }
    }

    inner class CHeaderRenderer(val primary: Boolean) : DefaultTableCellRenderer() {
        override fun getTableCellRendererComponent(table: JTable?, value: Any?, isSelected: Boolean, hasFocus: Boolean, row: Int, column: Int): Component {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column)

            val fg = themeManager.curr.textLaF.base
            val bg = if (primary) themeManager.curr.globalLaF.bgPrimary else themeManager.curr.globalLaF.bgSecondary

            horizontalAlignment = SwingConstants.CENTER
            border = BorderFactory.createEmptyBorder()
            foreground = if (value is Memory.MemInstance) themeManager.curr.dataLaF.getMemInstanceColor(value.mark) else fg
            background = bg
            font = themeManager.curr.codeLaF.getFont().deriveFont(scaleManager.curr.fontScale.dataSize)
            border = DirectionalBorder(themeManager, scaleManager)
            border = scaleManager.curr.borderScale.getInsetBorder()

            return value as? Component ?: this
        }

    }

    inner class CCellEditor : DefaultCellEditor(CTextField(themeManager, scaleManager, CTextFieldUI.Type.DATA)), TableCellEditor {
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
        header.font = themeManager.curr.textLaF.getTitleFont().deriveFont(scaleManager.curr.fontScale.dataSize)
        header.defaultRenderer = CHeaderRenderer(!primary)
        //header.resizingAllowed = false
        header.reorderingAllowed = false
        header.updateTableInRealTime = true
    }

}