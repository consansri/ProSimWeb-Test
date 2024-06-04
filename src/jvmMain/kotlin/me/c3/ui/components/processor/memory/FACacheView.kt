package me.c3.ui.components.processor.memory

import emulator.kit.assembler.CodeStyle
import emulator.kit.common.memory.FACache
import me.c3.ui.Events
import me.c3.ui.States
import me.c3.ui.components.processor.models.CacheTableModel
import me.c3.ui.styled.CPanel
import me.c3.ui.styled.CScrollPane
import me.c3.ui.styled.CTable
import java.awt.BorderLayout
import javax.swing.SwingUtilities

class FACacheView(val cache: FACache): CPanel( primary = false) {

    val tableModel = CacheTableModel()
    val table = CTable( tableModel, false)
    val scrollPane = CScrollPane( primary = false, table)
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
        States.arch.addEvent {
            updateContent()
        }

        Events.exe.addListener {
            updateContent()
        }

        Events.compile.addListener {
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

            for (index in cache.blocks.indices) {
                val row = cache.blocks[index].get() as? FACache.FARow ?: continue

                val ascii = row.data.joinToString("") {
                    it.value.toASCII()
                }

                row.data.forEachIndexed { i, value ->
                    if (States.arch.get().regContainer.pc.get().toHex().getRawHexStr() == value.address?.getRawHexStr()) {
                        table.setCellHighlighting(index, i + 4, States.theme.get().codeLaF.getColor(CodeStyle.GREENPC))
                    }
                }

                tableModel.addRow(arrayOf(index, if (row.valid) "1" else "0", if (row.dirty) "1" else "0", row.tag?.toHex()?.toRawString() ?: "invalid", *row.data.map { it }.toTypedArray(), ascii))
            }

            updateColumnWidths(offsets)
            currentlyUpdating = false
        }
    }

    private fun updateColumnWidths(offsets: Int) {
        val wordSize = cache.instanceSize

        val rowIndexScale = cache.blockSize.toString(16).length + 1
        val oneBitScale = 2
        val tagScale = cache.tagBits / 4 + 1
        val asciiScale = offsets * wordSize.getByteCount()
        val divider = offsets * wordSize.hexChars + 2 * oneBitScale + tagScale + asciiScale + rowIndexScale

        val oneCharSpace = table.width / divider

        val oneBitWidth = oneBitScale * oneCharSpace
        val rowIndexWidth = rowIndexScale * oneCharSpace
        val tagWidth = tagScale * oneCharSpace

        val inBetweenWidth = wordSize.hexChars * oneCharSpace
        val asciiWidth = asciiScale * oneCharSpace

        for (colIndex in 0 until table.columnCount) {
            table.columnModel.getColumn(colIndex).preferredWidth = when (colIndex) {
                0 -> rowIndexWidth
                1, 2 -> oneBitWidth
                3 -> tagWidth
                table.columnCount - 1 -> asciiWidth
                else -> inBetweenWidth
            }
            table.columnModel.getColumn(colIndex).minWidth = when (colIndex) {
                0 -> rowIndexWidth
                1, 2 -> oneBitWidth
                3 -> tagWidth
                table.columnCount - 1 -> asciiWidth
                else -> inBetweenWidth
            }
        }
    }

}