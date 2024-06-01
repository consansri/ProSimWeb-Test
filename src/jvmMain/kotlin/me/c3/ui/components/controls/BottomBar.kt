package me.c3.ui.components.controls

import emulator.kit.assembler.CodeStyle
import kotlinx.coroutines.*
import me.c3.ui.manager.ArchManager
import me.c3.ui.manager.EventManager
import me.c3.ui.manager.MainManager
import me.c3.ui.manager.ThemeManager
import me.c3.ui.styled.CLabel
import me.c3.ui.styled.CPanel
import me.c3.ui.styled.params.BorderMode
import me.c3.ui.styled.params.FontType
import java.awt.GridBagConstraints
import java.awt.GridBagLayout

/**
 * Represents a panel for displaying information at the bottom.
 * @property mainManager The main manager instance.
 */
class BottomBar() : CPanel( borderMode = BorderMode.NORTH) {

    // Labels for displaying various types of information
    val tagInfo = CLabel( "Back to work? :D", FontType.BASIC)
    val editorInfo = CLabel( "", FontType.BASIC)
    val compilerInfo = CLabel( "", FontType.BASIC)
    val generalPurpose = CLabel( "", FontType.BASIC)

    // Coroutine variables for observing compiler processes
    private var compilerObservingProcess: Job? = null
    private var compilerObserverScope = CoroutineScope(Dispatchers.Default)

    init {
        attachComponents()
        observeArchitectureChange()
        observeCompilation()
        resetCompilerProcessPrinter()
    }

    /**
     * Sets error text and color.
     * @param text The text to display.
     */
    fun setError(text: String) {
        generalPurpose.setColouredText(text, ThemeManager.curr.codeLaF.getColor(CodeStyle.RED))
    }

    /**
     * Sets warning text and color.
     * @param text The text to display.
     */
    fun setWarning(text: String) {
        generalPurpose.setColouredText(text, ThemeManager.curr.codeLaF.getColor(CodeStyle.YELLOW))
    }

    /**
     * Sets informational text and color.
     * @param text The text to display.
     */
    fun setInfo(text: String) {
        generalPurpose.setColouredText(text, ThemeManager.curr.textLaF.baseSecondary)
    }

    /**
     * Attaches the components to the layout.
     */
    private fun attachComponents() {
        layout = GridBagLayout()

        val gbc = GridBagConstraints()
        gbc.gridx = 0
        gbc.gridy = 0
        gbc.weighty = 0.0
        gbc.weightx = 1.0
        gbc.fill = GridBagConstraints.HORIZONTAL

        add(tagInfo, gbc)

        gbc.gridx = 1
        gbc.weightx = 0.0
        gbc.fill = GridBagConstraints.NONE

        add(generalPurpose, gbc)

        gbc.gridx = 2

        add(compilerInfo, gbc)

        gbc.gridx = 3

        add(editorInfo, gbc)
    }

    /**
     * Observes architecture change and resets compiler process printer.
     */
    private fun observeArchitectureChange() {
        ArchManager.addArchChangeListener {
            resetCompilerProcessPrinter()
        }
    }

    /**
     * Observes compilation and updates the display accordingly.
     */
    private fun observeCompilation() {
        EventManager.addCompileListener { result ->
            if (result.success) {
                setInfo(result.shortInfoStr())
            } else {
                setError(result.shortInfoStr())
            }
        }
    }

    /**
     * Resets the compiler process printer coroutine.
     */
    private fun resetCompilerProcessPrinter() {
        compilerObservingProcess?.cancel()

        compilerObservingProcess = compilerObserverScope.launch {
            while (this.isActive) {
                val processes = ArrayList(ArchManager.curr.assembler.runningProcesses())
                val stateString = processes.joinToString(" -> ") {
                    it.toString()
                }
                compilerInfo.text = stateString
                delay(1000)
            }
        }
    }
}