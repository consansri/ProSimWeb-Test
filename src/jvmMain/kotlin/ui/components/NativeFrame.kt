package ui.components

import me.c3.ui.MainManager
import me.c3.ui.components.console.Console
import me.c3.ui.components.controls.AppControls
import me.c3.ui.components.controls.TopControls
import me.c3.ui.components.processor.ProcessorView
import me.c3.ui.components.styled.CSplitPane
import me.c3.ui.components.transcript.TranscriptView
import me.c3.ui.components.tree.FileTree
import java.awt.BorderLayout
import java.awt.Dimension
import java.io.File
import java.io.IOException
import javax.imageio.ImageIO
import javax.swing.ImageIcon
import javax.swing.JFrame
import javax.swing.JSplitPane
import javax.swing.SwingUtilities
import javax.swing.UIManager

class NativeFrame(private val mainManager: MainManager) : JFrame() {

    private val editor = mainManager.editor
    private val fileTree = FileTree(mainManager)
    val processorView = ProcessorView(mainManager)
    val transcriptView = TranscriptView(mainManager)
    private val bottomBar = mainManager.bBar
    private val topBar = TopControls(mainManager, showArchSwitch = true)
    private val leftBar = mainManager.editor.getControls()
    private val rightBar = AppControls(mainManager)
    private val consoleAndInfo = Console(mainManager)

    init {
        SwingUtilities.invokeLater {
            styleFrame()
            attachComponents()
            setup()
        }
    }

    private fun attachComponents() {
        layout = BorderLayout()

        // Set Sizes
        editor.minimumSize = Dimension(0, 0)
        fileTree.minimumSize = Dimension(0, 0)
        processorView.minimumSize = Dimension(0, 0)
        consoleAndInfo.minimumSize = Dimension(0, 0)

        val editorContainer = CSplitPane(mainManager.themeManager, mainManager.scaleManager, JSplitPane.HORIZONTAL_SPLIT, true, fileTree, editor)
        editorContainer.resizeWeight = 0.1

        val processorContainer = CSplitPane(mainManager.themeManager, mainManager.scaleManager, JSplitPane.HORIZONTAL_SPLIT, true, transcriptView, processorView)
        processorContainer.resizeWeight = 0.0

        val mainContainer = CSplitPane(mainManager.themeManager, mainManager.scaleManager, JSplitPane.HORIZONTAL_SPLIT, true, editorContainer, processorContainer)
        mainContainer.resizeWeight = 0.6

        val verticalMainCSplitPane = CSplitPane(mainManager.themeManager, mainManager.scaleManager, JSplitPane.VERTICAL_SPLIT, true, mainContainer, consoleAndInfo)
        verticalMainCSplitPane.resizeWeight = 1.0

        // Add split panes to the frame with BorderLayout constraints
        add(topBar, BorderLayout.NORTH)
        add(verticalMainCSplitPane, BorderLayout.CENTER)
        add(leftBar, BorderLayout.WEST)
        add(rightBar, BorderLayout.EAST)
        add(bottomBar, BorderLayout.SOUTH)
    }

    private fun styleFrame() {
        iconImage = mainManager.icons.appLogo.derive(64,64).image
    }

    private fun setup() {
        title = "ProSimJVM"
        defaultCloseOperation = EXIT_ON_CLOSE
        size = Dimension(1920, 1080)
        setLocationRelativeTo(null)
        isVisible = true
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