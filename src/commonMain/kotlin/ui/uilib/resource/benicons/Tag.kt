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
    Image(Tag, null)
}

private var _Tag: ImageVector? = null

public val Tag: ImageVector
		get() {
			if (_Tag != null) {
				return _Tag!!
			}
_Tag = ImageVector.Builder(
                name = "Tag",
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
    						strokeLineJoin = StrokeJoin.Round,
    						strokeLineMiter = 1.0f,
    						pathFillType = PathFillType.NonZero
						) {
							moveTo(74.38f, 41f)
							lineTo(39f, 76.44f)
							arcToRelative(5.67f, 5.67f, 0f, isMoreThanHalf = false, isPositiveArc = true, -8f, 0f)
							lineTo(15.21f, 60.69f)
							arcToRelative(5.7f, 5.7f, 0f, isMoreThanHalf = false, isPositiveArc = true, 0f, -8f)
							lineToRelative(35.4f, -35.4f)
							horizontalLineTo(66.42f)
							lineToRelative(8f, 8f)
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
							moveTo(64.6f, 31.9f)
							arcTo(4.84f, 4.84f, 0f, isMoreThanHalf = false, isPositiveArc = true, 59.76f, 36.739999999999995f)
							arcTo(4.84f, 4.84f, 0f, isMoreThanHalf = false, isPositiveArc = true, 54.92f, 31.9f)
							arcTo(4.84f, 4.84f, 0f, isMoreThanHalf = false, isPositiveArc = true, 64.6f, 31.9f)
							close()
}
}
}
}.build()
return _Tag!!
		}

