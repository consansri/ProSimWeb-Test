package prosim.ui.components

import prosim.ui.components.console.ConsoleView
import prosim.ui.components.controls.AppControls
import prosim.ui.components.controls.BottomBar
import prosim.ui.components.controls.TopControls
import prosim.ui.components.controls.buttons.ArchSwitch
import prosim.ui.components.docs.InfoView
import prosim.ui.components.editor.CodeEditor
import prosim.ui.components.processor.ProcessorView
import prosim.ui.components.transcript.TranscriptView
import prosim.ui.components.tree.FileTree
import prosim.uilib.UIStates
import prosim.uilib.state.*
import prosim.uilib.styled.*
import prosim.uilib.styled.frame.CustomFrame
import prosim.uilib.styled.params.IconSize
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
class BaseFrame() : CustomFrame(), ProSimFrame {

    override val bottomBar = BottomBar()
    override val topBar = TopControls(showArchSwitch = true)
    override val rightBar = AppControls(this)
    override val processorView = ProcessorView()
    override val transcriptView = TranscriptView()
    override val editor = CodeEditor(bottomBar)
    override val leftBar = editor.getControls()
    override val console: ConsoleView = ConsoleView()
    override val infoView: InfoView = InfoView()
    override val infoTabPane: CAdvancedTabPane = CAdvancedTabPane(primary = false, tabsAreCloseable = false)

    override val wsEditor: WSEditor = object : WSEditor {
        override fun openFile(file: File) {
            editor.openFile(file)
        }

        override fun updateFile(file: File) {
            editor.updateFile(file)
        }
    }
    override val wsLogger: WSLogger = object : WSLogger {
        override fun log(message: String) {
            bottomBar.setWSInfo(message)
        }

        override fun error(message: String) {
            bottomBar.setWSError(message)
        }
    }

    override val fileTree = FileTree(wsEditor, wsLogger)

    override val editorContainer = CSplitPane(JSplitPane.HORIZONTAL_SPLIT, true, fileTree, editor)
    override val processorContainer = CSplitPane(JSplitPane.HORIZONTAL_SPLIT, true, transcriptView, processorView)
    override val mainContainer = CSplitPane(JSplitPane.HORIZONTAL_SPLIT, true, editorContainer, processorContainer)
    override val verticalMainCSplitPane = CSplitPane(JSplitPane.VERTICAL_SPLIT, true, mainContainer, infoTabPane)

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

        infoTabPane.addTab(CIcon(UIStates.icon.get().console, IconSize.PRIMARY_SMALL), console)
        infoTabPane.addTab(CIcon(UIStates.icon.get().info, IconSize.SECONDARY_SMALL), infoView)

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