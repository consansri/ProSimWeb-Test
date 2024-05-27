package me.c3.ui.components

import me.c3.ui.ArchManager
import me.c3.ui.MainManager
import me.c3.ui.components.console.ConsoleView
import me.c3.ui.components.controls.AppControls
import me.c3.ui.components.controls.TopControls
import me.c3.ui.components.processor.ProcessorView
import me.c3.ui.styled.CIconButton
import me.c3.ui.styled.CSplitPane
import me.c3.ui.components.transcript.TranscriptView
import me.c3.ui.components.tree.FileTree
import me.c3.ui.scale.ScaleManager
import me.c3.ui.styled.CAdvancedTabPane
import me.c3.ui.theme.ThemeManager
import me.c3.ui.resources.icons.ProSimIcons
import me.c3.ui.components.docs.InfoView
import me.c3.ui.styled.CIcon
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
class NativeFrame(override val mManager: MainManager) : JFrame(), ProSimFrame {

    override val editor = mManager.editor
    override val fileTree = FileTree(mManager)
    override val processorView = ProcessorView(mManager)
    override val transcriptView = TranscriptView(mManager)
    override val bottomBar = mManager.bBar
    override val topBar = TopControls(mManager, showArchSwitch = true)
    override val leftBar = mManager.editor.getControls()
    override val console: ConsoleView = ConsoleView(mManager)
    override val infoView: InfoView = InfoView(mManager)
    override val rightBar = AppControls(this)
    override val infoTabPane: CAdvancedTabPane = CAdvancedTabPane(getThemeM(), getScaleM(), primary = false, icons = mManager.icons, tabsAreCloseable = false)
    override val editorContainer = CSplitPane(mManager.tm, mManager.sm, JSplitPane.HORIZONTAL_SPLIT, true, fileTree, editor)
    override val processorContainer = CSplitPane(mManager.tm, mManager.sm, JSplitPane.HORIZONTAL_SPLIT, true, transcriptView, processorView)
    override val mainContainer = CSplitPane(mManager.tm, mManager.sm, JSplitPane.HORIZONTAL_SPLIT, true, editorContainer, processorContainer)
    override val verticalMainCSplitPane = CSplitPane(mManager.tm, mManager.sm, JSplitPane.VERTICAL_SPLIT, true, mainContainer, infoTabPane)
    override fun getThemeM(): ThemeManager = mManager.tm
    override fun getScaleM(): ScaleManager = mManager.sm
    override fun getArchM(): ArchManager = mManager.archManager
    override fun getIcons(): ProSimIcons = mManager.icons

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

        infoTabPane.addTab(CIcon(getThemeM(), getScaleM(), mManager.icons.console, mode = CIconButton.Mode.PRIMARY_SMALL), console)
        infoTabPane.addTab(CIcon(getThemeM(), getScaleM(), mManager.icons.info, mode = CIconButton.Mode.SECONDARY_SMALL), infoView)

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
        iconImage = mManager.icons.appLogo.derive(64, 64).image
    }

    /**
     * Sets up the frame title, default close operation, size, and visibility.
     */
    private fun setup() {
        title = "ProSimJVM"
        defaultCloseOperation = EXIT_ON_CLOSE
        size = Dimension(1920, 1080)
        setLocationRelativeTo(null)
        isVisible = true
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
}