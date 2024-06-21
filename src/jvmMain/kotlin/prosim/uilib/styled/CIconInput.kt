package prosim.uilib.styled

import com.formdev.flatlaf.extras.FlatSVGIcon
import prosim.uilib.styled.params.BorderMode
import prosim.uilib.styled.params.FontType
import java.awt.GridBagConstraints
import java.awt.GridBagLayout

class CIconInput(icon: FlatSVGIcon, fontType: FontType, primary: Boolean = false) : prosim.uilib.styled.CPanel( primary, roundCorners = true, borderMode = BorderMode.THICKNESS) {

    val button = prosim.uilib.styled.CIconButton(icon)
    val input = prosim.uilib.styled.CTextField(fontType)

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