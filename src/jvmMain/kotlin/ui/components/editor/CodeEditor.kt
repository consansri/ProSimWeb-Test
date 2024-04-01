package me.c3.ui.components.editor

import me.c3.ui.UIManager
import javax.swing.JLabel
import javax.swing.JTabbedPane

class CodeEditor(uiManager: UIManager) : JTabbedPane() {

    val panels = mutableListOf<FileMap>()

    init {

        uiManager.eventManager.addFileChangeListener {
            initFiles(uiManager)
        }
        uiManager.themeManager.addThemeChangeListener {
            background = it.globalStyle.bgPrimary
        }

        // Defaults
        background = uiManager.currTheme().globalStyle.bgPrimary

        initFiles(uiManager)
    }

    fun initFiles(uiManager: UIManager) {
        removeAll()


        val fileName = "main.s"
        val editPanel = EditPanel(uiManager, fileName)
        val label = JLabel(fileName)
        panels.add(FileMap(editPanel, label))
        val icon = uiManager.icons.fileNotCompiled.derive(uiManager.currScale().controlScale.size, uiManager.currScale().controlScale.size)
        addTab(fileName, icon, editPanel)

    }

    fun compileCurrent(uiManager: UIManager, build: Boolean){
        panels.getOrNull(selectedIndex)?.editPanel?.triggerCompile(uiManager, build, true)
    }


    inner class FileMap(val editPanel: EditPanel, val tabLabel: JLabel)

}