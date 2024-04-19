package ui.components.docs

import me.c3.ui.MainManager
import me.c3.ui.components.styled.CPanel
import me.c3.ui.components.styled.CTabbedPane
import me.c3.ui.styled.params.FontType
import ui.main
import java.awt.BorderLayout

class InfoView(mainManager: MainManager) : CPanel(mainManager.themeManager, mainManager.scaleManager, primary = false) {

    val docTabs = CTabbedPane(mainManager.themeManager, mainManager.scaleManager, primary = false, FontType.BASIC)

    init {
        attachComponents()
        attachListeners(mainManager)
    }

    private fun updateDocs(mainManager: MainManager) {
        val docs = mainManager.currArch().getDescription().docs


    }

    private fun attachListeners(mainManager: MainManager) {
        mainManager.archManager.addArchChangeListener {
            updateDocs(mainManager)
        }

        updateDocs(mainManager)
    }

    private fun attachComponents() {
        layout = BorderLayout()
        add(docTabs, BorderLayout.CENTER)
    }


}