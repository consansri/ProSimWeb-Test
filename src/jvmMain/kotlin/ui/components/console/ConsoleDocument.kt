package me.c3.ui.components.console

import me.c3.emulator.kit.hlAndAppendToDoc
import me.c3.ui.MainManager
import javax.swing.text.DefaultStyledDocument

class ConsoleDocument(mainManager: MainManager) : DefaultStyledDocument() {

    init {
        mainManager.currArch().getConsole().getMessages().forEach {
            it.hlAndAppendToDoc(mainManager.currTheme().codeLaF, this)
        }
    }


}