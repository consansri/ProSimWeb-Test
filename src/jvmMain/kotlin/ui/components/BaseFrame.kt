package me.c3.ui.components

import me.c3.ui.components.controls.AppControls
import me.c3.ui.UIManager
import me.c3.ui.components.console.Console
import me.c3.ui.components.controls.TopControls
import me.c3.ui.components.controls.buttons.ArchSwitch
import me.c3.ui.components.processor.ProcessorView
import me.c3.ui.components.styled.CSplitPane
import me.c3.ui.components.transcript.TranscriptView
import me.c3.ui.components.tree.FileTree
import me.c3.ui.styled.CFrame
import java.awt.BorderLayout
import java.awt.Dimension
import java.io.File
import java.io.IOException
import javax.imageio.ImageIO
import javax.swing.ImageIcon
import javax.swing.JSplitPane
import javax.swing.SwingUtilities

class BaseFrame(private val uiManager: UIManager) : CFrame(uiManager.themeManager, uiManager.scaleManager, uiManager.icons) {

    private val editor = uiManager.editor
    private val fileTree = FileTree(uiManager)
    val processorView = ProcessorView(uiManager)
    val transcriptView = TranscriptView(uiManager)
    private val bottomBar = uiManager.bBar
    private val topBar = TopControls(uiManager)
    private val leftBar = uiManager.editor.getControls()
    private val rightBar = AppControls(this, uiManager)
    private val consoleAndInfo = Console(uiManager)

    init {
        SwingUtilities.invokeLater {
            setup()
            attachComponents()
        }
    }

    private fun attachComponents() {
        content.layout = BorderLayout()

        // Set Sizes
        editor.minimumSize = Dimension(0, 0)
        fileTree.minimumSize = Dimension(0, 0)
        processorView.minimumSize = Dimension(0, 0)
        consoleAndInfo.minimumSize = Dimension(0, 0)

        val editorContainer = CSplitPane(uiManager.themeManager, uiManager.scaleManager, JSplitPane.HORIZONTAL_SPLIT, true, fileTree, editor)
        editorContainer.resizeWeight = 0.1

        val processorContainer = CSplitPane(uiManager.themeManager, uiManager.scaleManager, JSplitPane.HORIZONTAL_SPLIT, true, transcriptView, processorView)
        processorContainer.resizeWeight = 0.0

        val mainContainer = CSplitPane(uiManager.themeManager, uiManager.scaleManager, JSplitPane.HORIZONTAL_SPLIT, true, editorContainer, processorContainer)
        mainContainer.resizeWeight = 0.6

        val verticalMainCSplitPane = CSplitPane(uiManager.themeManager, uiManager.scaleManager, JSplitPane.VERTICAL_SPLIT, true, mainContainer, consoleAndInfo)
        verticalMainCSplitPane.resizeWeight = 1.0

        // Add split panes to the frame with BorderLayout constraints
        addContent(topBar, BorderLayout.NORTH)
        addContent(verticalMainCSplitPane, BorderLayout.CENTER)
        addContent(leftBar, BorderLayout.WEST)
        addContent(rightBar, BorderLayout.EAST)
        addContent(bottomBar, BorderLayout.SOUTH)
        addTitleBar(ArchSwitch(uiManager))
    }

    private fun setup() {
        SwingUtilities.invokeLater {
            //setFrameTitle("ProSim")
            defaultCloseOperation = EXIT_ON_CLOSE
            size = Dimension(1920, 1080)
            setLocationRelativeTo(null)
            isVisible = true
        }
    }

    private fun loadImage(path: String): ImageIcon? {
        return try {
            val url = File(path).toURI().toURL()
            ImageIcon(ImageIO.read(url))
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }
}