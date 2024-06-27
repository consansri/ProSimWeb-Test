package prosim.uilib.scale.scalings

import prosim.uilib.scale.core.Scaling
import prosim.uilib.scale.core.components.*

class StandardScaling : Scaling {
    override val name: String = "100%"
    override val controlScale: ControlScale = ControlScale(28, 18, 2, 4, 10, 128)
    override val fontScale: FontScale = FontScale(14f, 14f, 14f, 16f, 4)
    override val borderScale: BorderScale = BorderScale(1, 3, 4, 10)
    override val dividerScale: DividerScale = DividerScale(3)
    override val scrollScale: ScrollScale = ScrollScale(8)
    override val shadowScale: ShadowScale = ShadowScale(2)

    override fun toString(): String {
        return name
    }
}