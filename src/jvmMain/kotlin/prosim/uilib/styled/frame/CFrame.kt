package prosim.uilib.styled.frame

import prosim.uilib.UIStates
import prosim.uilib.styled.CPanel
import java.awt.Color
import java.awt.Image
import javax.swing.JFrame

open class CFrame(title: String? = null): JFrame() {
    val themeListener = UIStates.theme.createAndAddListener {
        revalidate()
        repaint()
    }

    val scaleListener = UIStates.scale.createAndAddListener {
        revalidate()
        repaint()
    }

    init {
        this.title = title
        this.revalidate()
        this.defaultCloseOperation = EXIT_ON_CLOSE
        this.contentPane = CPanel(primary = true)
    }

    override fun getBackground(): Color {
        return UIStates.theme.get().COLOR_BG_0
    }

    override fun getForeground(): Color {
        return UIStates.theme.get().COLOR_FG_0
    }

    override fun getIconImage(): Image {
        return UIStates.icon.get().appLogo.image
    }

}