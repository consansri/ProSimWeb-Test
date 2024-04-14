package me.c3.ui.styled

import com.formdev.flatlaf.extras.FlatSVGIcon
import me.c3.ui.components.styled.CIconButton
import me.c3.ui.components.styled.CPanel
import me.c3.ui.spacing.ScaleManager
import me.c3.ui.theme.ThemeManager
import java.awt.GridBagConstraints
import java.awt.GridBagLayout

class CIconInput(themeManager: ThemeManager, scaleManager: ScaleManager, icon: FlatSVGIcon, fieldType: CTextFieldUI.Type, primary: Boolean = false) : CPanel(themeManager, scaleManager, primary, roundCorners = true, borderMode = BorderMode.BASIC) {

    val button = CIconButton(themeManager, scaleManager, icon)
    val input = CTextField(themeManager, scaleManager, fieldType)

    init {
        attachComponents()
    }

    private fun attachComponents(){
        layout = GridBagLayout()

        val gbc = GridBagConstraints()
        gbc.gridx = 0
        gbc.gridy = 0
        gbc.weightx = 1.0
        gbc.weighty = 0.0
        gbc.fill = GridBagConstraints.HORIZONTAL

        add(button, gbc)

        gbc.gridx = 1
        add(input, gbc)
    }



}