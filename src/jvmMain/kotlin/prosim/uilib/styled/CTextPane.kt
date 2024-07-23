package prosim.uilib.styled

import emulator.kit.assembler.CodeStyle
import emulator.kit.install
import prosim.uilib.UIStates
import prosim.uilib.styled.params.FontType
import java.awt.Color
import java.awt.Font
import javax.swing.BorderFactory
import javax.swing.JScrollPane
import javax.swing.JTextPane
import javax.swing.border.Border
import javax.swing.text.SimpleAttributeSet

class CTextPane(val fontType: FontType = FontType.CODE) : JTextPane() {

    init {
        setUI(CTextPaneUI())
    }

    override fun setBounds(x: Int, y: Int, width: Int, height: Int) {
        val size = this.preferredSize
        super.setBounds(x, y, size.width.coerceAtLeast(width), height)
    }

    fun setInitialText(text: String) {
        this.document.remove(0, this.document.length)
        val attrs = SimpleAttributeSet()
        this.document.insertString(0, text, attrs)
    }

    fun createScrollPane(): CScrollPane {
        return CScrollPane(true, this, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS)
    }

    override fun getScrollableTracksViewportWidth(): Boolean {
        return false
    }

    override fun getFont(): Font {
        val font = fontType.getFont()
        font.install(this, UIStates.scale.get().fontScale.codeSize)
        return font
    }

    override fun getBorder(): Border {
        return BorderFactory.createEmptyBorder(0, UIStates.scale.get().borderScale.insets, 0, UIStates.scale.get().borderScale.insets)
    }

    override fun getBackground(): Color {
        return UIStates.theme.get().globalLaF.bgPrimary
    }

    override fun getCaretColor(): Color {
        return UIStates.theme.get().codeLaF.getColor(CodeStyle.BASE0)
    }

    override fun getForeground(): Color {
        return UIStates.theme.get().codeLaF.getColor(CodeStyle.BASE0)
    }


}