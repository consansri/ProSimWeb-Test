package me.c3.ui.components.controls

import emulator.kit.assembly.Compiler
import me.c3.ui.UIManager
import me.c3.ui.components.styled.CLabel
import me.c3.ui.components.styled.CPanel
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import javax.swing.JLabel
import javax.swing.SwingConstants

class BottomBar(private val uiManager: UIManager) : CPanel(uiManager) {

    val tagInfo = CLabel(uiManager, "tags")
    val editorInfo = CLabel(uiManager, "")
    val generalPurpose = CLabel(uiManager, "")

    init {
        layout = GridBagLayout()

        val gbc = GridBagConstraints()
        gbc.gridx = 0
        gbc.gridy = 0
        gbc.weighty = 0.0
        gbc.weightx = 1.0
        gbc.fill = GridBagConstraints.HORIZONTAL

        add(tagInfo, gbc)

        gbc.gridx = 1
        gbc.weightx = 0.0
        gbc.fill = GridBagConstraints.NONE

        add(editorInfo, gbc)

        gbc.gridx = 2

        add(generalPurpose, gbc)
    }

    fun setError(text: String){
        generalPurpose.setColouredText(text, uiManager.currTheme().codeLaF.getColor(Compiler.CodeStyle.RED))
    }

    fun setWarning(text: String){
        generalPurpose.setColouredText(text, uiManager.currTheme().codeLaF.getColor(Compiler.CodeStyle.YELLOW))
    }

    fun setInfo(text: String){
        generalPurpose.setColouredText(text, uiManager.currTheme().textLaF.baseSecondary)
    }


}