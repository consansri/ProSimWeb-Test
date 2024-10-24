package prosim.ui.components.processor

import cengine.util.integer.*
import emulator.kit.Architecture
import emulator.kit.common.RegContainer
import emulator.kit.nativeWarn
import prosim.ui.Events
import prosim.ui.States
import prosim.ui.components.processor.models.RegTableModel
import prosim.uilib.UIStates
import prosim.uilib.state.StateListener
import prosim.uilib.styled.CAdvancedTabPane
import prosim.uilib.styled.CLabel
import prosim.uilib.styled.CPanel
import prosim.uilib.styled.CTable
import prosim.uilib.styled.params.BorderMode
import prosim.uilib.styled.params.FontType
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.awt.Insets
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.SwingConstants
import javax.swing.event.TableModelEvent
import kotlin.math.abs

class RegisterView : CPanel(primary = true, BorderMode.SOUTH), StateListener<Architecture> {

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

    val archFeatureListener = Events.archFeatureChange.createAndAddListener {
        resetRegViews()
    }

    val archSettingListener = Events.archSettingChange.createAndAddListener {
        resetRegViews()
    }

    init {
        layout = GridBagLayout()

        gbc.weightx = 1.0
        gbc.weighty = 1.0
        gbc.fill = GridBagConstraints.BOTH

        States.arch.addEvent(this)

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
        gbc.insets = Insets(0, UIStates.scale.get().SIZE_INSET_MEDIUM, 0, 0)
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
        val cTabbedPane = CAdvancedTabPane(tabsAreCloseable = false, primary = false, borderMode = BorderMode.NONE)

        States.arch.get().getAllRegFiles().forEach {
            val tabLabel = CLabel(it.name, FontType.BASIC)
            if (it.getRegisters(States.arch.get().features).isNotEmpty()) {
                val regFileTable = RegFileTable(it) {
                    updateAllValues()
                }
                regFileTable.updateContent()
                cTabbedPane.addTab(tabLabel, regFileTable)
            }
        }

        return cTabbedPane
    }

    class RegFileTable(val regFile: RegContainer.RegisterFile, val tableModel: RegTableModel = RegTableModel(), val onRegValueChange: (RegContainer.Register) -> Unit) :
        CTable(tableModel, false, SwingConstants.LEFT, SwingConstants.CENTER, SwingConstants.CENTER, SwingConstants.LEFT) {

        private var currentlyUpdating = false
        private val identifierLabel = "Identifiers"
        private val ccLabel = "CC"
        private val description = "Description"

        private var sortOrder: SortOrder = SortOrder.ALIASES
            set(value) {
                field = value
                regs = when (value) {
                    SortOrder.ALIASES -> regFile.getRegisters(States.arch.get().features).sortedBy { it.aliases.firstOrNull() }
                    SortOrder.ADDRESS -> regFile.getRegisters(States.arch.get().features).sortedBy { it.address.toRawString() }
                }
            }

        private var regs = regFile.getRegisters(States.arch.get().features)
            set(value) {
                field = value
                updateContent()
            }

        var showDetails = true
            set(value) {
                field = value
                updateContent()
            }
        private var numericType: Value.Types = Value.Types.Hex
            set(value) {
                field = value
                updateContent()
            }

        val exeListener = Events.exe.createAndAddListener {
            updateRegValues()
        }

        val compileListener = Events.compile.createAndAddListener {
            updateRegValues()
        }

        init {
            this.setClickableHeaders(0, 1)

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
                            Value.Types.Bin -> Bin(newStringValue.toString(), reg.get().size)
                            Value.Types.Hex -> Hex(newStringValue.toString(), reg.get().size)
                            Value.Types.Dec -> Dec(newStringValue.toString(), reg.get().size)
                            Value.Types.UDec -> UDec(newStringValue.toString(), reg.get().size)
                        }
                        if (newValue.valid) {
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
                                    Value.Types.Bin -> Value.Types.Hex
                                    Value.Types.Hex -> Value.Types.Dec
                                    Value.Types.Dec -> Value.Types.UDec
                                    Value.Types.UDec -> Value.Types.Bin
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

    override suspend fun onStateChange(newVal: Architecture) {
        resetRegViews()
    }
}