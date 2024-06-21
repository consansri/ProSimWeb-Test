package prosim.uilib.scale.core.components

import java.awt.Insets
import javax.swing.BorderFactory
import javax.swing.border.Border

data class BorderScale(
    val thickness: Int,
    val markedThickness: Int,
    val insets: Int,
    val cornerRadius: Int
) {
    fun getInsetBorder(): Border = BorderFactory.createEmptyBorder(insets, insets, insets, insets)
    fun getThicknessBorder(): Border = BorderFactory.createEmptyBorder(thickness, thickness, thickness, thickness)
    fun getInsets(): Insets = Insets(insets, insets, insets, insets)
}