package me.c3.ui.components

import com.formdev.flatlaf.extras.FlatSVGIcon
import me.c3.ui.components.controls.AppControls
import me.c3.ui.UIManager
import me.c3.ui.components.console.Console
import me.c3.ui.components.editor.CodeEditor
import me.c3.ui.components.editor.EditorControls
import me.c3.ui.styled.ColouredPanel
import me.c3.ui.components.processor.Processor
import me.c3.ui.components.styled.CIconButton
import me.c3.ui.components.styled.CPanel
import me.c3.ui.components.styled.CSplitPane
import me.c3.ui.components.tree.FileTree
import me.c3.ui.styled.CFrame
import me.c3.ui.theme.core.ui.UIAdapter
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Dimension
import java.io.File
import java.io.IOException
import java.nio.file.Paths
import javax.imageio.ImageIO
import javax.swing.ImageIcon
import javax.swing.JFrame
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JSplitPane
import javax.swing.SwingUtilities

class BaseFrame(private val uiManager: UIManager) : CFrame(uiManager), UIAdapter {

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
            attachComponents()

            setupUI(uiManager)
        }
    }

    private fun attachComponents() {
        content.layout = BorderLayout()

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
        addContent(topBar, BorderLayout.NORTH)
        addContent(verticalMainCSplitPane, BorderLayout.CENTER)
        addContent(leftBar, BorderLayout.WEST)
        addContent(rightBar, BorderLayout.EAST)
        addContent(bottomBar, BorderLayout.SOUTH)
    }

    override fun setupUI(uiManager: UIManager) {
        setFrameTitle("ProSim")

        uiManager.themeManager.addThemeChangeListener {
            setDefaults(uiManager)
        }

        setDefaults(uiManager)
    }

    private fun setDefaults(uiManager: UIManager) {
        content.background = uiManager.currTheme().globalLaF.bgSecondary
        SwingUtilities.invokeLater {
            repaint()
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