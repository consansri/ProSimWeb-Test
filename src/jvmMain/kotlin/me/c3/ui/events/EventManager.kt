package me.c3.ui.events

import emulator.kit.Architecture
import emulator.kit.assembler.Process
import me.c3.ui.ArchManager

/**
 * Manages events related to compilation and execution, allowing for event registration and triggering.
 * @param archManager The manager for handling architecture-related operations.
 */
class EventManager(private val archManager: ArchManager) {

    // List of listeners for compilation events.
    private val compileEventListeners = mutableListOf<(Process.Result) -> Unit>()

    // List of listeners for execution events.
    private val exeEventListeners = mutableListOf<(Architecture) -> Unit>()

    /**
     * Adds a listener for compilation events.
     * @param event The event listener to be added.
     */
    fun addCompileListener(event: (Process.Result) -> Unit) {
        compileEventListeners.add(event)
    }

    /**
     * Adds a listener for execution events.
     * @param event The event listener to be added.
     */
    fun addExeEventListener(event: (Architecture) -> Unit){
        exeEventListeners.add(event)
    }

    /**
     * Removes a listener for compilation events.
     * @param event The event listener to be removed.
     */
    fun removeCompileListener(event: (Process.Result) -> Unit) {
        compileEventListeners.remove(event)
    }

    /**
     * Removes a listener for execution events.
     * @param event The event listener to be removed.
     */
    fun removeExeEventListener(event: (Architecture) -> Unit) {
        exeEventListeners.remove(event)
    }

    /**
     * Triggers all registered listeners for compilation events.
     * @param success The result of the compilation process.
     */
    fun triggerCompileFinished(success: Process.Result) {
        compileEventListeners.forEach {
            it(success)
        }
    }

    /**
     * Triggers all registered listeners for execution events.
     */
    fun triggerExeEvent(){
        exeEventListeners.forEach {
            it(archManager.curr)
        }
    }
}