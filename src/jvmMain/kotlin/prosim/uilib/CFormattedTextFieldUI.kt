package prosim.uilib

import prosim.uilib.styled.CFormattedTextField
import prosim.uilib.styled.params.FontType
import java.awt.Color
import java.lang.ref.WeakReference
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

        UIStates.theme.addEvent(WeakReference(tf)) { _ ->
            setDefaults(tf)
        }

        UIStates.scale.addEvent(WeakReference(tf)) { _ ->
            setDefaults(tf)
        }

        setDefaults(tf)
    }

    private fun setDefaults(tf: CFormattedTextField) {
        tf.isOpaque = false
        tf.font = fontType.getFont()
        tf.caretColor = UIStates.theme.get().textLaF.base
        updateTextColors(tf)
    }

    fun updateTextColors(tf: CFormattedTextField) {
        val customFG = tf.customFG
        val customBG = tf.customBG
        tf.background = customBG ?: Color(0, 0, 0, 0)
        tf.foreground = customFG ?: UIStates.theme.get().textLaF.base
    }


}