package me.c3.ui.components.console

import me.c3.ui.MainManager
import me.c3.ui.components.styled.CLabel
import me.c3.ui.components.styled.CPanel
import me.c3.ui.components.styled.CTextPane
import me.c3.ui.styled.params.FontType
import java.awt.BorderLayout

class Console(mainManager: MainManager) : CPanel(mainManager.themeManager, mainManager.scaleManager, primary = false) {

    val topBar = CPanel(mainManager.themeManager, mainManager.scaleManager, primary = false)
    val textPane: CTextPane = CTextPane(mainManager.themeManager, mainManager.scaleManager)
    val contentPane = textPane.createScrollPane(mainManager.themeManager, mainManager.scaleManager)

    init {
        textPane.document = ConsoleDocument(mainManager)

        connectChildren(mainManager)
        attachListeners(mainManager)
        setDefaults()
    }

    private fun connectChildren(mainManager: MainManager) {
        layout = BorderLayout()

        topBar.add(CLabel(mainManager.themeManager,mainManager.scaleManager, "Console", FontType.TITLE))

        add(topBar, BorderLayout.NORTH)
        add(contentPane, BorderLayout.CENTER)
    }

    private fun setDefaults() {
        textPane.isEditable = false
    }

    private fun attachListeners(mainManager: MainManager) {
        mainManager.archManager.addArchChangeListener {
            update(mainManager)
        }

        mainManager.addAnyEventListener {
            update(mainManager)
        }
    }

    fun update(mainManager: MainManager) {
        textPane.document = ConsoleDocument(mainManager)
    }

}