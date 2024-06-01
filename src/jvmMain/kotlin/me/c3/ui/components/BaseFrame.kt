package me.c3.ui.components

import me.c3.ui.components.controls.AppControls
import me.c3.ui.components.console.ConsoleView
import me.c3.ui.components.controls.TopControls
import me.c3.ui.components.controls.buttons.ArchSwitch
import me.c3.ui.components.processor.ProcessorView
import me.c3.ui.styled.CIconButton
import me.c3.ui.styled.CSplitPane
import me.c3.ui.components.transcript.TranscriptView
import me.c3.ui.components.tree.FileTree
import me.c3.ui.styled.CAdvancedTabPane
import me.c3.ui.styled.CFrame
import me.c3.ui.resources.icons.ProSimIcons
import me.c3.ui.components.docs.InfoView
import me.c3.ui.manager.*
import me.c3.ui.styled.CIcon
import java.awt.BorderLayout
import java.awt.Dimension
import java.io.File
import java.io.IOException
import javax.imageio.ImageIO
import javax.swing.ImageIcon
import javax.swing.JSplitPane
import javax.swing.SwingUtilities

/**
 * Represents the main UI frame of the application, providing a Swing-based interface.
 * @param mManager The main manager responsible for coordinating UI components and actions.
 */
class BaseFrame() : CFrame( ResManager.icons), ProSimFrame {

    override val editor = MainManager.editor
    override val fileTree = FileTree()
    override val processorView = ProcessorView()
    override val transcriptView = TranscriptView()
    override val bottomBar = MainManager.bBar
    override val topBar = TopControls( showArchSwitch = true)
    override val leftBar = MainManager.editor.getControls()
    override val console: ConsoleView = ConsoleView()
    override val infoView: InfoView = InfoView()
    override val rightBar = AppControls(this)

    override val infoTabPane: CAdvancedTabPane = CAdvancedTabPane( primary = false, icons = ResManager.icons, tabsAreCloseable = false)

    override val editorContainer = CSplitPane( JSplitPane.HORIZONTAL_SPLIT, true, fileTree, editor)
    override val processorContainer = CSplitPane( JSplitPane.HORIZONTAL_SPLIT, true, transcriptView, processorView)
    override val mainContainer = CSplitPane( JSplitPane.HORIZONTAL_SPLIT, true, editorContainer, processorContainer)
    override val verticalMainCSplitPane = CSplitPane( JSplitPane.VERTICAL_SPLIT, true, mainContainer, infoTabPane)

    private val mainDivider = 0.4
    private val verticalDivider = 0.8

    /**
     * Toggles the visibility and layout of specific components dynamically based on boolean flags.
     * @param processorViewVisible Flag indicating whether the processor view should be visible.
     * @param consoleAndInfoVisible Flag indicating whether the console and info views should be visible.
     */
    override fun toggleComponents(processorViewVisible: Boolean, consoleAndInfoVisible: Boolean) {
        if (consoleAndInfoVisible) {
            verticalMainCSplitPane.setDividerLocation(verticalDivider)
            verticalMainCSplitPane.isOneTouchExpandable = true
        } else {
            verticalMainCSplitPane.setDividerLocation(1.0)
            verticalMainCSplitPane.isOneTouchExpandable = false
        }

        if (!processorViewVisible) {
            mainContainer.setDividerLocation(1.0)
            mainContainer.isOneTouchExpandable = false
        } else {
            mainContainer.setDividerLocation(mainDivider)
            mainContainer.isOneTouchExpandable = true
        }

        revalidate()
        repaint()
    }

    init {
        SwingUtilities.invokeLater {
            attachComponents()
            setup()
        }
    }

    private fun attachComponents() {
        content.layout = BorderLayout()

        infoTabPane.addTab(CIcon( ResManager.icons.console, CIconButton.Mode.PRIMARY_SMALL), console)
        infoTabPane.addTab(CIcon( ResManager.icons.info, CIconButton.Mode.SECONDARY_SMALL), infoView)

        infoTabPane.select(0)

        // Set Sizes
        editor.minimumSize = Dimension(0, 0)
        fileTree.minimumSize = Dimension(0, 0)
        processorView.minimumSize = Dimension(0, 0)
        infoTabPane.minimumSize = Dimension(0, 0)

        editorContainer.resizeWeight = 0.1
        processorContainer.resizeWeight = 0.0
        mainContainer.resizeWeight = 0.6
        verticalMainCSplitPane.resizeWeight = 1.0

        // Add split panes to the frame with BorderLayout constraints
        addContent(topBar, BorderLayout.NORTH)
        addContent(verticalMainCSplitPane, BorderLayout.CENTER)
        addContent(leftBar, BorderLayout.WEST)
        addContent(rightBar, BorderLayout.EAST)
        addContent(bottomBar, BorderLayout.SOUTH)
        addTitleBar(ArchSwitch())
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