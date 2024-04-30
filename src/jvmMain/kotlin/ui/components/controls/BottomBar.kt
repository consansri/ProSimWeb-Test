package me.c3.ui.components.controls

import emulator.kit.assembler.CodeStyle
import kotlinx.coroutines.*
import me.c3.ui.MainManager
import me.c3.ui.components.styled.CLabel
import me.c3.ui.components.styled.CPanel
import me.c3.ui.styled.params.BorderMode
import me.c3.ui.styled.params.FontType
import java.awt.GridBagConstraints
import java.awt.GridBagLayout

class BottomBar(private val mainManager: MainManager) : CPanel(mainManager.themeManager, mainManager.scaleManager, borderMode = BorderMode.NORTH) {

    val tagInfo = CLabel(mainManager.themeManager, mainManager.scaleManager, "Back to work? :D", FontType.BASIC)
    val editorInfo = CLabel(mainManager.themeManager, mainManager.scaleManager, "", FontType.BASIC)
    val compilerInfo = CLabel(mainManager.themeManager, mainManager.scaleManager, "", FontType.BASIC)
    val generalPurpose = CLabel(mainManager.themeManager, mainManager.scaleManager, "", FontType.BASIC)

    private var compilerObservingProcess: Job? = null
    private var compilerObserverScope = CoroutineScope(Dispatchers.Default)

    init {
        attachComponents()

        mainManager.archManager.addArchChangeListener {
            resetCompilerProcessPrinter()
        }

        resetCompilerProcessPrinter()
    }

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

    fun setError(text: String) {
        generalPurpose.setColouredText(text, mainManager.currTheme().codeLaF.getColor(CodeStyle.RED))
    }

    fun setWarning(text: String) {
        generalPurpose.setColouredText(text, mainManager.currTheme().codeLaF.getColor(CodeStyle.YELLOW))
    }

    fun setInfo(text: String) {
        generalPurpose.setColouredText(text, mainManager.currTheme().textLaF.baseSecondary)
    }

    private fun resetCompilerProcessPrinter() {
        compilerObservingProcess?.cancel()

        compilerObservingProcess = compilerObserverScope.launch {
            while (this.isActive) {
                val processes = ArrayList(mainManager.currArch().getCompiler().runningProcesses())
                val stateString = processes.joinToString(" -> ") {
                    it.toString()
                }
                compilerInfo.text = stateString
                delay(1000)
            }
        }
    }
}