package me.c3.ui.spacing.core

import me.c3.ui.scale.core.components.BorderScale
import me.c3.ui.scale.core.components.ControlScale
import me.c3.ui.scale.core.components.DividerScale
import me.c3.ui.scale.core.components.FontScale

interface Scaling {

    val name: String

    val controlScale: ControlScale
    val fontScale: FontScale
    val borderScale: BorderScale
    val dividerScale: DividerScale

}