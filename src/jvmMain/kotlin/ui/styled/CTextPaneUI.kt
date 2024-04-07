package me.c3.ui.styled

import emulator.kit.assembly.Compiler
import me.c3.ui.components.styled.CTextPane
import javax.swing.BorderFactory
import javax.swing.JComponent
import javax.swing.plaf.basic.BasicTextPaneUI

class CTextPaneUI : BasicTextPaneUI() {

    override fun installUI(c: JComponent?) {
        super.installUI(c)

        val pane = c as? CTextPane ?: return
    }

}