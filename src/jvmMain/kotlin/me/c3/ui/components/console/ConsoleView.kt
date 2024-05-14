package me.c3.ui.components.console

import emulator.kit.toStyledContent
import me.c3.ui.MainManager

class ConsoleView(mainManager: MainManager) : me.c3.ui.styled.editor.CConsole(mainManager.themeManager, mainManager.scaleManager){
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