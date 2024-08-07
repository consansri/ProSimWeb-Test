package prosim.ide.editor.code

import cengine.editor.EditorModification
import prosim.ide.editor.toColor
import prosim.uilib.UIStates
import prosim.uilib.styled.CList
import prosim.uilib.styled.COverlay
import prosim.uilib.styled.CScrollPane
import java.awt.BorderLayout
import java.awt.Component
import java.awt.event.KeyAdapter
import java.awt.event.KeyEvent
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.DefaultListModel
import javax.swing.JComponent
import javax.swing.JList
import javax.swing.ListSelectionModel

class ModificationOverlay<T : EditorModification>(val editor: PerformantCodeEditor) : COverlay() {
    private val listModel = DefaultListModel<T>()
    private val list = CList(listModel, true)
    private val scrollPane = CScrollPane(true, list)

    companion object {
        const val MAX_VISIBLE_ITEMS = 5
    }

    init {
        layout = BorderLayout()

        list.selectionMode = ListSelectionModel.SINGLE_SELECTION
        list.cellRenderer = ModificationRenderer()

        list.addMouseListener(object : MouseAdapter() {
            override fun mouseClicked(e: MouseEvent) {
                if (e.clickCount == 2) {
                    selectCurrentItem()
                }
            }
        })

        list.addKeyListener(object : KeyAdapter() {
            override fun keyTyped(e: KeyEvent) {
                val newEvent = KeyEvent(
                    editor,
                    e.id,
                    e.`when`,
                    e.modifiersEx,
                    e.keyCode,
                    e.keyChar,
                    e.keyLocation
                )
                editor.dispatchEvent(newEvent)
                e.consume()
            }

            override fun keyPressed(e: KeyEvent) {
                when (e.keyCode) {
                    KeyEvent.VK_ENTER -> {
                        selectCurrentItem()
                        e.consume()
                    }

                    KeyEvent.VK_ESCAPE -> {
                        makeInvisible()
                        e.consume()
                    }

                    KeyEvent.VK_UP, KeyEvent.VK_DOWN -> {

                    }

                    else -> {
                        //
                        val newEvent = KeyEvent(
                            editor,
                            e.id,
                            e.`when`,
                            e.modifiersEx,
                            e.keyCode,
                            e.keyChar,
                            e.keyLocation
                        )
                        editor.dispatchEvent(newEvent)
                        e.consume()
                    }
                }
            }
        })

        add(scrollPane, BorderLayout.CENTER)
    }

    fun showOverlay(items: List<T>, x: Int, y: Int, parentComponent: JComponent, width: Int? = null) {
        listModel.clear()
        items.forEach { listModel.addElement(it) }

        if (items.isNotEmpty()) {
            list.selectedIndex = 0
        }

        val oneItemHeight = list.cellRenderer.getListCellRendererComponent(list, items.maxByOrNull { it.displayText.length }, 0, false, false).preferredSize.height

        val height = items.size.coerceAtMost(MAX_VISIBLE_ITEMS) * oneItemHeight

        showAtLocation(x, y, width, height, parentComponent)
        list.requestFocusInWindow()
    }

    private inner class ModificationRenderer() : CList.OverlayItemRenderer<T>() {
        override fun getListCellRendererComponent(list: JList<out T>?, value: T, index: Int, isSelected: Boolean, cellHasFocus: Boolean): Component {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus)
            customFG = value.severity?.toColor(editor.lang).toColor(UIStates.theme.get().COLOR_FG_0)
            text = value.displayText
            return this
        }
    }

    private fun selectCurrentItem() {
        val selectedValue = list.selectedValue
        if (selectedValue != null) {
            selectedValue.execute(editor)
            makeInvisible()
            editor.invalidateContent()
        }
    }

    override fun processKeyEvent(e: KeyEvent) {
        if (e.keyCode !in listOf(KeyEvent.VK_ENTER, KeyEvent.VK_ESCAPE, KeyEvent.VK_UP, KeyEvent.VK_DOWN)) {
            val newEvent = KeyEvent(
                editor,
                e.id,
                e.getWhen(),
                e.modifiersEx,
                e.keyCode,
                e.keyChar,
                e.keyLocation
            )
            editor.dispatchEvent(newEvent)
        } else {
            super.processKeyEvent(e)
        }
    }
}