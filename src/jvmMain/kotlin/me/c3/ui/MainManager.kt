package me.c3.ui

import emulator.Link
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import me.c3.ui.components.controls.BottomBar
import me.c3.ui.components.editor.CodeEditor
import me.c3.ui.events.EventManager
import me.c3.ui.scale.ScaleManager
import me.c3.ui.theme.ThemeManager
import me.c3.ui.resources.icons.BenIcons
import java.nio.file.Paths

/**
 * The MainManager class is the central manager for the ProSim application.
 * It initializes and manages key components such as theme, scale, and event managers,
 * as well as the workspace and its associated components.
 */
class MainManager {

    // Manager for the architectural model, initialized with RV32I architecture.
    val archManager = ArchManager(Link.RV32I.arch)

    // Manager for application icons.
    val icons = BenIcons()

    // Lists of listeners for generic and workspace change events.
    private val anyEventListeners = mutableListOf<() -> Unit>()
    private val wsChangedListeners = mutableListOf<(Workspace) -> Unit>()

    // Managers for theme, scale, and events.
    val tm = ThemeManager(icons)
    val sm = ScaleManager()
    val eventManager = EventManager(archManager)

    // Bottom bar and code editor components.
    val bBar = BottomBar(this)
    val editor = CodeEditor(this)

    // Current workspace, initialized with the default path.
    private var ws = Workspace(Paths.get("").toAbsolutePath().toString(), editor, this)

    // Initialization block to set up listeners for theme, scale, and event changes.
    init {
        tm.addThemeChangeListener {
            triggerAnyEvent()
        }
        sm.addScaleChangeEvent {
            triggerAnyEvent()
        }
        eventManager.addExeEventListener {
            triggerAnyEvent()
        }
        eventManager.addCompileListener {
            triggerAnyEvent()
        }
        addWSChangedListener {
            triggerAnyEvent()
        }
    }

    // Retrieves the current theme.
    fun currTheme() = tm.curr

    // Retrieves the current scale.
    fun currScale() = sm.curr

    // Retrieves the current architecture.
    fun currArch() = archManager.curr

    // Retrieves the current workspace.
    fun currWS() = ws

    /**
     * Sets the current workspace to a new path and updates the bottom bar.
     * @param path The new workspace path.
     */
    fun setCurrWS(path: String) {
        bBar.generalPurpose.text = "Switching Workspace ($path)"
        CoroutineScope(Dispatchers.Default).launch {
            ws = Workspace(path, editor, this@MainManager)
            bBar.generalPurpose.text = ""
            triggerWSChanged()
        }
    }

    /**
     * Adds a generic event listener.
     * @param event The event listener to be added.
     */
    fun addAnyEventListener(event: () -> Unit) {
        anyEventListeners.add(event)
    }

    /**
     * Adds a workspace change event listener.
     * @param event The event listener to be added.
     */
    fun addWSChangedListener(event: (Workspace) -> Unit) {
        wsChangedListeners.add(event)
    }

    // Triggers all generic event listeners.
    private fun triggerAnyEvent() {
        val listenersCopy = ArrayList(anyEventListeners)
        listenersCopy.forEach {
            it()
        }
    }

    // Triggers all workspace change event listeners.
    private fun triggerWSChanged() {
        val listenersCopy = ArrayList(wsChangedListeners)
        listenersCopy.forEach {
            it(currWS())
        }
    }
}
