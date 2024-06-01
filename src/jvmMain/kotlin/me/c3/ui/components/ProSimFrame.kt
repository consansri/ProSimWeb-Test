package me.c3.ui.components

import me.c3.ui.manager.ArchManager
import me.c3.ui.manager.MainManager
import me.c3.ui.components.console.ConsoleView
import me.c3.ui.components.controls.AppControls
import me.c3.ui.components.controls.BottomBar
import me.c3.ui.components.controls.TopControls
import me.c3.ui.components.editor.CodeEditor
import me.c3.ui.components.editor.EditorControls
import me.c3.ui.components.processor.ProcessorView
import me.c3.ui.styled.CSplitPane
import me.c3.ui.components.transcript.TranscriptView
import me.c3.ui.components.tree.FileTree
import me.c3.ui.manager.ScaleManager
import me.c3.ui.styled.CAdvancedTabPane
import me.c3.ui.manager.ThemeManager
import me.c3.ui.resources.icons.ProSimIcons
import me.c3.ui.components.docs.InfoView

/**
 * Interface representing the main frame of the ProSim application.
 * It defines the main components and managers used in the application.
 */
interface ProSimFrame {

    // Editor component for code editing functionality.
    val editor: CodeEditor

    // Component representing the file tree structure.
    val fileTree: FileTree

    // Component displaying processor-related information.
    val processorView: ProcessorView

    // Component displaying transcript information.
    val transcriptView: TranscriptView

    // Bottom bar component for additional controls or status information.
    val bottomBar: BottomBar

    // Top controls component, usually containing main toolbar buttons.
    val topBar: TopControls

    // Left-side controls component, associated with the editor.
    val leftBar: EditorControls

    // Right-side controls component, for additional application controls.
    val rightBar: AppControls

    // Console view component for displaying logs or command outputs.
    val console: ConsoleView

    // Component for displaying information or documentation.
    val infoView: InfoView

    // Advanced tab pane for organizing multiple information tabs.
    val infoTabPane: CAdvancedTabPane

    // Split pane containing the editor and associated components.
    val editorContainer: CSplitPane

    // Split pane containing processor-related components.
    val processorContainer: CSplitPane

    // Main split pane of the application, organizing the primary layout.
    val mainContainer: CSplitPane

    // Vertical split pane for main application areas.
    val verticalMainCSplitPane: CSplitPane

    /**
     * Function to toggle the visibility of various components.
     * @param processorViewVisible Boolean indicating if the processor view should be visible.
     * @param consoleAndInfoVisible Boolean indicating if the console and info views should be visible.
     */
    fun toggleComponents(processorViewVisible: Boolean, consoleAndInfoVisible: Boolean)

}