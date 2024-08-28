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
    Image(Home, null)
}

private var _Home: ImageVector? = null

public val Home: ImageVector
		get() {
			if (_Home != null) {
				return _Home!!
			}
_Home = ImageVector.Builder(
                name = "Home",
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
    						strokeLineCap = StrokeCap.Butt,
    						strokeLineJoin = StrokeJoin.Miter,
    						strokeLineMiter = 10f,
    						pathFillType = PathFillType.NonZero
						) {
							moveTo(76.68f, 36.75f)
							verticalLineTo(74.83f)
							arcTo(5.67f, 5.67f, 0f, isMoreThanHalf = false, isPositiveArc = true, 71f, 80.5f)
							horizontalLineTo(61f)
							arcToRelative(5.67f, 5.67f, 0f, isMoreThanHalf = false, isPositiveArc = true, -5.67f, -5.67f)
							verticalLineTo(56.32f)
							arcToRelative(10f, 10f, 0f, isMoreThanHalf = false, isPositiveArc = false, -10f, -10f)
							horizontalLineToRelative(0f)
							arcToRelative(10f, 10f, 0f, isMoreThanHalf = false, isPositiveArc = false, -10f, 10f)
							verticalLineTo(74.83f)
							arcTo(5.67f, 5.67f, 0f, isMoreThanHalf = false, isPositiveArc = true, 29.7f, 80.5f)
							horizontalLineToRelative(-10f)
							arcTo(5.67f, 5.67f, 0f, isMoreThanHalf = false, isPositiveArc = true, 14f, 74.83f)
							verticalLineTo(36.74f)
							arcToRelative(2.84f, 2.84f, 0f, isMoreThanHalf = false, isPositiveArc = true, 1f, -2.14f)
							lineToRelative(26.63f, -23f)
							arcToRelative(5.68f, 5.68f, 0f, isMoreThanHalf = false, isPositiveArc = true, 7.42f, 0f)
							lineToRelative(26.64f, 23f)
							arcTo(2.85f, 2.85f, 0f, isMoreThanHalf = false, isPositiveArc = true, 76.68f, 36.75f)
							close()
}
}
}
}.build()
return _Home!!
		}

