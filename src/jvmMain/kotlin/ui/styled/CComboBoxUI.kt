package me.c3.ui.styled

import me.c3.ui.UIManager
import me.c3.ui.components.styled.CIconButton
import java.awt.Color
import javax.swing.BorderFactory
import javax.swing.JButton
import javax.swing.JComponent
import javax.swing.plaf.basic.BasicComboBoxUI

class CComboBoxUI(private val uiManager: UIManager) : BasicComboBoxUI() {

    override fun installUI(c: JComponent?) {
        super.installUI(c)

        val comboBox = c as? CComboBox<*> ?: return

        comboBox.border = BorderFactory.createEmptyBorder()
        comboBox.background = Color(0,0,0,0)
    }

    override fun createArrowButton(): JButton {
        return CIconButton(uiManager, icon = uiManager.icons.folderOpen)
    }


}