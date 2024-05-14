package me.c3.ui.components

import me.c3.ui.ArchManager
import me.c3.ui.MainManager
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
import me.c3.ui.scale.ScaleManager
import me.c3.ui.styled.CAdvancedTabPane
import me.c3.ui.theme.ThemeManager
import me.c3.ui.resources.icons.ProSimIcons
import me.c3.ui.components.docs.InfoView

interface ProSimFrame {

    val mManager: MainManager
    val editor: CodeEditor
    val fileTree: FileTree
    val processorView: ProcessorView
    val transcriptView: TranscriptView
    val bottomBar: BottomBar
    val topBar: TopControls
    val leftBar: EditorControls
    val rightBar: AppControls
    val console: ConsoleView
    val infoView: InfoView

    val infoTabPane: CAdvancedTabPane
    val editorContainer: CSplitPane
    val processorContainer: CSplitPane
    val mainContainer: CSplitPane
    val verticalMainCSplitPane: CSplitPane

    fun getThemeM(): ThemeManager

    fun getScaleM(): ScaleManager

    fun getArchM(): ArchManager

    fun getIcons(): ProSimIcons

    fun toggleComponents(processorViewVisible: Boolean, consoleAndInfoVisible: Boolean)

}