package prosim.uilib.styled

import cengine.editor.EditorModification
import prosim.uilib.UIStates
import prosim.uilib.styled.params.BorderMode
import prosim.uilib.styled.params.FontType
import java.awt.Color
import java.awt.Component
import javax.swing.JList
import javax.swing.ListCellRenderer
import javax.swing.ListModel
import javax.swing.SwingConstants

class CList<T>(dataModel: ListModel<T>, val primary: Boolean = false) : JList<T>(dataModel) {

    init {
        cellRenderer = OverlayItemRenderer<T>()
    }

    override fun getBackground(): Color {
        return if (primary) UIStates.theme.get().COLOR_BG_0 else UIStates.theme.get().COLOR_BG_1
    }

    open class OverlayItemRenderer<T> : CLabel("", FontType.CODE, BorderMode.NONE), ListCellRenderer<T> {
        val selectedBGColor: Color get() = UIStates.theme.get().COLOR_SELECTION

        init {
            horizontalAlignment = SwingConstants.LEFT
            isOpaque = true
            isFocusable = true
        }

        override fun getListCellRendererComponent(list: JList<out T>?, value: T, index: Int, isSelected: Boolean, cellHasFocus: Boolean): Component {
            customBG = if (isSelected || cellHasFocus) selectedBGColor else null
            text = (value as? EditorModification)?.displayText ?: value.toString()
            return this
        }
    }
}