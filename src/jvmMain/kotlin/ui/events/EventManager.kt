package me.c3.ui.events

import emulator.kit.Architecture
import me.c3.ui.ArchManager

class EventManager(private val archManager: ArchManager) {

    private val compileEventListeners = mutableListOf<(Architecture) -> Unit>()
    private val exeEventListeners = mutableListOf<(Architecture) -> Unit>()

    fun addCompileListener(event: (Architecture) -> Unit) {
        compileEventListeners.add(event)
    }

    fun addExeEventListener(event: (Architecture) -> Unit){
        exeEventListeners.add(event)
    }

    fun removeCompileListener(event: (Architecture) -> Unit) {
        compileEventListeners.remove(event)
    }

    fun removeExeEventListener(event: (Architecture) -> Unit) {
        exeEventListeners.remove(event)
    }

    fun triggerCompileFinished() {
        compileEventListeners.forEach {
            it(archManager.curr)
        }
    }

    fun triggerExeEvent(){
        exeEventListeners.forEach {
            it(archManager.curr)
        }
    }


}