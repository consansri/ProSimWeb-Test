package me.c3.ui.components.editor

import kotlinx.coroutines.*
import me.c3.ui.MainManager
import me.c3.ui.styled.CAdvancedTabPane
import me.c3.ui.styled.CLabel
import me.c3.ui.styled.params.FontType
import java.io.File
import javax.swing.*

/**
 * Represents a code editor with tabbed interface for managing multiple files.
 * @property mainManager The main manager instance.
 */
class CodeEditor(private val mainManager: MainManager) : CAdvancedTabPane(mainManager.tm, mainManager.sm, mainManager.icons, true, true, emptyMessage = "Open File through the tree!") {

    // List of editor panels
    private val panels = mutableListOf<ProSimEditor>()

    // List of file edit events
    private val fileEditEvents = mutableListOf<(EditorFile) -> Unit>()

    init {
        // Attach workspace listener
        attachWorkspaceListener()
        // Select current tab
        selectCurrentTab(null)
    }

    /**
     * Opens a file in the editor.
     * @param file The file to be opened.
     */
    fun openFile(file: File) {
        val alreadyOpen = searchByName(file)
        if (alreadyOpen != null) {
            select(panels.indexOf(alreadyOpen))
            return
        }

        if (!file.exists()) {
            file.createNewFile()
        }

        val editorFile = EditorFile(file)
        val editPanel = ProSimEditor(mainManager, editorFile)
        panels.add(editPanel)
        addTab(CLabel(mainManager.tm, mainManager.sm, file.getName(), FontType.BASIC), editPanel) { e, tab ->
            when (e) {
                Event.LOSTFOCUS -> {}
                Event.CLOSE -> {
                    panels.remove(editPanel)
                }
            }
        }
    }

    /**
     * Gets the controls associated with the editor.
     * @return The editor controls.
     */
    fun getControls(): EditorControls = EditorControls(mainManager, this)

    /**
     * Searches for an editor panel by file name.
     * @param fileName The name of the file.
     * @return The editor panel if found, null otherwise.
     */
    fun searchByName(file: File): ProSimEditor? {
        return tabs.firstOrNull { file == (it.content as? ProSimEditor)?.editorFile?.file }?.content as? ProSimEditor?
    }

    /**
     * Compiles the current editor file.
     * @param build Whether to build before compilation.
     */
    fun compileCurrent(build: Boolean) {
        getCurrentEditor()?.fireCompilation(build)
    }

    /**
     * Gets the current editor panel.
     * @return The current editor panel.
     */
    fun getCurrentEditor(): ProSimEditor? {
        return getCurrent()?.content as? ProSimEditor
    }

    /**
     * Adds a file edit event listener.
     * @param event The event handler.
     */
    fun addFileEditEvent(event: (EditorFile) -> Unit) {
        fileEditEvents.add(event)
    }

    /**
     * Attaches a workspace change listener to remove tabs for files that are no longer in the workspace.
     */
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
}