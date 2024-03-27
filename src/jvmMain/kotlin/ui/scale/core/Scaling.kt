package me.c3.ui.spacing.core

import me.c3.ui.scale.core.components.BorderScale
import me.c3.ui.scale.core.components.FontScale
import me.c3.ui.theme.core.spacing.ControlSpacing

interface Scaling {

    val name: String

    val controlSpacing: ControlSpacing
    val fontScale: FontScale
    val borderScale: BorderScale

}