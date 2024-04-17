package me.c3.ui.components.transcript

import emulator.kit.assembly.Compiler
import emulator.kit.common.ArchState
import emulator.kit.common.Transcript
import me.c3.ui.MainManager
import me.c3.ui.components.styled.CPanel
import me.c3.ui.components.styled.CScrollPane
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

class TranscriptView(mainManager: MainManager) : CPanel(mainManager.themeManager, mainManager.scaleManager, primary = false) {

    // SubComponents
    val compiledModel = TSTableModel()
    val compiledView = CTable(mainManager.themeManager, mainManager.scaleManager, compiledModel, primary = false, SwingConstants.CENTER, SwingConstants.RIGHT, SwingConstants.LEFT, SwingConstants.LEFT).apply {
        minimumSize = Dimension(0, 0)
    }
    val disassembledModel = TSTableModel()
    val disasmView = CTable(mainManager.themeManager, mainManager.scaleManager, disassembledModel, primary = false, SwingConstants.CENTER, SwingConstants.RIGHT, SwingConstants.LEFT, SwingConstants.LEFT).apply {
        minimumSize = Dimension(0, 0)
    }

    val label = CVerticalLabel(mainManager.themeManager, mainManager.scaleManager, "compile transcript", FontType.CODE)

    // MainComponents
    val labelPane = CPanel(mainManager.themeManager, mainManager.scaleManager, primary = false, borderMode = BorderMode.EAST).apply {
        this.layout = GridBagLayout()
        val gbc = GridBagConstraints()
        gbc.weighty = 0.0
        gbc.fill = GridBagConstraints.CENTER
        this.add(label, gbc)
    }
    val contentPane = CScrollPane(mainManager.themeManager, mainManager.scaleManager, primary = false).apply {
        minimumSize = Dimension(0, 0)
    }

    private var showCompiled: Boolean = true

    init {
        mainManager.eventManager.addCompileListener {
            SwingUtilities.invokeLater {
                updateContent(mainManager)
                updateSizing()
            }
        }

        mainManager.addWSChangedListener {
            SwingUtilities.invokeLater {
                updateContent(mainManager)
                updateSizing()
            }
        }

        mainManager.eventManager.addExeEventListener {
            highlightPCRow(mainManager)
        }

        SwingUtilities.invokeLater {
            attachSettings()
            attachMainComponents()
            setSelectedView()
            updateContent(mainManager)
            updateSizing()
        }
    }

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

    private fun attachSettings() {
        // Attach Listeners
        label.addMouseListener(object : MouseAdapter() {
            override fun mouseClicked(e: MouseEvent?) {
                showCompiled = !showCompiled
                setSelectedView()
            }
        })
    }

    private fun setSelectedView() {
        label.text = if (showCompiled) "compile transcript" else "disassemble transcript"
        contentPane.setViewportView(if (showCompiled) compiledView else disasmView)
    }

    private fun updateContent(mainManager: MainManager) {
        disassembledModel.rowCount = 0
        compiledModel.rowCount = 0
        val transcript = mainManager.currArch().getTranscript()
        if (!transcript.deactivated() && (mainManager.currArch().getState().currentState == ArchState.State.EXECUTABLE || mainManager.currArch().getState().currentState == ArchState.State.EXECUTION)) {
            val disasmIDs = mainManager.currArch().getTranscript().getHeaders(Transcript.Type.DISASSEMBLED)
            val compIDs = mainManager.currArch().getTranscript().getHeaders(Transcript.Type.COMPILED)
            val currDisasmTS = mainManager.currArch().getTranscript().getContent(Transcript.Type.DISASSEMBLED)
            val currCompTS = mainManager.currArch().getTranscript().getContent(Transcript.Type.COMPILED)

            disassembledModel.setColumnIdentifiers(disasmIDs.toTypedArray())
            for (row in currDisasmTS) {
                disassembledModel.addRow(row.getContent().toTypedArray())
            }

            compiledModel.setColumnIdentifiers(compIDs.toTypedArray())
            for (row in currCompTS) {
                compiledModel.addRow(row.getContent().toTypedArray())
            }
            compiledView.fitColumnWidths(mainManager.currScale().borderScale.insets)
            disasmView.fitColumnWidths(mainManager.currScale().borderScale.insets)
            highlightPCRow(mainManager)
            contentPane.isVisible = true
        } else {
            contentPane.isVisible = true
        }
    }

    private fun updateSizing() {
        if (contentPane.isVisible) {
            this.maximumSize = Dimension(label.width + contentPane.maximumSize.width, maximumSize.height)
        } else {
            this.maximumSize = Dimension(label.width, this.maximumSize.height)
        }
    }

    private fun highlightPCRow(mainManager: MainManager) {
        val currPC = mainManager.currArch().getRegContainer().pc
        val currTS = mainManager.currArch().getTranscript()
        val pcIndexCompiledTS = currTS.compiled.indexOfFirst { it.getAddresses().map { it.toHex().getRawHexStr() }.contains(currPC.get().toHex().getRawHexStr()) }
        val pcIndexDisasmTS = currTS.disassembled.indexOfFirst { it.getAddresses().map { it.toHex().getRawHexStr() }.contains(currPC.get().toHex().getRawHexStr()) }
        compiledView.setCellHighlighting(pcIndexCompiledTS, null, mainManager.currTheme().codeLaF.getColor(Compiler.CodeStyle.GREENPC))
        disasmView.setCellHighlighting(pcIndexDisasmTS, null, mainManager.currTheme().codeLaF.getColor(Compiler.CodeStyle.GREENPC))
    }

}