package prosim.ui.components.console

import emulator.kit.Architecture
import emulator.kit.assembler.Process
import emulator.kit.toStyledContent
import prosim.ui.Events
import prosim.ui.States
import prosim.uilib.UIStates
import prosim.uilib.state.EventListener
import prosim.uilib.styled.editor.CConsole

/**
 * This class represents the console view within the application.
 * It displays messages generated during execution and compilation events.
 */
class ConsoleView : CConsole(){

    val exeListener = object: EventListener<Architecture>{
        override suspend fun onTrigger(newVal: Architecture) {
            updateContent(States.arch.get().console.getMessages().toStyledContent(UIStates.theme.get()))
        }
    }

    val compileListener = object : EventListener<Process.Result>{
        override suspend fun onTrigger(newVal: Process.Result) {
            updateContent(States.arch.get().console.getMessages().toStyledContent(UIStates.theme.get()))
        }
    }

    init {
        Events.exe.addListener(exeListener)
        Events.compile.addListener(compileListener)
        isEditable = false
    }
}