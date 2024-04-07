package me.c3.ui.components.console

import me.c3.emulator.kit.hlAndAppendToDoc
import me.c3.ui.UIManager
import javax.swing.text.DefaultStyledDocument
import javax.swing.text.StyledDocument

class ConsoleDocument(uiManager: UIManager) : DefaultStyledDocument() {

    init {
        uiManager.currArch().getConsole().getMessages().forEach {
            it.hlAndAppendToDoc(uiManager.currTheme().codeLaF, this)
        }
    }


}