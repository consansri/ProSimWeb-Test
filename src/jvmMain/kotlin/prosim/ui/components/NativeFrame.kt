package prosim.ui.components

import emulator.kit.nativeLog
import prosim.ui.components.console.ConsoleView
import prosim.ui.components.controls.AppControls
import prosim.ui.components.controls.BottomBar
import prosim.ui.components.controls.TopControls
import prosim.ui.components.docs.InfoView
import prosim.ui.components.editor.CodeEditor
import prosim.ui.components.processor.ProcessorView
import prosim.ui.components.transcript.TranscriptView
import prosim.ui.components.tree.FileTree
import prosim.uilib.UIStates
import prosim.uilib.scale.core.Scaling
import prosim.uilib.state.*
import prosim.uilib.styled.CAdvancedTabPane
import prosim.uilib.styled.CIcon
import prosim.uilib.styled.CIconButton
import prosim.uilib.styled.CSplitPane
import prosim.uilib.theme.core.Theme
import java.awt.BorderLayout
import java.awt.Dimension
import java.io.File
import java.io.IOException
import javax.imageio.ImageIO
import javax.swing.ImageIcon
import javax.swing.JFrame
import javax.swing.JSplitPane
import javax.swing.SwingUtilities

/**
 * Represents the main frame of the application, built using Swing components.
 * @param mManager The main manager responsible for coordinating UI components and actions.
 */
class NativeFrame : JFrame(), ProSimFrame {

    val themeListener = object : StateListener<Theme> {
        override suspend fun onStateChange(newVal: Theme) {
            nativeLog("${this.javaClass.simpleName} onStateChange(theme: Theme)")
            revalidate()
            repaint()
        }
    }

    val scaleListener = object : StateListener<Scaling> {
        override suspend fun onStateChange(newVal: Scaling) {
            nativeLog("${this.javaClass.simpleName} onStateChange(scaling: Scaling)")
            revalidate()
            repaint()
        }
    }

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

    init {
        SwingUtilities.invokeLater {
            styleFrame()
            attachComponents()
            setup()
            toggleComponents(false, false)
        }
    }

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

    /**
     * Attaches UI components to the frame and sets up their initial configuration.
     */
    private fun attachComponents() {
        layout = BorderLayout()

        infoTabPane.addTab(CIcon(UIStates.icon.get().console, mode = CIconButton.Mode.PRIMARY_SMALL), console)
        infoTabPane.addTab(CIcon(UIStates.icon.get().info, mode = CIconButton.Mode.SECONDARY_SMALL), infoView)

        infoTabPane.select(0)

        // Set Sizes
        editor.minimumSize = Dimension(0, 0)
        fileTree.minimumSize = Dimension(0, 0)
        processorView.minimumSize = Dimension(0, 0)
        infoTabPane.minimumSize = Dimension(0, 0)

        editorContainer.resizeWeight = 0.2
        processorContainer.resizeWeight = 0.5

        // Add split panes to the frame with BorderLayout constraints
        add(topBar, BorderLayout.NORTH)
        add(verticalMainCSplitPane, BorderLayout.CENTER)
        add(leftBar, BorderLayout.WEST)
        add(rightBar, BorderLayout.EAST)
        add(bottomBar, BorderLayout.SOUTH)
    }

    /**
     * Styles the frame, setting its icon image.
     */
    private fun styleFrame() {
        iconImage = UIStates.icon.get().appLogo.derive(64, 64).image
    }

    /**
     * Sets up the frame title, default close operation, size, and visibility.
     */
    private fun setup() {
        title = "ProSimJVM"
        defaultCloseOperation = EXIT_ON_CLOSE
        size = Dimension(1920, 1080)
        setLocationRelativeTo(null)

        UIStates.theme.addEvent(themeListener)
        UIStates.scale.addEvent(scaleListener)

        isVisible = true

        revalidate()
        repaint()
    }

    /**
     * Loads an image from the specified path and returns it as an ImageIcon.
     * @param path The path to the image file.
     * @return ImageIcon representing the loaded image, or null if loading fails.
     */
    private fun loadImage(path: String): ImageIcon? {
        return try {
            val url = File(path).toURI().toURL()
            ImageIcon(ImageIO.read(url))
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }

    override fun dispose() {
        super.dispose()
        UIStates.theme.removeEvent(themeListener)
        UIStates.scale.removeEvent(scaleListener)
    }
}