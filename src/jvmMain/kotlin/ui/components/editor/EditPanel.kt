package me.c3.ui.components.editor

import me.c3.ui.components.borders.DirectionalBorder
import me.c3.ui.resources.UIManager
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.awt.GridLayout
import javax.swing.BorderFactory
import javax.swing.JPanel
import javax.swing.JTextPane

class EditPanel(uiManager: UIManager): JPanel() {

    val lineNumbers = JTextPane()
    val textPane = JTextPane()

    init {
        layout = GridBagLayout()
        val constraints = GridBagConstraints()

        lineNumbers.text = "1\n2\n3\n4\n5\n..."
        lineNumbers.border = DirectionalBorder(uiManager, east = true)

        textPane.text = "Code ..."

        constraints.gridx = 0
        constraints.gridy = 0
        constraints.weightx = 0.0
        constraints.weighty = 1.0
        constraints.fill = GridBagConstraints.BOTH
        constraints.anchor = GridBagConstraints.NORTHWEST

        add(lineNumbers, constraints)

        constraints.gridx = 1
        constraints.weightx = 1.0
        add(textPane, constraints)
    }

}