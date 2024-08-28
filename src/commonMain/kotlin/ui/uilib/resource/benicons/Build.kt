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
    Image(Build, null)
}

private var _Build: ImageVector? = null

public val Build: ImageVector
		get() {
			if (_Build != null) {
				return _Build!!
			}
_Build = ImageVector.Builder(
                name = "Build",
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
							moveTo(90.71f, 0f)
							lineTo(90.71f, 90.71f)
							lineTo(0f, 90.71f)
							lineTo(0f, 0f)
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
    						strokeLineJoin = StrokeJoin.Miter,
    						strokeLineMiter = 10f,
    						pathFillType = PathFillType.NonZero
						) {
							moveTo(20.37f, 60.76f)
							horizontalLineToRelative(28.8f)
							arcToRelative(0f, 0f, 0f, isMoreThanHalf = false, isPositiveArc = true, 0f, 0f)
							verticalLineToRelative(15.6f)
							arcToRelative(0f, 0f, 0f, isMoreThanHalf = false, isPositiveArc = true, 0f, 0f)
							horizontalLineTo(26f)
							arcToRelative(5.67f, 5.67f, 0f, isMoreThanHalf = false, isPositiveArc = true, -5.67f, -5.67f)
							verticalLineTo(60.76f)
							arcToRelative(0f, 0f, 0f, isMoreThanHalf = false, isPositiveArc = true, 0f, 0f)
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
							moveTo(49.17f, 60.76f)
							horizontalLineTo(78f)
							arcToRelative(0f, 0f, 0f, isMoreThanHalf = false, isPositiveArc = true, 0f, 0f)
							verticalLineToRelative(9.93f)
							arcToRelative(5.67f, 5.67f, 0f, isMoreThanHalf = false, isPositiveArc = true, -5.67f, 5.67f)
							horizontalLineTo(49.17f)
							arcToRelative(0f, 0f, 0f, isMoreThanHalf = false, isPositiveArc = true, 0f, 0f)
							verticalLineTo(60.76f)
							arcTo(0f, 0f, 0f, isMoreThanHalf = false, isPositiveArc = true, 49.17f, 60.76f)
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
							moveTo(34.77f, 45.16f)
							horizontalLineTo(63.57000000000001f)
							verticalLineTo(60.76f)
							horizontalLineTo(34.77f)
							verticalLineTo(45.16f)
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
							moveTo(54.84f, 29.56f)
							horizontalLineTo(72.3f)
							arcTo(5.67f, 5.67f, 0f, isMoreThanHalf = false, isPositiveArc = true, 78f, 35.23f)
							verticalLineToRelative(9.93f)
							arcToRelative(0f, 0f, 0f, isMoreThanHalf = false, isPositiveArc = true, 0f, 0f)
							horizontalLineTo(49.17f)
							arcToRelative(0f, 0f, 0f, isMoreThanHalf = false, isPositiveArc = true, 0f, 0f)
							verticalLineTo(35.23f)
							arcTo(5.67f, 5.67f, 0f, isMoreThanHalf = false, isPositiveArc = true, 54.84f, 29.56f)
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
							moveTo(15.99f, 18.87f)
							horizontalLineTo(33.45f)
							arcTo(5.67f, 5.67f, 0f, isMoreThanHalf = false, isPositiveArc = true, 39.120000000000005f, 24.54f)
							verticalLineTo(28.799999999999997f)
							arcTo(5.67f, 5.67f, 0f, isMoreThanHalf = false, isPositiveArc = true, 33.45f, 34.47f)
							horizontalLineTo(15.99f)
							arcTo(5.67f, 5.67f, 0f, isMoreThanHalf = false, isPositiveArc = true, 10.32f, 28.799999999999997f)
							verticalLineTo(24.54f)
							arcTo(5.67f, 5.67f, 0f, isMoreThanHalf = false, isPositiveArc = true, 15.99f, 18.87f)
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
							moveTo(63.57f, 45.16f)
							horizontalLineTo(77.97f)
							verticalLineTo(60.76f)
							horizontalLineTo(63.57f)
							verticalLineTo(45.16f)
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
							moveTo(26f, 45.16f)
							horizontalLineToRelative(8.73f)
							arcToRelative(0f, 0f, 0f, isMoreThanHalf = false, isPositiveArc = true, 0f, 0f)
							verticalLineToRelative(15.6f)
							arcToRelative(0f, 0f, 0f, isMoreThanHalf = false, isPositiveArc = true, 0f, 0f)
							horizontalLineTo(20.37f)
							arcToRelative(0f, 0f, 0f, isMoreThanHalf = false, isPositiveArc = true, 0f, 0f)
							verticalLineTo(50.83f)
							arcTo(5.67f, 5.67f, 0f, isMoreThanHalf = false, isPositiveArc = true, 26f, 45.16f)
							close()
}
}
}
}.build()
return _Build!!
		}

