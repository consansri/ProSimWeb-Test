package me.c3.ui.components.console

import me.c3.ui.UIManager
import me.c3.ui.components.styled.CLabel
import me.c3.ui.components.styled.CPanel
import me.c3.ui.components.styled.CTextPane
import java.awt.BorderLayout

class Console(uiManager: UIManager) : CPanel(uiManager, primary = false) {

    val topBar = CPanel(uiManager, primary = false)
    val textPane: CTextPane = CTextPane(uiManager, ConsoleDocument(uiManager))
    val contentPane = textPane.createScrollPane(uiManager)

    init {
        connectChildren(uiManager)
        attachListeners(uiManager)
        setDefaults()
    }

    private fun connectChildren(uiManager: UIManager) {
        layout = BorderLayout()

        topBar.add(CLabel(uiManager, "Console"))

        add(topBar, BorderLayout.NORTH)
        add(contentPane, BorderLayout.CENTER)
    }

    private fun setDefaults() {
        textPane.isEditable = false
    }

    private fun attachListeners(uiManager: UIManager) {
        uiManager.archManager.addArchChangeListener {
            update(uiManager)
        }

        uiManager.addAnyEventListener {
            update(uiManager)
        }
    }

    fun update(uiManager: UIManager) {
        textPane.document = ConsoleDocument(uiManager)
    }

}