package prosim.uilib.styled

import prosim.uilib.styled.params.FontType
import javax.swing.BorderFactory
import javax.swing.JComponent
import javax.swing.SwingConstants
import javax.swing.plaf.basic.BasicFormattedTextFieldUI

class CFormattedTextFieldUI(val fontType: FontType) : BasicFormattedTextFieldUI() {

    override fun installUI(c: JComponent?) {
        super.installUI(c)

        val tf = c as? CFormattedTextField ?: return
        tf.horizontalAlignment = SwingConstants.CENTER
        tf.border = BorderFactory.createEmptyBorder()
        tf.isOpaque = false
    }
}