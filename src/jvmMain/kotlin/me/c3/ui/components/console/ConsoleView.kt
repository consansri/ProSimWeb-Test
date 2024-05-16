package me.c3.ui.components.console

import emulator.kit.toStyledContent
import me.c3.ui.MainManager
import me.c3.ui.styled.editor.CConsole

/**
 * This class represents the console view within the application.
 * It displays messages generated during execution and compilation events.
 */
class ConsoleView(mainManager: MainManager) : CConsole(mainManager.themeManager, mainManager.scaleManager){
    init {
        mainManager.eventManager.addExeEventListener {
            updateContent(mainManager.currArch().getConsole().getMessages().toStyledContent(mainManager.currTheme().codeLaF))
        }
        mainManager.eventManager.addCompileListener {
            updateContent(mainManager.currArch().getConsole().getMessages().toStyledContent(mainManager.currTheme().codeLaF))
        }
        isEditable = false
    }
}