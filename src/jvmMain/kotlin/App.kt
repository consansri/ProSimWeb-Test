package me.c3

import com.formdev.flatlaf.util.SystemInfo
import me.c3.emulator.kit.install
import me.c3.ui.UIManager
import me.c3.ui.components.BaseFrame
import me.c3.ui.components.editor.CDocument
import me.c3.ui.styled.CFrame
import java.awt.Dimension
import javax.swing.*

fun main() {
    println("#######################################\nWhaaaat ProSimWeb has a jvm ui?\nCrazy! :D\n#######################################")

    if (SystemInfo.isLinux) {
        JFrame.setDefaultLookAndFeelDecorated(true)
        JDialog.setDefaultLookAndFeelDecorated(true)
    }

    testBaseApp()

}

fun testBaseApp() {
    BaseFrame(UIManager())
}

fun testCustomFrame() {
    val frame = CFrame(UIManager())
    frame.setFrameTitle("ProSimWeb")
}

fun testTextPane() {
    SwingUtilities.invokeLater {
        val uiManager = UIManager()

        val frame = JFrame()
        frame.size = Dimension(1280, 768)
        frame.isVisible = true
        frame.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
        frame.setLocationRelativeTo(null)

        val textPane = JTextPane()
        textPane.isEditable = true
        frame.add(textPane)

        uiManager.currTheme().codeLaF.getFont().install(textPane, uiManager.currScale().fontScale.codeSize)

        textPane.text = "Hallo ich bin neuer Text!"

        textPane.styledDocument = CDocument()

        /*textPane.font = font*/



    }
}