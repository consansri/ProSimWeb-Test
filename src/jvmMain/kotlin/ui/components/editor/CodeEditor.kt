package me.c3.ui.components.editor

import kotlinx.coroutines.*
import me.c3.ui.UIManager
import me.c3.ui.components.styled.*
import me.c3.ui.styled.CAdvancedTabPane
import java.io.File
import javax.swing.*

class CodeEditor(private val uiManager: UIManager) : CAdvancedTabPane(uiManager.themeManager, uiManager.scaleManager, uiManager.icons, true, true, emptyMessage = "Open File through the tree!") {

    private val panels = mutableListOf<ProSimEditor>()
    private val fileEditEvents = mutableListOf<(EditorFile) -> Unit>()

    init {
        attachWorkspaceListener()
        selectCurrentTab(null)
    }

    private fun attachWorkspaceListener() {
        uiManager.addWSChangedListener { ws ->
            SwingUtilities.invokeLater {
                val bufferedTabs = ArrayList(tabs)
                for (tab in bufferedTabs) {
                    val content = tab.content
                    if (content !is ProSimEditor) {
                        removeTab(tab)
                        continue
                    }

                    if (ws.getAllFiles().firstOrNull { it.path == content.editorFile.file.path && it.name == content.editorFile.file.name } == null) {
                        removeTab(tab)
                        panels.remove(content)
                        continue
                    }
                }
            }
        }
    }

    fun openFile(file: File) {
        if (searchByName(file.name) != null) return

        if (!file.exists()) {
            file.createNewFile()
        }

        val editorFile = EditorFile(file)

        val editPanel = ProSimEditor(uiManager, editorFile)
        panels.add(editPanel)
        addTab(CLabel(uiManager.themeManager, uiManager.scaleManager, file.getName()), editPanel) { e, tab ->
            when (e) {
                ClosableTab.Event.LOSTFOCUS -> {}
                ClosableTab.Event.CLOSE -> {
                    panels.remove(editPanel)
                }
            }
        }
    }

    fun getControls(): EditorControls = EditorControls(uiManager, this)

    fun searchByName(fileName: String): ProSimEditor? {
        return tabs.firstOrNull { fileName == (it.content as? ProSimEditor)?.editorFile?.file?.name }?.content as? ProSimEditor?
    }

    fun compileCurrent(build: Boolean) {
        CoroutineScope(Dispatchers.Default).launch {
            getCurrentEditor()?.compile(build)
        }
    }

    fun getCurrentEditor(): ProSimEditor? {
        return getCurrent()?.content as? ProSimEditor
    }

    fun addFileEditEvent(event: (EditorFile) -> Unit) {
        fileEditEvents.add(event)
    }

}