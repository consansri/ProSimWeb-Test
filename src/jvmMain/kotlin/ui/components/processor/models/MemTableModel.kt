package me.c3.ui.components.processor.models

import emulator.kit.common.Memory
import me.c3.ui.UIManager
import javax.swing.event.TableModelListener
import javax.swing.table.AbstractTableModel

class MemTableModel(private val uiManager: UIManager) : AbstractTableModel() {

    var entrysInRow = 0
    val rowAddresses = mutableListOf<String>()
    val ascii = mutableListOf<String>()

    init {
        updateContent()
        addContentChangeListener()
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
        rowAddresses.clear()
        entrysInRow = uiManager.currArch().getMemory().getEntrysInRow()
        val copyOfMemList = ArrayList(uiManager.currArch().getMemory().memList)
        copyOfMemList.forEach {
            if (!rowAddresses.contains(it.row.getRawHexStr())) {
                rowAddresses.add(it.row.getRawHexStr())
            }
        }
        rowAddresses.sort()
        for (index in rowAddresses.indices) {
            ascii[index] = copyOfMemList.filter { it.row.getRawHexStr() == rowAddresses[index] }.sortedBy { it.offset }.joinToString(" ") { it.variable.get().toASCII() }
        }
        this.fireTableStructureChanged()
        this.fireTableDataChanged()
    }

    override fun getRowCount(): Int {
        return rowAddresses.size
    }

    override fun getColumnCount(): Int {
        return entrysInRow + 2
    }

    override fun getValueAt(rowIndex: Int, columnIndex: Int): Any {
        return when {
            columnIndex == 0 -> {
                rowAddresses[rowIndex]
            }

            columnIndex == entrysInRow + 1 -> {
                ascii[rowIndex]
            }

            else -> {
                uiManager.currArch().getMemory().memList.firstOrNull {
                    it.row.getRawHexStr() == rowAddresses[rowIndex] && it.offset == columnIndex - 1
                } ?: uiManager.currArch().getMemory().getInitialBinary()
            }
        }
    }
}