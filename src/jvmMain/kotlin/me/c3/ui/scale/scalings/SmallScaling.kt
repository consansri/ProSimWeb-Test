package me.c3.ui.scale.scalings

import me.c3.ui.scale.core.components.*
import me.c3.ui.spacing.core.Scaling

class SmallScaling : Scaling {
    override val name: String = "75%"
    override val controlScale: ControlScale = ControlScale(21, 14, 2, 3, 8, 96)
    override val fontScale: FontScale = FontScale(11f, 11f, 11f, 12f, 4)
    override val borderScale: BorderScale = BorderScale(1, 3, 4, 10)
    override val dividerScale: DividerScale = DividerScale(2)
    override val scrollScale: ScrollScale = ScrollScale(6)
    override val shadowScale: ShadowScale = ShadowScale(2)

    override fun toString(): String {
        return name
    }
}