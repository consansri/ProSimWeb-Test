package me.c3.ui.styled

import me.c3.ui.scale.ScaleManager
import me.c3.ui.theme.ThemeManager
import javax.swing.JComponent
import javax.swing.plaf.basic.BasicOptionPaneUI

class COptionPaneUI(private val tm: ThemeManager, private val sm: ScaleManager) : BasicOptionPaneUI() {

    override fun installUI(c: JComponent?) {
        super.installUI(c)



    }


}