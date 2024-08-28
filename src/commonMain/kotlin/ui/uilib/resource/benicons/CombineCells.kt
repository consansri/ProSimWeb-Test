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
    Image(CombineCells, null)
}

private var _CombineCells: ImageVector? = null

public val CombineCells: ImageVector
		get() {
			if (_CombineCells != null) {
				return _CombineCells!!
			}
_CombineCells = ImageVector.Builder(
                name = "CombineCells",
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
							moveTo(64.79f, 25.92f)
							verticalLineTo(64.79f)
							horizontalLineTo(25.92f)
							verticalLineTo(25.92f)
							horizontalLineTo(64.79f)
							moveToRelative(2.51f, -6f)
							horizontalLineTo(23.41f)
							arcToRelative(3.49f, 3.49f, 0f, isMoreThanHalf = false, isPositiveArc = false, -3.49f, 3.49f)
							verticalLineTo(67.3f)
							arcToRelative(3.49f, 3.49f, 0f, isMoreThanHalf = false, isPositiveArc = false, 3.49f, 3.49f)
							horizontalLineTo(67.3f)
							arcToRelative(3.49f, 3.49f, 0f, isMoreThanHalf = false, isPositiveArc = false, 3.49f, -3.49f)
							verticalLineTo(23.41f)
							arcToRelative(3.49f, 3.49f, 0f, isMoreThanHalf = false, isPositiveArc = false, -3.49f, -3.49f)
							close()
}
}
}
}.build()
return _CombineCells!!
		}

