package me.c3.ui.components.editor

import emulator.kit.nativeInfo
import kotlinx.coroutines.*
import me.c3.ui.MainManager
import me.c3.ui.components.styled.*
import me.c3.ui.styled.CAdvancedTabPane
import me.c3.ui.styled.params.FontType
import java.io.File
import javax.swing.*

class CodeEditor(private val mainManager: MainManager) : CAdvancedTabPane(mainManager.themeManager, mainManager.scaleManager, mainManager.icons, true, true, emptyMessage = "Open File through the tree!") {

    private val panels = mutableListOf<ProSimEditor>()
    private val fileEditEvents = mutableListOf<(EditorFile) -> Unit>()

    init {
        attachWorkspaceListener()
        selectCurrentTab(null)
    }

    private fun attachWorkspaceListener() {
        mainManager.addWSChangedListener { ws ->
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
        nativeInfo("File found!")

        if (!file.exists()) {
            nativeInfo("File does not exist so it will be created!")
            file.createNewFile()
        }

        val editorFile = EditorFile(file)
        nativeInfo("Created EditorFile ${editorFile.getName()} ${editorFile.getRawContent()}!")
        val editPanel = ProSimEditor(mainManager, editorFile)
        panels.add(editPanel)
        addTab(CLabel(mainManager.themeManager, mainManager.scaleManager, file.getName(), FontType.BASIC), editPanel) { e, tab ->
            when (e) {
                ClosableTab.Event.LOSTFOCUS -> {}
                ClosableTab.Event.CLOSE -> {
                    panels.remove(editPanel)
                }
            }
        }
    }

    fun getControls(): EditorControls = EditorControls(mainManager, this)

    fun searchByName(fileName: String): ProSimEditor? {
        return tabs.firstOrNull { fileName == (it.content as? ProSimEditor)?.editorFile?.file?.name }?.content as? ProSimEditor?
    }

    fun compileCurrent(build: Boolean) {
        CoroutineScope(Dispatchers.Default).launch {
            getCurrentEditor()?.fireCompilation(build)
        }
    }

    fun getCurrentEditor(): ProSimEditor? {
        return getCurrent()?.content as? ProSimEditor
    }

    fun addFileEditEvent(event: (EditorFile) -> Unit) {
        fileEditEvents.add(event)
    }

}