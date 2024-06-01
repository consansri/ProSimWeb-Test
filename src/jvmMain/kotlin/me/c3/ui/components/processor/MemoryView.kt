package me.c3.ui.components.processor

import emulator.kit.MicroSetup
import emulator.kit.common.memory.DirectMappedCache
import emulator.kit.common.memory.MainMemory
import me.c3.ui.manager.MainManager
import me.c3.ui.components.processor.memory.DMCacheView
import me.c3.ui.components.processor.memory.MainMemView
import me.c3.ui.manager.ArchManager
import me.c3.ui.manager.ResManager
import me.c3.ui.styled.*
import me.c3.ui.styled.params.FontType

class MemoryView : CAdvancedTabPane( ResManager.icons, tabsAreCloseable = false) {

    init {
        addContentChangeListener()
    }

    private fun addContentChangeListener() {
        ArchManager.addArchChangeListener {
            updateContent()
        }
        updateContent()
    }

    private fun updateContent() {
        removeAllTabs()
        MicroSetup.getMemoryInstances().forEach {
            val content = when (it) {
                is DirectMappedCache -> DMCacheView( it)
                is MainMemory -> MainMemView( it)
            }
            addTab(CLabel( it::class.simpleName.toString(), FontType.BASIC), content)
        }
    }
}