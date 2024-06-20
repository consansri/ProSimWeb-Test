package me.c3.ui.components.processor.memory

import emulator.core.Value
import emulator.kit.assembler.CodeStyle
import emulator.kit.memory.MainMemory
import emulator.kit.nativeWarn
import me.c3.ui.Events
import me.c3.ui.States
import me.c3.ui.components.processor.models.MemTableModel
import me.c3.uilib.UIStates
import me.c3.uilib.styled.CPanel
import me.c3.uilib.styled.CTable
import java.awt.BorderLayout
import java.lang.ref.WeakReference
import javax.swing.SwingUtilities
import javax.swing.event.TableModelEvent

class MainMemView(val memory: MainMemory) : CPanel(primary = false) {

    val tableModel = MemTableModel()
    val table = CTable(tableModel, false)
    val addrTitle = "ADDR"
    val asciiTitle = "ASCII"
    var currentlyUpdating = false

    init {
        layout = BorderLayout()
        add(table.tableHeader, BorderLayout.NORTH)
        add(table, BorderLayout.CENTER)
        this.maximumSize = table.maximumSize
        updateContent()
        addContentChangeListener()
        attachTableModelListener()
    }

    private fun attachTableModelListener() {
        tableModel.addTableModelListener { e ->
            if (e.type == TableModelEvent.UPDATE && !currentlyUpdating) {
                val row = e.firstRow
                val col = e.column
                try {
                    val rowAddr = tableModel.getValueAt(row, 0)
                    val newValue = tableModel.getValueAt(row, col)
                    val offset = col - 1
                    val memInstance = memory.memList.firstOrNull {
                        it.row.getRawHexStr() == rowAddr.toString() && it.offset == offset
                    }
                    memInstance?.let { memInst ->
                        val hex = Value.Hex(newValue.toString(), memory.instanceSize)
                        if (hex.checkResult.valid) {
                            memInst.variable.set(hex)
                        }
                        currentlyUpdating = true
                        tableModel.setValueAt(memInstance, row, col)
                        currentlyUpdating = false
                    } ?: nativeWarn("Couldn't find MemInstance for rowAddr: $rowAddr and offset: $offset")
                } catch (e: IndexOutOfBoundsException) {
                    nativeWarn("Received Index Out Of Bounds Exception: $e")
                }
            }
        }
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
            currentlyUpdating = true
            tableModel.rowCount = 0
            val rowAddresses = mutableListOf<String>()
            val entrysInRow = memory.entrysInRow
            val copyOfMemList = ArrayList(memory.memList)
            copyOfMemList.forEach {
                if (!rowAddresses.contains(it.row.getRawHexStr())) {
                    rowAddresses.add(it.row.getRawHexStr())
                }
            }
            rowAddresses.sort()

            val columnIdentifiers: Array<String> = arrayOf(addrTitle, *Array(entrysInRow) { it.toString(16) }, asciiTitle)
            tableModel.setColumnIdentifiers(columnIdentifiers)

            table.resetCellHighlighting()

            for (index in rowAddresses.indices) {
                val contentArray: Array<Any> = Array(entrysInRow) { memory.getInitialBinary().get().toHex().toRawString() }
                copyOfMemList.filter { it.row.getRawHexStr() == rowAddresses[index] }.sortedBy { it.offset }.forEach {
                    contentArray[it.offset] = it
                    if (States.arch.get().regContainer.pc.get().toHex().getRawHexStr() == it.address.getRawHexStr()) {
                        table.addCellHighlighting(UIStates.theme.get().codeLaF.getColor(CodeStyle.GREENPC), index, it.offset + 1)
                    }
                }
                val ascii = contentArray.joinToString("") {
                    when (it) {
                        is MainMemory.MemInstance -> it.variable.get().toASCII()
                        else -> "·"
                    }
                }

                tableModel.addRow(arrayOf(rowAddresses[index], *contentArray.map { it }.toTypedArray(), ascii))
            }

            updateColumnWidths(entrysInRow)
            currentlyUpdating = false
        }
    }

    private fun updateColumnWidths(entrysInRow: Int) {
        val charWidth = getFontMetrics(UIStates.theme.get().codeLaF.getFont().deriveFont(UIStates.scale.get().fontScale.dataSize)).charWidth('0')
        val wordSize = memory.instanceSize
        val addrScale = memory.addressSize
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