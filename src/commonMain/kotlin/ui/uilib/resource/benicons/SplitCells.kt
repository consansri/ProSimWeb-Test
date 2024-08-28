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
    Image(SplitCells, null)
}

private var _SplitCells: ImageVector? = null

public val SplitCells: ImageVector
		get() {
			if (_SplitCells != null) {
				return _SplitCells!!
			}
_SplitCells = ImageVector.Builder(
                name = "SplitCells",
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
							moveTo(23.41f, 11.41f)
							horizontalLineTo(67.3f)
							arcTo(12f, 12f, 0f, isMoreThanHalf = false, isPositiveArc = true, 79.3f, 23.41f)
							verticalLineTo(67.3f)
							arcTo(12f, 12f, 0f, isMoreThanHalf = false, isPositiveArc = true, 67.3f, 79.3f)
							horizontalLineTo(23.41f)
							arcTo(12f, 12f, 0f, isMoreThanHalf = false, isPositiveArc = true, 11.41f, 67.3f)
							verticalLineTo(23.41f)
							arcTo(12f, 12f, 0f, isMoreThanHalf = false, isPositiveArc = true, 23.41f, 11.41f)
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
							moveTo(36.6f, 25.91f)
							verticalLineTo(64.79f)
							horizontalLineTo(25.91f)
							verticalLineTo(25.91f)
							horizontalLineTo(36.6f)
							moveToRelative(2.51f, -6f)
							horizontalLineTo(23.41f)
							arcToRelative(3.5f, 3.5f, 0f, isMoreThanHalf = false, isPositiveArc = false, -3.5f, 3.5f)
							verticalLineTo(67.3f)
							arcToRelative(3.5f, 3.5f, 0f, isMoreThanHalf = false, isPositiveArc = false, 3.5f, 3.49f)
							horizontalLineToRelative(15.7f)
							arcTo(3.49f, 3.49f, 0f, isMoreThanHalf = false, isPositiveArc = false, 42.6f, 67.3f)
							verticalLineTo(23.41f)
							arcToRelative(3.5f, 3.5f, 0f, isMoreThanHalf = false, isPositiveArc = false, -3.49f, -3.5f)
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
							moveTo(64.79f, 25.91f)
							verticalLineTo(64.79f)
							horizontalLineTo(54.1f)
							verticalLineTo(25.91f)
							horizontalLineTo(64.79f)
							moveToRelative(2.51f, -6f)
							horizontalLineTo(51.6f)
							arcToRelative(3.5f, 3.5f, 0f, isMoreThanHalf = false, isPositiveArc = false, -3.5f, 3.5f)
							verticalLineTo(67.3f)
							arcToRelative(3.5f, 3.5f, 0f, isMoreThanHalf = false, isPositiveArc = false, 3.5f, 3.49f)
							horizontalLineTo(67.3f)
							arcToRelative(3.49f, 3.49f, 0f, isMoreThanHalf = false, isPositiveArc = false, 3.49f, -3.49f)
							verticalLineTo(23.41f)
							arcToRelative(3.5f, 3.5f, 0f, isMoreThanHalf = false, isPositiveArc = false, -3.49f, -3.5f)
							close()
}
}
}
}.build()
return _SplitCells!!
		}

