import androidx.compose.runtime.Composable
import androidx.compose.foundation.Image
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp




@Composable
private fun VectorPreview() {
    Image(Bars, null)
}

private var _Bars: ImageVector? = null

public val Bars: ImageVector
		get() {
			if (_Bars != null) {
				return _Bars!!
			}
_Bars = ImageVector.Builder(
                name = "Bars",
                defaultWidth = 800.dp,
                defaultHeight = 800.dp,
                viewportWidth = 24f,
                viewportHeight = 24f
            ).apply {
				path(
    				fill = SolidColor(Color(0xFF000000)),
    				fillAlpha = 1.0f,
    				stroke = null,
    				strokeAlpha = 1.0f,
    				strokeLineWidth = 1.0f,
    				strokeLineCap = StrokeCap.Butt,
    				strokeLineJoin = StrokeJoin.Miter,
    				strokeLineMiter = 1.0f,
    				pathFillType = PathFillType.NonZero
				) {
					moveTo(19f, 12.75f)
					horizontalLineTo(5f)
					curveTo(4.8011f, 12.75f, 4.6103f, 12.671f, 4.4697f, 12.5303f)
					curveTo(4.329f, 12.3897f, 4.25f, 12.1989f, 4.25f, 12f)
					curveTo(4.25f, 11.8011f, 4.329f, 11.6103f, 4.4697f, 11.4697f)
					curveTo(4.6103f, 11.329f, 4.8011f, 11.25f, 5f, 11.25f)
					horizontalLineTo(19f)
					curveTo(19.1989f, 11.25f, 19.3897f, 11.329f, 19.5303f, 11.4697f)
					curveTo(19.671f, 11.6103f, 19.75f, 11.8011f, 19.75f, 12f)
					curveTo(19.75f, 12.1989f, 19.671f, 12.3897f, 19.5303f, 12.5303f)
					curveTo(19.3897f, 12.671f, 19.1989f, 12.75f, 19f, 12.75f)
					close()
}
				path(
    				fill = SolidColor(Color(0xFF000000)),
    				fillAlpha = 1.0f,
    				stroke = null,
    				strokeAlpha = 1.0f,
    				strokeLineWidth = 1.0f,
    				strokeLineCap = StrokeCap.Butt,
    				strokeLineJoin = StrokeJoin.Miter,
    				strokeLineMiter = 1.0f,
    				pathFillType = PathFillType.NonZero
				) {
					moveTo(19f, 8.25f)
					horizontalLineTo(5f)
					curveTo(4.8011f, 8.25f, 4.6103f, 8.171f, 4.4697f, 8.0303f)
					curveTo(4.329f, 7.8897f, 4.25f, 7.6989f, 4.25f, 7.5f)
					curveTo(4.25f, 7.3011f, 4.329f, 7.1103f, 4.4697f, 6.9697f)
					curveTo(4.6103f, 6.829f, 4.8011f, 6.75f, 5f, 6.75f)
					horizontalLineTo(19f)
					curveTo(19.1989f, 6.75f, 19.3897f, 6.829f, 19.5303f, 6.9697f)
					curveTo(19.671f, 7.1103f, 19.75f, 7.3011f, 19.75f, 7.5f)
					curveTo(19.75f, 7.6989f, 19.671f, 7.8897f, 19.5303f, 8.0303f)
					curveTo(19.3897f, 8.171f, 19.1989f, 8.25f, 19f, 8.25f)
					close()
}
				path(
    				fill = SolidColor(Color(0xFF000000)),
    				fillAlpha = 1.0f,
    				stroke = null,
    				strokeAlpha = 1.0f,
    				strokeLineWidth = 1.0f,
    				strokeLineCap = StrokeCap.Butt,
    				strokeLineJoin = StrokeJoin.Miter,
    				strokeLineMiter = 1.0f,
    				pathFillType = PathFillType.NonZero
				) {
					moveTo(19f, 17.25f)
					horizontalLineTo(5f)
					curveTo(4.8011f, 17.25f, 4.6103f, 17.171f, 4.4697f, 17.0303f)
					curveTo(4.329f, 16.8897f, 4.25f, 16.6989f, 4.25f, 16.5f)
					curveTo(4.25f, 16.3011f, 4.329f, 16.1103f, 4.4697f, 15.9697f)
					curveTo(4.6103f, 15.829f, 4.8011f, 15.75f, 5f, 15.75f)
					horizontalLineTo(19f)
					curveTo(19.1989f, 15.75f, 19.3897f, 15.829f, 19.5303f, 15.9697f)
					curveTo(19.671f, 16.1103f, 19.75f, 16.3011f, 19.75f, 16.5f)
					curveTo(19.75f, 16.6989f, 19.671f, 16.8897f, 19.5303f, 17.0303f)
					curveTo(19.3897f, 17.171f, 19.1989f, 17.25f, 19f, 17.25f)
					close()
}
}.build()
return _Bars!!
		}

