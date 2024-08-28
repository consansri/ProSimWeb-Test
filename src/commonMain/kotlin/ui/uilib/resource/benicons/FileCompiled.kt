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
    Image(FileCompiled, null)
}

private var _FileCompiled: ImageVector? = null

public val FileCompiled: ImageVector
		get() {
			if (_FileCompiled != null) {
				return _FileCompiled!!
			}
_FileCompiled = ImageVector.Builder(
                name = "FileCompiled",
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
    						stroke = null,
    						strokeAlpha = 1.0f,
    						strokeLineWidth = 1.0f,
    						strokeLineCap = StrokeCap.Butt,
    						strokeLineJoin = StrokeJoin.Miter,
    						strokeLineMiter = 10f,
    						pathFillType = PathFillType.NonZero
						) {
							moveTo(65f, 47.91f)
							verticalLineTo(20.35f)
							arcToRelative(5.67f, 5.67f, 0f, isMoreThanHalf = false, isPositiveArc = false, -5.67f, -5.67f)
							horizontalLineTo(42f)
							arcToRelative(5.66f, 5.66f, 0f, isMoreThanHalf = false, isPositiveArc = false, -4f, 1.66f)
							lineTo(24.61f, 29.68f)
							arcToRelative(5.67f, 5.67f, 0f, isMoreThanHalf = false, isPositiveArc = false, -1.66f, 4f)
							verticalLineTo(69.36f)
							arcTo(5.67f, 5.67f, 0f, isMoreThanHalf = false, isPositiveArc = false, 28.62f, 75f)
							horizontalLineToRelative(9.2f)
}
						path(
    						fill = SolidColor(Color.Black),
    						fillAlpha = 1.0f,
    						stroke = null,
    						strokeAlpha = 1.0f,
    						strokeLineWidth = 1.0f,
    						strokeLineCap = StrokeCap.Butt,
    						strokeLineJoin = StrokeJoin.Miter,
    						strokeLineMiter = 10f,
    						pathFillType = PathFillType.NonZero
						) {
							moveTo(53.55f, 29.28f)
							lineTo(40.32f, 29.28f)
}
						path(
    						fill = SolidColor(Color.Black),
    						fillAlpha = 1.0f,
    						stroke = null,
    						strokeAlpha = 1.0f,
    						strokeLineWidth = 1.0f,
    						strokeLineCap = StrokeCap.Butt,
    						strokeLineJoin = StrokeJoin.Miter,
    						strokeLineMiter = 10f,
    						pathFillType = PathFillType.NonZero
						) {
							moveTo(53.55f, 43.39f)
							lineTo(34.34f, 43.39f)
}
						path(
    						fill = SolidColor(Color.Black),
    						fillAlpha = 1.0f,
    						stroke = null,
    						strokeAlpha = 1.0f,
    						strokeLineWidth = 1.0f,
    						strokeLineCap = StrokeCap.Butt,
    						strokeLineJoin = StrokeJoin.Miter,
    						strokeLineMiter = 10f,
    						pathFillType = PathFillType.NonZero
						) {
							moveTo(37.78f, 57.51f)
							lineTo(34.34f, 57.51f)
}
						path(
    						fill = SolidColor(Color.Black),
    						fillAlpha = 1.0f,
    						stroke = null,
    						strokeAlpha = 1.0f,
    						strokeLineWidth = 1.0f,
    						strokeLineCap = StrokeCap.Butt,
    						strokeLineJoin = StrokeJoin.Round,
    						strokeLineMiter = 1.0f,
    						pathFillType = PathFillType.NonZero
						) {
							moveTo(76.44f, 53.6f)
							lineTo(58.78f, 78.03f)
							lineTo(48.83f, 66.27f)
}
}
}
}.build()
return _FileCompiled!!
		}

