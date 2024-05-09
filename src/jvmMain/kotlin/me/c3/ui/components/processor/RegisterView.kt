package me.c3.ui.components.processor

import emulator.kit.common.RegContainer
import emulator.kit.nativeWarn
import emulator.kit.types.Variable
import me.c3.ui.MainManager
import me.c3.ui.components.processor.models.RegTableModel
import me.c3.ui.components.styled.CLabel
import me.c3.ui.components.styled.CPanel
import me.c3.ui.styled.CAdvancedTabPane
import me.c3.ui.styled.CTable
import me.c3.ui.styled.params.BorderMode
import me.c3.ui.styled.params.FontType
import java.awt.Dimension
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.awt.Insets
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.Box
import javax.swing.BoxLayout
import javax.swing.SwingConstants
import javax.swing.event.TableModelEvent
import kotlin.math.abs

class RegisterView(private val mainManager: MainManager) : CPanel(mainManager.themeManager, mainManager.scaleManager, primary = true, BorderMode.SOUTH) {

    private val regViews = mutableListOf<CAdvancedTabPane>()

    val gbc = GridBagConstraints()

    var registerPaneCount = 1
        set(value) {
            val diff = value - field
            when {
                diff < 0 -> repeat(abs(diff)) { if (componentCount > 0) removeBox() }
                diff > 0 -> repeat(diff) { addBox() }
                else -> {}
            }
            field = value
            updateDetails()
            revalidate()
            repaint()
        }

    init {
        layout = GridBagLayout()

        gbc.weightx = 1.0
        gbc.weighty = 1.0
        gbc.fill = GridBagConstraints.BOTH

        mainManager.archManager.addArchChangeListener {
            resetRegViews()
        }

        mainManager.archManager.addFeatureChangeListener {
            resetRegViews()
        }

        resetRegViews()
    }

    private fun resetRegViews() {
        val bufferedRegViews = ArrayList(regViews)
        bufferedRegViews.forEach {
            remove(it)
        }
        regViews.clear()

        gbc.gridx = 0
        gbc.insets = Insets(0, 0, 0, 0)

        repeat(registerPaneCount) {
            addBox()
        }
        updateDetails()
    }

    private fun addBox() {
        val regView = initializeRegView()
        regViews.add(regView)
        add(regView, gbc)
        gbc.gridx++
        gbc.insets = Insets(0, mainManager.currScale().borderScale.insets, 0, 0)
    }

    private fun removeBox() {
        if (regViews.isNotEmpty()) {
            remove(regViews.removeAt(regViews.size - 1))
            gbc.gridx--
            if (gbc.gridx <= 0) {
                gbc.insets = Insets(0, 0, 0, 0)
            }
        }
    }

    private fun updateDetails() {
        regViews.forEach { tabbedPane ->
            tabbedPane.tabs.forEach { tab ->
                (tab.content as? RegFileTable)?.showDetails = registerPaneCount == 1
            }
        }
    }

    private fun updateAllValues() {
        regViews.forEach { pane ->
            pane.tabs.forEach { tab ->
                (tab.content as? RegFileTable)?.updateRegValues()
            }
        }
    }

    private fun initializeRegView(): CAdvancedTabPane {
        val cTabbedPane = CAdvancedTabPane(mainManager.themeManager, mainManager.scaleManager, mainManager.icons, tabsAreCloseable = false, primary = false, borderMode = BorderMode.NONE)

        mainManager.currArch().getAllRegFiles().forEach {
            val tabLabel = CLabel(mainManager.themeManager, mainManager.scaleManager, it.name, FontType.BASIC)
            if (it.getRegisters(mainManager.currArch().getAllFeatures()).isNotEmpty()) {
                val regFileTable = RegFileTable(mainManager, it) {
                    updateAllValues()
                }
                regFileTable.updateContent()
                cTabbedPane.addTab(tabLabel, regFileTable)
            }
        }

        return cTabbedPane
    }

