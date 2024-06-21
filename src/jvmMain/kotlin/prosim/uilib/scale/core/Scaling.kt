package prosim.uilib.scale.core

import prosim.uilib.scale.core.components.*

interface Scaling {

    val name: String

    val controlScale: ControlScale
    val fontScale: FontScale
    val borderScale: BorderScale
    val dividerScale: DividerScale
    val scrollScale: ScrollScale
    val shadowScale: ShadowScale

}