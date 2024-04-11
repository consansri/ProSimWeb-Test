package me.c3.ui

import emulator.Link
import me.c3.ui.components.editor.CodeEditor
import me.c3.ui.events.EventManager
import me.c3.ui.spacing.ScaleManager
import me.c3.ui.theme.ThemeManager
import me.c3.ui.theme.core.style.CodeLaF
import me.c3.ui.theme.icons.BenIcons
import java.awt.Font
import java.awt.GraphicsEnvironment
import java.io.File
import java.nio.file.Paths
import javax.swing.JFrame
import javax.swing.JTextPane
import javax.swing.text.StyleConstants


class UIManager() {

    val archManager = ArchManager(Link.RV32I.arch)

    val icons = BenIcons()

    private val anyEventListeners = mutableListOf<() -> Unit>()
    private val wsChangedListeners = mutableListOf<(Workspace) -> Unit>()

    val themeManager = ThemeManager(icons)
    val scaleManager = ScaleManager()
    val eventManager = EventManager(archManager)

    val editor = CodeEditor(this)

    private var ws = Workspace(Paths.get("").toAbsolutePath().toString(), editor, this)


    init {
        themeManager.addThemeChangeListener {
            triggerAnyEvent()
        }
        scaleManager.addScaleChangeEvent {
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

    fun currTheme() = themeManager.currentTheme
    fun currScale() = scaleManager.currentScaling
    fun currArch() = archManager.curr
    fun currWS() = ws
    fun setCurrWS(path: String) {
        ws = Workspace(path, editor, this)
        triggerWSChanged()
    }

    fun addAnyEventListener(event: () -> Unit) {
        anyEventListeners.add(event)
    }

    fun addWSChangedListener(event: (Workspace) -> Unit) {
        wsChangedListeners.add(event)
    }

    private fun triggerAnyEvent() {
        val listenersCopy = ArrayList(anyEventListeners)
        listenersCopy.forEach {
            it()
        }
    }

    private fun triggerWSChanged() {
        val listenersCopy = ArrayList(wsChangedListeners)
        listenersCopy.forEach {
            it(currWS())
        }
    }




}
