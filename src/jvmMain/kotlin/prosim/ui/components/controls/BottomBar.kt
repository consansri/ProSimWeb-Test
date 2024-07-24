package prosim.ui.components.controls

import emulator.kit.assembler.CodeStyle
import kotlinx.coroutines.*
import prosim.ui.Events
import prosim.ui.States
import prosim.uilib.UIStates


import prosim.uilib.styled.CLabel
import prosim.uilib.styled.CPanel
import prosim.uilib.styled.params.BorderMode
import prosim.uilib.styled.params.FontType
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.lang.management.ManagementFactory
import java.lang.ref.WeakReference
import javax.swing.SwingConstants

/**
 * Represents a panel for displaying information at the bottom.
 * @property mainManager The main manager instance.
 */
class BottomBar() : CPanel(borderMode = BorderMode.NORTH) {

    // Labels for displaying various types of information
    private val wsInfo = CLabel("Back to work? :D", FontType.BASIC).apply {
        horizontalAlignment = SwingConstants.LEFT
    }
    val editorInfo = CLabel("", FontType.BASIC)
    val compilerInfo = CLabel("", FontType.BASIC)
    val generalPurpose = CLabel("", FontType.BASIC)
    val memoryUsage = CLabel("", FontType.BASIC)

    // Coroutine variables for observing compiler processes
    private var compilerObservingProcess: Job? = null
    private var compilerObserverScope = CoroutineScope(Dispatchers.Default)

    init {
        attachComponents()
        observeArchitectureChange()
        observeCompilation()
        resetPrinterInterval()
    }

    /**
     * Sets error text and color.
     * @param text The text to display.
     */
    fun setError(text: String) {
        generalPurpose.setColouredText(text, UIStates.theme.get().getColor(CodeStyle.RED))
    }

    /**
     * Sets warning text and color.
     * @param text The text to display.
     */
    fun setWarning(text: String) {
        generalPurpose.setColouredText(text, UIStates.theme.get().getColor(CodeStyle.YELLOW))
    }

    /**
     * Sets informational text and color.
     * @param text The text to display.
     */
    fun setInfo(text: String) {
        generalPurpose.setColouredText(text, UIStates.theme.get().COLOR_FG_1)
    }

    fun setWSError(text: String) {
        wsInfo.setColouredText(text, UIStates.theme.get().getColor(CodeStyle.RED))
    }

    fun setWSWarning(text: String) {
        wsInfo.setColouredText(text, UIStates.theme.get().getColor(CodeStyle.YELLOW))
    }

    fun setWSInfo(text: String) {
        wsInfo.setColouredText(text, UIStates.theme.get().COLOR_FG_1)
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
        gbc.ipadx = UIStates.scale.get().SIZE_INSET_MEDIUM
        gbc.fill = GridBagConstraints.HORIZONTAL

        add(wsInfo, gbc)

        gbc.gridx = 1
        gbc.weightx = 0.0
        gbc.fill = GridBagConstraints.NONE

        add(generalPurpose, gbc)

        gbc.gridx = 2

        add(compilerInfo, gbc)

        gbc.gridx = 3

        add(editorInfo, gbc)

        gbc.gridx = 4
        add(memoryUsage, gbc)
    }

    /**
     * Observes architecture change and resets compiler process printer.
     */
    private fun observeArchitectureChange() {
        States.arch.addEvent(WeakReference(this)) {
            resetPrinterInterval()
        }
    }

    /**
     * Observes compilation and updates the display accordingly.
     */
    private fun observeCompilation() {
        Events.compile.addListener(WeakReference(this)) { result ->
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
    private fun resetPrinterInterval() {
        compilerObservingProcess?.cancel()

        compilerObservingProcess = compilerObserverScope.launch {
            while (this.isActive) {
                printAktiveProcesses()
                printMemoryUsage()
                delay(1000)
            }
        }
    }

    private fun printAktiveProcesses() {
        val processes = ArrayList(States.arch.get().assembler.runningProcesses())
        val stateString = processes.joinToString(" -> ") {
            it.toString()
        }
        compilerInfo.text = stateString
    }

    private fun printMemoryUsage() {
        val memoryMXBean = ManagementFactory.getMemoryMXBean()
        val usedHeap = memoryMXBean.heapMemoryUsage.used / (1024 * 1024)
        val usedNonHeap = memoryMXBean.nonHeapMemoryUsage.used / (1024 * 1024)
        val maxHeap = memoryMXBean.heapMemoryUsage.max / (1024 * 1024)
        val maxNonHeap = memoryMXBean.nonHeapMemoryUsage.max / (1024 * 1024)

        memoryUsage.text = "$usedHeap MB / $maxHeap MB ${if (usedNonHeap != 0L) "(extern $usedNonHeap MB)" else ""}"
    }

}