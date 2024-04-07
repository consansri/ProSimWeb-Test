package me.c3.ui.components.editor

import me.c3.ui.UIManager
import me.c3.ui.components.styled.CTabbedPane

class CodeEditor(private val uiManager: UIManager) : CTabbedPane(uiManager, true) {

    var workspace: String = "/"

    init {
        uiManager.themeManager.addThemeChangeListener {
            background = it.globalLaF.bgPrimary
        }

        uiManager.fileManager.addOpenFileChangeListener { fm ->
            this.removeAll()
            fm.getCurrentFile()?.let {
                this.addTextFileTab(uiManager, it)
            }
        }

        // Defaults
        background = uiManager.currTheme().globalLaF.bgPrimary
    }

    /*fun compileCurrent(uiManager: UIManager, build: Boolean) {
        panels.getOrNull(selectedIndex)?.editPanel?.triggerCompile(uiManager, build, true)
    }*/

}