package prosim.uilib.styled.editor2

import emulator.kit.assembler.CodeStyle
import org.jetbrains.skia.Font
import org.jetbrains.skia.Paint
import prosim.uilib.UIStates
import prosim.uilib.styled.params.FontType

class EditorLAF {
    val fontCode: Font =  FontType.CODE.getSkiaFont()
    val fontMarkup: Font = FontType.CODE_INFO.getSkiaFont()
    val fgPaintSec: Paint = Paint().apply {
        color = UIStates.theme.get().textLaF.baseSecondary.rgb
        strokeWidth = 1f
        isAntiAlias = true
    }
    val fgPaint: Paint = Paint().apply {
        color = UIStates.theme.get().codeLaF.getColor(CodeStyle.BASE0).rgb
        strokeWidth = 2f
        isAntiAlias = true
    }
    val bgPaint: Paint = Paint().apply {
        color = UIStates.theme.get().globalLaF.bgPrimary.rgb
    }
    val selPaint: Paint = Paint().apply {
        color = UIStates.theme.get().codeLaF.selectionColor.rgb
    }
    val lineInset: Float = 2f
}
