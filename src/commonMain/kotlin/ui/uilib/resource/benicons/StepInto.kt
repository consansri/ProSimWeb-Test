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
    Image(StepInto, null)
}

private var _StepInto: ImageVector? = null

public val StepInto: ImageVector
		get() {
			if (_StepInto != null) {
				return _StepInto!!
			}
_StepInto = ImageVector.Builder(
                name = "StepInto",
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
    						strokeLineJoin = StrokeJoin.Miter,
    						strokeLineMiter = 10f,
    						pathFillType = PathFillType.NonZero
						) {
							moveTo(21f, 16.13f)
							horizontalLineTo(41.09f)
							curveToRelative(2.35f, 0f, 4.26f, 2.95f, 4.26f, 6.6f)
							verticalLineTo(53.79f)
}
						path(
    						fill = SolidColor(Color.Black),
    						fillAlpha = 1.0f,
    						stroke = SolidColor(Color(0xFF000000)),
    						strokeAlpha = 1.0f,
    						strokeLineWidth = 6f,
    						strokeLineCap = StrokeCap.Round,
    						strokeLineJoin = StrokeJoin.Miter,
    						strokeLineMiter = 10f,
    						pathFillType = PathFillType.NonZero
						) {
							moveTo(45.35f, 53.79f)
							lineTo(60.02f, 39.13f)
}
						path(
    						fill = SolidColor(Color.Black),
    						fillAlpha = 1.0f,
    						stroke = SolidColor(Color(0xFF000000)),
    						strokeAlpha = 1.0f,
    						strokeLineWidth = 6f,
    						strokeLineCap = StrokeCap.Round,
    						strokeLineJoin = StrokeJoin.Miter,
    						strokeLineMiter = 10f,
    						pathFillType = PathFillType.NonZero
						) {
							moveTo(30.69f, 39.13f)
							lineTo(45.35f, 53.79f)
}
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
							moveTo(51.64f, 67.79f)
							arcTo(6.29f, 6.29f, 0f, isMoreThanHalf = false, isPositiveArc = true, 45.35f, 74.08000000000001f)
							arcTo(6.29f, 6.29f, 0f, isMoreThanHalf = false, isPositiveArc = true, 39.06f, 67.79f)
							arcTo(6.29f, 6.29f, 0f, isMoreThanHalf = false, isPositiveArc = true, 51.64f, 67.79f)
							close()
}
}
}
}.build()
return _StepInto!!
		}

