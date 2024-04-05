package me.c3.ui.components.editor

import emulator.kit.nativeLog
import me.c3.ui.UIManager
import me.c3.ui.components.styled.CTabbedPane
import java.io.File
import javax.swing.JLabel

class CodeEditor(private val uiManager: UIManager) : CTabbedPane(uiManager, true) {

    var workspace: String = "/"

    init {
        uiManager.themeManager.addThemeChangeListener {
            background = it.globalStyle.bgPrimary
        }

        // Defaults
        background = uiManager.currTheme().globalStyle.bgPrimary
    }

    fun openFile(file: File) {
        this.addTextFileTab(uiManager, file)
        nativeLog("Try to open ${file.name}")
    }

    /*fun compileCurrent(uiManager: UIManager, build: Boolean) {
        panels.getOrNull(selectedIndex)?.editPanel?.triggerCompile(uiManager, build, true)
    }*/

}