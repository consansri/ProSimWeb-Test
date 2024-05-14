package me.c3.ui.styled

import com.formdev.flatlaf.extras.FlatSVGIcon
import com.formdev.flatlaf.extras.FlatSVGIcon.ColorFilter
import me.c3.ui.styled.CIconButton.Mode
import me.c3.ui.scale.ScaleManager
import me.c3.ui.theme.ThemeManager
import java.awt.Color
import javax.swing.JLabel

class CIcon(private val themeManager: ThemeManager, private val scaleManager: ScaleManager, val svgIcon: FlatSVGIcon, val mode: Mode = Mode.PRIMARY_NORMAL) : JLabel() {

    init {
        themeManager.addThemeChangeListener {
            setDefaults()
        }

        scaleManager.addScaleChangeEvent {
            setDefaults()
        }

        setDefaults()
    }

    private fun setDefaults() {
        isOpaque = false
        background = Color(0, 0, 0, 0)
        border = when (mode) {
            Mode.PRIMARY_NORMAL, Mode.SECONDARY_NORMAL, Mode.GRADIENT_NORMAL -> scaleManager.curr.controlScale.getNormalInsetBorder()
            Mode.GRADIENT_SMALL, Mode.SECONDARY_SMALL, Mode.PRIMARY_SMALL -> scaleManager.curr.controlScale.getSmallInsetBorder()
        }

        svgIcon.colorFilter = ColorFilter {
            when (mode) {
                Mode.PRIMARY_NORMAL, Mode.PRIMARY_SMALL -> themeManager.curr.iconLaF.iconFgPrimary
                Mode.SECONDARY_NORMAL, Mode.SECONDARY_SMALL -> themeManager.curr.iconLaF.iconFgSecondary
                Mode.GRADIENT_NORMAL, Mode.GRADIENT_SMALL -> null
            }
        }

        val size = when (mode) {
            Mode.PRIMARY_SMALL, Mode.SECONDARY_SMALL, Mode.GRADIENT_SMALL -> scaleManager.curr.controlScale.smallSize
            Mode.PRIMARY_NORMAL, Mode.SECONDARY_NORMAL, Mode.GRADIENT_NORMAL -> scaleManager.curr.controlScale.normalSize
        }

        icon = svgIcon.derive(size, size)
    }


}