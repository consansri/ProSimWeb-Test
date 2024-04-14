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

class CTextField(themeManager: ThemeManager, scaleManager: ScaleManager, mode: Type) : JTextField() {

    init {
        this.setUI(CTextFieldUI(themeManager, scaleManager, mode))
    }

}