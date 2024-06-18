package me.c3.ui.components.editor

import me.c3.ui.Events
import me.c3.ui.States
import me.c3.ui.state.*
import me.c3.ui.styled.CIconButton
import me.c3.ui.styled.CPanel
import me.c3.ui.styled.params.BorderMode
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
    private val statusIcon: CIconButton = CIconButton(States.icon.get().statusLoading)
    private val undoButton: CIconButton = CIconButton(States.icon.get().backwards)
    private val redoButton: CIconButton = CIconButton(States.icon.get().forwards)
    private val buildButton: CIconButton = CIconButton(States.icon.get().build)
    private val infoButton: CIconButton = CIconButton(States.icon.get().info)

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
        States.scale.addEvent(WeakReference(this)) {
            val insets = it.borderScale.insets
            border = BorderFactory.createEmptyBorder(insets, insets, insets, insets)
        }
        States.theme.addEvent(WeakReference(this)) {
            background = it.globalLaF.bgSecondary
        }

        // Functions
        installStatusButton()
        installBuildButton(editor)

        // Set Defaults
        val insets = States.scale.get().borderScale.insets
        border = BorderFactory.createEmptyBorder(insets, insets, insets, insets)
        background = States.theme.get().globalLaF.bgSecondary

        statusIcon.isDeactivated = true
        statusIcon.rotating = true
    }

    /**
     * Installs the status button and handles its functionality.
     * @param mainManager The main manager instance.
     */
    private fun installStatusButton() {
        Events.fileEdit.addListener(WeakReference(this)) {
            statusIcon.svgIcon = States.icon.get().statusLoading
            statusIcon.rotating = true
        }

        Events.compile.addListener(WeakReference(this)) { result ->
            if (result.success) {
                statusIcon.svgIcon = States.icon.get().statusFine
                statusIcon.rotating = false
            } else {
                statusIcon.svgIcon = States.icon.get().statusError
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