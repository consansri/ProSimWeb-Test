package me.c3.ui.styled

import com.formdev.flatlaf.extras.FlatSVGIcon
import me.c3.ui.scale.ScaleManager
import me.c3.ui.styled.params.BorderMode
import me.c3.ui.styled.params.FontType
import me.c3.ui.theme.ThemeManager
import java.awt.GridBagConstraints
import java.awt.GridBagLayout

class CIconInput(tm: ThemeManager, sm: ScaleManager, icon: FlatSVGIcon, fontType: FontType, primary: Boolean = false) : CPanel(tm, sm, primary, roundCorners = true, borderMode = BorderMode.THICKNESS) {

    val button = CIconButton(tm, sm, icon)
    val input = CTextField(tm, sm, fontType).apply {
    }

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