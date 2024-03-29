package me.c3

import com.formdev.flatlaf.FlatIntelliJLaf
import com.formdev.flatlaf.extras.FlatSVGIcon
import com.formdev.flatlaf.util.SystemInfo
import me.c3.ui.components.editor.CodeEditor
import me.c3.ui.components.frame.BaseFrame
import me.c3.ui.UIManager
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


    //FlatDarculaLaf.setup()

    testBaseApp()
}

fun testBaseApp() {
    BaseFrame("BaseApp")
}

fun exampleTheme() {
    val frame1 = JFrame("ProSimDesktop")
    frame1.contentPane.add(CodeEditor(UIManager(frame1)), BorderLayout.CENTER)
    frame1.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
    frame1.size = Dimension(600, 400)
    frame1.setLocationRelativeTo(null)
    frame1.isVisible = true

    val button = JButton("Change Theme")
    button.addActionListener {
        FlatIntelliJLaf.setup()
        FlatIntelliJLaf.updateUI()
        println("Button clicked!")
    }
    button.isVisible = true
    frame1.contentPane.add(button)
}

fun exampleEditor() {
    val frame = JFrame("Syntax Highlighting Example")
    val textPane = JTextPane()
    val scrollPane = JScrollPane(textPane)

    // Create a styled document for the text pane
    val document = DefaultStyledDocument()

    // Set the document for the text pane
    textPane.document = document

    // Set syntax highlighting for specific keywords
    val keywordAttributes = SimpleAttributeSet()
    StyleConstants.setForeground(keywordAttributes, java.awt.Color.BLUE) // Set keyword color

    val keywords = arrayOf("fun", "val", "var", "if", "else", "for", "while", "return", "class") // Example keywords

    for (keyword in keywords) {
        document.insertString(document.length, "$keyword ", keywordAttributes)
    }

    // Set syntax highlighting for specific keywords
    val keywordAttributes1 = SimpleAttributeSet()
    StyleConstants.setForeground(keywordAttributes1, java.awt.Color.RED) // Set keyword color

    val keywords1 = arrayOf("gustav") // Example keywords

    for (keyword in keywords1) {
        document.insertString(document.length, "$keyword ", keywordAttributes1)
    }



    frame.add(scrollPane)
    frame.setSize(400, 300)
    frame.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
    frame.isVisible = true
}

fun exampleSVG(){
    SwingUtilities.invokeLater {
        // Load SVG icon
        val svgIcon = FlatSVGIcon("benicons/add.svg")

        // Create a JLabel and set the SVG icon
        val iconLabel = JLabel(svgIcon)

        // Create a JFrame and add the icon label to it
        val frame = JFrame("SVG Icon Example")
        frame.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
        frame.contentPane.add(iconLabel)
        frame.pack()
        frame.setLocationRelativeTo(null) // Center the frame
        frame.isVisible = true
    }
}