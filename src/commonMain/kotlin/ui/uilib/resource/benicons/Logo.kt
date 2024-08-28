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
    Image(Logo, null)
}

private var _Logo: ImageVector? = null

public val Logo: ImageVector
		get() {
			if (_Logo != null) {
				return _Logo!!
			}
_Logo = ImageVector.Builder(
                name = "Logo",
                defaultWidth = 100.dp,
                defaultHeight = 100.dp,
                viewportWidth = 100f,
                viewportHeight = 100f
            ).apply {
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
						horizontalLineTo(100f)
						verticalLineTo(100f)
						horizontalLineTo(0f)
						verticalLineTo(0f)
						close()
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
						moveTo(10f, 90f)
						lineTo(90f, 10f)
}
}
}.build()
return _Logo!!
		}

