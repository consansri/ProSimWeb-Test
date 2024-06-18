package me.c3.ui.components.console

import emulator.kit.toStyledContent
import me.c3.ui.Events
import me.c3.ui.States
import me.c3.ui.styled.editor.CConsole
import java.lang.ref.WeakReference

/**
 * This class represents the console view within the application.
 * It displays messages generated during execution and compilation events.
 */
class ConsoleView : CConsole(){
    init {
        Events.exe.addListener(WeakReference(this)) {
            updateContent(States.arch.get().console.getMessages().toStyledContent(States.theme.get().codeLaF))
        }
        Events.compile.addListener(WeakReference(this)) {
            updateContent(States.arch.get().console.getMessages().toStyledContent(States.theme.get().codeLaF))
        }
        isEditable = false
    }
}