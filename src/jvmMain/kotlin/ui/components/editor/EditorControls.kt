package me.c3.ui.components.editor

import me.c3.ui.MainManager
import me.c3.ui.components.styled.CIconButton
import me.c3.ui.components.styled.CPanel
import me.c3.ui.styled.params.BorderMode
import javax.swing.BorderFactory
import javax.swing.BoxLayout

class EditorControls(mainManager: MainManager, private val editor: CodeEditor) : CPanel(mainManager.themeManager, mainManager.scaleManager, false, borderMode = BorderMode.EAST) {

    private val statusIcon: CIconButton = CIconButton(mainManager.themeManager, mainManager.scaleManager, mainManager.icons.statusLoading)
    private val undoButton: CIconButton = CIconButton(mainManager.themeManager, mainManager.scaleManager, mainManager.icons.backwards)
    private val redoButton: CIconButton = CIconButton(mainManager.themeManager, mainManager.scaleManager, mainManager.icons.forwards)
    private val buildButton: CIconButton = CIconButton(mainManager.themeManager, mainManager.scaleManager, mainManager.icons.build)
    private val infoButton: CIconButton = CIconButton(mainManager.themeManager, mainManager.scaleManager, mainManager.icons.info)

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
        mainManager.scaleManager.addScaleChangeEvent {
            val insets = it.borderScale.insets
            border = BorderFactory.createEmptyBorder(insets, insets, insets, insets)
        }
        mainManager.themeManager.addThemeChangeListener {
            background = it.globalLaF.bgSecondary
        }

        // Functions
        installStatusButton(mainManager)
        installBuildButton(editor)

        // Set Defaults
        val insets = mainManager.currScale().borderScale.insets
        border = BorderFactory.createEmptyBorder(insets, insets, insets, insets)
        background = mainManager.currTheme().globalLaF.bgSecondary

        statusIcon.isDeactivated = true
        statusIcon.rotating = true
    }

    private fun installStatusButton(mainManager: MainManager) {
        mainManager.editor.addFileEditEvent {
            statusIcon.svgIcon = mainManager.icons.statusLoading
            statusIcon.rotating = true
        }

        mainManager.eventManager.addCompileListener { success ->
            if (success) {
                statusIcon.svgIcon = mainManager.icons.statusFine
                statusIcon.rotating = false
            } else {
                statusIcon.svgIcon = mainManager.icons.statusError
                statusIcon.rotating = false
            }
        }
    }

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