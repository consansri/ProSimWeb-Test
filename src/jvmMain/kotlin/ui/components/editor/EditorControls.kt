package me.c3.ui.components.editor

import emulator.kit.common.ArchState
import me.c3.ui.UIManager
import me.c3.ui.components.styled.CIconButton
import me.c3.ui.components.styled.CPanel
import javax.swing.BorderFactory
import javax.swing.BoxLayout

class EditorControls(uiManager: UIManager, editor: CodeEditor) : CPanel(uiManager, false) {

    private val transcriptButton: CIconButton
    private val statusIcon: CIconButton
    private val undoButton: CIconButton
    private val redoButton: CIconButton
    private val buildButton: CIconButton
    private val infoButton: CIconButton
    private val deleteButton: CIconButton

    init {
        // Instantiate
        transcriptButton = CIconButton(uiManager, uiManager.icons.disassembler)
        statusIcon = CIconButton(uiManager, uiManager.icons.statusLoading)
        undoButton = CIconButton(uiManager, uiManager.icons.backwards)
        redoButton = CIconButton(uiManager, uiManager.icons.forwards)
        buildButton = CIconButton(uiManager, uiManager.icons.build)
        infoButton = CIconButton(uiManager, uiManager.icons.info)
        deleteButton = CIconButton(uiManager, uiManager.icons.deleteBlack)

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
        installBuildButton(uiManager, editor)


        // Set Defaults
        val insets = uiManager.currScale().borderScale.insets
        border = BorderFactory.createEmptyBorder(insets, insets, insets, insets)
        background = uiManager.currTheme().globalLaF.bgSecondary

        statusIcon.isDeactivated = true
        statusIcon.rotating = true
    }

    private fun installStatusButton(uiManager: UIManager) {
        /*uiManager.eventManager.addEditListener {
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
        }*/

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
            //codeEditor.compileCurrent(uiManager, build = true)
        }
    }

}