package me.c3.ui.components.editor

import emulator.kit.common.ArchState
import me.c3.ui.UIManager
import me.c3.ui.components.styled.IconButton
import javax.swing.BorderFactory
import javax.swing.BoxLayout
import javax.swing.JPanel

class EditorControls(uiManager: UIManager, editor: CodeEditor) : JPanel() {

    val transcriptButton: IconButton
    val statusIcon: IconButton
    val undoButton: IconButton
    val redoButton: IconButton
    val buildButton: IconButton
    val infoButton: IconButton
    val deleteButton: IconButton

    init {
        // Instantiate
        transcriptButton = IconButton(uiManager, uiManager.icons.disassembler)
        statusIcon = IconButton(uiManager, uiManager.icons.statusLoading)
        undoButton = IconButton(uiManager, uiManager.icons.backwards)
        redoButton = IconButton(uiManager, uiManager.icons.forwards)
        buildButton = IconButton(uiManager, uiManager.icons.build)
        infoButton = IconButton(uiManager, uiManager.icons.info)
        deleteButton = IconButton(uiManager, uiManager.icons.deleteBlack)

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
            background = it.globalStyle.bgSecondary
        }

        // Functions
        installStatusButton(uiManager)
        installBuildButton(uiManager, editor)


        // Set Defaults
        val insets = uiManager.currScale().borderScale.insets
        border = BorderFactory.createEmptyBorder(insets, insets, insets, insets)
        background = uiManager.currTheme().globalStyle.bgSecondary

        statusIcon.isDeactivated = true
        statusIcon.rotating = true
    }

    private fun installStatusButton(uiManager: UIManager) {
        uiManager.eventManager.addEditListener {
            when (uiManager.archManager.curr.getState().currentState) {
                ArchState.State.UNCHECKED -> {
                    statusIcon.svgIcon = uiManager.icons.statusLoading
                    statusIcon.rotating = true
                }

                ArchState.State.HASERRORS -> {
                    statusIcon.svgIcon = uiManager.icons.statusError
                    statusIcon.rotating = false
                }

                ArchState.State.EXECUTABLE -> {
                    statusIcon.svgIcon = uiManager.icons.statusFine
                    statusIcon.rotating = false
                }

                ArchState.State.EXECUTION -> {
                    statusIcon.svgIcon = uiManager.icons.statusFine
                    statusIcon.rotating = false
                }
            }
        }

        uiManager.eventManager.addCompileListener {
            when (it.getState().currentState) {
                ArchState.State.UNCHECKED -> {
                    statusIcon.svgIcon = uiManager.icons.statusLoading
                    statusIcon.rotating = true
                }

                ArchState.State.HASERRORS -> {
                    statusIcon.svgIcon = uiManager.icons.statusError
                    statusIcon.rotating = false
                }

                ArchState.State.EXECUTABLE -> {
                    statusIcon.svgIcon = uiManager.icons.statusFine
                    statusIcon.rotating = false
                }

                ArchState.State.EXECUTION -> {
                    statusIcon.svgIcon = uiManager.icons.statusFine
                    statusIcon.rotating = false
                }
            }
        }
    }

    private fun installBuildButton(uiManager: UIManager, codeEditor: CodeEditor) {
        buildButton.addActionListener {
            codeEditor.compileCurrent(uiManager, build = true)
        }
    }

}