package me.c3.ui.styled

import me.c3.ui.UIManager
import me.c3.ui.components.editor.CDocument
import me.c3.ui.spacing.ScaleManager
import me.c3.ui.theme.ThemeManager
import javax.swing.JTextField
import javax.swing.text.AbstractDocument
import javax.swing.text.AttributeSet
import javax.swing.text.DocumentFilter
import me.c3.ui.styled.CTextFieldUI.Type
import javax.swing.text.DefaultStyledDocument

class CTextField(themeManager: ThemeManager, scaleManager: ScaleManager, mode: Type) : JTextField("3", 5) {

    init {
        this.setUI(CTextFieldUI(themeManager, scaleManager, mode))

        document = CDocument()

        (document as? AbstractDocument)?.documentFilter = object : DocumentFilter() {
            override fun insertString(fb: FilterBypass?, offset: Int, string: String, attr: AttributeSet?) {
                if (mode.inputRegex != null) {
                    if (string.matches(mode.inputRegex)) {
                        super.insertString(fb, offset, string, attr)
                    }
                } else {
                    super.insertString(fb, offset, string, attr)
                }
            }

            override fun replace(fb: FilterBypass?, offset: Int, length: Int, text: String, attrs: AttributeSet?) {
                if (mode.inputRegex != null) {
                    if (text.matches(mode.inputRegex)) {
                        super.insertString(fb, offset, text, attrs)
                    }
                } else {
                    super.insertString(fb, offset, text, attrs)
                }
                super.replace(fb, offset, length, text, attrs)
            }
        }
    }

}