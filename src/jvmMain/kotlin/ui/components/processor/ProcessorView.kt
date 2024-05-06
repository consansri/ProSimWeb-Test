package me.c3.ui.components.processor

import me.c3.ui.MainManager
import me.c3.ui.components.styled.CPanel
import me.c3.ui.components.styled.CSplitPane
import java.awt.Dimension
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import javax.swing.JSplitPane

class ProcessorView(mainManager: MainManager) : CPanel(mainManager.themeManager, mainManager.scaleManager, primary = false) {

    val exeControl = ExecutionControls(mainManager)
    val regView = RegisterView(mainManager).apply {
        minimumSize = Dimension(0,0)
    }
    val memoryView = MemoryView(mainManager).apply {
        minimumSize = Dimension(0,0)
    }
    val splitPane = CSplitPane(mainManager.themeManager, mainManager.scaleManager, JSplitPane.VERTICAL_SPLIT, true, regView, memoryView).apply {
        resizeWeight = 0.7
        setDividerLocation(0.7)
    }
    val processorSettings = ProcessorSettings(mainManager, this)

    init {
        attachContent()
    }

    private fun attachContent(){
        layout = GridBagLayout()

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