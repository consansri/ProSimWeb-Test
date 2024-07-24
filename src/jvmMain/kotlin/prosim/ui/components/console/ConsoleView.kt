package prosim.ui.components.console

import emulator.kit.toStyledContent
import prosim.ui.Events
import prosim.ui.States
import prosim.uilib.UIStates
import prosim.uilib.styled.editor.CConsole
import java.lang.ref.WeakReference

/**
 * This class represents the console view within the application.
 * It displays messages generated during execution and compilation events.
 */
class ConsoleView : CConsole(){
    init {
        Events.exe.addListener(WeakReference(this)) {
            updateContent(States.arch.get().console.getMessages().toStyledContent(UIStates.theme.get()))
        }
        Events.compile.addListener(WeakReference(this)) {
            updateContent(States.arch.get().console.getMessages().toStyledContent(UIStates.theme.get()))
        }
        isEditable = false
    }
}