package me.c3

import com.formdev.flatlaf.FlatIntelliJLaf
import com.formdev.flatlaf.FlatLightLaf
import com.formdev.flatlaf.extras.FlatSVGIcon
import com.formdev.flatlaf.util.SystemInfo
import me.c3.ui.components.editor.CodeEditor
import me.c3.ui.components.frame.BaseFrame
import me.c3.ui.UIManager
import me.c3.ui.theme.icons.BenIcons
import me.c3.ui.theme.themes.DarkTheme
import java.awt.BorderLayout
import java.awt.Dimension
import javax.swing.*
import javax.swing.text.DefaultStyledDocument
import javax.swing.text.SimpleAttributeSet
import javax.swing.text.StyleConstants

fun main() {
    println("#######################################\nWhaaaat ProSimWeb has a jvm ui?\nCrazy! :D\n#######################################")

    if (SystemInfo.isLinux) {
        JFrame.setDefaultLookAndFeelDecorated(true)
        JDialog.setDefaultLookAndFeelDecorated(true)
    }

    testBaseApp()
}

fun testBaseApp() {
    BaseFrame("BaseApp")
}