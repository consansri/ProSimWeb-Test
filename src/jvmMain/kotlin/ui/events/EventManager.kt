package me.c3.ui.events

import emulator.kit.nativeLog
import me.c3.ui.components.editor.EditorDocument

class EventManager {

    private val editEvents = mutableListOf<(EditorDocument) -> Unit>()

    fun addEditEvent(event: (EditorDocument) -> Unit) {
        editEvents.add(event)
    }

    fun removeEditEvent(event: (EditorDocument) -> Unit) {
        editEvents.remove(event)
    }

    fun triggerEdit(document: EditorDocument) {
        editEvents.forEach {
            it(document)
        }
        nativeLog("EventManager: Trigger Edit!")
    }

}