package prosim.ui.components.editor

import prosim.ui.Events
import prosim.uilib.UIStates
import prosim.uilib.scale.core.Scaling
import prosim.uilib.state.*
import prosim.uilib.styled.CIconButton
import prosim.uilib.styled.CPanel
import prosim.uilib.styled.params.BorderMode
import prosim.uilib.theme.core.Theme
import javax.swing.BorderFactory
import javax.swing.BoxLayout

/**
 * Represents the control panel for the code editor.
 * @property mainManager The main manager instance.
 * @property editor The code editor associated with the controls.
 */
class EditorControls(private val editor: CodeEditor) : CPanel(false, borderMode = BorderMode.EAST) {

    val scaleListener = object : StateListener<Scaling> {
        override suspend fun onStateChange(newVal: Scaling) {
            val insets = newVal.SIZE_INSET_MEDIUM
            border = BorderFactory.createEmptyBorder(insets, insets, insets, insets)
        }
    }

    val themeListener = object : StateListener<Theme> {
        override suspend fun onStateChange(newVal: Theme) {
            background = newVal.COLOR_BG_1
        }
    }

    // Control buttons
    private val statusIcon: CIconButton = CIconButton(UIStates.icon.get().statusLoading)
    private val undoButton: CIconButton = CIconButton(UIStates.icon.get().backwards)
    private val redoButton: CIconButton = CIconButton(UIStates.icon.get().forwards)
    private val buildButton: CIconButton = CIconButton(UIStates.icon.get().build)
    private val infoButton: CIconButton = CIconButton(UIStates.icon.get().info)

    val fileEditListener = Events.fileEdit.createAndAddListener {
        statusIcon.svgIcon = UIStates.icon.get().statusLoading
        statusIcon.rotating = true
    }

    val compileListener = Events.compile.createAndAddListener { result ->
        if (result.success) {
            statusIcon.svgIcon = UIStates.icon.get().statusFine
            statusIcon.rotating = false
        } else {
            statusIcon.svgIcon = UIStates.icon.get().statusError
            statusIcon.rotating = false
        }
    }

    init {
        // Apply layout
        layout = BoxLayout(this, BoxLayout.Y_AXIS)
        statusIcon.alignmentX = CENTER_ALIGNMENT
        undoButton.alignmentX = CENTER_ALIGNMENT
        redoButton.alignmentX = CENTER_ALIGNMENT
        buildButton.alignmentX = CENTER_ALIGNMENT
        infoButton.alignmentX = CENTER_ALIGNMENT

        // Add Components
        add(statusIcon)
        add(undoButton)
        add(redoButton)
        add(buildButton)
        add(infoButton)

        // Listeners
        UIStates.scale.addEvent(scaleListener)
        UIStates.theme.addEvent(themeListener)

        // Functions
        installBuildButton(editor)

        // Set Defaults
        val insets = UIStates.scale.get().SIZE_INSET_MEDIUM
        border = BorderFactory.createEmptyBorder(insets, insets, insets, insets)
        background = UIStates.theme.get().COLOR_BG_1

        statusIcon.isDeactivated = true
        statusIcon.rotating = true
    }

    /**
     * Installs the build button and handles its functionality.
     * @param codeEditor The code editor instance.
     */
    private fun installBuildButton(codeEditor: CodeEditor) {
        buildButton.addActionListener {
            codeEditor.compileCurrent(build = true)
        }
        undoButton.addActionListener {
            codeEditor.getCurrentEditor()?.undo()
        }
        redoButton.addActionListener {
            codeEditor.getCurrentEditor()?.redo()
        }
    }
}