package me.c3.ui.styled

import emulator.kit.common.Memory
import me.c3.ui.UIManager
import me.c3.ui.styled.borders.DirectionalBorder
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

class CTableUI(private val uiManager: UIManager) : BasicTableUI() {

    override fun installUI(c: JComponent?) {
        super.installUI(c)

        val table = c as? CTable ?: return

        uiManager.themeManager.addThemeChangeListener {
            setDefaults(uiManager, table)
        }

        uiManager.scaleManager.addScaleChangeEvent {
            setDefaults(uiManager, table)
        }

        setDefaults(uiManager, table)
    }

    inner class CCellRenderer(val primary: Boolean) : DefaultTableCellRenderer() {

        init {
            horizontalAlignment = SwingConstants.CENTER
        }

        override fun getTableCellRendererComponent(table: JTable?, value: Any?, isSelected: Boolean, hasFocus: Boolean, row: Int, column: Int): Component {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column)

            val fg = if (primary) uiManager.currTheme().textLaF.base else uiManager.currTheme().textLaF.baseSecondary
            val bg = if (primary) uiManager.currTheme().globalLaF.bgPrimary else uiManager.currTheme().globalLaF.bgSecondary

            border = BorderFactory.createEmptyBorder()
            foreground = if (value is Memory.MemInstance) uiManager.currTheme().dataLaF.getMemInstanceColor(value.mark) else fg
            background = bg
            font = uiManager.currTheme().codeLaF.getFont().deriveFont(uiManager.currScale().fontScale.dataSize)
            border = DirectionalBorder(uiManager)

            border = uiManager.currScale().borderScale.getInsetBorder()

            return this
        }
    }

    inner class CCellEditor : DefaultCellEditor(CTextField(uiManager, CTextFieldUI.Type.DATA)), TableCellEditor {
        init {
            (editorComponent as? CTextField)?.horizontalAlignment = JTextField.CENTER
        }

        override fun getTableCellEditorComponent(table: JTable?, value: Any?, isSelected: Boolean, row: Int, column: Int): Component {
            super.getTableCellEditorComponent(table, value, isSelected, row, column)
            return editorComponent
        }
    }

    private fun setDefaults(uiManager: UIManager, table: CTable) {
        table.background = uiManager.currTheme().globalLaF.bgSecondary
        table.autoResizeMode = JTable.AUTO_RESIZE_OFF
        table.setDefaultRenderer(Any::class.java, CCellRenderer(false))
        table.setDefaultEditor(Any::class.java, CCellEditor())
        table.tableHeader.border = DirectionalBorder(uiManager, south = true)
        table.isOpaque = true
        table.setShowGrid(false)
        table.showVerticalLines = false
        table.showHorizontalLines = false
        table.gridColor = table.background

        val header = table.tableHeader
        header.background = uiManager.currTheme().globalLaF.bgPrimary
        header.foreground = uiManager.currTheme().textLaF.baseSecondary
        header.font = uiManager.currTheme().textLaF.getTitleFont().deriveFont(uiManager.currScale().fontScale.dataSize)
        header.defaultRenderer = CCellRenderer(true)
        header.resizingAllowed = false
        header.reorderingAllowed = false
        header.updateTableInRealTime = true
    }
}