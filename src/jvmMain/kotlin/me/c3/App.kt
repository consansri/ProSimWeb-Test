package me.c3

import java.awt.BorderLayout
import java.awt.Dimension
import javax.swing.JFrame
import javax.swing.JScrollPane
import javax.swing.JTextArea

fun main() {
    println("Hello from jvm!")

    val textArea = JTextArea()
    textArea.text = "Hello, Kotlin/Swing world!"
    val scrollPane = JScrollPane(textArea)

    val frame = JFrame("ProSimDesktop")
    frame.contentPane.add(scrollPane, BorderLayout.CENTER)
    frame.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
    frame.size = Dimension(600, 400)
    frame.setLocationRelativeTo(null)
    frame.isVisible = true

}