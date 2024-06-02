package me.c3.ui.styled

import com.formdev.flatlaf.extras.FlatSVGIcon
import me.c3.ui.styled.params.BorderMode
import me.c3.ui.styled.params.FontType
import java.awt.GridBagConstraints
import java.awt.GridBagLayout

class CIconInput(icon: FlatSVGIcon, fontType: FontType, primary: Boolean = false) : CPanel( primary, roundCorners = true, borderMode = BorderMode.THICKNESS) {

    val button = CIconButton( icon)
    val input = CTextField( fontType)

    init {
        attachComponents()
    }

    private fun attachComponents() {
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