package me.c3.ui.components.processor

import emulator.kit.MicroSetup
import emulator.kit.assembler.CodeStyle
import emulator.kit.common.memory.DirectMappedCache
import emulator.kit.common.memory.MainMemory
import emulator.kit.nativeWarn
import emulator.kit.types.Variable
import me.c3.ui.MainManager
import me.c3.ui.components.processor.memory.DMCacheView
import me.c3.ui.components.processor.memory.MainMemView
import me.c3.ui.components.processor.models.MemTableModel
import me.c3.ui.styled.*
import me.c3.ui.styled.params.FontType
import java.awt.BorderLayout
import javax.swing.SwingUtilities
import javax.swing.event.TableModelEvent

class MemoryView(private val mm: MainManager) : CAdvancedTabPane(mm.tm, mm.sm, mm.icons, tabsAreCloseable = false) {

    init {
        addContentChangeListener()
    }

    private fun addContentChangeListener() {
        mm.archManager.addArchChangeListener {
            updateContent()
        }
        updateContent()
    }

    private fun updateContent() {
        removeAllTabs()
        MicroSetup.getMemoryInstances().forEach {
            val content = when (it) {
                is DirectMappedCache -> DMCacheView(mm, it)
                is MainMemory -> MainMemView(mm, it)
            }
            addTab(CLabel(mm.tm, mm.sm, it::class.simpleName.toString(), FontType.BASIC), content)
        }
    }
}