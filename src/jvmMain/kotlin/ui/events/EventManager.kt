package me.c3.ui.events

import emulator.kit.Architecture
import emulator.kit.nativeLog
import io.nacular.doodle.controls.form.file
import me.c3.ui.ArchManager
import me.c3.ui.components.editor.EditorDocument

class EventManager(private val archManager: ArchManager) {

    private val editEvents = mutableListOf<(EditorDocument) -> Unit>()
    private val compileEventListeners = mutableListOf<(Architecture) -> Unit>()
    private val fileChangeListeners = mutableListOf<(Architecture) -> Unit>()

    fun addEditListener(event: (EditorDocument) -> Unit) {
        editEvents.add(event)
    }

    fun addCompileListener(event: (Architecture) -> Unit) {
        compileEventListeners.add(event)
    }

    fun addFileChangeListener(event: (Architecture) -> Unit){
        fileChangeListeners.add(event)
    }

    fun removeEditListener(event: (EditorDocument) -> Unit) {
        editEvents.remove(event)
    }

    fun removeCompileListener(event: (Architecture) -> Unit){
        compileEventListeners.remove(event)
    }

    fun removeFileChangeListener(event: (Architecture) -> Unit){
        fileChangeListeners.remove(event)
    }

    fun triggerEdit(document: EditorDocument) {
        archManager.curr.getState().edit()
        editEvents.forEach {
            it(document)
        }
        nativeLog("EventManager: File Edit!")
    }

    fun compileFinished(){
        compileEventListeners.forEach {
            it(archManager.curr)
        }
    }

    fun triggerFileChange(){
        fileChangeListeners.forEach {
            it(archManager.curr)
        }
    }


}