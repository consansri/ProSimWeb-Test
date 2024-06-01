package me.c3.ui.components.console

import emulator.kit.toStyledContent
import me.c3.ui.manager.*
import me.c3.ui.styled.editor.CConsole

/**
 * This class represents the console view within the application.
 * It displays messages generated during execution and compilation events.
 */
class ConsoleView() : CConsole( ResManager.icons){
    init {
        EventManager.addExeEventListener {
            updateContent(ArchManager.curr.console.getMessages().toStyledContent(ThemeManager.curr.codeLaF))
        }
        EventManager.addCompileListener {
            updateContent(ArchManager.curr.console.getMessages().toStyledContent(ThemeManager.curr.codeLaF))
        }
        isEditable = false
    }
}