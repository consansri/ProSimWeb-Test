package prosim.ui.components.transcript

import emulator.core.*
import emulator.core.Size.*
import emulator.core.Value.*
import emulator.kit.assembler.CodeStyle
import emulator.kit.assembler.Process
import emulator.kit.assembler.gas.GASParser
import prosim.ui.Events
import prosim.ui.States
import prosim.uilib.UIStates
import prosim.uilib.state.*
import prosim.uilib.styled.CPanel
import prosim.uilib.styled.CScrollPane
import prosim.uilib.styled.CTable
import prosim.uilib.styled.CVerticalLabel
import prosim.uilib.styled.params.BorderMode
import prosim.uilib.styled.params.FontType
import java.awt.Dimension
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.lang.ref.WeakReference
import javax.swing.SwingConstants
import javax.swing.SwingUtilities

/**
 * Represents a panel containing a transcript view for displaying assembly code and its execution status.
 * @property mainManager The main manager responsible for coordinating UI components and actions.
 */
class TranscriptView() : CPanel(primary = false) {

    // SubComponents
    val compIDs = listOf("Address", "Labels", "Bytes", "Disassembled")
    private val model = TSTableModel()
    val modelView = CTable(model, primary = false, SwingConstants.CENTER, SwingConstants.RIGHT, SwingConstants.LEFT, SwingConstants.LEFT).apply {
        minimumSize = Dimension(0, 0)
    }
    private val sections: MutableMap<GASParser.Section, Array<GASParser.Section.BundledContent>> = mutableMapOf()
    private var section: GASParser.Section? = null
        set(value) {
            field = value
            label.text = section?.name ?: "[no section selected]"
            content = section?.getTSContent() ?: arrayOf()
            updateContent()
        }
    var content: Array<GASParser.Section.BundledContent> = arrayOf()

    private val label = CVerticalLabel("[no section selected]", FontType.CODE)

    // MainComponents
    val labelPane = CPanel(primary = false, borderMode = BorderMode.EAST).apply {
        this.layout = GridBagLayout()
        val gbc = GridBagConstraints()
        gbc.weighty = 0.0
        gbc.fill = GridBagConstraints.CENTER
        this.add(label, gbc)
    }

    val contentPane = CScrollPane(primary = false).apply {
        setViewportView(modelView)
        minimumSize = Dimension(0, 0)
    }

    private var showCompiled: Boolean = true

    init {
        Events.compile.addListener(WeakReference(this)) {
            SwingUtilities.invokeLater {
                updateResult(it)
            }
        }

        States.ws.addEvent(WeakReference(this)) {
            SwingUtilities.invokeLater {
                updateResult()
            }
        }

        Events.exe.addListener(WeakReference(this)) {
            highlightPCRow()
        }

        SwingUtilities.invokeLater {
            attachSettings()
            attachMainComponents()
            switchSection()
            updateContent()
            updateSizing()
            attachContentMouseListeners()
        }
    }

    /**
     * Updates the transcript view with the compilation result.
     * @param result The result of the compilation process.
     */
    private fun updateResult(result: Process.Result? = null) {
        sections.clear()
        result?.sections?.forEach {
            sections[it] = it.getTSContent()
        }
        section = sections.map { it.key }.firstOrNull()
    }

    /**
     * Executes the code until the specified address.
     * @param address The address to execute until.
     */
    private fun executeUntilAddress(address: Value.Hex) {
        States.arch.get().exeUntilAddress(address)
        Events.exe.triggerEvent(States.arch.get())
    }

    /**
     * Attaches main components to the transcript view.
     */
    private fun attachMainComponents() {
        layout = GridBagLayout()

        val gbc = GridBagConstraints()
        gbc.gridx = 0
        gbc.gridy = 0
        gbc.weightx = 0.0
        gbc.weighty = 1.0
        gbc.fill = GridBagConstraints.VERTICAL
        add(labelPane, gbc)

        gbc.gridx = 1
        gbc.weightx = 1.0
        gbc.fill = GridBagConstraints.BOTH
        add(contentPane, gbc)
    }

    /**
     * Attaches mouse listeners to the transcript content.
     */
    private fun attachContentMouseListeners() {
        modelView.addMouseListener(object : MouseAdapter() {
            override fun mouseClicked(e: MouseEvent?) {
                val row = modelView.rowAtPoint(e?.point ?: return)
                if (row >= 0) {
                    val addr = content.getOrNull(row)?.address ?: return
                    executeUntilAddress(addr)
                }
            }
        })
    }

    /**
     * Attaches settings and event listeners to the transcript view label.
     */
    private fun attachSettings() {
        // Attach Listeners
        label.addMouseListener(object : MouseAdapter() {
            override fun mouseClicked(e: MouseEvent?) {
                showCompiled = !showCompiled
                switchSection()
            }
        })
    }

    /**
     * Switches to the next section in the transcript view.
     */
    private fun switchSection() {
        val index = sections.map { it.key }.indexOf(section)
        section = if (index >= 0 && index < sections.size - 1) {
            sections.map { it.key }.getOrNull(index + 1)
        } else {
            sections.map { it.key }.getOrNull(0)
        }
    }

    /**
     * Updates the content of the transcript view.
     * @param mainManager The main manager responsible for coordinating UI components and actions.
     */
    private fun updateContent() {
        model.rowCount = 0

        if (section == null) return

        model.setColumnIdentifiers(compIDs.toTypedArray())
        for (row in content) {
            model.addRow(row.getAddrLblBytesTranscript())
        }
        modelView.fitColumnWidths(UIStates.scale.get().borderScale.insets)
        highlightPCRow()
        modelView.revalidate()
        modelView.repaint()
    }

    /**
     * Updates the sizing of the transcript view.
     */
    private fun updateSizing() {
        if (contentPane.isVisible) {
            this.maximumSize = Dimension(label.width + contentPane.maximumSize.width, maximumSize.height)
        } else {
            this.maximumSize = Dimension(label.width, this.maximumSize.height)
        }
    }

    /**
     * Highlights the row in the transcript view corresponding to the current program counter value.
     * @param mainManager The main manager responsible for coordinating UI components and actions.
     */
    private fun highlightPCRow() {
        modelView.resetCellHighlighting()
        val currPC = States.arch.get().regContainer.pc

        val index = content.indexOfFirst { it.address.toRawString() == currPC.get().toHex().toRawString() }
        modelView.addCellHighlighting(UIStates.theme.get().codeLaF.getColor(CodeStyle.GREENPC), index, null)
    }

    override fun getMinimumSize(): Dimension {
        return Dimension(label.width, 100)
    }
}