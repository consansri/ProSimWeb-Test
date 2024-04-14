package me.c3.ui.components.processor

import me.c3.ui.UIManager
import me.c3.ui.components.styled.CPanel
import me.c3.ui.components.styled.CSplitPane
import java.awt.Dimension
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import javax.swing.JSplitPane

class ProcessorView(uiManager: UIManager) : CPanel(uiManager.themeManager, uiManager.scaleManager, primary = false) {

    val exeControl = ExecutionControls(uiManager)
    val regView = RegisterView(uiManager)
    val memoryView = MemoryView(uiManager)
    val splitPane = CSplitPane(uiManager.themeManager, uiManager.scaleManager, JSplitPane.VERTICAL_SPLIT, true, regView, memoryView)
    val processorSettings = ProcessorSettings(uiManager, this)

    init {
        attachContent()
    }

    private fun attachContent(){
        layout = GridBagLayout()

        splitPane.resizeWeight = 0.5

        val gbc = GridBagConstraints()

        gbc.gridx = 0
        gbc.gridy = 0
        gbc.weightx = 1.0
        gbc.weighty = 0.0
        gbc.fill = GridBagConstraints.HORIZONTAL
        add(exeControl, gbc)

        gbc.gridx = 0
        gbc.gridy = 1
        gbc.weightx = 1.0
        gbc.weighty = 1.0
        gbc.fill = GridBagConstraints.BOTH
        add(splitPane, gbc)

        gbc.gridx = 0
        gbc.gridy = 2
        gbc.weighty = 0.0
        gbc.weightx = 1.0
        gbc.fill = GridBagConstraints.HORIZONTAL
        add(processorSettings, gbc)
    }

}