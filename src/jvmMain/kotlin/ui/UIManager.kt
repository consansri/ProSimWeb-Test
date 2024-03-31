package me.c3.ui

import emulator.Link
import me.c3.ui.ArchManager
import me.c3.ui.events.EventManager
import me.c3.ui.spacing.ScaleManager
import me.c3.ui.theme.ThemeManager
import me.c3.ui.theme.icons.BenIcons
import javax.swing.JFrame
import kotlin.reflect.cast


class UIManager(mainFrame: JFrame) {

    val archManager = ArchManager(Link.RV32I.arch)

    val icons = BenIcons()

    val themeManager = ThemeManager(mainFrame, icons)
    val scaleManager = ScaleManager()
    val eventManager = EventManager(archManager)

    fun currTheme() = themeManager.currentTheme
    fun currScale() = scaleManager.currentScaling
    fun currArch() = archManager.curr

}
