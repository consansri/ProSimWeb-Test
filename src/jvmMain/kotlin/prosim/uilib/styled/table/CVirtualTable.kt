package prosim.uilib.styled.table

import emulator.kit.nativeLog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import prosim.uilib.styled.CLabel
import prosim.uilib.styled.params.FontType
import java.awt.Color
import java.awt.Graphics
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.JComponent
import javax.swing.SwingConstants
import javax.swing.SwingUtilities
import kotlin.time.measureTime

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
    val content: List<CCellRenderer>
    val headers: List<CHeaderRenderer>

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
        isOpaque = false

        layout = GridBagLayout()
        val offsetX = if (rowHeaders != null) 1 else 0
        val offsetY = if (colHeaders != null) 1 else 0

        headers = attachTableHeaders(offsetX, offsetY)
        content = attachTableContent(offsetX, offsetY)

        attachWheelListener()
    }

    override fun paint(g: Graphics?) {
        val time = measureTime {
            super.paint(g)
        }
        nativeLog("${this::class.simpleName} paint took ${time.inWholeNanoseconds} ns")
    }

    abstract fun getCellContent(contentRowID: Int, contentColID: Int): String
    abstract fun isEditable(contentRowID: Int, contentColID: Int): Boolean
    abstract fun onEdit(newVal: String, contentRowID: Int, contentColID: Int)
    abstract fun customCellFGColor(contentRowID: Int, contentColID: Int): Color?
    abstract fun customCellBGColor(contentRowID: Int, contentColID: Int): Color?
    abstract fun onCellClick(cell: CCellRenderer, contentRowID: Int, contentColID: Int)
    abstract fun onHeaderClick(header: CHeaderRenderer, headerRowID: Int, headerColID: Int)

    fun updateCellContent() {
        updateJob?.cancel()
        updateJob = scrollScope.launch {
            revalidate()
            repaint()
        }
    }

    private fun attachTableContent(offsetX: Int, offsetY: Int): List<CCellRenderer> {
        val content = mutableListOf<CCellRenderer>()
        val gbc = GridBagConstraints()
        gbc.fill = GridBagConstraints.BOTH

        for (rowID in 0..<visibleRowCount) {
            for (colID in 0..<visibleColCount) {
                gbc.gridx = colID + offsetX
                gbc.gridy = rowID + offsetY
                gbc.weightx = colWeights.getOrNull(colID) ?: defaultWeight
                gbc.weighty = rowWeights.getOrNull(rowID) ?: defaultWeight
                val renderer = createCellRenderer(this, rowID, colID)
                content.add(renderer)
                add(renderer, gbc)
            }
        }

        return content
    }

    private fun attachTableHeaders(offsetX: Int, offsetY: Int): List<CHeaderRenderer> {
        val headers = mutableListOf<CHeaderRenderer>()

        if (colHeaders != null) {
            val gbc = GridBagConstraints()
            gbc.gridy = 0
            gbc.fill = GridBagConstraints.HORIZONTAL

            colHeaders.forEachIndexed { i, text ->
                gbc.gridx = i + offsetX
                gbc.weightx = colWeights.getOrNull(i - offsetX) ?: defaultWeight
                val renderer = createHeaderRenderer(this, 0, i, text)
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
                val renderer = createHeaderRenderer(this, i, 0, text)
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

    fun createHeaderRenderer(table: CVirtualTable, rowID: Int, colID: Int, text: String): CHeaderRenderer {
        return CHeaderRenderer(table, text, table.headerFontType, rowID, colID)
    }

    fun createCellRenderer(table: CVirtualTable, rowID: Int, colID: Int): CCellRenderer {
        return CCellRenderer(table, table.contentfontType, rowID, colID)
    }

    class CHeaderRenderer(val table: CVirtualTable, text: String, fontType: FontType, val rowID: Int, val colID: Int) : CLabel(text, fontType) {
        init {
            horizontalAlignment = SwingConstants.CENTER
            verticalAlignment = SwingConstants.CENTER
            addMouseListener(object : MouseAdapter() {
                override fun mouseClicked(e: MouseEvent) {
                    if (SwingUtilities.isLeftMouseButton(e)) {
                        table.onHeaderClick(this@CHeaderRenderer, rowID, colID)
                    }
                }
            })
        }
    }

    class CCellRenderer(val table: CVirtualTable, fontType: FontType, val rowID: Int, val colID: Int) : CCell(fontType) {

        init {
            addMouseListener(object : MouseAdapter() {
                override fun mouseClicked(e: MouseEvent) {
                    if (SwingUtilities.isLeftMouseButton(e)) {
                        table.onCellClick(this@CCellRenderer, rowID + table.vScrollOffset, colID + table.hScrollOffset)
                    }
                }
            })
        }

        override fun textToDraw(): String {
            val realRowID = rowID + table.vScrollOffset
            val realColID = colID + table.hScrollOffset
            customFG = table.customCellFGColor(realRowID, realColID)
            customBG = table.customCellBGColor(realRowID, realColID)
            return table.getCellContent(realRowID, realColID)
        }
    }

}