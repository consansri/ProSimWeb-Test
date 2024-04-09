package me.c3.ui.components.editor

import me.c3.ui.UIManager
import me.c3.ui.components.styled.CTabbedPane
import me.c3.ui.components.styled.CClosableTab

class CodeEditor(private val uiManager: UIManager) : CTabbedPane(uiManager, true) {

    val fileManager = FileManager()

    init {
        filesChangedReaction()
        currFileEditReaction()
    }

    private fun filesChangedReaction() {
        fileManager.addOpenFileChangeListener { fm ->
            this.removeAll()
            fm.openFiles.forEach {
                this.addTextFileTab(it)
            }
        }
    }

    private fun addTextFileTab(file: FileManager.CodeFile) {
        if (!file.file.exists()) {
            file.file.createNewFile()
        }

        val editPanel = EditPanel(file, uiManager)
        addTab(null, editPanel)
        val lastIndex = tabCount - 1
        setTabComponentAt(lastIndex, CClosableTab(uiManager, file.getName()){
            fileManager.closeFile(file)
            this.removeTabAt(lastIndex)
        })
    }

    private fun currFileEditReaction() {
        fileManager.addCurrFileEditEventListener { fm ->

        }
    }

    fun getControls(): EditorControls = EditorControls(uiManager, this)
}