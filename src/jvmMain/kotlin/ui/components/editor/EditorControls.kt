package me.c3.ui.components.editor

import emulator.kit.common.ArchState
import io.nacular.doodle.controls.list.listEditor
import me.c3.ui.UIManager
import me.c3.ui.components.styled.CIconButton
import me.c3.ui.components.styled.CPanel
import javax.swing.BorderFactory
import javax.swing.BoxLayout

class EditorControls(uiManager: UIManager, private val editor: CodeEditor) : CPanel(uiManager.themeManager, uiManager.scaleManager, false, borderMode = BorderMode.EAST) {

    private val transcriptButton: CIconButton = CIconButton(uiManager.themeManager, uiManager.scaleManager, uiManager.icons.disassembler)
    private val statusIcon: CIconButton = CIconButton(uiManager.themeManager, uiManager.scaleManager, uiManager.icons.statusLoading)
    private val undoButton: CIconButton = CIconButton(uiManager.themeManager, uiManager.scaleManager, uiManager.icons.backwards)
    private val redoButton: CIconButton = CIconButton(uiManager.themeManager, uiManager.scaleManager, uiManager.icons.forwards)
    private val buildButton: CIconButton = CIconButton(uiManager.themeManager, uiManager.scaleManager, uiManager.icons.build)
    private val infoButton: CIconButton = CIconButton(uiManager.themeManager, uiManager.scaleManager, uiManager.icons.info)
    private val deleteButton: CIconButton = CIconButton(uiManager.themeManager, uiManager.scaleManager, uiManager.icons.deleteBlack)

    init {
        // Apply layout
        layout = BoxLayout(this, BoxLayout.Y_AXIS)
        transcriptButton.alignmentX = CENTER_ALIGNMENT
        statusIcon.alignmentX = CENTER_ALIGNMENT
        undoButton.alignmentX = CENTER_ALIGNMENT
        redoButton.alignmentX = CENTER_ALIGNMENT
        buildButton.alignmentX = CENTER_ALIGNMENT
        infoButton.alignmentX = CENTER_ALIGNMENT
        deleteButton.alignmentX = CENTER_ALIGNMENT

        // Add Components
        add(transcriptButton)
        add(statusIcon)
        add(undoButton)
        add(redoButton)
        add(buildButton)
        add(infoButton)
        add(deleteButton)

        // Listeners
        uiManager.scaleManager.addScaleChangeEvent {
            val insets = it.borderScale.insets
            border = BorderFactory.createEmptyBorder(insets, insets, insets, insets)
        }
        uiManager.themeManager.addThemeChangeListener {
            background = it.globalLaF.bgSecondary
        }

        // Functions
        installStatusButton(uiManager)
        installBuildButton(editor)

        // Set Defaults
        val insets = uiManager.currScale().borderScale.insets
        border = BorderFactory.createEmptyBorder(insets, insets, insets, insets)
        background = uiManager.currTheme().globalLaF.bgSecondary

        statusIcon.isDeactivated = true
        statusIcon.rotating = true
    }

    private fun installStatusButton(uiManager: UIManager) {
        uiManager.editor.addFileEditEvent {
            statusIcon.svgIcon = uiManager.icons.statusLoading
            statusIcon.rotating = true
        }

        uiManager.eventManager.addCompileListener { success ->
            if (success) {
                statusIcon.svgIcon = uiManager.icons.statusFine
                statusIcon.rotating = false
            } else {
                statusIcon.svgIcon = uiManager.icons.statusError
                statusIcon.rotating = false
            }
        }
    }

    private fun installBuildButton(codeEditor: CodeEditor) {
        buildButton.addActionListener {
            codeEditor.compileCurrent(build = true)
        }
        undoButton.addActionListener {
            codeEditor.getCurrentEditPanel()?.undo()
        }
        redoButton.addActionListener {
            codeEditor.getCurrentEditPanel()?.redo()
        }
    }

}