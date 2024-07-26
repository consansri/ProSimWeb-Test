package prosim.ide.editor.code

import cengine.editor.EditorModification
import emulator.kit.assembler.CodeStyle
import prosim.ide.editor.toColor
import prosim.uilib.UIStates
import prosim.uilib.alpha
import prosim.uilib.styled.COverlay
import prosim.uilib.styled.CScrollPane
import prosim.uilib.styled.params.FontType
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Component
import java.awt.event.KeyAdapter
import java.awt.event.KeyEvent
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.*

class ModificationOverlay<T : EditorModification>(val editor: PerformantCodeEditor) : COverlay() {
    private val listModel = DefaultListModel<T>()
    private val list = JList(listModel)
    private val scrollPane = CScrollPane(true, list)

    companion object {
        const val MAX_VISIBLE_ITEMS = 5
    }

    init {
        layout = BorderLayout()

        list.selectionMode = ListSelectionModel.SINGLE_SELECTION
        list.cellRenderer = OverlayItemRenderer()

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

        val oneItemHeight = list.cellRenderer.getListCellRendererComponent(list, items.firstOrNull(), 0, false, false).preferredSize.height

        val height = items.size.coerceAtMost(MAX_VISIBLE_ITEMS) * oneItemHeight

        showAtLocation(x, y, width, height, parentComponent)
        list.requestFocusInWindow()
    }

    private fun selectCurrentItem() {
        val selectedValue = list.selectedValue
        if (selectedValue != null) {
            selectedValue.execute(editor)
            makeInvisible()
            editor.invalidateContent()
        }
    }

    private inner class OverlayItemRenderer : DefaultListCellRenderer() {
        val selectedBGColor: Color get() = UIStates.theme.get().getColor(CodeStyle.BLUE).alpha(0x33)
        override fun getListCellRendererComponent(list: JList<*>?, value: Any?, index: Int, isSelected: Boolean, cellHasFocus: Boolean): Component {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus)
            foreground = (value as? EditorModification)?.severity?.toColor(editor.psiManager?.lang).toColor(UIStates.theme.get().getColor(CodeStyle.BASE0))
            background = if (cellHasFocus) selectedBGColor else UIStates.theme.get().COLOR_BG_0
            border = BorderFactory.createEmptyBorder()
            roundedCorners = true
            font = FontType.CODE.getFont()
            text = (value as? EditorModification)?.displayText ?: value.toString()
            return this
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