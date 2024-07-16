package prosim.uilib.styled.editor3

import cengine.editor.CodeEditor
import cengine.editor.EditorModification
import emulator.kit.assembler.CodeStyle
import prosim.uilib.UIStates
import prosim.uilib.styled.COverlay
import prosim.uilib.styled.CScrollPane
import prosim.uilib.styled.params.FontType
import java.awt.BorderLayout
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

    init {
        layout = BorderLayout()

        list.selectionMode = ListSelectionModel.SINGLE_SELECTION
        list.cellRenderer = OverlayItemRenderer()

        list.addMouseListener(object : MouseAdapter() {
            override fun mouseClicked(e: MouseEvent) {
                if (e.clickCount == 2) {
                    selectCurrentItem(editor)
                }
            }
        })

        list.addKeyListener(object : KeyAdapter() {
            override fun keyPressed(e: KeyEvent) {
                when (e.keyCode) {
                    KeyEvent.VK_ENTER -> {
                        selectCurrentItem(editor)
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

    fun showOverlay(items: List<T>, x: Int, y: Int, parentComponent: JComponent, width: Int? = null, height: Int? = null) {
        listModel.clear()
        items.forEach { listModel.addElement(it) }

        showAtLocation(x, y, null,null, parentComponent)
        list.requestFocusInWindow()
    }

    private fun selectCurrentItem(editor: CodeEditor) {
        val selectedValue = list.selectedValue
        if (selectedValue != null) {
            selectedValue.execute(editor)
            makeInvisible()
        }
    }

    private inner class OverlayItemRenderer : DefaultListCellRenderer() {
        override fun getListCellRendererComponent(list: JList<*>?, value: Any?, index: Int, isSelected: Boolean, cellHasFocus: Boolean): Component {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus)
            foreground = (value as? EditorModification)?.severity?.toColor(editor.psiManager?.lang).toColor(UIStates.theme.get().codeLaF.getColor(CodeStyle.BASE0))
            background = UIStates.theme.get().globalLaF.bgPrimary
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