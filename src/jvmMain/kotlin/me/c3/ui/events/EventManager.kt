package me.c3.ui.events

import emulator.kit.Architecture
import emulator.kit.assembler.Process
import me.c3.ui.ArchManager

class EventManager(private val archManager: ArchManager) {

    private val compileEventListeners = mutableListOf<(Process.Result) -> Unit>()
    private val exeEventListeners = mutableListOf<(Architecture) -> Unit>()

    fun addCompileListener(event: (Process.Result) -> Unit) {
        compileEventListeners.add(event)
    }

    fun addExeEventListener(event: (Architecture) -> Unit){
        exeEventListeners.add(event)
    }

    fun removeCompileListener(event: (Process.Result) -> Unit) {
        compileEventListeners.remove(event)
    }

    fun removeExeEventListener(event: (Architecture) -> Unit) {
        exeEventListeners.remove(event)
    }

    fun triggerCompileFinished(success: Process.Result) {
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