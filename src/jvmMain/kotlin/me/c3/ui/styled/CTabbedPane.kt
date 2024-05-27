package me.c3.ui.styled

import me.c3.ui.scale.ScaleManager
import me.c3.ui.styled.CTabbedPaneUI
import me.c3.ui.styled.params.FontType
import me.c3.ui.theme.ThemeManager
import java.awt.Graphics
import java.awt.Graphics2D
import javax.swing.JTabbedPane

open class CTabbedPane(tm: ThemeManager, sm: ScaleManager, val primary: Boolean, fontType: FontType) : JTabbedPane() {

    init {
        this.setUI(CTabbedPaneUI(tm, sm, primary, fontType))
    }


    override fun paint(g: Graphics) {
        val g2d = g.create() as Graphics2D

        g2d.color = background
        g2d.fillRect(0, 0, width, height)

        super.paint(g2d)
        g2d.dispose()
    }


}