package prosim.uilib.styled

import prosim.uilib.styled.params.BorderMode
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.plaf.basic.BasicPanelUI

open class CPanel( primary: Boolean = false, borderMode: BorderMode = BorderMode.NONE, roundCorners: Boolean = false, val isOverlay: Boolean = false) : JComponent() {



    var roundedCorners: Boolean = roundCorners
        set(value) {
            field = value
            revalidate()
            repaint()
        }
    var primary: Boolean = primary
        set(value) {
            field = value
            revalidate()
            repaint()
        }

    var borderMode: BorderMode = borderMode
        set(value) {
            field = value
            revalidate()
            repaint()
        }

    init {
        this.setUI(CPanelUI())

        val ui = BasicPanelUI()

        val panel = JPanel().preferredSize
    }
}