package prosim.ui.components.editor

import prosim.ui.Events
import prosim.uilib.UIStates
import prosim.uilib.state.*
import prosim.uilib.styled.CIconButton
import prosim.uilib.styled.CPanel
import prosim.uilib.styled.params.BorderMode
import java.lang.ref.WeakReference
import javax.swing.BorderFactory
import javax.swing.BoxLayout

/**
 * Represents the control panel for the code editor.
 * @property mainManager The main manager instance.
 * @property editor The code editor associated with the controls.
 */
class EditorControls(private val editor: CodeEditor) : CPanel(false, borderMode = BorderMode.EAST) {

    // Control buttons
    private val statusIcon: CIconButton = CIconButton(UIStates.icon.get().statusLoading)
    private val undoButton: CIconButton = CIconButton(UIStates.icon.get().backwards)
    private val redoButton: CIconButton = CIconButton(UIStates.icon.get().forwards)
    private val buildButton: CIconButton = CIconButton(UIStates.icon.get().build)
    private val infoButton: CIconButton = CIconButton(UIStates.icon.get().info)

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
        UIStates.scale.addEvent(WeakReference(this)) {
            val insets = it.borderScale.insets
            border = BorderFactory.createEmptyBorder(insets, insets, insets, insets)
        }
        UIStates.theme.addEvent(WeakReference(this)) {
            background = it.globalLaF.bgSecondary
        }

        // Functions
        installStatusButton()
        installBuildButton(editor)

        // Set Defaults
        val insets = UIStates.scale.get().borderScale.insets
        border = BorderFactory.createEmptyBorder(insets, insets, insets, insets)
        background = UIStates.theme.get().globalLaF.bgSecondary

        statusIcon.isDeactivated = true
        statusIcon.rotating = true
    }

    /**
     * Installs the status button and handles its functionality.
     * @param mainManager The main manager instance.
     */
    private fun installStatusButton() {
        Events.fileEdit.addListener(WeakReference(this)) {
            statusIcon.svgIcon = UIStates.icon.get().statusLoading
            statusIcon.rotating = true
        }

        Events.compile.addListener(WeakReference(this)) { result ->
            if (result.success) {
                statusIcon.svgIcon = UIStates.icon.get().statusFine
                statusIcon.rotating = false
            } else {
                statusIcon.svgIcon = UIStates.icon.get().statusError
                statusIcon.rotating = false
            }
        }
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