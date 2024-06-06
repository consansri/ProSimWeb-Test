package me.c3.ui.components.processor.memory

import emulator.kit.assembler.CodeStyle
import emulator.kit.common.memory.Cache
import me.c3.ui.Events
import me.c3.ui.States
import me.c3.ui.components.processor.models.CacheTableModel
import me.c3.ui.styled.CPanel
import me.c3.ui.styled.CScrollPane
import me.c3.ui.styled.CTable
import java.awt.BorderLayout
import javax.swing.SwingUtilities

class CacheView(val cache: Cache) : CPanel(primary = false) {

    val tableModel = CacheTableModel()
    val table = CTable(tableModel, false)
    val scrollPane = CScrollPane(primary = false, table)
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

            val offsets = cache.model.offsetCount

            val columnIdentifiers: Array<String> = arrayOf("i", "m", "v", "d", "tag", *Array(offsets) { it.toString(16) }, asciiTitle)
            tableModel.setColumnIdentifiers(columnIdentifiers)

            table.resetCellHighlighting()

            for (index in cache.model.rows.indices) {
                val row = cache.model.rows[index]

                for (blockIndex in row.blocks.indices) {
                    val block = row.blocks[blockIndex]

                    val ascii = block.data.joinToString("") {
                        it.value.toASCII()
                    }

                    block.data.forEachIndexed { i, value ->
                        if (States.arch.get().regContainer.pc.get().toHex().getRawHexStr() == value.address?.getRawHexStr()) {
                            table.setCellHighlighting(index, i + 5, States.theme.get().codeLaF.getColor(CodeStyle.GREENPC))
                        }
                    }

                    tableModel.addRow(
                        arrayOf(
                            index.toString(16),
                            if (row.decider.indexToReplace() == blockIndex) "> ${blockIndex.toString(16)}" else blockIndex.toString(16),
                            if (block.valid) "1" else "0",
                            if (block.dirty) "1" else "0",
                            block.tag?.toHex()?.toRawString() ?: "invalid",
                            *block.data.map { it }.toTypedArray(),
                            ascii
                        )
                    )
                }
            }

            updateColumnWidths(offsets)
            currentlyUpdating = false
        }
    }

    private fun updateColumnWidths(offsets: Int) {
        val wordSize = cache.instanceSize

        val rowIndexScale = cache.model.indexCount.toString(16).length + 1
        val blockIndexScale = cache.model.blockCount.toString(16).length + 3
        val oneBitScale = 2
        val tagScale = cache.model.tagBits / 4 + 1
        val asciiScale = offsets * wordSize.getByteCount()
        val divider = offsets * wordSize.hexChars + 2 * oneBitScale + tagScale + asciiScale + blockIndexScale + rowIndexScale

        val oneCharSpace = table.width / divider

        val oneBitWidth = oneBitScale * oneCharSpace
        val rowIndexWidth = rowIndexScale * oneCharSpace
        val blockIndexWidth = blockIndexScale * oneCharSpace
        val tagWidth = tagScale * oneCharSpace

        val inBetweenWidth = wordSize.hexChars * oneCharSpace
        val asciiWidth = asciiScale * oneCharSpace

        for (colIndex in 0 until table.columnCount) {
            table.columnModel.getColumn(colIndex).preferredWidth = when (colIndex) {
                0 -> rowIndexWidth
                1 -> blockIndexWidth
                2, 3 -> oneBitWidth
                4 -> tagWidth
                table.columnCount - 1 -> asciiWidth
                else -> inBetweenWidth
            }
            table.columnModel.getColumn(colIndex).minWidth = when (colIndex) {
                0 -> rowIndexWidth
                1 -> blockIndexWidth
                2, 3 -> oneBitWidth
                4 -> tagWidth
                table.columnCount - 1 -> asciiWidth
                else -> inBetweenWidth
            }
        }
    }

}