package prosim.uilib.scale.scalings

import prosim.uilib.scale.core.Scaling
import prosim.uilib.scale.core.components.*

class LargeScaling: Scaling {
    override val name: String = "150%"
    override val controlScale: ControlScale = ControlScale(32, 24, 2, 4, 10, 128)
    override val fontScale: FontScale = FontScale(21f, 21f, 21f, 24f, 4)
    override val borderScale: BorderScale = BorderScale(2, 4, 6, 15)
    override val dividerScale: DividerScale = DividerScale(4)
    override val scrollScale: ScrollScale = ScrollScale(12)
    override val shadowScale: ShadowScale = ShadowScale(3)

    override fun toString(): String {
        return name
    }
}