package me.c3.ui.components.processor.memory

import emulator.kit.assembler.CodeStyle
import emulator.kit.memory.Cache
import me.c3.ui.Events
import me.c3.ui.States
import me.c3.ui.components.processor.models.CacheTableModel
import me.c3.uilib.styled.CPanel
import me.c3.uilib.styled.CTable
import me.c3.uilib.styled.CTextButton
import me.c3.uilib.styled.params.FontType
import java.awt.BorderLayout
import java.lang.ref.WeakReference
import javax.swing.SwingUtilities

class CacheView(val cache: Cache) : CPanel(primary = false) {

    val tableModel = CacheTableModel()
    val table = CTable(tableModel, false)
    val wbAll = CTextButton("write back all", FontType.CODE).apply {
        this.addActionListener {
            cache.writeBackAll()
        }
    }
    val asciiTitle = "ASCII"


    init {
        layout = BorderLayout()
        add(table.tableHeader, BorderLayout.NORTH)
        add(table, BorderLayout.CENTER)
        add(wbAll, BorderLayout.SOUTH)
        this.maximumSize = table.maximumSize
        updateContent()
        addContentChangeListener()
        attachTableModelListener()
    }

    private fun attachTableModelListener() {
        // not editable
    }

    private fun addContentChangeListener() {
        States.arch.addEvent(WeakReference(this)) {
            updateContent()
        }

        Events.exe.addListener(WeakReference(this)) {
            updateContent()
        }

        Events.compile.addListener(WeakReference(this)) {
            updateContent()
        }
    }

    private fun updateContent() {
        SwingUtilities.invokeLater {
            tableModel.rowCount = 0

            val offsets = cache.model.offsetCount

            val columnIdentifiers: Array<String> = arrayOf("i", "m", "v", "d", "tag", *Array(offsets) { it.toString(16) }, asciiTitle)
            tableModel.setColumnIdentifiers(columnIdentifiers)

            table.resetCellHighlighting()

            cache.model.rows.forEachIndexed { rowID, row ->
                row.blocks.forEachIndexed { blockID, block ->
                    val ascii = block.data.joinToString("") {
                        it.value.toASCII()
                    }

                    if (!block.valid) {
                        table.addCellHighlighting(States.theme.get().codeLaF.getColor(CodeStyle.error), rowID, 2)
                    }

                    if (block.dirty) {
                        table.addCellHighlighting(States.theme.get().codeLaF.getColor(CodeStyle.YELLOW), rowID, 3)
                    }

                    block.data.forEachIndexed { i, value ->
                        if (States.arch.get().regContainer.pc.get().toHex().getRawHexStr() == value.address?.getRawHexStr()) {
                            table.addCellHighlighting(States.theme.get().codeLaF.getColor(CodeStyle.GREENPC), rowID, i + 5)
                        }
                    }

                    tableModel.addRow(
                        arrayOf(
                            if (blockID == 0) rowID.toString(16) else "",
                            if (row.decider.indexToReplace() == blockID) "> ${blockID.toString(16)}" else blockID.toString(16),
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