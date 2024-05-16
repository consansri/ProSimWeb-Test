package me.c3.ui.components.transcript

import emulator.kit.assembler.CodeStyle
import emulator.kit.assembler.Process
import emulator.kit.assembler.gas.GASParser
import emulator.kit.types.Variable
import me.c3.ui.MainManager
import me.c3.ui.styled.CPanel
import me.c3.ui.styled.CScrollPane
import me.c3.ui.styled.CTable
import me.c3.ui.styled.CVerticalLabel
import me.c3.ui.styled.params.BorderMode
import me.c3.ui.styled.params.FontType
import java.awt.Dimension
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.SwingConstants
import javax.swing.SwingUtilities

/**
 * Represents a panel containing a transcript view for displaying assembly code and its execution status.
 * @property mainManager The main manager responsible for coordinating UI components and actions.
 */
class TranscriptView(private val mainManager: MainManager) : CPanel(mainManager.themeManager, mainManager.scaleManager, primary = false) {

    // SubComponents
    val compIDs = listOf("Address", "Labels", "Bytes", "Disassembled")
    private val model = TSTableModel()
    val modelView = CTable(mainManager.themeManager, mainManager.scaleManager, model, primary = false, SwingConstants.CENTER, SwingConstants.RIGHT, SwingConstants.LEFT, SwingConstants.LEFT).apply {
        minimumSize = Dimension(0, 0)
    }
    private val sections: MutableMap<GASParser.Section, Array<GASParser.Section.BundledContent>> = mutableMapOf()
    private var section: GASParser.Section? = null
        set(value) {
            field = value
            label.text = section?.name ?: "[no section selected]"
            content = section?.getTSContent() ?: arrayOf()
            updateContent(mainManager)
        }
    var content: Array<GASParser.Section.BundledContent> = arrayOf()

    private val label = CVerticalLabel(mainManager.themeManager, mainManager.scaleManager, "[no section selected]", FontType.CODE)

    // MainComponents
    val labelPane = CPanel(mainManager.themeManager, mainManager.scaleManager, primary = false, borderMode = BorderMode.EAST).apply {
        this.layout = GridBagLayout()
        val gbc = GridBagConstraints()
        gbc.weighty = 0.0
        gbc.fill = GridBagConstraints.CENTER
        this.add(label, gbc)
    }

    val contentPane = CScrollPane(mainManager.themeManager, mainManager.scaleManager, primary = false).apply {
        setViewportView(modelView)
        minimumSize = Dimension(0, 0)
    }

    private var showCompiled: Boolean = true

    init {
        mainManager.eventManager.addCompileListener {
            SwingUtilities.invokeLater {
                updateResult(it)
            }
        }

        mainManager.addWSChangedListener {
            SwingUtilities.invokeLater {
                updateResult()
            }
        }

        mainManager.eventManager.addExeEventListener {
            highlightPCRow(mainManager)
        }

        SwingUtilities.invokeLater {
            attachSettings()
            attachMainComponents()
            switchSection()
            updateContent(mainManager)
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
    private fun executeUntilAddress(address: Variable.Value.Hex) {
        mainManager.currArch().exeUntilAddress(address)
        mainManager.eventManager.triggerExeEvent()
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
    private fun updateContent(mainManager: MainManager) {
        model.rowCount = 0

        if (section == null) return

        model.setColumnIdentifiers(compIDs.toTypedArray())
        for (row in content) {
            model.addRow(row.getAddrLblBytesTranscript())
        }
        modelView.fitColumnWidths(mainManager.currScale().borderScale.insets)
        highlightPCRow(mainManager)
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
    private fun highlightPCRow(mainManager: MainManager) {
        val currPC = mainManager.currArch().getRegContainer().pc

        val index = content.indexOfFirst { it.address.getRawHexStr() == currPC.get().toHex().getRawHexStr() }
        modelView.setCellHighlighting(index, null, mainManager.currTheme().codeLaF.getColor(CodeStyle.GREENPC))
    }

    override fun getMinimumSize(): Dimension {
        return Dimension(label.width, 100)
    }
}