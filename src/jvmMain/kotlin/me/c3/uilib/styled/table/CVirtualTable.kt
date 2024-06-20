package me.c3.uilib.styled.table

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import me.c3.uilib.styled.params.FontType
import java.awt.Color
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import javax.swing.JComponent

abstract class CVirtualTable(
    val contentfontType: FontType,
    val headerFontType: FontType,
    val rowCount: Int,
    val colCount: Int,
    val visibleRowCount: Int,
    val visibleColCount: Int,
    val colHeaders: Array<String>?,
    val rowHeaders: Array<String>?,
    val defaultWeight: Double = 0.0,
    val colWeights: Array<Double> = arrayOf(),
    val rowWeights: Array<Double> = arrayOf()
) : JComponent() {

    val content: List<CVirtualTableUI.CCellRenderer>
    val headers: List<CVirtualTableUI.CHeaderRenderer>

    val scrollScope = CoroutineScope(Dispatchers.Main)
    var updateJob: Job? = null

    var vScrollOffset: Int = 0
        set(value) {
            field = value
            updateCellContent()
        }

    var hScrollOffset: Int = 0
        set(value) {
            field = value
            updateCellContent()
        }


    init {
        this.setUI(CVirtualTableUI())

        val vTableUI = (ui as? CVirtualTableUI)
        layout = GridBagLayout()
        val offsetX = if (rowHeaders != null) 1 else 0
        val offsetY = if (colHeaders != null) 1 else 0

        if (vTableUI != null) {
            headers = attachTableHeaders(vTableUI, offsetX, offsetY)
            content = attachTableContent(vTableUI, offsetX, offsetY)
        } else {
            headers = listOf()
            content = listOf()
        }

        attachWheelListener()
    }

    abstract fun getCellContent(contentRowID: Int, contentColID: Int): String
    abstract fun isEditable(contentRowID: Int, contentColID: Int): Boolean
    abstract fun onEdit(newVal: String, contentRowID: Int, contentColID: Int)
    abstract fun customCellFGColor(contentRowID: Int, contentColID: Int): Color?
    abstract fun customCellBGColor(contentRowID: Int, contentColID: Int): Color?
    abstract fun onCellClick(cell: CVirtualTableUI.CCellRenderer, contentRowID: Int, contentColID: Int)
    abstract fun onHeaderClick(header: CVirtualTableUI.CHeaderRenderer, headerRowID: Int, headerColID: Int)

    fun updateCellContent() {
        updateJob?.cancel()
        updateJob = scrollScope.launch {
            revalidate()
            repaint()
        }
    }

    private fun attachTableContent(vTableUI: CVirtualTableUI, offsetX: Int, offsetY: Int): List<CVirtualTableUI.CCellRenderer> {
        val content = mutableListOf<CVirtualTableUI.CCellRenderer>()
        val gbc = GridBagConstraints()
        gbc.fill = GridBagConstraints.BOTH

        for (rowID in 0..<visibleRowCount) {
            for (colID in 0..<visibleColCount) {
                gbc.gridx = colID + offsetX
                gbc.gridy = rowID + offsetY
                gbc.weightx = colWeights.getOrNull(colID) ?: defaultWeight
                gbc.weighty = rowWeights.getOrNull(rowID) ?: defaultWeight
                val renderer = vTableUI.createCellRenderer(this, rowID, colID)
                content.add(renderer)
                add(renderer, gbc)
            }
        }

        return content
    }

    private fun attachTableHeaders(vTableUI: CVirtualTableUI, offsetX: Int, offsetY: Int): List<CVirtualTableUI.CHeaderRenderer> {
        val headers = mutableListOf<CVirtualTableUI.CHeaderRenderer>()

        if (colHeaders != null) {
            val gbc = GridBagConstraints()
            gbc.gridy = 0
            gbc.fill = GridBagConstraints.HORIZONTAL

            colHeaders.forEachIndexed { i, text ->
                gbc.gridx = i + offsetX
                gbc.weightx = colWeights.getOrNull(i - offsetX) ?: defaultWeight
                val renderer = vTableUI.createHeaderRenderer(this, 0, i, text)
                headers.add(renderer)
                add(renderer, gbc)
            }
        }

        if (rowHeaders != null) {
            val gbc = GridBagConstraints()
            gbc.gridx = 0
            gbc.fill = GridBagConstraints.VERTICAL

            rowHeaders.forEachIndexed { i, text ->
                gbc.gridy = i + offsetY
                gbc.weighty = rowWeights.getOrNull(i - offsetY) ?: defaultWeight
                val renderer = vTableUI.createHeaderRenderer(this, i, 0, text)
                headers.add(renderer)
                add(renderer, gbc)
            }
        }

        return headers
    }

    private fun attachWheelListener() {
        addMouseWheelListener {
            if (it.isShiftDown) {
                if (it.wheelRotation < 0) {
                    scrollLeft(-it.wheelRotation)
                } else {
                    scrollRight(it.wheelRotation)
                }
            } else {
                if (it.wheelRotation < 0) {
                    scrollUp(-it.wheelRotation)
                } else {
                    scrollDown(it.wheelRotation)
                }
            }
        }
    }

    private fun scrollUp(units: Int) {
        if (vScrollOffset - units * visibleRowCount >= 0) {
            vScrollOffset -= units * visibleRowCount
        }
    }

    private fun scrollDown(units: Int) {
        if (vScrollOffset + units * visibleRowCount < rowCount) {
            vScrollOffset += units * visibleRowCount
        }
    }

    private fun scrollLeft(units: Int) {
        if (hScrollOffset - units * visibleColCount >= 0) {
            hScrollOffset -= units * visibleColCount
        }
    }

    private fun scrollRight(units: Int) {
        if (hScrollOffset + units * visibleColCount < colCount) {
            hScrollOffset += units * visibleColCount
        }
    }
}