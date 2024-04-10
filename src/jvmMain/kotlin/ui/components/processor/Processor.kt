package me.c3.ui.components.processor

import me.c3.ui.UIManager
import me.c3.ui.components.styled.CPanel
import me.c3.ui.components.styled.CSplitPane
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import javax.swing.BoxLayout
import javax.swing.JSplitPane

class Processor(uiManager: UIManager) : CPanel(uiManager, primary = false) {

    private val exeControl = ExecutionControls(uiManager)
    private val regView = RegisterView(uiManager)
    private val memoryView = MemoryView(uiManager)
    private val splitPane = CSplitPane(uiManager, JSplitPane.VERTICAL_SPLIT, true, regView, memoryView)

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

    }

}