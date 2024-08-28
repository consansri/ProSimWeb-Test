import androidx.compose.runtime.Composable
import androidx.compose.foundation.Image
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.group
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp




@Composable
private fun VectorPreview() {
    Image(Disassembler, null)
}

private var _Disassembler: ImageVector? = null

public val Disassembler: ImageVector
		get() {
			if (_Disassembler != null) {
				return _Disassembler!!
			}
_Disassembler = ImageVector.Builder(
                name = "Disassembler",
                defaultWidth = 90.71.dp,
                defaultHeight = 90.71.dp,
                viewportWidth = 90.71f,
                viewportHeight = 90.71f
            ).apply {
				group {
					group {
						path(
    						fill = SolidColor(Color.Black),
    						fillAlpha = 1.0f,
    						stroke = null,
    						strokeAlpha = 1.0f,
    						strokeLineWidth = 1.0f,
    						strokeLineCap = StrokeCap.Butt,
    						strokeLineJoin = StrokeJoin.Miter,
    						strokeLineMiter = 1.0f,
    						pathFillType = PathFillType.NonZero
						) {
							moveTo(0f, 0f)
							horizontalLineTo(90.71f)
							verticalLineTo(90.71f)
							horizontalLineTo(0f)
							verticalLineTo(0f)
							close()
}
						path(
    						fill = SolidColor(Color.Black),
    						fillAlpha = 1.0f,
    						stroke = SolidColor(Color(0xFF000000)),
    						strokeAlpha = 1.0f,
    						strokeLineWidth = 6f,
    						strokeLineCap = StrokeCap.Round,
    						strokeLineJoin = StrokeJoin.Round,
    						strokeLineMiter = 1.0f,
    						pathFillType = PathFillType.NonZero
						) {
							moveTo(72.08f, 55.29f)
							verticalLineTo(68.61f)
							arcToRelative(6.24f, 6.24f, 0f, isMoreThanHalf = false, isPositiveArc = true, -6.24f, 6.24f)
							horizontalLineTo(23.1f)
							arcToRelative(6.23f, 6.23f, 0f, isMoreThanHalf = false, isPositiveArc = true, -6.23f, -6.24f)
							verticalLineTo(25.86f)
							arcToRelative(6.23f, 6.23f, 0f, isMoreThanHalf = false, isPositiveArc = true, 6.23f, -6.23f)
							horizontalLineTo(36.42f)
							arcToRelative(6.23f, 6.23f, 0f, isMoreThanHalf = false, isPositiveArc = true, 6.23f, 6.23f)
							verticalLineToRelative(17f)
							arcToRelative(6.23f, 6.23f, 0f, isMoreThanHalf = false, isPositiveArc = false, 6.24f, 6.23f)
							horizontalLineToRelative(17f)
							arcTo(6.24f, 6.24f, 0f, isMoreThanHalf = false, isPositiveArc = true, 72.08f, 55.29f)
							close()
}
						path(
    						fill = SolidColor(Color.Black),
    						fillAlpha = 1.0f,
    						stroke = SolidColor(Color(0xFF000000)),
    						strokeAlpha = 1.0f,
    						strokeLineWidth = 6f,
    						strokeLineCap = StrokeCap.Round,
    						strokeLineJoin = StrokeJoin.Round,
    						strokeLineMiter = 1.0f,
    						pathFillType = PathFillType.NonZero
						) {
							moveTo(58.82f, 13.86f)
							horizontalLineTo(71.60000000000001f)
							arcTo(6.24f, 6.24f, 0f, isMoreThanHalf = false, isPositiveArc = true, 77.84f, 20.1f)
							verticalLineTo(32.88f)
							arcTo(6.24f, 6.24f, 0f, isMoreThanHalf = false, isPositiveArc = true, 71.60000000000001f, 39.120000000000005f)
							horizontalLineTo(58.82f)
							arcTo(6.24f, 6.24f, 0f, isMoreThanHalf = false, isPositiveArc = true, 52.58f, 32.88f)
							verticalLineTo(20.1f)
							arcTo(6.24f, 6.24f, 0f, isMoreThanHalf = false, isPositiveArc = true, 58.82f, 13.86f)
							close()
}
}
}
}.build()
return _Disassembler!!
		}

