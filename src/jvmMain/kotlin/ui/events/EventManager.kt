package me.c3.ui.events

import emulator.kit.Architecture
import me.c3.ui.ArchManager

class EventManager(private val archManager: ArchManager) {

    private val compileEventListeners = mutableListOf<(Boolean) -> Unit>()
    private val exeEventListeners = mutableListOf<(Architecture) -> Unit>()

    fun addCompileListener(event: (Boolean) -> Unit) {
        compileEventListeners.add(event)
    }

    fun addExeEventListener(event: (Architecture) -> Unit){
        exeEventListeners.add(event)
    }

    fun removeCompileListener(event: (Boolean) -> Unit) {
        compileEventListeners.remove(event)
    }

    fun removeExeEventListener(event: (Architecture) -> Unit) {
        exeEventListeners.remove(event)
    }

    fun triggerCompileFinished(success: Boolean) {
        compileEventListeners.forEach {
            it(success)
        }
    }

    fun triggerExeEvent(){
        exeEventListeners.forEach {
            it(archManager.curr)
        }
    }


}