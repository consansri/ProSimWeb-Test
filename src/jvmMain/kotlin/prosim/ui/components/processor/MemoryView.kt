package prosim.ui.components.processor

import emulator.kit.MicroSetup
import emulator.kit.memory.Cache
import emulator.kit.memory.MainMemory
import prosim.ui.Events
import prosim.ui.States
import prosim.ui.components.processor.memory.MainMemView
import prosim.ui.components.processor.memory.NewCacheView
import prosim.uilib.styled.CAdvancedTabPane
import prosim.uilib.styled.CLabel
import prosim.uilib.styled.params.FontType
import java.lang.ref.WeakReference

class MemoryView : CAdvancedTabPane(tabsAreCloseable = false) {

    init {
        addContentChangeListener()
    }

    private fun addContentChangeListener() {
        States.arch.addEvent(WeakReference(this)) {
            updateContent()
        }
        Events.archSettingChange.addListener(WeakReference(this)) {
            updateContent()
        }
        updateContent()
    }

    private fun updateContent() {
        removeAllTabs()
        MicroSetup.getMemoryInstances().forEach {
            val content = when (it) {
                is Cache -> NewCacheView(it)
                is MainMemory -> MainMemView(it)
            }
            addTab(CLabel(it.name, FontType.BASIC), content)
        }
    }
}