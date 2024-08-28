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
    Image(DeleteBlack, null)
}

private var _DeleteBlack: ImageVector? = null

public val DeleteBlack: ImageVector
		get() {
			if (_DeleteBlack != null) {
				return _DeleteBlack!!
			}
_DeleteBlack = ImageVector.Builder(
                name = "DeleteBlack",
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
							moveTo(31.759999999999998f, 26.07f)
							horizontalLineTo(58.94f)
							arcTo(5.67f, 5.67f, 0f, isMoreThanHalf = false, isPositiveArc = true, 64.61f, 31.740000000000002f)
							verticalLineTo(68.49f)
							arcTo(5.67f, 5.67f, 0f, isMoreThanHalf = false, isPositiveArc = true, 58.94f, 74.16f)
							horizontalLineTo(31.759999999999998f)
							arcTo(5.67f, 5.67f, 0f, isMoreThanHalf = false, isPositiveArc = true, 26.09f, 68.49f)
							verticalLineTo(31.740000000000002f)
							arcTo(5.67f, 5.67f, 0f, isMoreThanHalf = false, isPositiveArc = true, 31.759999999999998f, 26.07f)
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
							moveTo(34.65f, 35.24f)
							lineTo(34.65f, 64.99f)
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
							moveTo(45.35f, 35.24f)
							lineTo(45.35f, 64.99f)
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
							moveTo(56.06f, 35.24f)
							lineTo(56.06f, 64.99f)
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
							moveTo(29.72f, 16.55f)
							lineTo(60.99f, 16.55f)
}
}
}
}.build()
return _DeleteBlack!!
		}

