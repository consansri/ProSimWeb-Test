package prosim.uilib.styled

import prosim.uilib.UIStates
import prosim.uilib.styled.params.BorderMode
import java.awt.BorderLayout
import java.awt.Dimension
import javax.swing.JComponent
import javax.swing.border.Border

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
        layout = BorderLayout()

        this.setUI(CPanelUI())
        this.revalidate()
        this.repaint()
    }

    override fun getPreferredSize(): Dimension {
        return layout.preferredLayoutSize(this)
    }

    override fun getBorder(): Border {
        return if(isOverlay) UIStates.scale.get().borderScale.getInsetBorder() else  borderMode.getBorder()
    }

}