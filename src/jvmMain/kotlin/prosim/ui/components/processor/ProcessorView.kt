package prosim.ui.components.processor

import prosim.uilib.styled.CPanel
import prosim.uilib.styled.CSplitPane
import java.awt.Dimension
import java.awt.Graphics
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import javax.swing.JSplitPane

class ProcessorView : CPanel( primary = false) {

    val exeControl = ExecutionControls()
    val regView = RegisterView().apply {
        minimumSize = Dimension(0,0)
    }
    val memoryView = MemoryView().apply {
        minimumSize = Dimension(0,0)
    }
    val splitPane = CSplitPane( JSplitPane.VERTICAL_SPLIT, true, regView, memoryView).apply {
        resizeWeight = 0.7
        setDividerLocation(0.7)
    }
    val processorSettings = ProcessorSettings( this)

    init {
        attachContent()
    }

    override fun paint(g: Graphics?) {
        super.paint(g)
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