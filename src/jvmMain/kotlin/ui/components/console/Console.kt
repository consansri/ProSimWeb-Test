package me.c3.ui.components.console

import kotlinx.coroutines.*
import me.c3.emulator.kit.toStyledContent
import me.c3.ui.MainManager
import me.c3.ui.components.styled.CLabel
import me.c3.ui.components.styled.CPanel
import me.c3.ui.components.styled.CTextPane
import me.c3.ui.styled.params.FontType
import ui.main
import ui.styled.editor.CConsole
import java.awt.BorderLayout

class Console(mainManager: MainManager) : CConsole(mainManager.themeManager, mainManager.scaleManager){
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