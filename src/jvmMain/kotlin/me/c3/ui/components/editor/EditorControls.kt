package me.c3.ui.components.editor

import me.c3.ui.manager.*
import me.c3.ui.styled.CIconButton
import me.c3.ui.styled.CPanel
import me.c3.ui.styled.params.BorderMode
import javax.swing.BorderFactory
import javax.swing.BoxLayout

/**
 * Represents the control panel for the code editor.
 * @property mainManager The main manager instance.
 * @property editor The code editor associated with the controls.
 */
class EditorControls( private val editor: CodeEditor) : CPanel( false, borderMode = BorderMode.EAST) {

    // Control buttons
    private val statusIcon: CIconButton = CIconButton( ResManager.icons.statusLoading)
    private val undoButton: CIconButton = CIconButton( ResManager.icons.backwards)
    private val redoButton: CIconButton = CIconButton( ResManager.icons.forwards)
    private val buildButton: CIconButton = CIconButton( ResManager.icons.build)
    private val infoButton: CIconButton = CIconButton( ResManager.icons.info)

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
        ScaleManager.addScaleChangeEvent {
            val insets = it.borderScale.insets
            border = BorderFactory.createEmptyBorder(insets, insets, insets, insets)
        }
        ThemeManager.addThemeChangeListener {
            background = it.globalLaF.bgSecondary
        }

        // Functions
        installStatusButton()
        installBuildButton(editor)

        // Set Defaults
        val insets = ScaleManager.curr.borderScale.insets
        border = BorderFactory.createEmptyBorder(insets, insets, insets, insets)
        background = ThemeManager.curr.globalLaF.bgSecondary

        statusIcon.isDeactivated = true
        statusIcon.rotating = true
    }

    /**
     * Installs the status button and handles its functionality.
     * @param mainManager The main manager instance.
     */
    private fun installStatusButton() {
        MainManager.editor.addFileEditEvent {
            statusIcon.svgIcon = ResManager.icons.statusLoading
            statusIcon.rotating = true
        }

        EventManager.addCompileListener { result ->
            if (result.success) {
                statusIcon.svgIcon = ResManager.icons.statusFine
                statusIcon.rotating = false
            } else {
                statusIcon.svgIcon = ResManager.icons.statusError
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