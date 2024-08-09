package prosim.uilib.styled.button

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import prosim.uilib.styled.COptionPane
import prosim.uilib.styled.CPanel
import prosim.uilib.styled.CTextButton
import prosim.uilib.styled.CTextField
import prosim.uilib.styled.params.FontType
import java.awt.Dimension
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.io.File

class CDirectoryChooser : CPanel() {
    val currDirectory = CTextField(FontType.BASIC).apply {
        isEditable = false
    }

    val changeDirectory = CTextButton("select").apply {
        addActionListener {
            CoroutineScope(Dispatchers.Default).launch {
                val result = COptionPane.showDirectoryChooser(this@CDirectoryChooser, "Select Project Directory").await()
                selectedDirectory = result
            }
        }
    }

    var selectedDirectory: File? = null
        set(value) {
            field = value
            currDirectory.text = selectedDirectory?.path ?: "-"
            currDirectory.revalidate()
            currDirectory.repaint()
        }

    init {
        layout = GridBagLayout()
        val gbc = GridBagConstraints()
        gbc.gridx = 0
        gbc.weightx = 1.0
        gbc.fill = GridBagConstraints.HORIZONTAL
        add(currDirectory, gbc)
        gbc.gridx = 1
        gbc.weightx = 0.0
        gbc.fill = GridBagConstraints.NONE
        add(changeDirectory, gbc)

        minimumSize = Dimension(300, changeDirectory.minimumSize.height)
    }

}