    class RegFileTable(private val mainManager: MainManager, val regFile: RegContainer.RegisterFile, val tableModel: RegTableModel = RegTableModel(), val onRegValueChange: (RegContainer.Register) -> Unit) :
        CTable(mainManager.themeManager, mainManager.scaleManager, tableModel, false, SwingConstants.LEFT, SwingConstants.CENTER, SwingConstants.CENTER, SwingConstants.LEFT) {

        private var currentlyUpdating = false
        private val identifierLabel = "Identifiers"
        private val ccLabel = "CC"
        private val description = "Description"

        private var sortOrder: SortOrder = SortOrder.ALIASES
            set(value) {
                field = value
                regs = when (value) {
                    SortOrder.ALIASES -> regFile.getRegisters(mainManager.currArch().getAllFeatures()).sortedBy { it.aliases.firstOrNull() }
                    SortOrder.ADDRESS -> regFile.getRegisters(mainManager.currArch().getAllFeatures()).sortedBy { it.address.toRawString() }
                }
            }

        private var regs = regFile.getRegisters(mainManager.currArch().getAllFeatures())
            set(value) {
                field = value
                updateContent()
            }

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

        init {
            this.setClickableHeaders(0, 1)
            mainManager.eventManager.addExeEventListener {
                updateRegValues()
            }

            mainManager.eventManager.addCompileListener {
                updateRegValues()
            }

            attachNumericSwitcher()
            attachValueChangeListener()
        }

        fun updateRegValues() {
            currentlyUpdating = true

            for (regIndex in regs.indices) {
                val reg = regs[regIndex]
                tableModel.setValueAt(reg.variable.get(numericType).toRawString(), regIndex, 1)
            }

            currentlyUpdating = false
        }

        fun updateContent() {
            currentlyUpdating = true
            tableModel.rowCount = 0

            createHeaders()

            val maxNameLength = regs.maxOf { reg -> if (reg.names.isNotEmpty()) reg.names.maxOf { it.length } else 0 }
            val maxAliasLength = regs.maxOf { reg -> if (reg.aliases.isNotEmpty()) reg.aliases.maxOf { it.length } else 0 }
            for (reg in regs) {
                val names = (reg.names.joinToString(" ") { it.padEnd(maxNameLength, ' ') } + " " + reg.aliases.joinToString(" ") { it.padEnd(maxAliasLength, ' ') })
                val currentValue = reg.variable.get(numericType).toRawString()

                if (showDetails) {
                    tableModel.addRow(arrayOf(names, currentValue, reg.callingConvention.displayName, reg.description))
                } else {
                    tableModel.addRow(arrayOf(names, currentValue))
                }
            }

            fitColumnWidths(0)
            updateRegValues()
            currentlyUpdating = false
        }

        private fun attachValueChangeListener() {
            tableModel.addTableModelListener { e ->
                if (e.type == TableModelEvent.UPDATE && !currentlyUpdating) {
                    val row = e.firstRow
                    val col = e.column
                    try {
                        val newStringValue = tableModel.getValueAt(row, col)
                        val reg = regs.getOrNull(row)
                        if (reg == null) {
                            nativeWarn("RegisterView: Couldn't find register on Value edit!")
                            return@addTableModelListener
                        }
                        val newValue = when (numericType) {
                            Variable.Value.Types.Bin -> Variable.Value.Bin(newStringValue.toString(), reg.get().size)
                            Variable.Value.Types.Hex -> Variable.Value.Hex(newStringValue.toString(), reg.get().size)
                            Variable.Value.Types.Dec -> Variable.Value.Dec(newStringValue.toString(), reg.get().size)
                            Variable.Value.Types.UDec -> Variable.Value.UDec(newStringValue.toString(), reg.get().size)
                        }
                        if (newValue.checkResult.valid) {
                            reg.set(newValue)
                        }
                        onRegValueChange(reg)
                    } catch (e: IndexOutOfBoundsException) {
                        nativeWarn("Received Index Out Of Bounds Exception: $e")
                    }
                }
            }
        }

        private fun attachNumericSwitcher() {
            tableHeader.addMouseListener(object : MouseAdapter() {
                override fun mouseClicked(e: MouseEvent?) {
                    e?.let { _ ->
                        val colum = columnAtPoint(e.point)
                        when (colum) {
                            0 -> {
                                sortOrder = when (sortOrder) {
                                    SortOrder.ALIASES -> SortOrder.ADDRESS
                                    SortOrder.ADDRESS -> SortOrder.ALIASES
                                }
                            }

                            1 -> {
                                numericType = when (numericType) {
                                    Variable.Value.Types.Bin -> Variable.Value.Types.Hex
                                    Variable.Value.Types.Hex -> Variable.Value.Types.Dec
                                    Variable.Value.Types.Dec -> Variable.Value.Types.UDec
                                    Variable.Value.Types.UDec -> Variable.Value.Types.Bin
                                }
                            }
                        }
                    }
                }
            })
        }

        private fun createHeaders() {
            if (!showDetails) {
                val identifiers = arrayOf(identifierLabel, numericType.visibleName)
                tableModel.setColumnIdentifiers(identifiers)
            } else {
                val identifiers = arrayOf(identifierLabel, numericType.visibleName, ccLabel, description)
                tableModel.setColumnIdentifiers(identifiers)
            }
        }

        private enum class SortOrder {
            ALIASES,
            ADDRESS
        }
    }
}