package ui

import com.formdev.flatlaf.ui.FlatRootPaneUI
import com.formdev.flatlaf.util.SystemInfo
import me.c3.emulator.kit.install
import me.c3.ui.MainManager
import me.c3.ui.components.editor.CDocument
import me.c3.ui.spacing.ScaleManager
import me.c3.ui.styled.CFrame
import me.c3.ui.styled.editor.CEditor
import me.c3.ui.theme.ThemeManager
import me.c3.ui.theme.icons.BenIcons
import ui.components.NativeFrame
import java.awt.BorderLayout
import java.awt.Dimension
import javax.swing.*

fun main() {
    println("#######################################\nWhaaaat ProSimWeb has a jvm ui?\nCrazy! :D\n#######################################")

    if (SystemInfo.isLinux) {
        JFrame.setDefaultLookAndFeelDecorated(true)
        JDialog.setDefaultLookAndFeelDecorated(true)
    }

    testBaseApp()
    //testAdvancedEditor()
}

fun testAdvancedEditor() {
    val manager = MainManager()
    //    manager.themeManager.curr = DarkTheme(BenIcons())

    val editor = CEditor(manager.themeManager, manager.scaleManager)

    val frame = JFrame()
    frame.layout = BorderLayout()
    frame.rootPane.setUI(FlatRootPaneUI())
    frame.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
    frame.setLocationRelativeTo(null)
    frame.size = Dimension(600, 300)

    frame.add(editor, BorderLayout.CENTER)

    frame.isVisible = true
}

fun testFlatWindows() {
    val frame = JFrame()
    frame.rootPane.setUI(FlatRootPaneUI())
    frame.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
    frame.setLocationRelativeTo(null)
    frame.isVisible = true
}

fun testBaseApp() {
    NativeFrame(MainManager())
    //BaseFrame(UIManager())
}

fun testCustomFrame() {
    val frame = CFrame(ThemeManager(BenIcons()), ScaleManager(), BenIcons())
    frame.setFrameTitle("ProSimWeb")
}

fun testTextPane() {
    SwingUtilities.invokeLater {
        val mainManager = MainManager()

        val frame = JFrame()
        frame.size = Dimension(1280, 768)
        frame.isVisible = true
        frame.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
        frame.setLocationRelativeTo(null)

        val textPane = JTextPane()
        textPane.isEditable = true
        frame.add(textPane)

        mainManager.currTheme().codeLaF.getFont().install(textPane, mainManager.currScale().fontScale.codeSize)

        textPane.text = "Hallo ich bin neuer Text!"

        textPane.styledDocument = CDocument()

        /*textPane.font = font*/


    }
}