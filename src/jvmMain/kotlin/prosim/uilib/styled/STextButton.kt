package prosim.uilib.styled

import org.jetbrains.skia.Canvas
import org.jetbrains.skia.Font
import org.jetbrains.skia.Paint
import org.jetbrains.skia.RRect
import prosim.uilib.UIStates
import prosim.uilib.resource.Icons
import prosim.uilib.scale.core.Scaling
import prosim.skialib.core.SComponent
import prosim.uilib.styled.params.FontType
import prosim.uilib.theme.core.Theme
import java.awt.Color
import java.awt.Dimension
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent

open class STextButton(val text: String, val fontType: FontType, installHover: Boolean = true, val locationPrimary: Boolean = false) : SComponent() {
    private var skiaFont: Font = fontType.getSkiaFont()
    private var isHovered: Boolean = false
        set(value) {
            field = value
            repaint()
        }
    private var hoverColor: Color = UIStates.theme.get().iconLaF.iconBgHover

    init {
        transparency = true
        if (installHover) installHoverEffect()
        setup()
    }

    fun addActionListener(event: (MouseEvent) -> Unit) {
        addMouseListener(object : MouseAdapter() {
            override fun mouseClicked(e: MouseEvent) {
                event(e)
            }
        })
    }

    private fun installHoverEffect() {
        addMouseListener(object : MouseAdapter() {
            override fun mouseEntered(e: MouseEvent?) {
                isHovered = true
            }

            override fun mouseExited(e: MouseEvent?) {
                isHovered = false
            }
        })
    }

    override fun setDefaults(theme: Theme, scaling: Scaling, icons: Icons) {
        transparency = true
        isFocusable = true
        skiaFont = fontType.getSkiaFont()
        border = scaling.borderScale.getInsetBorder()
        foreground = theme.textLaF.base
        background = if (locationPrimary) theme.globalLaF.bgPrimary else theme.globalLaF.bgSecondary
        hoverColor = UIStates.theme.get().iconLaF.iconBgHover
    }

    override fun onRender(canvas: Canvas, width: Int, height: Int, nanoTime: Long) {
        val fgPaint = Paint().apply {
            color = foreground.rgb
        }

        canvas.clear(background.rgb)

        if (isHovered) {
            val hoverPaint = Paint().apply {
                color = hoverColor.rgb
            }

            val rect = RRect.makeXYWH(
                insets.left.toFloat(),
                insets.top.toFloat(),
                width.toFloat() - insets.left - insets.right,
                height.toFloat() - insets.top - insets.bottom,
                UIStates.scale.get().borderScale.cornerRadius.toFloat() / 2
            )

            canvas.drawRRect(rect, hoverPaint)
        }

        font?.let {
            // Calculate Text Position
            val textRect = skiaFont.measureText(text, fgPaint)
            val x = (width - textRect.width) / 2
            val y = (height - textRect.height) / 2 - textRect.top

            // Draw Text
            canvas.drawString(text, x, y, skiaFont, fgPaint)
        }
    }

    override fun getPreferredSize(): Dimension {
        return Dimension(insets.left + insets.right + skiaFont.measureText(text).toIRect().width, insets.top + insets.bottom + skiaFont.measureText(text).toIRect().height)
    }
}