package me.c3.ui.components

import me.c3.ui.components.controls.AppControls
import me.c3.ui.UIManager
import me.c3.ui.components.console.Console
import me.c3.ui.components.editor.CodeEditor
import me.c3.ui.components.editor.EditorControls
import me.c3.ui.styled.ColouredPanel
import me.c3.ui.components.processor.Processor
import me.c3.ui.components.styled.CPanel
import me.c3.ui.components.styled.CSplitPane
import me.c3.ui.components.tree.FileTree
import java.awt.BorderLayout
import java.awt.Dimension
import java.nio.file.Paths
import javax.swing.JFrame
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JSplitPane
import javax.swing.SwingUtilities

class BaseFrame(title: String) : JFrame(title) {
    private val uiManager = UIManager(this)

    private val topBar = CPanel(uiManager, primary = false)
    private val editor = CodeEditor(uiManager)
    private val fileTree = FileTree(uiManager, Paths.get("").toAbsolutePath().toString())
    private val leftBar = EditorControls(uiManager, editor)
    private val processor = Processor(uiManager)
    private val rightBar = AppControls(uiManager, this)
    private val consoleAndInfo = Console(uiManager)
    private val bottomBar = ColouredPanel(uiManager, false)

    init {
        SwingUtilities.invokeLater {
            layout = BorderLayout()

            // Set Sizes
            editor.minimumSize = Dimension(0, 0)
            fileTree.minimumSize = Dimension(0, 0)
            processor.minimumSize = Dimension(0, 0)
            consoleAndInfo.minimumSize = Dimension(0, 0)

            val bottomBarLabel = JLabel("Bottom Bar")

            bottomBar.add(bottomBarLabel)

            val editorContainer = CSplitPane(uiManager, JSplitPane.HORIZONTAL_SPLIT, true, fileTree, editor)
            editorContainer.resizeWeight = 0.25

            val mainContainer = CSplitPane(uiManager, JSplitPane.HORIZONTAL_SPLIT, true, editorContainer, processor)
            mainContainer.resizeWeight = 0.5

            val verticalMainCSplitPane = CSplitPane(uiManager, JSplitPane.VERTICAL_SPLIT, true, mainContainer, consoleAndInfo)
            verticalMainCSplitPane.resizeWeight = 0.8

            //verticalMainSplitPane.setDividerLocation(0.8)

            // Add split panes to the frame with BorderLayout constraints
            add(topBar, BorderLayout.NORTH)
            add(verticalMainCSplitPane, BorderLayout.CENTER)
            add(leftBar, BorderLayout.WEST)
            add(rightBar, BorderLayout.EAST)
            add(bottomBar, BorderLayout.SOUTH)

            setSize(1920, 1080)
            setLocationRelativeTo(null)
            defaultCloseOperation = EXIT_ON_CLOSE
            isVisible = true

            // Listeners
            uiManager.themeManager.addThemeChangeListener {
                background = it.globalLaF.bgSecondary
            }

            // Apply defaults
            background = uiManager.currTheme().globalLaF.bgSecondary

            //pack() // set swing preferred size
        }
    }
}