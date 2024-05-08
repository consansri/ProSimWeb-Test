package me.c3.ui.components.console

import me.c3.emulator.kit.toStyledContent
import me.c3.ui.MainManager
import me.c3.ui.styled.editor.CConsole

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