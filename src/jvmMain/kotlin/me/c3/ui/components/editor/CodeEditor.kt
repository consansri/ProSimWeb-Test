package me.c3.ui.components.editor

import kotlinx.coroutines.*
import me.c3.ui.States
import me.c3.ui.components.controls.BottomBar
import me.c3.uilib.styled.CAdvancedTabPane
import me.c3.uilib.styled.CLabel
import me.c3.uilib.styled.params.FontType
import java.io.File
import java.lang.ref.WeakReference
import javax.swing.*

/**
 * Represents a code editor with tabbed interface for managing multiple files.
 * @property mainManager The main manager instance.
 */
class CodeEditor(val bBar: BottomBar) : CAdvancedTabPane(true, true, emptyMessage = "Open File through the tree!") {

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
        val editPanel = ProSimEditor(editorFile, bBar)
        panels.add(editPanel)
        addTab(CLabel(file.getName(), FontType.BASIC), editPanel) { e, tab ->
            when (e) {
                Event.LOSTFOCUS -> {}
                Event.CLOSE -> {
                    panels.remove(editPanel)
                }
            }
        }
    }

    fun updateFile(file: File) {
        val alreadyOpen = searchByName(file) ?: return
        alreadyOpen.reloadFromDisk()
    }

    /**
     * Gets the controls associated with the editor.
     * @return The editor controls.
     */
    fun getControls(): EditorControls = EditorControls(this)

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
        States.ws.addEvent(WeakReference(this)) { ws ->
            SwingUtilities.invokeLater {
                if (ws == null) {
                    removeAllTabs()
                    return@invokeLater
                }

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