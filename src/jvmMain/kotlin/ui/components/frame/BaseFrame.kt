package me.c3.ui.components.frame

import me.c3.ui.components.controls.AppControls
import me.c3.ui.resources.UIManager
import java.awt.BorderLayout
import java.awt.Dimension
import javax.swing.JFrame
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JSplitPane
import javax.swing.SwingUtilities

class BaseFrame(title: String) : JFrame(title) {
    val UIManager = UIManager()

    init {
        SwingUtilities.invokeLater {
            layout = BorderLayout()

            // Create components
            val topBar = JPanel()
            val leftBar = JPanel()
            val editor = JPanel()
            val processor = JPanel()
            val rightBar = AppControls(UIManager, this)
            val consoleAndInfo = JPanel()
            val bottomBar = JPanel()

            editor.minimumSize = Dimension(0, 0)
            processor.minimumSize = Dimension(0, 0)
            consoleAndInfo.minimumSize = Dimension(0, 0)

            // Create labels for each region
            val topBarLabel = JLabel("TopBar")
            val lControlLabel = JLabel("LControls")
            val editorLabel = JLabel("Editor")
            val processorLabel = JLabel("Processor")
            val consoleAndInfoLabel = JLabel("Console and Info")
            val bottomBarLabel = JLabel("Bottom Bar")

            // Add labels to panels
            topBar.add(topBarLabel)
            leftBar.add(lControlLabel)
            editor.add(editorLabel)
            processor.add(processorLabel)
            consoleAndInfo.add(consoleAndInfoLabel)
            bottomBar.add(bottomBarLabel)

            val mainContainer = JSplitPane(JSplitPane.HORIZONTAL_SPLIT, true, editor, processor)
            mainContainer.resizeWeight = 0.5

            val verticalMainSplitPane = JSplitPane(JSplitPane.VERTICAL_SPLIT, true, mainContainer, consoleAndInfo)
            verticalMainSplitPane.resizeWeight = 0.8

            //verticalMainSplitPane.setDividerLocation(0.8)

            // Add split panes to the frame with BorderLayout constraints
            add(topBar, BorderLayout.NORTH)
            add(verticalMainSplitPane, BorderLayout.CENTER)
            add(leftBar, BorderLayout.WEST)
            add(rightBar, BorderLayout.EAST)
            add(bottomBar, BorderLayout.SOUTH)

            setSize(1920, 1080)
            setLocationRelativeTo(null)
            defaultCloseOperation = EXIT_ON_CLOSE
            isVisible = true
        }

    }
}