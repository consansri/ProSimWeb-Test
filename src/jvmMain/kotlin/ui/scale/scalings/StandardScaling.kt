package me.c3.ui.scale.scalings

import me.c3.ui.scale.core.components.BorderScale
import me.c3.ui.scale.core.components.ControlScale
import me.c3.ui.scale.core.components.DividerScale
import me.c3.ui.scale.core.components.FontScale
import me.c3.ui.spacing.core.Scaling

class StandardScaling : Scaling {
    override val name: String = "standard"
    override val controlScale: ControlScale = ControlScale(28, 18, 2, 4, 10, 128)
    override val fontScale: FontScale = FontScale(14f, 14f, 14f, 15f)
    override val borderScale: BorderScale = BorderScale(1, 3, 4, 10)
    override val dividerScale: DividerScale = DividerScale(2)
}