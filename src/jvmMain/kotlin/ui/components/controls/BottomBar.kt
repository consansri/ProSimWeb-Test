package me.c3.ui.components.controls

import emulator.kit.assembly.Compiler
import me.c3.ui.MainManager
import me.c3.ui.components.styled.CLabel
import me.c3.ui.components.styled.CPanel
import me.c3.ui.styled.params.BorderMode
import me.c3.ui.styled.params.FontType
import java.awt.GridBagConstraints
import java.awt.GridBagLayout

class BottomBar(private val mainManager: MainManager) : CPanel(mainManager.themeManager, mainManager.scaleManager, borderMode = BorderMode.NORTH) {

    val tagInfo = CLabel(mainManager.themeManager, mainManager.scaleManager, "Back to work? :D", FontType.BASIC)
    val editorInfo = CLabel(mainManager.themeManager, mainManager.scaleManager, "", FontType.CODE)
    val generalPurpose = CLabel(mainManager.themeManager,mainManager.scaleManager, "", FontType.CODE)

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

        add(generalPurpose, gbc)

        gbc.gridx = 2

        add(editorInfo, gbc)
    }

    fun setError(text: String){
        generalPurpose.setColouredText(text, mainManager.currTheme().codeLaF.getColor(Compiler.CodeStyle.RED))
    }

    fun setWarning(text: String){
        generalPurpose.setColouredText(text, mainManager.currTheme().codeLaF.getColor(Compiler.CodeStyle.YELLOW))
    }

    fun setInfo(text: String){
        generalPurpose.setColouredText(text, mainManager.currTheme().textLaF.baseSecondary)
    }
}