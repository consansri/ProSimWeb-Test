package me.c3.ui.components.processor

import emulator.kit.MicroSetup
import emulator.kit.common.memory.Cache
import emulator.kit.common.memory.MainMemory
import me.c3.ui.Events
import me.c3.ui.States
import me.c3.ui.components.processor.memory.CacheView
import me.c3.ui.components.processor.memory.MainMemView
import me.c3.ui.styled.CAdvancedTabPane
import me.c3.ui.styled.CLabel
import me.c3.ui.styled.params.FontType

class MemoryView : CAdvancedTabPane( tabsAreCloseable = false) {

    init {
        addContentChangeListener()
    }

    private fun addContentChangeListener() {
        States.arch.addEvent {
            updateContent()
        }
        Events.archSettingChange.addListener {
            updateContent()
        }
        updateContent()
    }

    private fun updateContent() {
        removeAllTabs()
        MicroSetup.getMemoryInstances().forEach {
            val content = when (it) {
                is Cache -> CacheView(it)
                is MainMemory -> MainMemView( it)
            }
            addTab(CLabel( it.name, FontType.BASIC), content)
        }
    }
}