package me.c3.ui.components.processor.memory

import emulator.kit.assembler.CodeStyle
import emulator.kit.common.memory.Cache
import emulator.kit.common.memory.DirectMappedCache
import emulator.kit.common.memory.MainMemory
import me.c3.ui.MainManager
import me.c3.ui.components.processor.models.MemTableModel
import me.c3.ui.styled.CPanel
import me.c3.ui.styled.CScrollPane
import me.c3.ui.styled.CTable
import java.awt.BorderLayout
import javax.swing.SwingUtilities

class DMCacheView(private val mm: MainManager, val cache: DirectMappedCache) : CPanel(mm.tm, mm.sm, primary = false) {

    val tableModel = MemTableModel()
    val table = CTable(mm.tm, mm.sm, tableModel, false)
    val scrollPane = CScrollPane(mm.tm, mm.sm, primary = false, table)
    val asciiTitle = "ASCII"
    var currentlyUpdating = false

    init {
        layout = BorderLayout()
        add(scrollPane, BorderLayout.CENTER)
        this.maximumSize = table.maximumSize
        updateContent()
        addContentChangeListener()
        attachTableModelListener()
    }

    private fun attachTableModelListener() {
        // not editable
    }

    private fun addContentChangeListener() {
        mm.archManager.addArchChangeListener {
            updateContent()
        }

        mm.eventManager.addExeEventListener {
            updateContent()
        }

        mm.eventManager.addCompileListener {
            updateContent()
        }
    }

    private fun updateContent() {
        SwingUtilities.invokeLater {
            currentlyUpdating = true
            tableModel.rowCount = 0

            val offsets = cache.offsets

            val columnIdentifiers: Array<String> = arrayOf("i", "v", "d", "tag", *Array(offsets) { it.toString() }, asciiTitle)
            tableModel.setColumnIdentifiers(columnIdentifiers)

            table.resetCellHighlighting()

            for (index in 0..<cache.rows) {
                val row = cache.block.data[index] as? DirectMappedCache.DMRow ?: continue

                val ascii = row.data.joinToString("") {
                    it.value.toASCII()
                }

                tableModel.addRow(arrayOf(index, if(row.valid) "1" else "0", if(row.dirty) "1" else "0", row.tag ?: "invalid" ,*row.data.map { it }.toTypedArray(), ascii))
            }

            updateColumnWidths(offsets)
            currentlyUpdating = false
        }
    }

    private fun updateColumnWidths(entrysInRow: Int) {
        val charWidth = getFontMetrics(mm.currTheme().codeLaF.getFont().deriveFont(mm.currScale().fontScale.dataSize)).charWidth('0')
        val wordSize = cache.instanceSize
        val addrScale = cache.addressSize
        val asciiScale = entrysInRow * wordSize.getByteCount()
        val divider = entrysInRow * wordSize.hexChars + asciiScale + addrScale.hexChars

        val oneCharSpace = table.width / divider
        val firstColumnWidth = addrScale.hexChars * oneCharSpace
        val inBetweenWidth = wordSize.hexChars * oneCharSpace
        val lastColumnWidth = asciiScale * oneCharSpace

        for (colIndex in 0 until table.columnCount) {
            table.columnModel.getColumn(colIndex).preferredWidth = when (colIndex) {
                0 -> firstColumnWidth
                table.columnCount - 1 -> lastColumnWidth
                else -> inBetweenWidth
            }
            table.columnModel.getColumn(colIndex).minWidth = when (colIndex) {
                0 -> charWidth * addrScale.hexChars
                table.columnCount - 1 -> asciiScale * charWidth
                else -> wordSize.hexChars * charWidth
            }
        }
    }
}