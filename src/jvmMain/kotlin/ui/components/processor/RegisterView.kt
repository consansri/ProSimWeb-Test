package me.c3.ui.components.processor

import emulator.kit.common.RegContainer
import emulator.kit.nativeLog
import emulator.kit.types.Variable
import me.c3.ui.UIManager
import me.c3.ui.components.processor.models.RegTableModel
import me.c3.ui.components.styled.CLabel
import me.c3.ui.components.styled.CPanel
import me.c3.ui.components.styled.CTabbedPane
import me.c3.ui.components.styled.CTextButton
import me.c3.ui.styled.CAdvancedTabPane
import me.c3.ui.styled.CComboBox
import me.c3.ui.styled.CTable
import java.awt.Component
import javax.swing.Box
import javax.swing.BoxLayout
import javax.swing.JTable
import javax.swing.SwingConstants
import javax.swing.table.AbstractTableModel
import javax.swing.table.DefaultTableCellRenderer
import javax.swing.table.DefaultTableModel
import javax.swing.table.TableModel
import kotlin.math.abs

class RegisterView(private val uiManager: UIManager) : CPanel(uiManager, primary = true, BorderMode.SOUTH) {

    private val regViews = mutableListOf<CAdvancedTabPane>()

    var registerPaneCount = 1
        set(value) {
            val diff = value - field
            when {
                diff < 0 -> repeat(abs(diff)) { if (componentCount > 0) removeBox() }
                diff > 0 -> repeat(diff) { addBox() }
                else -> {}
            }
            field = value
            regViews.forEach { tabbedPane ->
                tabbedPane.tabs.forEach { tab ->
                    (tab.content as? RegFileTable)?.showDetails = value == 1
                }
            }
            revalidate()
            repaint()
        }

    init {
        layout = BoxLayout(this, BoxLayout.X_AXIS)

        uiManager.archManager.addArchChangeListener {
            resetRegViews()
        }

        resetRegViews()
    }

    private fun resetRegViews() {
        removeAll()
        repeat(registerPaneCount) {
            addBox()
        }
    }

    private fun addBox() {
        val regView = initializeRegView()
        regViews.add(regView)
        add(regView)
    }

    private fun removeBox() {
        if (regViews.isNotEmpty()) {
            remove(regViews.removeAt(regViews.size - 1))
        }
    }

    private fun initializeRegView(): CAdvancedTabPane {
        val cTabbedPane = CAdvancedTabPane(uiManager, tabsAreCloseable = false, primary = false, borderMode = BorderMode.NONE)

        uiManager.currArch().getAllRegFiles().forEach {
            val tabLabel = CLabel(uiManager, it.name)
            if (it.getRegisters(uiManager.currArch().getAllFeatures()).isNotEmpty()) {
                val regFileTable = RegFileTable(uiManager, it.name)
                regFileTable.updateContent()
                cTabbedPane.addTab(tabLabel, regFileTable)
            }
        }

        return cTabbedPane
    }

    class RegFileTable(private val uiManager: UIManager, val regFileName: String, val tableModel: RegTableModel = RegTableModel()) : CTable(uiManager, tableModel, false, SwingConstants.LEFT, SwingConstants.CENTER, SwingConstants.CENTER, SwingConstants.LEFT) {

        private var currentlyUpdating = false
        private val numericTypeSwitch = CComboBox(uiManager, Variable.Value.Types.entries.toTypedArray())
        private val identifierLabel = CLabel(uiManager, "Identifiers")
        private val ccLabel = CLabel(uiManager, "CC")
        private val description = CLabel(uiManager, "Description")
        var showDetails = true
            set(value) {
                field = value
                updateContent()
            }

        private var numericType: Variable.Value.Types = Variable.Value.Types.Hex
            set(value) {
                field = value
                updateContent()
            }

        fun updateContent() {
            currentlyUpdating = true
            tableModel.rowCount = 0

            createHeaders()

            val currRegFile = uiManager.currArch().getAllRegFiles().firstOrNull { it.name == regFileName } ?: return

            for (reg in currRegFile.getRegisters(uiManager.currArch().getAllFeatures())) {
                val names = (reg.names + reg.aliases).joinToString(",") { it }


                tableModel.addRow(arrayOf(names, ))
            }

            updateColumnWidths()
            currentlyUpdating = false
        }

        private fun createHeaders() {
            if (!showDetails) {
                val identifiers = arrayOf(identifierLabel, numericTypeSwitch)
                tableModel.setColumnIdentifiers(identifiers)
            } else {
                val identifiers = arrayOf(identifierLabel, numericTypeSwitch, ccLabel, description)
                tableModel.setColumnIdentifiers(identifiers)
            }
        }

        private fun updateColumnWidths() {

        }

        class RegHeaderCellRenderer(private val headerComponents: Array<Component>) : DefaultTableCellRenderer() {
            override fun getTableCellRendererComponent(table: JTable?, value: Any?, isSelected: Boolean, hasFocus: Boolean, row: Int, column: Int): Component {
                //super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column)
                val headerComponent = headerComponents.getOrNull(column)
                headerComponent?.let {
                    nativeLog("Found header: ${headerComponent::class.simpleName}")
                }
                return headerComponent ?: this
            }
        }

    }


}