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
    Image(Edit, null)
}

private var _Edit: ImageVector? = null

public val Edit: ImageVector
		get() {
			if (_Edit != null) {
				return _Edit!!
			}
_Edit = ImageVector.Builder(
                name = "Edit",
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
							moveTo(0f, 90.71f)
							lineTo(0f, 0f)
							lineTo(90.71f, 0f)
							lineTo(90.71f, 90.71f)
}
}
					group {
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
							moveTo(35.95f, 25.18f)
							horizontalLineTo(52.58f)
							verticalLineTo(67.7f)
							horizontalLineTo(35.95f)
							verticalLineTo(25.18f)
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
							moveTo(35.11f, 67.35f)
							lineTo(18.65f, 75.93f)
							arcToRelative(2.87f, 2.87f, 0f, isMoreThanHalf = false, isPositiveArc = true, -3.87f, -3.88f)
							lineTo(23.36f, 55.6f)
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
							moveTo(66.34f, 14.41f)
							horizontalLineToRelative(0f)
							arcToRelative(9.86f, 9.86f, 0f, isMoreThanHalf = false, isPositiveArc = true, 9.86f, 9.86f)
							verticalLineTo(34.33f)
							arcToRelative(0f, 0f, 0f, isMoreThanHalf = false, isPositiveArc = true, 0f, 0f)
							horizontalLineTo(56.49f)
							arcToRelative(0f, 0f, 0f, isMoreThanHalf = false, isPositiveArc = true, 0f, 0f)
							verticalLineTo(24.26f)
							arcToRelative(9.86f, 9.86f, 0f, isMoreThanHalf = false, isPositiveArc = true, 9.86f, -9.86f)
							close()
}
}
}
}.build()
return _Edit!!
		}

