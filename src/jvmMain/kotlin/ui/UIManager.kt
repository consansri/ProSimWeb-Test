package me.c3.ui

import emulator.Link
import me.c3.ui.events.EventManager
import me.c3.ui.spacing.ScaleManager
import me.c3.ui.theme.ThemeManager
import me.c3.ui.theme.icons.BenIcons
import javax.swing.JFrame


class UIManager(mainFrame: JFrame) {

    val archManager = ArchManager(Link.RV32I.arch)
    val fileManager = FileManager()

    val icons = BenIcons()

    val themeManager = ThemeManager(mainFrame, icons)
    val scaleManager = ScaleManager()
    val eventManager = EventManager(archManager)

    val anyEventListeners = mutableListOf<() -> Unit>()

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
        fileManager.addOpenFileChangeListener {
            triggerAnyEvent()
        }
        eventManager.addCompileListener {
            triggerAnyEvent()
        }
    }

    fun currTheme() = themeManager.currentTheme
    fun currScale() = scaleManager.currentScaling
    fun currArch() = archManager.curr

    fun addAnyEventListener(event: () -> Unit){
        anyEventListeners.add(event)
    }
    fun removeAnyEventListener(event: () -> Unit){
        anyEventListeners.remove(event)
    }
    private fun triggerAnyEvent(){
        anyEventListeners.forEach {
            it()
        }
    }

}
