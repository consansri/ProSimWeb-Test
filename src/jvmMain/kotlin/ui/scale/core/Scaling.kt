package me.c3.ui.spacing.core

import me.c3.ui.scale.core.components.*

interface Scaling {

    val name: String

    val controlScale: ControlScale
    val fontScale: FontScale
    val borderScale: BorderScale
    val dividerScale: DividerScale
    val scrollScale: ScrollScale

}