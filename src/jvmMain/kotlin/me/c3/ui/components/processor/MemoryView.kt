package me.c3.ui.components.processor

import emulator.kit.MicroSetup
import emulator.kit.memory.Cache
import emulator.kit.memory.MainMemory
import me.c3.ui.Events
import me.c3.ui.States
import me.c3.ui.components.processor.memory.MainMemView
import me.c3.ui.components.processor.memory.NewCacheView
import me.c3.uilib.styled.CAdvancedTabPane
import me.c3.uilib.styled.CLabel
import me.c3.uilib.styled.params.FontType
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