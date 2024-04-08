package me.c3.ui.styled

import emulator.kit.assembly.Compiler
import me.c3.ui.UIManager
import me.c3.ui.components.styled.CTextPane
import java.awt.Font
import javax.swing.BorderFactory
import javax.swing.JComponent
import javax.swing.plaf.basic.BasicTextPaneUI

class CTextPaneUI(private val uiManager: UIManager) : BasicTextPaneUI() {

    override fun installUI(c: JComponent?) {
        super.installUI(c)
        val pane = c as? CTextPane ?: return
        pane.font = uiManager.currTheme().codeLaF.font.deriveFont(uiManager.currScale().fontScale.codeSize)
    }

}