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
    Image(StatusError, null)
}

private var _StatusError: ImageVector? = null

public val StatusError: ImageVector
		get() {
			if (_StatusError != null) {
				return _StatusError!!
			}
_StatusError = ImageVector.Builder(
                name = "StatusError",
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
    						stroke = null,
    						strokeAlpha = 1.0f,
    						strokeLineWidth = 1.0f,
    						strokeLineCap = StrokeCap.Butt,
    						strokeLineJoin = StrokeJoin.Miter,
    						strokeLineMiter = 1.0f,
    						pathFillType = PathFillType.NonZero
						) {
							moveTo(50.19f, 71.3f)
							arcTo(4.84f, 4.84f, 0f, isMoreThanHalf = false, isPositiveArc = true, 45.35f, 76.14f)
							arcTo(4.84f, 4.84f, 0f, isMoreThanHalf = false, isPositiveArc = true, 40.510000000000005f, 71.3f)
							arcTo(4.84f, 4.84f, 0f, isMoreThanHalf = false, isPositiveArc = true, 50.19f, 71.3f)
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
							moveTo(45.35f, 17.12f)
							lineTo(45.35f, 57.48f)
}
}
}
}.build()
return _StatusError!!
		}

