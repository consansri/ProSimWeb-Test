package me.c3.ui.theme.core.ui

import me.c3.ui.theme.core.Theme
import java.awt.Dimension
import javax.swing.AbstractButton
import javax.swing.JComponent
import javax.swing.plaf.basic.BasicButtonUI

class IconButtonUI(private val theme: Theme): BasicButtonUI() {
    override fun installUI(c: JComponent?) {
        super.installUI(c)
        val button = c as AbstractButton
        button.foreground = theme.iconStyle.iconFgPrimary
        button.border = null
        button.isFocusPainted = false
    }
}