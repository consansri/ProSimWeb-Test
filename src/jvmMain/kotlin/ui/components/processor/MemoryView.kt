package me.c3.ui.components.processor

import emulator.kit.nativeLog
import emulator.kit.nativeWarn
import emulator.kit.types.Variable
import me.c3.ui.UIManager
import me.c3.ui.components.styled.CPanel
import me.c3.ui.components.styled.CScrollPane
import me.c3.ui.styled.CTable
import java.awt.BorderLayout
import javax.swing.event.TableModelEvent
import javax.swing.table.DefaultTableModel

class MemoryView(private val uiManager: UIManager) : CPanel(uiManager, primary = false) {

    val tableModel = DefaultTableModel()
    val table = CTable(uiManager, tableModel)
    val scrollPane = CScrollPane(uiManager, primary = false, table)
    val addrTitle = "ADDR"
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
        tableModel.addTableModelListener { e ->
            if (e.type == TableModelEvent.UPDATE && !currentlyUpdating) {
                val row = e.firstRow
                val col = e.column
                try {
                    val rowAddr = tableModel.getValueAt(row, 0)
                    val newValue = tableModel.getValueAt(row, col)
                    val offset = col - 1
                    val memInstance = uiManager.currArch().getMemory().memList.firstOrNull {
                        it.row.getRawHexStr() == rowAddr.toString() && it.offset == offset
                    }
                    memInstance?.let { memInst ->
                        val hex = Variable.Value.Hex(newValue.toString(), uiManager.currArch().getMemory().getWordSize())
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
        uiManager.archManager.addArchChangeListener {
            updateContent()
        }

        uiManager.eventManager.addExeEventListener {
            updateContent()
        }

        uiManager.eventManager.addCompileListener {
            updateContent()
        }
    }

    private fun updateContent() {
        currentlyUpdating = true
        tableModel.rowCount = 0
        val rowAddresses = mutableListOf<String>()
        val entrysInRow = uiManager.currArch().getMemory().getEntrysInRow()
        val copyOfMemList = ArrayList(uiManager.currArch().getMemory().memList)
        copyOfMemList.forEach {
            if (!rowAddresses.contains(it.row.getRawHexStr())) {
                rowAddresses.add(it.row.getRawHexStr())
            }
        }
        rowAddresses.sort()

        val columnIdentifiers: Array<String> = arrayOf(addrTitle, *Array(entrysInRow) { it.toString() }, asciiTitle)
        tableModel.setColumnIdentifiers(columnIdentifiers)

        for (index in rowAddresses.indices) {
            val ascii = copyOfMemList.filter { it.row.getRawHexStr() == rowAddresses[index] }.sortedBy { it.offset }.joinToString(" ") { it.variable.get().toASCII() }
            val contentArray: Array<Any> = Array(entrysInRow) { uiManager.currArch().getMemory().getInitialBinary().get().toHex().getRawHexStr() }
            copyOfMemList.filter { it.row.getRawHexStr() == rowAddresses[index] }.sortedBy { it.offset }.forEach {
                contentArray[it.offset] = it
            }
            tableModel.addRow(arrayOf(rowAddresses[index], *contentArray.map { it }.toTypedArray(), ascii))
        }

        updateColumnWidths(entrysInRow)
        currentlyUpdating = false
    }

    private fun updateColumnWidths(entrysInRow: Int) {
        val charWidth = getFontMetrics(uiManager.currTheme().codeLaF.getFont().deriveFont(uiManager.currScale().fontScale.dataSize)).charWidth('0')
        val wordSize = uiManager.currArch().getMemory().getWordSize()
        val addrScale = uiManager.currArch().getMemory().getAddressSize()
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