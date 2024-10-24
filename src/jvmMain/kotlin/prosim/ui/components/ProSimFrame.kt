package prosim.ui.components

import prosim.ui.components.console.ConsoleView
import prosim.ui.components.controls.AppControls
import prosim.ui.components.controls.BottomBar
import prosim.ui.components.controls.TopControls
import prosim.ui.components.editor.CodeEditor
import prosim.ui.components.editor.EditorControls
import prosim.ui.components.processor.ProcessorView
import prosim.uilib.styled.CSplitPane
import prosim.ui.components.transcript.TranscriptView
import prosim.ui.components.tree.FileTree
import prosim.uilib.styled.CAdvancedTabPane
import prosim.ui.components.docs.InfoView
import prosim.uilib.state.WSEditor
import prosim.uilib.state.WSLogger

/**
 * Interface representing the main frame of the ProSim application.
 * It defines the main components and managers used in the application.
 */
interface ProSimFrame {

    // Bottom bar component for additional controls or status information.
    val bottomBar: BottomBar

    // Top controls component, usually containing main toolbar buttons.
    val topBar: TopControls

    // Right-side controls component, for additional application controls.
    val rightBar: AppControls

    // Component displaying processor-related information.
    val processorView: ProcessorView

    // Component displaying transcript information.
    val transcriptView: TranscriptView

    // Editor component for code editing functionality.
    val editor: CodeEditor

    // Left-side controls component, associated with the editor.
    val leftBar: EditorControls

    // Console view component for displaying logs or command outputs.
    val console: ConsoleView

    // Component for displaying information or documentation.
    val infoView: InfoView

    // Advanced tab pane for organizing multiple information tabs.
    val infoTabPane: CAdvancedTabPane

    val wsEditor: WSEditor
    val wsLogger: WSLogger

    // Component representing the file tree structure.
    val fileTree: FileTree

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