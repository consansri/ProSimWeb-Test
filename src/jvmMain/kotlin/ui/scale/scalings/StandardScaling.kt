package me.c3.ui.scale.scalings

import me.c3.ui.scale.core.components.BorderScale
import me.c3.ui.scale.core.components.FontScale
import me.c3.ui.spacing.core.Scaling
import me.c3.ui.theme.core.spacing.ControlSpacing

class StandardScaling : Scaling {

    override val name: String = "standard"
    override val controlSpacing: ControlSpacing = ControlSpacing(28)
    override val fontScale: FontScale = FontScale(10f, 10f)
    override val borderScale: BorderScale = BorderScale(1)

}